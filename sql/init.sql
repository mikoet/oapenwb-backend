-- SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
-- SPDX-License-Identifier: AGPL-3.0-only

-- last updated: 2022-02-27

-- these are remainings of the auto-generated stuff from Hibernate

create sequence audio_seq start 1 increment 1;
create sequence langortho_seq start 1 increment 1;
create sequence orthography_seq start 1 increment 1;
create sequence permission_seq start 1 increment 20;
create sequence revision_seq start 1 increment 10;
--create sequence sentence_seq start 1 increment 1;
--create sequence sentencegroup_seq start 1 increment 1;
create sequence synlink_seq start 1 increment 1;
create sequence user_seq start 1 increment 5;

create table Audios (id int8 not null, version int4 not null, active boolean not null, filename varchar(256) not null, formTypeID int4 not null, lexemeID int8 not null, variantID int8 not null, uploadTs timestamp not null, userID int4, primary key (id));
create table Audios_AUD (id int8 not null, REV int8 not null, REVTYPE int2, active boolean, filename varchar(256), formTypeID int4, lexemeID int8, variantID int8, uploadTs timestamp, userID int4, primary key (id, REV));
create table CondensedData (id varchar(7) not null, conType int4, numberOfSearches int4 not null, numberOfUsers int4 not null, topTenSearches varchar(255) not null, primary key (id));
create table Bans (ip varchar(45) not null, bannedUntil timestamp not null, primary key (ip, bannedUntil));
create table LangOrthoMappings (id int4 not null, version int4 not null, langID int4 not null, orthographyID int4 not null, position int2 not null, primary key (id));
create table LangOrthoMappings_AUD (id int4 not null, REV int8 not null, REVTYPE int2, langID int4, orthographyID int4, position int2, primary key (id, REV));
create table Orthographies (id int4 not null, version int4 not null, abbreviation varchar(32) not null, description varchar(1024), parentID int4, publicly boolean, uitID varchar(64) not null, primary key (id));
create table Orthographies_AUD (id int4 not null, REV int8 not null, REVTYPE int2, abbreviation varchar(32), description varchar(1024), parentID int4, publicly boolean, uitID varchar(64), primary key (id, REV));
create table Permissions (id int4 not null, version int4 not null, access varchar(4) not null, entity varchar(128) not null, entityID varchar(64) not null, userID int4 not null, primary key (id));
create table Permissions_AUD (id int4 not null, REV int8 not null, REVTYPE int2, access varchar(4), entity varchar(128), entityID varchar(64), userID int4, primary key (id, REV));
create table SearchRuns (whenTS timestamp not null, hit boolean not null, searchText varchar(255) not null, primary key (whenTS));
--create table SentenceGroups (id int4 not null, version int4 not null, description varchar(512), primary key (id));
--create table Sentences (id int4 not null, version int4 not null, dialectID int4, groupID int4 not null, langID int4 not null, orthographyID int4 not null, text varchar(256) not null, primary key (id));
create table SynLinks (id int4 not null, version int4 not null, endSynGroupID int8 not null, startSynGroupID int8 not null, typeID int4 not null, primary key (id));
create table SynLinks_AUD (id int4 not null, REV int8 not null, REVTYPE int2, endSynGroupID int8, startSynGroupID int8, typeID int4, primary key (id, REV));
create table UiLanguages (locale varchar(32) not null, version int4 not null, active boolean, isDefault boolean, localName varchar(32) not null, primary key (locale));
create table UiLanguages_AUD (locale varchar(32) not null, REV int8 not null, REVTYPE int2, active boolean, isDefault boolean, localName varchar(32), primary key (locale, REV));
create table Users (id int4 not null, version int4 not null, activated boolean not null, email varchar(320) not null, facebookID varchar(64), failedLogins int4, firstname varchar(32) not null, joinTS timestamp not null, lastActiveTS timestamp, lastname varchar(32), pwHash bytea, role character not null, salt varchar(20), showName int4, type int2 not null, username varchar(32), primary key (id));
create table Users_AUD (id int4 not null, REV int8 not null, REVTYPE int2, activated boolean, email varchar(320), facebookID varchar(64), failedLogins int4, firstname varchar(32), joinTS timestamp, lastActiveTS timestamp, lastname varchar(32), pwHash bytea, role character, salt varchar(20), showName int4, type int2, username varchar(32), primary key (id, REV));
create table VersionInfos (version varchar(12) not null, actionTS timestamp, primary key (version));
create table Violation (ip varchar(45) not null, whenTS timestamp not null, info varchar(256) not null, type character not null, primary key (ip, whenTS));


-- From here on things are in self control

create table RevInfos (
	id int8 not null,
	timestamp int8 not null,
	userID int4,
	comment varchar(384),
	primary key (id)
);


create table RegistryTokens (
	token varchar(16) not null,
	email varchar(320),
	used boolean not null,
	validUntil timestamp not null,
	primary key (token)
);
alter table if exists RegistryTokens add constraint UK_RegistryTokens_email unique (email);


create sequence uiresultcat_seq start 1 increment 1;
create table UiResultCategories (
	id int4 not null,
	version int4 not null,
	name varchar(64) not null,
	position int2 not null,
	uitID varchar(64) not null,
	primary key (id)
);

create table UiResultCategories_AUD (
	id int4 not null,
	REV int8 not null,
	REVTYPE int2,
	name varchar(64),
	position int2,
	uitID varchar(64),
	primary key (id, REV)
);


create table UiTranslationScopes (
	id varchar(32) not null,
	version int4 not null,
	description varchar(128),
	essential boolean,
	primary key (id)
);

create table UiTranslationScopes_AUD (
	id varchar(32) not null,
	REV int8 not null,
	REVTYPE int2,
	description varchar(128),
	essential boolean,
	primary key (id, REV)
);


create table UiTranslations (
	id varchar(32) not null,
	version int4 not null,
	locale varchar(32) not null,
	scopeID varchar(31) not null,
	createdAt timestamp not null,
	updatedAt timestamp not null,
	essential boolean not null,
	text varchar(8192) not null,
	primary key (id, locale, scopeID)
);

create table UiTranslations_AUD (
	id varchar(32) not null,
	locale varchar(32) not null,
	scopeID varchar(31) not null,
	REV int8 not null,
	REVTYPE int2,
	createdAt timestamp,
	essential boolean,
	text varchar(8192),
	updatedAt timestamp,
	primary key (id, locale, scopeID, REV)
);



create sequence category_seq start 1 increment 1;
create table Categories (
	id int4 not null,
	version int4 not null,
	parentID int4,
	uitID varchar(64) not null,
	uitID_abbr varchar(64) not null,
	description varchar(1024),
	primary key (id)
);
alter table if exists Categories add constraint FK_Languages_parentID foreign key (parentID) references Categories;

create table Categories_AUD (
	id int4 not null,
	REV int8 not null,
	REVTYPE int2,
	description varchar(1024),
	parentID int4,
	uitID varchar(64),
	uitID_abbr varchar(64),
	primary key (id, REV)
);


create sequence unitlevel_seq start 1 increment 1;
create table UnitLevels (
	id int4 not null,
	version int4 not null,
	uitID varchar(64) not null,
	uitID_abbr varchar(64) not null,
	description varchar(1024),
	primary key (id)
);

create table UnitLevels_AUD (
	id int4 not null,
	REV int8 not null,
	REVTYPE int2,
	description varchar(1024),
	uitID varchar(64),
	uitID_abbr varchar(64),
	primary key (id, REV)
);


create sequence lang_seq start 1 increment 1;
create table Languages (
	id int4 not null,
	version int4 not null,
	uitID_abbr varchar(64) not null,
	locale varchar(32) not null,
	localName varchar(32) not null,
	mainOrthographyID int4 not null,
	parentID int4,
	uitID varchar(64) not null,
	primary key (id)
);
alter table if exists Languages add constraint UK_Languages_locale unique (locale);
alter table if exists Languages add constraint FK_Languages_mainOrthographyID foreign key (mainOrthographyID) references Orthographies;
alter table if exists Languages add constraint FK_Languages_parentID foreign key (parentID) references Languages;

create table Languages_AUD (
	id int4 not null,
	REV int8 not null,
	REVTYPE int2,
	localName varchar(32),
	locale varchar(32),
	mainOrthographyID int4,
	parentID int4,
	uitID varchar(64),
	uitID_abbr varchar(64),
	primary key (id, REV)
);


create table LangPairs (
	id varchar(32) not null,
	version int4 not null,
	langOneID int4 not null,
	langTwoID int4 not null,
	position int4 not null,
	primary key (id)
);
alter table if exists LangPairs add constraint UK_LangPairs_langOneID_langTwoID unique (langOneID, langTwoID);
alter table if exists LangPairs add constraint FK_LangPairs_langOneID foreign key (langOneID) references Languages on delete restrict;
alter table if exists LangPairs add constraint FK_LangPairs_langTwoID foreign key (langTwoID) references Languages on delete restrict;
create table LangPairs_AUD (
	id varchar(32) not null,
	REV int8 not null,
	REVTYPE int2,
	langOneID int4,
	langTwoID int4,
	position int4,
	primary key (id, REV)
);


create sequence lexemetype_seq start 1 increment 1;
create table LexemeTypes (
	id int4 not null,
	version int4 not null,
	name varchar(64),
	uiCategoryID int4,
	uitID varchar(64) not null,
	primary key (id)
);
alter table if exists LexemeTypes add constraint UK_LexemeTypes_name unique (name);
alter table if exists LexemeTypes add constraint FK_LexemeTypes_uiCategoryID foreign key (uiCategoryID) references UiResultCategories;

create table LexemeTypes_AUD (
	id int4 not null,
	REV int8 not null,
	REVTYPE int2,
	name varchar(64),
	uiCategoryID int4,
	uitID varchar(64),
	primary key (id, REV)
);


create sequence lexemeformtype_seq start 1 increment 1;
create table LexemeFormTypes (
	id int4 not null,
	version int4 not null,
	description varchar(1024),
	lexemeTypeID int4 not null,
	mandatory boolean not null,
	name varchar(64) not null,
	position int2 not null,
	uitID varchar(64) not null,
	primary key (id)
);
alter table if exists LexemeFormTypes add constraint FK_LexemeFormTypes_lexemeTypeID foreign key (lexemeTypeID) references LexemeTypes;
-- TODO Create indices on name?

create table LexemeFormTypes_AUD (
	id int4 not null,
	REV int8 not null,
	REVTYPE int2,
	description varchar(1024),
	lexemeTypeID int4,
	mandatory boolean,
	name varchar(64),
	position int2,
	uitID varchar(64),
	primary key (id, REV)
);


create sequence typelang_seq start 1 increment 1;
create table TypeLanguageConfigs (
	id int4 not null,
	version int4 not null,
	lexemeTypeID int4 not null,
	langID int4 not null,
	formTypePositions jsonb,
	primary key (id)
);
alter table if exists TypeLanguageConfigs add constraint UK_TypeLanguageConfigs unique (lexemeTypeID, langID);
alter table if exists TypeLanguageConfigs add constraint FK_TypeLanguageConfigs_lexemeTypeID foreign key (lexemeTypeID) references LexemeTypes;
alter table if exists TypeLanguageConfigs add constraint FK_TypeLanguageConfigs_langID foreign key (langID) references Languages;

create table TypeLanguageConfigs_AUD (
	id int4 not null,
	REV int8 not null,
	REVTYPE int2,
	formTypePositions jsonb,
	langID int4,
	lexemeTypeID int4,
	primary key (id, REV)
);


create sequence lemmatemplate_seq start 1 increment 1;
create table LemmaTemplates (
	id int4 not null,
	version int4 not null,
	name varchar(64),
	lexemeTypeID int4 not null,
	langID int4,
	dialectIDs jsonb,
	orthographyID int4,
	preText varchar(128),
	mainText varchar(256) not null,
	postText varchar(128),
	alsoText varchar(128),
	primary key (id)
);
alter table if exists LemmaTemplates add constraint UK_LemmaTemplates unique (name, lexemeTypeID, langID, dialectIDs, orthographyID);
alter table if exists LemmaTemplates add constraint FK_LemmaTemplates_lexemeTypeID foreign key (lexemeTypeID) references LexemeTypes;
alter table if exists LemmaTemplates add constraint FK_LemmaTemplates_langID foreign key (langID) references Languages;
alter table if exists LemmaTemplates add constraint FK_LemmaTemplates_orthographyID foreign key (orthographyID) references Orthographies;

create table LemmaTemplates_AUD (
	id int4 not null,
	REV int8 not null,
	REVTYPE int2,
	alsoText varchar(128),
	dialectIDs jsonb,
	langID int4,
	lexemeTypeID int4,
	mainText varchar(256),
	name varchar(64),
	orthographyID int4,
	postText varchar(128),
	preText varchar(128),
	primary key (id, REV)
);


create sequence lexeme_seq start 1 increment 1;
create table Lexemes (
	id int8 not null,
	active boolean not null,
	createdAt timestamp not null,
	creatorID int4,
	langID int4 not null,
	notes varchar(8192),
	parserID varchar(48),
	properties jsonb,
	showVariantsFrom int8,
	tags jsonb,
	typeID int4 not null,
	updatedAt timestamp,
	version int4 not null,
	primary key (id),
	constraint check_tags check (tags is null or jsonb_typeof(tags) = 'array')
);
alter table if exists Lexemes add constraint FK_Lexemes_langID foreign key (langID) references Languages on delete restrict;
alter table if exists Lexemes add constraint FK_Lexemes_typeID foreign key (typeID) references LexemeTypes on delete restrict;
alter table if exists Lexemes add constraint FK_Lexemes_showVariantsFrom foreign key (showVariantsFrom) references Lexemes on delete restrict;
alter table if exists Lexemes add constraint U_Lexemes_parserID unique (parserID);
create index IDX_Lexemes_updatedAt on Lexemes (updatedAt);
create index IDX_Lexemes_langID on Lexemes (langID);
create index IDX_Lexemes_typeID on Lexemes (typeID);
create index IDX_Lexemes_parserID on Lexemes (parserID);
create index IDX_Lexemes_active on Lexemes (active);
create index IDX_Lexemes_showVariantsFrom on Lexemes (showVariantsFrom);
create index IDX_Lexemes_tags on Lexemes using GIN (tags jsonb_path_ops);
create index IDX_Lexemes_properties on Lexemes using GIN (properties jsonb_path_ops);
create table Lexemes_AUD (
	id int8 not null,
	REV int8 not null,
	REVTYPE int2,
	active boolean,
	createdAt timestamp,
	creatorID int4,
	langID int4,
	notes varchar(8192),
	parserID varchar(48),
	properties jsonb,
	showVariantsFrom int8,
	tags jsonb,
	typeID int4,
	updatedAt timestamp,
	primary key (id, REV)
);
alter table if exists Lexemes_AUD add constraint Lexemes_AUD_RevInfos_REV foreign key (REV) references RevInfos;


create sequence variant_seq start 1 increment 1;
create table Variants (
	id int8 not null,
	active boolean not null,
	createdAt timestamp not null,
	creatorID int4,
	dialectIDs jsonb,
	also varchar(64),
	fillLemma int4 not null,
	main varchar(64) not null,
	post varchar(32),
	pre varchar(32),
	lexemeID int8 not null,
	mainVariant boolean not null,
	metaInfos jsonb,
	orthographyID int4 not null,
	properties jsonb,
	updatedAt timestamp,
	version int4 not null,
	primary key (id),
	constraint check_dialectIDs check (dialectIDs is null or jsonb_typeof(dialectIDs) = 'array'),
	constraint check_metaInfos check (metaInfos is null or jsonb_typeof(metaInfos) = 'object')
);
--alter table if exists Variants add constraint FK_Variants_dialectID foreign key (dialectID) references Languages on delete restrict;
alter table if exists Variants add constraint FK_Variants_orthographyID foreign key (orthographyID) references Orthographies on delete restrict;
alter table if exists Variants add constraint FK_Variants_lexemeID foreign key (lexemeID) references Lexemes on delete cascade;
create index IDX_Variants_active on Variants (active);
create index IDX_Variants_main on Variants (main);
create index IDX_Variants_pre on Variants (pre);
create index IDX_Variants_post on Variants (post);
create index IDX_Variants_also on Variants (also);
create index IDX_Variants_lexemeID on Variants (lexemeID);
create index IDX_Variants_mainVariant on Variants (mainVariant);
create index IDX_Variants_orthographyID on Variants (orthographyID);
create index IDX_Variants_dialectIDs on Variants using GIN (dialectIDs jsonb_path_ops);
create index IDX_Variants_metaInfos on Variants using GIN (metaInfos jsonb_path_ops);
create index IDX_Variants_properties on Variants using GIN (properties jsonb_path_ops);
-- TODO structure of data in metaInfos shall be: { "key1": {"value":"real value"}, "key2": {"value":"real value 2"}}
-- because this will ultimately allow to search like: SELECT '{ "key1": {"value":"real value"}, "key2": {"value":"real value 2"}}'::jsonb @> '{"key2": {}}'::jsonb; -- true
-- and SELECT '{ "key1": {"value":"real value"}, "key2": {"value":"real value 2"}}'::jsonb @> '{"key2": {"value":"real value 2"}}'::jsonb; -- true

create table Variants_AUD (
	id int8 not null,
	REV int8 not null,
	REVTYPE int2,
	active boolean,
	createdAt timestamp,
	creatorID int4,
	dialectIDs jsonb,
	also varchar(64),
	fillLemma int4,
	main varchar(64),
	post varchar(32),
	pre varchar(32),
	lexemeID int8,
	mainVariant boolean,
	metaInfos jsonb,
	orthographyID int4,
	properties jsonb,
	updatedAt timestamp,
	primary key (id, REV)
);


create sequence sememe_seq start 1 increment 1;
create table Sememes (
	id int8 not null,
	createdAt timestamp not null,
	updatedAt timestamp,
	creatorID int4,
	active boolean,
	categoryIDs jsonb,
	dialectIDs jsonb,
	fillSpec int4 not null,
	internalName varchar(32) not null,
	levelIDs jsonb,
	lexemeID int8 not null,
	properties jsonb,
	spec varchar(64),
	specTemplate varchar(256),
	synGroupID int4,
	variantIDs jsonb not null,
	version int4 not null,
	primary key (id),
	constraint check_categoryIDs check (categoryIDs is null or jsonb_typeof(categoryIDs) = 'array'),
	constraint check_levelIDs check (levelIDs is null or jsonb_typeof(levelIDs) = 'array'),
	constraint check_variantIDs check (jsonb_typeof(variantIDs) = 'array')
);
alter table if exists Sememes add constraint FK_Sememes_lexemeID foreign key (lexemeID) references Lexemes on delete cascade;
create index IDX_Sememes_categoryIDs on Sememes using GIN (categoryIDs jsonb_path_ops);
-- jsonb_path_ops index makes it possible to make indexed searches with the operator @>
-- example: select * from Lexemes where categoryIDs @> '[1, 3]'::jsonb;
-- what it does: select all Lexemes that have the categories 1 and 3
create index IDX_Sememes_dialectIDs on Sememes using GIN (dialectIDs jsonb_path_ops);
create index IDX_Sememes_levelIDs on Sememes using GIN (levelIDs jsonb_path_ops);
create index IDX_Sememes_variantIDs on Sememes using GIN (variantIDs jsonb_path_ops);
create index IDX_Sememes_properties on Sememes using GIN (properties jsonb_path_ops);
create index IDX_Sememes_active on Sememes (active);
create index IDX_Sememes_lexemeID on Sememes (lexemeID);

create table Sememes_AUD (
	id int8 not null,
	REV int8 not null,
	REVTYPE int2,
	createdAt timestamp,
	updatedAt timestamp,
	creatorID int4,
	active boolean,
	categoryIDs jsonb,
	dialectIDs jsonb,
	fillSpec int4,
	internalName varchar(32),
	levelIDs jsonb,
	lexemeID int8,
	properties jsonb,
	spec varchar(64),
	specTemplate varchar(256),
	synGroupID int4,
	variantIDs jsonb not null,
	primary key (id, REV)
);
alter table if exists Sememes_AUD add constraint Sememes_AUD_REvInfos_REV foreign key (REV) references RevInfos;


create table Tags (
	tag varchar(32) not null,
	version int4 not null,
	usageCount int4 not null,
	guarded boolean,
	primary key (tag)
);

create table Tags_AUD (
	tag varchar(32) not null,
	REV int8 not null,
	REVTYPE int2,
	guarded boolean,
	usageCount int4,
	primary key (tag, REV)
);


create table LexemeForms (
	variantID int8 not null,
	formTypeID int4 not null,
	state int2 not null,
	text varchar(64),
	searchableText tsvector generated always as (to_tsvector('simple', text)) stored,
	primary key (variantID, formTypeID),
	constraint check_state check (state in (0, 1, 2, 3, 4))
);
alter table if exists LexemeForms add constraint FK_LexemeForms_variantID foreign key (variantID) references Variants on delete cascade;
alter table if exists LexemeForms add constraint FK_LexemeForms_formTypeID foreign key (formTypeID) references LexemeFormTypes on delete restrict;
create index IDX_LexemeForms_searchableText on LexemeForms using GIN (searchableText);

create table LexemeForms_AUD (
	variantID int8 not null,
	formTypeID int4 not null,
	REV int8 not null,
	REVTYPE int2,
	state int2,
	text varchar(64),
	primary key (variantID, formTypeID, REV)
);


create sequence mapping_seq start 1 increment 1;
create table Mappings (
	id int8 not null,
	version int4 not null,
	creatorID int4,
	langPair varchar(32) not null,
--	lexemeOneID int8 not null,
--	lexemeTwoID int8 not null,
	sememeOneID int8 not null,
	sememeTwoID int8 not null,
	weight int2 not null,
	primary key (id)
);
--alter table if exists Mappings add constraint UK_Mappings_lexemeOneID_lexemeTwoID unique (lexemeOneID, lexemeTwoID);
--alter table if exists Mappings add constraint FK_Mappings_lexemeOneID foreign key (lexemeOneID) references Lexemes;
--alter table if exists Mappings add constraint FK_Mappings_lexemeTwoID foreign key (lexemeTwoID) references Lexemes;
alter table if exists Mappings add constraint FK_Mappings_langPair foreign key (langPair) references LangPairs on delete restrict;
alter table if exists Mappings add constraint UK_Mappings_sememeOneID_sememeTwoID unique (sememeOneID, sememeTwoID);
alter table if exists Mappings add constraint FK_Mappings_sememeOneID foreign key (sememeOneID) references Sememes;
alter table if exists Mappings add constraint FK_Mappings_sememeTwoID foreign key (sememeTwoID) references Sememes;
-- TODO Do not forget indices

create table Mappings_AUD (
	id int8 not null,
	REV int8 not null,
	REVTYPE int2,
	creatorID int4,
	langPair varchar(32),
--	lexemeOneID int8,
--	lexemeTwoID int8,
	sememeOneID int8,
	sememeTwoID int8,
	weight int2,
	primary key (id, REV)
);


create sequence linktype_seq start 1 increment 1;
create table LinkTypes (
	id int4 not null,
	version int4 not null,
	description varchar(512) not null,
	target character not null default 'L',
	start_uitID varchar(64) not null,
	end_uitID varchar(64) not null,
	verbal_uitID varchar(64) not null,
	properties jsonb,
	primary key (id),
	constraint check_target check (target = 'L' or target = 'S')
);
create index IDX_LinkTypes_properties on LinkTypes using GIN (properties jsonb_path_ops);

create table LinkTypes_AUD (
	id int4 not null,
	REV int8 not null,
	REVTYPE int2,
	description varchar(512),
	end_uitID varchar(64),
	start_uitID varchar(64),
	target character,
	verbal_uitID varchar(64),
	properties jsonb,
	primary key (id, REV)
);


create sequence link_seq start 1 increment 1;
create table Links (
	id int4 not null,
	version int4 not null,
	creatorID int4,
	typeID int4 not null,
	startSememeID int8 not null,
	endSememeID int8 not null,
	primary key (id),
	constraint check_sememesAreNotSame check (startSememeID <> endSememeID)
);
alter table if exists Links add constraint FK_Links_typeID foreign key (typeID) references LinkTypes;
alter table if exists Links add constraint FK_Links_startSememeID foreign key (startSememeID) references Sememes;
alter table if exists Links add constraint FK_Links_endSememeID foreign key (endSememeID) references Sememes;

create table Links_AUD (
	id int4 not null,
	REV int8 not null,
	REVTYPE int2,
	creatorID int4,
	endSememeID int8,
	startSememeID int8,
	typeID int4,
	primary key (id, REV)
);


create sequence syngroup_seq start 1 increment 1;
create table SynGroups (
	id int4 not null,
	version int4 not null,
	createdAt timestamp not null,
	creatorID int4,
	description varchar(4096),
	sememeIDs jsonb,	-- must contain only an array of sememeIDs
	presentation varchar(2048) not null,
	updatedAt timestamp,
	primary key (id),
	constraint check_sememeIDs check (sememeIDs is null or jsonb_typeof(sememeIDs) = 'array')
);
create index IDX_SynGroups_sememeIDs on SynGroups using GIN (sememeIDs jsonb_path_ops);

create table SynGroups_AUD (
	id int4 not null,
	REV int8 not null,
	REVTYPE int2,
	createdAt timestamp,
	creatorID int4,
	description varchar(4096),
	sememeIDs jsonb,
	presentation varchar(2048),
	updatedAt timestamp,
	primary key (id, REV)
);


-- the following is generated again
alter table if exists Orthographies add constraint UK_tmvkgpu0jkdf7amubrffxmw2s unique (abbreviation);
alter table if exists Users add constraint UK_ncoa9bfasrql0x4nhmh1plxxy unique (email);
alter table if exists Users add constraint UK_23y4gd49ajvbqgl3psjsvhff6 unique (username);
create index Violation_type_idx on Violation (type);
alter table if exists LangOrthoMappings add constraint FKoajnd2drrbkhtjvq5r60nghu4 foreign key (langID) references Languages;
alter table if exists LangOrthoMappings add constraint FKog6bngtrsff8e60hk3cxb80bd foreign key (orthographyID) references Orthographies;
alter table if exists Orthographies add constraint FK9onah7q1i4i7uka2i2kot5s7c foreign key (parentID) references Orthographies;
alter table if exists Permissions add constraint FK39j46aw4cj6e1g0yefk34npg1 foreign key (userID) references Users;
--alter table if exists Sentences add constraint FKgcx8i19lek87bvjsnkf1knoih foreign key (dialectID) references Languages;
--alter table if exists Sentences add constraint FKm0i8bf0axj7n4mrvh9cgt0kdy foreign key (groupID) references SentenceGroups;
--alter table if exists Sentences add constraint FK6l7sb05a1wyxnsralmib155jm foreign key (langID) references Languages;
--alter table if exists Sentences add constraint FK3c3lvnf2cpmhpv8gtx6piapyx foreign key (orthographyID) references Orthographies;
alter table if exists SynLinks add constraint FKtdecj4osguukahy3ooqnltkmx foreign key (startSynGroupID) references SynGroups;
alter table if exists SynLinks add constraint FK9ub84y8g1nlbrt029esqfnkb6 foreign key (endSynGroupID) references SynGroups;
alter table if exists SynLinks add constraint FKnk3xotfpyrgag0yo2a1i0mswp foreign key (typeID) references LinkTypes;

-- alleyn de _AUD constraints
alter table if exists Audios_AUD add constraint FKqsoqio0lf1qtequda73fja82y foreign key (REV) references RevInfos;
alter table if exists Categories_AUD add constraint FK884pw6h022edg0iibj1e4a3uf foreign key (REV) references RevInfos;
alter table if exists LangOrthoMappings_AUD add constraint FKpc44mjis3ayvbj49eq7lp5vwx foreign key (REV) references RevInfos;
alter table if exists LangPairs_AUD add constraint FKay60carlr0myb3b0jkbj73tim foreign key (REV) references RevInfos;
alter table if exists Languages_AUD add constraint FKsy707uoujgie408qv1a24uqs6 foreign key (REV) references RevInfos;
alter table if exists LemmaTemplates_AUD add constraint FKiy0fyvqiyh8jlp2ja8tu76ilr foreign key (REV) references RevInfos;
alter table if exists LexemeForms_AUD add constraint FKqm67kwnxrf0k3g8ls79gk18ut foreign key (REV) references RevInfos;
alter table if exists LexemeFormTypes_AUD add constraint FK5nkebyivc9fhybssvaumebsdm foreign key (REV) references RevInfos;
alter table if exists Mappings_AUD add constraint FKoo41w5kdwtxtjoytfc7a44cfy foreign key (REV) references RevInfos;
--alter table if exists Lexemes_AUD add constraint FKk25ou47c6w2ksk5ab9jmbokdd foreign key (REV) references RevInfos;
alter table if exists LexemeTypes_AUD add constraint FKqv1wag4xvkbpqwhhedth7y4qm foreign key (REV) references RevInfos;
alter table if exists Links_AUD add constraint FK4qkuh4ym84muq46bfwc8vtlqg foreign key (REV) references RevInfos;
alter table if exists LinkTypes_AUD add constraint FK5dvyytaxvs4iw8s7i8p6pwwh7 foreign key (REV) references RevInfos;
alter table if exists Orthographies_AUD add constraint FKlda86txqpt17b8o60ws9ob5p2 foreign key (REV) references RevInfos;
alter table if exists Permissions_AUD add constraint FK1u07ahdb9lu4rgbjal757c3vs foreign key (REV) references RevInfos;
alter table if exists SynGroups_AUD add constraint FK2g1d27r06j9sjr505mekk9a52 foreign key (REV) references RevInfos;
alter table if exists SynLinks_AUD add constraint FKsn022efrx6uuqpy7qsskuqkot foreign key (REV) references RevInfos;
alter table if exists Tags_AUD add constraint FKjvrrtghx3b4fx2fj4rb3mxj71 foreign key (REV) references RevInfos;
alter table if exists TypeLanguageConfigs_AUD add constraint FKrwtn6gp4hdikm2e5mxxluu9m foreign key (REV) references RevInfos;
alter table if exists UiLanguages_AUD add constraint FK16ejy18bu6wvdq1uvpquttfo3 foreign key (REV) references RevInfos;
alter table if exists UiResultCategories_AUD add constraint FK8sn2417ycpyxrawwkid7cfbqw foreign key (REV) references RevInfos;
alter table if exists UiTranslations_AUD add constraint FK81gsd8wsrg1g74jexh3k0snup foreign key (REV) references RevInfos;
alter table if exists UiTranslationScopes_AUD add constraint FKbmy8ooc9p7xw498l0bxg7l0f7 foreign key (REV) references RevInfos;
alter table if exists UnitLevels_AUD add constraint FKd04qmjwbjbsxjll4wk1uj2grs foreign key (REV) references RevInfos;
alter table if exists Users_AUD add constraint FK46ilhjmslqj9c4ovsd105jb1g foreign key (REV) references RevInfos;
alter table if exists Variants_AUD add constraint FKji90mg52g3eolf4c4gn8u46n4 foreign key (REV) references RevInfos;
