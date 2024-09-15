// SPDX-FileCopyrightText: © 2024 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb2.service.content;

import dk.ule.oapenwb.persistency.entity.content.basedata.Level;
import dk.ule.oapenwb2.persistence.content.basedata.LevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class LevelService
{
	private final LevelRepository levelRepository;

	public List<Level> getAllLevels() {
		return levelRepository.findAll();
	}
}
