// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.lexeme.sememe;

import com.google.inject.Singleton;
import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.logic.admin.generic.EntityController;
import dk.ule.oapenwb.logic.admin.lexeme.LexemesController;
import dk.ule.oapenwb.util.HibernateUtil;
import dk.ule.oapenwb.util.JsonUtil;
import dk.ule.oapenwb.util.Pair;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * <p>This controller for now only has some special methods regarding the loading of sememes.</p>
 * <ul>
 * <li><b>21-09-17:</b> Instances of this class are created here and there in the code, there is no centrally stored
 *   instance by now. If that's to change at one point in time – e.g. because the controller get's non static
 *   attributes – all the distributed instances must be replaced.</li>
 * </ul>
 */
@Singleton
public class SememesController extends EntityController<Sememe, Long>
{
	private static final Logger LOG = LoggerFactory.getLogger(SememesController.class);

	private static final int MAX_LEXEMES = 10;

	public SememesController()
	{
		super(Sememe::new, Sememe.class, ids -> Long.parseLong(ids[0]), true);
	}

	public List<Sememe> loadByIDs(final Set<Long> sememeIDs) throws CodeException {
		List<Sememe> entities;
		try {
			Session session = HibernateUtil.getSession();
			Query<Sememe> qSememes = session.createQuery(
				"FROM Sememe S WHERE S.id IN (:sememeIDs) ORDER BY S.id", Sememe.class);
			qSememes.setParameterList("sememeIDs", sememeIDs);
			entities = qSememes.list();
		} catch (Exception e) {
			LOG.error("Error fetching instances of type Sememe", e);
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "GET-BY-IDS"), new Pair<>("entity", "Sememe")));
		}
		return entities;
	}

	public SSearchResult find(
		final SSearchRequest request) throws CodeException
	{
		final SSearchResult result = new SSearchResult();
		try {
			// Query for the lexemes
			final List<LexemeSlimPlus> lexemesList = new LinkedList<>();
			final NativeQuery<Object[]> lexemesQuery = createLexemesQuery(request);
			final List<Object[]> lexemeRows = HibernateUtil.listAndCast(lexemesQuery);
			for (Object[] row : lexemeRows) {
				final LexemeSlimPlus lexemeSlimPlus = new LexemeSlimPlus(
					(Long) row[0],		// id
					(String) row[1],	// parserID
					(Integer) row[2],	// typeID
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

	private NativeQuery<Object[]> createLexemesQuery(final SSearchRequest request)
	{
		final StringBuilder sb = new StringBuilder();

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
		final Session session = HibernateUtil.getSession();
		final NativeQuery<Object[]> query = session.createNativeQuery(sb.toString(), Object[].class)
			.addScalar("id", StandardBasicTypes.LONG)
			.addScalar("parserID", StandardBasicTypes.STRING)
			.addScalar("typeID", StandardBasicTypes.INTEGER)
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

	/**
	 * 1. fetch the sememe
	 * 2. fetch the lexeme for the sememe
	 * 3. fetch the main variant for the lexeme
	 *
	 * @param id the sememe's id
	 * @return SememesSlim instance
	 * @throws CodeException in case fetching gets an error
	 */
	public SememeSlim getOneSlim(Long id) throws CodeException
	{
		SememeSlim slim = null;
		try {
			// TODO (hibernate-update): test this part intensively
			final NativeQuery<SememeSlim> lexemesQuery = createLSememeSlimQuery(id);
			slim = HibernateUtil.getSingleResult(lexemesQuery);
		} catch (Exception e) {
			LOG.error("Error fetching instance of type " + Lexeme.class.getSimpleName(), e);
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "GET-ONE-SLIM"), new Pair<>("entity", SememeSlim.class.getSimpleName())));
		}

		return slim;
	}

	private NativeQuery<SememeSlim> createLSememeSlimQuery(long sememeID)
	{
		final StringBuilder sb = new StringBuilder();

		// Query (Q030)
		sb.append("select S.id as id, S.internalName as internalName, S.active as active, S.spec as spec,\n");
		sb.append("  S.lexemeID as lexemeID, L.typeID as typeID, L.langID as langID, L.active as lexActive,\n");
		sb.append("  V.pre as pre, V.main as main, V.post as post\n");
		sb.append("from Sememes S inner join Lexemes L on S.lexemeID = L.id\n");
		sb.append("  inner join Variants V on (L.id = V.lexemeID and V.mainVariant)\n");
		sb.append("where S.id = :sememeID");

		// Create the query
		final Session session = HibernateUtil.getSession();
		final NativeQuery<SememeSlim> query = session.createNativeQuery(sb.toString(), SememeSlim.class)
			.addScalar("id", StandardBasicTypes.LONG)
			.addScalar("internalName", StandardBasicTypes.STRING)
			.addScalar("active", StandardBasicTypes.BOOLEAN)
			.addScalar("spec", StandardBasicTypes.STRING)
			.addScalar("lexemeID", StandardBasicTypes.LONG)
			.addScalar("typeID", StandardBasicTypes.INTEGER)
			.addScalar("langID", StandardBasicTypes.INTEGER)
			.addScalar("lexActive", StandardBasicTypes.BOOLEAN)
			.addScalar("pre", StandardBasicTypes.STRING)
			.addScalar("main", StandardBasicTypes.STRING)
			.addScalar("post", StandardBasicTypes.STRING);

		query.setParameter("sememeID", sememeID);

		return query;
	}
}