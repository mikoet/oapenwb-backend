// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin;

import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.entity.ui.UiTranslation;
import dk.ule.oapenwb.entity.ui.UiTranslationKey;
import dk.ule.oapenwb.entity.ui.UiTranslationSet;
import dk.ule.oapenwb.logic.admin.common.FilterCriterion;
import dk.ule.oapenwb.logic.admin.common.IRestController;
import dk.ule.oapenwb.logic.admin.generic.EntityController;
import dk.ule.oapenwb.logic.context.Context;
import dk.ule.oapenwb.logic.context.ITransaction;
import dk.ule.oapenwb.util.Pair;

import java.util.*;

/**
 * <p>The UiTranslationSetController manages the {@link UiTranslation}s by combining the entities with the same
 * key properties (scope, uitID) plus the locale into entities of the type {@link UiTranslationSet} which are then
 * the objects received and send to over the REST interface.
 *
 * <ul>
 *   <li>TODO REFACT Is it really necessary / 'good' to implement {@link IRestController}?</li>
 * </ul>
 * </p>
 */
public class UiTranslationSetController implements IRestController<UiTranslationSet, String>
{
	private final EntityController<UiTranslation, UiTranslationKey> uitController;
	private final Context _context;
	@Override
	public Context getContext() {
		return _context;
	}

	public UiTranslationSetController(EntityController<UiTranslation, UiTranslationKey> uitController) {
		this.uitController = uitController;
		this._context = new Context(true);
	}

	@Override
	public List<UiTranslationSet> list() throws CodeException {
		final SortedMap<String, UiTranslationSet> map = new TreeMap<>();
		for (UiTranslation uit : this.uitController.list()) {
			final String uitID = uit.getUitKey().getId();
			final String scope = uit.getUitKey().getScopeID();
			final String mapKey =
				((scope == null || scope.isEmpty()) ? "" : scope + "/-/") + uitID;
			UiTranslationSet uitSet = map.get(mapKey);
			if (uitSet == null) {
				uitSet = new UiTranslationSet();
				uitSet.setUitID(uitID);
				uitSet.setScopeID(scope);
				uitSet.setEssential(uit.isEssential());
				map.put(mapKey, uitSet);
			}
			uitSet.getTranslations().put(
				uit.getUitKey().getLocale(), uit.getText()
			);
		}
		return new ArrayList<>(map.values());
	}

	/**
	 * This method must not be used. Use {@link #get(String, String)} instead.
	 *
	 * @param id does not exist this way
	 * @return Will never return anything
	 * @throws CodeException is actually never thrown
	 */
	@Override
	public UiTranslationSet get(String id) throws CodeException {
		throw new CodeException(ErrorCode.Admin_EntityOperation_NotSupported,
			Arrays.asList(new Pair<>("operation", "GET-BY-SIMPLE-ID"), new Pair<>("entity", "UiTranslationSet")));
	}

	public UiTranslationSet get(String scope, String uitID) throws CodeException {
		UiTranslationSet uitSet = null; // return null if no UiTranslation instance will be found
		// Load all UiTranslation instances for the given id (uitID)
		List<UiTranslation> uiTranslations = uitController.getBy(
			new FilterCriterion("uitKey.id", uitID, FilterCriterion.Operator.Equals),
			new FilterCriterion("uitKey.scopeID", scope, FilterCriterion.Operator.Equals)
		);
		if (uiTranslations != null && uiTranslations.size() > 0) {
			// If UiTranslation instances were found combine them into an UiTranslationSet instance
			for (UiTranslation uit : uiTranslations) {
				if (uitSet == null) {
					uitSet = new UiTranslationSet();
					uitSet.setUitID(uitID);
					uitSet.setScopeID(scope);
					uitSet.setEssential(uit.isEssential());
				}
				uitSet.getTranslations().put(uit.getUitKey().getLocale(), uit.getText());
			}
		}
		return uitSet;
	}

	@Override
	public Object create(UiTranslationSet entity, final Context context) throws CodeException {
		// TODO use Admin_EntityOperation_PropertyNotOk
		if (entity.getUitID() == null || entity.getUitID().trim().isEmpty())
		{
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "CREATE"),
					new Pair<>("entity", UiTranslationSet.class.getSimpleName())));
		}

		ITransaction ta = context.beginTransaction();
		try {
			for (Map.Entry<String, String> entry : entity.getTranslations().entrySet()) {
				this.uitController.create(
					new UiTranslation(entity.getUitID(), entry.getKey(), entity.getScopeID(), entry.getValue(),
						entity.isEssential()),
					Context.USE_OUTER_TRANSACTION_CONTEXT
				);
			}
			context.setRevisionComment("Created UI translation with scope='" + entity.getScopeID() + "', uitID='"
				+ entity.getUitID() + "'");
			ta.commit();
		} catch (Exception e) {
			ta.rollback();
			throw e;
		}
		return entity.getUitID();
	}

	public void update(String scope, String uitID, UiTranslationSet entity) throws CodeException {
		update(scope, uitID, entity, getContext());
	}

	public void update(String scope, String uitID, UiTranslationSet entity, final Context context) throws CodeException {
		if (entity.getUitID() == null || entity.getUitID().trim().isEmpty()
			|| !entity.getUitID().equals(uitID))
		{
			throw new CodeException(ErrorCode.Admin_EntityOperation_PropertyNotOk,
				Arrays.asList(new Pair<>("property", "uitID"),
					new Pair<>("operation", "UPDATE"),
					new Pair<>("entity", UiTranslationSet.class.getSimpleName())));
		}

		ITransaction ta = context.beginTransaction();
		try {
			for (Map.Entry<String, String> entry : entity.getTranslations().entrySet()) {
				UiTranslationKey uitKey = new UiTranslationKey(uitID, scope, entry.getKey());
				UiTranslation uit = this.uitController.get(uitKey);
				// Update the UIT's text
				uit.setText(entry.getValue());
				// Update the uit on the DB
				this.uitController.update(uitKey, uit, Context.USE_OUTER_TRANSACTION_CONTEXT);
			}
			context.setRevisionComment("Updated UI translation with scope='" + scope + "', uitID='" + uitID + "'");
			ta.commit();
		} catch (Exception e) {
			ta.rollback();
			throw e;
		}
	}

	@Override
	public void update(String id, UiTranslationSet entity, final Context context) throws CodeException {
		throw new CodeException(ErrorCode.Admin_EntityOperation_NotSupported,
			Arrays.asList(new Pair<>("operation", "UPDATE-BY-ID"), new Pair<>("entity", "UiTranslationSet")));
	}

	public void delete(String scope, String uitID) throws CodeException {
		delete(scope, uitID, getContext());
	}

	public void delete(String scope, String uitID, final Context context) throws CodeException {
		if (uitID == null || uitID.trim().isEmpty())
		{
			throw new CodeException(ErrorCode.Admin_EntityOperation_PropertyNotOk,
				Arrays.asList(new Pair<>("property", "uitID"),
					new Pair<>("operation", "DELETE"),
					new Pair<>("entity", UiTranslationSet.class.getSimpleName())));
		}

		ITransaction ta = context.beginTransaction();
		try {
			this.uitController.deleteBy(Context.USE_OUTER_TRANSACTION_CONTEXT,
				new FilterCriterion("uitKey.scopeID", scope, FilterCriterion.Operator.Equals),
				new FilterCriterion("uitKey.id", uitID, FilterCriterion.Operator.Equals));
			context.setRevisionComment("Deleted UI translation with scope='" + scope + "', uitID='" + uitID + "'");
			ta.commit();
		} catch (Exception e) {
			ta.rollback();
			throw e;
		}
	}

	@Override
	public void delete(String id, UiTranslationSet entity, final Context context) throws CodeException {
		throw new CodeException(ErrorCode.Admin_EntityOperation_NotSupported,
			Arrays.asList(new Pair<>("operation", "DELETE-BY-ID"), new Pair<>("entity", "UiTranslationSet")));
	}
}