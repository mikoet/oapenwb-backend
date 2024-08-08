// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.ui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.base.Views;
import dk.ule.oapenwb.logic.admin.generic.IEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

/**
 * <p>For the tabular search result the found translations will be grouped into 'result categories',
 * e.g. "Verbs", "Nouns", "Adjectives and adverbs", etc.</p>
 * <p>Those are represented by such UiResultCategory.</p>
 */
@Data
@Entity
@Table(name = "UiResultCategories")
@Audited
@NoArgsConstructor
@AllArgsConstructor
public class UiResultCategory implements IEntity<Integer> {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "uiresultcat_seq")
	@SequenceGenerator(name = "uiresultcat_seq", sequenceName = "uiresultcat_seq", allocationSize = 1)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer id;

	@Version
	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private Integer version;

	@Column(length = 64, nullable = false)
	@JsonView(Views.REST.class)
	private String name;

	@Column(length = 64, nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private String uitID;

	@Column(nullable = false)
	@JsonView({Views.BaseConfig.class, Views.REST.class})
	private short position;

	@Override
	public void setEntityID(Integer id) {  setId(id); }
	@JsonIgnore
	@Override
	public Integer getEntityID() { return getId(); }
}