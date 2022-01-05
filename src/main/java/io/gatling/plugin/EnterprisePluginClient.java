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

import static io.gatling.plugin.util.ObjectsUtil.nonEmptyParam;
import static io.gatling.plugin.util.ObjectsUtil.nonNullParam;

import io.gatling.plugin.client.EnterpriseClient;
import io.gatling.plugin.client.exceptions.*;
import io.gatling.plugin.model.*;
import io.gatling.plugin.model.Pkg;
import java.io.File;
import java.util.*;

public final class EnterprisePluginClient extends PluginClient implements EnterprisePlugin {

  public EnterprisePluginClient(EnterpriseClient enterpriseClient) {
    super(enterpriseClient);
  }

  @Override
  public long uploadPackage(UUID packageId, File file)
      throws EnterpriseClientException {
    nonNullParam(file, "packageId");
    nonNullParam(file, "file");
    return enterpriseClient.uploadPackageWithChecksum(packageId, file);
  }

  @Override
  public long uploadPackageWithSimulationId(UUID simulationId, File file)
      throws EnterpriseClientException {
    nonNullParam(file, "simulationId");
    nonNullParam(file, "file");
    Simulation simulation = enterpriseClient.getSimulation(simulationId);
    return enterpriseClient.uploadPackageWithChecksum(simulation.pkgId, file);
  }

  @Override
  public SimulationAndRunSummary uploadPackageAndStartSimulation(
      UUID simulationId, Map<String, String> systemProperties, File file)
      throws EnterpriseClientException {
    nonNullParam(simulationId, "simulationId");
    nonNullParam(systemProperties, "systemProperties");
    nonNullParam(file, "file");

    final Simulation simulation = enterpriseClient.getSimulation(simulationId);
    enterpriseClient.uploadPackageWithChecksum(simulation.pkgId, file);
    return new SimulationAndRunSummary(
        simulation, enterpriseClient.startSimulation(simulationId, systemProperties));
  }

  private Team defaultTeam(UUID teamId) throws EnterpriseClientException {
    final List<Team> teams = enterpriseClient.getTeams();
    if (teams.isEmpty()) {
      throw new IllegalStateException(
          "Cannot create a simulation: no team was found and a simulation must belong to a team. In order to create a team, see https://gatling.io/docs/enterprise/cloud/reference/admin/teams/");
    } else if (teamId == null && teams.size() != 1) {
      throw new SeveralTeamsFoundException(
          teams,
          "Cannot configure a team: several teams were found, you must provide a configuration for a single team.");
    }

    if (teamId != null) {
      return teams.stream()
          .filter(t -> teamId.equals(t.id))
          .findFirst()
          .orElseThrow(() -> new TeamNotFoundException(teamId));
    } else {
      return teams.get(0);
    }
  }

  private Map<UUID, HostByPool> defaultHostByPool() throws EnterpriseClientException {
    final List<Pool> pools = enterpriseClient.getPools();
    if (pools.isEmpty()) {
      // Should never happen on Gatling Enterprise Cloud
      throw new IllegalStateException(
          "Cannot automatically create a simulation if no pool is available");
    }
    final Pool pool = pools.get(0);
    return Collections.singletonMap(pool.id, new HostByPool(1, 0));
  }

  private Pkg createAndUploadDefaultPackage(Team team, String groupId, String artifactId, File file)
      throws EnterpriseClientException {
    final String packageName = groupId != null ? groupId + ":" + artifactId : artifactId;
    final Pkg pkg = enterpriseClient.createPackage(packageName, team.id);
    enterpriseClient.uploadPackage(pkg.id, file);
    return pkg;
  }

  private SimulationAndRunSummary createAndStartSimulation(
      Team team,
      Pkg pkg,
      String className,
      Map<UUID, HostByPool> hostsByPool,
      Map<String, String> systemProperties)
      throws EnterpriseClientException {
    final String[] classNameParts = className.split("\\.");
    final String simulationName = classNameParts[classNameParts.length - 1];

    final Simulation simulation =
        enterpriseClient.createSimulation(simulationName, team.id, className, pkg.id, hostsByPool);
    final RunSummary runSummary = enterpriseClient.startSimulation(simulation.id, systemProperties);
    return new SimulationAndRunSummary(simulation, runSummary);
  }

  @Override
  public SimulationAndRunSummary createAndStartSimulation(
      UUID teamId,
      String groupId,
      String artifactId,
      String className,
      Map<String, String> systemProperties,
      File file)
      throws EnterpriseClientException {
    nonEmptyParam(artifactId, "artifactId");
    nonEmptyParam(className, "className");

    final Team team = defaultTeam(teamId);
    final Pkg pkg = createAndUploadDefaultPackage(team, groupId, artifactId, file);
    final Map<UUID, HostByPool> hostsByPool = defaultHostByPool();
    return createAndStartSimulation(team, pkg, className, hostsByPool, systemProperties);
  }
}
