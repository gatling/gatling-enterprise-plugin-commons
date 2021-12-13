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
import java.util.UUID;

import static io.gatling.plugin.util.ObjectsUtil.nonNullParam;

public final class PackageCreationPayload {
  public final String name;
  /** Optional. */
  public final UUID teamId;

  public PackageCreationPayload(String name, UUID teamId) {
    nonNullParam(name, "name");
    this.name = name;
    this.teamId = teamId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PackageCreationPayload that = (PackageCreationPayload) o;
    return name.equals(that.name) && Objects.equals(teamId, that.teamId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, teamId);
  }

  @Override
  public String toString() {
    return String.format("PackageCreationPayload{name='%s',teamId='%s'}", name, teamId);
  }
}
