// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.content.basedata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.entity.Views;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lemma;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.entity.IEntity;
import dk.ule.oapenwb.logic.admin.lexeme.LemmaTemplateProcessor;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>A LemmaTemplate specifies how a {@link Variant}'s
 * {@link Lemma} will be filled when the variant is configured to
 * will the lemma automatically by either choosing a LemmaTemplate automatically or when a specific LemmaTemplate
 * is set to fill the lemma.</p>
 * <p>The process of picking a LemmaTemplate automatically is depending on properties like lexemeTypeID, langID and
 * orthographyID, and is done by the {@link LemmaTemplateProcessor}.</p>
 */
@Data
@Entity
@Audited
@Table(
	name = "LemmaTemplates",
	uniqueConstraints=@UniqueConstraint(
		columnNames = {"name", "lexemeTypeID", "langID", "dialectIDs", "orthographyID"}
	)
)
@NoArgsConstructor
@AllArgsConstructor
public class LemmaTemplate implements IEntity<Integer>
{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lemmatemplate_seq")
	@SequenceGenerator(name = "lemmatemplate_seq", sequenceName = "lemmatemplate_seq", allocationSize = 1)
	@JsonView(Views.REST.class)
	private Integer id;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	@Column(length = 64)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private String name;

	@NotNull
	@Column(nullable = false)
	@JsonView(Views.REST.class)
	private int lexemeTypeID;

	@Column
	@JsonView(Views.REST.class)
	private Integer langID;

	@Column(columnDefinition = "jsonb")
	@Type(JsonBinaryType.class)
	@JsonView(Views.REST.class)
	private Set<Integer> dialectIDs = new LinkedHashSet<>();

	@Column
	@JsonView(Views.REST.class)
	private Integer orthographyID;

	@Column(length = 128)
	@JsonView(Views.REST.class)
	private String preText;

	@NotNull
	@Column(length = 256, nullable = false)
	@JsonView(Views.REST.class)
	private String mainText; // hyr steit dän as byspil "$.inf" üm den infinitiv to bruken

	@Column(length = 128)
	@JsonView(Views.REST.class)
	private String postText;

	@Column(length = 128)
	@JsonView(Views.REST.class)
	private String alsoText;

	@Override
	public void setEntityID(Integer id) { setId(id); }
	@JsonIgnore
	@Override
	public Integer getEntityID() { return getId(); }
}