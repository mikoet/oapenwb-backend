// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.sheet.lexemecreator;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.data.importer.sheet.SheetFileImporter;
import dk.ule.oapenwb.entity.basis.ApiAction;
import dk.ule.oapenwb.entity.content.lexemes.LexemeForm;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lemma;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.logic.admin.lexeme.LexemeDetailedDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AbstractCreator
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractCreator.class);

	private static long NEXT_VARIANT_ID = -1L;

	protected final AdminControllers adminControllers;
	protected final SheetFileImporter.TypeFormPair typeFormsPair;

	public AbstractCreator(AdminControllers adminControllers, SheetFileImporter.TypeFormPair typeFormsPair)
	{
		this.adminControllers = adminControllers;
		this.typeFormsPair = typeFormsPair;
	}

	protected LexemeDetailedDTO createDTO(CreationConfig config, List<Variant> variants)
	{
		//SheetFileImporter.TypeFormPair typeFormsPair = this.typeFormMap.get(pos);
		if (typeFormsPair == null) {
			LOG.warn("Entry in line {} utilises unknown PoS '{}' and is skipped", config.getLineNo(), config.getPos());
			return null;
		}

		LexemeDetailedDTO result = new LexemeDetailedDTO();

		// Create the lexeme itself
		{
			Lexeme lexeme = new Lexeme();
			lexeme.setLangID(config.getLanguage().getId());
			lexeme.setTypeID(typeFormsPair.getLeft().getId());
			lexeme.getTags().add("imported");
			if (config.getTagName() != null && !config.getTagName().isEmpty()) {
				lexeme.getTags().add(config.getTagName());
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
			sememe.setVariantIDs(Set.of(-1L)); // TODO Hyr müs ik de IDs to vaten krygen
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

	// TODO Shouldn't the name simply be createVariant?
	protected Variant createLexemeFormsAndVariant(CreationConfig config, List<LexemeForm> lexemeForms, int orthographyID)
	{
		// Create the lemma
		Lemma lemma = new Lemma();
		lemma.setFillLemma(Lemma.FILL_LEMMA_AUTOMATICALLY);

		long variantID;
		synchronized (this.getClass()) {
			variantID = NEXT_VARIANT_ID--;
		}

		// Finally create the variant
		Variant variant = new Variant();
		variant.setId(variantID);
		variant.setOrthographyID(orthographyID);
		variant.setLexemeForms(lexemeForms);
		variant.setLemma(lemma);

		variant.setActive(true);
		variant.setApiAction(ApiAction.Insert);
		variant.setChanged(true);

		return variant;
	}

	protected LexemeForm createLexemeForm(int formTypeID, String text)
	{
		LexemeForm form = new LexemeForm();
		form.setState(LexemeForm.STATE_TYPED);
		form.setFormTypeID(formTypeID);
		form.setText(text);

		return form;
	}
}
