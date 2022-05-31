/*
 * Copyright 2011-2022 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gatling.plugin.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public final class PropertiesParserUtil {
  private PropertiesParserUtil() {}

  /**
   * Parse a String following the format key1=value1,key2=value2 into a map
   *
   * @param properties the properties map
   * @return The map containing the properties, minus malformed properties
   */
  public static Map<String, String> parseProperties(String properties) {
    if (properties == null) {
      return Collections.emptyMap();
    }
    return Arrays.stream(properties.split(","))
        .map(property -> property.split("=", 2))
        .filter(property -> property.length == 2)
        .collect(
            Collectors.toMap(
                property -> {
                  String key = property[0].trim();
                  if (key.matches(".*\\s.*")) {
                    throw new IllegalArgumentException(
                        "A property key cannot contain whitespaces, invalid property: " + key);
                  }
                  return key;
                },
                property -> property[1].trim()));
  }
}
