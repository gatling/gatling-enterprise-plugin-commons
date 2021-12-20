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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.gatling.plugin.util.exceptions.EnterpriseClientException;
import io.gatling.plugin.util.exceptions.InvalidApiCallException;
import io.gatling.plugin.util.exceptions.PackageNotFoundException;
import io.gatling.plugin.util.exceptions.UnauthorizedApiCallException;
import io.gatling.plugin.util.exceptions.UnhandledApiCallException;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.UUID;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;

public class OkHttpEnterpriseClientTest {

  private final String TOKEN = "invalid";
  private final URL CLOUD_URL = new URL("https://cloud.gatling.io/api/public");
  private final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();
  private final UUID ARTIFACT_ID = UUID.randomUUID();

  private final File ARTIFACT_FILE =
      new File(getClass().getResource("/artifacts/maven-sample.jar").getPath());

  public OkHttpEnterpriseClientTest() throws MalformedURLException {}

  private MockWebServer mockWebServer(MockResponse... responses) {
    MockWebServer server = new MockWebServer();
    Arrays.stream(responses).forEach(server::enqueue);
    return server;
  }

  private OkHttpEnterpriseClient okHttpEnterpriseClientMockWebServer(MockWebServer server) {
    return new OkHttpEnterpriseClient(OK_HTTP_CLIENT, server.url("/").url(), TOKEN);
  }

  @Test
  void UploadPackage_ContentType_OctetStream()
      throws EnterpriseClientException, InterruptedException {
    MockWebServer server =
        mockWebServer(
            new MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND), // checksum package response
            new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK));
    OkHttpEnterpriseClient client = okHttpEnterpriseClientMockWebServer(server);
    client.uploadPackage(ARTIFACT_ID, ARTIFACT_FILE);
    assertEquals(2, server.getRequestCount());
    assertEquals("GET", server.takeRequest().getMethod());
    assertEquals("application/octet-stream", server.takeRequest().getHeader("Content-Type"));
  }

  @Test
  void UploadPackage_ContentLength_MavenSampleLength()
      throws EnterpriseClientException, InterruptedException {
    MockWebServer server =
        mockWebServer(
            new MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND), // checksum package response
            new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK));
    OkHttpEnterpriseClient client = okHttpEnterpriseClientMockWebServer(server);
    client.uploadPackage(ARTIFACT_ID, ARTIFACT_FILE);
    assertEquals(2, server.getRequestCount());
    assertEquals("GET", server.takeRequest().getMethod());
    assertEquals(
        ARTIFACT_FILE.length(), Long.valueOf(server.takeRequest().getHeader("Content-Length")));
  }

  private EnterpriseClientException UploadPackage_Status_EnterpriseClientException(int code) {
    MockWebServer server =
        mockWebServer(
            new MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND), // checksum package response
            new MockResponse().setResponseCode(code));
    OkHttpEnterpriseClient client = okHttpEnterpriseClientMockWebServer(server);
    return assertThrows(
        EnterpriseClientException.class, () -> client.uploadPackage(ARTIFACT_ID, ARTIFACT_FILE));
  }

  @Test
  void UploadPackage_StatusNotFound_EnterpriseClientException() {
    EnterpriseClientException e =
        UploadPackage_Status_EnterpriseClientException(HttpURLConnection.HTTP_NOT_FOUND);
    assertTrue(e instanceof PackageNotFoundException);
  }

  @Test
  void UploadPackage_StatusEntityTooLarge_EnterpriseClientException() {
    EnterpriseClientException e =
        UploadPackage_Status_EnterpriseClientException(HttpURLConnection.HTTP_ENTITY_TOO_LARGE);
    assertTrue(e instanceof InvalidApiCallException);
    assertTrue(e.getMessage().contains("Package exceeds maximum allowed size (5 GB)"));
  }

  @Test
  void UploadPackage_StatusUnauthorized_EnterpriseClientException() {
    EnterpriseClientException e =
        UploadPackage_Status_EnterpriseClientException(HttpURLConnection.HTTP_UNAUTHORIZED);
    assertTrue(e instanceof UnauthorizedApiCallException);
  }

  @Test
  void UploadPackage_StatusUnknown_EnterpriseClientException() {
    EnterpriseClientException e = UploadPackage_Status_EnterpriseClientException(666);
    assertTrue(e instanceof UnhandledApiCallException);
    assertTrue(e.getMessage().contains("666"));
  }
}
