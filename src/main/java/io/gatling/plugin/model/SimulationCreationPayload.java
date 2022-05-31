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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class SimulationCreationPayload {

  public final String name;
  public final UUID teamId;
  public final String className;

  @JsonProperty(value = "build")
  @JsonSerialize(using = PkgIdJsonSerializer.class)
  public final UUID pkgId;

  public final String jvmOptions;
  public final Map<String, String> systemProperties;
  public final Map<String, String> environmentVariables;
  public final boolean ignoreGlobalProperties;
  public final MeaningfulTimeWindow meaningfulTimeWindow;
  public final Map<UUID, HostByPool> hostsByPool;
  public final boolean usePoolWeights;
  public final boolean usePoolDedicatedIps;

  public SimulationCreationPayload(
      String name,
      UUID teamId,
      String className,
      UUID pkgId,
      String jvmOptions,
      Map<String, String> systemProperties,
      Map<String, String> environmentVariables,
      boolean ignoreGlobalProperties,
      MeaningfulTimeWindow meaningfulTimeWindow,
      Map<UUID, HostByPool> hostsByPool,
      boolean usePoolWeights,
      boolean usePoolDedicatedIps) {
    nonNullParam(name, "name");
    nonNullParam(teamId, "teamId");
    nonNullParam(className, "className");
    nonNullParam(pkgId, "pkgId");
    nonNullParam(systemProperties, "systemProperties");
    nonNullParam(environmentVariables, "environmentVariables");
    nonNullParam(meaningfulTimeWindow, "meaningfulTimeWindow");
    nonNullParam(hostsByPool, "hostsByPool");

    this.name = name;
    this.teamId = teamId;
    this.className = className;
    this.pkgId = pkgId;
    this.jvmOptions = jvmOptions;
    this.systemProperties = systemProperties;
    this.environmentVariables = environmentVariables;
    this.ignoreGlobalProperties = ignoreGlobalProperties;
    this.meaningfulTimeWindow = meaningfulTimeWindow;
    this.hostsByPool = hostsByPool;
    this.usePoolWeights = usePoolWeights;
    this.usePoolDedicatedIps = usePoolDedicatedIps;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SimulationCreationPayload that = (SimulationCreationPayload) o;
    return ignoreGlobalProperties == that.ignoreGlobalProperties
        && usePoolWeights == that.usePoolWeights
        && usePoolDedicatedIps == that.usePoolDedicatedIps
        && name.equals(that.name)
        && teamId.equals(that.teamId)
        && className.equals(that.className)
        && pkgId.equals(that.pkgId)
        && Objects.equals(jvmOptions, that.jvmOptions)
        && systemProperties.equals(that.systemProperties)
        && environmentVariables.equals(that.environmentVariables)
        && meaningfulTimeWindow.equals(that.meaningfulTimeWindow)
        && hostsByPool.equals(that.hostsByPool);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        name,
        teamId,
        className,
        pkgId,
        jvmOptions,
        systemProperties,
        environmentVariables,
        ignoreGlobalProperties,
        meaningfulTimeWindow,
        hostsByPool,
        usePoolWeights,
        usePoolDedicatedIps);
  }

  @Override
  public String toString() {
    return String.format(
        "Simulation{name='%s',teamId='%s',className='%s',pkgId='%s'}",
        name, teamId, className, pkgId);
  }

  private static final class PkgIdJsonSerializer extends JsonSerializer<UUID> {
    @Override
    public void serialize(UUID value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeStartObject();
      gen.writeStringField("pkgId", value.toString());
      gen.writeEndObject();
    }
  }
}
