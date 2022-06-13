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

import io.gatling.plugin.exceptions.ApiCallIOException;
import io.gatling.plugin.exceptions.EnterprisePluginException;
import io.gatling.plugin.exceptions.InvalidApiCallException;
import io.gatling.plugin.exceptions.PackageNotFoundException;
import io.gatling.plugin.model.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.util.UUID;

class PackagesApiRequests extends AbstractApiRequests {
  PackagesApiRequests(String baseUrl, String token) {
    super(baseUrl, token);
  }

  Packages listPackages() throws EnterprisePluginException {
    return getJson("/artifacts", Packages.class);
  }

  Pkg readPackage(UUID packageId) throws EnterprisePluginException {
    return getJson(
        "/artifacts/" + packageId.toString(),
        Pkg.class,
        response -> {
          if (response.code == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new PackageNotFoundException(packageId);
          }
        });
  }

  Pkg createPackage(PackageCreationPayload pkg) throws EnterprisePluginException {
    return postJson("/artifacts", pkg, Pkg.class);
  }

  long uploadPackage(UUID packageId, File file) throws EnterprisePluginException {
    return executeRequest(
        HTTP_PUT_METHOD,
        "/artifacts/" + packageId.toString() + "/content?filename=" + urlEncode(file.getName()),
        connection -> {
          connection.setRequestProperty(CONTENT_TYPE_HEADER, OCTET_STREAM_MEDIA_TYPE);
          connection.setDoOutput(true);
          try (final OutputStream os = connection.getOutputStream()) {
            Files.copy(file.toPath(), os);
          } catch (IOException e) {
            throw new ApiCallIOException(e);
          }
        },
        response -> file.length(),
        response -> {
          if (response.code == HttpURLConnection.HTTP_ENTITY_TOO_LARGE) {
            throw new InvalidApiCallException("Package exceeds maximum allowed size (5 GB)");
          }
          if (response.code == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new PackageNotFoundException(packageId);
          }
        });
  }
}
