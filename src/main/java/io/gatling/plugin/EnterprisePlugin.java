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

import io.gatling.plugin.exceptions.*;
import io.gatling.plugin.model.SimulationStartResult;
import java.io.File;
import java.util.Map;
import java.util.UUID;

/**
 * Enterprise plugin features
 *
 * <p>The following exception sub-classes of {@link EnterprisePluginException} can be thrown by all
 * methods of this client:
 *
 * <ul>
 *   <li>{@link UnauthorizedApiCallException}: invalid authentication token
 *   <li>{@link ForbiddenApiCallException}: authentication token with insufficient privileges
 *   <li>{@link ApiCallIOException}: unexpected IO error
 *   <li>{@link InvalidApiCallException}: invalid input data
 * </ul>
 */
public interface EnterprisePlugin {

  /**
   * Upload file to the package associated with given packageId
   *
   * @param packageId Required
   * @param file Path to the packaged JAR file to upload and run; required
   */
  long uploadPackage(UUID packageId, File file) throws EnterprisePluginException;

  /**
   * Upload file to the package associated to the given simulationId
   *
   * @param simulationId Required
   * @param file Path to the packaged JAR file to upload and run; required
   */
  long uploadPackageWithSimulationId(UUID simulationId, File file) throws EnterprisePluginException;

  /**
   * Upload file to the package configured on the given simulation ID, and start the simulation
   *
   * @param simulationId Required
   * @param systemProperties Required, can be an empty map
   * @param file Required, Path to the packaged JAR file to upload and run
   * @throws SimulationNotFoundException if the simulationId does not exist
   */
  SimulationStartResult uploadPackageAndStartSimulation(
      UUID simulationId, Map<String, String> systemProperties, File file)
      throws EnterprisePluginException;

  /**
   * Create and start a simulation with given parameters
   *
   * @param teamId Optional
   * @param groupId Optional
   * @param artifactId Required
   * @param simulationClass Required
   * @param packageId Optional
   * @param systemProperties Required, can be an empty Map
   * @param file Required
   * @throws SimulationStartException if simulation start failed after creation
   */
  SimulationStartResult createAndStartSimulation(
      UUID teamId,
      String groupId,
      String artifactId,
      String simulationClass,
      UUID packageId,
      Map<String, String> systemProperties,
      File file)
      throws EnterprisePluginException;
}
