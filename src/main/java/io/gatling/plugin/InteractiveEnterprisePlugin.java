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
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Enterprise plugin features that require inputs from the user, each method may block awaiting for
 * user input.
 *
 * <p>All methods can throw an {@link EmptyChoicesException} if the API returns an empty set of
 * available choices.
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
public interface InteractiveEnterprisePlugin {

  /**
   * Create and start a simulation with given parameters
   *
   * @param teamId Optional, if not null and the team exists, will be automatically pick
   * @param groupId Optional, if not null, will prefix the proposed package name
   * @param artifactId Optional, if not null, will suffix the proposed package name
   * @param className Optional, if not null, will be added to the list of available classNames
   * @param classNames Required, the list of potential Simulation class names in the project,
   *     required
   * @param systemProperties Required, can be an empty map; override conflicting system properties
   *     when running the simulation
   * @param file Required, path to the packaged JAR file to upload and run
   */
  SimulationAndRunSummary createOrStartSimulation(
      UUID teamId,
      String groupId,
      String artifactId,
      String className,
      List<String> classNames,
      Map<String, String> systemProperties,
      File file)
      throws EnterpriseClientException, EmptyChoicesException;
}
