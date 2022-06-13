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
import java.io.File;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.UUID;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;

public class HttpEnterpriseClientTest {

  private static final UUID ARTIFACT_ID = UUID.randomUUID();

  private final File ARTIFACT_FILE =
      new File(getClass().getResource("/artifacts/maven-sample.jar").getPath());

  private MockWebServer createMockWebServer(MockResponse... responses) {
    MockWebServer server = new MockWebServer();
    MockResponse clientSupportResponse =
        new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK);
    server.enqueue(
        clientSupportResponse); // okHttpEnterpriseClientMockWebServer getInstance checkVersion call
    Arrays.stream(responses).forEach(server::enqueue);
    return server;
  }

  private HttpEnterpriseClient createHttpEnterpriseClient(MockWebServer server) {
    try {
      HttpEnterpriseClient client =
          new HttpEnterpriseClient(server.url("/").url(), "invalid", "client", "version");
      server.takeRequest(); // Remove checkVersion enqueue request
      return client;
    } catch (EnterprisePluginException | InterruptedException e) {
      throw new IllegalStateException(
          "Test version support should always work from enqueue response", e);
    }
  }

  @Test
  void UploadPackage_ContentType_OctetStream()
      throws EnterprisePluginException, InterruptedException {
    MockWebServer server =
        createMockWebServer(new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK));
    HttpEnterpriseClient client = createHttpEnterpriseClient(server);
    client.uploadPackage(ARTIFACT_ID, ARTIFACT_FILE);
    assertEquals("application/octet-stream", server.takeRequest().getHeader("Content-Type"));
  }

  @Test
  void UploadPackage_ContentLength_MavenSampleLength()
      throws EnterprisePluginException, InterruptedException {
    MockWebServer server =
        createMockWebServer(new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK));
    HttpEnterpriseClient client = createHttpEnterpriseClient(server);
    client.uploadPackage(ARTIFACT_ID, ARTIFACT_FILE);
    assertEquals(
        ARTIFACT_FILE.length(), Long.valueOf(server.takeRequest().getHeader("Content-Length")));
  }

  private EnterprisePluginException UploadPackage_Status_EnterpriseClientException(int code) {
    MockWebServer server = createMockWebServer(new MockResponse().setResponseCode(code));
    HttpEnterpriseClient client = createHttpEnterpriseClient(server);
    return assertThrows(
        EnterprisePluginException.class, () -> client.uploadPackage(ARTIFACT_ID, ARTIFACT_FILE));
  }

  @Test
  void UploadPackage_StatusNotFound_EnterpriseClientException() {
    EnterprisePluginException e =
        UploadPackage_Status_EnterpriseClientException(HttpURLConnection.HTTP_NOT_FOUND);
    assertThat(e, instanceOf(PackageNotFoundException.class));
  }

  @Test
  void UploadPackage_StatusEntityTooLarge_EnterpriseClientException() {
    EnterprisePluginException e =
        UploadPackage_Status_EnterpriseClientException(HttpURLConnection.HTTP_ENTITY_TOO_LARGE);
    assertThat(e, instanceOf(InvalidApiCallException.class));
    assertThat(e.getMessage(), containsString("Package exceeds maximum allowed size (5 GB)"));
  }

  @Test
  void UploadPackage_StatusUnauthorized_EnterpriseClientException() {
    EnterprisePluginException e =
        UploadPackage_Status_EnterpriseClientException(HttpURLConnection.HTTP_UNAUTHORIZED);
    assertThat(e, instanceOf(UnauthorizedApiCallException.class));
  }

  @Test
  void UploadPackage_StatusUnknown_EnterpriseClientException() {
    EnterprisePluginException e = UploadPackage_Status_EnterpriseClientException(666);
    assertThat(e, instanceOf(UnhandledApiCallException.class));
    assertThat(e.getMessage(), containsString("666"));
  }
}
