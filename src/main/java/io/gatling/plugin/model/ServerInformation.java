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

package io.gatling.plugin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class ServerInformation {

  public final Versions versions;

  @JsonCreator
  public ServerInformation(@JsonProperty(value = "versions", required = true) Versions versions) {
    this.versions = versions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ServerInformation that = (ServerInformation) o;
    return Objects.equals(versions, that.versions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(versions);
  }

  @Override
  public String toString() {
    return "ServerInformation{" + "versions=" + versions + '}';
  }
}
