// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.content.basedata.tlConfig;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.base.Views;
import dk.ule.oapenwb.entity.content.basedata.LexemeFormType;
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
import java.util.LinkedList;
import java.util.List;

/**
 * <p>The TypeLangConfig is the place at which configuration is stored that belongs to the relation between
 * a language and a lexeme type. As of now this stored configuration conists only of a list of
 * {@link FormTypePos} instances allowing to define which {@link LexemeFormType}
 * will shall be shown in the UI as well as the positioning/order of these.</p>
 * <p>This makes it possible to arrange the input fields for the LexemeForms for each LexemeType depending on the
 * actual language.</p>
 */
@Data
@Entity
@Table(name = "TypeLanguageConfigs", uniqueConstraints=@UniqueConstraint(columnNames={"lexemeTypeID", "langID"}))
@Audited
@NoArgsConstructor
@AllArgsConstructor
public class TypeLanguageConfig implements IEntity<Integer>
{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "typelang_seq")
	@SequenceGenerator(name = "typelang_seq", sequenceName = "typelang_seq", allocationSize = 1)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer id;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	@NotNull
	@Column(nullable = false)
	@JsonView(Views.REST.class)
	private int lexemeTypeID;

	@NotNull
	@Column(nullable = false)
	@JsonView(Views.REST.class)
	private int langID;

	@Valid
	@Column
	@Type(JsonBinaryType.class)
	@JsonView(Views.REST.class)
	private List<FormTypePos> formTypePositions = new LinkedList<>();

	@Override
	public void setEntityID(Integer id) { setId(id); }
	@JsonIgnore
	@Override
	public Integer getEntityID() { return getId(); }
}