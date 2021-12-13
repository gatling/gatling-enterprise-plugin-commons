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
import java.util.Objects;
import java.util.UUID;

public final class RunSummary {
  public final UUID runId;
  public final String className;
  public final String reportsUrl;

  @JsonCreator
  public RunSummary(
      @JsonProperty(value = "runId", required = true) UUID runId,
      @JsonProperty(value = "className", required = true) String className,
      @JsonProperty(value = "reportsUrl", required = true) String reportsUrl) {
    Objects.requireNonNull(runId, "Property 'runId' is required");
    Objects.requireNonNull(className, "Property 'className' is required");
    Objects.requireNonNull(reportsUrl, "Property 'reportsUrl' is required");
    this.runId = runId;
    this.className = className;
    this.reportsUrl = reportsUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RunSummary that = (RunSummary) o;
    return runId.equals(that.runId)
        && className.equals(that.className)
        && reportsUrl.equals(that.reportsUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(runId, className, reportsUrl);
  }

  @Override
  public String toString() {
    return String.format(
        "RunSummary{runId='%s',className='%s',reportsUrl='%s'}", runId, className, reportsUrl);
  }
}
