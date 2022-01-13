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
import io.gatling.plugin.exceptions.EnterprisePluginException;
import io.gatling.plugin.exceptions.SimulationStartException;
import io.gatling.plugin.exceptions.UserQuitException;
import io.gatling.plugin.io.PluginIO;
import io.gatling.plugin.io.input.InputChoice;
import io.gatling.plugin.model.*;
import io.gatling.plugin.util.LambdaExceptionUtil.ConsumerWithExceptions;
import java.io.File;
import java.util.*;
import java.util.Collections;
import java.util.stream.Collectors;

public final class InteractiveEnterprisePluginClient extends PluginClient
    implements InteractiveEnterprisePlugin {

  private final InputChoice inputChoice;

  private static final int DEFAULT_HOST_WEIGHT = 100;

  public InteractiveEnterprisePluginClient(EnterpriseClient enterpriseClient, PluginIO pluginIO) {
    super(enterpriseClient, pluginIO.getLogger());
    this.inputChoice = new InputChoice(pluginIO);
  }

  public SimulationStartResult createOrStartSimulation(
      UUID teamId,
      String groupId,
      String artifactId,
      String simulationClass,
      List<String> discoveredSimulationClasses,
      UUID configuredPackageId,
      Map<String, String> systemProperties,
      File file)
      throws EnterprisePluginException, EmptyChoicesException {
    nonNullParam(discoveredSimulationClasses, "discoveredSimulationClasses");
    nonNullParam(systemProperties, "systemProperties");
    nonNullParam(file, "file");

    List<Simulation> simulations = enterpriseClient.getSimulations();
    boolean createSimulation = simulations.isEmpty() || chooseIfCreateSimulation();

    return createSimulation
        ? createAndStart(
            teamId,
            groupId,
            artifactId,
            simulationClass,
            discoveredSimulationClasses,
            configuredPackageId,
            file,
            simulations,
            systemProperties)
        : startSimulation(file, systemProperties, simulations);
  }

  private boolean chooseIfCreateSimulation() throws UserQuitException {
    logger.info("Do you want to create a new simulation or start an existing one?");
    final String create = "Create a new Simulation on Gatling Enterprise, then start it";
    final String start = "Start an existing Simulation on Gatling Enterprise";
    final List<String> choices = Arrays.asList(create, start);
    return inputChoice.inputFromStringList(choices, false).equals(create);
  }

  private SimulationStartResult startSimulation(
      File packageFile, Map<String, String> systemProperties, List<Simulation> simulations)
      throws EnterprisePluginException, EmptyChoicesException {
    logger.info("Proceeding to start simulation");

    if (simulations.isEmpty()) {
      throw new EmptyChoicesException("simulations");
    }
    final Simulation simulation =
        inputChoice.inputFromList(simulations, Show::simulation, Comparator.comparing(s -> s.name));

    uploadPackageWithChecksum(simulation.pkgId, packageFile);

    final RunSummary runSummary = enterpriseClient.startSimulation(simulation.id, systemProperties);

    // TODO: custom pools from configuration, MISC-313
    return new SimulationStartResult(simulation, runSummary, false);
  }

  /**
   * Create and start a simulation with given parameters
   *
   * @param configuredTeamId Optional
   * @param groupId Optional
   * @param artifactId Optional
   * @param configuredSimulationClass Optional
   * @param discoveredSimulationClasses List of potential Simulations in the project, required
   * @param packageFile Path to the packaged JAR file to upload and run; required
   */
  private SimulationStartResult createAndStart(
      UUID configuredTeamId,
      String groupId,
      String artifactId,
      String configuredSimulationClass,
      List<String> discoveredSimulationClasses,
      UUID configuredPackageId,
      File packageFile,
      List<Simulation> existingSimulations,
      Map<String, String> systemProperties)
      throws EnterprisePluginException, EmptyChoicesException {
    logger.info("Proceeding to the create simulation step");
    String simulationClass =
        chooseSimulationClass(configuredSimulationClass, discoveredSimulationClasses);
    Team team = chooseTeam(configuredTeamId);
    String simulationName = chooseSimulationName(simulationClass, existingSimulations);
    Pkg pkg = chooseOrCreatePackage(team.id, groupId, artifactId, configuredPackageId);
    Pool pool = choosePool();
    int size = chooseSize();

    uploadPackageWithChecksum(pkg.id, packageFile);

    // TODO: custom pools from configuration, MISC-313
    Map<UUID, HostByPool> hostsByPool =
        Collections.singletonMap(pool.id, new HostByPool(size, DEFAULT_HOST_WEIGHT));
    Simulation simulation =
        enterpriseClient.createSimulation(
            simulationName, team.id, simulationClass, pkg.id, hostsByPool);

    try {
      RunSummary runSummary = enterpriseClient.startSimulation(simulation.id, systemProperties);
      return new SimulationStartResult(simulation, runSummary, true);
    } catch (EnterprisePluginException e) {
      throw new SimulationStartException(simulation, e);
    }
  }

  private String chooseSimulationClass(
      String configuredSimulationClass, List<String> discoveredSimulationClasses)
      throws UserQuitException {
    if (configuredSimulationClass != null && !configuredSimulationClass.isEmpty()) {
      // Always accept explicit simulationClass configuration
      logger.info("Picking the configured simulation class: " + configuredSimulationClass);
      return configuredSimulationClass;
    }

    if (discoveredSimulationClasses.isEmpty()) {
      throw new IllegalStateException(
          "No simulation class discovered. Your project should contain at least one simulation (https://gatling.io/docs/gatling/reference/current/core/simulation/).");
    }

    logger.info("Choose a simulation class from the list:");
    final List<String> choices =
        discoveredSimulationClasses.stream().sorted().collect(Collectors.toList());
    return inputChoice.inputFromStringList(choices, true);
  }

  /** @param configuredTeamId Optional */
  private Team chooseTeam(UUID configuredTeamId)
      throws EnterprisePluginException, EmptyChoicesException {
    final List<Team> teams = enterpriseClient.getTeams();

    if (configuredTeamId != null) {
      // Always handle explicit configuration first
      final Team team =
          teams.stream()
              .filter(t -> t.id.equals(configuredTeamId))
              .findFirst()
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "Configured team ID " + configuredTeamId + " was not found"));
      logger.info(String.format("Picking the configured team: %s (%s)\n", team.id, team.name));
      return team;
    }

    if (teams.isEmpty()) {
      throw new EmptyChoicesException("teams");
    }

    logger.info("Choose a team from the list:");
    return inputChoice.inputFromList(teams, Show::team, Comparator.comparing(t -> t.name));
  }

  private static String simulationNameFromClassName(String className) {
    String[] parts = className.split("\\.");
    return parts[parts.length - 1];
  }

  private String chooseSimulationName(
      String simulationClass, List<Simulation> existingSimulations) {
    final String defaultSimulationName = simulationNameFromClassName(simulationClass);
    final List<String> existingSimulationNames =
        existingSimulations.stream()
            .map(simulation -> simulation.name)
            .collect(Collectors.toList());

    final ConsumerWithExceptions<String, IllegalArgumentException> validator =
        name -> {
          if (name.isEmpty()) {
            throw new IllegalArgumentException("The simulation name should not be empty");
          }
          if (existingSimulationNames.contains(name)) {
            throw new IllegalArgumentException("A simulation named " + name + " already exists");
          }
        };

    if (!existingSimulationNames.contains(defaultSimulationName)) {
      logger.info(
          "Enter a simulation name, or just hit enter to accept the default name ("
              + defaultSimulationName
              + ")");
      return inputChoice.inputStringWithDefault(defaultSimulationName, validator);
    } else {
      logger.info("Enter a simulation name");
      return inputChoice.inputString(validator);
    }
  }

  /**
   * @param groupId Optional
   * @param artifactId Optional
   * @param configuredPackageId Optional
   */
  private Pkg chooseOrCreatePackage(
      UUID teamId, String groupId, String artifactId, UUID configuredPackageId)
      throws EnterprisePluginException {
    final List<PkgIndex> existingPackages = enterpriseClient.getPackages();

    if (configuredPackageId != null) {
      // Always handle explicit configuration first
      final UUID packageId =
          existingPackages.stream()
              .filter(p -> p.id.equals(configuredPackageId))
              .map(p -> p.id)
              .findFirst()
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "Configured package ID " + configuredPackageId + " was not found"));
      return enterpriseClient.getPackage(packageId);
    }

    boolean createPackageChoice = existingPackages.isEmpty() || chooseIfCreatePackage();
    return createPackageChoice
        ? createPackage(teamId, groupId, artifactId, existingPackages)
        : choosePackage(existingPackages);
  }

  private boolean chooseIfCreatePackage() throws UserQuitException {
    logger.info("Do you want to create a new package or upload your project to an existing one?");
    final String create = "Create a new package on Gatling Enterprise";
    final String start = "Choose an existing package on Gatling Enterprise";
    final List<String> choices = Arrays.asList(create, start);
    return inputChoice.inputFromStringList(choices, false).equals(create);
  }

  private Pkg choosePackage(List<PkgIndex> existingPackages) throws EnterprisePluginException {
    logger.info("Choose a package from the list:");
    final UUID packageId =
        inputChoice.inputFromList(
                existingPackages, Show::packageIndex, Comparator.comparing(p -> p.name))
            .id;
    return enterpriseClient.getPackage(packageId);
  }

  private Pkg createPackage(
      UUID teamId, String groupId, String artifactId, List<PkgIndex> existingPackages)
      throws EnterprisePluginException {
    final String defaultPackageName =
        artifactId != null ? (groupId != null ? groupId + ":" + artifactId : artifactId) : null;
    final Set<String> existingPackageNames =
        existingPackages.stream().map(p -> p.name).collect(Collectors.toSet());

    final ConsumerWithExceptions<String, IllegalArgumentException> validator =
        name -> {
          if (name.isEmpty()) {
            throw new IllegalArgumentException("The package name should not be empty");
          }
          if (existingPackageNames.contains(name)) {
            throw new IllegalArgumentException("A package named " + name + " already exists");
          }
        };

    final String packageName;
    if (defaultPackageName != null && !existingPackageNames.contains(defaultPackageName)) {
      logger.info(
          "Enter a package name, or just hit enter to accept the default name ("
              + defaultPackageName
              + ")");
      packageName = inputChoice.inputStringWithDefault(defaultPackageName, validator);
    } else {
      logger.info("Enter a package name");
      packageName = inputChoice.inputString(validator);
    }

    return enterpriseClient.createPackage(packageName, teamId);
  }

  private Pool choosePool() throws EnterprisePluginException, EmptyChoicesException {
    final List<Pool> pools = enterpriseClient.getPools();

    if (pools.isEmpty()) {
      throw new EmptyChoicesException("pools");
    }

    logger.info("Choose the load injectors region");
    return inputChoice.inputFromList(pools, Show::pool, Comparator.comparing(p -> p.name));
  }

  private int chooseSize() {
    logger.info("Enter the number of load injectors");
    return inputChoice.inputInt(1);
  }
}
