// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.ui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.entity.IEntity;
import dk.ule.oapenwb.entity.Views;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

/**
 * A {@link UiTranslation} can have an optional scope. All UiTranslations outside of the default scope (non/empty scope)
 * will be lazy loaded (at least can be) once a translation of that scope is needed. If no scope is set a translation
 * is part of the default scope.
 */
@Data
@Entity
@Table(name = "UiTranslationScopes")
@Audited
@NoArgsConstructor
@AllArgsConstructor
public class UiTranslationScope implements IEntity<String> {
	@Id
	@Column(length = 32)
	@JsonView(Views.REST.class)
	private String id;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	@Column(length = 128)
	@JsonView(Views.REST.class)
	private String description;

	@Column
	@JsonView(Views.REST.class)
	private boolean essential;

	@JsonIgnore
	@Override
	public String getEntityID() { return id; }
	@Override
	public void setEntityID(String id) { this.id = id; }
}