// SPDX-FileCopyrightText: © 2024 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb2.persistence.content.basedata;

import dk.ule.oapenwb.persistency.entity.content.basedata.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Integer>
{
	@Query("SELECT l FROM Language l WHERE l.parentID IS null ORDER BY l.localName")
	List<Language> findAllTopLevelLanguages();

	List<Language> findAllByParentIDOrderByLocalName(Integer parentID);
}
