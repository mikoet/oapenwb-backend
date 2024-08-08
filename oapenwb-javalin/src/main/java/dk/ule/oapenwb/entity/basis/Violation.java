// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.basis;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * A violation is part of a security mechanism to protect the backend against threats like password attacks (brutforce),
 * mass registration of accounts, and massive use of forget password functionality.
 */
@Data
@Entity
@Table(name = "Violation", indexes = { @Index(columnList = "type", name = "Violation_type_idx") })
@NoArgsConstructor
public class Violation
{
	@EmbeddedId
	private ViolationKey key;

	@Column(columnDefinition = "character", length = 1, nullable = false)
	@Convert(converter = ViolationTypeConverter.class)
	private ViolationType type;

	@Column(nullable = false, length = 256)
	private String info;

	public Violation(Instant when, String ip, ViolationType type, String info)
	{
		this.key = new ViolationKey(when, ip);
		this.type = type;
		this.info = info;
	}
}