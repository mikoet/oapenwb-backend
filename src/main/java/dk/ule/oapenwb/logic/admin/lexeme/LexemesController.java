// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.lexeme;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.base.error.MultiCodeException;
import dk.ule.oapenwb.entity.content.basedata.LemmaTemplate;
import dk.ule.oapenwb.entity.content.basedata.LexemeFormType;
import dk.ule.oapenwb.entity.content.lexemes.LexemeForm;
import dk.ule.oapenwb.entity.content.lexemes.Link;
import dk.ule.oapenwb.entity.content.lexemes.Mapping;
import dk.ule.oapenwb.entity.content.lexemes.SynGroup;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Tag;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.logic.admin.LangPairsController;
import dk.ule.oapenwb.logic.admin.LexemeTypesController;
import dk.ule.oapenwb.logic.admin.LinkTypesController;
import dk.ule.oapenwb.logic.admin.TagsController;
import dk.ule.oapenwb.logic.admin.generic.CGEntityController;
import dk.ule.oapenwb.logic.admin.lexeme.sememe.SememesController;
import dk.ule.oapenwb.logic.admin.syngroup.SynGroupsController;
import dk.ule.oapenwb.logic.context.Context;
import dk.ule.oapenwb.logic.context.ITransaction;
import dk.ule.oapenwb.util.HibernateUtil;
import dk.ule.oapenwb.util.JsonUtil;
import dk.ule.oapenwb.util.Pair;
import dk.ule.oapenwb.util.json.Pagination;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.type.BooleanType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.OptimisticLockException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>The LexemesController handles:
 * <ol>
 * <li>Loading of lexemes, including paging and filtering as well as supplying the substructures like
 *   {@link Tag}s, {@link SynGroup}s,
 *   and {@link Sememe}s.</li>
 * <li>Saving the complex structure of lexemes and its substructures. This is done in a kind that is rather
 *   a Remote Procedure Call, not standard REST.</li>
 * <li>{@link LexemeCreator} is utilized for saving new lexemes first hand, while {@link LexemeUpdater} is utilized
 *   to update already existing ones.</li>
 * </ol>
 * </p>
 */
@Singleton
public class LexemesController
{
	private static final Logger LOG = LoggerFactory.getLogger(LexemesController.class);

	private final CGEntityController<LexemeFormType, Integer, Integer> lexemeFormTypesController;
	private final LexemeTypesController lexemeTypesController;
	private final CGEntityController<LemmaTemplate, Integer, Integer> lemmaTemplatesController;
	private final TagsController tagsController;
	private final SynGroupsController synGroupsController;
	private final LangPairsController langPairsController;
	private final SememesController sememesController;
	private final LinkTypesController linkTypesController;

	private final Context _context;

	@Inject
	public LexemesController(
		@Named(AdminControllers.CONTROLLER_LEXEME_FORM_TYPES)
			final CGEntityController<LexemeFormType, Integer, Integer> lexemeFormTypesController,
		final LexemeTypesController lexemeTypesController,
		@Named(AdminControllers.CONTROLLER_LEMMA_TEMPLATES)
			final CGEntityController<LemmaTemplate, Integer, Integer> lemmaTemplatesController,
		TagsController tagsController,
		final SynGroupsController synGroupsController,
		LangPairsController langPairsController,
		final SememesController sememesController,
		LinkTypesController linkTypesController)
	{
		this.lexemeFormTypesController = lexemeFormTypesController;
		this.lexemeTypesController = lexemeTypesController;
		this.lemmaTemplatesController = lemmaTemplatesController;
		this.tagsController = tagsController;
		this.synGroupsController = synGroupsController;
		this.langPairsController = langPairsController;
		this.sememesController = sememesController;
		this.linkTypesController = linkTypesController;
		this._context = new Context(true);
	}

	public Context getContext()
	{
		return _context;
	}

	public List<LexemeSlimDTO> list(
		final Pagination pagination,
		final LSearchRequest request) throws CodeException
	{
		List<LexemeSlimDTO> lexemes = new LinkedList<>();
		try {
			checkParameters(request, pagination);

			if (request.getFilter() == null && request.getOptions() == null) {
				loadLexemesWithoutFilter(lexemes, pagination);
			} else {
				loadLexemesWithFilter(lexemes, request, pagination);
			}
		} catch (Exception e) {
			LOG.error("Error fetching instances of type " + Lexeme.class.getSimpleName(), e);
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "GET-ALL"), new Pair<>("entity", Lexeme.class.getSimpleName())));
		}
		return lexemes;
	}

	private void checkParameters(final LSearchRequest request, final Pagination pagination)
	{
		String filter = request.getFilter() == null || request.getFilter().isEmpty()
							? null : request.getFilter();
		int offset = request.getOffset() == null ? 0 : request.getOffset();
		int limit = request.getLimit() == null ? 50 : request.getLimit();
		if (limit > 100) limit = 100; // maximum 100


		request.setFilter(filter);
		request.setOffset(offset);
		request.setLimit(limit);

		pagination.setOffset(offset);
		pagination.setLimit(limit);
	}

	public LexemeDetailedDTO get(Long id) throws CodeException {
		LexemeDetailedDTO lexemeDTO = null;
		try {
			Session session = HibernateUtil.getSession();
			Lexeme lexeme = session.get(Lexeme.class, id);
			if (lexeme != null) {
				lexemeDTO = new LexemeDetailedDTO();
				lexemeDTO.setLexeme(lexeme);
				// Load all other data for this lexeme: variations, sememes, links, mappings, lexeme forms
				fillDetailedDto(lexemeDTO);
			}
		} catch (Exception e) {
			LOG.error("Error fetching instance of type " + Lexeme.class.getSimpleName(), e);
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "GET-ONE"), new Pair<>("entity", Lexeme.class.getSimpleName())));
		}

		return lexemeDTO;
	}

	public LexemeSlimDTO getOneSlim(Long id) throws CodeException
	{
		LexemeSlimDTO slimDTO = null;
		try {
			Session session = HibernateUtil.getSession();
			// Fetch the lexeme
			Lexeme lexeme = session.get(Lexeme.class, id);

			// Fetch the main variant
			String queryString = "FROM " + Variant.class.getSimpleName()
				+ " E WHERE E.lexemeID = :lexemeID AND mainVariant = :main";
			Query<Variant> variantQuery = session.createQuery(queryString, Variant.class);
			variantQuery.setParameter("lexemeID", lexeme.getId());
			variantQuery.setParameter("main", true);
			Variant mainVariant = variantQuery.getSingleResult();

			// Fetch the first sememe
			queryString = "FROM " + Sememe.class.getSimpleName()
									 + " E WHERE E.lexemeID = :lexemeID ORDER BY id ASC";
			Query<Sememe> sememeQuery = session.createQuery(queryString, Sememe.class);
			sememeQuery.setParameter("lexemeID", lexeme.getId());
			sememeQuery.setFetchSize(1);
			Sememe firstSememe = sememeQuery.getSingleResult();

			// Create the LexemeSlimDTO
			slimDTO = new LexemeSlimDTO(lexeme, mainVariant, firstSememe);
		} catch (Exception e) {
			LOG.error("Error fetching instance of type " + Lexeme.class.getSimpleName(), e);
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "GET-ONE-SLIM"),
					new Pair<>("entity", Lexeme.class.getSimpleName())));
		}

		return slimDTO;
	}

	public LexemeSlimDTO create(final LexemeDetailedDTO lexemeDTO) throws CodeException, MultiCodeException
	{
		return create(lexemeDTO, getContext());
	}

	/**
	 * Checks the given LexemeDetailedDTO object and if the checks succeed without errors it will be persisted.
	 *
	 * Note: When created only the lexeme itself can be persisted. Substructures like variations, links, etc.
	 * but also the lexeme forms must be saved with an update.
	 *
	 * @param lexemeDTO
	 * @return LexemeSlimDTO instance of the created lexeme
	 * @throws CodeException
	 * @throws MultiCodeException
	 */
	public LexemeSlimDTO create(final LexemeDetailedDTO lexemeDTO, final Context context)
		throws CodeException, MultiCodeException
	{
		LexemeSlimDTO result;
		Session session = HibernateUtil.getSession();
		// The whole storing of the detailed lexeme including its variations etc. shall be done within one transaction
		ITransaction transaction = context.beginTransaction();
		try {
			result = new LexemeCreator(lexemeFormTypesController, lexemeTypesController, lemmaTemplatesController,
				tagsController, synGroupsController, langPairsController, this, sememesController,
				linkTypesController).create(session, lexemeDTO);
			// If everything went fine, commit the transaction
			context.setRevisionComment("Created lexeme with ID " + result.getId());
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			throw e;
		}
		return result;
	}

	public LexemeSlimDTO update(Long id, LexemeDetailedDTO lexemeDTO) throws CodeException, MultiCodeException
	{
		return update(id, lexemeDTO, getContext());
	}

	/*
	 * TODO
	 * - showVariationDetailsFrom == lexeme.id -> showVariationDetailsFrom = null
	 */
	public LexemeSlimDTO update(Long id, LexemeDetailedDTO lexemeDTO, final Context context)
		throws CodeException, MultiCodeException
	{
		LexemeSlimDTO result;
		Session session = HibernateUtil.getSession();
		ITransaction transaction = context.beginTransaction();
		try {
			LexemeDetailedDTO oldLexemeDTO = this.get(id);
			// Associations with the old lexemes must be removed from the sessions in order
			// to store the updated lexeme objects
			session.clear();
			result = new LexemeUpdater(lexemeFormTypesController, lexemeTypesController, lemmaTemplatesController,
				tagsController, synGroupsController, langPairsController, this, sememesController)
				.update(session, id, lexemeDTO, oldLexemeDTO);
			// If everything went fine, commit the transaction
			context.setRevisionComment("Updated lexeme with ID " + id);
			transaction.commit();
		} catch (OptimisticLockException e) {
			LOG.warn("Error updating instance of type " + Lexeme.class.getSimpleName());
			LOG.warn("  The entity was already updated or deleted. Entity ID: " + id.toString());
			transaction.rollback();
			if (e.getCause() instanceof StaleObjectStateException) {
				StaleObjectStateException cause = (StaleObjectStateException) e.getCause();
				final String entityName = cause.getEntityName().substring(cause.getEntityName().lastIndexOf('.') + 1);
				throw new CodeException(ErrorCode.Admin_EntityOperation_OptimisticLockWithCause,
					Arrays.asList(new Pair<>("operation", "UPDATE"), new Pair<>("entity", Lexeme.class.getSimpleName()),
						new Pair<>("entityName", entityName), new Pair<>("entityID", cause.getIdentifier())));
			}
			throw new CodeException(ErrorCode.Admin_EntityOperation_OptimisticLock,
				Arrays.asList(new Pair<>("operation", "UPDATE"), new Pair<>("entity", Lexeme.class.getSimpleName())));
		} catch (Exception e) {
			LOG.error("Error updating instance of type Lexeme", e);
			transaction.rollback();
			throw e;
		}
		return result;
	}

	public void delete(Long id) throws CodeException
	{
		delete(id, getContext());
	}

	public void delete(Long id, final Context context) throws CodeException
	{
		// TODO by't delete mut de struktuur 'log' vülled wean un kontrolleerd warden
		// Givt dat eyn semeem, variatioon, ... dee nich med in't log is, mut eyn feyler koamen un dat
		// entity wardt nich löskd.
	}

	/**
	 * Used to load a batch of lexemes in the {@link dk.ule.oapenwb.logic.search.SearchController}.
	 *
	 * @param lexemeIDs
	 * @return
	 * @throws CodeException
	 */
	public List<Lexeme> loadByIDs(final Set<Long> lexemeIDs) throws CodeException {
		List<Lexeme> entities;
		try {
			Session session = HibernateUtil.getSession();
			Query<Lexeme> query = session.createQuery(
				"FROM Lexeme E WHERE E.id IN (:lexemeIDs)", Lexeme.class);
			query.setParameterList("lexemeIDs", lexemeIDs);
			entities = query.list();
		} catch (Exception e) {
			LOG.error("Error fetching instances of type Lexeme", e);
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "GET-BY-IDS"), new Pair<>("entity", "Lexeme")));
		}
		return entities;
	}

	private void loadLexemesWithoutFilter(List<LexemeSlimDTO> resultList, final Pagination pagination)
		throws JsonProcessingException
	{
		Session session = HibernateUtil.getSession();
		// Get the count
		Long total = (Long) session.createQuery("SELECT count(*) FROM "+ Lexeme.class.getSimpleName()).uniqueResult();
		pagination.setTotal(total.intValue());
		// Get the lexemes
		NativeQuery<?> query = null;
		try {
			query = createQuery(pagination);
			List<Object[]> rows = HibernateUtil.listAndCast(query);
			for (Object[] row : rows) {
				resultList.add(new LexemeSlimDTO(
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
				));
			}
		} catch (Exception e) {
			LOG.error("Runetime error occured. Error is: {}", e.getMessage());
			throw e;
		}

		/*
		Query<Lexeme> query = session.createQuery("FROM " + Lexeme.class.getSimpleName()
			+ " E order by E.lemma.main", Lexeme.class);
		query.setFirstResult(pagination.getOffset());
		query.setMaxResults(pagination.getLimit());
		List<Lexeme> entities = query.list();
		for (final Lexeme lexeme : entities) {
			// Fetch the main variant for each lexeme.
			// TODO is this a bottleneck performance wise? Might be better to simply make a query with an left outer join
			// TODO to the variants.
			String queryString = "FROM " + Variant.class.getSimpleName()
				+ " E WHERE E.lexemeID = :lexemeID AND mainVariant = :main";
			Query<Variant> variantQuery = session.createQuery(queryString, Variant.class);
			variantQuery.setParameter("lexemeID", lexeme.getId());
			variantQuery.setParameter("main", true);
			Variant mainVariant = variantQuery.getSingleResult();

			resultList.add(new LexemeSlimDTO(lexeme, mainVariant));
		}
		 */
	}

	private NativeQuery<?> createQuery(final Pagination pagination)
	{
		StringBuilder sb = new StringBuilder();
		// Basis query (related to Q011)
		sb.append("select L.id as id, L.parserID as parserID, L.typeID as typeID, L.langID as langID,\n");
		sb.append("  V.pre as pre, V.main as main, V.post as post, L.active as active, 5 as condition,\n");
		sb.append("  L.tags as tags, S.id as sememeID\n");
		sb.append("from Lexemes L left join Variants V on (L.id = V.lexemeID and V.mainVariant=true)\n");
		sb.append("  left join Sememes S on (L.id = S.lexemeID AND S.id = (SELECT MIN(lexemeID) FROM Sememes WHERE lexemeID = L.id))\n");
		// Add the order clause and the paging data
		sb.append("order by V.main\n");
		sb.append("limit :limit offset :offset");

		// Create the query
		Session session = HibernateUtil.getSession();
		NativeQuery<?> query = session.createSQLQuery(sb.toString())
			.addScalar("id", new LongType())
			.addScalar("parserID", new StringType())
			.addScalar("typeID", new LongType())
			.addScalar("langID", new IntegerType())
			.addScalar("pre", new StringType())
			.addScalar("main", new StringType())
			.addScalar("post", new StringType())
			.addScalar("active", new BooleanType())
			.addScalar("condition", new IntegerType())
			.addScalar("tags", new StringType())
			.addScalar("sememeID", new LongType());

		query.setParameter("offset", pagination.getOffset());
		query.setParameter("limit", pagination.getLimit());

		return query;
	}

	private void loadLexemesWithFilter(List<LexemeSlimDTO> resultList, final LSearchRequest request,
		final Pagination pagination) throws JsonProcessingException
	{
		// Get the count
		NativeQuery<?> countQuery = null;
		NativeQuery<?> query = null;
		try {
			countQuery = createFilteredCountQuery(request);
			Integer total = (Integer) countQuery.uniqueResult();
			pagination.setTotal(total);

			// Get the lexemes
			query = createFilteredQuery(request);
			List<Object[]> rows = HibernateUtil.listAndCast(query);
			for (Object[] row : rows) {
				resultList.add(new LexemeSlimDTO(
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
				));
			}
		} catch (Exception e) {
			final StringBuilder paramString = new StringBuilder();
			if (countQuery != null) {
				countQuery.getParameters().stream().forEach(param -> {
					paramString.append(param.getName());
					paramString.append("  ");
				});
			}
			LOG.error("Runetime error occured. Error is: {}", e.getMessage());
			LOG.error("SQL query: {}", countQuery != null ? countQuery.getQueryString() : "(null)");
			LOG.error("SQL query parameters: {}", paramString.toString());
			throw e;
		}
	}

	//

	private NativeQuery<?> createFilteredCountQuery(final LSearchRequest request)
	{
		StringBuilder sb = new StringBuilder();
		// Basis query
		sb.append("select count(*) as total from Lexemes where\n");
		String filterText = request.getFilter();
		if (filterText != null && !filterText.isEmpty()) {
			final Pair<String, String> filterResult = buildFilterStatementAndText(request.getFilter(),
				request.getTextSearchType());
			final String filterStatement = filterResult.getLeft();
			filterText = filterResult.getRight();
			// Add the text filtering part if it's set
			sb.append("  id in (select lexemeID from Variants\n");
			sb.append("    where id in (select variantID from LexemeForms where ")
				.append(filterStatement).append("))\n");
		} else {
			sb.append("  1=1\n");
		}
		if (request.getOptions() != null) {
			// Add the different filter options
			FilterOptions fo = request.getOptions();
			if (!fo.getState().equals(State.Both)) {
				sb.append("  and active = :active\n");
			}
			if (fo.getLangIDs() != null && fo.getLangIDs().size() > 0) {
				sb.append("  and langID in (:langIDs)\n");
			}
			if (fo.getTypeIDs() != null && fo.getTypeIDs().size() > 0) {
				sb.append("  and typeID in (:typeIDs)\n");
			}
			if (fo.getTags() != null && fo.getTags().size() > 0) {
				sb.append("  and tags @> ");
				sb.append(createTagsString(fo.getTags()));
				sb.append("\n");
			}
		}

		// Create the query
		Session session = HibernateUtil.getSession();
		NativeQuery<?> countQuery = session.createSQLQuery(sb.toString())
			.addScalar("total", new IntegerType());

		// Set the parameters
		if (filterText != null) {
			countQuery.setParameter("filter", filterText);
		}
		if (request.getOptions() != null) {
			// Add the different filter options
			FilterOptions fo = request.getOptions();
			if (!fo.getState().equals(State.Both)) {
				countQuery.setParameter("active", State.Active.equals(fo.getState()));
			}
			if (fo.getLangIDs() != null && fo.getLangIDs().size() > 0) {
				countQuery.setParameterList("langIDs", fo.getLangIDs());
			}
			if (fo.getTypeIDs() != null && fo.getTypeIDs().size() > 0) {
				countQuery.setParameterList("typeIDs", fo.getTypeIDs());
			}
			// Tags string was already added
		}

		return countQuery;
	}

	private NativeQuery<?> createFilteredQuery(final LSearchRequest request)
	{
		StringBuilder sb = new StringBuilder();

		// Basis query (related to Q011)
		sb.append("select L.id as id, L.parserID as parserID, L.typeID as typeID, L.langID as langID,\n");
		sb.append("  V.pre as pre, V.main as main, V.post as post, L.active as active,\n");
		sb.append("  5 as condition, L.tags as tags, S.id as sememeID\n");
		sb.append("from Lexemes L left join Variants V on (L.id = V.lexemeID and V.mainVariant=true)\n");
		sb.append("  left join Sememes S on (L.id = S.lexemeID AND S.id = (SELECT MIN(lexemeID) FROM Sememes WHERE lexemeID = L.id))\n");
		sb.append("where 1 = 1\n");

		String filterText = request.getFilter();
		if (filterText != null && !filterText.isEmpty()) {
			final Pair<String, String> filterResult = buildFilterStatementAndText(request.getFilter(),
				request.getTextSearchType());
			final String filterStatement = filterResult.getLeft();
			filterText = filterResult.getRight();
			// Add the text filtering part if it's set
			sb.append("  and L.id in (select lexemeID from Variants Vi\n");
			sb.append("    where Vi.id in (select variantID from LexemeForms where " + filterStatement + "))\n");
		}
		if (request.getOptions() != null) {
			// Add the different filter options
			FilterOptions fo = request.getOptions();
			if (!fo.getState().equals(State.Both)) {
				sb.append("  and L.active = :active\n");
			}
			if (fo.getLangIDs() != null && fo.getLangIDs().size() > 0) {
				sb.append("  and L.langID in (:langIDs)\n");
			}
			if (fo.getTypeIDs() != null && fo.getTypeIDs().size() > 0) {
				sb.append("  and L.typeID in (:typeIDs)\n");
			}
			if (fo.getTags() != null && fo.getTags().size() > 0) {
				sb.append("  and L.tags @> ");
				sb.append(createTagsString(fo.getTags()));
				sb.append("\n");
			}
		}
		// Add the order clause and the paging data
		sb.append("order by V.main\n");
		sb.append("limit :limit offset :offset");

		// Create the query
		Session session = HibernateUtil.getSession();
		NativeQuery<?> query = session.createSQLQuery(sb.toString())
			.addScalar("id", new LongType())
			.addScalar("parserID", new StringType())
			.addScalar("typeID", new LongType())
			.addScalar("langID", new IntegerType())
			.addScalar("pre", new StringType())
			.addScalar("main", new StringType())
			.addScalar("post", new StringType())
			.addScalar("active", new BooleanType())
			.addScalar("condition", new IntegerType())
			.addScalar("tags", new StringType())
			.addScalar("sememeID", new LongType());

		query.setParameter("offset", request.getOffset());
		query.setParameter("limit", request.getLimit());

		// Set the parameters
		if (filterText != null) {
			query.setParameter("filter", filterText);
		}
		if (request.getOptions() != null) {
			// Add the different filter options
			FilterOptions fo = request.getOptions();
			if (!fo.getState().equals(State.Both)) {
				query.setParameter("active", State.Active.equals(fo.getState()));
			}
			if (fo.getLangIDs() != null && fo.getLangIDs().size() > 0) {
				query.setParameterList("langIDs", fo.getLangIDs());
			}
			if (fo.getTypeIDs() != null && fo.getTypeIDs().size() > 0) {
				query.setParameterList("typeIDs", fo.getTypeIDs());
			}
			// Tags string was already added (about 40 lines above)
		}

		return query;
	}

	/**
	 * @param filterText the filter string
	 * @param textSearchType the optional type of search
	 * @return the pair will contain the FilterStatement in the first and the FilterText in the second attribute
	 */
	public static Pair<String, String> buildFilterStatementAndText(
		String filterText,
		Optional<TextSearchType> textSearchType)
	{
		if (filterText == null || filterText.isEmpty()) {
			throw new RuntimeException("Function has been called with an empty filter in the request");
		}
		// Build the text search part
		TextSearchType searchType = textSearchType.isEmpty() ? TextSearchType.PostgreWeb : textSearchType.get();
		final String filterStatement;
		switch (searchType) {
			case PostgreWeb -> {
				filterStatement = "searchableText @@ websearch_to_tsquery('simple', :filter)";
			}
			case Prefixed -> {
				filterStatement = "searchableText @@ to_tsquery('simple', :filter)";
				if (filterText.contains(" ")) {
					StringBuilder newFilterText = new StringBuilder();
					final String[] parts = filterText.split(" ");
					boolean first = true;
					for (final String part : parts) {
						if (!part.isEmpty() && !part.startsWith("!")) {
							if (!first) {
								// OR operator in search
								newFilterText.append(" | ");
							} else {
								first = false;
							}
							// append the part plus wild card operator
							newFilterText.append(part);
							newFilterText.append(":*");
						}
					}
					filterText = newFilterText.toString();
				} else if (!filterText.contains(":*") && !filterText.startsWith("!")) {
					filterText = filterText + ":*";
				} else if (filterText.equals("!")) {
					// Examination mark alone does not work
					filterText = "";
				}
			}
			default -> {
				throw new RuntimeException("Unsupported text search type was used.");
			}
		}
		return new Pair<>(filterStatement, filterText);
	}

	/**
	 * Builds a string for the tags set for the Postgres jsonb query format:
	 * '["tag1", "tag2", "tag3"]'::jsonb
	 *
	 * TODO TESTING This is critical to SQL injection and needs a unit test (!)
	 * TODO This method could be static, couldn't it? And be moved into a util class.
	 *
	 * @param tags set of tags
	 * @return String containing the tags in PostgreSQL jsonb array format
	 */
	private String createTagsString(final Set<String> tags)
	{
		StringBuilder sb = new StringBuilder();
		// An alternative could be to work with string literals prefixed with an E (E'string').
		// string literals starting with an E are escaped strings similar to how it works in Java
		// https://www.postgresql.org/docs/current/sql-syntax-lexical.html#SQL-SYNTAX-CONSTANTS
		// But it made some issues with escaping the single quotes within the JSON string literals of the arrays.
		sb.append("'[");
		boolean first = true;
		for (String tag : tags) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			// Input: My "Tag" with \ strange 'content'
			// Output: My "Tag" with \\ strange ''content''
			String modified = tag.replace("\\", "\\\\").replace("\"", "\\\"").replace("'", "''").replace(":", "\\:");
			sb.append('"');
			sb.append(modified);
			sb.append('"');
		}
		sb.append("]'");
		sb.append(HibernateUtil.CONSTANT_JSONB);

		return sb.toString();
	}

	private void fillDetailedDto(final LexemeDetailedDTO lexemeDTO) throws CodeException {
		if (lexemeDTO == null || lexemeDTO.getLexeme() == null) {
			throw new RuntimeException("This should not have happened. No lexeme was supplied.");
		}
		Session session = HibernateUtil.getSession();

		// Load variations
		Query<Variant> qVariations = session.createQuery(
			"FROM Variant V WHERE V.lexemeID = :lexemeID order by V.id", Variant.class);
		qVariations.setParameter("lexemeID", lexemeDTO.getLexeme().getId());
		lexemeDTO.setVariants(qVariations.list());

		// Load LexemeForms for all variations
		lexemeDTO.getVariants().forEach(var -> loadLexemeForms(session, var));

		// Load sememes
		Query<Sememe> qSememes = session.createQuery(
			"FROM Sememe S WHERE S.lexemeID = :lexemeID order by S.id", Sememe.class);
		qSememes.setParameter("lexemeID", lexemeDTO.getLexeme().getId());
		lexemeDTO.setSememes(qSememes.list());

		// Load the SynGroup for each sememe
		lexemeDTO.getSememes().forEach(sem -> loadSynGroup(session, sem));


		// Load links for each sememe
		List<Link> linkList = new LinkedList<>();
		lexemeDTO.getSememes().forEach(sem -> {
			Query<Link> qLinks = session.createQuery(
				"FROM Link L WHERE (L.startSememeID = :sememeID or L.endSememeID = :sememeID)" +
					"order by L.typeID, L.startSememeID, L.endSememeID", Link.class);
			qLinks.setParameter("sememeID", sem.getId());
			linkList.addAll(qLinks.list());
		});
		lexemeDTO.setLinks(linkList);

		// Load mappings
		Query<Mapping> qMappings = session.createQuery(
			"FROM Mapping M WHERE M.sememeOneID in (:sememeIDs) or M.sememeTwoID in (:sememeIDs)" +
			" ORDER by M.weight", Mapping.class);
		qMappings.setParameterList("sememeIDs",
			lexemeDTO.getSememes().stream().map(Sememe::getId).collect(Collectors.toList()));
		lexemeDTO.setMappings(qMappings.list());

		// Load the slim sememes for each mapping
		for (Mapping mapping : lexemeDTO.getMappings()) {
			mapping.setSememeOne(sememesController.getOneSlim(mapping.getSememeOneID()));
			mapping.setSememeTwo(sememesController.getOneSlim(mapping.getSememeTwoID()));
		}
	}

	private void loadLexemeForms(final Session session, final Variant variant)
	{
		Query<LexemeForm> query = session.createQuery(
			"FROM LexemeForm E WHERE E.variantID = :id order by E.formTypeID", LexemeForm.class);
		query.setParameter("id", variant.getId());
		variant.setLexemeForms(query.list());
	}

	private void loadSynGroup(final Session session, final Sememe sememe)
	{
		if (sememe.getSynGroupID() != null) {
			try {
				sememe.setSynGroup(this.synGroupsController.get(sememe.getSynGroupID()));
			} catch (CodeException e) {
				LOG.error("This should not have happened. The SynGroup with ID {} was there and then not.",
					sememe.getSynGroupID());
				LOG.error("  Exception thrown when loading SynGroup:", e);
			}
		}
	}
}