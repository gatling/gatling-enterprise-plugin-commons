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

import io.gatling.plugin.util.model.RunSummary;
import io.gatling.plugin.util.model.Simulation;
import java.io.File;
import java.util.Map;
import java.util.UUID;

public interface EnterpriseClient {

  /**
   * @param client Required
   * @param version Required
   */
  void checkVersionSupport(String client, String version)
      throws UnsupportedClientException, EnterpriseClientException;

  /**
   * @param packageId Required
   * @param file Path to the packaged JAR file to upload; required
   */
  long uploadPackage(UUID packageId, File file) throws EnterpriseClientException;

  /**
   * @param simulationId Required
   * @param systemProperties Required (can be an empty map)
   * @param file Path to the packaged JAR file to upload and run; required
   */
  RunSummary startSimulation(UUID simulationId, Map<String, String> systemProperties, File file)
      throws EnterpriseClientException;

  /**
   * @param groupId Optional
   * @param artifactId Required
   * @param className Required
   */
  Simulation createSimulation(String groupId, String artifactId, String className)
      throws EnterpriseClientException;
}
