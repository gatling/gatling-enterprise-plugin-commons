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

package io.gatling.plugin.util;

import com.fasterxml.jackson.core.type.TypeReference;
import io.gatling.plugin.util.model.Simulation;
import io.gatling.plugin.util.model.Simulations;
import java.util.List;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

class SimulationsApiRequests extends AbstractApiRequests {
  SimulationsApiRequests(OkHttpClient okHttpClient, HttpUrl url, String token) {
    super(okHttpClient, url, token);
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
}
