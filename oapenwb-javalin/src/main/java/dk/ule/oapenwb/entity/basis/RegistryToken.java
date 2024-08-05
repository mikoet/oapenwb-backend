// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.basis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * In order to register for an account on the dictionary a visitor needs a registry token.
 * Those tokens are stored via this entity.
 */
@Data
@Entity
@Table(name = "RegistryTokens")
@NoArgsConstructor
public class RegistryToken
{
	// Unique random string of 16 characters length
	@Id
	@Column(length = 16)
	private String token;

	// Can be null for a token to be used by everyone
	@Column(length = 320, unique = true)
	private String email;

	// Every token expires
	@Column(nullable = false)
	private Instant validUntil;

	// Once used tokens are kept in store
	@Column(nullable = false)
	boolean used;
}