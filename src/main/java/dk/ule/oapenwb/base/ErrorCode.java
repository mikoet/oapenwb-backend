// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.base;

import dk.ule.oapenwb.base.error.IMessage;
import dk.ule.oapenwb.util.Pair;
import lombok.Getter;

import java.util.List;

public enum ErrorCode implements IMessage {
	General_MalformedJson(1001, "The received JSON was malformed."),
	General_VersionMismatch(1010, "Database content version differs from app version."),
	General_IpBanned(1020, "Request could not be executed."),

	Config_BaseConfigNotLoaded(1101, "Server-side backend does not offer the dictionary configuration."),
	Config_BaseConfigJsonError(1102, "Server-side backend does not offer the dictionary configuration."),
	L10n_TranslationsNotLoaded(1201, "Server-side backend does not offer translations for the user interface."),

	Register_Critical(1301, "A critical error occured in the registration process."),
	Register_EmailMissing(1302, "The email address is missing."),
	Register_EmailExists(1303, "The given email address is already in use."),
	Register_EmailInvalid(1304, "The given email address is invalid."),
	Register_PasswordMissing(1305, "No password was given."),
	Register_PasswordInvalid(1306, "The given password is invalid."),
	Register_UsernameMissing(1307, "No username was given."),
	Register_UsernameExists(1308, "The given username is already in use."),
	Register_UsernameInvalid(1309, "The given username is invalid."),
	Register_FacebookIdExists(1310, ""),
	Register_TokenIsEmpty(1311, "The token (code) is missing."),
	Register_TokenInvalid(1312, "The token (code) is invalid."),

	Login_NoCredentials(1401, "No credentials were given."),
	Login_Critical(1402, "A critical error occured in the login process."),
	Login_IdentifierBlank(1410, "There is no users for the given email address."),
	Login_PasswordBlank(1411, "There is no users for the given email address."),
	Login_NoUserFound(1420, "The given user could not be found."),
	Login_EmailInvalid(1440, "The given email address is invalid."),
	Login_WrongCredentials(1450, "The given credentials are wrong."),

	Search_NoQueryData(2001, "No query data was submitted."),
	Search_QueryDataInconsistent(2002, "The submitted query data is not consistent."),
	Search_QueryParameterInvalid(2003, "Query parameter '{{param}}' has invalid value '{{value}}'"),

	Email_NotConfigured(3001, "E-mail transport was not configured"),
	Email_SendFailed(3002, "Sending e-mail failed"),

	Admin_UnknownError(11000, ""), // Text will be set seperately for this type of error
	Admin_EntityOperation(11001, "Could not perform operation {{operation}} for type {{entity}} on database."),
	Admin_EntityOperation_NotSupported(11002, "Operation {{operation}} is not supported for type {{entity}}."),
	Admin_EntityOperation_PropertyNotOk(11003, "There was a problem with the property {{property}} when performing "
		+ "operation {{operation}} for type {{entity}} on database."),
	Admin_EntityOperationConstraintViolation(
		11004,  "Could not perform operation {{operation}} for entity of type {{type}} because of a constraint violation."),
	Admin_EntityOperation_OptimisticLock(11005, "Could not perform operation {{operation}} for entity of type {{entity}} on database because the entity was already updated or deleted."),
	Admin_EntityOperation_OptimisticLockWithCause(11006,
		"Could not perform operation {{operation}} for entity of type {{entity}} on database because the entity itself or a connected entity was already updated or deleted.\n" +
		"The causing entity is of type '{{entityName}}' with ID '{{entityID}}'."),

	Admin_EntityNotFound(11010, "The entity of type {{type}} with ID {{id}} does not exist."),
	Admin_EntityUnknownError(11011, "An unknown error occured when processing entity of type {{type}} with ID {{id}}: {{msg}}"),
	Admin_EntitiesOfTypeNotInCreate(11012, "{{types}} must be created in a subsequent step."),
	Admin_EntityBrokenLexemeID(11013, "This lexeme contains a {{type}} that is assigned to another lexeme."),
	Admin_EntityIdSetInCreate(11014, "The id of the entity of type {{type}} must not be set when it shall be created."),
	Admin_EntityIdDiffersInUpdate(11015, "The id of the entity of type {{type}} differs from the URL parameter."),
	Admin_EntityIdNotSet(11015, "The id of an entity of type {{type}} is not set when it shall be updated or deleted."),

	//Admin_Lexeme_(12001, ""),

	Admin_Lexeme_LB_NoTemplatesForLexemeType(12101, "There are no lemma templates defined for the type of the lexeme."),
	Admin_Lexeme_LB_NoAutoTemplate(12102, "No lemma template was found for variant {{variantNo}}."),
	Admin_Lexeme_LB_TemplateNotFound(12103, "Lemma template with ID {{templateID}} was not found for variant {{variantNo}}."),
	Admin_Lexeme_LB_MatchNotFound(12104, "Lemma of variant {{variantNo}} could not be filled via template (ID {{templateID}}). No matching word form was found for variable '{{variable}}'."),
	Admin_Lexeme_LB_NoFormTypesAvailable(12105, "There are no form types available for building the lemmatas."),

	Import_AppPropertyEmpty(20001, "The application property '{{property}}' is not set."),
	Import_AppPropertyInvalidPath(20002, "The path in application property '{{property}}' is not valid or not writable."),
	Import_FileNotExists(20003, "File specified in property '{{property}}' does not exist."),
	Import_FilenameCheckFailed(20010, "Filename in property '{{property}}' contains invalid characters.");

	@Getter
	final int code;
	@Getter
	final String message;
	@Getter
	final List<Pair<String, Object>> arguments;

	ErrorCode(int code, String message, List<Pair<String, Object>> arguments) {
		this.code = code;
		this.message = message;
		this.arguments = arguments;
	}

	ErrorCode(int code, String message) {
		this(code, message, null);
	}
}
