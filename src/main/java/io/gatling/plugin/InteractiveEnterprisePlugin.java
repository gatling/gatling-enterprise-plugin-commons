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
 * Enterprise plugin features that require inputs from the user, each method may block awaiting for
 * user input.
 *
 * <p>All methods can throw an {@link EmptyChoicesException} if the API returns an empty set of
 * available choices.
 *
 * <p>The following exception sub-classes of {@link EnterprisePluginException} can be thrown by all
 * methods of this client:
 *
 * <ul>
 *   <li>{@link UnauthorizedApiCallException}: invalid authentication token
 *   <li>{@link ForbiddenApiCallException}: authentication token with insufficient privileges
 *   <li>{@link ApiCallIOException}: unexpected IO error
 *   <li>{@link InvalidApiCallException}: invalid input data
 *   <li>{@link UserQuitException}: execution cancelled by the user
 * </ul>
 */
public interface InteractiveEnterprisePlugin extends AutoCloseable {

  /**
   * Create and start a simulation with given parameters
   *
   * @param teamId Optional, if not null, will be automatically picked
   * @param groupId Optional, if not null, will prefix the proposed package name
   * @param artifactId Optional, if not null, will suffix the proposed package name
   * @param simulationClass Optional, if not null, will be automatically picked
   * @param packageId Optional, if not null, will be automatically picked when creating a new
   *     simulation
   * @param systemProperties Required, can be an empty map; override conflicting system properties
   *     when running the simulation
   * @param file Required, path to the packaged JAR file to upload and run
   * @throws SimulationStartException if simulation start failed after creation
   */
  SimulationStartResult createOrStartSimulation(
      UUID teamId,
      String groupId,
      String artifactId,
      String simulationClass,
      UUID packageId,
      Map<String, String> systemProperties,
      File file)
      throws EnterprisePluginException, EmptyChoicesException;
}
