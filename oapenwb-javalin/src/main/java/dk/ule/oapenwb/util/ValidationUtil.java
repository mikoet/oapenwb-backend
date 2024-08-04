// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.util;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Validation;
import javax.validation.Validator;


/**
 * <p>The ValidationUtil creates one Validator instance (Java Bean Validation) per thread on request
 * and then retains it.</p>
 */
public class ValidationUtil
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationUtil.class);

	//private static ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
	private static final ThreadLocal<Validator> VALIDATORS = new ThreadLocal<>();

	public static Validator getValidator()
	{
		Validator validator = VALIDATORS.get();
		if (validator == null) {
			//validator = FACTORY.getValidator();
			validator = Validation.byDefaultProvider()
								  .configure()
								  .messageInterpolator(new ParameterMessageInterpolator())
								  .buildValidatorFactory()
								  .getValidator();
			VALIDATORS.set(validator);
		}
		return validator;
	}
}