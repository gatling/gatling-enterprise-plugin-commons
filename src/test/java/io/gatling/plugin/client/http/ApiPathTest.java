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

import static org.junit.jupiter.api.Assertions.*;

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.Test;

public class ApiPathTest {

  private static final URL BASE_URL;
  private static final URL BASE_URL_SLASH;

  static {
    try {
      BASE_URL = new URL("https://example.com/base/path");
      BASE_URL_SLASH = new URL("https://example.com/base/path/");
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void emptyPath() throws Exception {
    ApiPath path = ApiPath.of();
    assertEquals(new URL("https://example.com/base/path"), path.buildUrl(BASE_URL));
  }

  @Test
  void emptyPathWithSlash() throws Exception {
    ApiPath path = ApiPath.of();
    assertEquals(new URL("https://example.com/base/path"), path.buildUrl(BASE_URL_SLASH));
  }

  @Test
  void simplePath() throws Exception {
    ApiPath path = ApiPath.of("foo");
    assertEquals(new URL("https://example.com/base/path/foo"), path.buildUrl(BASE_URL));
  }

  @Test
  void simplePathWithSlash() throws Exception {
    ApiPath path = ApiPath.of("foo");
    assertEquals(new URL("https://example.com/base/path/foo"), path.buildUrl(BASE_URL_SLASH));
  }

  @Test
  void multipleAppends() throws Exception {
    ApiPath path = ApiPath.of("foo1", "foo2", "foo3").append("bar").append("baz1", "baz2");
    assertEquals(
        new URL("https://example.com/base/path/foo1/foo2/foo3/bar/baz1/baz2"),
        path.buildUrl(BASE_URL));
  }

  @Test
  void oneQueryParam() throws Exception {
    ApiPath path = ApiPath.of().addQueryParam("key1", "value1");
    assertEquals(new URL("https://example.com/base/path?key1=value1"), path.buildUrl(BASE_URL));
  }

  @Test
  void multipleQueryParams() throws Exception {
    ApiPath path =
        ApiPath.of()
            .addQueryParam("key1", "value1")
            .addQueryParam("key2", "value2")
            .addQueryParam("key3", "value3");
    assertEquals(
        new URL("https://example.com/base/path?key1=value1&key2=value2&key3=value3"),
        path.buildUrl(BASE_URL));
  }

  @Test
  void pathAndQueryParams() throws Exception {
    ApiPath path =
        ApiPath.of("foo", "bar").addQueryParam("key1", "value1").addQueryParam("key2", "value2");
    assertEquals(
        new URL("https://example.com/base/path/foo/bar?key1=value1&key2=value2"),
        path.buildUrl(BASE_URL));
  }

  @Test
  void encoding() throws Exception {
    ApiPath path = ApiPath.of("=&? £").append("éḀ").addQueryParam("clé1", "=&? £éḀ\n//");
    assertEquals(
        new URL(
            "https://example.com/base/path/=&%3F%20%C2%A3/%C3%A9%E1%B8%80?cl%C3%A91=%3D%26%3F+%C2%A3%C3%A9%E1%B8%80%0A%2F%2F"),
        path.buildUrl(BASE_URL));
  }
}
