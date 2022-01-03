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

package io.gatling.plugin.util;

import static io.gatling.plugin.util.JsonUtil.JSON_MAPPER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.gatling.plugin.util.exceptions.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

abstract class AbstractApiRequests {

  private static final String AUTHORIZATION_HEADER = "Authorization";

  protected static final MediaType OCTET_STREAM_MEDIA_TYPE =
      MediaType.get("application/octet-stream");
  protected static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json");

  protected final OkHttpClient okHttpClient;
  protected final HttpUrl url;
  protected final String token;

  AbstractApiRequests(OkHttpClient okHttpClient, HttpUrl url, String token) {
    this.okHttpClient = okHttpClient;
    this.url = url;
    this.token = token;
  }

  <T> T executeRequest(
      Request.Builder unauthenticatedRequest,
      LambdaExceptionUtil.FunctionWithExceptions<Response, T, EnterpriseClientException>
          handleSuccessfulResponse,
      LambdaExceptionUtil.ConsumerWithExceptions<Response, EnterpriseClientException>
          validateResponse)
      throws EnterpriseClientException {
    Request request = unauthenticatedRequest.header(AUTHORIZATION_HEADER, token).build();
    try (Response response = okHttpClient.newCall(request).execute()) {
      validateResponse.accept(response);
      defaultValidateResponse(response);
      return handleSuccessfulResponse.apply(response);
    } catch (IOException e) {
      throw new ApiCallIOException(e);
    }
  }

  <T> T executeRequest(
      Request.Builder unauthenticatedRequest,
      LambdaExceptionUtil.FunctionWithExceptions<Response, T, EnterpriseClientException>
          handleSuccessfulResponse)
      throws EnterpriseClientException {
    return executeRequest(unauthenticatedRequest, handleSuccessfulResponse, response -> {});
  }

  void defaultValidateResponse(Response response) throws EnterpriseClientException {
    if (!response.isSuccessful()) {
      switch (response.code()) {
        case HttpURLConnection.HTTP_UNAUTHORIZED:
          throw new UnauthorizedApiCallException();
        case HttpURLConnection.HTTP_FORBIDDEN:
          throw new ForbiddenApiCallException();
        case HttpURLConnection.HTTP_BAD_REQUEST:
          throw new InvalidApiCallException(readResponseBody(response));
        default:
          throw new UnhandledApiCallException(response.code(), readResponseBody(response));
      }
    }
  }

  String readResponseBody(Response response) throws EnterpriseClientException {
    try (ResponseBody body = response.body()) {
      return body.string();
    } catch (IOException e) {
      throw new JsonResponseProcessingException(e);
    }
  }

  <T> T readResponseJson(Response response, Class<T> valueType) throws EnterpriseClientException {
    try {
      return JSON_MAPPER.readValue(readResponseBody(response), valueType);
    } catch (JsonProcessingException e) {
      throw new JsonResponseProcessingException(e);
    }
  }

  <T> T readResponseJson(Response response, TypeReference<T> valueTypeRef)
      throws EnterpriseClientException {
    try {
      return JSON_MAPPER.readValue(readResponseBody(response), valueTypeRef);
    } catch (JsonProcessingException e) {
      throw new JsonResponseProcessingException(e);
    }
  }

  RequestBody jsonRequestBody(Object obj) {
    final String json;
    try {
      json = JSON_MAPPER.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new JsonRequestProcessingException(e);
    }
    return RequestBody.create(JSON_MEDIA_TYPE, json);
  }
}
