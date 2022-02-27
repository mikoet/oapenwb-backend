// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.lexeme;

import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.IMessage;
import dk.ule.oapenwb.base.error.Message;
import dk.ule.oapenwb.base.error.MultiCodeException;
import dk.ule.oapenwb.entity.content.basedata.*;
import dk.ule.oapenwb.entity.content.lexemes.Link;
import dk.ule.oapenwb.entity.content.lexemes.Mapping;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.logic.admin.generic.IGroupedEntitySupplier;
import dk.ule.oapenwb.util.HibernateUtil;
import dk.ule.oapenwb.util.Pair;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.LongType;

import javax.persistence.NoResultException;
import java.util.*;
import java.util.stream.Stream;

/**
 * <p>The LexemeDetailDTOChecker performs several checks and some smaller auto-corrections on a
 * {@link LexemeDetailedDTO} instance.
 * <ul>
 *   <li><b>TODO</b> Java Bean Validation (https://www.baeldung.com/javax-validation) is currently done in the
 *     LexemeCreator and LexemeUpdater. Should it be moved into this class?</li>
 *   <li>TODO Do a clean up in this class with all the TODOs and outcommented code.</li>
 * </ul>
 * </p>
 */
public class LexemeDetailDTOChecker
{
	private final IGroupedEntitySupplier<LexemeFormType, Integer, Integer> lftSupplier;
	private final LexemeDetailedDTO lexemeDTO;
	private final Operation op;
	private final List<IMessage> messages = new LinkedList<>();

	public enum Operation
	{
		Create,
		Update,
		Delete
	}

	public LexemeDetailDTOChecker(IGroupedEntitySupplier<LexemeFormType, Integer, Integer> lftSupplier,
		LexemeDetailedDTO lexemeDTO, Operation op)
	{
		this.lftSupplier = lftSupplier;
		this.lexemeDTO = lexemeDTO;
		this.op = op;
	}

	public void check() throws MultiCodeException {
		switch (op) {
			case Create, Update -> {
				checkLexeme(lexemeDTO.getLexeme());
				checkVariations(lexemeDTO.getVariants());
				checkSememes(lexemeDTO.getSememes());
				checkLinks(lexemeDTO.getLinks());
				checkMappings(lexemeDTO.getMappings());
			}
		}

		if (messages.size() > 0) {
			throw new MultiCodeException(messages);
		}
	}

	private void checkLexeme(Lexeme lexeme)
	{
		if (lexeme == null) {
			// TODO
			throw new RuntimeException("Lexeme must be set.");
		}

		checkIfEntitiesExist(Collections.singletonList(lexeme.getTypeID()), LexemeType.class);
		checkIfEntitiesExist(Collections.singletonList(lexeme.getLangID()), Language.class);
	}

	private void checkVariations(Collection<Variant> variants)
	{
		switch (op) {
			case Create:
			case Update:
			case Delete:
				assert variants != null;
				variants.forEach(this::checkVariant);
				break;
		}
	}

	private void checkVariant(Variant variant)
	{
		/*
		if (variant.getLexemeID() != lexemeDTO.getLexeme().getId()) {
			messages.add(new Message(ErrorCode.Admin_EntityBrokenLexemeID,
				Collections.singletonList(new Pair<>("type", "variant"))));
		}
		 */

		checkIfEntitiesExist(variant.getDialectIDs(), Language.class);
		checkIfEntitiesExist(Collections.singletonList(variant.getOrthographyID()), Orthography.class);
		// Check LexemeForms
		checkLexemeForms(variant);
		// Check the lemma configuration
		// TODO
	}

	private void checkSememes(Collection<Sememe> sememes)
	{
		switch (op) {
			case Create:
			case Update:
			case Delete:
				assert sememes != null;
				sememes.forEach(this::checkSememe);
				break;
		}
	}

	private void checkSememe(Sememe sememe)
	{
		// TODO 001 checkAbstractVariant(sememe);
		/*
		if (sememe.getLexemeID() != lexemeDTO.getLexeme().getId()) {
			messages.add(new Message(ErrorCode.Admin_EntityBrokenLexemeID,
				Collections.singletonList(new Pair<>("type", "sememe"))));
		}
		 */

		// The variants cannot exist at this point in time...
		checkIfEntitiesExist(sememe.getCategoryIDs(), Category.class);
		checkIfEntitiesExist(sememe.getLevelIDs(), Level.class);

		// TODO spec und specTemplate prüfen, SynGroup prüfen
		// - mind. eins davon muss gesetzt sein
		// -
		//if (sememe.getSpec() && sememe.getSpecTemplate())
	}

	private void checkLinks(Collection<Link> links)
	{
		switch (op) {
			case Create:
			case Update:
			case Delete:
				assert links != null;
				links.forEach(this::checkLink);
				break;
		}
	}

	private void checkLink(Link link)
	{
		Long lexemeID = lexemeDTO.getLexeme().getId();
		/*
		if (link.getStartLexemeID() != lexemeID && link.getEndLexemeID() != lexemeID)
		{
			messages.add(new Message(ErrorCode.Admin_EntityBrokenLexemeID,
				Collections.singletonList(new Pair<>("type", "link"))));
		}
		 */
		checkIfEntitiesExist(Collections.singletonList(link.getTypeID()), LinkType.class);
	}

	private void checkMappings(Collection<Mapping> mappings)
	{
		switch (op) {
			case Create:
			case Update:
			case Delete:
				assert mappings != null;
				mappings.forEach(this::checkMapping);
				break;
		}
	}

	private void checkMapping(Mapping mapping)
	{
		Long lexemeID = lexemeDTO.getLexeme().getId();
		/*
		if (mapping.getLexemeOneID() != lexemeID && mapping.getLexemeTwoID() != lexemeID)
		{
			messages.add(new Message(ErrorCode.Admin_EntityBrokenLexemeID,
				Collections.singletonList(new Pair<>("type", "mapping"))));
		}
		 */
		// 21-07-16 Wy hevvet dår eyrstmål neyne sememen meyr med in de mäppings
		//checkIfEntitiesExist(Collections.singletonList(mapping.getSememeOneID()), Sememe.class);
		//checkIfEntitiesExist(Collections.singletonList(mapping.getSememeTwoID()), Sememe.class);
	}

	private void checkLexemeForms(Variant variant)
	{
		// TODO
	}

	// Only works for entites with a single type ID field called "id"
	private <S, T> void checkIfEntitiesExist(final Collection<S> entities, final Class<T> clazz)
	{
		if (HibernateUtil.isDisableJsonIdChecks()) {
			// The checks will be disabled e.g. for mass inserts (FileImporter)
			return;
		}

		if (entities != null && entities.size() > 0) {
			final String tableName = HibernateUtil.getTableName(clazz);
			// This idea was from https://stackoverflow.com/a/30118121 which also offers a more advanced one
			entities.stream().flatMap(id -> {
					try {
						Session session = HibernateUtil.getSession();
						String queryString = "SELECT 1 as col from "+ tableName + " E where E.id= :id";
						NativeQuery<?> query = session.createSQLQuery(queryString)
							.addScalar("col", new LongType());
						query.setParameter("id", id);
						//Integer result = (Integer) query.uniqueResult();
						query.getSingleResult();
						return null;
					} catch (NoResultException e) {
						return Stream.of(new Message(ErrorCode.Admin_EntityNotFound,
							Arrays.asList(new Pair<>("type", clazz.getSimpleName()), new Pair<>("id", id))));
					} catch (Exception e) {
						return Stream.of(new Message(ErrorCode.Admin_EntityUnknownError,
							Arrays.asList(new Pair<>("type", clazz.getSimpleName()), new Pair<>("id", id),
								new Pair<>("msg", e.getMessage()))));
					}
				}).forEach(this.messages::add);
		}
	}
}