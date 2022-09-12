// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.generic;

import com.google.inject.Singleton;
import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.logic.admin.common.FilterCriterion;
import dk.ule.oapenwb.logic.context.Context;
import dk.ule.oapenwb.logic.context.ITransaction;
import dk.ule.oapenwb.util.HibernateUtil;
import dk.ule.oapenwb.util.Pair;
import lombok.Getter;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>The EntityController is able to manage entities that implement the interface {@link IEntity} and thus their
 * IDs must be a single object (i.e. atomic types, instances like a string or an embeddable consisting of multiple
 * objects).</p>
 *
 * TODO support for order by clause in the list() method will be needed at some point
 *
 * @param <T> Type of the entities that shall be managed by this controller
 * @param <S> Type of the IDs of the managed entities
 */
@Singleton
public class EntityController<T extends IEntity<S>, S extends Serializable> implements IEntityController<T, S>
{
	private static final Logger LOG = LoggerFactory.getLogger(EntityController.class);

	// supplies (creates) a new instance of the entity
	private final Supplier<T> supplier;

	// the class of the entity
	@Getter
	private Class<T> clazz;

	// Function that converts an ID in string array form (from REST requests) into a real typed ID instance
	// For that the function takes an array of strings as arguments and returns an ID instance.
	private final Function<String[], S> convertFn;

	private final boolean resetIdOnCreate;

	private final Context _context;


	public static String criteraToString(FilterCriterion...criteria)
	{
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (FilterCriterion criterion : criteria) {
			if (count > 0) {
				sb.append(", ");
			}
			sb.append(criterion.getAttribute());
			sb.append(" ");
			sb.append(criterion.getOperator());
			sb.append(" '");
			sb.append(criterion.getValue());
			sb.append("'");
			count++;
		}
		return sb.toString();
	}


	public EntityController(Supplier<T> supplier, Class<T> clazz, Function<String[], S> convertFn,
		boolean resetIdOnCreate, boolean transactional)
	{
		this.supplier = supplier;
		this.clazz = clazz;
		this.convertFn = convertFn;
		this.resetIdOnCreate = resetIdOnCreate;
		this._context = new Context(transactional);

		//LOG.warn("Instance for class " + clazz.getSimpleName() + " was created");
	}

	public EntityController(Supplier<T> supplier, Class<T> clazz, Function<String[], S> convertFn,
		boolean resetIdOnCreate)
	{
		this(supplier, clazz, convertFn, resetIdOnCreate, true);
	}

	public EntityController(Supplier<T> supplier, Class<T> clazz, Function<String[], S> convertFn)

	{
		this(supplier, clazz, convertFn, true, true);
	}

	public T createInstance() {
		return supplier.get();
	}

	public S stringToId(String... ids) {
		return convertFn.apply(ids);
	}

	public Context getContext()
	{
		return _context;
	}

	@Override
	public List<T> list() throws CodeException
	{
		List<T> entities;
		try {
			Session session = HibernateUtil.getSession();
			String orderClause = checkOrderClause(getDefaultOrderClause());
			Query<T> query = session.createQuery(
				"FROM " + clazz.getSimpleName() + " E" + orderClause,
				clazz);
			entities = query.list();
		} catch (Exception e) {
			LOG.error("Error fetching instances of type " + clazz.getSimpleName(), e);
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "GET-ALL"), new Pair<>("entity", clazz.getSimpleName())));
		}
		return entities;
	}

	private String checkOrderClause(String clause) {
		if (clause.length() > 0 && clause.charAt(0) != ' ') {
			clause = " " + clause;
		}
		return clause;
	}

	/**
	 * May be overridden to set the default order clause of the data returned by the methods
	 * {@link #list()} and {@link #getBy(FilterCriterion...)}. If an unempty string is returned the clause has to start
	 * with the string ' ORDER BY ' itself. The columns of the entity must be accessed with a starting
	 * 'E.', e.g. 'E.column1.
	 *
	 * @return the default order clause or an empty string
	 */
	protected String getDefaultOrderClause() {
		return "";
	}

	@Override
	public T get(S id) throws CodeException
	{
		T entity;
		try {
			Session session = HibernateUtil.getSession();
			entity = session.get(this.clazz, id);

			//Query<T> query = session.createQuery(
			//"FROM " + clazz.getSimpleName() + " E WHERE E.id = :id",
			//clazz);
			//query.setParameter("id", id);
			//entity = query.getSingleResult();
		} catch (Exception e) {
			LOG.error("Error fetching instance of type " + clazz.getSimpleName(), e);
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "GET-ONE"), new Pair<>("entity", clazz.getSimpleName())));
		}
		return entity;
	}

	public List<T> getBy(final FilterCriterion...criteria) throws CodeException
	{
		// TODO Introduce method checkCriteria when getBy is made public via EntityFace
		// TODO REFACT Can't method criteriaToString() be utilized? This code should be probably be centralized and reused
		Session session = HibernateUtil.getSession();
		List<T> entities;
		try {
			// Build the query string, 1st iteration
			StringBuilder queryString = new StringBuilder("FROM " + clazz.getSimpleName() + " E WHERE");
			int count = 0;
			for (FilterCriterion criterion : criteria) {
				if (count > 0) {
					queryString.append(" AND");
				}

				if (criterion.getOperator().isUseValue()) {
					queryString.append(" E.").append(criterion.getAttribute()).append(" ").append(
						criterion.getOperator()).append(" :value").append(count);
				} else {
					queryString.append(" E.").append(criterion.getAttribute()).append(" ").append(
						criterion.getOperator());
				}

				count++;
			}

			// Create the query
			Query<T> query = session.createQuery(queryString.toString(), clazz);

			// Set the parameters, 2nd iteration
			count = 0;
			for (FilterCriterion criterion : criteria) {
				if (criterion.getOperator().isUseValue()) {
					query.setParameter("value" + count, criterion.getValue());
				}
				count++;
			}

			// Execute the query
			entities = query.list();
		} catch (Exception e) {
			LOG.error("Error fetching instances of type " + clazz.getSimpleName(), e);
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "GET-BY"), new Pair<>("entity", clazz.getSimpleName())));
		}
		return entities;
	}

	// TODO Instead of Object as return value S should be used
	@Override
	public Object create(T entity, final Context context) throws CodeException
	{
		Object id;

		if (this.resetIdOnCreate) {
			// Set the ID to null to make sure it will be generate by Hibernate
			entity.setEntityID(null);
		}

		Session session = HibernateUtil.getSession();
		ITransaction transaction = context.beginTransaction();
		try {
			id = session.save(entity);
			context.setRevisionComment(
				"Created entity of type " + clazz.getSimpleName() + " with ID " + id.toString());
			transaction.commit();
		} catch (PersistenceException e) {
			if (e.getCause() instanceof ConstraintViolationException) {
				LOG.error("Constraint violation on inserting instance of type " + clazz.getSimpleName());
				transaction.rollback();
				throw new CodeException(ErrorCode.Admin_EntityOperationConstraintViolation,
					Arrays.asList(new Pair<>("operation", "CREATE"), new Pair<>("type", clazz.getSimpleName())));
			} else {
				LOG.error("Error deleting instance of type " + clazz.getSimpleName(), e);
				transaction.rollback();
				throw new CodeException(ErrorCode.Admin_EntityOperation,
					Arrays.asList(new Pair<>("operation", "CREATE"), new Pair<>("entity", clazz.getSimpleName())));
			}
		} catch (Exception e) {
			LOG.error("Error creating instance of type " + clazz.getSimpleName(), e);
			transaction.rollback();
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "CREATE"), new Pair<>("entity", clazz.getSimpleName())));
		}
		return id;
	}

	@Override
	public void update(S id, T entity, final Context context) throws CodeException
	{
		// Check if the given ID and the ID in the entity are the same
		if (id == null || !id.equals(entity.getEntityID())) {
			LOG.error("The give ID and the ID within the entity differ for type " + clazz.getSimpleName());
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "UPDATE"), new Pair<>("entity", clazz.getSimpleName())));
		}

		Session session = HibernateUtil.getSession();
		ITransaction transaction = context.beginTransaction();
		// Update the entity
		try {
			session.update(entity);
			context.setRevisionComment(
				"Updated entity of type " + clazz.getSimpleName() + " with ID " + id.toString());
			transaction.commit();
		} catch (OptimisticLockException e) {
			LOG.warn("Error updating instance of type " + clazz.getSimpleName());
			LOG.warn("  The entity was already updated or deleted. Entity ID: " + id.toString());
			transaction.rollback();
			throw new CodeException(ErrorCode.Admin_EntityOperation_OptimisticLock,
				Arrays.asList(new Pair<>("operation", "UPDATE"), new Pair<>("entity", clazz.getSimpleName())));
		} catch (PersistenceException e) {
			if (e.getCause() instanceof ConstraintViolationException) {
				LOG.error("Constraint violation on updating instance of type " + clazz.getSimpleName() + ", id '"
					+ id + "'");
				transaction.rollback();
				throw new CodeException(ErrorCode.Admin_EntityOperationConstraintViolation,
					Arrays.asList(new Pair<>("operation", "UPDATE"), new Pair<>("type", clazz.getSimpleName())));
			} else {
				LOG.error("Error updating instance of type " + clazz.getSimpleName(), e);
				transaction.rollback();
				throw new CodeException(ErrorCode.Admin_EntityOperation,
					Arrays.asList(new Pair<>("operation", "UPDATE"), new Pair<>("entity", clazz.getSimpleName())));
			}
		} catch (Exception e) {
			LOG.error("Error deleting instance of type " + clazz.getSimpleName(), e);
			transaction.rollback();
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "UPDATE"), new Pair<>("entity", clazz.getSimpleName())));
		}
	}

	@Override
	public void delete(S id, T entity, final Context context) throws CodeException
	{
		Session session = HibernateUtil.getSession();
		ITransaction transaction = context.beginTransaction();
		try {
			//T entity = session.load(clazz, id); //createInstance();
			//entity.setEntityID(id);
			session.delete(entity);
			context.setRevisionComment(
				"Deleted entity of type " + clazz.getSimpleName() + " with ID " + id.toString());
			transaction.commit();
		} catch (OptimisticLockException e) {
			LOG.warn("Error deleting instance of type " + clazz.getSimpleName());
			LOG.warn("  The entity was already updated or deleted. Entity ID: " + id.toString());
			transaction.rollback();
			throw new CodeException(ErrorCode.Admin_EntityOperation_OptimisticLock,
				Arrays.asList(new Pair<>("operation", "DELETE"), new Pair<>("entity", clazz.getSimpleName())));
		} catch (PersistenceException e) {
			if (e.getCause() instanceof ConstraintViolationException) {
				LOG.error("Constraint violation on deleting instance of type " + clazz.getSimpleName() + ", id '"
					+ id + "'");
				transaction.rollback();
				throw new CodeException(ErrorCode.Admin_EntityOperationConstraintViolation,
					Arrays.asList(new Pair<>("operation", "DELETE"), new Pair<>("type", clazz.getSimpleName())));
			} else {
				LOG.error("Error deleting instance of type " + clazz.getSimpleName(), e);
				transaction.rollback();
				throw new CodeException(ErrorCode.Admin_EntityOperation,
					Arrays.asList(new Pair<>("operation", "DELETE"), new Pair<>("entity", clazz.getSimpleName())));
			}
		} catch (Exception e) {
			LOG.error("Error deleting instance of type " + clazz.getSimpleName(), e);
			transaction.rollback();
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "DELETE"), new Pair<>("entity", clazz.getSimpleName())));
		}
	}

	public int deleteBy(final Context context, final FilterCriterion...criteria) throws CodeException
	{
		List<T> entitiesToDelete = this.getBy(criteria);

		Session session = HibernateUtil.getSession();
		ITransaction transaction = context.beginTransaction();
		try {
			for (T entity : entitiesToDelete) {
				session.delete(entity);
			}
			context.setRevisionComment(
				"Deleted entities of type " + clazz.getSimpleName() + " with: " + EntityController.criteraToString(criteria));
			transaction.commit();
		} catch (OptimisticLockException e) {
			LOG.warn("Error deleting instance of type " + clazz.getSimpleName());
			LOG.warn("  The entity was already updated or deleted. Criteria: " + EntityController.criteraToString(criteria));
			transaction.rollback();
			throw new CodeException(ErrorCode.Admin_EntityOperation_OptimisticLock,
				Arrays.asList(new Pair<>("operation", "DELETE"), new Pair<>("entity", clazz.getSimpleName())));
		} catch (PersistenceException e) {
			if (e.getCause() instanceof ConstraintViolationException) {
				LOG.error("Constraint violation on deleting instance of type " + clazz.getSimpleName() + ", criteria '"
					+ criteria + "'");
				transaction.rollback();
				throw new CodeException(ErrorCode.Admin_EntityOperationConstraintViolation,
					Arrays.asList(new Pair<>("operation", "DELETE-BY"), new Pair<>("type", clazz.getSimpleName())));
			} else {
				LOG.error("Error deleting instance of type " + clazz.getSimpleName(), e);
				transaction.rollback();
				throw new CodeException(ErrorCode.Admin_EntityOperation,
					Arrays.asList(new Pair<>("operation", "DELETE-BY"), new Pair<>("entity", clazz.getSimpleName())));
			}
		} catch (Exception e) {
			LOG.error("Error deleting instance of type " + clazz.getSimpleName(), e);
			transaction.rollback();
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "DELETE-BY"), new Pair<>("entity", clazz.getSimpleName())));
		}
		return entitiesToDelete.size();
	}

	public int deleteBy(final FilterCriterion...criteria) throws CodeException
	{
		return deleteBy(getContext(), criteria);
	}
}