// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.basis;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.persistency.entity.Views;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

/**
 * Contains a permission for a user to a type of entity and/or specific IDs of that entity.
 * This class is by now only an idea/a prototype.
 */
@Data
@Entity
@Table(name = "Permissions")
@Audited
@NoArgsConstructor
public class Permission
{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "permission_seq")
	@SequenceGenerator(name = "permission_seq", sequenceName = "permission_seq", allocationSize = 20)
	private Integer id;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	@Column(nullable = false)
	private Integer userID;
	@JoinColumn(name = "userID", insertable = false, updatable = false)
	@ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY) // FetchType.EAGER instead?
	private User user;

	@Column(length = 128, nullable = false)
	private String entity;

	@Column(length = 64, nullable = false)
	private String entityID;

	/* CRUD: C = Create, R = Read, U = Update, D = Delete */
	@Column(length = 4, nullable = false)
	private String access;
}