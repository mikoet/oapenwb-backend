// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.basis;

import dk.ule.oapenwb.data.UserRevisionListener;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

/**
 * RevisionEntity for the revisions of data changes in the audited entities.
 */
@Data
@Entity
@Table(name = "RevInfos")
@RevisionEntity(UserRevisionListener.class)
public class UserRevisionEntity /*extends DefaultRevisionEntity*/
{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "revision_seq")
	@SequenceGenerator(name = "revision_seq", sequenceName = "revision_seq", allocationSize = 10)
	@RevisionNumber
	private long id;

	@RevisionTimestamp
	private long timestamp;

	@Column
	private Integer userID;

	@Column(length = 384)
	private String comment;
}