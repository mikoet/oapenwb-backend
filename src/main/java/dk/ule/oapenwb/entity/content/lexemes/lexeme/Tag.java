// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.content.lexemes.lexeme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.base.Views;
import dk.ule.oapenwb.logic.admin.generic.IEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

@Data @AllArgsConstructor
@NoArgsConstructor
@Entity
@Audited
@Table(name = "Tags")
public class Tag implements IEntity<String> {
	@Id
	@Size(min = 3, max = 32)
	@Pattern(regexp = "^[a-zA-Z0-9 @äâáàåÄÂÁÀÅëêéèËÊÉÈïîíìÏÎÍÌöôóòÖÔÓÒüûúùÜÛÚÙœæŒÆøØ]{3,32}$")
	@Column(length = 32, nullable = false)
	@JsonView(Views.REST.class)
	private String tag;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	@NotNull
	@PositiveOrZero
	@Column(nullable = false)
	@JsonView(Views.REST.class)
	private int usageCount;

	// Guarded tags will not be automatically delete once the usageCount reaches 0
	@Column(nullable = false)
	@JsonView(Views.REST.class)
	private boolean guarded;

	@Override
	public void setEntityID(String id) { setTag(id); }
	@Override @JsonIgnore
	public String getEntityID() { return getTag(); }
}