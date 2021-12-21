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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public final class PackageFile {

  public final String filename;

  public final String version;

  public final String checksum;

  public PackageFile(
      @JsonProperty(value = "filename") String filename,
      @JsonProperty(value = "version") String version,
      @JsonProperty(value = "checksum") String checksum) {
    this.filename = filename;
    this.version = version;
    this.checksum = checksum;
  }

  @Override
  public String toString() {
    return String.format(
        "PackageFile{filename='%s',version='%s',checksum='%s'}", filename, version, checksum);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PackageFile that = (PackageFile) o;
    return Objects.equals(filename, that.filename)
        && Objects.equals(version, that.version)
        && Objects.equals(checksum, that.checksum);
  }

  @Override
  public int hashCode() {
    return Objects.hash(filename, version, checksum);
  }
}
