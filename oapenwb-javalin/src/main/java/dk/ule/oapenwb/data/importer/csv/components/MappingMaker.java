// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.data.importer.csv.CsvImporterConfig;
import dk.ule.oapenwb.data.importer.csv.CsvImporterContext;
import dk.ule.oapenwb.entity.ApiAction;
import dk.ule.oapenwb.entity.content.basedata.LangPair;
import dk.ule.oapenwb.entity.content.lexemes.Mapping;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MappingMaker
{
	protected final MakerMode mode;

	protected final AdminControllers adminControllers;

	@Getter
	protected final String langPair;

	@Getter
	protected final String startLang;

	@Getter
	protected final String endLang;

	@Getter
	protected final short defaultWeight;

	@Getter
	protected final String messageContext;

	public MappingMaker(
		MakerMode mode,
		AdminControllers adminControllers,
		String langPair,
		short defaultWeight) throws CodeException
	{
		this.mode = mode;
		this.adminControllers = adminControllers;
		this.langPair = langPair;
		this.defaultWeight = defaultWeight;
		this.messageContext = String.format("MappingMaker '%s'", langPair);

		LangPair langPairObj = adminControllers.getLangPairsController().get(langPair);
		this.startLang = langPairObj.getLangOne().getLocale();
		this.endLang = langPairObj.getLangTwo().getLocale();
	}

	public List<Mapping> build(
		CsvImporterConfig config,
		CsvImporterContext context,
		Map<String, List<Long>> sememeIDsByLocale)
	{
		List<Mapping> result;

		switch (mode) {
			case SingleAndMulti -> result = buildSingleAndMulti(config, context, sememeIDsByLocale);
			default -> throw new RuntimeException(String.format("Unsupported mode: %s", mode));
		}

		return result;
	}

	protected List<Mapping> buildSingleAndMulti(
		CsvImporterConfig config,
		CsvImporterContext context,
		Map<String, List<Long>> sememeIDsByLocale)
	{
		// Retrieve the start sememe ID, or do error handling
		LexemeProvider startProvider = config.getLexemeProviders().get(startLang);
		List<Long> startSememeIDs = sememeIDsByLocale.get(startLang);
		if (startSememeIDs == null || startSememeIDs.size() == 0) {
			if (startProvider.isMustProvide()) {
				throw new RuntimeException(String.format(
					"No sememe IDs found for start language '%s' to create the desired mapping", startLang));
			}
			return null;
		}
		Long sememeOneID = startSememeIDs.get(0);

		// Retrieve the end sememe IDs, or do error handling
		MultiLexemeProvider endProvider = config.getMultiLexemeProviders().get(endLang);
		List<Long> endSememeIDs = sememeIDsByLocale.get(endLang);
		if (endSememeIDs == null || endSememeIDs.size() == 0) {
			if (endProvider.isMustProvide()) {
				throw new RuntimeException(String.format(
					"No sememe IDs found for end language '%s' to create the desired mapping", endLang));
			}
			return null;
		}

		// Build the Mapping instances
		List<Mapping> result = new LinkedList<>();
		for (Long sememeTwoID : endSememeIDs) {
			Mapping mapping = new Mapping();
			mapping.setLangPair(langPair);
			mapping.setSememeOneID(sememeOneID);
			mapping.setSememeTwoID(sememeTwoID);
			mapping.setWeight(defaultWeight);
			mapping.setApiAction(ApiAction.Insert);
			mapping.setChanged(true);

			result.add(mapping);
		}

		return result;
	}
}
