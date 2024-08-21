// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.persistency.entity.content.basedata.tlConfig;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.persistency.entity.Views;
import dk.ule.oapenwb.persistency.entity.content.basedata.LexemeFormType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Assigns a position (or index) to a {@link LexemeFormType}. These
 * positions are then used by the frontend to determine the arrangements of the text fields of the lexeme forms.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormTypePos
{
	@JsonView(Views.REST.class)
	private int formTypeID;

	@JsonView(Views.REST.class)
	private short position;
}