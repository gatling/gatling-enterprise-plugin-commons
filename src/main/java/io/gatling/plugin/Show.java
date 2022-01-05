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

package io.gatling.plugin;

import io.gatling.plugin.model.*;

public final class Show {

  static String simulation(Simulation simulation) {
    return String.format("Simulation '%s', id='%s'", simulation.name, simulation.id);
  }

  static String team(Team team) {
    return String.format("Team '%s', id='%s'", team.name, team.id);
  }

  static String pool(Pool pool) {
    return "Pool " + pool.name;
  }

  static String pkgIndex(PkgIndex pkg) {
    return String.format("Package '%s', id='%s'", pkg.name, pkg.id);
  }
}
