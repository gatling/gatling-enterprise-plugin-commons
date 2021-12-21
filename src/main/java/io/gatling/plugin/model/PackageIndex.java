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
import java.util.Objects;
import java.util.UUID;

public final class PackageIndex {
  public final UUID id;
  /** Optional. */
  public final UUID teamId;

  public final String name;
  /** Optional. */
  public final String fileName;

  @JsonCreator
  public PackageIndex(
      @JsonProperty(value = "id", required = true) UUID id,
      @JsonProperty(value = "teamId") UUID teamId,
      @JsonProperty(value = "name", required = true) String name,
      @JsonProperty(value = "fileName") String fileName) {
    nonNullParam(id, "id");
    nonNullParam(name, "name");
    this.id = id;
    this.teamId = teamId;
    this.name = name;
    this.fileName = fileName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PackageIndex aPackage = (PackageIndex) o;
    return id.equals(aPackage.id)
        && Objects.equals(teamId, aPackage.teamId)
        && name.equals(aPackage.name)
        && Objects.equals(fileName, aPackage.fileName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, teamId, name, fileName);
  }

  @Override
  public String toString() {
    return String.format(
        "Package{id='%s',teamId='%s',name='%s',fileName='%s'}", id, teamId, name, fileName);
  }
}
