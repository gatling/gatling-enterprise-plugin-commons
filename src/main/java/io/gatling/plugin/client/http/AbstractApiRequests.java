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

import static io.gatling.plugin.client.json.JsonUtil.JSON_MAPPER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.gatling.plugin.exceptions.*;
import io.gatling.plugin.util.InputStreamUtils;
import io.gatling.plugin.util.LambdaExceptionUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

abstract class AbstractApiRequests {

  private static final String HTTP_GET_METHOD = "GET";
  private static final String HTTP_POST_METHOD = "POST";
  private static final String HTTP_PUT_METHOD = "PUT";

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String ACCEPT_HEADER = "Accept";
  protected static final String CONTENT_TYPE_HEADER = "Content-Type";

  private static final String CONNECTION_HEADER = "Connection";

  protected static final String OCTET_STREAM_MEDIA_TYPE = "application/octet-stream";
  protected static final String JSON_MEDIA_TYPE = "application/json";

  protected static final String CLOSE = "close";

  private static final int DEFAULT_TIMEOUT_MS = 10_000;

  protected final URL baseUrl;
  protected final String token;

  AbstractApiRequests(URL baseUrl, String token) {
    this.baseUrl = baseUrl;
    this.token = token;
  }

  protected HttpResponse get(
      ApiPath path,
      LambdaExceptionUtil.ConsumerWithExceptions<HttpResponse, EnterprisePluginException>
          validateResponse)
      throws EnterprisePluginException {
    return executeRequest(HTTP_GET_METHOD, path, connection -> {}, validateResponse);
  }

  protected <T> T getJson(
      ApiPath path,
      Class<T> valueType,
      LambdaExceptionUtil.ConsumerWithExceptions<HttpResponse, EnterprisePluginException>
          validateResponse)
      throws EnterprisePluginException {
    return parseJsonResponse(get(path, validateResponse), valueType);
  }

  protected <T> T getJson(ApiPath path, Class<T> valueType) throws EnterprisePluginException {
    return getJson(path, valueType, response -> {});
  }

  protected <T> T getJson(
      ApiPath path,
      TypeReference<T> valueTypeRef,
      LambdaExceptionUtil.ConsumerWithExceptions<HttpResponse, EnterprisePluginException>
          validateResponse)
      throws EnterprisePluginException {
    return parseJsonResponse(get(path, validateResponse), valueTypeRef);
  }

  protected <T> T getJson(ApiPath path, TypeReference<T> valueTypeRef)
      throws EnterprisePluginException {
    return getJson(path, valueTypeRef, response -> {});
  }

  protected HttpResponse post(
      ApiPath path,
      LambdaExceptionUtil.ConsumerWithExceptions<HttpURLConnection, EnterprisePluginException>
          beforeRequest,
      LambdaExceptionUtil.ConsumerWithExceptions<HttpResponse, EnterprisePluginException>
          validateResponse)
      throws EnterprisePluginException {
    return executeRequest(HTTP_POST_METHOD, path, beforeRequest, validateResponse);
  }

  protected <T> T postJson(
      ApiPath path,
      Object body,
      Class<T> valueType,
      LambdaExceptionUtil.ConsumerWithExceptions<HttpResponse, EnterprisePluginException>
          validateResponse)
      throws EnterprisePluginException {
    return parseJsonResponse(
        post(path, connection -> writeJsonRequestBody(connection, body), validateResponse),
        valueType);
  }

  protected <T> T postJson(ApiPath path, Object body, Class<T> valueType)
      throws EnterprisePluginException {
    return postJson(path, body, valueType, response -> {});
  }

  protected HttpResponse put(
      ApiPath path,
      LambdaExceptionUtil.ConsumerWithExceptions<HttpURLConnection, EnterprisePluginException>
          beforeRequest,
      LambdaExceptionUtil.ConsumerWithExceptions<HttpResponse, EnterprisePluginException>
          validateResponse)
      throws EnterprisePluginException {
    return executeRequest(HTTP_PUT_METHOD, path, beforeRequest, validateResponse);
  }

  protected <T> T putJson(
      ApiPath path,
      Object body,
      Class<T> valueType,
      LambdaExceptionUtil.ConsumerWithExceptions<HttpResponse, EnterprisePluginException>
          validateResponse)
      throws EnterprisePluginException {
    return parseJsonResponse(
        put(path, connection -> writeJsonRequestBody(connection, body), validateResponse),
        valueType);
  }

  protected <T> T putJson(ApiPath path, Object body, Class<T> valueType)
      throws EnterprisePluginException {
    return putJson(path, body, valueType, response -> {});
  }

  private HttpResponse executeRequest(
      String method,
      ApiPath path,
      LambdaExceptionUtil.ConsumerWithExceptions<HttpURLConnection, EnterprisePluginException>
          beforeRequest,
      LambdaExceptionUtil.ConsumerWithExceptions<HttpResponse, EnterprisePluginException>
          validateResponse)
      throws EnterprisePluginException {
    try {
      final HttpURLConnection connection = openConnection(method, path.buildUrl(baseUrl));

      beforeRequest.accept(connection);
      connection.connect();

      try {
        final HttpResponse response = readResponse(connection);

        validateResponse.accept(response);
        defaultValidateResponse(response);
        return response;
      } finally {
        connection.disconnect();
      }
    } catch (IOException e) {
      throw new ApiCallIOException(e);
    }
  }

  private HttpURLConnection openConnection(String method, URL url) throws IOException {
    final URLConnection urlConnection = url.openConnection();
    if (urlConnection instanceof HttpURLConnection) {
      final HttpURLConnection connection = (HttpURLConnection) urlConnection;
      connection.setRequestMethod(method);
      connection.setRequestProperty(AUTHORIZATION_HEADER, token);
      connection.setRequestProperty(ACCEPT_HEADER, JSON_MEDIA_TYPE);
      connection.setRequestProperty(AUTHORIZATION_HEADER, token);
      connection.setRequestProperty(CONNECTION_HEADER, CLOSE);
      connection.setConnectTimeout(DEFAULT_TIMEOUT_MS);
      connection.setReadTimeout(DEFAULT_TIMEOUT_MS);
      return connection;
    } else {
      throw new IllegalStateException(
          "Expected an HttpURLConnection, got " + urlConnection.getClass().getName());
    }
  }

  private InputStream getRealInputStream(HttpURLConnection connection) {
    try {
      return connection.getInputStream();
    } catch (IOException e) {
      return connection.getErrorStream();
    }
  }

  private HttpResponse readResponse(HttpURLConnection connection) throws IOException {
    int status = connection.getResponseCode();
    if (status < 0) {
      throw new IOException("Response could not be parsed as HTTP");
    }

    try (final InputStream is = getRealInputStream(connection)) {
      if (is == null) {
        return new HttpResponse(status, "");
      } else {
        return new HttpResponse(
            status, InputStreamUtils.inputStreamToString(is, StandardCharsets.UTF_8));
      }
    }
  }

  private void defaultValidateResponse(HttpResponse response) throws EnterprisePluginException {
    if (!response.isSuccessful()) {
      switch (response.code) {
        case HttpURLConnection.HTTP_UNAUTHORIZED:
          throw new UnauthorizedApiCallException();
        case HttpURLConnection.HTTP_FORBIDDEN:
          throw new ForbiddenApiCallException();
        case HttpURLConnection.HTTP_BAD_REQUEST:
          throw new InvalidApiCallException(response.body);
        default:
          throw new UnhandledApiCallException(response.code, response.body);
      }
    }
  }

  private <T> T parseJsonResponse(HttpResponse response, Class<T> valueType) {
    try {
      return JSON_MAPPER.readValue(response.body, valueType);
    } catch (JsonProcessingException e) {
      throw new JsonResponseProcessingException(e);
    }
  }

  private <T> T parseJsonResponse(HttpResponse response, TypeReference<T> valueTypeRef) {
    try {
      return JSON_MAPPER.readValue(response.body, valueTypeRef);
    } catch (JsonProcessingException e) {
      throw new JsonResponseProcessingException(e);
    }
  }

  private void writeJsonRequestBody(HttpURLConnection connection, Object body)
      throws ApiCallIOException {
    connection.setRequestProperty(CONTENT_TYPE_HEADER, JSON_MEDIA_TYPE);
    connection.setDoOutput(true);
    try (final OutputStream os = connection.getOutputStream()) {
      JSON_MAPPER.writeValue(os, body);
    } catch (JsonProcessingException e) {
      throw new JsonRequestProcessingException(e);
    } catch (IOException e) {
      throw new ApiCallIOException(e);
    }
  }
}
