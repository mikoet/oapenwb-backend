// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.persistency.entity.content.lexemes;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.persistency.dto.SememeSlim;
import dk.ule.oapenwb.persistency.entity.ApiAction;
import dk.ule.oapenwb.persistency.entity.IRPCEntity;
import dk.ule.oapenwb.persistency.entity.Views;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * <p>A Mapping maps two lexemes of different languages but with the same meaning to each other.
 * These lexemes are then translations for each other.</p>
 */
@Data
@Entity
@Audited
@Table(name = "Mappings", uniqueConstraints=@UniqueConstraint(columnNames={
	/*"lexemeOneID", "lexemeTwoID",*/ "sememeOneID", "sememeTwoID"}))
@NoArgsConstructor
public class Mapping implements IRPCEntity<Long>
{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mapping_seq")
	@SequenceGenerator(name = "mapping_seq", sequenceName = "mapping_seq", allocationSize = 1)
	@JsonView(Views.REST.class)
	private Long id;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	@Column(updatable = false)
	@JsonView(Views.REST.class)
	private Integer creatorID;

	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private String langPair;

	/**
	 * This must be conforming to langOne from LangPair.
	 */
	@Column
	@JsonView(Views.REST.class)
	private Long sememeOneID;

	/**
	 * This must be conforming to langTwo from LangPair.
	 */
	@Column
	@JsonView(Views.REST.class)
	private Long sememeTwoID;

	// The backend will supply a slim sememe when a mapping is delivered
	@Transient
	@JsonView(Views.REST.class)
	private SememeSlim sememeOne;

	// The backend will supply a slim sememe when a mapping is delivered
	@Transient
	@JsonView(Views.REST.class)
	private SememeSlim sememeTwo;

	/**
	 * <p>De resultaten wardet nå wicht öärdned. En wicht van 0 is de standard.
	 * Ouk negative wichten sünt möäglik.<p>
	 *
	 * Vgl. etwa linguee.com:
	 * almost always used = 90
	 * often used = 70
	 * common = 50
	 * less common = 30
	 * rare = 10
	 */
	@NotNull
	@Min(0) @Max(100)
	@Column(nullable = false)
	@JsonView(Views.REST.class)
	private short weight = 0;

	@Transient
	@NotNull
	@JsonView(Views.REST.class)
	private ApiAction apiAction = ApiAction.None;

	@JsonView(Views.REST.class)
	@Transient
	//@JsonProperty(required = true)
	private boolean changed = false;

	// TODO Add example entity and link examples to a mapping
}