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

package io.gatling.plugin.client.exceptions;

import io.gatling.plugin.model.Team;
import java.util.List;

public final class TeamConfigurationRequiredException extends EnterpriseClientException {

  private final List<Team> availableTeams;

  public TeamConfigurationRequiredException(List<Team> availableTeams, String contextMsg) {
    super(
        contextMsg
            + ": several teams were found, you must provide a configuration for a single team.");
    this.availableTeams = availableTeams;
  }

  public List<Team> getAvailableTeams() {
    return availableTeams;
  }
}
