// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.content.lexemes;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.base.Views;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.Instant;

/**
 * An instance of class Audio represents an audio sample of a LexemeForm.
 */
@Data
@Entity
@Table(name = "Audios")
@Audited
@NoArgsConstructor
public class Audio
{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audio_seq")
	@SequenceGenerator(name = "audio_seq", sequenceName = "audio_seq", allocationSize = 1)
	private Long id;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	// ID of the main lexeme
	@Column(nullable = false)
	private Long lexemeID;

	// ID of the variation and formType
	@Column(nullable = false)
	private Long variantID;
	@Column(nullable = false)
	private Integer formTypeID;
	// TODO Reference other tables in database

	@Column
	private Integer userID;
	/* TODO Reference it
	@JsonIgnore
	@JoinColumn(name = "creatorID", insertable = false, updatable = false)
	@ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
	private User creator;
	 */

	@Column(length = 256, nullable = false)
	private String filename;

	@Column(nullable = false)
	private boolean active = false;

	@Basic
	@Column(nullable = false)
	private Instant uploadTs;
}