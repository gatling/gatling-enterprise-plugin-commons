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

import static io.gatling.plugin.util.LambdaExceptionUtil.FunctionWithExceptions;

public final class OkHttpEnterpriseClient implements EnterpriseClient {

  private static final MediaType OCTET_STREAM_MEDIA_TYPE =
      MediaType.get("application/octet-stream");
  private static final String AUTHORIZATION_HEADER = "Authorization";

  private final OkHttpClient okHttpClient;
  private final HttpUrl url;
  private final String token;

  public OkHttpEnterpriseClient(OkHttpClient okHttpClient, URL url, String token) {
    this.okHttpClient = okHttpClient;
    this.url = HttpUrl.get(url);
    this.token = token;
  }

  public OkHttpEnterpriseClient(URL url, String token) {
    this(new OkHttpClient(), url, token);
  }

  @Override
  public void checkVersionSupport(String client, String version)
      throws UnsupportedClientException, EnterpriseClientException {
    Request.Builder request = checkVersionSupportRequest(client, version);
    // TODO see how this API call should respond
    boolean isSupported = executeRequest(request, Response::isSuccessful);
    if (!isSupported) {
        throw new UnsupportedClientException(client, version);
    }
  }

  private Request.Builder checkVersionSupportRequest(String client, String version) {
    // TODO update this API call url/parameters
    HttpUrl requestUrl =
            url.newBuilder()
                    .addPathSegment("supportedClient")
                    .addQueryParameter("name", client)
                    .addQueryParameter("version", version)
                    .build();
    return new Request.Builder().url(requestUrl).get();
  }

  @Override
  public long uploadPackage(UUID packageId, File file) throws EnterpriseClientException {
    Request.Builder request = uploadPackageRequest(packageId, file);
    return executeRequest(request, response -> {
        if (response.isSuccessful()) {
            return file.length();
        } else {
            switch (response.code()) {
                case HttpURLConnection.HTTP_ENTITY_TOO_LARGE:
                    throw new EnterpriseClientException("Package exceed maximum allowed size (5 Go)");
                case HttpURLConnection.HTTP_NOT_FOUND:
                    throw new EnterpriseClientException(String.format("Package with id %s does not exist", packageId));
                default:
                    throw new EnterpriseClientException(String.format("Unhandled response (status code: %s, body: %s)", response.code(), readResponseBody(response)));
            }
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

  private <T> T executeRequest(
      Request.Builder unauthenticatedRequest, FunctionWithExceptions<Response, T, EnterpriseClientException> handleResponse)
      throws EnterpriseClientException {
    Request request = unauthenticatedRequest.header(AUTHORIZATION_HEADER, token).build();
    try (Response response = okHttpClient.newCall(request).execute()) {
        validateResponse(response);
        return handleResponse.apply(response);
    } catch (IOException e) {
      throw new EnterpriseClientException("A call to the Gatling Enterprise API failed", e);
    }
  }

  private void validateResponse(Response response) throws EnterpriseClientException {
    if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
      throw new EnterpriseClientException(
          "Your API token was not recognized by the Gatling Enterprise server: please configure a valid token");
    }
    if (response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
      throw new EnterpriseClientException(
          "Your API token does not have enough privileges: please configure a token with the correct role");
    }
  }

  private String readResponseBody(Response response) throws EnterpriseClientException {
    try (ResponseBody body = response.body()) {
      return body.string();
    } catch (IOException e) {
      throw new EnterpriseClientException("The Gatling Enterprise API returned an unreadable response");
    }
  }
}
