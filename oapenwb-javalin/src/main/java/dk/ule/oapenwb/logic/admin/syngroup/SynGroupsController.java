// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.syngroup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.persistency.entity.ApiAction;
import dk.ule.oapenwb.persistency.entity.content.lexemes.SynGroup;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.logic.admin.generic.EntityController;
import dk.ule.oapenwb.logic.admin.lexeme.LexemesController;
import dk.ule.oapenwb.logic.admin.lexeme.VariantController;
import dk.ule.oapenwb.logic.admin.lexeme.sememe.LexemeSlimPlus;
import dk.ule.oapenwb.logic.context.Context;
import dk.ule.oapenwb.logic.presentation.ControllerSet;
import dk.ule.oapenwb.logic.presentation.PresentationBuilder;
import dk.ule.oapenwb.logic.presentation.options.PresentationOptions;
import dk.ule.oapenwb.util.CurrentUser;
import dk.ule.oapenwb.util.HibernateUtil;
import dk.ule.oapenwb.util.JsonUtil;
import dk.ule.oapenwb.util.Pair;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>The controller to manage the entities of type {@link SynGroup}.</p>
 */
@Singleton
public class SynGroupsController extends EntityController<SynGroup, Integer>
{
	private static final Logger LOG = LoggerFactory.getLogger(SynGroupsController.class);
	private static final int MAX_SYN_GROUPS = 25;
	private static final int MAX_LEXEMES = 10;

	@Inject
	private ControllerSet controllers;

	public SynGroupsController()
	{
		super(SynGroup::new, SynGroup.class, ids -> Integer.parseInt(ids[0]), true);
	}

	@Override
	protected String getDefaultOrderClause() {
		return " order by E.presentation ASC";
	}

	public void persist(final SynGroup synGroup, final Context context) throws CodeException {
		if (!synGroup.getSememeIDs().isEmpty()) {
			this.generatePresentation(synGroup);
			if (synGroup.getApiAction() == ApiAction.Insert) {
				synGroup.setId(null);
				synGroup.setCreatorID(CurrentUser.INSTANCE.get());
				create(synGroup, context);
			} else if (synGroup.getApiAction() == ApiAction.Update) {
				update(synGroup.getId(), synGroup, context);
			}
		} else {
			// If there are no sememe IDs left in the SynGroup it is to be deleted.
			delete(synGroup.getId(), synGroup, context);
		}
	}

	private void generatePresentation(final SynGroup synGroup) throws CodeException {
		// This was the old way of doing it
		//new PresentationBuilder().generatePresentation(synGroup);

		// The new way
		VariantController variantController = new VariantController();

		// Load all sememes of the given SynGroup
		List<Sememe> sememes = controllers.getSememesController().loadByIDs(synGroup.getSememeIDs());
		// Put all variantIDs of the loaded sememes into one big set
		Set<Long> variantIDs = new HashSet<>();
		for (Sememe sememe : sememes) {
			variantIDs.addAll(sememe.getVariantIDs());
		}
		// Now load all variants from that set, and give everything to the PresentationBuilder to do its magic
		PresentationBuilder builder = new PresentationBuilder();
		String presentation = builder.build(PresentationOptions.DEFAULT_PRESENTATION_OPTIONS, this.controllers, sememes,
			variantController.loadByIDs(variantIDs));
		synGroup.setPresentation(presentation);
	}

	public SGSearchResult find(
		final SGSearchRequest request) throws CodeException
	{
		final SGSearchResult result = new SGSearchResult();
		try {
			// Query the SynGroups
			final NativeQuery<SynGroupItem> synGroupQuery = createSynGroupQuery(request);
			final List<SynGroupItem> synGroupList = synGroupQuery.list();
			result.setSynGroups(synGroupList);

			// Query for the lexemes
			final List<LexemeSlimPlus> lexemesList = new LinkedList<>();
			final NativeQuery<Object[]> lexemesQuery = createLexemesQuery(request);
			//List<Object[]> lexemeRows = HibernateUtil.listAndCast(lexemesQuery);
			final List<Object[]> lexemeRows = lexemesQuery.list();
			for (final Object[] row : lexemeRows) {
				final LexemeSlimPlus lexemeSlimPlus = new LexemeSlimPlus(
					(Long) row[0],		// id
					(String) row[1],	// parserID
					(Long) row[2],		// typeID
					(Integer) row[3],	// langID
					(String) row[4],	// pre
					(String) row[5],	// main
					(String) row[6],	// post
					(Boolean) row[7],	// active
					(Integer) row[8],	// condition
					JsonUtil.convertJsonbStringToLinkedHashSet((String) row[9]), // tags
					(Long) row[10]		// sememeID
				);
				lexemesList.add(lexemeSlimPlus);

				// Query the sememes for each lexeme
				final Session session = HibernateUtil.getSession();
				final Query<Sememe> query = session.createQuery(
					"FROM " + Sememe.class.getSimpleName() + " S WHERE S.lexemeID = :lexemeID ORDER BY S.id ASC", Sememe.class);
				query.setParameter("lexemeID", lexemeSlimPlus.getId());
				lexemeSlimPlus.setSememes(query.list());
			}
			result.setLexemes(lexemesList);
		} catch (Exception e) {
			LOG.error("Error fetching instances of type " + Lexeme.class.getSimpleName(), e);
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "GET-ALL"), new Pair<>("entity", Lexeme.class.getSimpleName())));
		}
		return result;
	}

	private NativeQuery<SynGroupItem> createSynGroupQuery(final SGSearchRequest request)
	{
		final StringBuilder sb = new StringBuilder();

		// Basis query (Q010)
		sb.append("select sg.id as id, sg.description as description, sg.presentation as presentation\n");
		sb.append("from SynGroups sg, Sememes se left join Variants Va on (se.lexemeID = Va.lexemeID and Va.mainVariant=true)\n");
		sb.append("where sg.sememeIDs @> ('[' || se.id || ']')");
		sb.append(HibernateUtil.CONSTANT_JSONB);
		sb.append("\n  and se.lexemeID in (\n");
		sb.append("    select L.id\n");
		sb.append("    from Lexemes L left join Variants V on (L.id = V.lexemeID and V.mainVariant=true)\n");
		sb.append("    where L.langID = :langID\n");
		if (request.getTypeID().isPresent()) {
			sb.append("      and L.typeID = :typeID\n");
		}
		sb.append("      and L.id in (select lexemeID from Variants Vi\n");
		sb.append("      where Vi.id in (select variantID from LexemeForms\n");

		String filterText = request.getFilter();
		if (filterText != null && !filterText.isEmpty()) {
			final Pair<String, String> filterResult = LexemesController.buildFilterStatementAndText(request.getFilter(),
				request.getTextSearchType());
			final String filterStatement = filterResult.getLeft();
			filterText = filterResult.getRight();
			// Add the text filtering part if it's set
			sb.append("        where ").append(filterStatement).append("\n");
		}
		sb.append(")))\n");

		// Add the order clause and the paging data
		sb.append("order by Va.main\n");
		sb.append("limit :limit offset :offset");

		// Create the query
		final Session session = HibernateUtil.getSession();
		final NativeQuery<SynGroupItem> query = session.createNativeQuery(sb.toString(), SynGroupItem.class)
			.addScalar("id", StandardBasicTypes.INTEGER)
			.addScalar("description", StandardBasicTypes.STRING)
			.addScalar("presentation", StandardBasicTypes.STRING);

		// Set the parameters
		if (filterText != null) {
			query.setParameter("filter", filterText);
		}
		query.setParameter("langID", request.getLangID());
		if (request.getTypeID().isPresent()) {
			query.setParameter("typeID", request.getTypeID().get());
		}
		query.setParameter("offset", 0);
		query.setParameter("limit", MAX_SYN_GROUPS);

		return query;
	}

	private NativeQuery<Object[]> createLexemesQuery(final SGSearchRequest request)
	{
		StringBuilder sb = new StringBuilder();

		// Basis query (Q011)
		sb.append("select L.id as id, L.parserID as parserID, L.typeID as typeID, L.langID as langID,\n");
		sb.append("  V.pre as pre, V.main as main, V.post as post, L.active as active, 5 as condition,\n");
		sb.append("  L.tags as tags, S.id as sememeID\n");
		sb.append("from Lexemes L left join Variants V on (L.id = V.lexemeID and V.mainVariant=true)\n");
		sb.append("  left join Sememes S on (L.id = S.lexemeID AND S.id = (SELECT MIN(lexemeID) FROM Sememes WHERE lexemeID = L.id))\n");
		sb.append("where 1 = 1\n");

		String filterText = request.getFilter();
		if (filterText != null && !filterText.isEmpty()) {
			final Pair<String, String> filterResult = LexemesController.buildFilterStatementAndText(request.getFilter(),
				request.getTextSearchType());
			final String filterStatement = filterResult.getLeft();
			filterText = filterResult.getRight();
			// Add the text filtering part if it's set
			sb.append("  and L.id in (select lexemeID from Variants Vi\n");
			sb.append("    where Vi.id in (select variantID from LexemeForms where ")
			  .append(filterStatement)
			  .append("))\n");
		}
		sb.append("  and L.langID = :langID\n");
		if (request.getTypeID().isPresent()) {
			sb.append("  and L.typeID = :typeID\n");
		}
		// Add the order clause and the paging data
		sb.append("order by V.main\n");
		sb.append("limit :limit offset :offset");

		// Create the query
		Session session = HibernateUtil.getSession();
		NativeQuery<Object[]> query = session.createNativeQuery(sb.toString(), Object[].class)
			.addScalar("id", StandardBasicTypes.LONG)
			.addScalar("parserID", StandardBasicTypes.STRING)
			.addScalar("typeID", StandardBasicTypes.LONG)
			.addScalar("langID", StandardBasicTypes.INTEGER)
			.addScalar("pre", StandardBasicTypes.STRING)
			.addScalar("main", StandardBasicTypes.STRING)
			.addScalar("post", StandardBasicTypes.STRING)
			.addScalar("active", StandardBasicTypes.BOOLEAN)
			.addScalar("condition", StandardBasicTypes.INTEGER)
			.addScalar("tags", StandardBasicTypes.STRING)
			.addScalar("sememeID", StandardBasicTypes.LONG);

		query.setParameter("offset", 0);
		query.setParameter("limit", MAX_LEXEMES);

		// Set the parameters
		if (filterText != null) {
			query.setParameter("filter", filterText);
		}
		query.setParameter("langID", request.getLangID());
		if (request.getTypeID().isPresent()) {
			query.setParameter("typeID", request.getTypeID());
		}

		return query;
	}
}