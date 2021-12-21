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

import static io.gatling.plugin.util.ObjectsUtil.nonNullParam;

import io.gatling.plugin.client.EnterpriseClient;
import io.gatling.plugin.client.exceptions.EnterpriseClientException;
import io.gatling.plugin.io.PluginIO;
import io.gatling.plugin.io.PluginLogger;
import io.gatling.plugin.io.input.InputChoice;
import io.gatling.plugin.model.*;
import io.gatling.plugin.util.LambdaExceptionUtil.ConsumerWithExceptions;
import java.io.File;
import java.util.*;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class InteractiveEnterprisePluginClient extends PluginClient
    implements InteractiveEnterprisePlugin {

  private final InputChoice inputChoice;
  private final PluginLogger logger;

  private static final int EXCLUSIVE_MAX_POOL_SIZE = 11;
  private static final int DEFAULT_HOST_WEIGHT = 100;

  public InteractiveEnterprisePluginClient(EnterpriseClient enterpriseClient, PluginIO pluginIO) {
    super(enterpriseClient);
    this.inputChoice = new InputChoice(pluginIO);
    this.logger = pluginIO.getLogger();
  }

  private boolean createOrStartSimulation() {
    logger.info("Do you want to create a new simulation or start an existing one?");
    String create = "Create a new Simulation on Gatling Enterprise, then start it";
    String start = "Start an existing Simulation on Gatling Enterprise";
    HashSet<String> choices = new HashSet<>();
    choices.add(create);
    choices.add(start);
    return inputChoice.inputFromList(choices, Function.identity()).equals(create);
  }

  public SimulationAndRunSummary createOrStartSimulation(
      UUID teamId,
      String groupId,
      String artifactId,
      String className,
      List<String> classNames,
      Map<String, String> systemProperties,
      File file)
      throws EnterpriseClientException, EmptyChoicesException {
    nonNullParam(systemProperties, "systemProperties");
    nonNullParam(file, "file");

    List<Simulation> simulations = enterpriseClient.getSimulations();
    boolean createSimulation = simulations.isEmpty() || createOrStartSimulation();

    return createSimulation
        ? createAndStart(
            teamId, groupId, artifactId, className, classNames, file, simulations, systemProperties)
        : startSimulation(file, systemProperties, simulations);
  }

  private Simulation chooseSimulation(List<Simulation> simulations) throws EmptyChoicesException {
    if (simulations.isEmpty()) {
      throw new EmptyChoicesException("simulations");
    }

    if (simulations.size() == 1) {
      Simulation simulation = simulations.get(0);
      logger.info("Picking only available simulation: " + Show.simulation(simulation));
      return simulation;
    } else {
      return inputChoice.inputFromList(new HashSet<>(simulations), Show::simulation);
    }
  }

  private void uploadPackage(UUID artifactId, File packageFile) throws EnterpriseClientException {
    logger.info("Uploading package...");
    enterpriseClient.uploadPackage(artifactId, packageFile);
    logger.info("Package uploaded");
  }

  private SimulationAndRunSummary startSimulation(
      File packageFile, Map<String, String> systemProperties, List<Simulation> simulations)
      throws EnterpriseClientException, EmptyChoicesException {
    logger.info("Proceeding to start simulation");
    Simulation simulation = chooseSimulation(simulations);

    uploadPackage(simulation.pkgId, packageFile);

    RunSummary runSummary = enterpriseClient.startSimulation(simulation.id, systemProperties);

    // TODO: custom pools from configuration, MISC-313
    return new SimulationAndRunSummary(simulation, runSummary);
  }

  private int chooseSize() {
    logger.info(
        String.format(
            "Please, enter the number of load injectors (must be between 1 and %d)",
            EXCLUSIVE_MAX_POOL_SIZE - 1));
    return inputChoice.inputInt(1, EXCLUSIVE_MAX_POOL_SIZE);
  }

  /**
   * Create and start a simulation with given parameters
   *
   * @param configurationTeamId Optional
   * @param configurationGroupId Optional
   * @param configurationArtifactId Optional
   * @param configurationClassName Optional
   * @param discoveredClassNames List of potential Simulation in the project, required
   * @param packageFile Path to the packaged JAR file to upload and run; required
   */
  private SimulationAndRunSummary createAndStart(
      UUID configurationTeamId,
      String configurationGroupId,
      String configurationArtifactId,
      String configurationClassName,
      List<String> discoveredClassNames,
      File packageFile,
      List<Simulation> existingSimulations,
      Map<String, String> systemProperties)
      throws EnterpriseClientException, EmptyChoicesException {
    logger.info("Proceeding to the create simulation step");
    String className = chooseClassName(configurationClassName, discoveredClassNames);
    String simulationName = chooseSimulationName(className, existingSimulations);
    String packageName = choosePackageName(configurationGroupId, configurationArtifactId);
    Team team = chooseTeam(configurationTeamId);
    Pool pool = choosePool();
    int size = chooseSize();

    Pkg pkg = enterpriseClient.createPackage(packageName, team.id);
    uploadPackage(pkg.id, packageFile);

    // TODO: custom pools from configuration, MISC-313
    Map<UUID, HostByPool> hostsByPool =
        Collections.singletonMap(pool.id, new HostByPool(size, DEFAULT_HOST_WEIGHT));
    Simulation simulation =
        enterpriseClient.createSimulation(simulationName, team.id, className, pkg.id, hostsByPool);

    RunSummary runSummary = enterpriseClient.startSimulation(simulation.id, systemProperties);

    return new SimulationAndRunSummary(simulation, runSummary);
  }

  private String chooseClassName(String configurationClassName, List<String> discoveredClassNames) {
    if (discoveredClassNames.isEmpty()) {
      throw new IllegalStateException(
          "No simulation class discovered. see https://gatling.io/docs/gatling/reference/current/core/simulation/");
    }

    if (configurationClassName != null && discoveredClassNames.contains(configurationClassName)) {
      logger.info("Picking configured class name: " + configurationClassName);
      return configurationClassName;
    } else {
      logger.info("Choose the class name for your simulation");
      return inputChoice.inputFromList(new HashSet<>(discoveredClassNames), Function.identity());
    }
  }

  private static String simulationNameFromClassName(String className) {
    String[] parts = className.split("\\.");
    return parts[parts.length - 1];
  }

  private String chooseSimulationName(String className, List<Simulation> existingSimulations) {
    String defaultSimulationName = simulationNameFromClassName(className);
    List<String> existingSimulationNames =
        existingSimulations.stream()
            .map(simulation -> simulation.name)
            .collect(Collectors.toList());

    ConsumerWithExceptions<String, IllegalArgumentException> validator =
        simulationName -> {
          if (existingSimulationNames.contains(simulationName)) {
            throw new IllegalArgumentException(
                String.format("Simulation name %s already exist", simulationName));
          }
        };

    if (existingSimulationNames.contains(defaultSimulationName)) {
      logger.info("Enter simulation name");
      return inputChoice.inputString(validator);
    } else {
      logger.info("Choose the simulation name");
      return inputChoice.inputFromStringListWithCustom(
          Collections.singleton(simulationNameFromClassName(className)), validator);
    }
  }

  /**
   * @param groupId Optional
   * @param artifactId Optional
   */
  private String choosePackageName(String groupId, String artifactId)
      throws EnterpriseClientException {
    List<String> existingPackageNames =
        enterpriseClient.getPackages().stream().map(pkg -> pkg.name).collect(Collectors.toList());

    String defaultPackageName =
        artifactId != null ? (groupId != null ? groupId + ":" + artifactId : artifactId) : null;

    ConsumerWithExceptions<String, IllegalArgumentException> packageValidation =
        name -> {
          if (name.isEmpty()) {
            throw new IllegalArgumentException("package name should not be empty");
          } else if (existingPackageNames.contains(name)) {
            throw new IllegalArgumentException("package name already exist");
          }
        };

    if (defaultPackageName != null && !existingPackageNames.contains(defaultPackageName)) {
      Set<String> packageNames = new HashSet<>();
      packageNames.add(defaultPackageName);
      logger.info("Please, choose your package name");
      return inputChoice.inputFromStringListWithCustom(packageNames, packageValidation);
    } else {
      logger.info("Please, enter your package name");
      return inputChoice.inputString(packageValidation);
    }
  }

  /** @param configurationTeamId Optional */
  private Team chooseTeam(UUID configurationTeamId)
      throws EnterpriseClientException, EmptyChoicesException {
    Set<Team> teams = new HashSet<>(enterpriseClient.getTeams());
    if (teams.isEmpty()) {
      throw new EmptyChoicesException("teams");
    } else {
      return teams.stream()
          .filter(t -> t.id == configurationTeamId)
          .findFirst()
          .orElseGet(() -> inputChoice.inputFromList(teams, Show::team));
    }
  }

  private Pool choosePool() throws EnterpriseClientException, EmptyChoicesException {
    Set<Pool> pools = new HashSet<>(enterpriseClient.getPools());
    if (pools.isEmpty()) {
      throw new EmptyChoicesException("pools");
    } else {
      return inputChoice.inputFromList(pools, Show::pool);
    }
  }
}
