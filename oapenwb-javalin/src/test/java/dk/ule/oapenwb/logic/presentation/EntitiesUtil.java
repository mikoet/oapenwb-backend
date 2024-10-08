// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.presentation;

import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Lemma;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Variant;

import java.util.Set;

public class EntitiesUtil
{
	public static Sememe createSememe(Set<Long> variantIDs, Set<Integer> categoryIDs, Set<Integer> levelIDs)
	{
		Sememe s = new Sememe();
		s.setVariantIDs(variantIDs);
		s.setCategoryIDs(categoryIDs);
		s.setLevelIDs(levelIDs);
		return s;
	}

	public static Variant createVariant(Long id, int orthographyID, Set<Integer> dialectIDs, Lemma lemma, boolean active)
	{
		Variant v = new Variant();
		v.setId(id);
		v.setOrthographyID(orthographyID);
		v.setDialectIDs(dialectIDs);
		v.setLemma(lemma);
		v.setActive(active);
		return v;
	}

	public static Variant createVariant(int orthographyID, Set<Integer> dialectIDs, Lemma lemma, boolean active)
	{
		return createVariant(null, orthographyID, dialectIDs, lemma, active);
	}

	public static Lemma createLemma(String pre, String main, String post, String also)
	{
		Lemma l = new Lemma();
		l.setPre(pre);
		l.setMain(main);
		l.setPost(post);
		l.setAlso(also);
		return l;
	}

	public static Lemma createLemma(String main)
	{
		return createLemma(null, main, null, null);
	}
}
