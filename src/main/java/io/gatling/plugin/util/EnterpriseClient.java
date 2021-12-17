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

package io.gatling.plugin.util;

import io.gatling.plugin.util.exceptions.*;
import io.gatling.plugin.util.model.SimulationAndRunSummary;
import java.io.File;
import java.util.Map;
import java.util.UUID;

/**
 * The following exception sub-classes of {@link EnterpriseClientException} can be thrown by all
 * methods of this public API:
 *
 * <ul>
 *   <li>{@link UnauthorizedApiCallException}: invalid authentication token
 *   <li>{@link ForbiddenApiCallException}: authentication token with insufficient privileges
 *   <li>{@link ApiCallIOException}: unexpected IO error
 *   <li>{@link InvalidApiCallException}: invalid input data
 * </ul>
 */
public interface EnterpriseClient {

  /**
   * @param client Required
   * @param version Required
   * @throws UnsupportedClientException if this client version is outdated
   */
  void checkVersionSupport(String client, String version) throws EnterpriseClientException;

  /**
   * @param packageId Required
   * @param file Path to the packaged JAR file to upload; required
   * @throws PackageNotFoundException if the packageId does not exist
   */
  long uploadPackage(UUID packageId, File file) throws EnterpriseClientException;

  /**
   * @param simulationId Required
   * @param systemProperties Required (can be an empty map)
   * @param file Path to the packaged JAR file to upload and run; required
   * @throws SimulationNotFoundException if the simulationId does not exist
   */
  SimulationAndRunSummary startSimulation(
      UUID simulationId, Map<String, String> systemProperties, File file)
      throws EnterpriseClientException;

  /**
   * @param teamId Optional
   * @param groupId Optional
   * @param artifactId Required
   * @param className Required
   * @throws TeamConfigurationRequiredException if unable to choose a team (teamId should be
   *     specified)
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
