// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.ui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.base.Views;
import dk.ule.oapenwb.logic.admin.generic.IEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import java.io.Serializable;
import java.time.Instant;

/**
 * En UiTranslation is en öäversetting vöär de bruker-snidstea van't wöördebook.
 */
@Data
@Entity
@Table(name = "UiTranslations" /*indexes = { @Index(columnList = "scopeID", name = "UiTranslation_scope") }*/)
@Audited
@NoArgsConstructor
public class UiTranslation implements IEntity<UiTranslationKey>, Serializable
{
	public UiTranslation(String id, String locale, String scopeID, String text, boolean essential)
	{
		this.setUitKey(new UiTranslationKey(id, scopeID, locale));
		this.text = text;
		this.essential = essential;
	}

	@EmbeddedId
	private UiTranslationKey uitKey;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	@JsonView(Views.REST.class)
	private Instant createdAt;

	@UpdateTimestamp
	@Column
	@JsonView(Views.REST.class)
	private Instant updatedAt;

	// the scopeID is part of the uitKey property
	/* TODO not really needed as an object here...
	@JoinColumn(name = "scopeID", insertable = false, updatable = false)
	@ManyToOne(targetEntity = UiTranslationScope.class, fetch = FetchType.LAZY) // FetchType.EAGER instead?
	private UiTranslationScope scope;
	 */

	@Column(length = 8192, nullable = false)
	private String text;

	@Column(nullable = false)
	private boolean essential;

	@Override
	public void setEntityID(UiTranslationKey id)
	{
		this.setUitKey(id);
	}
	@JsonIgnore
	@Override
	public UiTranslationKey getEntityID() { return this.getUitKey(); }

	public String getId()
	{
		return uitKey.getId();
	}

	public String getLocale()
	{
		return uitKey.getLocale();
	}
}