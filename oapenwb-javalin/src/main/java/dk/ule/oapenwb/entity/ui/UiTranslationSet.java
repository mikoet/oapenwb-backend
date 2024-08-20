// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.ui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.entity.Views;
import dk.ule.oapenwb.entity.IEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>A UiTranslationSet represents all UiTranslation with the same key (uitID, scopeID), but different
 * locales, mapped into one object.</p>
 * <p>Thus, it is not a JPA entity itself.</p>
 */
@Data
@NoArgsConstructor
public class UiTranslationSet implements IEntity<String> {
	@JsonView(Views.REST.class)
	private String uitID;

	@JsonView(Views.REST.class)
	private String scopeID;

	@JsonView(Views.REST.class)
	private boolean essential;

	@JsonView(Views.REST.class)
	private Map<String, String> translations = new HashMap<>();

	@Override
	public void setEntityID(String id) { this.setUitID(id); }
	@Override
	@JsonIgnore
	public String getEntityID() { return this.getUitID(); }
}