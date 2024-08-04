// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.data.importer.csv.CsvImporterConfig;
import dk.ule.oapenwb.data.importer.csv.CsvImporterContext;
import dk.ule.oapenwb.entity.basis.ApiAction;
import dk.ule.oapenwb.entity.content.basedata.LinkType;
import dk.ule.oapenwb.entity.content.lexemes.Link;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LinkMaker
{
	protected final MakerMode mode;

	protected final AdminControllers adminControllers;

	@Getter
	protected final int typeID;

	@Getter
	protected final String startLang;

	@Getter
	protected final String endLang;

	@Getter
	protected final String messageContext;

	public LinkMaker(MakerMode mode, AdminControllers adminControllers, LinkType type, String startLang, String endLang)
	{
		this.mode = mode;
		this.adminControllers = adminControllers;
		this.typeID = type.getId();
		this.startLang = startLang;
		this.endLang = endLang;
		this.messageContext = String.format("LinkMaker '%s'", type.getDescription());
	}

	public List<Link> build(
		CsvImporterConfig config,
		CsvImporterContext context,
		Map<String, List<Long>> sememeIDsByLocale)
	{
		List<Link> result = null;

		switch (mode) {
			case SingleAndSingle -> {
				result = buildSingleAndSingle(config, context, sememeIDsByLocale);
			}
			case SingleAndMulti -> {
				result = buildSingleAndMulti(config, context, sememeIDsByLocale);
			}
			default -> throw new RuntimeException(String.format("Unsupported mode: %s", mode));
		}

		return result;
	}

	protected List<Link> buildSingleAndSingle(
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
					"No sememe IDs found for start language '%s' to create the desired link", startLang));
			}
			return null;
		}
		Long startSememeID = startSememeIDs.get(0);

		// Retrieve the end sememe IDs, or do error handling
		MultiLexemeProvider endProvider = config.getMultiLexemeProviders().get(endLang);
		List<Long> endSememeIDs = sememeIDsByLocale.get(endLang);
		if (endSememeIDs == null || endSememeIDs.size() == 0) {
			if (endProvider.isMustProvide()) {
				throw new RuntimeException(String.format(
					"No sememe IDs found for end language '%s' to create the desired link", endLang));
			}
			return null;
		}
		Long endSememeID = endSememeIDs.get(0);

		// Build the Link instance
		Link link = new Link();
		link.setTypeID(typeID);
		link.setStartSememeID(startSememeID);
		link.setEndSememeID(endSememeID);
		link.setApiAction(ApiAction.Insert);
		link.setChanged(true);

		List<Link> result = new LinkedList<>();
		result.add(link);

		return result;
	}

	protected List<Link> buildSingleAndMulti(
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
					"No sememe IDs found for start language '%s' to create the desired link", startLang));
			}
			return null;
		}
		Long startSememeID = startSememeIDs.get(0);

		// Retrieve the end sememe IDs, or do error handling
		MultiLexemeProvider endProvider = config.getMultiLexemeProviders().get(endLang);
		List<Long> endSememeIDs = sememeIDsByLocale.get(endLang);
		if (endSememeIDs == null || endSememeIDs.size() == 0) {
			if (endProvider.isMustProvide()) {
				throw new RuntimeException(String.format(
					"No sememe IDs found for end language '%s' to create the desired link", endLang));
			}
			return null;
		}

		// Build the Link instances
		List<Link> result = new LinkedList<>();
		for (Long endSememeID : endSememeIDs) {
			Link link = new Link();
			link.setTypeID(typeID);
			link.setStartSememeID(startSememeID);
			link.setEndSememeID(endSememeID);
			link.setApiAction(ApiAction.Insert);
			link.setChanged(true);
			result.add(link);
		}

		return result;
	}
}
