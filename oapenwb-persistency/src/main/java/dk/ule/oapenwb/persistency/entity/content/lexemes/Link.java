// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.persistency.entity.content.lexemes;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.persistency.entity.ApiAction;
import dk.ule.oapenwb.persistency.entity.IRPCEntity;
import dk.ule.oapenwb.persistency.entity.Views;
import dk.ule.oapenwb.persistency.entity.content.basedata.LinkType;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Sememe;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.validation.constraints.NotNull;

/**
 * A Link resembles a link between two {@link Sememe}s. of two different {@link Lexeme}s.
 * The type – meaning – of a link is defined by the {@link LinkType} and free to set by the editors.
 * In this way Links could be used to map relations between Sememes like:<br>
 * <ul>
 * <li>Antonyms</li>
 * </ul>
 */
@Data
@Entity
@Table(name = "Links")
@Audited
@NoArgsConstructor
public class Link implements IRPCEntity<Integer>
{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "link_seq")
	@SequenceGenerator(name = "link_seq", sequenceName = "link_seq", allocationSize = 1)
	@JsonView(Views.REST.class)
	private Integer id;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	@Column(updatable = false)
	@JsonView(Views.REST.class)
	private Integer creatorID;

	@NotNull
	@Column(nullable = false)
	@JsonView(Views.REST.class)
	private int typeID;

	@NotNull
	@Column(nullable = false)
	@JsonView(Views.REST.class)
	private long startSememeID;

	@NotNull
	@Column(nullable = false)
	@JsonView(Views.REST.class)
	private long endSememeID;

	@Transient
	@NotNull
	@JsonView(Views.REST.class)
	private ApiAction apiAction = ApiAction.None;

	@JsonView(Views.REST.class)
	@Transient
	private boolean changed = false;
}