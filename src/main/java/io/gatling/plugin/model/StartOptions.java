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

import static io.gatling.plugin.util.ObjectsUtil.nonNullParam;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;

public class StartOptions {

  public final Map<String, String> extraSystemProperties;

  @JsonCreator
  public StartOptions(
      @JsonProperty(value = "extraSystemProperties", required = true)
          Map<String, String> extraSystemProperties) {
    nonNullParam(extraSystemProperties, "extraSystemProperties");
    this.extraSystemProperties = extraSystemProperties;
  }

  public Map<String, String> getExtraSystemProperties() {
    return extraSystemProperties;
  }

  @Override
  public String toString() {
    return "StartOptions{" + "extraSystemProperties=" + extraSystemProperties + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StartOptions that = (StartOptions) o;
    return Objects.equals(extraSystemProperties, that.extraSystemProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(extraSystemProperties);
  }
}