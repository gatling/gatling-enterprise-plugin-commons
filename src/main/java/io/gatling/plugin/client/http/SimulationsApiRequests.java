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

package io.gatling.plugin.client.http;

import com.fasterxml.jackson.core.type.TypeReference;
import io.gatling.plugin.exceptions.EnterprisePluginException;
import io.gatling.plugin.exceptions.SimulationNotFoundException;
import io.gatling.plugin.model.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;

class SimulationsApiRequests extends AbstractApiRequests {

  private static final ApiPath SIM_PATH = ApiPath.of("simulations");

  SimulationsApiRequests(URL baseUrl, String token) {
    super(baseUrl, token);
  }

  Simulation getSimulation(UUID simulationId) throws EnterprisePluginException {
    return getJson(
        SIM_PATH.append(simulationId.toString()),
        Simulation.class,
        response -> {
          if (response.code == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new SimulationNotFoundException(simulationId);
          }
        });
  }

  Simulations listSimulations() throws EnterprisePluginException {
    List<Simulation> data = getJson(SIM_PATH, new TypeReference<List<Simulation>>() {});
    return new Simulations(data);
  }

  Simulation createSimulation(SimulationCreationPayload simulation)
      throws EnterprisePluginException {
    return postJson(SIM_PATH, simulation, Simulation.class);
  }

  RunSummary startSimulation(UUID simulationId, StartOptions options)
      throws EnterprisePluginException {
    final ApiPath path =
        SIM_PATH.append("start").addQueryParam("simulation", simulationId.toString());
    return postJson(path, options, RunSummary.class);
  }

  SimulationClassName updateSimulationClassName(UUID simulationId, String className)
      throws EnterprisePluginException {
    return putJson(
        SIM_PATH.append(simulationId.toString(), "classname"),
        new SimulationClassName(className),
        SimulationClassName.class);
  }
}
