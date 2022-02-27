// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.content.lexemes.lexeme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import dk.ule.oapenwb.base.Views;
import dk.ule.oapenwb.entity.basis.ApiAction;
import dk.ule.oapenwb.entity.content.lexemes.SynGroup;
import dk.ule.oapenwb.logic.admin.generic.IEntity;
import dk.ule.oapenwb.logic.admin.lexeme.IRPCEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>A Sememe represents one or more meanings of a lexeme.</p>
 * <p>See also: https://en.wikipedia.org/wiki/Lexical_semantics</p>
 */
@Data
@Entity
@Audited
@Table(name = "Sememes")
@NoArgsConstructor
@EqualsAndHashCode
@TypeDefs({ @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class) })
public class Sememe implements IRPCEntity<Long>, IEntity<Long>
{
	public static final int FILL_SPEC_NONE = 1;
	public static final int FILL_SPEC_FROM_TEMPLATE = 2;
	public static final int FILL_SPEC_MANUALLY = 3;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sememe_seq")
	@SequenceGenerator(name = "sememe_seq", sequenceName = "sememe_seq", allocationSize = 1)
	@JsonView(Views.REST.class)
	private Long id;

	@Override
	public void setEntityID(Long id) { setId(id); }
	@Override @JsonIgnore
	public Long getEntityID() { return getId(); }

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

	@NotNull
	@Column(nullable = false)
	@JsonView(Views.REST.class)
	private long lexemeID;

	@NotNull
	@JsonView(Views.REST.class)
	@Column(length = 32)
	private String internalName;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb")
	@JsonView(Views.REST.class)
	@Size(min = 1)
	@NotNull
	private Set<Long> variantIDs = new LinkedHashSet<>();

	// The dialects in which this meaning is valid
	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb")
	@JsonView(Views.REST.class)
	private Set<Integer> dialectIDs = new LinkedHashSet<>();

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb")
	@JsonView(Views.REST.class)
	private Set<Integer> levelIDs = new LinkedHashSet<>();

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb")
	@JsonView(Views.REST.class)
	private Set<Integer> categoryIDs = new LinkedHashSet<>();

	/** !! ny */
	/*
	 * 1 = No specification text
	 * 2 = From template
	 * 3 = Manually
	 */
	@JsonView(Views.REST.class)
	@Column(nullable = false)
	@NotNull
	private int fillSpec;

	// template for determining the specification text in parser syntax
	// example: !way_s.s   – noun way in singular
	// example: !hebben_v.3sp   – verb hebben, 3rd person singular presence
	@Column(length = 256)
	@JsonView(Views.REST.class)
	private String specTemplate;

	// TODO Should the spec be a jsonb structure that contains the specification for several orthographies
	// TODO and/or dialects?
	@Column(length = 64)
	@JsonView(Views.REST.class)
	private String spec; // specification, e.g. <method> for lexeme <way>

	@Valid
	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb")
	@JsonView(Views.REST.class)
	private Map<String, Object> properties = new HashMap<>();

	@JsonView(Views.REST.class)
	@Column
	private boolean active;

	@JsonView(Views.REST.class)
	@Transient
	private boolean changed = false;

	@JsonView(Views.REST.class)
	@Column
	private Integer synGroupID;

	/*
	 * As the persisting of a lexeme is to be run in a single transaction the optional SynGroup a sememe is assigned
	 * to will be transfered here (within the same REST call).
	 */
	@Transient
	@JsonView(Views.REST.class)
	private SynGroup synGroup;

	@Transient
	@NotNull
	@JsonView(Views.REST.class)
	private ApiAction apiAction = ApiAction.None;
}