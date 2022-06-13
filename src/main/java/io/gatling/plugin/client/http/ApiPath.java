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

import io.gatling.plugin.exceptions.ApiCallIOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class ApiPath {

  static ApiPath of(String... initialSegments) {
    return new ApiPath().append(initialSegments);
  }

  private final List<String> segments;
  private final Map<String, String> queryParams;

  private ApiPath() {
    this(Collections.emptyList(), Collections.emptyMap());
  }

  private ApiPath(List<String> segments, Map<String, String> queryParams) {
    this.segments = segments;
    this.queryParams = queryParams;
  }

  URL buildUrl(URL baseUrl) throws ApiCallIOException {
    // new URL(baseUrl, spec) has different corner cases, depending e.g. on whether or not the
    // baseUrl ends with a '/', etc. For our purpose it's easier to just build the new URL
    // ourselves.

    final String base = baseUrl.toString();
    final String path = encodePath(String.join("/", segments));
    final String queryString =
        queryParams.entrySet().stream()
            .map(
                entry ->
                    encodeQueryParam(entry.getKey()) + "=" + encodeQueryParam(entry.getValue()))
            .collect(Collectors.joining("&"));

    final StringBuilder builder = new StringBuilder(base);
    if (base.endsWith("/")) {
      builder.deleteCharAt(base.length() - 1);
    }
    if (!path.isEmpty()) {
      builder.append("/").append(path);
    }
    if (!queryString.isEmpty()) {
      builder.append("?").append(queryString);
    }

    try {
      return new URL(builder.toString());
    } catch (MalformedURLException e) {
      throw new ApiCallIOException(e);
    }
  }

  ApiPath append(String... newSegments) {
    final List<String> updatedSegments = new ArrayList<>(segments.size() + newSegments.length);
    updatedSegments.addAll(segments);
    updatedSegments.addAll(Arrays.asList(newSegments));
    return new ApiPath(Collections.unmodifiableList(updatedSegments), queryParams);
  }

  ApiPath addQueryParam(String key, String value) {
    final Map<String, String> newQueryParams = new HashMap<>(queryParams);
    newQueryParams.put(key, value);
    return new ApiPath(segments, Collections.unmodifiableMap(newQueryParams));
  }

  private String encodePath(String s) {
    try {
      // Don't use java.net.URLEncoder: it encodes for HTML forms, not for RFC2396 encoding, which
      // is slightly different (e.g. '+' rather than '%20' to represent a space, etc.).
      return new URI(null, null, s, null).toASCIIString();
    } catch (URISyntaxException e) {
      // Should not happen
      throw new RuntimeException(e);
    }
  }

  private String encodeQueryParam(String s) {
    try {
      return URLEncoder.encode(s, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      // Should not happen
      throw new RuntimeException(e);
    }
  }
}
