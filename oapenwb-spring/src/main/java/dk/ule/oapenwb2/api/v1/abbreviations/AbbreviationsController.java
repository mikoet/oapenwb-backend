// SPDX-FileCopyrightText: © 2024 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb2.api.v1.abbreviations;

import dk.ule.oapenwb.persistency.entity.content.basedata.Category;
import dk.ule.oapenwb.persistency.entity.content.basedata.Language;
import dk.ule.oapenwb.persistency.entity.content.basedata.Level;
import dk.ule.oapenwb2.api.v1.abbreviations.domain.CategoryDto;
import dk.ule.oapenwb2.api.v1.abbreviations.domain.LanguageDto;
import dk.ule.oapenwb2.api.v1.abbreviations.domain.LevelDto;
import dk.ule.oapenwb2.api.v1.abbreviations.mapper.CategoryMapper;
import dk.ule.oapenwb2.api.v1.abbreviations.mapper.LanguageMapper;
import dk.ule.oapenwb2.api.v1.abbreviations.mapper.LevelMapper;
import dk.ule.oapenwb2.service.content.CategoryService;
import dk.ule.oapenwb2.service.content.LanguageService;
import dk.ule.oapenwb2.service.content.LevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("abbreviations")
@RequiredArgsConstructor
public class AbbreviationsController
{
	private final CategoryMapper categoryMapper;
	private final LanguageMapper languageMapper;
	private final LevelMapper levelMapper;

	private final CategoryService categoryService;
	private final LanguageService languageService;
	private final LevelService levelService;

	@GetMapping(path = "categories")
	public List<CategoryDto> allCategories() {
		final List<Category> categoryList = categoryService.getAllTopLevelCategories();
		return categoryMapper.mapCategoryList(categoryList);
	}

	@GetMapping(path = "languages")
	public List<LanguageDto> allLanguages() {
		final List<Language> languageList = languageService.getAllLanguages();
		return languageMapper.mapLanguageList(languageList);
	}

	@GetMapping(path = "levels")
	public List<LevelDto> allLevels() {
		final List<Level> levelList = levelService.getAllLevels();
		return levelMapper.mapLevelList(levelList);
	}
}
