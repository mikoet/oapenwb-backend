// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.content.basedata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.entity.Views;
import dk.ule.oapenwb.entity.content.lexemes.Link;
import dk.ule.oapenwb.entity.content.lexemes.SynLink;
import dk.ule.oapenwb.logic.admin.generic.IEntity;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * The LinkType specifies the type of a link between Sememes – realized by {@link Link} – as well as between
 * SynGroups – realized by {@link SynLink}.
 */
@Data
@Entity
@Table(name = "LinkTypes")
@Audited
@NoArgsConstructor
@AllArgsConstructor
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
	 * For target Sememe:
	 * - startLangIDs: IDs of allowed language for the start sememe/lexeme. If not existent all languages are allowed.
	 * - endLangIDs: IDs of allowed language for the end sememe/lexeme. If not existent all languages are allowed.
	 * - selfReferring: can the end sememe/lexeme be of the same language as the start language? (choose a different
	 *   name like languageInternal, or something like that)
	 *
	 * For target SynGroup:
	 * - …
	 */
	@Valid
	@Column(columnDefinition = "jsonb")
	@Type(JsonBinaryType.class)
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