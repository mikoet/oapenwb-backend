// SPDX-FileCopyrightText: © 2024 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb2.api.v1.abbreviations.domain;

import java.util.List;

public record LanguageDto(
	String ownName,
	String uitId,
	String uitIdAbbr,
	List<LanguageDto> dialects,
	List<OrthographyDto> orthographies
) { }
