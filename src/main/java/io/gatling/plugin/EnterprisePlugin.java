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

import io.gatling.plugin.client.exceptions.*;
import io.gatling.plugin.model.SimulationAndRunSummary;
import java.io.File;
import java.util.Map;
import java.util.UUID;

/**
 * Enterprise plugin features
 *
 * <p>The following exception sub-classes of {@link EnterpriseClientException} can be thrown by all
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
   * Upload file to the package associated with given ID
   *
   * @param packageId Required
   * @param file Path to the packaged JAR file to upload and run; required
   */
  long uploadPackage(UUID packageId, File file) throws EnterpriseClientException;

  /**
   * Upload file to the package configured on the given simulation ID, and start the simulation
   *
   * @param simulationId Required
   * @param systemProperties Required, can be an empty map
   * @param file Required, Path to the packaged JAR file to upload and run
   * @throws SimulationNotFoundException if the simulationId does not exist
   */
  SimulationAndRunSummary uploadPackageAndStartSimulation(
      UUID simulationId, Map<String, String> systemProperties, File file)
      throws EnterpriseClientException;

  /**
   * Create and start a simulation with given parameters
   *
   * @param teamId Optional
   * @param groupId Optional
   * @param artifactId Required
   * @param className Required
   */
  SimulationAndRunSummary createAndStartSimulation(
      UUID teamId,
      String groupId,
      String artifactId,
      String className,
      Map<String, String> systemProperties,
      File file)
      throws EnterpriseClientException;
}