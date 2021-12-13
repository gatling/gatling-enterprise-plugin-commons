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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DatabindException;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class JsonUnmarshallingTest {

  @Test
  public void Packages_unmarshall() throws JsonProcessingException {
    final String json =
        "{\"data\":[{\"id\":\"c0074b5a-5b97-4fbb-bad9-b3848bac3082\",\"name\":\"First package\"},{\"id\":\"44191a0c-2e15-49b3-8876-7aa47cc97587\",\"name\":\"Second package\",\"teamId\":\"40084389-c812-42c1-aaef-f8730959510c\"},{\"id\":\"0cf26226-b261-4af6-a52a-1fec36f4394a\",\"name\":\"Third package\",\"teamId\":\"c5f9e46c-3860-4d95-a851-db0814bdd360\",\"fileName\":\"my-gatling-simulation-1.0.0.jar\"}]}";
    final Packages actual = JSON_MAPPER.readValue(json, Packages.class);
    final Packages expected =
        new Packages(
            Arrays.asList(
                new Package(
                    UUID.fromString("c0074b5a-5b97-4fbb-bad9-b3848bac3082"),
                    null,
                    "First package",
                    null),
                new Package(
                    UUID.fromString("44191a0c-2e15-49b3-8876-7aa47cc97587"),
                    UUID.fromString("40084389-c812-42c1-aaef-f8730959510c"),
                    "Second package",
                    null),
                new Package(
                    UUID.fromString("0cf26226-b261-4af6-a52a-1fec36f4394a"),
                    UUID.fromString("c5f9e46c-3860-4d95-a851-db0814bdd360"),
                    "Third package",
                    "my-gatling-simulation-1.0.0.jar")));
    assertEquals(expected, actual);
  }

  @Test
  public void Packages_unmarshall_invalid() throws JsonProcessingException {
    assertThrows(DatabindException.class, () -> JSON_MAPPER.readValue("{}", Packages.class));
    assertThrows(
        DatabindException.class,
        () ->
            JSON_MAPPER.readValue(
                "{\"data\":[{\"name\":\"Third package\",\"teamId\":\"c5f9e46c-3860-4d95-a851-db0814bdd360\",\"fileName\":\"my-gatling-simulation-1.0.0.jar\"}]}",
                Packages.class));
    assertThrows(
        DatabindException.class,
        () ->
            JSON_MAPPER.readValue(
                "{\"data\":[{\"id\":\"0cf26226-b261-4af6-a52a-1fec36f4394a\",\"teamId\":\"c5f9e46c-3860-4d95-a851-db0814bdd360\",\"fileName\":\"my-gatling-simulation-1.0.0.jar\"}]}",
                Packages.class));
  }

  @Test
  public void Pools_unmarshall() throws JsonProcessingException {
    final String json =
        "{\"data\":[{\"id\":\"b2a567f7-07f1-4de7-857a-2e0450b73377\",\"name\":\"Europe - Paris\"},{\"id\":\"43d47d5e-86c3-4918-b047-caf7fb1f1f71\",\"name\":\"US East - N. Virginia\"}]}";
    final Pools actual = JSON_MAPPER.readValue(json, Pools.class);
    final Pools expected =
        new Pools(
            Arrays.asList(
                new Pool(UUID.fromString("b2a567f7-07f1-4de7-857a-2e0450b73377"), "Europe - Paris"),
                new Pool(
                    UUID.fromString("43d47d5e-86c3-4918-b047-caf7fb1f1f71"),
                    "US East - N. Virginia")));
    assertEquals(expected, actual);
  }

  @Test
  public void Simulations_unmarshall() throws JsonProcessingException {
    final String json =
        "{\"data\":[{\"id\":\"92634bbd-a88b-4c45-8968-756055e19e5b\",\"teamId\":\"2bc38879-6dd1-461d-a8fd-47df4991fd9b\",\"name\":\"My Gatling Simulation\",\"className\":\"my.package.MyGatlingSimulation\",\"build\":{\"artifactId\":\"0cf26226-b261-4af6-a52a-1fec36f4394a\"},\"jvmOptions\":\"string\",\"systemProperties\":{},\"ignoreGlobalProperties\":true,\"meaningfulTimeWindow\":{\"rampUp\":0,\"rampDown\":0},\"hostsByPool\":{\"poolId\":{\"size\":0,\"weight\":0}},\"usePoolWeights\":true,\"usePoolDedicatedIps\":true}]}";
    final Simulations actual = JSON_MAPPER.readValue(json, Simulations.class);
    final Simulations expected =
        new Simulations(
            Arrays.asList(
                new Simulation(
                    UUID.fromString("92634bbd-a88b-4c45-8968-756055e19e5b"),
                    "My Gatling Simulation",
                    UUID.fromString("2bc38879-6dd1-461d-a8fd-47df4991fd9b"),
                    "my.package.MyGatlingSimulation",
                    UUID.fromString("0cf26226-b261-4af6-a52a-1fec36f4394a"))));
    assertEquals(expected, actual);
  }

  @Test
  public void Teams_unmarshall() throws JsonProcessingException {
    final String json =
        "{\"data\":[{\"id\":\"2bc38879-6dd1-461d-a8fd-47df4991fd9b\",\"name\":\"Default team\"},{\"id\":\"0217cee8-f89a-4a70-9384-e4000a88f8d1\",\"name\":\"Other team\"}]}";
    final Teams actual = JSON_MAPPER.readValue(json, Teams.class);
    final Teams expected =
        new Teams(
            Arrays.asList(
                new Team(UUID.fromString("2bc38879-6dd1-461d-a8fd-47df4991fd9b"), "Default team"),
                new Team(UUID.fromString("0217cee8-f89a-4a70-9384-e4000a88f8d1"), "Other team")));
    assertEquals(expected, actual);
  }

  @Test
  public void RunSummary_unmarshall() throws JsonProcessingException {
    final String json =
        "{\"className\": \"my.package.MyGatlingSimulation\",\"runId\": \"34c8716e-00bd-48dc-a28d-0568e9aa9622\",\"reportsUrl\":\"https://cloud.gatling.io/#/simulations/reports/34c8716e-00bd-48dc-a28d-0568e9aa9622/1636401292643/1636401306643/requests/*/*/*\"}\n";
    final RunSummary actual = JSON_MAPPER.readValue(json, RunSummary.class);
    final RunSummary expected =
        new RunSummary(
            UUID.fromString("34c8716e-00bd-48dc-a28d-0568e9aa9622"),
            "my.package.MyGatlingSimulation",
            "https://cloud.gatling.io/#/simulations/reports/34c8716e-00bd-48dc-a28d-0568e9aa9622/1636401292643/1636401306643/requests/*/*/*");
    assertEquals(expected, actual);
  }
}
