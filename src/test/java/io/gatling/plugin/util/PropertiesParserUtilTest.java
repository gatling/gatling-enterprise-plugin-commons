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

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PropertiesParserUtilTest {
  @Test
  void parseProperties_ValidProperties() {
    String properties = "key1=value1,key2=value2,key3=value3=value4=value5";
    final Map<String, String> propertiesMap = new HashMap<>();
    propertiesMap.put("key1", "value1");
    propertiesMap.put("key2", "value2");
    propertiesMap.put("key3", "value3=value4=value5");
    assertEquals(propertiesMap, PropertiesParserUtil.parseProperties(properties));
  }

  @Test
  void parseProperties_InvalidProperties() {
    String properties = "Lorempsumdolorsitamet,consecteturadipiscingelit";
    assertEquals(new HashMap<>(), PropertiesParserUtil.parseProperties(properties));
  }

  @Test
  void parseProperties_SomeInvalidProperties() {
    String properties = "consecteturadipiscing,key1=value1,key2=value2,Loremipsumdolorsitamet";
    final Map<String, String> propertiesMap = new HashMap<>();
    propertiesMap.put("key1", "value1");
    propertiesMap.put("key2", "value2");
    assertEquals(propertiesMap, PropertiesParserUtil.parseProperties(properties));
  }

  @Test
  void parseProperties_WhitespaceKey() {
    String properties = "key1 whitespace=value1,key2=value2,Loremipsumdolorsitamet";
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> PropertiesParserUtil.parseProperties(properties));
    assertTrue(exception.getMessage().startsWith("A property key cannot contain whitespaces"));
  }
}
