// SPDX-FileCopyrightText: © 2024 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb2.config.persistence;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import dk.ule.oapenwb2.persistence.UserRevisionListener;

@Configuration
@EntityScan(basePackages = {"dk.ule.oapenwb2.persistence", "dk.ule.oapenwb.persistency.entity"})
public class PersistenceConfig
{
	/**
	 * <p>Bean to store and access revision related information within request scope. This will be used by
	 * {@link UserRevisionListener} when writing new revisions.</p>
	 *
	 * @return the revision info bean
	 */
	@Bean
	@RequestScope
	public RevisionInfo revisionInfo() {
		return new RevisionInfo();
	}
}
