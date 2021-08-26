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
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import okhttp3.*;

public final class OkHttpEnterpriseClient implements EnterpriseClient {

  private static final MediaType OCTET_STREAM_MEDIA_TYPE =
      MediaType.get("application/octet-stream");
  private static final String AUTHORIZATION_HEADER = "Authorization";

  private final OkHttpClient okHttpClient;
  private final URL url;
  private final String token;

  public OkHttpEnterpriseClient(OkHttpClient okHttpClient, URL url, String token) {
    this.okHttpClient = okHttpClient;
    this.url = url;
    this.token = token;
  }

  public OkHttpEnterpriseClient(URL url, String token) {
    this(new OkHttpClient(), url, token);
  }

  private HttpUrl uploadPackageUrl(UUID artifactId, File file) {
    return HttpUrl.get(url)
        .newBuilder()
        .addPathSegment("artifacts")
        .addPathSegment(artifactId.toString())
        .addPathSegment("content")
        .addQueryParameter("filename", file.getName())
        .build();
  }

  private Request uploadPackageRequest(UUID artifactId, File file) {
    HttpUrl url = uploadPackageUrl(artifactId, file);
    RequestBody body = RequestBody.create(file, OCTET_STREAM_MEDIA_TYPE);
    return new Request.Builder().url(url).put(body).header(AUTHORIZATION_HEADER, token).build();
  }

  @Override
  public long uploadPackage(UUID packageId, File file) throws EnterpriseClientException {
    Request request = uploadPackageRequest(packageId, file);
    try {
      Response response = okHttpClient.newCall(request).execute();
      if (response.isSuccessful()) {
        return file.length();
      } else {
        switch (response.code()) {
          case HttpURLConnection.HTTP_UNAUTHORIZED:
            throw new EnterpriseClientException("Insufficient permissions on token");
          case HttpURLConnection.HTTP_ENTITY_TOO_LARGE:
            throw new EnterpriseClientException("Package exceed maximum allowed size (5 Go)");
          case HttpURLConnection.HTTP_NOT_FOUND:
            throw new EnterpriseClientException(
                String.format("Package with id %s does not exist", packageId));
          default:
            throw new EnterpriseClientException(
                String.format(
                    "Unhandled response (status code: %s, body: %s)",
                    response.code(), response.body().string()));
        }
      }
    } catch (IOException e) {
      throw new EnterpriseClientException("Upload request failed", e);
    }
  }
}
