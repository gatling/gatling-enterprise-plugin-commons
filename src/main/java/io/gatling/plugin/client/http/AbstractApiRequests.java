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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

abstract class AbstractApiRequests {

  protected static final String HTTP_GET_METHOD = "GET";
  protected static final String HTTP_POST_METHOD = "POST";
  protected static final String HTTP_PUT_METHOD = "PUT";

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String ACCEPT_HEADER = "Accept";
  protected static final String CONTENT_TYPE_HEADER = "Content-Type";

  protected static final String OCTET_STREAM_MEDIA_TYPE = "application/octet-stream";
  protected static final String JSON_MEDIA_TYPE = "application/json";

  private static final int DEFAULT_TIMEOUT_MS = 10_000;

  protected final String baseUrl;
  protected final String token;

  AbstractApiRequests(String baseUrl, String token) {
    this.baseUrl = baseUrl;
    this.token = token;
  }

  String urlEncode(String s) throws ApiCallIOException {
    try {
      return URLEncoder.encode(s, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      throw new ApiCallIOException(e);
    }
  }

  <T> T executeRequest(
      String method,
      String path,
      LambdaExceptionUtil.ConsumerWithExceptions<HttpURLConnection, EnterprisePluginException>
          beforeRequest,
      LambdaExceptionUtil.FunctionWithExceptions<HttpResponse, T, EnterprisePluginException>
          handleSuccessfulResponse,
      LambdaExceptionUtil.ConsumerWithExceptions<HttpResponse, EnterprisePluginException>
          validateResponse)
      throws EnterprisePluginException {
    final HttpURLConnection connection;
    try {
      connection = openConnection(method, path);

      beforeRequest.accept(connection);
      connection.connect();

      final HttpResponse response = readResponse(connection);

      validateResponse.accept(response);
      defaultValidateResponse(response);
      return handleSuccessfulResponse.apply(response);
    } catch (IOException e) {
      throw new ApiCallIOException(e);
    }
  }

  void get(
      String path,
      LambdaExceptionUtil.ConsumerWithExceptions<HttpResponse, EnterprisePluginException>
          validateResponse)
      throws EnterprisePluginException {
    executeRequest(HTTP_GET_METHOD, path, connection -> {}, response -> null, validateResponse);
  }

  <T> T getJson(
      String path,
      Class<T> valueType,
      LambdaExceptionUtil.ConsumerWithExceptions<HttpResponse, EnterprisePluginException>
          validateResponse)
      throws EnterprisePluginException {
    return executeRequest(
        HTTP_GET_METHOD,
        path,
        connection -> {},
        response -> readResponseJson(response, valueType),
        validateResponse);
  }

  <T> T getJson(String path, Class<T> valueType) throws EnterprisePluginException {
    return getJson(path, valueType, response -> {});
  }

  <T> T getJson(
      String path,
      TypeReference<T> valueTypeRef,
      LambdaExceptionUtil.ConsumerWithExceptions<HttpResponse, EnterprisePluginException>
          validateResponse)
      throws EnterprisePluginException {
    return executeRequest(
        HTTP_GET_METHOD,
        path,
        connection -> {},
        response -> readResponseJson(response, valueTypeRef),
        validateResponse);
  }

  <T> T getJson(String path, TypeReference<T> valueTypeRef) throws EnterprisePluginException {
    return getJson(path, valueTypeRef, response -> {});
  }

  <T> T postJson(
      String path,
      Object body,
      Class<T> valueType,
      LambdaExceptionUtil.ConsumerWithExceptions<HttpResponse, EnterprisePluginException>
          validateResponse)
      throws EnterprisePluginException {
    return executeRequest(
        HTTP_POST_METHOD,
        path,
        connection -> writeJsonBody(connection, body),
        response -> readResponseJson(response, valueType),
        validateResponse);
  }

  <T> T postJson(String path, Object body, Class<T> valueType) throws EnterprisePluginException {
    return postJson(path, body, valueType, response -> {});
  }

  <T> T putJson(
      String path,
      Object body,
      Class<T> valueType,
      LambdaExceptionUtil.ConsumerWithExceptions<HttpResponse, EnterprisePluginException>
          validateResponse)
      throws EnterprisePluginException {
    return executeRequest(
        HTTP_PUT_METHOD,
        path,
        connection -> writeJsonBody(connection, body),
        response -> readResponseJson(response, valueType),
        validateResponse);
  }

  <T> T putJson(String path, Object body, Class<T> valueType) throws EnterprisePluginException {
    return putJson(path, body, valueType, response -> {});
  }

  void defaultValidateResponse(HttpResponse response) throws EnterprisePluginException {
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

  <T> T readResponseJson(HttpResponse response, Class<T> valueType) {
    try {
      return JSON_MAPPER.readValue(response.body, valueType);
    } catch (JsonProcessingException e) {
      throw new JsonResponseProcessingException(e);
    }
  }

  <T> T readResponseJson(HttpResponse response, TypeReference<T> valueTypeRef) {
    try {
      return JSON_MAPPER.readValue(response.body, valueTypeRef);
    } catch (JsonProcessingException e) {
      throw new JsonResponseProcessingException(e);
    }
  }

  void writeJsonBody(HttpURLConnection connection, Object body) throws ApiCallIOException {
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

  private HttpURLConnection openConnection(String method, String path) throws IOException {
    final URL url;
    try {
      url = new URL(baseUrl + path);
    } catch (MalformedURLException e) {
      throw new IllegalStateException(e);
    }

    final URLConnection urlConnection = url.openConnection();
    if (urlConnection instanceof HttpURLConnection) {
      final HttpURLConnection connection = (HttpURLConnection) urlConnection;
      connection.setRequestMethod(method);
      connection.setRequestProperty(AUTHORIZATION_HEADER, token);
      connection.setRequestProperty(ACCEPT_HEADER, JSON_MEDIA_TYPE);
      connection.setConnectTimeout(DEFAULT_TIMEOUT_MS);
      connection.setReadTimeout(DEFAULT_TIMEOUT_MS);
      return connection;
    } else {
      throw new IllegalStateException(
          "Expected an HttpURLConnection, got " + urlConnection.getClass().getName());
    }
  }

  private HttpResponse readResponse(HttpURLConnection connection) throws IOException {
    int status = connection.getResponseCode();
    if (status < 0) {
      throw new IOException("Response could not be parsed as HTTP");
    }

    try(final InputStream is = status < 300 ? connection.getInputStream() : connection.getErrorStream()) {
      if(is == null) {
        return new HttpResponse(status, "");
      } else {
        return new HttpResponse(
            status, InputStreamUtils.inputStreamToString(is, StandardCharsets.UTF_8));
      }
    }
  }
}
