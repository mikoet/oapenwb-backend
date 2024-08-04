// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.javalin.plugin.json.JavalinJackson;

import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * <p>Utility class with helpful methods for use with JSON data.</p>
 */
public class JsonUtil
{
	/**
	 * Converts a string containing a JSON(B) array from PostgreSQL database into a {@link LinkedHashSet}.
	 *
	 * @param jsonbData string containing the json array
	 * @param <T> type of the returning data within the HashSet, e.g. String, Double, Boolean, ... (possible
	 *   JSON datatypes)
	 * @return LinkedHashSet containing the data from the JSON array in the string
	 * @throws JsonProcessingException Can be thrown where the given jsonbData cannot be deserialized
	 */
	public static <T> LinkedHashSet<T> convertJsonbStringToLinkedHashSet(final String jsonbData)
		throws JsonProcessingException
	{
		if (jsonbData == null) {
			return null;
		}
		LinkedHashSet<T> resultSet = new LinkedHashSet<>();
		Object[] array = JavalinJackson.Companion.defaultMapper().readValue(jsonbData, Object[].class);
		Arrays.stream(array).forEach(o -> {
			T value = (T) o;
			resultSet.add(value);
		});
		return resultSet;
	}
}