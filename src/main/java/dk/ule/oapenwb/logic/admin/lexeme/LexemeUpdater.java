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
import dk.ule.oapenwb.entity.content.basedata.LexemeType;
import dk.ule.oapenwb.entity.content.lexemes.LexemeForm;
import dk.ule.oapenwb.entity.content.lexemes.Mapping;
import dk.ule.oapenwb.entity.content.lexemes.SynGroup;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.logic.admin.LangPairsController;
import dk.ule.oapenwb.logic.admin.LexemeTypesController;
import dk.ule.oapenwb.logic.admin.TagsController;
import dk.ule.oapenwb.logic.admin.generic.CEntityController;
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
 * The LexemeUpdater checks and – if OK – saves an {@link LexemeDetailedDTO} for an already persistent lexeme.
 */
public class LexemeUpdater
{
	//private final Context context;
	private final CGEntityController<LexemeFormType, Integer, Integer> lexemeFormTypesController;
	private final LexemeTypesController lexemeTypesController;
	private final CGEntityController<LemmaTemplate, Integer, Integer> lemmaTemplatesController;
	private final TagsController tagsController;
	private final SynGroupsController synGroupsController;
	private final LangPairsController langPairsController;
	private final LexemesController lexemesController;
	private final SememesController sememesController;

	// Maps to map the internal, negative IDs to the real given IDs after persisting
	private final Map<Long, Long> variantIdMapping = new HashMap<>();
	private final Map<Long, Long> sememeIdMapping = new HashMap<>();
	// TODO This one is never used for translating the IDs afterwards. Forgotten or unnecessary?
	private final Map<Long, Long> mappingIdMapping = new HashMap<>();

	public LexemeUpdater(
		//final Context context,
		final CGEntityController<LexemeFormType, Integer, Integer> lexemeFormTypesController,
		final LexemeTypesController lexemeTypesController,
		final CGEntityController<LemmaTemplate, Integer, Integer> lemmaTemplatesController,
		final TagsController tagsController, final SynGroupsController synGroupsController,
		final LangPairsController langPairsController, final LexemesController lexemesController,
		final SememesController sememesController)
	{
		//this.context = context;
		this.lexemeFormTypesController = lexemeFormTypesController;
		this.lexemeTypesController = lexemeTypesController;
		this.lemmaTemplatesController = lemmaTemplatesController;
		this.tagsController = tagsController;
		this.synGroupsController = synGroupsController;
		this.langPairsController = langPairsController;
		this.lexemesController = lexemesController;
		this.sememesController = sememesController;
	}

	public LexemeSlimDTO update(final Session session, final Long id, final LexemeDetailedDTO lexemeDTO,
		final LexemeDetailedDTO oldLexemeDTO)
		throws CodeException, MultiCodeException
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
		new LexemeDetailDTOChecker(lexemeFormTypesController, lexemeDTO, LexemeDetailDTOChecker.Operation.Update).check();

		// ID check
		Lexeme lexeme = lexemeDTO.getLexeme();
		if (!lexeme.getId().equals(id)) {
			throw new CodeException(ErrorCode.Admin_EntityIdDiffersInUpdate,
				Collections.singletonList(new Pair<>("type", "Lexeme")));
		}
		// Make some basic checks with the old lexeme
		Lexeme oldLexeme = oldLexemeDTO.getLexeme();
		if (oldLexeme == null) {
			// TODO
			throw new RuntimeException("The lexeme you tried to save doesn't exist anymore.");
		}
		// The lexeme's and lexemeOld's updatedAt timestamp must be equal
		/* This is out of date.
		if (!lexeme.getUpdatedAt().equals(oldLexeme.getUpdatedAt())) {
			// TODO
			throw new RuntimeException("You cannot save the lexeme because it was updated by someone else.");
		}*/
		// The parserID cannot be changed once it was set
		if (oldLexeme.getParserID() != null && !oldLexeme.getParserID().equals(lexeme.getParserID())) {
			// TODO
			throw new RuntimeException("The parser ID cannot be changed once it was set.");
		}
		// TODO also not changeable: language, lexeme type
		// TODO !! they are, but need a special treatment of the lexemeForms of the lexeme and its variants
		// Nu köänet wy dat nye lexeem sülvens seakern
		if (lexeme.getParserID() != null && lexeme.getParserID().isEmpty()) {
			lexeme.setParserID(null);
		}
		lexeme.setChanged(false);
		session.update(lexeme);

		// Handle the tags
		handleTags(session, lexeme.getTags(), oldLexeme.getTags());

		// Persist the variants including their lexeme forms
		persistVariants(session, lexemeDTO, oldLexemeDTO);

		// The LemmaTemplateProcessor will build the lemmas for all variants and also save them again
		LemmaTemplateProcessor lemmaTemplateProcessor = new LemmaTemplateProcessor(session, lexemeDTO,
			lemmaTemplatesController,
			lexemeTypesController, lexemeFormTypesController);
		lemmaTemplateProcessor.buildLemmata();

		// Persist the sememes
		persistSememes(session, lexemeDTO, oldLexemeDTO);

		// Persist the mappings
		persistMappings(session, lexemeDTO, oldLexemeDTO);

		// Persist the links
		// TODO persistLinks(session, lexemeDTO);

		// Some last checks
		Optional<Variant> mainVariant = lexemeDTO.getVariants().stream().filter(Variant::isMainVariant).findFirst();
		Optional<Sememe> firstSememe = lexemeDTO.getSememes().stream().findFirst();

		if (mainVariant.isEmpty()) {
			throw new RuntimeException("Something went wrong. Could not find the main variant after persisting.");
		}
		if (firstSememe.isEmpty()) {
			throw new RuntimeException("Something went wrong. Could not find the first sememe after persisting.");
		}

		// Create the result that will be sent to the client
		return new LexemeSlimDTO(lexeme, mainVariant.get(), firstSememe.get());
	}

	private void handleTags(final Session session, final Set<String> tags, final Set<String> oldTags) throws CodeException {
		Set<String> tagsToUnuse = oldTags != null ? new HashSet<>(oldTags) : new HashSet<>();
		if (tags != null) {
			for (String tag : tags) {
				if (tagsToUnuse.contains(tag)) {
					// The tag was already in use and as it still is it doesn't have to be unused
					tagsToUnuse.remove(tag);
				} else {
					// this tag is new in use
					tagsController.useTag(session, tag);
				}
			}
		}
		// Unuse the old tags that aren't used anymore
		for (String tag : tagsToUnuse) {
			tagsController.unuseTag(session, tag);
		}
	}

	/**
	 * Will persist the LexemeForms and also set the variantID if needed.
	 *
	 * @param session current Hibernate session
	 * @param variant {@link Variant} instance to persist the {@link LexemeForm}s for.
	 */
	private void persistLexemeForms(final Session session, final Variant variant, final List<LexemeForm> oldLexemeForms)
	{
		if (variant.getLexemeForms() == null) {
			return;
		}
		// As long as we do not generated any lexeme forms and have no protected lexeme forms we can simply
		// delete all the persistent forms and persist the incoming ones
		// TODO Later we must take account of the LexemeForm's state.
		if (oldLexemeForms != null) {
			for (LexemeForm form : oldLexemeForms) {
				session.delete(form);
			}
		}
		// Persist the incoming LexemeForms
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

	private void persistVariants(final Session session, final LexemeDetailedDTO lexemeDTO,
		final LexemeDetailedDTO oldLexemeDTO)
	{
		if (lexemeDTO.getVariants() == null) {
			return;
		}

		List<Variant> oldVariants = oldLexemeDTO.getVariants() != null
			? new LinkedList<>(oldLexemeDTO.getVariants())
			: new LinkedList<>();

		/*
		 * Pröven: varianten dee dat al geaven deit möätet de lyke ID hebben
		 */

		for (final Variant variant : lexemeDTO.getVariants()) {
			if (ApiAction.Insert.equals(variant.getApiAction())) {
				if (variant.getId() != null && variant.getId() > 0) {
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
				persistLexemeForms(session, variant, null);
			} else if (ApiAction.Update.equals(variant.getApiAction())) {
				// Dat mut en olde variante med de ID geaven
				Variant oldVariant = (Variant) getEntityByID(oldVariants, variant.getId());
				if (oldVariant == null) {
					throw new RuntimeException("Variant with ID " + variant.getId()
						+ " is marked for update but did not exist in the database.");
				}
				oldVariants.removeIf(var -> var.getId().equals(variant.getId()));
				variant.setLexemeID(lexemeDTO.getLexeme().getId());
				variant.setChanged(false);
				session.update(variant);
				persistLexemeForms(session, variant, oldVariant.getLexemeForms());
			} else if (ApiAction.Delete.equals(variant.getApiAction())) {
				// Dat mut en olde variante med de ID geaven
				Variant oldVariant = (Variant) getEntityByID(oldVariants, variant.getId());
				if (oldVariant == null) {
					throw new RuntimeException("Variant with ID " + variant.getId()
						+ " is marked for update but did not exist in the database.");
				}
				// Delete all LexemeForms
				if (oldVariant.getLexemeForms() != null) {
					for (final LexemeForm oldForm : oldVariant.getLexemeForms()) {
						session.delete(oldForm);
					}
				}
				// Delete the variant itself
				session.delete(oldVariant);
			}
			// ApiAction.None: Nothing to do with these ones
		}
	}

	private IRPCEntity<Long> getEntityByID(final List<? extends IRPCEntity<Long>> list, final Long id) {
		if (id == null) {
			// TODO
			throw new RuntimeException("Cannot get the entity without an ID.");
		}
		IRPCEntity<Long> result = null;
		if (list != null) {
			for (final IRPCEntity<Long> entity : list) {
				if (id.equals(entity.getId())) {
					result = entity;
					break;
				}
			}
		}
		return result;
	}

	private void persistSememes(final Session session, final LexemeDetailedDTO lexemeDTO,
		final LexemeDetailedDTO oldLexemeDTO) throws CodeException {
		if (lexemeDTO.getSememes() == null) {
			return;
		}

		List<Sememe> oldSememes = oldLexemeDTO.getSememes() != null
			? new LinkedList<>(oldLexemeDTO.getSememes())
			: new LinkedList<>();

		/*
		 * Persisting is done with three loops because of the Synonym groups that can only be persisted
		 * once the real IDs for the sememes are there.
		 */

		for (final Sememe sememe : lexemeDTO.getSememes()) {
			if (ApiAction.Insert.equals(sememe.getApiAction())) {
				if (sememe.getId() != null && sememe.getId() > 0) {
					throw new RuntimeException("Sememe that is marked as new already has an ID.");
				} else if (sememe.getId() == null || (sememe.getId() != null && sememe.getId() == 0)) {
					throw new RuntimeException("Sememe that is marked as new has not gotten an internal ID.");
				}
				Long internalID = sememe.getId();
				sememe.setId(null);
				sememe.setLexemeID(lexemeDTO.getLexeme().getId());
				// Remap internal IDs to real IDs
				if (sememe.getVariantIDs() != null) {
					sememe.setVariantIDs(
						sememe.getVariantIDs().stream().map(id -> id < 0 ? this.variantIdMapping.get(id) : id)
							  .collect(Collectors.toCollection(LinkedHashSet::new)));
				}
				sememe.setChanged(false);
				sememe.setCreatorID(CurrentUser.INSTANCE.get());
				session.save(sememe);
				this.sememeIdMapping.put(internalID, sememe.getId());
			} else if (ApiAction.Update.equals(sememe.getApiAction())) {
				// Dat mut en old semeem med de ID geaven
				Sememe oldSememe = getSememeByID(oldSememes, sememe.getId());
				if (oldSememe == null) {
					throw new RuntimeException("Sememe with ID " + sememe.getId()
						+ " is marked for update but did not exist in the database.");
				}
				// TODO Why did I remove the old sememe here at first?!
				//oldSememes.removeIf(var -> var.getId().equals(sememe.getId()));
				sememe.setLexemeID(lexemeDTO.getLexeme().getId());
				// Remap internal IDs to real IDs
				if (sememe.getVariantIDs() != null) {
					sememe.setVariantIDs(
						sememe.getVariantIDs().stream().map(id -> id < 0 ? this.variantIdMapping.get(id) : id)
							  .collect(Collectors.toCollection(LinkedHashSet::new)));
				}
				sememe.setChanged(false);
				session.update(sememe);
			} else if (ApiAction.Delete.equals(sememe.getApiAction())) {
				// Dat mut en old semeem med de ID geaven
				Sememe oldSememe = getSememeByID(oldSememes, sememe.getId());
				if (oldSememe == null) {
					throw new RuntimeException("Sememe with ID " + sememe.getId()
												   + " is marked for update but did not exist in the database.");
				}
				if (oldSememe.getSynGroup() != null) {
					// Remove the sememe from the old SynGroup and persist it in that state
					oldSememe.getSynGroup().getSememeIDs().remove(sememe.getId());
					persistSynGroup(oldSememe);
				}
				// Delete the sememe itself
				session.delete(oldSememe);
			}
			// ApiAction.None: Nothing to do with these ones
		}

		// Flush current data into the database so they can be found in further queries
		session.flush();

		// After flush persist the SynGroups
		for (final Sememe sememe : lexemeDTO.getSememes()) {
			if (ApiAction.Insert.equals(sememe.getApiAction())) {
				// Persist the SynGroup
				persistSynGroup(sememe);
			} else if (ApiAction.Update.equals(sememe.getApiAction())) {
				Sememe oldSememe = getSememeByID(oldSememes, sememe.getId());
				// Persist the SynGroup
				if (oldSememe.getSynGroup() == null && sememe.getSynGroup() == null) {
					// Nothing to do
				} else if (oldSememe.getSynGroup() == null && sememe.getSynGroup() != null) {
					// Persist the now set SynGroup
					persistSynGroup(sememe);
				} else if (oldSememe.getSynGroup() != null && sememe.getSynGroup() == null) {
					// Remove the sememe from the old SynGroup and persist it in that state
					removeSememeFromSynGroup(oldSememe.getSynGroup(), sememe.getId());
					persistSynGroup(oldSememe);
					sememe.setSynGroupID(null);
				} else if (oldSememe.getSynGroup() != null && sememe.getSynGroup() != null) {
					if (oldSememe.getSynGroup().getId().equals(sememe.getSynGroup().getId())) {
						// The assigned SynGroup was just modified
						persistSynGroup(sememe);
					} else {
						// Another SynGroup was assigned that was modified.
						// Remove the sememe from the old SynGroup and persist it in that state
						removeSememeFromSynGroup(oldSememe.getSynGroup(), sememe.getId());
						persistSynGroup(oldSememe);
						// Persist the now set SynGroup
						persistSynGroup(sememe);
					}
				}
			} else if (ApiAction.Delete.equals(sememe.getApiAction())) {
				sememe.setSynGroupID(null);
				// Only this case the SynGroup itself was already handled in previous loop
			}
			// ApiAction.None: Nothing to do with these ones
		}

		// One last run to persist the sememes again
		for (final Sememe sememe : lexemeDTO.getSememes()) {
			if (ApiAction.Insert.equals(sememe.getApiAction())) {
				session.save(sememe);
			} else if (ApiAction.Update.equals(sememe.getApiAction())) {
				session.update(sememe);
			}
			// ApiAction.None: Nothing to do with these ones
		}
	}

	private Sememe getSememeByID(final List<Sememe> list, final Long id) {
		if (id == null) {
			throw new RuntimeException("Cannot get a sememe without an ID.");
		}
		Sememe result = null;
		if (list != null) {
			for (final Sememe sememe : list) {
				if (id.equals(sememe.getId())) {
					result = sememe;
					break;
				}
			}
		}
		return result;
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
			List<Long> others = synGroup.getSememeIDs().stream().filter(id -> id > 0 && !id.equals(sememe.getId()))
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
			// in this case the SynGroup will be deleted by the SynGroupsController
		}
		// Do the good
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

	/**
	 * Used to remove a sememe from the previously used SynGroup.
	 *
	 * @param synGroup would be the previously used SynGroup (probably from the oldSememe)
	 * @param sememeID the ID of the sememe that shall be removed from the SynGroup
	 */
	private void removeSememeFromSynGroup(final SynGroup synGroup, final Long sememeID)
	{
		if (synGroup.getSememeIDs().remove(sememeID)
				&& synGroup.getApiAction() == ApiAction.None)
		{
			// We must set the REST action on our own here since the frontend doesn't take care
			// about this
			synGroup.setApiAction(ApiAction.Update);
		}
	}

	private void persistMappings(final Session session, final LexemeDetailedDTO lexemeDTO,
		final LexemeDetailedDTO oldLexemeDTO) throws CodeException {
		if (lexemeDTO.getMappings() == null) {
			return;
		}

		List<Mapping> oldMappings = oldLexemeDTO.getMappings() != null
			? new LinkedList<>(oldLexemeDTO.getMappings())
			: new LinkedList<>();

		// Make a first loop over the mappings to delete those marked for deleting.
		// That is neccessary because of the unique index on the sememe pair (a user might have selected
		// a mapping for deletion and recreated it).
		for (final Mapping mapping : lexemeDTO.getMappings()) {
			if (ApiAction.Delete.equals(mapping.getApiAction())) {
				// Dat mut en old mäpping med de ID geaven
				Mapping oldMapping = (Mapping) getEntityByID(oldMappings, mapping.getId());
				if (oldMapping == null) {
					throw new RuntimeException("Mapping with ID " + mapping.getId()
						+ " is marked for update but did not exist in the database.");
				}
				// Delete the mapping itself
				session.delete(oldMapping);
			}
			// ApiAction.None: Nothing to do with these ones
		}
		session.flush();
		// Second loop for other actions.
		for (final Mapping mapping : lexemeDTO.getMappings()) {
			if (ApiAction.Insert.equals(mapping.getApiAction())) {
				if (mapping.getId() != null && mapping.getId() > 0) {
					throw new RuntimeException("Mapping that is marked as new already has an ID.");
				} else if (mapping.getId() == null || (mapping.getId() != null && mapping.getId() == 0)) {
					throw new RuntimeException("Mapping that is marked as new has not gotten an internal ID.");
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
			} else if (ApiAction.Update.equals(mapping.getApiAction())) {
				// Dat mut en old mäpping med de ID geaven
				Mapping oldMapping = (Mapping) getEntityByID(oldMappings, mapping.getId());
				if (oldMapping == null) {
					throw new RuntimeException("Mapping with ID " + mapping.getId()
						+ " is marked for update but did not exist in the database.");
				}
				oldMappings.removeIf(var -> var.getId().equals(mapping.getId()));
				// Check if the sememes are compatible with each other (languages of the lexemes checked against
				// the langPair). It also exchanges internal sememe IDs against the real ones.
				checkMappingCompatibility(session, sememesController, lexemeDTO.getLexeme(), mapping);
				// Update / TODO merge?
				mapping.setChanged(false);
				session.update(mapping);
			}
			// ApiAction.Delete or ApiAction.None: Nothing to do with these ones
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
