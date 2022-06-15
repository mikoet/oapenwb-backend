// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.content.lexemes;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.base.Views;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

/**
 * A form of a lexeme assigned to a variant of a lexeme.
 */
@Data
@Table(name = "LexemeForms")
@Entity
@Audited
@NoArgsConstructor
public class LexemeForm implements Serializable
{
	public static final byte STATE_TYPED = 1;
	public static final byte STATE_GENERATED = 2;
	public static final byte STATE_GENERATED_OVERWRITTEN = 3; // was generated, but overwritten by editor
	public static final byte STATE_GENERATED_PROTECTED = 4; // was generated and cannot be overwritten

	// Link to the Variant (must be nullable for creation of Lexemes)
	@Id
	@JsonView(Views.REST.class)
	private Long variantID;

	// Link to the LexemeFormType
	@Id
	@NotNull
	@JsonView(Views.REST.class)
	private Integer formTypeID;

	@NotNull
	@Min(STATE_TYPED)
	@Max(STATE_GENERATED_PROTECTED)
	@Column(nullable = false)
	@JsonView(Views.REST.class)
	private byte state = STATE_TYPED;

	@NotNull
	@Size(min = 1, max = 64)
	@Column(length = 64)
	@JsonView(Views.REST.class)
	private String text;

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LexemeForm that = (LexemeForm) o;

		return Objects.equals(this.variantID, that.variantID) &&
				   Objects.equals(this.formTypeID, that.formTypeID) &&
				   state == that.state &&
				   Objects.equals(this.text, that.text);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(variantID, formTypeID);
	}
}
