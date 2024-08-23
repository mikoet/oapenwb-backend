// SPDX-FileCopyrightText: © 2024 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb2.config.persistence;

import lombok.Data;

@Data
public class RevisionInfo
{
	private Integer activeUserID;
	private String revisionComment;
}
