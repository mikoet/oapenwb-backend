// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.content.basedata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.persistency.entity.IEntity;
import dk.ule.oapenwb.persistency.entity.Views;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

/**
 * The LangOrthoMapping represents an assignment of a {@link Orthography} to a {@link Language}.
 */
@Data
@Entity
@Table(name = "LangOrthoMappings")
@Audited
@NoArgsConstructor
@AllArgsConstructor
public class LangOrthoMapping implements IEntity<Integer>
{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "langortho_seq")
	@SequenceGenerator(name = "langortho_seq", sequenceName = "langortho_seq", allocationSize = 1)
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

	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private int langID;

	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private int orthographyID;

	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private short position;
}