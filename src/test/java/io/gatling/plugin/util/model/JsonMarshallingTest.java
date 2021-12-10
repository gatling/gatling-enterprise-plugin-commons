/*
 * Copyright 2011-2021 GatlingCorp (https://gatling.io)
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

package io.gatling.plugin.util.model;

import static io.gatling.plugin.util.JsonUtil.JSON_MAPPER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class JsonMarshallingTest {

  @Test
  public void PackageCreationPayload_marshall() throws JsonProcessingException {
    final PackageCreationPayload withGlobalTeam = new PackageCreationPayload("First package", null);
    final String actualWithGlobalTeam = JSON_MAPPER.writeValueAsString(withGlobalTeam);
    final String expectedWithGlobalTeam = "{\"name\":\"First package\"}";
    assertEquals(expectedWithGlobalTeam, actualWithGlobalTeam);

    final PackageCreationPayload withSpecificTeam =
        new PackageCreationPayload(
            "First package", UUID.fromString("2bc38879-6dd1-461d-a8fd-47df4991fd9b"));
    final String actualWithSpecificTeam = JSON_MAPPER.writeValueAsString(withSpecificTeam);
    final String expectedWithSpecificTeam =
        "{\"name\":\"First package\",\"teamId\":\"2bc38879-6dd1-461d-a8fd-47df4991fd9b\"}";
    assertEquals(expectedWithSpecificTeam, actualWithSpecificTeam);
  }
}
