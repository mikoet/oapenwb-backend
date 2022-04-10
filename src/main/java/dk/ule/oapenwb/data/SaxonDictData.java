// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data;

import dk.ule.oapenwb.entity.content.basedata.*;
import dk.ule.oapenwb.entity.ui.UiResultCategory;
import dk.ule.oapenwb.util.Pair;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Initial data specific to the (Low) Saxon dictionary.
 */
public class SaxonDictData implements DataStrategy {
	private Map<String, Language> langMap = new HashMap<>();

	@Override
	public void createData(Session session) {
		createLangData(session);
		createLexemeTypes(session);
	}

	private void createLangData(Session session) {
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
		Language lSaxon = new Language(null, null, "nds", "Neddersassisk", "l:nds", "l:nds", oNSS.getId());
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

		// Dialects of Low Saxon
		Language lNorthernLowSaxon = new Language(null, lSaxon.getId(), "nds-nns", "Noordneddersassisk", "l:nds-nns", "l:nds-nns", oNSS.getId());
		session.save(lNorthernLowSaxon);

		Language lDitmarsk = new Language(null, lNorthernLowSaxon.getId(), "nds-nns-dm", "Ditmarsk", "l:nds-nns-dm", "l:nds-nns-dm", oNSS.getId());
		Language lLuemborgsk = new Language(null, lNorthernLowSaxon.getId(), "nds-nns-lb", "Lümborgsk", "l:nds-nns-lb", "l:nds-nns-lb", oNSS.getId());
		session.save(lDitmarsk);
		session.save(lLuemborgsk);

		Language lWestphalian = new Language(null, lSaxon.getId(), "nds-wf", "Westföälsk", "l:nds-wf", "l:nds-wf", oNSS.getId());
		session.save(lWestphalian);

		Language lSuderlandsk = new Language(null, lWestphalian.getId(), "nds-wf-sl", "Suderlandsk", "l:nds-wf-sl", "l:nds-wf-sl", oNSS.getId());
		Language lMoensterlaendsk = new Language(null, lWestphalian.getId(), "nds-wf-ml", "Mönsterländsk", "l:nds-wf-ml", "l:nds-wf-ml", oNSS.getId());
		session.save(lSuderlandsk);
		session.save(lMoensterlaendsk);

		Language lEastphalian = new Language(null, lSaxon.getId(), "nds-of", "Ostfälisk", "l:nds-of", "l:nds-of", oNSS.getId());
		session.save(lEastphalian);

		Language lHilmsensk = new Language(null, lEastphalian.getId(), "nds-of-hi", "Hilmsensk", "l:nds-of-hi", "l:nds-of-hi", oNSS.getId());
		Language lPapendyksk = new Language(null, lEastphalian.getId(), "nds-of-pd", "Papendyksk", "l:nds-of-pd", "l:nds-of-pd", oNSS.getId());
		session.save(lHilmsensk);
		session.save(lPapendyksk);

		DataInitializer.createUiTranslations(session, "full", "l:nds-nns", true, new Pair<>("nds", "Noordneddersassisk"),
			new Pair<>("de", "Nordniedersächsisch"), new Pair<>("en", "Northern Low Saxon"));
		DataInitializer.createUiTranslations(session, "abbr", "l:nds-nns", true, new Pair<>("nds", "Nnds."),
			new Pair<>("de", "Nnds."), new Pair<>("en", "N.L.S."));

		DataInitializer.createUiTranslations(session, "full", "l:nds-nns-dm", true, new Pair<>("nds", "Ditmarsk"),
			new Pair<>("de", "Dithmarsch"), new Pair<>("en", "Ditmarsh"));
		DataInitializer.createUiTranslations(session, "abbr", "l:nds-nns-dm", true, new Pair<>("nds", "Dit."),
			new Pair<>("de", "Dit."), new Pair<>("en", "Dit."));

		DataInitializer.createUiTranslations(session, "full", "l:nds-nns-lb", true, new Pair<>("nds", "Lümborgsk"),
			new Pair<>("de", "Lüneburgisch"), new Pair<>("en", "Lunenburgish"));
		DataInitializer.createUiTranslations(session, "abbr", "l:nds-nns-lb", true, new Pair<>("nds", "Lüm."),
			new Pair<>("de", "Lün."), new Pair<>("en", "Lun."));
		//
		DataInitializer.createUiTranslations(session, "full", "l:nds-wf", true, new Pair<>("nds", "Westföälsk"),
			new Pair<>("de", "Westfälisch"), new Pair<>("en", "Westphalian"));
		DataInitializer.createUiTranslations(session, "abbr", "l:nds-wf", true, new Pair<>("nds", "Wf."),
			new Pair<>("de", "Wf."), new Pair<>("en", "Wph."));

		DataInitializer.createUiTranslations(session, "full", "l:nds-wf-sl", true, new Pair<>("nds", "Suderlandsk"),
			new Pair<>("de", "Sauerländisch"), new Pair<>("en", "Sauerlandic"));
		DataInitializer.createUiTranslations(session, "abbr", "l:nds-wf-sl", true, new Pair<>("nds", "Sauerl."),
			new Pair<>("de", "Suderl."), new Pair<>("en", "Sauerl."));

		DataInitializer.createUiTranslations(session, "full", "l:nds-wf-ml", true, new Pair<>("nds", "Mönsterländsk"),
			new Pair<>("de", "Münsterländisch"), new Pair<>("en", "Munsterlandic"));
		DataInitializer.createUiTranslations(session, "abbr", "l:nds-wf-ml", true, new Pair<>("nds", "Möns."),
			new Pair<>("de", "Müns."), new Pair<>("en", "Muns."));
		//
		DataInitializer.createUiTranslations(session, "full", "l:nds-of", true, new Pair<>("nds", "Oustföälsk"),
			new Pair<>("de", "Ostfälisch"), new Pair<>("en", "Eastphalian"));
		DataInitializer.createUiTranslations(session, "abbr", "l:nds-of", true, new Pair<>("nds", "Of."),
			new Pair<>("de", "Of."), new Pair<>("en", "Eph."));

		DataInitializer.createUiTranslations(session, "full", "l:nds-of-hi", true, new Pair<>("nds", "Hilmsensk"),
			new Pair<>("de", "Hildesheimisch"), new Pair<>("en", "Hildesheimian"));
		DataInitializer.createUiTranslations(session, "abbr", "l:nds-of-hi", true, new Pair<>("nds", "Hilm."),
			new Pair<>("de", "Hild."), new Pair<>("en", "Hild."));

		DataInitializer.createUiTranslations(session, "full", "l:nds-of-pd", true, new Pair<>("nds", "Papendyksk"),
			new Pair<>("de", "Papenteichisch"), new Pair<>("en", "Papendician"));
		DataInitializer.createUiTranslations(session, "abbr", "l:nds-of-pd", true, new Pair<>("nds", "Papend."),
			new Pair<>("de", "Papent."), new Pair<>("en", "Papend."));

		//

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

	private void createLexemeTypes(Session session) {
		UiResultCategory uiRC_verbs = new UiResultCategory(null, null, "verbs", "urc:verbs", (short) 1);
		UiResultCategory uiRC_nouns = new UiResultCategory(null, null, "nouns", "urc:nouns", (short) 1);
		session.save(uiRC_verbs);
		session.save(uiRC_nouns);

		/* Setup LexemeTypes */
		// Internal Lexeme Type to map rektion (case government) for high variant languages like Low Saxon
		LexemeType ltCg = new LexemeType(null, null, LexemeType.TYPE_I_CG, uiRC_verbs.getId(), "lt:icg");

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
		// Symbool bruket wy ni sea Janine (dat sünt teykens etc.)
		//LexemeType ltSym = new LexemeType(null, null, LexemeType.TYPE_SYM, uiRC_verbs.getId(), "lt:sym");
		LexemeType ltVerb = new LexemeType(null, null, LexemeType.TYPE_VERB, uiRC_verbs.getId(), "lt:verb");
		LexemeType ltX = new LexemeType(null, null, LexemeType.TYPE_X, uiRC_verbs.getId(), "lt:x");

		session.save(ltCg);
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

		// iCG: Case Government (rektioon)
		LexemeFormType lftCgBase = new LexemeFormType(null, null, ltCg.getId(), "bf", "baseForm",
			null, true, (short) 0);
		session.save(lftCgBase);

		// ADJ
		LexemeFormType lftAdjPositive = new LexemeFormType(null, null, ltAdj.getId(), "pos", "positive",
			null, true, (short) 0);
		LexemeFormType lftAdjComparative = new LexemeFormType(null, null, ltAdj.getId(), "com", "comparative",
			null, true, (short) 1);
		LexemeFormType lftAdjSuperlative = new LexemeFormType(null, null, ltAdj.getId(), "sup", "superlative",
			null, true, (short) 2);
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
		LexemeFormType lftNounSingNom = new LexemeFormType(null, null, ltNoun.getId(), "sn", "sinNom",
			null, true, (short) 0);
		session.save(lftNounSingNom);

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

		// VERB
		LexemeFormType lftVerbInf = new LexemeFormType(null, null, ltVerb.getId(), "inf", "verbInf",
			null, true, (short) 0);
		session.save(lftVerbInf);
		// TODO

		// X
		LexemeFormType lftXBase = new LexemeFormType(null, null, ltX.getId(), "bf", "baseForm",
			null, true, (short) 0);
		session.save(lftXBase);

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


		// Create UiTranslations
		DataInitializer.createUiTranslations(session, "full", "lt:icg", true, new Pair<>("nds", "Rektioon"),
			new Pair<>("de", "Rektion"), new Pair<>("en", "Case Government"));

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

		// Öäversettings vöär de lekseemfoormtypen
		DataInitializer.createUiTranslations(session, "formType", "baseForm", true, new Pair<>("nds", "Grundfoorm"),
			new Pair<>("de", "Grundform"), new Pair<>("en", "Base form"));

		DataInitializer.createUiTranslations(session, "formType", "positive", true, new Pair<>("nds", "Positiv"),
			new Pair<>("de", "Positiv"), new Pair<>("en", "Positive"));
		DataInitializer.createUiTranslations(session, "formType", "comparative", true, new Pair<>("nds", "Komparativ"),
			new Pair<>("de", "Komparativ"), new Pair<>("en", "Comparative"));
		DataInitializer.createUiTranslations(session, "formType", "superlative", true, new Pair<>("nds", "Superlativ"),
			new Pair<>("de", "Superlativ"), new Pair<>("en", "Superlative"));

		DataInitializer.createUiTranslations(session, "formType", "sinNom", true, new Pair<>("nds", "Singulaar / Nominativ"),
			new Pair<>("de", "Singular / Nominativ"), new Pair<>("en", "Singular / Nominative"));
		DataInitializer.createUiTranslations(session, "formType", "verbInf", true, new Pair<>("nds", "Infinitiv"),
			new Pair<>("de", "Infinitiv"), new Pair<>("en", "Infinitive"));

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
}