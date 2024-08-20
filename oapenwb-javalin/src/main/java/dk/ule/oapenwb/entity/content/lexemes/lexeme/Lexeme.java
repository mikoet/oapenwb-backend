// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.content.lexemes.lexeme;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.entity.ApiAction;
import dk.ule.oapenwb.entity.IRPCEntity;
import dk.ule.oapenwb.entity.Views;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A Lexeme is a word with its different word forms, or a term, expressed in one language.
 */
@Data
@Entity
@Audited
@NoArgsConstructor
@Table(name = "Lexemes")
public class Lexeme implements IRPCEntity<Long>
{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lexeme_seq")
	@SequenceGenerator(name = "lexeme_seq", sequenceName = "lexeme_seq", allocationSize = 1)
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

	// for this kind of property double definition see:
	// https://stackoverflow.com/a/50378345
	// TODO foreign key on delete restrict
	@NotNull
	@JsonView(Views.REST.class)
	@Column(nullable = false)
	private int langID;

	// TODO foreign key on delete restrict
	@Min(1)
	@NotNull
	@JsonView(Views.REST.class)
	@Column(nullable = false)
	private Integer typeID;

	// Attribute containing an ID for access per term parser (!(gan_v.inf))
	@JsonView(Views.REST.class)
	@Size(min = 3, max = 48)
	@Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9]{0,46}_[a-zA-Z]{1,10}$")
	@Column(length = 48)
	private String parserID;

	@Valid
	@Column(columnDefinition = "jsonb")
	@Type(JsonBinaryType.class)
	@JsonView(Views.REST.class)
	private Set<@Size(min=3, max=32) String> tags = new LinkedHashSet<>();

	// Markdown notes
	@JsonView(Views.REST.class)
	@Column(length = 8192)
	private String notes;

	/**
	 * <p>If this property is not <b>null</b>, the lexeme forms that will be shown for
	 * this lexeme will be taken from the linked lexme.</p>
	 * <p>If the linked lexeme has no details but has variants, then the details shall be taken from the
	 * variants.</p>
	 */
	// TODO foreign key on delete cascade
	@JsonView(Views.REST.class)
	@Column
	private Long showVariantsFrom;

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
}