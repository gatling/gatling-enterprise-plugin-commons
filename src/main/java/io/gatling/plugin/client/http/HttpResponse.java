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

import java.util.Objects;

public final class HttpResponse {

  public final int code;
  public final String body;

  public HttpResponse(int code, String body) {
    this.code = code;
    this.body = body;
  }

  public boolean isSuccessful() {
    return code >= 200 && code < 300;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    HttpResponse that = (HttpResponse) o;
    return code == that.code && Objects.equals(body, that.body);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, body);
  }

  @Override
  public String toString() {
    return String.format("HttpResponse{status='%s',body='%s'}", code, body);
  }
}
