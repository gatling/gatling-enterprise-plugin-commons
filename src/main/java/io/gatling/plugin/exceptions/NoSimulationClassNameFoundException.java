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

package io.gatling.plugin.exceptions;

public class NoSimulationClassNameFoundException extends EnterprisePluginException {
  public NoSimulationClassNameFoundException() {
    super(
        "No simulation class discovered. Your project should contain at least one class extending Simulation (https://gatling.io/docs/gatling/reference/current/core/simulation/).");
  }
}
