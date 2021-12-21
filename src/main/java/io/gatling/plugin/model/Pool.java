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

package io.gatling.plugin.model;

import static io.gatling.plugin.util.ObjectsUtil.nonNullParam;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.gatling.plugin.util.show.Showable;
import java.util.Objects;
import java.util.UUID;

public final class Pool implements Showable {
  public final UUID id;
  public final String name;

  @JsonCreator
  public Pool(
      @JsonProperty(value = "id", required = true) UUID id,
      @JsonProperty(value = "name", required = true) String name) {
    nonNullParam(id, "id");
    nonNullParam(name, "name");
    this.id = id;
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Pool pool = (Pool) o;
    return id.equals(pool.id) && name.equals(pool.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name);
  }

  @Override
  public String toString() {
    return String.format("Pool{id='%s',name='%s'}", id, name);
  }

  @Override
  public String show() {
    return "Pool " + name;
  }
}
