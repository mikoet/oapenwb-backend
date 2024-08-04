// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.content.basedata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.base.Views;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.logic.admin.generic.IEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.*;

/**
 * A pair of two {@link Language}s. Creating a pairs of languages is what allows creating mappings between
 * {@link Lexeme}s/{@link Sememe}s
 * of these languages.
 */
@Data
@Entity
@Audited
@Table(name = "LangPairs", uniqueConstraints=@UniqueConstraint(columnNames={"langOneID", "langTwoID"}))
@NoArgsConstructor
public class LangPair implements IEntity<String>
{
	public static final int ID_LENGTH = 32;

	public LangPair(String id, int langOneID, int langTwoID, int position)
	{
		this.id = id;
		this.langOneID = langOneID;
		this.langTwoID = langTwoID;
		this.position = position;
	}

	@Id
	@Column(length = ID_LENGTH)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private String id;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	@Override
	public void setEntityID(String id) {  setId(id); }
	@JsonIgnore
	@Override
	public String getEntityID() {  return getId(); }

	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private int langOneID;
	@JoinColumn(name = "langOneID", insertable = false, updatable = false)
	// When lazy loading is used: https://github.com/FasterXML/jackson-datatype-hibernate
	@ManyToOne(targetEntity = Language.class, fetch = FetchType.EAGER)
	@JsonIgnore
	private Language langOne;

	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private int langTwoID;
	@JoinColumn(name = "langTwoID", insertable = false, updatable = false)
	@ManyToOne(targetEntity = Language.class, fetch = FetchType.EAGER)
	@JsonIgnore
	private Language langTwo;

	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private int position;
}