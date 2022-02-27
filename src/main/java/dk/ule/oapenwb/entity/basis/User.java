// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.basis;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.base.Views;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * A registered user has a User instance.
 */
@Data
@Entity
@Audited
@Table(name = "Users")
@NoArgsConstructor
//@JsonIgnoreProperties({ "type", "email", "" })
public class User
{
	/**
	 * Type for users that directly registered.
	 */
	public static final byte TYPE_DIRECT = 1;
	/**
	 * Type for users that registered via Facebook.
	 */
	public static final byte TYPE_FACEBOOK = 5;

	public User(final String email, final String username, final byte[] pwHash, final char[] salt,
				final String firstname, final String lastname)
	{
		setEmail(email);
		setUsername(username);
		setPwHash(pwHash);
		setSalt(salt);

		setType(User.TYPE_DIRECT);
		setFirstname(firstname);
		setLastname(lastname);
		setJoinTS(Instant.now());
		setLastActiveTS(null);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
	@SequenceGenerator(name = "user_seq", sequenceName = "user_seq", allocationSize = 5)
	private Integer id;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	// Type of the account, the TYPE_DIRECT, TYPE_FACEBOOK
	@Column(nullable = false)
	private byte type;

	@Column(length = 320, unique = true, nullable = false)
	// Part of json
	private String email;

	@Column(length = 32, unique = true, nullable = true)
	// Part of json
	private String username;

	@Column(length = 32, nullable = false)
	// Part of json
	private String firstname;

	@Column(length = 32)
	// Part of json
	private String lastname;

	@Column
	private ShowName showName = ShowName.Firstname_FirstLetterLastname;

	@Column(length = 20)
	private char[] salt;

	// in PostgeSQL dialect the Lob is mapped to OID instead of bytea, and that seems to cause problems.
	// Just using byte[] without any special annotation seems to work fine, though.
	//@Lob
	@Column(length = 48)
	private byte[] pwHash;

	@Column(nullable = false)
	boolean activated = false;

	@Column(nullable = false)
	// Part of json
	private Instant joinTS;

	/**
	 * The user has never been active as long as the last active timestamp is null.
	 */
	@Column
	// Part of json
	private Instant lastActiveTS;

	// Failed logins in a row. Will be set to 0 after successful login.
	@Column
	private int failedLogins = 0;

	// Part of json
	@Column(length = 64)
	private String facebookID = null;

	@Column(columnDefinition = "character", length = 1, nullable = false)
	@Convert(converter = RoleTypeConverter.class)
	private RoleType role = RoleType.User;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private Set<Permission> permissions = new HashSet<>();

	/*
	 * TODO
	 * Vielleicht braucht es noch weitere Eigenschaften, ggf. auch in einer eigenen Tabelle:
	 * - Wie viele Vorschläge wurden vom Nutzer angenommen und wie viele eingereicht?
	 *   - damit können automatische Einreichungen begrenzt werden als Sicherheitsmaßnahme
	 *   - oder alternativ begrenzen auf 5 pro Stunde
	 * - Statistikwerte?
	 */
}