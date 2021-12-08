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

  long uploadPackage(UUID packageId, File file) throws EnterpriseClientException {
    Request.Builder request = uploadPackageRequest(packageId, file);
    return executeRequest(
        request,
        response -> file.length(),
        response -> {
          if (response.code() == HttpURLConnection.HTTP_ENTITY_TOO_LARGE) {
            throw new EnterpriseClientException("Package exceeds maximum allowed size (5 GB)");
          }
          if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new EnterpriseClientException(
                String.format("Package with id %s does not exist", packageId));
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
