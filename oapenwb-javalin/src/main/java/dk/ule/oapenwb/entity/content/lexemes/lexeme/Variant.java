// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.content.lexemes.lexeme;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.persistency.entity.ApiAction;
import dk.ule.oapenwb.persistency.entity.IRPCEntity;
import dk.ule.oapenwb.persistency.entity.Views;
import dk.ule.oapenwb.entity.content.lexemes.LexemeForm;
import dk.ule.oapenwb.entity.content.lexemes.MetaInfo;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.*;

/**
 * <p>A variant represents a phonetic and/or written variant of a {@link Lexeme}.</p>
 */
@Data
@Entity
@Audited
@NoArgsConstructor
@Table(name = "Variants")
public class Variant implements IRPCEntity<Long>, Cloneable
{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "variant_seq")
	@SequenceGenerator(name = "variant_seq", sequenceName = "variant_seq", allocationSize = 1)
	@JsonView(Views.REST.class)
	private Long id;

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

	// TODO on delete cascade
	@NotNull
	@Column(nullable = false)
	@JsonView(Views.REST.class)
	private long lexemeID;

	@NotNull
	@Column(nullable = false, updatable = false)
	@JsonView(Views.REST.class)
	private boolean mainVariant;

	@Column(columnDefinition = "jsonb")
	@Type(JsonBinaryType.class)
	@JsonView(Views.REST.class)
	private Set<Integer> dialectIDs = new LinkedHashSet<>();

	// TODO foreign key on delete restrict
	@Column(nullable = false)
	@NotNull
	@JsonView(Views.REST.class)
	private int orthographyID;

	@Transient
	@Valid
	@JsonView(Views.REST.class)
	private List<@Valid LexemeForm> lexemeForms;

	@Embedded
	@NotNull
	@JsonView(Views.REST.class)
	private Lemma lemma;

	@Valid
	@Column(columnDefinition = "jsonb")
	@Type(JsonBinaryType.class)
	@JsonView(Views.REST.class)
	private Set<@Valid MetaInfo> metaInfos;

	@Valid
	@Column(columnDefinition = "jsonb")
	@Type(JsonBinaryType.class)
	@JsonView(Views.REST.class)
	private Map<String, Object> properties = new HashMap<>();

	@JsonView(Views.REST.class)
	@Column(nullable = false)
	private boolean active;

	@JsonView(Views.REST.class)
	@Transient
	private boolean changed = false;

	@Transient
	@NotNull
	@JsonView(Views.REST.class)
	private ApiAction apiAction = ApiAction.None;

	@Override
	public Object clone() throws CloneNotSupportedException {
		// For now we don't care about other complex attributes like metaInfos or properties because this clone() method
		// is only used in lemma building where those don't play a role.
		Variant clone = (Variant) super.clone();
		if (this.lemma != null) {
			clone.lemma = (Lemma) this.lemma.clone();
		}
		return clone;
	}
}