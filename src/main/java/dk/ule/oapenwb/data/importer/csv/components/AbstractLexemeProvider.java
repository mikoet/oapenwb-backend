// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.data.importer.csv.CsvImporterContext;
import dk.ule.oapenwb.data.importer.csv.CsvRowBasedImporter;
import dk.ule.oapenwb.data.importer.messages.MessageType;
import dk.ule.oapenwb.entity.basis.ApiAction;
import dk.ule.oapenwb.entity.content.basedata.Language;
import dk.ule.oapenwb.entity.content.lexemes.LexemeForm;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.logic.admin.lexeme.LexemeDetailedDTO;
import dk.ule.oapenwb.util.HibernateUtil;
import lombok.Getter;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.LongType;

import java.util.*;
import java.util.stream.Collectors;

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
		List<Variant> variants)
	{
		LexemeDetailedDTO result = new LexemeDetailedDTO();

		// # Retrieve some data that'll be needed
		// Retrieve the language instance, the tagNames and
		Language language = context.getLanguages().get(this.getLang());
		Set<String> tagNames = context.getConfig().getTagNames();
		Set<Long> variantIDs = variants.stream().map(Variant::getId).collect(Collectors.toSet());

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

		// Create a common default sememe
		{
			Sememe sememe = new Sememe();
			sememe.setId(-1L);
			sememe.setInternalName("$default");
			sememe.setVariantIDs(variantIDs); // TODO Past dat sou?
			sememe.setFillSpec(Sememe.FILL_SPEC_NONE);

			sememe.setActive(true);
			sememe.setApiAction(ApiAction.Insert);
			sememe.setChanged(true);
			result.setSememes(List.of(sememe));
		}

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
		if (variants == null || variants.size() == 0) {
			// Nothing to look up
			return null;
		}

		int langID = detailedDTO.getLexeme().getLangID();
		int typeID = detailedDTO.getLexeme().getTypeID();

		// For each variant check if it may already exist in the database
		// and collect the IDs of the lexemes of the already existing variants.
		Set<Long> allLexemeIDs = new HashSet<>();
		for (var variant : variants) {
			NativeQuery<?> checkQuery = createCheckQuery(langID, typeID, variant);
			List<Long> rows = HibernateUtil.listAndCast(checkQuery);
			if (rows.size() > 0) {
				Set<Long> lexemeIDs = new HashSet<>(rows);
				allLexemeIDs.addAll(lexemeIDs);
				context.getMessages().add(this.messageContext, MessageType.Debug,
					String.format("Specified variant '%s' already exists in database", variant.getLexemeForms().get(0)),
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
	protected NativeQuery<?> createCheckQuery(int langID, int typeID, Variant variant)
	{
		// Q901
		String sb = """
			SELECT distinct l.id AS lexemeID FROM Lexemes l
			  INNER JOIN Variants v ON l.id = v.lexemeID
			  INNER JOIN LexemeForms lf ON v.id = lf.variantID
			  WHERE l.langID = :langID AND l.typeID = :typeID
			    AND lf.text = :text AND lf.formTypeID = :formTypeID""";

		// Create the query and set parameters
		Session session = HibernateUtil.getSession();
		NativeQuery<?> query = session.createSQLQuery(sb)
			.addScalar("lexemeID", new LongType());

		// Get the first lexeme form
		LexemeForm form1 = variant.getLexemeForms().get(0);
		int formTypeID = form1.getFormTypeID();

		query.setParameter("langID", langID);
		query.setParameter("typeID", typeID);
		query.setParameter("formTypeID", formTypeID);
		query.setParameter("text", form1.getText());

		return query;
	}
}
