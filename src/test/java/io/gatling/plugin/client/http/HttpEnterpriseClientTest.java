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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.plugin.exceptions.*;
import io.gatling.plugin.model.Pkg;
import io.gatling.plugin.model.PkgIndex;
import io.gatling.plugin.model.ServerInformation;
import io.gatling.plugin.model.VersionSupported;
import io.gatling.plugin.model.Versions;
import io.gatling.plugin.util.LambdaExceptionUtil.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

public class HttpEnterpriseClientTest {

  private static final UUID ARTIFACT_ID = UUID.randomUUID();
  private static final String AUTH_TOKEN = "test-auth-token";

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
          new HttpEnterpriseClient(server.url("/").url(), AUTH_TOKEN, "client", "version");
      // Remove checkVersion enqueue request:
      server.takeRequest(1, TimeUnit.SECONDS);
      return testFunction.apply(server, client);
    }
  }

  @Test
  void getServerInformationOk() throws Exception {
    final String responseBody = loadJson("/api/responses/serverInformation.json");
    final ServerInformation expectedResponse =
        new ServerInformation(new Versions(new VersionSupported("8", "17")));
    withMockWebServer(
        new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(responseBody),
        (server, client) -> {
          final ServerInformation response = client.getServerInformation();
          final RecordedRequest record = server.takeRequest(1, TimeUnit.SECONDS);
          assertEquals("/info", record.getPath());
          assertEquals(AUTH_TOKEN, record.getHeader("Authorization"));
          assertEquals(expectedResponse, response);
          return null;
        });
  }

  @Test
  void getPackagesOk() throws Exception {
    final String responseBody = loadJson("/api/responses/getPackages.json");
    final List<PkgIndex> expectedResponse =
        Arrays.asList(
            new PkgIndex(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                UUID.fromString("00000000-0000-0000-0000-100000000000"),
                "name 1",
                "filename1.jar"),
            new PkgIndex(
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                null,
                "name 2",
                "filename2.jar"),
            new PkgIndex(
                UUID.fromString("00000000-0000-0000-0000-000000000003"),
                UUID.fromString("00000000-0000-0000-0000-200000000000"),
                "name 3",
                null));
    withMockWebServer(
        new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(responseBody),
        (server, client) -> {
          final List<PkgIndex> response = client.getPackages();
          final RecordedRequest record = server.takeRequest(1, TimeUnit.SECONDS);
          assertEquals("/artifacts", record.getPath());
          assertEquals(AUTH_TOKEN, record.getHeader("Authorization"));
          assertEquals(expectedResponse, response);
          return null;
        });
  }

  @Test
  void createPackageOk() throws Exception {
    final String responseBody = loadJson("/api/responses/createPackage.json");
    final Pkg expectedResponse =
        new Pkg(
            UUID.fromString("00000000-0000-0000-0000-000000000000"),
            UUID.fromString("00000000-0000-0000-0000-200000000000"),
            "test package name",
            null);
    final String expectedRequest = loadJson("/api/requests/createPackage.json");
    withMockWebServer(
        new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(responseBody),
        (server, client) -> {
          final Pkg response =
              client.createPackage(
                  "test package name", UUID.fromString("00000000-0000-0000-0000-200000000000"));
          final RecordedRequest record = server.takeRequest(1, TimeUnit.SECONDS);
          assertEquals("/artifacts", record.getPath());
          assertEquals(AUTH_TOKEN, record.getHeader("Authorization"));
          assertJsonEquals(expectedRequest, record.getBody().readUtf8());
          assertEquals(expectedResponse, response);
          return null;
        });
  }

  @Test
  void uploadPackageOk() throws Exception {
    withMockWebServer(
        new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK),
        (server, client) -> {
          client.uploadPackage(ARTIFACT_ID, ARTIFACT_FILE);
          final RecordedRequest record = server.takeRequest(1, TimeUnit.SECONDS);
          assertEquals("application/octet-stream", record.getHeader("Content-Type"));
          assertEquals(ARTIFACT_FILE.length(), Long.valueOf(record.getHeader("Content-Length")));
          assertEquals(ARTIFACT_FILE.length(), record.getBodySize());
          assertEquals(
              "/artifacts/" + ARTIFACT_ID + "/content?filename=" + ARTIFACT_FILE.getName(),
              record.getPath());
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

  private String loadJson(String resourcePath) throws IOException {
    try (InputStream is = getClass().getResourceAsStream(resourcePath);
        Reader ir = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(ir)) {
      return br.lines().collect(Collectors.joining("\n"));
    }
  }

  private void assertJsonEquals(String s1, String s2) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    assertEquals(mapper.readTree(s1), mapper.readTree(s2));
  }
}
