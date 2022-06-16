// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.data.importer.csv.CsvImporterContext;
import dk.ule.oapenwb.data.importer.csv.CsvRowBasedImporter;
import dk.ule.oapenwb.data.importer.messages.MessageType;
import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.entity.basis.ApiAction;
import dk.ule.oapenwb.entity.content.basedata.Language;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lemma;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.logic.admin.lexeme.LexemeDetailedDTO;
import dk.ule.oapenwb.util.HibernateUtil;
import lombok.Data;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.LongType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>Provides one {@link LexemeDetailedDTO} that will be build by 0..n {@link VariantBuilder}s,
 * or loaded from the database if one of the built variants already exists.</p>
 */
@Data
public class LexemeProvider
{
	private final AdminControllers adminControllers;
	private final String lang;
	private final String messageContext;
	private List<VariantBuilder> variantBuilders = new LinkedList<>();

	public LexemeProvider(AdminControllers adminControllers, String lang)
	{
		this.adminControllers = adminControllers;
		this.lang = lang;
		this.messageContext = String.format("Lexeme Provider '%s'", lang);
	}

	public LexemeDetailedDTO provide(
		CsvImporterContext context,
		CsvRowBasedImporter.TypeFormPair typeFormPair,
		RowData rowData)
	{
		LexemeDetailedDTO detailedDTO = createDTO(context, typeFormPair,
			buildVariants(context, typeFormPair, rowData));
		LexemeDetailedDTO otherDetailedDTO;
		if ((otherDetailedDTO = lookup(context, rowData.getLineNumber(), detailedDTO.getVariants())) !=  null) {
			// At least one variant already exists
			context.getMessages().add(messageContext, MessageType.Info,
				"Lexeme from database is being used", rowData.getLineNumber(), -1);
			detailedDTO = otherDetailedDTO;
		}

		return detailedDTO;
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

	private List<Variant> buildVariants(CsvImporterContext context, CsvRowBasedImporter.TypeFormPair typeFormPair, RowData rowData)
	{
		List<Variant> variantList = new LinkedList<>();
		for (var builder : variantBuilders) {
			variantList.addAll(builder.build(context, typeFormPair, rowData));
		}
		return variantList;
	}

	// 2. kyken of dat mind. eyn van de bouwden varianten al geaven deit
	// TODO Müs eygens de lexemeID leaveren soudännig wat dat lexemeDTO laden warden kan
	private LexemeDetailedDTO lookup(CsvImporterContext context, int lineNumber, List<Variant> variants)
	{
		// For each variant check if it may already exist in the database
		// and collect the IDs of the lexemes of the already existing variants.
		Set<Long> allLexemeIDs = new HashSet<>();
		for (var variant : variants) {
			NativeQuery<?> checkQuery = createCheckQuery(variant);
			List<Object[]> rows = HibernateUtil.listAndCast(checkQuery);
			if (rows.size() > 0) {
				Set<Long> lexemeIDs = new HashSet<>();
				for (Object[] row : rows) {
					lexemeIDs.add((Long) row[0]);
				}
				allLexemeIDs.addAll(lexemeIDs);
				context.getMessages().add(this.messageContext, MessageType.Warning,
					String.format("Specified variant '%s' already exists in database", variant.getLemma()),
					lineNumber, -1);
			}
		}

		if (allLexemeIDs.size() == 1) {
			// Load the existing lexeme
			try {
				return adminControllers.getLexemesController().get(allLexemeIDs.stream().findFirst().get());
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

	private NativeQuery<?> createCheckQuery(Variant variant)
	{
		// Q901
		String sb = """
			SELECT distinct l.id AS id FROM Lexemes l
			  INNER JOIN Variants v ON l.id = v.lexemeID
			  WHERE v.pre = :pre AND v.main = :main AND v.post = :post AND v.also = :also""";

		// Create the query and set parameters
		Session session = HibernateUtil.getSession();
		NativeQuery<?> query = session.createSQLQuery(sb)
			.addScalar("id", new LongType());
		Lemma lemma = variant.getLemma();
		query.setParameter("pre", lemma.getPre());
		query.setParameter("main", lemma.getMain());
		query.setParameter("post", lemma.getPost());
		query.setParameter("also", lemma.getAlso());

		return query;
	}
}
