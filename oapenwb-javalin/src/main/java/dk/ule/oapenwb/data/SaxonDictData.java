// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data;

import dk.ule.oapenwb.persistency.entity.content.basedata.*;
import dk.ule.oapenwb.persistency.entity.ui.UiResultCategory;
import dk.ule.oapenwb.util.Pair;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Initial data specific to the (Low) Saxon dictionary.
 */
public class SaxonDictData implements DataStrategy
{
	private final Map<String, Language> langMap = new HashMap<>();

	@Override
	public void createData(final Session session) {
		createLangData(session);
		createLexemeTypes(session);
		createMetaTagData(session);
	}

	private void createLangData(final Session session) {
		// Setup orthographies
		Orthography oNSS = new Orthography(null, null, "o:nss", Orthography.ABBR_SAXON_NYSASSISKE_SKRYVWYSE, "Nysassiske Skryvwyse", true);
		Orthography oDBO = new Orthography(null, null, "o:dbo", Orthography.ABBR_SAXON_GERMAN_BASED, "Düütschbaseerde Schriefwies", true);
		Orthography oNBO = new Orthography(null, null, "o:nbo", Orthography.ABBR_SAXON_DUTCH_BASED, "Nederlands gebaseerde orthografie", true);

		Orthography oBDR = new Orthography(null, null, "o:bdr", Orthography.ABBR_GERMAN_FEDERAL, "Bundesdeutsche Rechtschreibung", true);
		Orthography oBE = new Orthography(null, null, "o:be", Orthography.ABBR_ENGLISH_BRITISH, "British English", true);
		Orthography oNL = new Orthography(null, null, "o:nlo", Orthography.ABBR_DUTCH, "Nederlandse spelling", true);
		Orthography oDA = new Orthography(null, null, "o:dao", Orthography.ABBR_DANISH, "Dansk ortografi?", true);
		Orthography oSV = new Orthography(null, null, "o:svo", Orthography.ABBR_SWEDISH, "Svensk ortografi?", true);
		Orthography oFI = new Orthography(null, null, "o:fio", Orthography.ABBR_FINNISH, "Suomi ??", true);
		Orthography oBN = new Orthography(null, null, "o:bino", Orthography.ABBR_BINOMIAL_NOMENCLATURE, "Binomial nomenclature", true);

		session.save(oNSS);
		session.save(oDBO);
		session.save(oNBO);

		session.save(oBDR);
		session.save(oBE);
		session.save(oNL);
		session.save(oDA);
		session.save(oSV);
		session.save(oFI);
		session.save(oBN);

		// Setup languages
		Language lSaxon = new Language(null, null, "nds", "Neddersassisk", "l:nds", "l:nds", oNSS.getId(), "nds");
		Language lGerman = new Language(null, null, "de", "Deutsch", "l:de", "l:de", oBDR.getId());
		Language lEnglish = new Language(null, null, "en", "English", "l:en", "l:en", oBE.getId());
		Language lDutch = new Language(null, null, "nl", "Nederlands", "l:nl", "l:nl", oNL.getId());
		Language lDanish = new Language(null, null, "da", "Dansk", "l:da", "l:da", oDA.getId());
		Language lSwedish = new Language(null, null, "sv", "Svenska", "l:sv", "l:sv", oSV.getId());
		Language lFinnish = new Language(null, null, "fi", "Suomi", "l:fi", "l:fi", oFI.getId());
		Language lBino = new Language(null, null, "bino", "Binomial nomenclature", "l:bino", "l:bino", oBN.getId());

		session.save(lSaxon);
		session.save(lGerman);
		session.save(lEnglish);
		session.save(lDutch);
		session.save(lDanish);
		session.save(lSwedish);
		session.save(lFinnish);
		session.save(lBino);


		// !! Dialects of Low Saxon

		// - DE-sassisk
		Language lDeSassisk = createLsDialect(session, lSaxon, "nds_DE", "DE-sassisk", oNSS.getId(), "ndsde",
			"dt. sassisk", "dt. niedersächsisch", "German Low Saxon", "", "", "");


		// -- Northern Low Saxon
		Language lNorthernLowSaxon = createLsDialect(session, lDeSassisk, "nds_DE@nnds", "Sleeswyksk", oNSS.getId(), "dns",
			"Noordneddersassisk", "Nordniedersächsisch", "Northern Low Saxon", "", "", "");

		createLsDialect(session, lNorthernLowSaxon, "nds_DE@dns-sle", "Sleeswyksk", oNSS.getId(), "sle",
			"Sleeswyksk", "Schleswigsch", "Sleswicksh", "", "", "");

		createLsDialect(session, lNorthernLowSaxon, "nds_DE@dns-hol", "Holsteynsk", oNSS.getId(), "hol",
			"Holsteynsk", "Holsteinisch", "Holsatian", "", "", "");

		createLsDialect(session, lNorthernLowSaxon, "nds_DE@dns-dit", "Ditmarsk", oNSS.getId(), "dit",
			"Ditmarsk", "Dithmarsch", "Dithmarsh", "", "", "");

		createLsDialect(session, lNorthernLowSaxon, "nds_DE@dns-nhn", "Noordhannoversk", oNSS.getId(), "nhn",
			"Noordhannoversk", "Nordhannoversch", "Northern Hanoveranian", "", "", "");

		createLsDialect(session, lNorthernLowSaxon, "nds_DE@dns-olb", "Oldenborgsk", oNSS.getId(), "olb",
			"Oldenborgsk", "Oldenburgisch", "Oldenburgish", "", "", "");

		createLsDialect(session, lNorthernLowSaxon, "nds_DE@dns-ofr", "Oustfreesk", oNSS.getId(), "ofr",
			"Oustfreesk", "Ostfriesisch", "East Frisian", "", "", "");

		createLsDialect(session, lNorthernLowSaxon, "nds_DE@dns-ems", "Emslandsk", oNSS.getId(), "ems",
			"Emslandsk", "Emsländisch", "Emslandic", "", "", "");


		// -- Westphalian
		Language lWestphalian = createLsDialect(session, lDeSassisk, "nds_DE@dwf", "Westföälsk", oNSS.getId(), "dwf",
			"Westföälsk", "Westfälisch", "Westphalian", "", "", "");

		createLsDialect(session, lWestphalian, "nds_DE@dwf-mön", "Mönsterlandsk", oNSS.getId(), "mön",
			"Mönsterlandsk", "Münsterländisch", "Münsterlandic", "", "", "");

		createLsDialect(session, lWestphalian, "nds_DE@dwf-wmö", "Westmönsterlandsk", oNSS.getId(), "wmö",
			"Westmönsterlandsk", "Westmünsterländisch", "Western Münsterlandic", "", "", "");

		createLsDialect(session, lWestphalian, "nds_DE@dwf-owf", "Oustwestföälsk", oNSS.getId(), "owf",
			"Oustwestföälsk", "Ostwestfälisch", "Eastern Westphalian", "", "", "");

		createLsDialect(session, lWestphalian, "nds_DE@dwf-swf", "Süüdwestföälsk", oNSS.getId(), "swf",
			"Süüdwestföälsk", "Südwestfälisch", "Southern Westphalian", "", "", "");


		// -- Eastphalian
		Language lEastphalian = createLsDialect(session, lDeSassisk, "nds_DE@of", "Oustföälsk", oNSS.getId(), "ofl",
			"Oustföälsk", "Ostfälisch", "Eastphalian", "", "", "");

		createLsDialect(session, lEastphalian, "nds_DE@of-hof", "Heideoustföälsk", oNSS.getId(), "hof",
			"Heideoustföälsk", "Heideostfälisch", "Heathland Eastphalian", "", "", "");

		createLsDialect(session, lEastphalian, "nds_DE@of-kof", "Karnoustföälsk", oNSS.getId(), "kof",
			"Karnoustföälsk", "Kernostfälisch", "Coreland Eastphalian", "", "", "");

		createLsDialect(session, lEastphalian, "nds_DE@of-gög", "Göttingsk-grubenhagensk", oNSS.getId(), "gög",
			"Göttingsk-grubenhagensk", "Göttingisch-Grubenhagensch", "Göttingic-Grubenhagic", "", "", "");

		createLsDialect(session, lEastphalian, "nds_DE@of-eof", "Elvoustföälsk", oNSS.getId(), "eof",
			"Elvoustföälsk", "Elbeostfälisch", "Elbe Eastphalian", "", "", "");


		// -- Mecklenburgish-Western Pomeranian
		Language lMecklenburgishWesternPomeranian = createLsDialect(session, lDeSassisk, "nds_DE@mvp", "Meakelenborgsk-vöärpommersk",
			oNSS.getId(), "mvp", "Meakelenborgsk-vöärpommersk", "Mecklenburgisch-Vorpommersch", "Mecklenburgish-Western Pomeranian",
			"", "", "");

		createLsDialect(session, lMecklenburgishWesternPomeranian, "nds_DE@mvp-mkb", "Meakelenborgsk", oNSS.getId(), "mkb",
			"meakelenborgsk", "Mecklenburgisch", "Mecklenburgisch", "", "", "");

		createLsDialect(session, lMecklenburgishWesternPomeranian, "nds_DE@mvp-vpo", "Vöärpommersk", oNSS.getId(), "vpo",
			"Vöärpommersk", "Vorpommersch", "Western Pomeranian", "", "", "");

		createLsDialect(session, lMecklenburgishWesternPomeranian, "nds_DE@mvp-str", "Strelitzsk", oNSS.getId(), "str",
			"Strelitzsk", "Strelitzisch", "Strelitzish", "", "", "");


		// -- Brandenburgish
		Language lBrandenburgish = createLsDialect(session, lDeSassisk, "nds_DE@bra", "Brandenborgsk", oNSS.getId(), "bra",
			"Brandenborgsk", "Brandenburgisch", "Brandenburgish", "", "", "");

		createLsDialect(session, lBrandenburgish, "nds_DE@bra-nbr", "Noordbrandenborgsk", oNSS.getId(), "nbr",
			"Noordbrandenborgsk", "Nordbrandenburgisch", "Northern Brandenburgish", "", "", "");

		createLsDialect(session, lBrandenburgish, "nds_DE@bra-mpo", "Middelpommersk", oNSS.getId(), "mpo",
			"Middelpommersk", "Mittelpommersch", "Central Pomeranian(?)", "", "", "");

		createLsDialect(session, lBrandenburgish, "nds_DE@bra-mbr", "Middelbrandenborgsk", oNSS.getId(), "mbr",
			"Middelbrandenborgsk", "Mittelbrandenburgisch", "Central Brandenburgish", "", "", "");

		createLsDialect(session, lBrandenburgish, "nds_DE@bra-sbr", "Süüdbrandenborgsk", oNSS.getId(), "sbr",
			"Süüdbrandenborgsk", "Südbrandenburgisch", "Southern Brandenburgish", "", "", "");


		// -- Eastern Pomeranian
		createLsDialect(session, lDeSassisk, "nds_DE@pom", "Oustpommersk", oNSS.getId(), "pom",
			"Oustpommersk", "Ostpommersch", "Eastern Pomeranian", "", "", "");


		// -- Low Prussian
		createLsDialect(session, lDeSassisk, "nds_DE@npr", "Nedderprüüssisk", oNSS.getId(), "npr",
			"Nedderprüüssisk", "Niederpreußisch", "Low Prussian", "", "", "");


		// - NL-sassisk
		Language lNlSassisk = createLsDialect(session, lSaxon, "nds_NL", "NL-sassisk", oNSS.getId(), "ndsnl",
			"ndl. sassisk", "ndl. Niedersächsisch", "Dutch Low Saxon", "nds_NL", "nds_NL", "nds_NL");


		// -- NL-sassisk noord
		Language lNorternNlSassisk = createLsDialect(session, lNlSassisk, "nds_NL@nns", "NL-sassisk noord", oNSS.getId(), "nns",
			"NL-sassisk noord", "ndl. Nordniedersächsisch", "Northern Dutch Low Saxon", "", "", "");

		createLsDialect(session, lNorternNlSassisk, "nds_NL@nns-gro", "Grönningsk", oNSS.getId(), "gro",
			"Grönningsk", "Gronningsch", "Gronings", "", "", "");


		// -- NL-sassisk süüd
		Language lSouthernNlSassisk = createLsDialect(session, lNlSassisk, "nds_NL@nwf", "NL-sassisk süüd", oNSS.getId(), "nwf",
			"NL-sassisk süüd", "ndl. Südniedersächsisch", "Southern Dutch Low Saxon", "", "", "");

		createLsDialect(session, lSouthernNlSassisk, "nds_NL@nwf-stw", "Stellingwervsk", oNSS.getId(), "stw",
			"Stellingwervsk", "Stellingwerfs", "Stellingwarfs", "", "", "");

		createLsDialect(session, lSouthernNlSassisk, "nds_NL@nwf-dre", "Drentsk", oNSS.getId(), "dre",
			"Drentsk", "Drents", "Drents", "", "", "");

		Language lOaverysselsk = createLsDialect(session, lSouthernNlSassisk, "nds_NL@nwf-ovy", "Öäverysselsk", oNSS.getId(), "ovy",
			"Öäverysselsk", "Oberysselsch", "Overijssels", "", "", "");

		createLsDialect(session, lOaverysselsk, "nds_NL@nwf-ovy-sal", "Sallandsk", oNSS.getId(), "sal",
			"Sallandsk", "Sallandsch", "Sallaands", "", "", "");

		createLsDialect(session, lOaverysselsk, "nds_NL@nwf-ovy-twe", "Twentsk", oNSS.getId(), "twe",
			"Twentsk", "Twents", "Twents", "", "", "");

		Language lGelderlandsk = createLsDialect(session, lSouthernNlSassisk, "nds_NL@nwf-gel", "Gelderlandsk", oNSS.getId(), "gel",
			"Gelderlandsk", "Gelderlandsch", "Gelderlandic", "", "", "");

		createLsDialect(session, lGelderlandsk, "nds_NL@nwf-gel-ach", "Achterhooksk", oNSS.getId(), "ach",
			"Achterhooksk", "Achterhooksch", "Achterhooks", "", "", "");

		createLsDialect(session, lGelderlandsk, "nds_NL@nwf-gel-vel", "Veluwsk", oNSS.getId(), "vel",
			"Veluwsk", "Veluwsch", "Veluws", "", "", "");

		// !! End of dialects of Low Saxon


		langMap.put("nds", lSaxon);
		langMap.put("de", lGerman);
		langMap.put("en", lEnglish);
		langMap.put("nl", lDutch);
		langMap.put("da", lDanish);
		langMap.put("sv", lSwedish);
		langMap.put("fi", lFinnish);
		langMap.put("bino", lBino);

		// Create the link types
		// TODO Should be in an own method...but it's so comfy to do it here ;)
		List<Integer> properties_ltBino_startLangIDs = new ArrayList<>();
		properties_ltBino_startLangIDs.add(lSaxon.getId());
		properties_ltBino_startLangIDs.add(lGerman.getId());
		properties_ltBino_startLangIDs.add(lEnglish.getId());
		properties_ltBino_startLangIDs.add(lDutch.getId());
		properties_ltBino_startLangIDs.add(lDanish.getId());
		properties_ltBino_startLangIDs.add(lSwedish.getId());
		properties_ltBino_startLangIDs.add(lFinnish.getId());

		List<Integer> properties_ltBino_endLangIDs = new ArrayList<>();
		properties_ltBino_endLangIDs.add(lBino.getId());

		Map<String, Object> properties_ltBino = new HashMap<>();
		properties_ltBino.put("startLangIDs", properties_ltBino_startLangIDs);
		properties_ltBino.put("endLangIDs", properties_ltBino_endLangIDs);
		properties_ltBino.put("selfReferring", false); // 'selfReferring': can the end lexeme be of the same language as the start language?

		LinkType ltBino = new LinkType(null, null, LinkType.DESC_BINOMIAL_NOMEN, LinkTypeTarget.Lexeme,
			"binoSt", "binoEn",  "binoVe", properties_ltBino);

		session.save(ltBino);

		/// Create the UiTranslations
		// Orthographies
		DataInitializer.createUiTranslations(session, "full", "o:nss", true, new Pair<>("nds", "Nysassiske Skryvwyse"),
			new Pair<>("de", "Neusächsische Schreibweise"), new Pair<>("en", "New Saxon Spelling"));
		DataInitializer.createUiTranslations(session, "full", "o:dbo", true, new Pair<>("nds", "Düütsk-baseerde skryvwyse"),
			new Pair<>("de", "Deutsch-basierte Schreibweise"), new Pair<>("en", "German based spelling"));
		DataInitializer.createUiTranslations(session, "full", "o:nbo", true, new Pair<>("nds", "Nedderlandsk-baseerde skryvwyse"),
			new Pair<>("de", "Niederländisch-basierte Schreibweise"), new Pair<>("en", "Dutch based spelling"));

		DataInitializer.createUiTranslations(session, "full", "o:bdr", true, new Pair<>("nds", "Bundsdüütske Rechtskryving"),
			new Pair<>("de", "Bundesdeutsche Rechtschreibung"), new Pair<>("en", "Federal German spelling"));
		DataInitializer.createUiTranslations(session, "full", "o:be", true, new Pair<>("nds", "Britisk Engelsk"),
			new Pair<>("de", "Britisches Englisch"), new Pair<>("en", "British English"));
		DataInitializer.createUiTranslations(session, "full", "o:nlo", true, new Pair<>("nds", "Nedderlandske skryvwyse"),
			new Pair<>("de", "Niederländische Rechtschreibung"), new Pair<>("en", "Dutch Orthography"));
		DataInitializer.createUiTranslations(session, "full", "o:dao", true, new Pair<>("nds", "Däänske ortografy"),
			new Pair<>("de", "Dänische Rechtschreibung"), new Pair<>("en", "Danish Orthography"));
		DataInitializer.createUiTranslations(session, "full", "o:svo", true, new Pair<>("nds", "Sweedske ortografy"),
			new Pair<>("de", "Schwedische Rechtschreibung"), new Pair<>("en", "Swedish Orthography"));
		DataInitializer.createUiTranslations(session, "full", "o:fio", true, new Pair<>("nds", "Finske ortografy"),
			new Pair<>("de", "Finnische Rechtschreibung"), new Pair<>("en", "Finnish Orthography"));
		DataInitializer.createUiTranslations(session, "full", "o:bino", true, new Pair<>("nds", "Binomiale nomenklatuur"),
			new Pair<>("de", "Binomiale Nomenklatur"), new Pair<>("en", "Binomial nomenclature"));

		// Languages
		// Full names
		DataInitializer.createUiTranslations(session, "full", "l:nds", true, new Pair<>("nds", "Neddersassisk"),
			new Pair<>("de", "Niedersächsisch"), new Pair<>("en", "Low Saxon"));
		DataInitializer.createUiTranslations(session, "full", "l:de", true, new Pair<>("nds", "Düütsk"),
			new Pair<>("de", "Deutsch"), new Pair<>("en", "German"));
		DataInitializer.createUiTranslations(session, "full", "l:en", true, new Pair<>("nds", "Engelsk"),
			new Pair<>("de", "Englisch"), new Pair<>("en", "English"));
		DataInitializer.createUiTranslations(session, "full", "l:nl", true, new Pair<>("nds", "Nedderlandsk"),
			new Pair<>("de", "Niederländisch"), new Pair<>("en", "Dutch"));
		DataInitializer.createUiTranslations(session, "full", "l:da", true, new Pair<>("nds", "Däänsk"),
			new Pair<>("de", "Dänisch"), new Pair<>("en", "Danish"));
		DataInitializer.createUiTranslations(session, "full", "l:sv", true, new Pair<>("nds", "Sweedsk"),
			new Pair<>("de", "Schwedisch"), new Pair<>("en", "Swedish"));
		DataInitializer.createUiTranslations(session, "full", "l:fi", true, new Pair<>("nds", "Finsk"),
			new Pair<>("de", "Finnisch"), new Pair<>("en", "Finnish"));
		DataInitializer.createUiTranslations(session, "full", "l:bino", true, new Pair<>("nds", "Binomiale nomenklatuur"),
			new Pair<>("de", "Binomiale Nomenklatur"), new Pair<>("en", "Binomial nomenclature"));

		// Abbreviated names
		DataInitializer.createUiTranslations(session, "abbr", "l:nds", true, new Pair<>("nds", "Nds."),
			new Pair<>("de", "Nds."), new Pair<>("en", "L.S."));
		DataInitializer.createUiTranslations(session, "abbr", "l:de", true, new Pair<>("nds", "Dt."),
			new Pair<>("de", "Dt."), new Pair<>("en", "Ger."));
		DataInitializer.createUiTranslations(session, "abbr", "l:en", true, new Pair<>("nds", "En."),
			new Pair<>("de", "En."), new Pair<>("en", "Eng."));
		DataInitializer.createUiTranslations(session, "abbr", "l:nl", true, new Pair<>("nds", "Nl."),
			new Pair<>("de", "Nl."), new Pair<>("en", "Dut."));
		DataInitializer.createUiTranslations(session, "abbr", "l:da", true, new Pair<>("nds", "Dä."),
			new Pair<>("de", "Dä."), new Pair<>("en", "Da."));
		DataInitializer.createUiTranslations(session, "abbr", "l:sv", true, new Pair<>("nds", "Sw."),
			new Pair<>("de", "Schw."), new Pair<>("en", "Sw."));
		DataInitializer.createUiTranslations(session, "abbr", "l:fi", true, new Pair<>("nds", "Fin."),
			new Pair<>("de", "Fin."), new Pair<>("en", "Fin."));
		DataInitializer.createUiTranslations(session, "abbr", "l:bino", true, new Pair<>("nds", "Bin.Nom."),
			new Pair<>("de", "Bin.Nom."), new Pair<>("en", "Bin.Nom."));

		// Link type names: "binoStart", "binoEnd",  "binoVerbal"
		DataInitializer.createUiTranslations(session, "linkType", "binoSt", true, new Pair<>("nds", "Plante, deert, …"),
			new Pair<>("de", "Pflanze, Tier, …"), new Pair<>("en", "Plant, animal, …"));
		DataInitializer.createUiTranslations(session, "linkType", "binoEn", true, new Pair<>("nds", "Binomiale nomenklatuur"),
			new Pair<>("de", "Binomiale Nomenklatur"), new Pair<>("en", "Binomial nomenclature"));
		DataInitializer.createUiTranslations(session, "linkType", "binoVe", true, new Pair<>("nds", "heyt weatenskoplik"),
			new Pair<>("de", "heißt wissenschaftlich"), new Pair<>("en", "is called scientifically"));

		/// Setup lang ortho mappings
		LangOrthoMapping lomSaxonNSS = new LangOrthoMapping(null, null, lSaxon.getId(), oNSS.getId(), (short) 1);
		LangOrthoMapping lomSaxonDBO = new LangOrthoMapping(null, null, lSaxon.getId(), oDBO.getId(), (short) 2);
		LangOrthoMapping lomSaxonNBO = new LangOrthoMapping(null, null, lSaxon.getId(), oNBO.getId(), (short) 3);

		LangOrthoMapping lomGermanBDR = new LangOrthoMapping(null, null, lGerman.getId(), oBDR.getId(), (short) 1);
		LangOrthoMapping lomEnglishBE = new LangOrthoMapping(null, null, lEnglish.getId(), oBE.getId(), (short) 1);
		LangOrthoMapping lomDutchNLS = new LangOrthoMapping(null, null, lDutch.getId(), oNL.getId(), (short) 1);
		LangOrthoMapping lomDanishDO = new LangOrthoMapping(null, null, lDanish.getId(), oDA.getId(), (short) 1);
		LangOrthoMapping lomSwedishSO = new LangOrthoMapping(null, null, lSwedish.getId(), oSV.getId(), (short) 1);
		LangOrthoMapping lomFinnishFO = new LangOrthoMapping(null, null, lFinnish.getId(), oFI.getId(), (short) 1);
		LangOrthoMapping lomBinoBINO = new LangOrthoMapping(null, null, lBino.getId(), oBN.getId(), (short) 1);

		session.save(lomSaxonNSS);
		session.save(lomSaxonDBO);
		session.save(lomSaxonNBO);

		session.save(lomGermanBDR);
		session.save(lomEnglishBE);
		session.save(lomDutchNLS);
		session.save(lomDanishDO);
		session.save(lomSwedishSO);
		session.save(lomFinnishFO);
		session.save(lomBinoBINO);

		// Setup language pairs
		LangPair lpSaxonEnglish = new LangPair("nds-en", lSaxon.getId(), lEnglish.getId(), 1);
		LangPair lpSaxonGerman = new LangPair("nds-de", lSaxon.getId(), lGerman.getId(), 2);
		LangPair lpSaxonDutch = new LangPair("nds-nl", lSaxon.getId(), lDutch.getId(), 3);
		LangPair lpSaxonDanish = new LangPair("nds-da", lSaxon.getId(), lDanish.getId(), 4);
		LangPair lpSaxonSwedish = new LangPair("nds-sv", lSaxon.getId(), lSwedish.getId(), 5);
		LangPair lpSaxonFinnish = new LangPair("nds-fi", lSaxon.getId(), lFinnish.getId(), 6);

		session.save(lpSaxonGerman);
		session.save(lpSaxonEnglish);
		session.save(lpSaxonDutch);
		session.save(lpSaxonDanish);
		session.save(lpSaxonSwedish);
		session.save(lpSaxonFinnish);
	}

	private Language createLsDialect(Session session, Language parent, String locale, String localName,
		int orthographyID, String importAbbreviation, String name_nds, String name_de, String name_en,
		String nameShort_nds, String nameShort_de, String nameShort_en)
	{
		String uitID = "l:" + locale;
		String uitID_abbr = "l:" + locale;

		Language language = new Language(null, parent == null ? null : parent.getId(), locale, localName, uitID,
			uitID_abbr, orthographyID, importAbbreviation);
		session.save(language);

		DataInitializer.createUiTranslations(session, "full", uitID, true, new Pair<>("nds", name_nds),
			new Pair<>("de", name_de), new Pair<>("en", name_en));
		DataInitializer.createUiTranslations(session, "abbr", uitID_abbr, true, new Pair<>("nds", nameShort_nds),
			new Pair<>("de", nameShort_de), new Pair<>("en", nameShort_en));

		return language;
	}

	private void createLexemeTypes(Session session)
	{
		// TODO Create more UiResultCategories
		UiResultCategory uiRC_verbs = new UiResultCategory(null, null, "verbs", "urc:verbs", (short) 1);
		UiResultCategory uiRC_nouns = new UiResultCategory(null, null, "nouns", "urc:nouns", (short) 1);
		session.save(uiRC_verbs);
		session.save(uiRC_nouns);

		/* Setup LexemeTypes */

		// The 'Part of Speech' types as classified by Universal Dependencies
		LexemeType ltAdj = new LexemeType(null, null, LexemeType.TYPE_ADJ, uiRC_verbs.getId(), "lt:adj");
		LexemeType ltAdp = new LexemeType(null, null, LexemeType.TYPE_ADP, uiRC_verbs.getId(), "lt:adp");
		LexemeType ltAdv = new LexemeType(null, null, LexemeType.TYPE_ADV, uiRC_verbs.getId(), "lt:adv");
		LexemeType ltAux = new LexemeType(null, null, LexemeType.TYPE_AUX, uiRC_verbs.getId(), "lt:aux");
		LexemeType ltCconj = new LexemeType(null, null, LexemeType.TYPE_CCONJ, uiRC_verbs.getId(), "lt:cconj");
		LexemeType ltDet = new LexemeType(null, null, LexemeType.TYPE_DET, uiRC_verbs.getId(), "lt:det");
		LexemeType ltIntj = new LexemeType(null, null, LexemeType.TYPE_INTJ, uiRC_verbs.getId(), "lt:intj");
		LexemeType ltNoun = new LexemeType(null, null, LexemeType.TYPE_NOUN, uiRC_nouns.getId(), "lt:noun");
		LexemeType ltNum = new LexemeType(null, null, LexemeType.TYPE_NUM, uiRC_verbs.getId(), "lt:num");
		LexemeType ltPart = new LexemeType(null, null, LexemeType.TYPE_PART, uiRC_verbs.getId(), "lt:part");
		LexemeType ltPron = new LexemeType(null, null, LexemeType.TYPE_PRON, uiRC_verbs.getId(), "lt:pron");
		LexemeType ltPropn = new LexemeType(null, null, LexemeType.TYPE_PROPN, uiRC_verbs.getId(), "lt:propn");
		LexemeType ltPunct = new LexemeType(null, null, LexemeType.TYPE_PUNCT, uiRC_verbs.getId(), "lt:punct");
		LexemeType ltSconj = new LexemeType(null, null, LexemeType.TYPE_SCONJ, uiRC_verbs.getId(), "lt:sconj");
		// Symbool bruket wy ni (dat sünt teykens etc.)
		//LexemeType ltSym = new LexemeType(null, null, LexemeType.TYPE_SYM, uiRC_verbs.getId(), "lt:sym");
		LexemeType ltVerb = new LexemeType(null, null, LexemeType.TYPE_VERB, uiRC_verbs.getId(), "lt:verb");
		LexemeType ltX = new LexemeType(null, null, LexemeType.TYPE_X, uiRC_verbs.getId(), "lt:x");

		// Internal types to map rektioon (case government) for high variant languages like Low Saxon
		LexemeType ltCg = new LexemeType(null, null, LexemeType.TYPE_I_CG, uiRC_verbs.getId(), "lt:icg");

		// Custom types
		LexemeType ltUTDR = new LexemeType(null, null, LexemeType.TYPE_C_UTDR, uiRC_verbs.getId(), "lt:utdr");

		session.save(ltAdj);
		session.save(ltAdp);
		session.save(ltAdv);
		session.save(ltAux);
		session.save(ltCconj);
		session.save(ltDet);
		session.save(ltIntj);
		session.save(ltNoun);
		session.save(ltNum);
		session.save(ltPart);
		session.save(ltPron);
		session.save(ltPropn);
		session.save(ltPunct);
		session.save(ltSconj);
		//session.save(ltSym);
		session.save(ltVerb);
		session.save(ltX);

		session.save(ltCg);
		session.save(ltUTDR);

		// iCG: Case Government (rektioon)
		LexemeFormType lftCgBase = new LexemeFormType(null, null, ltCg.getId(), "bf", "baseForm",
			null, true, (short) 0);
		session.save(lftCgBase);

		// ADJ
		LexemeFormType lftAdjPositive = new LexemeFormType(null, null, ltAdj.getId(), "pos", "positive",
			null, true, (short) 0);
		LexemeFormType lftAdjComparative = new LexemeFormType(null, null, ltAdj.getId(), "com", "comparative",
			null, false, (short) 1);
		LexemeFormType lftAdjSuperlative = new LexemeFormType(null, null, ltAdj.getId(), "sup", "superlative",
			null, false, (short) 2);
		session.save(lftAdjPositive);
		session.save(lftAdjComparative);
		session.save(lftAdjSuperlative);

		// ADP
		LexemeFormType lftAdpBase = new LexemeFormType(null, null, ltAdp.getId(), "bf", "baseForm",
			null, true, (short) 0);
		session.save(lftAdpBase);

		// ADV
		LexemeFormType lftAdvBase = new LexemeFormType(null, null, ltAdv.getId(), "bf", "baseForm",
			null, true, (short) 0);
		session.save(lftAdvBase);

		// AUX
		LexemeFormType lftAuxBase = new LexemeFormType(null, null, ltAux.getId(), "bf", "baseForm",
			null, true, (short) 0);
		session.save(lftAuxBase);

		// CCONJ
		LexemeFormType lftCconjBase = new LexemeFormType(null, null, ltCconj.getId(), "bf", "baseForm",
			null, true, (short) 0);
		session.save(lftCconjBase);

		// DET
		LexemeFormType lftDetBase = new LexemeFormType(null, null, ltDet.getId(), "bf", "baseForm",
			null, true, (short) 0);
		session.save(lftDetBase);

		// INTJ
		LexemeFormType lftIntjBase = new LexemeFormType(null, null, ltIntj.getId(), "bf", "baseForm",
			null, true, (short) 0);
		session.save(lftIntjBase);

		// NOUN
		createFormType(session, ltNoun.getId(), "sn", "nounSinNom", "Singular nominative", true, 0);
		createFormType(session, ltNoun.getId(), "pn", "nounPluNom", "Plural nominative", false, 1);

		// NUM
		LexemeFormType lftNumBase = new LexemeFormType(null, null, ltNum.getId(), "bf", "baseForm",
			null, true, (short) 0);
		session.save(lftNumBase);

		// PART
		LexemeFormType lftPartBase = new LexemeFormType(null, null, ltPart.getId(), "bf", "baseForm",
			null, true, (short) 0);
		session.save(lftPartBase);

		// PRON
		LexemeFormType lftPronBase = new LexemeFormType(null, null, ltPron.getId(), "bf", "baseForm",
			null, true, (short) 0);
		session.save(lftPronBase);

		// PROPN
		LexemeFormType lftPropnBase = new LexemeFormType(null, null, ltPropn.getId(), "bf", "baseForm",
			null, true, (short) 0);
		session.save(lftPropnBase);

		// PUNCT
		LexemeFormType lftPunctBase = new LexemeFormType(null, null, ltPunct.getId(), "bf", "baseForm",
			null, true, (short) 0);
		session.save(lftPunctBase);

		// SCONJ
		LexemeFormType lftSconjBase = new LexemeFormType(null, null, ltSconj.getId(), "bf", "baseForm",
			null, true, (short) 0);
		session.save(lftSconjBase);

		// !! VERB
		createFormType(session, ltVerb.getId(), "inf", "verbInf", "Infinitive", true, 0);
		createFormType(session, ltVerb.getId(), "inf_div", "verbInfDiv", "Infinitive with divider", false, 1);
		createFormType(session, ltVerb.getId(), "inf_max", "verbInfMax", "Infinitive in maximal version", false, 2);

		createFormType(session, ltVerb.getId(), "s1ps", "verbS1ps", "Singular 1st person present", false, 3);
		createFormType(session, ltVerb.getId(), "s2ps", "verbS2ps", "Singular 2nd person present", false, 4);
		createFormType(session, ltVerb.getId(), "s3ps", "verbS3ps", "Singular 3rd person present", false, 5);
		createFormType(session, ltVerb.getId(), "p1ps", "verbP1ps", "Plural 1st person present", false, 6);
		createFormType(session, ltVerb.getId(), "p2ps", "verbP2ps", "Plural 2nd person present", false, 7);
		createFormType(session, ltVerb.getId(), "p3ps", "verbP3ps", "Plural 3rd person present", false, 8);

		createFormType(session, ltVerb.getId(), "s1pt", "verbS1pt", "Singular 1st person past tense", false, 9);
		createFormType(session, ltVerb.getId(), "s2pt", "verbS2pt", "Singular 2nd person past tense", false, 10);
		createFormType(session, ltVerb.getId(), "s3pt", "verbS3pt", "Singular 3rd person past tense", false, 11);
		createFormType(session, ltVerb.getId(), "p1pt", "verbP1pt", "Plural 1st person past tense", false, 12);
		createFormType(session, ltVerb.getId(), "p2pt", "verbP2pt", "Plural 2nd person past tense", false, 13);
		createFormType(session, ltVerb.getId(), "p3pt", "verbP3pt", "Plural 3rd person past tense", false, 14);

		createFormType(session, ltVerb.getId(), "ptc1", "verbPtc1", "Participle I", false, 15);
		createFormType(session, ltVerb.getId(), "ptc2", "verbPtc2", "Participle II", false, 16);

		createFormType(session, ltVerb.getId(), "simp", "verbSimp", "Singular imperative", false, 18);
		createFormType(session, ltVerb.getId(), "pimp", "verbPimp", "Plural imperative", false, 19);

		createFormType(session, ltVerb.getId(), "s1s1", "verbS1s1", "Singular 1st person subjunctive I", false, 21);
		createFormType(session, ltVerb.getId(), "s2s1", "verbS2s1", "Singular 2nd person subjunctive I", false, 22);
		createFormType(session, ltVerb.getId(), "s3s1", "verbS3s1", "Singular 3rd person subjunctive I", false, 23);
		createFormType(session, ltVerb.getId(), "p1s1", "verbP1s1", "Plural 1st person subjunctive I", false, 24);
		createFormType(session, ltVerb.getId(), "p2s1", "verbP2s1", "Plural 2nd person subjunctive I", false, 25);
		createFormType(session, ltVerb.getId(), "p3s1", "verbP3s1", "Plural 3rd person subjunctive I", false, 26);

		createFormType(session, ltVerb.getId(), "s1s2", "verbS1s2", "Singular 1st person subjunctive II", false, 27);
		createFormType(session, ltVerb.getId(), "s2s2", "verbS2s2", "Singular 2nd person subjunctive II", false, 28);
		createFormType(session, ltVerb.getId(), "s3s2", "verbS3s2", "Singular 3rd person subjunctive II", false, 29);
		createFormType(session, ltVerb.getId(), "p1s2", "verbP1s2", "Plural 1st person subjunctive II", false, 30);
		createFormType(session, ltVerb.getId(), "p2s2", "verbP2s2", "Plural 2nd person subjunctive II", false, 31);
		createFormType(session, ltVerb.getId(), "p3s2", "verbP3s2", "Plural 3rd person subjunctive II", false, 32);
		// !! / VERB

		// X
		LexemeFormType lftXBase = new LexemeFormType(null, null, ltX.getId(), "bf", "baseForm",
			null, true, (short) 0);
		session.save(lftXBase);

		// UTDR
		createFormType(session, ltUTDR.getId(), "bf", "baseForm", "Base form", true, 0);

		// Create the lemma templates
		session.save(new LemmaTemplate(null, null, null, ltCg.getId(), null, null, null, null, "$(bf)", null, null));

		session.save(new LemmaTemplate(null, null, null, ltAdj.getId(), null, null, null, null, "$(pos)", null, null));
		session.save(new LemmaTemplate(null, null, null, ltAdp.getId(), null, null, null, null, "$(bf)", null, null));
		session.save(new LemmaTemplate(null, null, null, ltAdv.getId(), null, null, null, null, "$(bf)", null, null));
		session.save(new LemmaTemplate(null, null, null, ltAux.getId(), null, null, null, null, "$(bf)", null, null));
		session.save(new LemmaTemplate(null, null, null, ltCconj.getId(), null, null, null, null, "$(bf)", null, null));
		session.save(new LemmaTemplate(null, null, null, ltDet.getId(), null, null, null, null, "$(bf)", null, null));
		session.save(new LemmaTemplate(null, null, null, ltIntj.getId(), null, null, null, null, "$(bf)", null, null));

		session.save(new LemmaTemplate(null, null, null, ltNoun.getId(), null, null, null, null, "$(sn)", null, null));

		session.save(new LemmaTemplate(null, null, null, ltNum.getId(), null, null, null, null, "$(bf)", null, null));
		session.save(new LemmaTemplate(null, null, null, ltPart.getId(), null, null, null, null, "$(bf)", null, null));
		session.save(new LemmaTemplate(null, null, null, ltPron.getId(), null, null, null, null, "$(bf)", null, null));
		session.save(new LemmaTemplate(null, null, null, ltPropn.getId(), null, null, null, null, "$(bf)", null, null));
		session.save(new LemmaTemplate(null, null, null, ltPunct.getId(), null, null, null, null, "$(bf)", null, null));
		session.save(new LemmaTemplate(null, null, null, ltSconj.getId(), null, null, null, null, "$(bf)", null, null));

		session.save(new LemmaTemplate(null, null, null, ltVerb.getId(), null, null, null, null, "$(inf)", null, null));
		session.save(new LemmaTemplate(null, null, null, ltX.getId(), null, null, null, null, "$(bf)", null, null));

		// LemmaTemplates specifically for Danish
		session.save(new LemmaTemplate(null, null, null, ltVerb.getId(), langMap.get("da").getId(), null, null, "at",
			"$(inf)", null, null));
		// LemmaTemplates specifically for English
		session.save(new LemmaTemplate(null, null, null, ltVerb.getId(), langMap.get("en").getId(), null, null, "to",
			"$(inf)", null, null));
		// LemmaTemplates specifically for Swedish
		session.save(new LemmaTemplate(null, null, null, ltVerb.getId(), langMap.get("sv").getId(), null, null, "att",
			"$(inf)", null, null));

		// UTDR
		session.save(new LemmaTemplate(null, null, null, ltUTDR.getId(), null, null, null, null, "$(bf)", null, null));


		// Create UiTranslations
		DataInitializer.createUiTranslations(session, "full", "urc:verbs", true, new Pair<>("nds", "Verben"),
			new Pair<>("de", "Verben"), new Pair<>("en", "Verbs"));
		DataInitializer.createUiTranslations(session, "full", "urc:nouns", true, new Pair<>("nds", "Nomen"),
			new Pair<>("de", "Nomen"), new Pair<>("en", "Nouns"));

		DataInitializer.createUiTranslations(session, "full", "lt:adj", true, new Pair<>("nds", "Adjektiv"),
			new Pair<>("de", "Adjektiv"), new Pair<>("en", "Adjective"));
		DataInitializer.createUiTranslations(session, "full", "lt:adp", true, new Pair<>("nds", "Adpositioon"),
			new Pair<>("de", "Adposition"), new Pair<>("en", "Adposition"));
		DataInitializer.createUiTranslations(session, "full", "lt:adv", true, new Pair<>("nds", "Adverb"),
			new Pair<>("de", "Adverb"), new Pair<>("en", "Adverb"));
		DataInitializer.createUiTranslations(session, "full", "lt:aux", true, new Pair<>("nds", "Auxillary"),
			new Pair<>("de", "Auxillary"), new Pair<>("en", "Auxillary"));
		DataInitializer.createUiTranslations(session, "full", "lt:cconj", true, new Pair<>("nds", "Coordinating conjunction"),
			new Pair<>("de", "Coordinating conjunction"), new Pair<>("en", "Coordinating conjunction"));
		DataInitializer.createUiTranslations(session, "full", "lt:det", true, new Pair<>("nds", "Determiner"),
			new Pair<>("de", "Determiner"), new Pair<>("en", "Determiner"));
		DataInitializer.createUiTranslations(session, "full", "lt:intj", true, new Pair<>("nds", "Interjection"),
			new Pair<>("de", "Interjection"), new Pair<>("en", "Interjection"));
		DataInitializer.createUiTranslations(session, "full", "lt:noun", true, new Pair<>("nds", "Nomen"),
			new Pair<>("de", "Nomen"), new Pair<>("en", "Noun"));
		DataInitializer.createUiTranslations(session, "full", "lt:num", true, new Pair<>("nds", "Numeral"),
			new Pair<>("de", "Numeral"), new Pair<>("en", "Numeral"));
		DataInitializer.createUiTranslations(session, "full", "lt:part", true, new Pair<>("nds", "Particle"),
			new Pair<>("de", "Particle"), new Pair<>("en", "Particle"));
		DataInitializer.createUiTranslations(session, "full", "lt:pron", true, new Pair<>("nds", "Pronomen"),
			new Pair<>("de", "Pronomen"), new Pair<>("en", "Pronoun"));
		DataInitializer.createUiTranslations(session, "full", "lt:propn", true, new Pair<>("nds", "Proper noun"),
			new Pair<>("de", "Proper noun"), new Pair<>("en", "Proper noun"));
		DataInitializer.createUiTranslations(session, "full", "lt:punct", true, new Pair<>("nds", "Punctuation"),
			new Pair<>("de", "Punctuation"), new Pair<>("en", "Punctuation"));
		DataInitializer.createUiTranslations(session, "full", "lt:sconj", true, new Pair<>("nds", "Subordinating conjunction"),
			new Pair<>("de", "Subordinating conjunction"), new Pair<>("en", "Subordinating conjunction"));
		//DataInitializer.createUiTranslations(session, "full", "lt:sym", true, new Pair<>("nds", "Symbol"),
		//	new Pair<>("de", "Symbol"), new Pair<>("en", "Symbol"));
		DataInitializer.createUiTranslations(session, "full", "lt:verb", true, new Pair<>("nds", "Verb"),
			new Pair<>("de", "Verb"), new Pair<>("en", "Verb"));
		DataInitializer.createUiTranslations(session, "full", "lt:x", true, new Pair<>("nds", "Ander"),
			new Pair<>("de", "Sonstige"), new Pair<>("en", "Other"));

		DataInitializer.createUiTranslations(session, "full", "lt:icg", true, new Pair<>("nds", "Rektioon"),
			new Pair<>("de", "Rektion"), new Pair<>("en", "Case Government"));
		DataInitializer.createUiTranslations(session, "full", "lt:utdr", true, new Pair<>("nds", "Meyrwoorduutdrükke"),
			new Pair<>("de", "Mehrwortausdrücke"), new Pair<>("en", "Multi-word expression"));

		// Öäversettings vöär de lekseemfoormtypen
		DataInitializer.createUiTranslations(session, "formType", "baseForm", true, new Pair<>("nds", "Grundfoorm"),
			new Pair<>("de", "Grundform"), new Pair<>("en", "Base form"));

		DataInitializer.createUiTranslations(session, "formType", "positive", true, new Pair<>("nds", "Positiv"),
			new Pair<>("de", "Positiv"), new Pair<>("en", "Positive"));
		DataInitializer.createUiTranslations(session, "formType", "comparative", true, new Pair<>("nds", "Komparativ"),
			new Pair<>("de", "Komparativ"), new Pair<>("en", "Comparative"));
		DataInitializer.createUiTranslations(session, "formType", "superlative", true, new Pair<>("nds", "Superlativ"),
			new Pair<>("de", "Superlativ"), new Pair<>("en", "Superlative"));

		DataInitializer.createUiTranslations(session, "formType", "nounSinNom", true, new Pair<>("nds", "Singulaar nominativ"),
			new Pair<>("de", "Singular Nominativ"), new Pair<>("en", "Singular nominative"));
		DataInitializer.createUiTranslations(session, "formType", "nounPluNom", true, new Pair<>("nds", "Pluraal nominativ"),
			new Pair<>("de", "Plural Nominativ"), new Pair<>("en", "Plural nominative"));

		DataInitializer.createUiTranslations(session, "formType", "verbInf", true, new Pair<>("nds", "Infinitiv"),
			new Pair<>("de", "Infinitiv"), new Pair<>("en", "Infinitive"));
		DataInitializer.createUiTranslations(session, "formType", "verbInfDiv", true, new Pair<>("nds", "Infinitiv (trenteyken/s)"),
			new Pair<>("de", "Infinitiv (Trennzeichen)"), new Pair<>("en", "Infinitive (divider/s)"));
		DataInitializer.createUiTranslations(session, "formType", "verbInfMax", true, new Pair<>("nds", "Infinitiv (maks.)"),
			new Pair<>("de", "Infinitiv (max.)"), new Pair<>("en", "Infinitive (max.)"));

		DataInitializer.createUiTranslations(session, "formType", "verbS1ps", true, new Pair<>("nds", "Singulaar 1 presens"),
			new Pair<>("de", "Singular 1. Präsens"), new Pair<>("en", "Singular 1st present"));
		DataInitializer.createUiTranslations(session, "formType", "verbS2ps", true, new Pair<>("nds", "Singulaar 2 presens"),
			new Pair<>("de", "Singular 2. Präsens"), new Pair<>("en", "Singular 2nd present"));
		DataInitializer.createUiTranslations(session, "formType", "verbS3ps", true, new Pair<>("nds", "Singulaar 3 presens"),
			new Pair<>("de", "Singular 3. Präsens"), new Pair<>("en", "Singular 3rd present"));
		DataInitializer.createUiTranslations(session, "formType", "verbP1ps", true, new Pair<>("nds", "Pluraal 1 presens"),
			new Pair<>("de", "Plural 1. Präsens"), new Pair<>("en", "Plural 1st present"));
		DataInitializer.createUiTranslations(session, "formType", "verbP2ps", true, new Pair<>("nds", "Pluraal 2 presens"),
			new Pair<>("de", "Plural 2. Präsens"), new Pair<>("en", "Plural 2nd present"));
		DataInitializer.createUiTranslations(session, "formType", "verbP3ps", true, new Pair<>("nds", "Pluraal 3 presens"),
			new Pair<>("de", "Plural 3. Präsens"), new Pair<>("en", "Plural 3rd present"));

		DataInitializer.createUiTranslations(session, "formType", "verbS1pt", true, new Pair<>("nds", "Singulaar 1 preteritum"),
			new Pair<>("de", "Singular 1. Präteritum"), new Pair<>("en", "Singular 1st preterite"));
		DataInitializer.createUiTranslations(session, "formType", "verbS2pt", true, new Pair<>("nds", "Singulaar 2 preteritum"),
			new Pair<>("de", "Singular 2. Präteritum"), new Pair<>("en", "Singular 2nd preterite"));
		DataInitializer.createUiTranslations(session, "formType", "verbS3pt", true, new Pair<>("nds", "Singulaar 3 preteritum"),
			new Pair<>("de", "Singular 3. Präteritum"), new Pair<>("en", "Singular 3rd preterite"));
		DataInitializer.createUiTranslations(session, "formType", "verbP1pt", true, new Pair<>("nds", "Pluraal 1 preteritum"),
			new Pair<>("de", "Plural 1. Präteritum"), new Pair<>("en", "Plural 1st preterite"));
		DataInitializer.createUiTranslations(session, "formType", "verbP2pt", true, new Pair<>("nds", "Pluraal 2 preteritum"),
			new Pair<>("de", "Plural 2. Präteritum"), new Pair<>("en", "Plural 2nd preterite"));
		DataInitializer.createUiTranslations(session, "formType", "verbP3pt", true, new Pair<>("nds", "Pluraal 3 preteritum"),
			new Pair<>("de", "Plural 3. Präteritum"), new Pair<>("en", "Plural 3rd preterite"));

		DataInitializer.createUiTranslations(session, "formType", "verbPtc1", true, new Pair<>("nds", "Participe I"),
			new Pair<>("de", "Partizip I"), new Pair<>("en", "Participle I"));
		DataInitializer.createUiTranslations(session, "formType", "verbPtc2", true, new Pair<>("nds", "Participe II"),
			new Pair<>("de", "Partizip II"), new Pair<>("en", "Participle II"));

		DataInitializer.createUiTranslations(session, "formType", "verbSimp", true, new Pair<>("nds", "Singulaar imperativ"),
			new Pair<>("de", "Singular Imperativ"), new Pair<>("en", "Singular imperative"));
		DataInitializer.createUiTranslations(session, "formType", "verbPimp", true, new Pair<>("nds", "Pluraal imperativ"),
			new Pair<>("de", "Plural Imperativ"), new Pair<>("en", "Plural imperative"));

		DataInitializer.createUiTranslations(session, "formType", "verbS1s1", true, new Pair<>("nds", "Singulaar 1 konjunktiv I"),
			new Pair<>("de", "Singular 1. Konjunktiv I"), new Pair<>("en", "Singular 1st subjunctive I"));
		DataInitializer.createUiTranslations(session, "formType", "verbS2s1", true, new Pair<>("nds", "Singulaar 2 konjunktiv I"),
			new Pair<>("de", "Singular 2. Konjunktiv I"), new Pair<>("en", "Singular 2nd subjunctive I"));
		DataInitializer.createUiTranslations(session, "formType", "verbS3s1", true, new Pair<>("nds", "Singulaar 3 konjunktiv I"),
			new Pair<>("de", "Singular 3. Konjunktiv I"), new Pair<>("en", "Singular 3rd subjunctive I"));
		DataInitializer.createUiTranslations(session, "formType", "verbP1s1", true, new Pair<>("nds", "Pluraal 1 konjunktiv I"),
			new Pair<>("de", "Plural 1. Konjunktiv I"), new Pair<>("en", "Plural 1st subjunctive I"));
		DataInitializer.createUiTranslations(session, "formType", "verbP2s1", true, new Pair<>("nds", "Pluraal 2 konjunktiv I"),
			new Pair<>("de", "Plural 2. Konjunktiv I"), new Pair<>("en", "Plural 2nd subjunctive I"));
		DataInitializer.createUiTranslations(session, "formType", "verbP3s1", true, new Pair<>("nds", "Pluraal 3 konjunktiv I"),
			new Pair<>("de", "Plural 3. Konjunktiv I"), new Pair<>("en", "Plural 3rd subjunctive I"));

		DataInitializer.createUiTranslations(session, "formType", "verbS1s2", true, new Pair<>("nds", "Singulaar 1 konjunktiv II"),
			new Pair<>("de", "Singular 1. Konjunktiv II"), new Pair<>("en", "Singular 1st subjunctive II"));
		DataInitializer.createUiTranslations(session, "formType", "verbS2s2", true, new Pair<>("nds", "Singulaar 2 konjunktiv II"),
			new Pair<>("de", "Singular 2. Konjunktiv II"), new Pair<>("en", "Singular 2nd subjunctive II"));
		DataInitializer.createUiTranslations(session, "formType", "verbS3s2", true, new Pair<>("nds", "Singulaar 3 konjunktiv II"),
			new Pair<>("de", "Singular 3. Konjunktiv II"), new Pair<>("en", "Singular 3rd subjunctive II"));
		DataInitializer.createUiTranslations(session, "formType", "verbP1s2", true, new Pair<>("nds", "Pluraal 1 konjunktiv II"),
			new Pair<>("de", "Plural 1. Konjunktiv II"), new Pair<>("en", "Plural 1st subjunctive II"));
		DataInitializer.createUiTranslations(session, "formType", "verbP2s2", true, new Pair<>("nds", "Pluraal 2 konjunktiv II"),
			new Pair<>("de", "Plural 2. Konjunktiv II"), new Pair<>("en", "Plural 2nd subjunctive II"));
		DataInitializer.createUiTranslations(session, "formType", "verbP3s2", true, new Pair<>("nds", "Pluraal 3 konjunktiv II"),
			new Pair<>("de", "Plural 3. Konjunktiv II"), new Pair<>("en", "Plural 3rd subjunctive II"));

		/*
		List of al Lexeme types to be created later:
		ADJ		adjective
		ADP		adposition
		ADV		adverb
		AUX		auxillary
		CCONJ	coordinating conjunction
		DET		determiner
		INTJ	interjection
		NOUN	noun
		NUM		numeral
		PART	particle
		PRON	pronoun
		PROPN	proper noun
		PUNCT	punctuation
		SCONJ	subordinating conjunction
		SYM		symbol
		VERB	verb
		X		other
		 */

		// Setup LexemeFormTypes for LexemeType base for every language
		// TODO
		/*
		for (Map.Entry<String, Language> entry : this.langMap.entrySet()) {
			Language lang = entry.getValue();

			LexemeFormType lftBaseInf = new LexemeFormType(ltBase.getId(), "inf", lang.getId(),
					"lft.inf", "Infinitive", true, (short) 0);
			LexemeFormType lftBaseAlt1 = new LexemeFormType(ltBase.getId(), "alt1", lang.getId(),
					"lft.alt1", "Alternative form 1", false, (short) 0);
			LexemeFormType lftBaseAlt2 = new LexemeFormType(ltBase.getId(), "alt2", lang.getId(),
					"lft.alt2", "Alternative form 2", false, (short) 0);
			LexemeFormType lftBaseAlt3 = new LexemeFormType(ltBase.getId(), "alt3", lang.getId(),
					"lft.alt3", "Alternative form 3", false, (short) 0);

			session.save(lftBaseInf);
			session.save(lftBaseAlt1);
			session.save(lftBaseAlt2);
			session.save(lftBaseAlt3);
		}
		 */
	}

	private void createFormType(final Session session, Integer lexemeTypeID, String name, String uitID, String description,
		boolean mandatory, int position)
	{
		LexemeFormType formType = new LexemeFormType(null, null, lexemeTypeID, name, uitID, description, mandatory,
			(short) position);
		session.save(formType);
	}

	private void createMetaTagData(final Session session) {
		DataInitializer.createUiTranslations(session, "metaTags", "itemprop='name'", true,
			new Pair<>("nds", "ULE wöördebook"),
			new Pair<>("de", "ULE Wörterbuch"),
			new Pair<>("en", "ULE dictionary"));

		DataInitializer.createUiTranslations(session, "metaTags", "itemprop='description'", true,
			new Pair<>("nds", "Grensöäverstryden neddersassisk/ platdüütsk wöördebook med öäversettings nå düütsk, nedderlandsk, engelsk un ander språken."),
			new Pair<>("de", "Grenzübergreifendes Niedersächsisch-/ Plattdeutsch-Wörterbuch mit Übersetzungen nach Deutsch, Niederländisch, Englisch und weiteren Sprachen."),
			new Pair<>("en", "Cross-border dictionary for the Low Saxon/ Low German language with translations to English, German, Dutch and other languages."));

		DataInitializer.createUiTranslations(session, "metaTags", "property='og:site_name'", true,
			new Pair<>("nds", "ULE wöördebook"),
			new Pair<>("de", "ULE Wörterbuch"),
			new Pair<>("en", "ULE dictionary"));

		DataInitializer.createUiTranslations(session, "metaTags", "property='og:title'", true,
			new Pair<>("nds", "ULE.DK – Grensöäverstryden neddersassisk/ platdüütsk wöördebook"),
			new Pair<>("de", "ULE.DK – Grenzübergreifendes Niedersächsisch-/ Plattdeutsch-Wörterbuch"),
			new Pair<>("en", "ULE.DK – Cross-border dictionary for the Low Saxon/ Low German language"));

		DataInitializer.createUiTranslations(session, "metaTags", "property='og:description'", true,
			new Pair<>("nds", "Grensöäverstryden neddersassisk/ platdüütsk wöördebook med öäversettings nå düütsk, nedderlandsk, engelsk un ander språken."),
			new Pair<>("de", "Grenzübergreifendes Niedersächsisch-/ Plattdeutsch-Wörterbuch mit Übersetzungen nach Deutsch, Niederländisch, Englisch und weiteren Sprachen."),
			new Pair<>("en", "Cross-border dictionary for the Low Saxon/ Low German language with translations to English, German, Dutch and other languages."));
	}
}
