// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.content.lexemes.lexeme;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.base.Views;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 * <p>This exmbeddable contains all attributes that are part of the lemma of
 * a {@link Variant}.</p>
 */
@Data
@Embeddable
@NoArgsConstructor
public class Lemma
{
	public static final int FILL_LEMMA_AUTOMATICALLY = -2;
	public static final int FILL_LEMMA_MANUALLY = -1;

	@JsonView(Views.REST.class)
	@Column(length = 32)
	private String pre; // pretext

	// FIXME Actually the content shouldn't be longer than 64 character. See LexemeForm.text for more info.
	@JsonView(Views.REST.class)
	@Column(length = 256, nullable = false)
	@NotNull
	private String main = ""; // maintext

	@JsonView(Views.REST.class)
	@Column(length = 32)
	private String post; // posttext

	@JsonView(Views.REST.class)
	@Column(length = 64)
	private String also; // also: other spelling variation 1, ..2, ...

	/*
	 * -2 = Automatically
	 * -1 = Manually
	 * else: ID of the LemmaTemplate
	 */
	@JsonView(Views.REST.class)
	@Column(nullable = false)
	@NotNull
	private int fillLemma = FILL_LEMMA_AUTOMATICALLY;

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (pre != null && pre.length() > 0) {
			sb.append(pre);
			sb.append(" ");
		}
		sb.append(main);
		if (post != null && post.length() > 0) {
			sb.append(" ");
			sb.append(post);
		}
		return sb.toString();
	}
}