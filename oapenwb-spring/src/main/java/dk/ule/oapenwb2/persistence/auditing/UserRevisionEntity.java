// SPDX-FileCopyrightText: © 2024 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb2.persistence.auditing;

import dk.ule.oapenwb2.persistence.UserRevisionListener;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

/**
 * <p>RevisionEntity for the revisions of data changes in the audited entities.</p>
 *
 * <p>This entity is defined in each of the currently two platforms (<b>oapenwb-javalin</b> and
 * <b>oapenwb-spring</b>). Both entities are mapped for the exact same table and with the exact
 * same columns. The difference, however, is that each of the two entities uses a different
 * RevisionListener which is platform specific.</p>
 */
@Data
@Entity
@Table(name = "RevInfos")
@RevisionEntity(UserRevisionListener.class)
public class UserRevisionEntity
{
	public static final String PLATFORM_SPRING = "spring";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "revision_seq")
	@SequenceGenerator(name = "revision_seq", sequenceName = "revision_seq", allocationSize = 10)
	@RevisionNumber
	private long id;

	@RevisionTimestamp
	@Column(nullable = false)
	private long timestamp;

	/** Should be the name of the platform, e.g. "javalin" or "spring" */
	@Column(length = 8, nullable = false)
	private String platform;

	@Column
	private Integer userID;

	@Column(length = 384)
	private String comment;
}
