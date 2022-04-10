<!-- SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com> -->
<!-- SPDX-License-Identifier: AGPL-3.0-only -->

# oapen wöördebook (backend)

[//]: # (Shared block begin)

oapen wöördebook (Low Saxon for *open dictionary*, lit. *open wordsbook*, abbreviated *oapenwb*) is an open source
lexeme-centric dictionary software.

oapenwb addresses the special needs that arose when the aim was to build a dictionary for the Low Saxon language
(abbreviated **nds**). Those special needs are:
- **nds** is not standardized, but a multi-dialectal language without a main variant. oapenwb has to handle mutiple 
  variants of a word (lexeme).
- **nds** has no standard spelling, thus oapenwb has to support different spellings of a word.

oapenwb uses the term lexeme in a more narrow sense, i.e. the lexeme as a unit of the different forms of a word
(examples: the verb *to write* including forms as *(he/she/it) writes*, *wrote*, *written*; the noun *ox* including
its plural *oxen*; it shall be noted that there are languages that are morphologically much richter than English is).

Further goals that oapenwb addresses:
- Each lexeme can have multiple variants that can differ in spelling and the dialects a variant is valid for.
- Multiple meanings can be created for each lexeme. Those meanings are called sememes, and they can be put into synonym
  groups with other sememes within one language, and categories and styles can be assigned to each sememe.
  Sememes of one language can be mapped with sememes of another language to declare those as a valid translation.
- Content data can be managed via an administrator's / editor's interface. Content such as lexemes with their variants
  and sememes, but this also concerns more fundamental data as languages, language pairs, orthographies, lexeme types,
  lexeme form types, categories (e.g. animals, plants, medical), or levels/styles (e.g. slang, archaic, vulgar).
- The user interface can be easily translated into a new language, i.e. the UI languages and UI translations can be
  managed via the administrator's interface as well.

oapenwb is in development stage. The administrator's interface is quiet usable.

[//]: # (Shared block end)

## License

Copyright 2022 Michael Köther

Licensed under the AGPLv3: https://opensource.org/licenses/agpl-3.0
