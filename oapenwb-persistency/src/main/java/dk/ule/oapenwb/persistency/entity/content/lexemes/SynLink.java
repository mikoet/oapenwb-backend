// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.persistency.entity.content.lexemes;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.persistency.entity.ApiAction;
import dk.ule.oapenwb.persistency.entity.Views;
import dk.ule.oapenwb.persistency.entity.content.basedata.LinkType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.validation.constraints.NotNull;

/**
 * A SynLink resembles a link between two {@link SynGroup}s. The type – meaning – of
 * a link is defined by the {@link LinkType} and free to set by the editors.
 * In this way SynLinks could be used to map relations between SynGroups like
 * one being an umbrella group for the other one.
 */
@Data
@Entity
@Table(name = "SynLinks")
@Audited
@NoArgsConstructor
public class SynLink {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "synlink_seq")
	@SequenceGenerator(name = "synlink_seq", sequenceName = "synlink_seq", allocationSize = 1)
	private Integer id;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	@NotNull
	@Column(nullable = false)
	@JsonView(Views.REST.class)
	private int typeID;

	@NotNull
	@Column(nullable = false)
	@JsonView(Views.REST.class)
	private long startSynGroupID;

	@NotNull
	@Column(nullable = false)
	@JsonView(Views.REST.class)
	private long endSynGroupID;

	@Transient
	@NotNull
	@JsonView(Views.REST.class)
	private ApiAction apiAction = ApiAction.None;

	@JsonView(Views.REST.class)
	@Transient
	private boolean changed = false;
}