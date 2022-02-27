// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.lexeme;

import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.base.error.IMessage;
import dk.ule.oapenwb.base.error.Message;
import dk.ule.oapenwb.base.error.MultiCodeException;
import dk.ule.oapenwb.entity.basis.ApiAction;
import dk.ule.oapenwb.entity.content.basedata.LemmaTemplate;
import dk.ule.oapenwb.entity.content.basedata.LexemeFormType;
import dk.ule.oapenwb.entity.content.lexemes.LexemeForm;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lemma;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.logic.admin.generic.CGEntityController;
import dk.ule.oapenwb.util.Pair;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The LemmaTemplateProcessor processes the lemma templates and thus produces the parts of a lemma (pre, main, post).
 */
public class LemmaTemplateProcessor
{
	private static final Logger LOG = LoggerFactory.getLogger(LemmaTemplateProcessor.class);

	// Regex to extract variables of the kind: "$.FORMTYPENAME" i.e. "$.inf"
	// (?<=^|[^A-Za-z])([A-Z][a-z]*)(?=[^A-Za-z]|$)
	private static final Pattern PATTERN_WAY_ONE = Pattern.compile("(?<=^|[^A-Za-z])(\\$\\.[a-zA-Z0-9_]{1,64})(?=[^A-Za-z]|$)");
	// Regex to extract variables of the kind: "$(FORMTYPENAME)" i.e. "$(inf)
	private static final Pattern PATTERN_WAY_TWO = Pattern.compile("(?<=^|[^A-Za-z])(\\$\\([a-zA-Z0-9_]{1,64}\\))(?=[^A-Za-z]|$)");

	// Regex to extract the variable name from the results of both pre patterns PATTERN_WAY_ONE and PATTERN_WAY_TWO
	private static final Pattern FORM_NAME_EXTRACTOR = Pattern.compile("(?<=^|[^A-Za-z])([a-zA-Z0-9_]{1,64})(?=[^A-Za-z]|$)");

	private final Session session;
	private final LexemeDetailedDTO lexemeDTO;
	private final CGEntityController<LemmaTemplate, Integer, Integer> ltController;
	private final CGEntityController<LexemeFormType, Integer, Integer> lftController;

	private final Map<String, LexemeFormType> formTypesMap;

	private int compositesCount = 0;
	private List<IMessage> errors = new LinkedList<>();

	// this variable is setto the variant that is currently processed, or else it's null
	private Integer variantNo = null;
	private Integer templateID = null;

	public LemmaTemplateProcessor(final Session session, final LexemeDetailedDTO lexemeDTO,
			final CGEntityController<LemmaTemplate, Integer, Integer> ltController,
			final CGEntityController<LexemeFormType, Integer, Integer> lftController) throws CodeException {
		this.session = session;
		this.lexemeDTO = lexemeDTO;
		this.ltController = ltController;
		this.lftController = lftController;
		// Also create the formTypesMap and fill it for later use
		this.formTypesMap = new HashMap<>();
		List<LexemeFormType> formTypes = lftController.getEntitiesByGroupKey(lexemeDTO.getLexeme().getTypeID());
		if (formTypes != null && formTypes.size() > 0) {
			for (final LexemeFormType formType : formTypes) {
				formTypesMap.put(formType.getName(), formType);
			}
		} else {
			throw new CodeException(ErrorCode.Admin_Lexeme_LB_NoFormTypesAvailable, null);
		}
	}

	public void buildLemmata() throws MultiCodeException {
		final Lexeme lexeme = lexemeDTO.getLexeme();
		// TODO 001
		//lexeme.setCompositeLemma(false);

		this.variantNo = 0;
		for (Variant variant : lexemeDTO.getVariants()) {
			if (variant.getApiAction() != ApiAction.Delete) {
				variantNo++;
				buildLemma(variant);
			}
		}
		this.variantNo = null;

		// TODO 001
		/* if ((this.compositesCount > 0 && !lexeme.getLemma().isInMainLemma())
				|| this.compositesCount > 1) {
			lexeme.setCompositeLemma(true);
			this.session.saveOrUpdate(lexeme);
		} */

		if (errors.size() > 0) {
			throw new MultiCodeException(errors);
		}
	}

	private void buildLemma(final Variant unit) {
		final Lemma lemma = unit.getLemma();
		// TODO 001
		/* if (lemma.isInMainLemma()) {
			this.compositesCount++;
		} */
		LemmaTemplate template = null;
		if (lemma.getFillLemma() == Lemma.FILL_LEMMA_AUTOMATICALLY) {
			try {
				template = findLemmaTemplateAutomatically(this.lexemeDTO.getLexeme(), unit);
			} catch (CodeException e) {
				// TODO write a static method that will replace the variables with the arguments
				LOG.error("Error when trying to get lemma template automatically: ", e.getMessage());
				LOG.error("Lexical unit ID: ", unit.getId());
			}
			if (template == null) {
				// Error was already added in the method #findLemmaTemplateAutomatically() itself.
				return;
			}
		} else if (lemma.getFillLemma() == Lemma.FILL_LEMMA_MANUALLY) {
			// nothing to do, the lemma is already filled
			return;
		} else {
			try {
				template = this.ltController.get(lemma.getFillLemma());
			} catch (CodeException e) {
				// TODO write a static method that will replace the variables with the arguments
				LOG.error("Error when trying to get lemma template: {}", e.getMessage());
				LOG.error("Template ID: {}", lemma.getFillLemma());
				LOG.error("Lexical unit ID: {}", unit.getId());
			}
			if (template == null) {
				this.errors.add(new Message(ErrorCode.Admin_Lexeme_LB_TemplateNotFound,
					Arrays.asList(new Pair<>("templateID", lemma.getFillLemma()), new Pair<>("variantNo", variantNo))));
				return;
			}
		}
		// Build the Lemma parts
		this.templateID = template.getId();
		unit.getLemma().setPre(generateLemmaPart(template.getPreText(), unit.getLexemeForms()));
		unit.getLemma().setMain(generateLemmaPart(template.getMainText(), unit.getLexemeForms()));
		unit.getLemma().setPost(generateLemmaPart(template.getPostText(), unit.getLexemeForms()));
		unit.getLemma().setAlso(generateLemmaPart(template.getAlsoText(), unit.getLexemeForms()));
		this.templateID = null;

		this.session.saveOrUpdate(unit);
	}

	private String generateLemmaPart(final String templatePart, final List<LexemeForm> lexemeForms)
	{
		if (templatePart == null || templatePart.isEmpty()) {
			return null;
		}
		// use both patterns to find variables to replace
		String result = replaceMatches(PATTERN_WAY_ONE.matcher(templatePart), templatePart, lexemeForms);
		result = replaceMatches(PATTERN_WAY_TWO.matcher(result), result, lexemeForms);
		return result;
	}

	private String replaceMatches(final Matcher matcher, final String templatePart, final List<LexemeForm> lexemeForms)
	{
		int lastIndex = 0;
		StringBuilder output = new StringBuilder();
		while (matcher.find()) {
			output.append(templatePart, lastIndex, matcher.start())
				  .append(replaceVar(matcher.group(1), lexemeForms));

			lastIndex = matcher.end();
		}
		if (lastIndex < templatePart.length()) {
			output.append(templatePart, lastIndex, templatePart.length());
		}

		return output.toString();
	}

	// Replaces a var like "$.inf" or "$(inf)" with the text of a lexeme form.
	private String replaceVar(final String match, final List<LexemeForm> lexemeForms)
	{
		final Matcher matcher = FORM_NAME_EXTRACTOR.matcher(match);
		if (matcher.find()) {
			final String formTypeName = matcher.group();
			final LexemeFormType formType = this.formTypesMap.get(formTypeName);
			if (formType != null) {
				final int formTypeID = this.formTypesMap.get(formTypeName).getId();
				for (LexemeForm form : lexemeForms) {
					if (formTypeID == form.getFormTypeID()) {
						// found the right LexemeForm
						return form.getText();
					}
				}
			}
		}
		this.errors.add(new Message(ErrorCode.Admin_Lexeme_LB_MatchNotFound,
			Arrays.asList(
				new Pair<>("variantNo", variantNo),
				new Pair<>("templateID", templateID),
				new Pair<>("variable", match))));
		return "";
	}

	private LemmaTemplate findLemmaTemplateAutomatically(final Lexeme lexeme, final Variant variant) throws CodeException {
		LemmaTemplate template = null;
		List<LemmaTemplate> templates = this.ltController.getEntitiesByGroupKey(this.lexemeDTO.getLexeme().getTypeID());
		if (templates == null || templates.size() == 0) {
			this.errors.add(new Message(ErrorCode.Admin_Lexeme_LB_NoTemplatesForLexemeType));
			return null;
		}
		// Filter out those that got a name – they are meant to be specific and to be manually selected
		templates = templates.stream().filter(theTemplate -> theTemplate.getName() == null || theTemplate.getName()
									  .isEmpty()).collect(Collectors.toList());
		if (templates == null || templates.size() == 0) {
			this.errors.add(new Message(ErrorCode.Admin_Lexeme_LB_NoAutoTemplate,
				Collections.singletonList(new Pair<>("variantNo", variantNo))));
			return null;
		}

		int langID = lexeme.getLangID();
		Set<Integer> dialectIDs = variant.getDialectIDs() != null ? variant.getDialectIDs() : new LinkedHashSet<>();
		int orthoID = variant.getOrthographyID();
		// Step 1: Try to find the template where all parameters (language, dialects and orthography) fit
		for (LemmaTemplate candidate : templates) {
			Set<Integer> candidateDialects = candidate.getDialectIDs() != null ?
				candidate.getDialectIDs() : new LinkedHashSet<>();
			boolean langsFit = candidate.getLangID() != null && langID == candidate.getLangID();
			/* TODO By de dialekten müs eyn eygens ouk de ölderen-kind-betrekking bekyken */
			boolean dialectsFit = (dialectIDs.isEmpty() && candidateDialects.isEmpty())
				|| candidateDialects.containsAll(dialectIDs);
			boolean orthosFit = candidate.getOrthographyID() != null && orthoID == candidate.getOrthographyID();
			if (langsFit && dialectsFit && orthosFit) {
				template = candidate;
				break;
			}
		}
		if (template == null) {
			// Step 2: Try to find the template where language and dialects fit (and orthography is not set)
			for (LemmaTemplate candidate : templates) {
				Set<Integer> candidateDialects = candidate.getDialectIDs() != null ?
					candidate.getDialectIDs() : new LinkedHashSet<>();
				boolean langsFit = candidate.getLangID() != null && langID == candidate.getLangID();
				boolean dialectsFit = (dialectIDs.isEmpty() && candidateDialects.isEmpty())
					|| candidateDialects.containsAll(dialectIDs);
				if (langsFit && dialectsFit && candidate.getOrthographyID() == null) {
					template = candidate;
					break;
				}
			}
		}
		if (template == null) {
			// Step 3: Try to find the template where only the language fits and the rest is not set
			for (LemmaTemplate candidate : templates) {
				Set<Integer> candidateDialects = candidate.getDialectIDs() != null ?
					candidate.getDialectIDs() : new LinkedHashSet<>();
				boolean langsFit = candidate.getLangID() != null && langID == candidate.getLangID();
				if (langsFit && candidateDialects.isEmpty() && candidate.getOrthographyID() == null) {
					template = candidate;
					break;
				}
			}
		}
		if (template == null) {
			// Step 4: Try to find the template where only the orthography fits and the rest is not set
			for (LemmaTemplate candidate : templates) {
				Set<Integer> candidateDialects = candidate.getDialectIDs() != null ?
					candidate.getDialectIDs() : new LinkedHashSet<>();
				boolean orthosFit = candidate.getOrthographyID() != null && orthoID == candidate.getOrthographyID();
				if (orthosFit && candidate.getLangID() == null
						&& candidateDialects.isEmpty()) {
					template = candidate;
					break;
				}
			}
		}
		if (template == null) {
			// Step 5: Try to find the template where nothing fits (that means, only the LexemeType fits)
			for (LemmaTemplate candidate : templates) {
				Set<Integer> candidateDialects = candidate.getDialectIDs() != null ?
					candidate.getDialectIDs() : new LinkedHashSet<>();
				if (candidate.getOrthographyID() == null && candidate.getLangID() == null
						&& candidateDialects.isEmpty()) {
					template = candidate;
					break;
				}
			}
		}
		return template;
	}
}