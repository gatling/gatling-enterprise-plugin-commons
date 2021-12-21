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

package io.gatling.plugin;

import static io.gatling.plugin.util.ObjectsUtil.nonEmptyParam;
import static io.gatling.plugin.util.ObjectsUtil.nonNullParam;

import io.gatling.plugin.client.EnterpriseClient;
import io.gatling.plugin.client.exceptions.*;
import io.gatling.plugin.model.*;
import io.gatling.plugin.model.Package;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class EnterprisePluginClient implements EnterprisePlugin {

  private final EnterpriseClient enterpriseClient;

  public EnterprisePluginClient(EnterpriseClient enterpriseClient) {
    this.enterpriseClient = enterpriseClient;
  }

  public void checkVersionSupport(String client, String version) throws EnterpriseClientException {
    nonEmptyParam(client, "client");
    nonEmptyParam(version, "version");
    enterpriseClient.checkVersionSupport(client, version);
  }

  public long uploadPackage(UUID packageId, File file) throws EnterpriseClientException {
    nonNullParam(packageId, "packageId");
    nonNullParam(file, "file");
    return enterpriseClient.uploadPackageWithChecksum(packageId, file);
  }

  /**
   * @param simulationId Required
   * @param systemProperties Required (can be an empty map)
   * @param file Path to the packaged JAR file to upload and run; required
   * @throws SimulationNotFoundException if the simulationId does not exist
   */
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

  /**
   * @param teamId Optional
   * @param groupId Optional
   * @param artifactId Required
   * @param className Required
   * @throws TeamConfigurationRequiredException if unable to choose a team (teamId should be
   *     specified)
   */
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

    final String packageName = groupId != null ? groupId + ":" + artifactId : artifactId;
    final String[] classNameParts = className.split("\\.");
    final String simulationName = classNameParts[classNameParts.length - 1];

    final List<Team> teams = enterpriseClient.getTeams();
    final Team team;
    if (teams.isEmpty()) {
      throw new UnsupportedOperationException(
          "Cannot create a simulation: no team was found and a simulation must belong to a team.");
    } else if (teamId != null) {
      team =
          teams.stream()
              .filter(t -> teamId.equals(t.id))
              .findFirst()
              .orElseThrow(() -> new TeamNotFoundException(teamId));
    } else if (teams.size() == 1) {
      team = teams.get(0);
    } else {
      throw new TeamConfigurationRequiredException(teams, "Cannot create a simulation");
    }

    final List<Pool> pools = enterpriseClient.getPools();
    final Pool pool;
    if (pools.size() > 0) {
      pool = pools.get(0);
    } else {
      // Should never happen on Gatling Enterprise Cloud
      throw new IllegalStateException(
          "Cannot automatically create a simulation if no pool is available");
    }

    final Package pkg = enterpriseClient.createPackage(packageName, team.id);
    enterpriseClient.uploadPackage(pkg.id, file);

    final Map<UUID, HostByPool> hostsByPool = new HashMap<>();
    hostsByPool.put(pool.id, new HostByPool(1, 0));

    final Simulation simulation =
        enterpriseClient.createSimulation(simulationName, team.id, className, pkg.id, hostsByPool);

    return new SimulationAndRunSummary(
        simulation, enterpriseClient.startSimulation(simulation.id, systemProperties));
  }
}
