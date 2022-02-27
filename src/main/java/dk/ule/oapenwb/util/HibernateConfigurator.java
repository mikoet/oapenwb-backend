// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.util;

import org.hibernate.cfg.Configuration;

/**
 * HibernateConfigurator defines the interface
 *
 * TODO COMMENT
 */
public interface HibernateConfigurator
{
	String getHibernateConfigFile();
	void configurateMinimal(Configuration configuration);
	void configurate(Configuration configuration);

	void setCreateTables(boolean createTables);
	boolean isCreateTables();
}