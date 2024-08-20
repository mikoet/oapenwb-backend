// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.content.lexemes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.entity.Views;
import dk.ule.oapenwb.entity.ApiAction;
import dk.ule.oapenwb.logic.admin.generic.IEntity;
import dk.ule.oapenwb.logic.admin.lexeme.IRPCEntity;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * SynGroup stands for synonym group and groups Sememes together that have about the same meaning, i.e.
 * are synonyms of each other.
 */
@Data
@Entity
@Table(name = "SynGroups")
@Audited
@NoArgsConstructor
public class SynGroup implements IRPCEntity<Integer>, IEntity<Integer>
{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "syngroup_seq")
	@SequenceGenerator(name = "syngroup_seq", sequenceName = "syngroup_seq", allocationSize = 1)
	private Integer id;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	@JsonView(Views.REST.class)
	private Instant createdAt;

	@UpdateTimestamp
	@Column
	@JsonView(Views.REST.class)
	private Instant updatedAt;

	@Column(updatable = false)
	@JsonView(Views.REST.class)
	private Integer creatorID;

	/*
	 * Must contain only sememe IDs. The lexemes of these sememes can be easily queried.
	 */
	@Column(columnDefinition = "jsonb")
	@Type(JsonBinaryType.class)
	@JsonView(Views.REST.class)
	@Size(min = 2)
	@NotNull
	private Set<Long> sememeIDs = new LinkedHashSet<>();

	/*
	 * The presentation contains the presentations of this SynGroup's Lexemes
	 * and will be rendered automatically (in the default-orthography of a languages
	 */
	@JsonView(Views.REST.class)
	@Column(length = 2048, nullable = false)
	private String presentation;

	/**
	 * The meaning is meant to be a descriptive text.
	 *
	 * For example for the three words 'to eat, to break bread, to have a bite'
	 * the descriptive text could be: to put food in your mouth, bite it and swallow it.
	 */
	@JsonView(Views.REST.class)
	@Column(length = 4096)
	private String description;

	@JsonView(Views.REST.class)
	@Transient
	private boolean changed = false;

	@Transient
	@NotNull
	@JsonView(Views.REST.class)
	private ApiAction apiAction = ApiAction.None;

	@Override
	public void setEntityID(Integer id) {
		setId(id);
	}
	@JsonIgnore
	@Override
	public Integer getEntityID() {
		return getId();
	}
}