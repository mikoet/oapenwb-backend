// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin;

import com.google.inject.Singleton;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.logic.admin.common.FilterCriterion;
import dk.ule.oapenwb.logic.admin.generic.CEntityController;
import dk.ule.oapenwb.persistency.entity.content.basedata.Language;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class LanguagesController extends CEntityController<Language, Integer>
{
	public LanguagesController() {
		super(Language::new, Language.class, ids -> Integer.parseInt(ids[0]));
	}

	@Override
	protected String getDefaultOrderClause() {
		return " order by E.locale ASC";
	}

	/**
	 * @param locale locale of the uppermost parent
	 * @return a map of construction &lt;importAbbreviation, Language instance&gt;
	 * @throws CodeException In case the instances could not be fetched...
	 */
	public Map<String, Language> getImportMapForLanguage(String locale) throws CodeException {
		List<Language> entities = getBy(
			new FilterCriterion("locale", String.format("%s%%", locale), FilterCriterion.Operator.Like),
			new FilterCriterion("importAbbreviation", null, FilterCriterion.Operator.IsNotNull));

		Map<String, Language> result = new HashMap<>();
		for (Language language : entities) {
			result.put(language.getImportAbbreviation(), language);
		}

		return result;
	}
}
