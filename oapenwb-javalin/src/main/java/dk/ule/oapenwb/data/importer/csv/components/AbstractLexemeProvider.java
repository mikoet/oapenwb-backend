// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.data.importer.csv.CsvImporterContext;
import dk.ule.oapenwb.data.importer.csv.CsvRowBasedImporter;
import dk.ule.oapenwb.data.importer.csv.components.sememecreators.DefaultSememeCreator;
import dk.ule.oapenwb.data.importer.csv.components.sememecreators.ISememeCreator;
import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.data.importer.messages.MessageType;
import dk.ule.oapenwb.logic.admin.lexeme.LexemeDetailedDTO;
import dk.ule.oapenwb.persistency.entity.ApiAction;
import dk.ule.oapenwb.persistency.entity.content.basedata.Language;
import dk.ule.oapenwb.persistency.entity.content.lexemes.LexemeForm;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.util.HibernateUtil;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;

import java.util.*;

public class AbstractLexemeProvider
{
	protected final AdminControllers adminControllers;

	@Getter
	protected final String lang;

	@Getter
	protected final boolean mustProvide;

	@Getter
	protected final String messageContext;

	@Getter
	protected List<VariantBuilder> variantBuilders = new LinkedList<>();

	@Getter
	@Setter
	protected ISememeCreator sememeCreator = new DefaultSememeCreator();

	public AbstractLexemeProvider(
		AdminControllers adminControllers,
		String lang,
		boolean mustProvide,
		String messageContext)
	{
		this.adminControllers = adminControllers;
		this.lang = lang;
		this.mustProvide = mustProvide;
		this.messageContext = messageContext;
	}

	/**
	 * <p>This method has to be called before the first call of method build() in order to initialise the creators
	 * with their necessary type information.</p>
	 *
	 * @param typeFormMap the TypeFormMap containing the LexemeType definitions for each LexemeType (PoS)
	 */
	public void initialise(CsvRowBasedImporter.TypeFormMap typeFormMap)
	{
		for (var builder : variantBuilders) {
			builder.initialise(typeFormMap);
		}
	}

	protected LexemeDetailedDTO createDTO(
		CsvImporterContext context,
		CsvRowBasedImporter.TypeFormPair typeFormPair,
		List<Variant> variants,
		RowData rowData)
	{
		LexemeDetailedDTO result = new LexemeDetailedDTO();

		// # Retrieve some data that'll be needed
		// Retrieve the language instance, the tagNames, variantIDs and all dialectIDs used in the variants
		Language language = context.getLanguages().get(this.getLang());
		Set<String> tagNames = context.getConfig().getTagNames();
		Set<Long> variantIDs = new HashSet<>();
		Set<Integer> dialectIDs = new HashSet<>();
		for (Variant variant : variants) {
			variantIDs.add(variant.getId());
			if (variant.getDialectIDs() != null) {
				dialectIDs.addAll(variant.getDialectIDs());
			}
		}

		// # Build the Lexeme data
		// Create the lexeme itself
		{
			Lexeme lexeme = new Lexeme();
			lexeme.setLangID(language.getId());
			lexeme.setTypeID(typeFormPair.getLeft().getId());
			// This tag will always be added
			lexeme.getTags().add("imported");
			if (tagNames != null && !tagNames.isEmpty()) {
				lexeme.getTags().addAll(tagNames);
			}
			lexeme.setCreatorID(null);
			//lexeme.getProperties().put("import-frequency", frequency);
			// TODO Skul ik hyr wat anders skryven? Dat originale lemma, or sou?

			lexeme.setActive(true);
			lexeme.setApiAction(ApiAction.Insert);
			lexeme.setChanged(true);

			result.setLexeme(lexeme);
		}

		// Set the variants
		result.setVariants(variants);

		// Create a common sememe
		result.setSememes(List.of(
			this.sememeCreator.create(context, rowData, variantIDs, dialectIDs)
		));

		result.setMappings(new ArrayList<>());
		result.setLinks(new ArrayList<>());

		return result;
	}

	/**
	 * Kikt of dat mind. eyn van de bouwden varianten al geaven deit in de databank.
	 *
	 * @param context the importer context of current run
	 * @param lineNumber line number the lexeme was read from
	 * @param detailedDTO the detailed lexeme instance
	 * @return a lexeme is returned if something was found on the DB, else null will be returned
	 */
	protected LexemeDetailedDTO lookup(CsvImporterContext context, int lineNumber, LexemeDetailedDTO detailedDTO)
	{
		List<Variant> variants = detailedDTO.getVariants();
		if (variants == null || variants.isEmpty()) {
			// Nothing to look up
			return null;
		}

		int langID = detailedDTO.getLexeme().getLangID();
		int typeID = detailedDTO.getLexeme().getTypeID();

		// For each variant check if it may already exist in the database
		// and collect the IDs of the lexemes of the already existing variants.
		Set<Long> allLexemeIDs = new HashSet<>();
		for (var variant : variants) {
			final NativeQuery<Object> checkQuery = createCheckQuery(langID, typeID, variant);
			final List<Long> rows = HibernateUtil.listAndCast(checkQuery);

			if (!rows.isEmpty()) {
				Set<Long> lexemeIDs = new HashSet<>(rows);
				allLexemeIDs.addAll(lexemeIDs);
				context.getMessages().add(this.messageContext, MessageType.Debug,
					String.format("Specified variant '%s' already exists in database", variant.getLexemeForms().getFirst()),
					lineNumber, -1);
			}
		}

		if (allLexemeIDs.size() == 1) {
			// Load the existing lexeme
			try {
				LexemeDetailedDTO lookedUpDTO =
					adminControllers.getLexemesController().get(allLexemeIDs.stream().findFirst().get());
				// This step is essential: before returning the instance the lookedUpDTO's ID must be added to the
				// context's loadedLexemeIDs set in order to not get persisted again
				context.getLoadedLexemeIDs().add(lookedUpDTO.getLexeme().getId());
				return lookedUpDTO;
			} catch (CodeException e) {
				context.getMessages().add(messageContext, MessageType.Error,
					String.format("Existing lexeme could not be loaded: %s", e.getMessage()), lineNumber, -1);
				throw new RuntimeException("Aborting import of this row");
			}
		} else if (allLexemeIDs.size() > 1) {
			// Error since it cannot be known which lexeme to load when there are
			// multiple ones available
			context.getMessages().add(messageContext, MessageType.Error,
				"Multiple existing lexemes were found for the specified variant(s)", lineNumber, -1);
			throw new RuntimeException("Aborting import of this row");
		}

		return null;
	}

	/**
	 * <p>Creates the query to look up if a variant for a specific language and type already exists.
	 * Could be overridden.</p>
	 *
	 * @param langID ID of the lexeme's language
	 * @param typeID ID of the lexeme's type
	 * @param variant variant to check for existence
	 * @return the NativeQuery instance
	 */
	protected NativeQuery<Object> createCheckQuery(int langID, int typeID, Variant variant)
	{
		// Q901
		final String sb = """
			SELECT distinct l.id AS lexemeID FROM Lexemes l
			  INNER JOIN Variants v ON l.id = v.lexemeID
			  INNER JOIN LexemeForms lf ON v.id = lf.variantID
			  WHERE l.langID = :langID AND l.typeID = :typeID
			    AND lf.text = :text AND lf.formTypeID = :formTypeID""";

		// Create the query and set parameters
		final Session session = HibernateUtil.getSession();
		final NativeQuery<Object> query = session.createNativeQuery(sb, Object.class)
			.addScalar("lexemeID", StandardBasicTypes.LONG);

		// Get the first lexeme form
		LexemeForm form1 = variant.getLexemeForms().getFirst();
		int formTypeID = form1.getFormTypeID();

		query.setParameter("langID", langID);
		query.setParameter("typeID", typeID);
		query.setParameter("formTypeID", formTypeID);
		query.setParameter("text", form1.getText());

		return query;
	}
}
