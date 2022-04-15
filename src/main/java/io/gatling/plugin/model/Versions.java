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

public class Versions {

  public final VersionSupported java;

  @JsonCreator
  public Versions(@JsonProperty(value = "java", required = true) VersionSupported java) {
    this.java = java;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Versions versions = (Versions) o;
    return Objects.equals(java, versions.java);
  }

  @Override
  public int hashCode() {
    return Objects.hash(java);
  }

  @Override
  public String toString() {
    return "Versions{" + "java=" + java + '}';
  }
}
