// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.ui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.entity.Views;
import dk.ule.oapenwb.logic.admin.generic.IEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

/**
 * En UiLanguage is en språke dee in de UI-snidstea van't wöördebook bruked warden kan.
 * Man uutwäälbår is see vöär en bruker blout as see active=true setted is.
 */
@Data
@Entity
@Audited
@Table(name = "UiLanguages")
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "default" })
public class UiLanguage implements IEntity<String> {
	@Id
	@Column(length = 32)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private String locale;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	/**
	 * The language's name in the language itself. So if this is an instance for
	 * German the local name would be 'Deutsch'.
	 */
	@Column(length = 32, nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private String localName;

	/**
	 * True if this is the default language. Shall not be set for multiple languages.
	 * If so it's uncertain which of them will be taken as default.
	 */
	@Column
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private boolean isDefault;

	/**
	 * A language won't be selectable for users unless active is set true.
	 */
	@Column
	@JsonView(Views.REST.class)
	private boolean active;

	// for interface IEntity
	@Override
	public void setEntityID(String id) { this.setLocale(id); }
	@JsonIgnore
	@Override
	public String getEntityID() { return this.getLocale(); }
}