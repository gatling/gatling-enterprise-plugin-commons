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

import static io.gatling.plugin.EnterpriseSimulationScanner.simulationFullyQualifiedNamesFromFile;
import static io.gatling.plugin.util.ObjectsUtil.nonEmptyParam;
import static io.gatling.plugin.util.ObjectsUtil.nonNullParam;

import io.gatling.plugin.client.EnterpriseClient;
import io.gatling.plugin.exceptions.*;
import io.gatling.plugin.io.PluginLogger;
import io.gatling.plugin.model.*;
import io.gatling.plugin.model.Pkg;
import java.io.File;
import java.util.*;

public final class BatchEnterprisePluginClient extends PluginClient
    implements BatchEnterprisePlugin {

  public BatchEnterprisePluginClient(EnterpriseClient enterpriseClient, PluginLogger logger) {
    super(enterpriseClient, logger);
  }

  @Override
  public long uploadPackage(UUID packageId, File file) throws EnterprisePluginException {
    nonNullParam(packageId, "packageId");
    nonNullParam(file, "file");
    return uploadPackageWithChecksum(packageId, file);
  }

  @Override
  public long uploadPackageWithSimulationId(UUID simulationId, File file)
      throws EnterprisePluginException {
    nonNullParam(file, "simulationId");
    nonNullParam(file, "file");
    Simulation simulation = enterpriseClient.getSimulation(simulationId);
    return enterpriseClient.uploadPackageWithChecksum(simulation.pkgId, file);
  }

  @Override
  public SimulationStartResult uploadPackageAndStartSimulation(
      UUID simulationId, Map<String, String> systemProperties, String simulationClass, File file)
      throws EnterprisePluginException {
    nonNullParam(simulationId, "simulationId");
    nonNullParam(systemProperties, "systemProperties");
    nonNullParam(file, "file");

    final Simulation simulation = enterpriseClient.getSimulation(simulationId);
    uploadPackageWithChecksum(simulation.pkgId, file);

    String className = simulationClassName(simulation, file, simulationClass);

    if (!simulation.className.equals(className)) {
      logger.info(
          String.format(
              "The Simulation configured in Gatling Enterprise was using the class %s. Updating to %s.",
              simulation.className, className));
      enterpriseClient.updateSimulationClassName(simulation.id, className);
    }

    final RunSummary runSummary = enterpriseClient.startSimulation(simulationId, systemProperties);
    return new SimulationStartResult(simulation, runSummary, false);
  }

  @Override
  public SimulationStartResult createAndStartSimulation(
      UUID teamId,
      String groupId,
      String artifactId,
      String simulationClass,
      UUID packageId,
      Map<String, String> systemProperties,
      File file)
      throws EnterprisePluginException {
    nonEmptyParam(artifactId, "artifactId");

    final String className = simulationClassName(null, file, simulationClass);
    final Team team = defaultTeam(teamId);
    final Pkg pkg =
        packageId != null
            ? enterpriseClient.getPackage(packageId)
            : createAndUploadDefaultPackage(team, groupId, artifactId, file);
    final Map<UUID, HostByPool> hostsByPool = defaultHostByPool();
    return createAndStartSimulation(team, pkg, className, hostsByPool, systemProperties);
  }

  private Team defaultTeam(UUID teamId) throws EnterprisePluginException {
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

  private Pkg createAndUploadDefaultPackage(Team team, String groupId, String artifactId, File file)
      throws EnterprisePluginException {
    final String packageName = groupId != null ? groupId + ":" + artifactId : artifactId;
    final Pkg pkg = enterpriseClient.createPackage(packageName, team.id);
    enterpriseClient.uploadPackage(pkg.id, file);
    return pkg;
  }

  private Map<UUID, HostByPool> defaultHostByPool() throws EnterprisePluginException {
    final List<Pool> pools = enterpriseClient.getPools();
    if (pools.isEmpty()) {
      // Should never happen on Gatling Enterprise Cloud
      throw new IllegalStateException(
          "Cannot automatically create a simulation if no pool is available");
    }
    final Pool pool = pools.get(0);
    return Collections.singletonMap(pool.id, new HostByPool(1, 0));
  }

  private SimulationStartResult createAndStartSimulation(
      Team team,
      Pkg pkg,
      String className,
      Map<UUID, HostByPool> hostsByPool,
      Map<String, String> systemProperties)
      throws EnterprisePluginException {
    final String[] classNameParts = className.split("\\.");
    final String simulationName = classNameParts[classNameParts.length - 1];

    final Simulation simulation =
        enterpriseClient.createSimulation(simulationName, team.id, className, pkg.id, hostsByPool);
    try {
      final RunSummary runSummary =
          enterpriseClient.startSimulation(simulation.id, systemProperties);
      return new SimulationStartResult(simulation, runSummary, true);
    } catch (EnterprisePluginException e) {
      throw new SimulationStartException(simulation, true, e);
    }
  }

  private String simulationClassName(Simulation simulation, File file, String simulationClass)
      throws EnterprisePluginException {
    final List<String> discoveredSimulationClasses = simulationFullyQualifiedNamesFromFile(file);

    if (discoveredSimulationClasses.isEmpty()) {
      throw new NoSimulationClassNameFoundException();
    } else if (simulationClass != null) {
      if (!discoveredSimulationClasses.contains(simulationClass)) {
        throw new InvalidSimulationClassException(discoveredSimulationClasses, simulationClass);
      }
      return simulationClass;
    } else if (simulation != null && discoveredSimulationClasses.contains(simulation.className)) {
      return simulation.className;
    } else if (discoveredSimulationClasses.size() == 1) {
      String head = discoveredSimulationClasses.get(0);
      logger.info("Pick only available simulation class name " + head);
      return head;
    }

    throw new SeveralSimulationClassNamesFoundException(discoveredSimulationClasses);
  }
}
