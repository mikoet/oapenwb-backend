// SPDX-FileCopyrightText: © 2024 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb2.service.content;

import dk.ule.oapenwb.persistency.entity.content.basedata.Category;
import dk.ule.oapenwb2.persistence.content.basedata.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService
{
	private final CategoryRepository categoryRepository;

	public List<Category> getAllTopLevelCategories() {
		return this.categoryRepository.findAllByParentID(null);
	}
}
