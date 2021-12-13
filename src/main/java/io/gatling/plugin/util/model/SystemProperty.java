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

package io.gatling.plugin.util.model;

import java.util.Objects;

import static io.gatling.plugin.util.ObjectsUtil.nonNullParam;

public final class SystemProperty {
  public final String key;
  public final String value;

  public SystemProperty(String key, String value) {
    nonNullParam(key, "key");
    nonNullParam(value, "value");
    this.key = key;
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SystemProperty that = (SystemProperty) o;
    return key.equals(that.key) && value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, value);
  }

  @Override
  public String toString() {
    return String.format("SystemProperty{key='%s',value='%s'}", key, value);
  }
}
