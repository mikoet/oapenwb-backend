// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.faces.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dk.ule.oapenwb.entity.content.basedata.LangPair;
import dk.ule.oapenwb.logic.admin.LangPairController;

@Singleton
public class LangPairFace extends EntityFace<LangPair, String>
{
	@Inject
	public LangPairFace(LangPairController controller)
	{
		super(controller);
	}
}
