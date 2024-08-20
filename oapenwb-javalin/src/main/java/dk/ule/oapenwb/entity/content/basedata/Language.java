// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.content.basedata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.persistency.entity.IEntity;
import dk.ule.oapenwb.persistency.entity.Views;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.validation.constraints.Size;

/**
 * A Language is a language in this dictionary. If it has a parent it is considered a dialect of its parent.
 */
@Data
@Entity
@Table(name = "Languages")
@Audited
@NoArgsConstructor
public class Language implements IEntity<Integer>
{
	public Language(
		final Integer id,
		final Integer parentID,
		final String locale,
		final String localName,
		final String uitID,
		final String uitID_abbr,
		final Integer mainOrthographyID,
		final String importAbbreviation
	) {
		this.id = id;
		this.parentID = parentID;
		this.locale = locale;
		this.localName = localName;
		this.uitID = uitID;
		this.uitID_abbr = uitID_abbr;
		this.mainOrthographyID = mainOrthographyID;
		this.importAbbreviation = importAbbreviation;
	}

	public Language(Integer id, Integer parentID, String locale, String localName, String uitID, String uitID_abbr,
		Integer mainOrthographyID)
	{
		this(id, parentID, locale, localName, uitID, uitID_abbr, mainOrthographyID, null);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lang_seq")
	@SequenceGenerator(name = "lang_seq", sequenceName = "lang_seq", allocationSize = 1)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer id;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	@Override
	public void setEntityID(Integer id) { setId(id); }
	@JsonIgnore
	@Override
	public Integer getEntityID() { return getId(); }

	// If a parent is set, then this language becomes a dialect
	@Column
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer parentID;

	@Column(length = 32, nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private String localName;

	@Size(min = 2, max = 32)
	@Column(length = 32, nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private String locale;

	@Column(length = 64, nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private String uitID;

	@Column(length = 64, nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private String uitID_abbr;

	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer mainOrthographyID;

	@Column(length = 16)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private String importAbbreviation;

	/*
	 * If this Language is a dialect (Language with a parent), then orthographies for it are optionally.
	 * If no orthographies are set the orthographies from the highest parent will be taken.
	 */
	/*
	@OneToMany(mappedBy = "lang", fetch = FetchType.LAZY)
	@JsonView(Views.Exclude.class)
	private Set<LangOrthoMapping> loMappings;
	 */
}