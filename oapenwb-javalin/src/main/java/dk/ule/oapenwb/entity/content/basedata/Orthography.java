// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.content.basedata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.entity.Views;
import dk.ule.oapenwb.logic.admin.generic.IEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

/**
 * An orthography is a way/convention to write a language or a dialect, e.g. Bundesdeutsche Rechtschreibung for the
 * German language or British English for the English language.
 */
@Data
@Entity
@Table(name = "Orthographies")
@Audited
@NoArgsConstructor
public class Orthography implements IEntity<Integer>
{
	public static final String ABBR_SAXON_NYSASSISKE_SKRYVWYSE = "NSS";
	public static final String ABBR_SAXON_GERMAN_BASED = "DBO";
	public static final String ABBR_SAXON_DUTCH_BASED = "NBO";

	public static final String ABBR_GERMAN_FEDERAL = "BDR";
	public static final String ABBR_ENGLISH_BRITISH = "BE";
	public static final String ABBR_DUTCH = "NLS";
	public static final String ABBR_DANISH = "DO";
	public static final String ABBR_SWEDISH = "SO";
	public static final String ABBR_FINNISH = "FO";
	public static final String ABBR_BINOMIAL_NOMENCLATURE = "BINO";

	public Orthography(Integer id, Integer parentID, String uitID, String abbreviation,
		String description, boolean publicly)
	{
		this.id = id;
		this.parentID = parentID;
		this.uitID = uitID;
		this.abbreviation = abbreviation;
		this.description = description;
		this.publicly = publicly;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orthography_seq")
	@SequenceGenerator(name = "orthography_seq", sequenceName = "orthography_seq", allocationSize = 1)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer id;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	@Override
	public void setEntityID(Integer id) {  setId(id); }
	@JsonIgnore
	@Override
	public Integer getEntityID() {  return getId(); }

	@Column(nullable = true)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer parentID;
	@JoinColumn(name = "parentID", insertable = false, updatable = false)
	@ManyToOne(targetEntity = Orthography.class, fetch = FetchType.EAGER)
	@JsonView(Views.Exclude.class)
	private Orthography parent;

	@Column(length = 64, nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private String uitID;

	@Column(length = 32, nullable = false, unique = true)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private String abbreviation;

	@JsonView(Views.REST.class)
	@Column(length = 1024)
	private String description;

	/**
	 * If false the orthography is only for internal purposes (like the maximum variation of the
	 * Nysassiske Skryvwyse)
	 */
	@JsonView(Views.REST.class)
	@Column
	private boolean publicly = true;
}