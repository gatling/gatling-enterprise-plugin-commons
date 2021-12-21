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

import io.gatling.plugin.client.exceptions.EnterpriseClientException;
import io.gatling.plugin.model.Teams;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

class TeamsApiRequests extends AbstractApiRequests {
  TeamsApiRequests(OkHttpClient okHttpClient, HttpUrl url, String token) {
    super(okHttpClient, url, token);
  }

  Teams listTeams() throws EnterpriseClientException {
    HttpUrl requestUrl = url.newBuilder().addPathSegment("teams").build();
    Request.Builder request = new Request.Builder().url(requestUrl).get();
    return executeRequest(request, response -> readResponseJson(response, Teams.class));
  }
}
