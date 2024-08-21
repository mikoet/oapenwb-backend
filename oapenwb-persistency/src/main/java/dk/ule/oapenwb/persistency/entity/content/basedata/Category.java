// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.persistency.entity.content.basedata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.persistency.entity.IEntity;
import dk.ule.oapenwb.persistency.entity.Views;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Sememe;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * <p>0..n categories can be assigned to a {@link Sememe}.
 * The categories theirselves can have a parent category, i.e. be in a hierarchy.</p>
 */
@Data
@Entity
@Table(name = "Categories")
@Audited
@NoArgsConstructor
@AllArgsConstructor
public class Category implements IEntity<Integer>
{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_seq")
	@SequenceGenerator(name = "category_seq", sequenceName = "category_seq", allocationSize = 1)
	@JsonView(Views.REST.class)
	private Integer id;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	@Column
	@JsonView(Views.REST.class)
	private Integer parentID;

	@Column(length = 64, nullable = false)
	@NotNull
	@Size(min = 2, max = 64)
	@JsonView(Views.REST.class)
	private String uitID;

	// uitID for the abbreviation (short text), e.g. "bot." for "botanical"
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