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

import io.gatling.plugin.client.exceptions.*;
import io.gatling.plugin.io.input.exceptions.EmptyChoicesException;
import io.gatling.plugin.model.SimulationAndRunSummary;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Enterprise plugin feature that require inputs from user, each methods may block waiting for user
 * input
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
   * @param groupId Optional
   * @param artifactId Optional
   * @param className Optional
   * @param classNames List of potential Simulation in the project, required
   * @param systemProperties Required (can be an empty map)
   * @param file Path to the packaged JAR file to upload and run; required
   */
  SimulationAndRunSummary createOrStartSimulation(
      String groupId,
      String artifactId,
      String className,
      List<String> classNames,
      Map<String, String> systemProperties,
      File file)
      throws EnterpriseClientException, EmptyChoicesException;
}
