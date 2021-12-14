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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class Simulation {

  public final UUID id;
  public final String name;
  public final UUID teamId;
  public final String className;
  public final UUID pkgId;

  public Simulation(UUID id, String name, UUID teamId, String className, UUID pkgId) {
    Objects.requireNonNull(id, "Property 'id' is required");
    Objects.requireNonNull(name, "Property 'name' is required");
    Objects.requireNonNull(teamId, "Property 'teamId' is required");
    Objects.requireNonNull(className, "Property 'className' is required");
    Objects.requireNonNull(pkgId, "Property 'pkgId' is required");
    this.id = id;
    this.name = name;
    this.teamId = teamId;
    this.className = className;
    this.pkgId = pkgId;
  }

  @JsonCreator
  public Simulation(
      @JsonProperty(value = "id", required = true) UUID id,
      @JsonProperty(value = "name", required = true) String name,
      @JsonProperty(value = "teamId", required = true) UUID teamId,
      @JsonProperty(value = "className", required = true) String className,
      @JsonProperty(value = "build", required = true) Map<String, Object> build) {
    this(id, name, teamId, className, UUID.fromString((String) build.get("pkgId")));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Simulation that = (Simulation) o;
    return id.equals(that.id)
        && name.equals(that.name)
        && teamId.equals(that.teamId)
        && className.equals(that.className)
        && pkgId.equals(that.pkgId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, teamId, className, pkgId);
  }

  @Override
  public String toString() {
    return String.format(
        "Simulation{id='%s',name='%s',teamId='%s',className='%s',pkgId='%s'}",
        id, name, teamId, className, pkgId);
  }
}
