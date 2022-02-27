// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.basis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.Instant;

/**
 * Key object for a {@link Violation}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ViolationKey implements Serializable {
	@Column(nullable = false)
	private Instant whenTS;

	// Maximum length of 45 for an IPv6 address
	// https://stackoverflow.com/questions/166132/maximum-length-of-the-textual-representation-of-an-ipv6-address
	@Column(nullable = false, length = 45)
	private String ip;
}