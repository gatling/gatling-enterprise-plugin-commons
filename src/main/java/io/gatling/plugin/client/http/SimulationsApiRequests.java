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

package io.gatling.plugin.client.http;

import com.fasterxml.jackson.core.type.TypeReference;
import io.gatling.plugin.client.exceptions.EnterpriseClientException;
import io.gatling.plugin.client.exceptions.SimulationNotFoundException;
import io.gatling.plugin.model.RunSummary;
import io.gatling.plugin.model.Simulation;
import io.gatling.plugin.model.SimulationCreationPayload;
import io.gatling.plugin.model.Simulations;
import io.gatling.plugin.model.SystemProperty;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.UUID;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

class SimulationsApiRequests extends AbstractApiRequests {
  SimulationsApiRequests(OkHttpClient okHttpClient, HttpUrl url, String token) {
    super(okHttpClient, url, token);
  }

  Simulation getSimulation(UUID simulationId) throws EnterpriseClientException {
    HttpUrl requestUrl =
        url.newBuilder()
            .addPathSegment("simulations")
            .addPathSegment(simulationId.toString())
            .build();
    Request.Builder request = new Request.Builder().url(requestUrl).get();
    return executeRequest(
        request,
        response -> readResponseJson(response, Simulation.class),
        response -> {
          if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new SimulationNotFoundException(simulationId);
          }
        });
  }

  Simulations listSimulations() throws EnterpriseClientException {
    HttpUrl requestUrl = url.newBuilder().addPathSegment("simulations").build();
    Request.Builder request = new Request.Builder().url(requestUrl).get();
    return executeRequest(
        request,
        response -> {
          List<Simulation> data =
              readResponseJson(response, new TypeReference<List<Simulation>>() {});
          return new Simulations(data);
        });
  }

  Simulation createSimulation(SimulationCreationPayload simulation)
      throws EnterpriseClientException {
    HttpUrl requestUrl = url.newBuilder().addPathSegment("simulations").build();
    RequestBody body = jsonRequestBody(simulation);
    Request.Builder request = new Request.Builder().url(requestUrl).post(body);
    return executeRequest(request, response -> readResponseJson(response, Simulation.class));
  }

  RunSummary startSimulation(UUID simulationId, List<SystemProperty> systemProperties)
      throws EnterpriseClientException {
    HttpUrl requestUrl =
        url.newBuilder()
            .addPathSegments("simulations/start")
            .addQueryParameter("simulation", simulationId.toString())
            .build();
    RequestBody body = jsonRequestBody(systemProperties);
    Request.Builder request = new Request.Builder().url(requestUrl).post(body);
    return executeRequest(request, response -> readResponseJson(response, RunSummary.class));
  }
}
