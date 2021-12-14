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

import io.gatling.plugin.util.exceptions.EnterpriseClientException;
import io.gatling.plugin.util.exceptions.InvalidApiCallException;
import io.gatling.plugin.util.exceptions.PackageNotFoundException;
import io.gatling.plugin.util.model.Package;
import io.gatling.plugin.util.model.PackageCreationPayload;
import io.gatling.plugin.util.model.Packages;
import java.io.File;
import java.net.HttpURLConnection;
import java.util.UUID;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

class PackagesApiRequests extends AbstractApiRequests {

  PackagesApiRequests(OkHttpClient okHttpClient, HttpUrl url, String token) {
    super(okHttpClient, url, token);
  }

  Packages listPackages() throws EnterpriseClientException {
    HttpUrl requestUrl = url.newBuilder().addPathSegment("artifacts").build();
    Request.Builder request = new Request.Builder().url(requestUrl).get();
    return executeRequest(request, response -> readResponseJson(response, Packages.class));
  }

  Package createPackage(PackageCreationPayload pkg) throws EnterpriseClientException {
    HttpUrl requestUrl = url.newBuilder().addPathSegment("artifacts").build();
    RequestBody body = jsonRequestBody(pkg);
    Request.Builder request = new Request.Builder().url(requestUrl).post(body);
    return executeRequest(request, response -> readResponseJson(response, Package.class));
  }

  long uploadPackage(UUID packageId, File file) throws EnterpriseClientException {
    Request.Builder request = uploadPackageRequest(packageId, file);
    return executeRequest(
        request,
        response -> file.length(),
        response -> {
          if (response.code() == HttpURLConnection.HTTP_ENTITY_TOO_LARGE) {
            throw new InvalidApiCallException("Package exceeds maximum allowed size (5 GB)");
          }
          if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new PackageNotFoundException(packageId);
          }
        });
  }

  private Request.Builder uploadPackageRequest(UUID artifactId, File file) {
    HttpUrl requestUrl =
        url.newBuilder()
            .addPathSegment("artifacts")
            .addPathSegment(artifactId.toString())
            .addPathSegment("content")
            .addQueryParameter("filename", file.getName())
            .build();
    RequestBody body = RequestBody.create(OCTET_STREAM_MEDIA_TYPE, file);
    return new Request.Builder().url(requestUrl).put(body);
  }
}
