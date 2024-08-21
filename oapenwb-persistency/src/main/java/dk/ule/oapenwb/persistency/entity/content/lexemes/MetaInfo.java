// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.persistency.entity.content.lexemes;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.persistency.entity.Views;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Variant;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * {@link Variant}s can be annotated with meta information.
 * Those can e.g. be used to contain information for a generator service to generate the LexemeForms.
 */
@Data
@NoArgsConstructor
public class MetaInfo implements Serializable
{
	@NotNull
	@Size(min = 1, max = 64)
	@JsonView(Views.REST.class)
	private String ident;

	@NotNull
	@Size(max = 1024)
	@JsonView(Views.REST.class)
	private String value;
}