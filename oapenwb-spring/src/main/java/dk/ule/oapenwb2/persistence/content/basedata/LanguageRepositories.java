// SPDX-FileCopyrightText: © 2024 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb2.persistence.content.basedata;

import dk.ule.oapenwb.persistency.entity.content.basedata.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LanguageRepositories extends JpaRepository<Language, Integer>
{
}
