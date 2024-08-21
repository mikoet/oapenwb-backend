// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.faces.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dk.ule.oapenwb.persistency.entity.content.basedata.LangPair;
import dk.ule.oapenwb.logic.admin.LangPairsController;

@Singleton
public class LangPairsFace extends EntityFace<LangPair, String>
{
	@Inject
	public LangPairsFace(LangPairsController controller)
	{
		super(controller);
	}
}
