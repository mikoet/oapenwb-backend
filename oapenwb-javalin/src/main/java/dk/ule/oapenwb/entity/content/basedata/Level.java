// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.content.basedata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.base.Views;
import dk.ule.oapenwb.logic.admin.generic.IEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * The Level specifies possible levels of sememes. Examples of possible levels are:
 * Archaic (outdated, used in old contexts) – sassisk ölderhaftig,
 * prescriptive (should be used),
 * suggestive (why not use this wonderful word?),
 * propositive,
 * vulgar, ...
 */
@Data
@Entity
@Table(name = "UnitLevels")
@Audited
@NoArgsConstructor
@AllArgsConstructor
public class Level implements IEntity<Integer>
{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "unitlevel_seq")
	@SequenceGenerator(name = "unitlevel_seq", sequenceName = "unitlevel_seq", allocationSize = 1)
	@JsonView(Views.REST.class)
	private Integer id;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	@Column(length = 64, nullable = false)
	@NotNull
	@Size(min = 2, max = 64)
	@JsonView(Views.REST.class)
	private String uitID;

	/*
	 * The texts of this uitID should be an abbreviation.
	 * For example "ugs." for German "umgangssprachlich" and "coll." for English "colloquial".
	 */
	@Column(length = 64, nullable = false)
	@NotNull
	@Size(min = 2, max = 64)
	@JsonView(Views.REST.class)
	private String uitID_abbr;

	@Column(length = 1024)
	@Size(max = 1024)
	@JsonView(Views.REST.class)
	private String description;

	@Override
	public void setEntityID(Integer id) { setId(id); }
	@JsonIgnore
	@Override
	public Integer getEntityID() { return getId(); }
}