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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  @Test
  public void SimulationCreationPayload_marshall() throws JsonProcessingException {
    final Map<String, String> systemProperties = new HashMap<>();
    systemProperties.put("key_1", "First Value");
    systemProperties.put("key_2", "Second Value");
    final Map<UUID, HostByPool> hostsByPool = new HashMap<>();
    hostsByPool.put(UUID.fromString("b2a567f7-07f1-4de7-857a-2e0450b73377"), new HostByPool(1, 25));
    hostsByPool.put(UUID.fromString("43d47d5e-86c3-4918-b047-caf7fb1f1f71"), new HostByPool(2, 75));
    final SimulationCreationPayload simulation =
        new SimulationCreationPayload(
            "My Gatling Simulation",
            UUID.fromString("2bc38879-6dd1-461d-a8fd-47df4991fd9b"),
            "my.package.MyGatlingSimulation",
            UUID.fromString("0cf26226-b261-4af6-a52a-1fec36f4394a"),
            /* jvmOptions */ null,
            systemProperties,
            /* ignoreGlobalProperties */ false,
            new MeaningfulTimeWindow(5, 10),
            hostsByPool,
            /* usePoolWeights */ true,
            /* usePoolDedicatedIps */ false);
    final String actual = JSON_MAPPER.writeValueAsString(simulation);
    final String expected =
        "{\"name\":\"My Gatling Simulation\",\"teamId\":\"2bc38879-6dd1-461d-a8fd-47df4991fd9b\",\"className\":\"my.package.MyGatlingSimulation\",\"systemProperties\":{\"key_2\":\"Second Value\",\"key_1\":\"First Value\"},\"ignoreGlobalProperties\":false,\"meaningfulTimeWindow\":{\"rampUp\":5,\"rampDown\":10},\"hostsByPool\":{\"b2a567f7-07f1-4de7-857a-2e0450b73377\":{\"size\":1,\"weight\":25},\"43d47d5e-86c3-4918-b047-caf7fb1f1f71\":{\"size\":2,\"weight\":75}},\"usePoolWeights\":true,\"usePoolDedicatedIps\":false,\"build\":{\"pkgId\":\"0cf26226-b261-4af6-a52a-1fec36f4394a\"}}";
    assertEquals(expected, actual);
  }

  @Test
  public void SystemPropertyList_marshall() throws JsonProcessingException {
    final List<SystemProperty> simulation =
        Arrays.asList(
            new SystemProperty("Property 1", "Value 1"),
            new SystemProperty("Property 2", "Value 2"));
    final String actual = JSON_MAPPER.writeValueAsString(simulation);
    final String expected =
        "[{\"key\":\"Property 1\",\"value\":\"Value 1\"},{\"key\":\"Property 2\",\"value\":\"Value 2\"}]";
    assertEquals(expected, actual);
  }
}
