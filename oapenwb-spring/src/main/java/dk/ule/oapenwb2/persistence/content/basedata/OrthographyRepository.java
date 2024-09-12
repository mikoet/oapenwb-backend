// SPDX-FileCopyrightText: © 2024 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb2.persistence.content.basedata;

import dk.ule.oapenwb.persistency.entity.content.basedata.Orthography;
import dk.ule.oapenwb2.api.v1.abbreviations.domain.OrthographyDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrthographyRepository extends JpaRepository<Orthography, Integer>
{
	@Query(value = """
		SELECT new dk.ule.oapenwb2.api.v1.abbreviations.domain.OrthographyDto(o.abbreviation, o.uitID)
		FROM Orthography o
		INNER JOIN LangOrthoMapping lo ON o.id=lo.orthographyID
		INNER JOIN Language l ON lo.langID=l.id
		WHERE l.id=?1
		""")
	List<OrthographyDto> findAllDtosByLangId(Integer langId);
}
