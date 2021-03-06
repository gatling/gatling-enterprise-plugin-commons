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

import io.gatling.plugin.exceptions.EnterprisePluginException;
import io.gatling.plugin.exceptions.UnsupportedClientException;
import java.net.HttpURLConnection;
import java.net.URL;

class PrivateApiRequests extends AbstractApiRequests {

  PrivateApiRequests(URL baseUrl, String token) {
    super(baseUrl, token);
  }

  /** @throws UnsupportedClientException if this client version is outdated */
  void checkVersionSupport(String client, String version) throws EnterprisePluginException {
    ApiPath path =
        ApiPath.of("compatibility")
            .addQueryParam("clientName", client)
            .addQueryParam("version", version);
    get(
        path,
        response -> {
          if (response.code == HttpURLConnection.HTTP_BAD_REQUEST) {
            throw new UnsupportedClientException(client, version);
          }
        });
  }
}
