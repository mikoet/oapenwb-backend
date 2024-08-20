// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.content.lexemes;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.entity.Views;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

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
	// FIXME Actually the content of a LexemeForm shouldn't be longer than 64 character.
	//   In the first run, though, we'll have descriptions in the text attribute being longer and
	//   only give a warning on import (and creation?) when a text is longer than 64 characters.
	public static final int TEXT_WARN_LENGTH = 64;
	public static final int TEXT_MAX_LENGTH = 256;

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
	@Size(min = 1, max = TEXT_MAX_LENGTH)
	@Column(length = TEXT_MAX_LENGTH)
	@JsonView(Views.REST.class)
	private String text;

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LexemeForm that = (LexemeForm) o;

		return Objects.equals(this.variantID, that.variantID)
			&& Objects.equals(this.formTypeID, that.formTypeID)
			&& this.state == that.state
			&& Objects.equals(this.text, that.text);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(variantID, formTypeID);
	}
}
