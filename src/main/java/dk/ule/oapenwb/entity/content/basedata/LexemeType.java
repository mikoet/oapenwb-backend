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

/**
 * <p>The LexemeType specifies a type of a lexeme like noun, verb, adjective and so on.</p>
 */
@Data
@Entity
@Audited
@Table(name = "LexemeTypes")
@NoArgsConstructor
@AllArgsConstructor
public class LexemeType implements IEntity<Integer>
{
	// -- Standard Parts of Speech as defined by https://universaldependencies.org/u/pos/
	public static final String TYPE_ADJ = "ADJ";
	public static final String TYPE_ADP = "ADP";
	public static final String TYPE_ADV = "ADV";
	public static final String TYPE_AUX = "AUX";
	public static final String TYPE_CCONJ = "CCONJ";
	public static final String TYPE_DET = "DET";
	public static final String TYPE_INTJ = "INTJ";
	public static final String TYPE_NOUN = "NOUN";
	public static final String TYPE_NUM = "NUM";
	public static final String TYPE_PART = "PART";
	public static final String TYPE_PRON = "PRON";
	public static final String TYPE_PROPN = "PROPN";
	public static final String TYPE_PUNCT = "PUNCT";
	public static final String TYPE_SCONJ = "SCONJ";
	//public static final String TYPE_SYM = "SYM";
	public static final String TYPE_VERB = "VERB";
	public static final String TYPE_X = "X";

	// -- Internal types
	/**
	 * <p>Case government (Rektioon)</p>
	 */
	public static final String TYPE_I_CG = "iCG";

	// -- Custom types
	/**
	 * <p>Multi-word expressions</p>
	 * <p>Since sometimes a word in one language can only be expressed by an multi-word expression in another,
	 * this type must be mappable to all other kinds of LexemeTypes besides iCG.</p>
	 */
	public static final String TYPE_C_UTDR = "UTDR";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lexemetype_seq")
	@SequenceGenerator(name = "lexemetype_seq", sequenceName = "lexemetype_seq", allocationSize = 1)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer id;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	@Column(length = 64, unique = true)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private String name;

	// only nullable for internal types like Rektion
	@Column
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer uiCategoryID;

	@Column(length = 64, nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private String uitID;

	@Override
	public void setEntityID(Integer id) { this.setId(id); }
	@JsonIgnore
	@Override
	public Integer getEntityID() { return this.getId(); }
}