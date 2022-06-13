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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import io.gatling.plugin.exceptions.*;
import io.gatling.plugin.util.LambdaExceptionUtil.*;
import java.io.File;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;

public class HttpEnterpriseClientTest {

  private static final UUID ARTIFACT_ID = UUID.randomUUID();

  private final File ARTIFACT_FILE =
      new File(getClass().getResource("/artifacts/maven-sample.jar").getPath());

  private <T> T withMockWebServer(
      MockResponse response,
      BiFunctionWithExceptions<MockWebServer, HttpEnterpriseClient, T, Exception> testFunction)
      throws Exception {
    return withMockWebServer(Collections.singletonList(response), testFunction);
  }

  private <T> T withMockWebServer(
      List<MockResponse> responses,
      BiFunctionWithExceptions<MockWebServer, HttpEnterpriseClient, T, Exception> testFunction)
      throws Exception {
    MockResponse clientSupportResponse =
        new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK);
    try (MockWebServer server = new MockWebServer()) {
      // The HTTP client's first call will always be the version check:
      server.enqueue(clientSupportResponse);
      responses.forEach(server::enqueue);
      server.start();
      HttpEnterpriseClient client =
          new HttpEnterpriseClient(server.url("/").url(), "invalid", "client", "version");
      // Remove checkVersion enqueue request:
      server.takeRequest(1, TimeUnit.SECONDS);
      return testFunction.apply(server, client);
    }
  }

  @Test
  void UploadPackage_ContentType_OctetStream() throws Exception {
    withMockWebServer(
        new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK),
        (server, client) -> {
          client.uploadPackage(ARTIFACT_ID, ARTIFACT_FILE);
          assertEquals(
              "application/octet-stream",
              server.takeRequest(1, TimeUnit.SECONDS).getHeader("Content-Type"));
          return null;
        });
  }

  @Test
  void UploadPackage_ContentLength_MavenSampleLength() throws Exception {
    withMockWebServer(
        new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK),
        (server, client) -> {
          client.uploadPackage(ARTIFACT_ID, ARTIFACT_FILE);
          assertEquals(
              ARTIFACT_FILE.length(),
              Long.valueOf(server.takeRequest(1, TimeUnit.SECONDS).getHeader("Content-Length")));
          return null;
        });
  }

  private EnterprisePluginException UploadPackage_Status_EnterpriseClientException(int code)
      throws Exception {
    return withMockWebServer(
        new MockResponse().setResponseCode(code),
        (server, client) ->
            assertThrows(
                EnterprisePluginException.class,
                () -> client.uploadPackage(ARTIFACT_ID, ARTIFACT_FILE)));
  }

  @Test
  void UploadPackage_StatusNotFound_EnterpriseClientException() throws Exception {
    EnterprisePluginException e =
        UploadPackage_Status_EnterpriseClientException(HttpURLConnection.HTTP_NOT_FOUND);
    assertThat(e, instanceOf(PackageNotFoundException.class));
  }

  @Test
  void UploadPackage_StatusEntityTooLarge_EnterpriseClientException() throws Exception {
    EnterprisePluginException e =
        UploadPackage_Status_EnterpriseClientException(HttpURLConnection.HTTP_ENTITY_TOO_LARGE);
    assertThat(e, instanceOf(InvalidApiCallException.class));
    assertThat(e.getMessage(), containsString("Package exceeds maximum allowed size (5 GB)"));
  }

  @Test
  void UploadPackage_StatusUnauthorized_EnterpriseClientException() throws Exception {
    EnterprisePluginException e =
        UploadPackage_Status_EnterpriseClientException(HttpURLConnection.HTTP_UNAUTHORIZED);
    assertThat(e, instanceOf(UnauthorizedApiCallException.class));
  }

  @Test
  void UploadPackage_StatusUnknown_EnterpriseClientException() throws Exception {
    EnterprisePluginException e = UploadPackage_Status_EnterpriseClientException(666);
    assertThat(e, instanceOf(UnhandledApiCallException.class));
    assertThat(e.getMessage(), containsString("666"));
  }
}
