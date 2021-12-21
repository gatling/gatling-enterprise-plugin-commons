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

import java.util.Objects;

public class SimulationAndRunSummary {

  public final Simulation simulation;
  public final RunSummary runSummary;

  public SimulationAndRunSummary(Simulation simulation, RunSummary runSummary) {
    nonNullParam(simulation, "simulation");
    nonNullParam(runSummary, "runSummary");
    this.simulation = simulation;
    this.runSummary = runSummary;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SimulationAndRunSummary that = (SimulationAndRunSummary) o;
    return Objects.equals(simulation, that.simulation)
        && Objects.equals(runSummary, that.runSummary);
  }

  @Override
  public int hashCode() {
    return Objects.hash(simulation, runSummary);
  }

  @Override
  public String toString() {
    return String.format(
        "SimulationAndRunSummary{simulation=%s, runSummary=%s}", simulation, runSummary);
  }
}
