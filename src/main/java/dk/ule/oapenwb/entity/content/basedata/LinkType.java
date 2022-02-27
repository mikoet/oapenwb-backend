// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.content.basedata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import dk.ule.oapenwb.base.Views;
import dk.ule.oapenwb.entity.content.lexemes.Link;
import dk.ule.oapenwb.entity.content.lexemes.SynLink;
import dk.ule.oapenwb.logic.admin.generic.IEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * The LinkType specifies the type of a link between Lexemes – realized by {@link Link} – as well as between
 * synonym groups – realized by {@link SynLink}.
 */
@Data
@Entity
@Table(name = "LinkTypes")
@Audited
@NoArgsConstructor
@AllArgsConstructor
@TypeDefs({ @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class) })
public class LinkType implements IEntity<Integer>
{
	// Descriptions of link types that will just exist in the dictionary
	public static final String DESC_BINOMIAL_NOMEN = "Binomial nomenclature";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "linktype_seq")
	@SequenceGenerator(name = "linktype_seq", sequenceName = "linktype_seq", allocationSize = 1)
	@JsonView(Views.REST.class)
	private Integer id;

	@Version
	@Column(nullable = false)
	@JsonView(Views.REST.class)
	private Integer version;

	@NotNull
	@Column(length = 512, nullable = false)
	@JsonView(Views.REST.class)
	private String description;

	@NotNull
	@Column(columnDefinition = "character", length = 1, nullable = false)
	@JsonView(Views.REST.class)
	@Convert(converter = LinkTypeTargetConverter.class)
	private LinkTypeTarget target;

	// Scope: linkType
	@NotNull
	@Column(length = 64, nullable = false)
	@JsonView(Views.REST.class)
	private String start_uitID;

	// Scope: linkType
	@NotNull
	@Column(length = 64, nullable = false)
	@JsonView(Views.REST.class)
	private String end_uitID;

	// Scope: linkType
	@NotNull
	@Column(length = 64, nullable = false)
	@JsonView(Views.REST.class)
	private String verbal_uitID;

	/**
	 * Possible properties
	 *
	 * For target Lexeme:
	 * - startLangIDs: IDs of allowed language for the start lexeme. If not existent all languages are allowed.
	 * - endLangIDs: IDs of allowed language for the end lexeme. If not existent all languages are allowed.
	 * - selfReferring: can the end lexeme be of the same language as the start language?
	 *
	 * For target SynGroup:
	 * - …
	 */
	@Valid
	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb")
	@JsonView(Views.REST.class)
	private Map<String, Object> properties = new HashMap<>();

	@Override
	public void setEntityID(Integer id) {
		this.id = id;
	}

	@JsonIgnore
	@Override
	public Integer getEntityID() {
		return id;
	}
}