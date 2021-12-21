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

import static io.gatling.plugin.util.ObjectsUtil.nonNullParam;

import io.gatling.plugin.client.EnterpriseClient;
import io.gatling.plugin.client.exceptions.EnterpriseClientException;
import io.gatling.plugin.io.PluginIO;
import io.gatling.plugin.io.PluginLogger;
import io.gatling.plugin.io.input.InputChoice;
import io.gatling.plugin.io.input.exceptions.EmptyChoicesException;
import io.gatling.plugin.io.input.exceptions.ValidationException;
import io.gatling.plugin.model.*;
import io.gatling.plugin.model.Package;
import io.gatling.plugin.util.LambdaExceptionUtil;
import io.gatling.plugin.util.show.ShowableString;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class InteractiveEnterprisePluginClient implements InteractiveEnterprisePlugin {

  private final EnterpriseClient enterpriseClient;
  private final InputChoice inputChoice;
  private final PluginLogger logger;

  private static final int EXCLUDED_MAX_POOL_SIZE = 10;

  public InteractiveEnterprisePluginClient(EnterpriseClient enterpriseClient, PluginIO pluginIO) {
    this.enterpriseClient = enterpriseClient;
    this.inputChoice = new InputChoice(pluginIO);
    this.logger = pluginIO.getLogger();
  }

  private boolean createOrStartSimulation() {
    logger.info("Do you want to create or start a simulation?");
    ShowableString create =
        new ShowableString("Create a new Simulation on Gatling Enterprise, then start it");
    ShowableString start = new ShowableString("Start an existing Simulation on Gatling Enterprise");
    HashSet<ShowableString> choices = new HashSet<>();
    choices.add(create);
    choices.add(start);
    return inputChoice.inputFromList(choices) == create;
  }

  public SimulationAndRunSummary createOrStartSimulation(
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

    if (createSimulation) {
      return createAndStart(
          groupId, artifactId, className, classNames, file, simulations, systemProperties);
    } else {
      return startSimulation(file, systemProperties, simulations);
    }
  }

  private Simulation chooseSimulation(List<Simulation> simulations) throws EmptyChoicesException {
    if (simulations.isEmpty()) {
      throw new EmptyChoicesException("simulations");
    } else if (simulations.size() == 1) {
      Simulation simulation = simulations.get(0);
      logger.info(String.format("Pick only available simulation %s", simulation.show()));
      return simulation;
    } else {
      return inputChoice.inputFromList(new HashSet<>(simulations));
    }
  }

  private void uploadPackage(UUID simulationId, File packageFile) throws EnterpriseClientException {
    logger.info("Uploading package...");
    enterpriseClient.uploadPackage(simulationId, packageFile);
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
    logger.info("Please, choose pool size");
    return inputChoice.inputInt(EXCLUDED_MAX_POOL_SIZE);
  }

  /**
   * Create and start a simulation with given parameters
   *
   * @param configurationGroupId Optional
   * @param configurationArtifactId Optional
   * @param configurationClassName Optional
   * @param discoveredClassNames List of potential Simulation in the project, required
   * @param packageFile Path to the packaged JAR file to upload and run; required
   */
  private SimulationAndRunSummary createAndStart(
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
    Team team = chooseTeam();
    Pool pool = choosePool();
    int size = chooseSize();

    Package pkg = enterpriseClient.createPackage(packageName, team.id);
    uploadPackage(pkg.id, packageFile);

    // TODO: custom pools from configuration, MISC-313
    Map<UUID, HostByPool> hostsByPool = new HashMap<>();
    hostsByPool.put(pool.id, new HostByPool(size, 100));
    Simulation simulation =
        enterpriseClient.createSimulation(simulationName, team.id, className, pkg.id, hostsByPool);

    RunSummary runSummary = enterpriseClient.startSimulation(simulation.id, systemProperties);

    return new SimulationAndRunSummary(simulation, runSummary);
  }

  private String chooseClassName(String configurationClassName, List<String> discoveredClassNames) {
    logger.info("Please, choose the className of your simulation");
    Set<String> availableClassnames = new HashSet<>(discoveredClassNames);
    if (configurationClassName != null) {
      availableClassnames.add(configurationClassName);
    }
    return inputChoice.inputFromStringListWithCustom(availableClassnames);
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

    LambdaExceptionUtil.FunctionWithExceptions<String, String, ValidationException> validator =
        simulationName -> {
          if (existingSimulationNames.contains(simulationName)) {
            throw new ValidationException(
                String.format("Simulation name %s already exist", simulationName));
          } else {
            return simulationName;
          }
        };

    if (existingSimulationNames.contains(defaultSimulationName)) {
      logger.info("Please, enter simulation name");
      return inputChoice.inputString(validator);
    } else {
      Set<String> simulationNames = new HashSet<>();
      simulationNames.add(simulationNameFromClassName(className));
      logger.info("Please, choose the simulation name");
      return inputChoice.inputFromStringListWithCustom(simulationNames, validator);
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
    if (defaultPackageName != null && !existingPackageNames.contains(defaultPackageName)) {
      Set<String> packageNames = new HashSet<>();
      packageNames.add(defaultPackageName);
      logger.info("Please, choose your package name");
      return inputChoice.inputFromStringListWithCustom(packageNames);
    } else {
      logger.info("Please, enter your package name");
      return inputChoice.inputString(
          name -> {
            if (name.isEmpty()) {
              throw new ValidationException("package name should not be empty");
            } else if (existingPackageNames.contains(name)) {
              throw new ValidationException("package name already exist");
            }
            return name;
          });
    }
  }

  private Team chooseTeam() throws EnterpriseClientException, EmptyChoicesException {
    Set<Team> teams = new HashSet<>(enterpriseClient.getTeams());
    if (teams.isEmpty()) {
      throw new EmptyChoicesException("teams");
    } else {
      return inputChoice.inputFromList(teams);
    }
  }

  private Pool choosePool() throws EnterpriseClientException, EmptyChoicesException {
    Set<Pool> pools = new HashSet<>(enterpriseClient.getPools());
    if (pools.isEmpty()) {
      throw new EmptyChoicesException("pools");
    } else {
      return inputChoice.inputFromList(pools);
    }
  }
}
