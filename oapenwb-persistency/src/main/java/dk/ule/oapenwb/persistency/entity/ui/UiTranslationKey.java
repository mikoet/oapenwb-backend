// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.persistency.entity.ui;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class UiTranslationKey implements Serializable {
	@Column(nullable = false, length = 32)
	@Pattern(regexp = "([A-Za-z0-9]{2,31}\\.)?[A-Za-z0-9_\\-:!]{2,32}")
	private String id;

	// if scope is an empty string then the translation belongs to the default scope
	@Column(nullable = false, length = 31) // 31 because of the . in a used uitID
	private String scopeID;

	@Column(nullable = false, length = 32)
	private String locale;
}