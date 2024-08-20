// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.content.basedata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.entity.IEntity;
import dk.ule.oapenwb.entity.Views;
import dk.ule.oapenwb.entity.content.lexemes.LexemeForm;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * A LexemeFormType represents one specific type of a {@link LexemeForm},
 * e.g. 'first person singular present time'.
 */
@Data
@Entity
@Audited
@Table(name = "LexemeFormTypes", uniqueConstraints=@UniqueConstraint(columnNames={"lexemeTypeID", "name"}))
@NoArgsConstructor
@AllArgsConstructor
public class LexemeFormType implements IEntity<Integer>, Serializable
{
	public static final String FT_VERB_INFINITIVE = "inf";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lexemeformtype_seq")
	@SequenceGenerator(name = "lexemeformtype_seq", sequenceName = "lexemeformtype_seq", allocationSize = 1)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer id;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer lexemeTypeID;

	@NotNull
	@Column(length = 64, nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private String name;

	@Column(length = 64, nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private String uitID;

	@Column(length = 1024)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private String description;

	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private boolean mandatory;

	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private short position;

	@Override
	public void setEntityID(Integer id) { this.setId(id); }
	@JsonIgnore
	@Override
	public Integer getEntityID() { return this.getId(); }

	// TODO
	// private boolean toSpellCheck; // Shall the LexemeForms for this LexemeFormType be used for a spell checking export?
}