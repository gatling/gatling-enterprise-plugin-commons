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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class SimulationCreationPayload {

  public final String name;
  public final UUID teamId;
  public final String className;

  @JsonProperty(value = "build")
  @JsonSerialize(using = ArtifactIdJsonSerializer.class)
  public final UUID artifactId;

  public SimulationCreationPayload(String name, UUID teamId, String className, UUID artifactId) {
    Objects.requireNonNull(name, "Property 'name' is required");
    Objects.requireNonNull(teamId, "Property 'teamId' is required");
    Objects.requireNonNull(className, "Property 'className' is required");
    Objects.requireNonNull(artifactId, "Property 'artifactId' is required");
    this.name = name;
    this.teamId = teamId;
    this.className = className;
    this.artifactId = artifactId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SimulationCreationPayload that = (SimulationCreationPayload) o;
    return name.equals(that.name)
        && teamId.equals(that.teamId)
        && className.equals(that.className)
        && artifactId.equals(that.artifactId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, teamId, className, artifactId);
  }

  @Override
  public String toString() {
    return String.format(
        "Simulation{name='%s',teamId='%s',className='%s',artifactId='%s'}",
        name, teamId, className, artifactId);
  }

  private static final class ArtifactIdJsonSerializer extends JsonSerializer<UUID> {
    @Override
    public void serialize(UUID value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeStartObject();
      gen.writeStringField("artifactId", value.toString());
      gen.writeEndObject();
    }
  }
}