// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.basis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

/**
 * Used to store the application's version in the database for compatiblity check on startup,
 * as well as for knowing how to update the data tables etc. for a newer application version.
 */
@Data
@Entity
@Table(name = "VersionInfos")
@NoArgsConstructor
@AllArgsConstructor
public class VersionInfo {
	@Id
	@Column(length = 12)
	private String version; // examples: regular version like 01.04.02,
	                        // snapshots like SNAP02.01.05 or more general SNAP00.01

	@Basic
	private Instant actionTS;
}