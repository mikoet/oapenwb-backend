// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.basis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * A ban contains information about an IP address being banned until a specified time.
 */
@Data
@Entity
@Table(name = "Bans")
@NoArgsConstructor
@AllArgsConstructor
public class Ban implements Serializable
{
	@Id
	@Column(length = 45, nullable = false)
	private String ip;

	@Id
	private Instant bannedUntil;
}