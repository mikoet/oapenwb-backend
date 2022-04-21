// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.lexeme;

import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.base.error.IMessage;
import dk.ule.oapenwb.base.error.Message;
import dk.ule.oapenwb.base.error.MultiCodeException;
import dk.ule.oapenwb.entity.basis.ApiAction;
import dk.ule.oapenwb.entity.content.basedata.LangPair;
import dk.ule.oapenwb.entity.content.basedata.LemmaTemplate;
import dk.ule.oapenwb.entity.content.basedata.LexemeFormType;
import dk.ule.oapenwb.entity.content.lexemes.LexemeForm;
import dk.ule.oapenwb.entity.content.lexemes.Mapping;
import dk.ule.oapenwb.entity.content.lexemes.SynGroup;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.logic.admin.LangPairsController;
import dk.ule.oapenwb.logic.admin.TagsController;
import dk.ule.oapenwb.logic.admin.generic.CGEntityController;
import dk.ule.oapenwb.logic.admin.lexeme.sememe.SememesController;
import dk.ule.oapenwb.logic.admin.syngroup.SynGroupsController;
import dk.ule.oapenwb.logic.context.Context;
import dk.ule.oapenwb.util.CurrentUser;
import dk.ule.oapenwb.util.HibernateUtil;
import dk.ule.oapenwb.util.Pair;
import dk.ule.oapenwb.util.ValidationUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.validation.ConstraintViolation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The LexemeCreator checks and – if OK – persists a freshly supplied {@link LexemeDetailedDTO}.
 */
public class LexemeCreator
{
	private final CGEntityController<LexemeFormType, Integer, Integer> lftController;
	private final CGEntityController<LemmaTemplate, Integer, Integer> ltController;
	private final TagsController tagsController;
	private final SynGroupsController synGroupsController;
	private final LangPairsController langPairsController;
	private final LexemesController lexemesController;
	private final SememesController sememesController;

	// Maps to map the internal, negative IDs to the real given IDs after persisting
	private final Map<Long, Long> variantIdMapping = new HashMap<>();
	private final Map<Long, Long> sememeIdMapping = new HashMap<>();
	private final Map<Long, Long> mappingIdMapping = new HashMap<>();

	public LexemeCreator(
		final CGEntityController<LexemeFormType, Integer, Integer> lftController,
		final CGEntityController<LemmaTemplate, Integer, Integer> ltController,
		final TagsController tagsController, final SynGroupsController synGroupsController,
		final LangPairsController langPairsController, final LexemesController lexemesController,
		final SememesController sememesController)
	{
		this.lftController = lftController;
		this.ltController = ltController;
		this.tagsController = tagsController;
		this.synGroupsController = synGroupsController;
		this.langPairsController = langPairsController;
		this.lexemesController = lexemesController;
		this.sememesController = sememesController;
	}

	public LexemeSlimDTO create(final Session session, final LexemeDetailedDTO lexemeDTO) throws CodeException, MultiCodeException
	{
		// Bean validation
		Set<ConstraintViolation<LexemeDetailedDTO>> violations = ValidationUtil.getValidator().validate(lexemeDTO);
		if (violations.size() > 0) {
			List<IMessage> errors = violations.stream().map(
				vio -> new Message(ErrorCode.Admin_UnknownError.getCode(),
					vio.getPropertyPath().toString() + vio.getMessage(), null)).collect(Collectors.toList());
			throw new MultiCodeException(errors);
		}

		// Check the lexeme's content, substructures (and do some auto-correction)
		new LexemeDetailDTOChecker(lftController, lexemeDTO, LexemeDetailDTOChecker.Operation.Create).check();

		// Persist the lexeme and retrieve the ID of it
		Lexeme lexeme = lexemeDTO.getLexeme();
		if (lexeme.getId() != null) {
			throw new CodeException(ErrorCode.Admin_EntityIdSetInCreate,
				Collections.singletonList(new Pair<>("type", "Lexeme")));
		}
		lexeme.setCreatorID(CurrentUser.INSTANCE.get());
		lexeme.setChanged(false);
		if (lexeme.getParserID() != null && lexeme.getParserID().isEmpty()) {
			lexeme.setParserID(null);
		}
		session.save(lexeme);

		// Handle the tags
		handleTags(session, lexeme.getTags());

		// Persist the variants including their lexeme forms
		persistVariants(session, lexemeDTO);

		// The LemmaTemplateProcessor will build the lemmas for all variants and also save the variants again
		LemmaTemplateProcessor lemmaTemplateProcessor = new LemmaTemplateProcessor(session, lexemeDTO, ltController, lftController);
		lemmaTemplateProcessor.buildLemmata();

		// Persist the sememes
		persistSememes(session, lexemeDTO);

		// Persist the mappings
		persistMappings(session, lexemeDTO);

		// Create the result that will be sent to the client
		return new LexemeSlimDTO(lexeme,
			lexemeDTO.getVariants().stream().filter(variant -> variant.isMainVariant()).findFirst().get());
	}

	private void handleTags(final Session session, final Set<String> tags) throws CodeException {
		if (tags != null) {
			for (String tag : tags) {
				tagsController.useTag(session, tag);
			}
		}
	}

	/**
	 * Will persist the LexemeForms and also set the variantID when needed.
	 *
	 * @param session
	 * @param variant
	 */
	private void persistLexemeForms(final Session session, final Variant variant)
	{
		if (variant.getLexemeForms() == null) {
			return;
		}
		/*
		 * TODO Berücksichtigen:
		 * - Welche LexemeFormen liegen geschützt in der DB bereits vor? Wichtig für Update, aber nicht Create.
		 */

		List<LexemeForm> newList = new LinkedList<>();
		for (final LexemeForm form : variant.getLexemeForms()) {
			if (form.getText() == null || form.getText().isEmpty()) {
				//
				continue;
			}
			form.setVariantID(variant.getId());
			form.setState(LexemeForm.STATE_TYPED);
			session.persist(form);
			newList.add(form);
		}
		variant.setLexemeForms(newList);
	}

	private void persistVariants(final Session session, final LexemeDetailedDTO lexemeDTO)
	{
		if (lexemeDTO.getVariants() == null) {
			return;
		}

		for (final Variant variant : lexemeDTO.getVariants()) {
			if (variant.getApiAction().equals(ApiAction.Delete)) {
				continue;
			} else if (variant.getId() != null && variant.getId() > 0) {
				throw new RuntimeException("Variant that is marked as new already has an ID.");
			} else if (variant.getId() == null || (variant.getId() != null && variant.getId() == 0)) {
				throw new RuntimeException("Variant that is marked as new has not gotten an internal ID.");
			}
			Long internalID = variant.getId();
			variant.setId(null);
			variant.setLexemeID(lexemeDTO.getLexeme().getId());
			variant.setChanged(false);
			variant.setCreatorID(CurrentUser.INSTANCE.get());
			session.save(variant);
			this.variantIdMapping.put(internalID, variant.getId());
			persistLexemeForms(session, variant);
		}
	}

	private void persistSememes(final Session session, final LexemeDetailedDTO lexemeDTO) throws CodeException
	{
		if (lexemeDTO.getSememes() == null) {
			return;
		}

		for (final Sememe sememe : lexemeDTO.getSememes()) {
			if (sememe.getApiAction().equals(ApiAction.Delete)) {
				continue;
			} else if (sememe.getId() != null && sememe.getId() > 0) {
				throw new RuntimeException("Sememe that is marked as new already has an ID.");
			} else if (sememe.getId() == null || (sememe.getId() != null && sememe.getId() == 0)) {
				throw new RuntimeException("Sememe that is marked as new has not gotten an internal ID.");
			}
			Long internalID = sememe.getId();
			sememe.setId(null);
			sememe.setLexemeID(lexemeDTO.getLexeme().getId());
			// Remap internal IDs to real IDs
			if (sememe.getVariantIDs() != null) {
				sememe.setVariantIDs(sememe.getVariantIDs().stream().map(id -> id < 0 ? this.variantIdMapping.get(id) : id)
										   .collect(Collectors.toCollection(LinkedHashSet::new)));
			}
			sememe.setChanged(false);
			sememe.setCreatorID(CurrentUser.INSTANCE.get());
			session.save(sememe);
			this.sememeIdMapping.put(internalID, sememe.getId());
		}

		// Flush current data into the database so they can be found in further queries
		session.flush();

		// After flush persist the SynGroups
		for (final Sememe sememe : lexemeDTO.getSememes()) {
			persistSynGroup(sememe);
		}

		// Save the sememes once more...
		for (final Sememe sememe : lexemeDTO.getSememes()) {
			session.save(sememe);
		}
	}

	// persist a SynGroup that is set on a sememe. It will also set the synGroupID on that sememe.
	private void persistSynGroup(final Sememe sememe) throws CodeException
	{
		final SynGroup synGroup = sememe.getSynGroup();
		if (synGroup == null) {
			sememe.setSynGroupID(null);
			// Niks to doon
			return;
		}
		/*
		 * Quiet special functionality: When the SynGroup is to be newly created the synGroupID must also be set
		 * on the sememe of the foreign lexeme that was used to create the new SynGroup.
		 */
		Long otherSememeID = null;
		if (synGroup.getApiAction() == ApiAction.Insert) {
			List<Long> others = synGroup.getSememeIDs().stream().filter(id -> id > 0)
					.collect(Collectors.toList());
			if (others.size() == 1) {
				otherSememeID = others.get(0);
				// the modifying and peristing of the other sememe will be done down below
			} else {
				throw new RuntimeException("This should not have happened: There must be only one other sememe when creating a synonym group.");
			}
		}
		if (synGroup.getSememeIDs() != null) {
			// Replace the internal IDs within the sememe ID set
			synGroup.setSememeIDs(synGroup.getSememeIDs().stream().map(id -> id < 0 ? this.sememeIdMapping.get(id) : id)
				.collect(Collectors.toCollection(LinkedHashSet::new)));
		} else {
			// this is strange and a mistake, at least two sememes must be set
			throw new RuntimeException("No sememes are set in a synonym group.");
		}
		// Do the good, persist the synGroup
		synGroupsController.persist(synGroup, Context.USE_OUTER_TRANSACTION_CONTEXT);
		// Put the synGroupID into the sememe
		sememe.setSynGroupID(synGroup.getId());

		if (otherSememeID != null) {
			// TODO Check if lock on that other sememe exists
			Session session = HibernateUtil.getSession();
			Query<Sememe> qSememe = session.createQuery(
				"FROM Sememe S WHERE S.id = :id", Sememe.class);
			qSememe.setParameter("id", otherSememeID);
			Sememe otherSememe = qSememe.getSingleResult();
			otherSememe.setSynGroupID(synGroup.getId());
			session.update(otherSememe);
		}
	}

	private void persistMappings(final Session session, final LexemeDetailedDTO lexemeDTO) throws CodeException
	{
		if (lexemeDTO.getMappings() == null || lexemeDTO.getMappings().size() == 0) {
			return;
		}

		for (final Mapping mapping : lexemeDTO.getMappings()) {
			if (mapping.getApiAction().equals(ApiAction.Delete)) {
				continue;
			} else if (mapping.getId() != null && mapping.getId() > 0) {
				throw new RuntimeException("Mapping that is marked as new already has an ID.");
			} else if (mapping.getId() == null || (mapping.getId() != null && mapping.getId() == 0)) {
				throw new RuntimeException("Variant that is marked as new has not gotten an internal ID.");
			}
			// Check if the sememes are compatible with each other (languages of the lexemes checked against
			// the langPair). It also exchanges internal sememe IDs against the real ones.
			checkMappingCompatibility(session, sememesController, lexemeDTO.getLexeme(), mapping);
			// Persist
			Long internalID = mapping.getId();
			mapping.setId(null);
			mapping.setChanged(false);
			mapping.setCreatorID(CurrentUser.INSTANCE.get());
			session.save(mapping);
			this.mappingIdMapping.put(internalID, mapping.getId());
		}
	}

	private void checkMappingCompatibility(final Session session, final SememesController sememesController,
		final Lexeme thisLexeme, final Mapping mapping) throws CodeException {
		// Exchange the sememe IDs if neccessary (temporary internal against real)
		if (mapping.getSememeOneID() < 0) {
			mapping.setSememeOneID(this.sememeIdMapping.get(mapping.getSememeOneID()));
		}
		if (mapping.getSememeTwoID() < 0) {
			mapping.setSememeTwoID(this.sememeIdMapping.get(mapping.getSememeTwoID()));
		}
		// Load the two sememes
		List<Sememe> sememes = sememesController.loadByIDs(Set.of(mapping.getSememeOneID(), mapping.getSememeTwoID()));
		final Sememe sememeOne =
			sememes.stream().filter(sememe -> sememe.getId().equals(mapping.getSememeOneID()))
				   .findFirst()
				   .orElseThrow(() -> new RuntimeException("Linked sememe in mapping does not exist."));
		final Sememe sememeTwo =
			sememes.stream().filter(sememe -> sememe.getId().equals(mapping.getSememeTwoID()))
				   .findFirst()
				   .orElseThrow(() -> new RuntimeException("Linked sememe in mapping does not exist."));
		// Get the languages of lexeme one and two
		final int lexemeOneLangID;
		if (thisLexeme.getId().equals(sememeOne.getLexemeID())) {
			lexemeOneLangID = thisLexeme.getLangID();
		} else {
			LexemeSlimDTO slimLexeme = lexemesController.getOneSlim(sememeOne.getLexemeID());
			if (slimLexeme == null) {
				throw new RuntimeException("Lexeme for linked sememe in mapping does not exist.");
			}
			lexemeOneLangID = slimLexeme.getLangID();
		}
		final int lexemeTwoLangID;
		if (thisLexeme.getId().equals(sememeTwo.getLexemeID())) {
			lexemeTwoLangID = thisLexeme.getLangID();
		} else {
			LexemeSlimDTO slimLexeme = lexemesController.getOneSlim(sememeTwo.getLexemeID());
			if (slimLexeme == null) {
				throw new RuntimeException("Lexeme for linked sememe in mapping does not exist.");
			}
			lexemeTwoLangID = slimLexeme.getLangID();
		}

		// Find the fitting LangPair, and if none is to be found throw an exception
		LangPair langPair = null;
		for (final LangPair pair : this.langPairsController.list()) {
			//if (pair.getLangOneID() == sememeOne.getL)
			if (pair.getLangOneID() == lexemeOneLangID && pair.getLangTwoID() == lexemeTwoLangID) {
				langPair = pair;
				break;
			}
		}
		if (langPair == null) {
			throw new RuntimeException("No language pair exists for a mapping for language with ID "
				+ lexemeOneLangID + " and language with ID " + lexemeTwoLangID);
		}
	}
}