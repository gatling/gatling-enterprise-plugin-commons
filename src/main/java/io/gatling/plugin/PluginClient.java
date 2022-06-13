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

import static io.gatling.plugin.EnterpriseSimulationScanner.simulationFullyQualifiedNamesFromFile;
import static io.gatling.plugin.util.ObjectsUtil.nonNullParam;

import io.gatling.plugin.client.EnterpriseClient;
import io.gatling.plugin.exceptions.EnterprisePluginException;
import io.gatling.plugin.exceptions.UnsupportedJavaVersionException;
import io.gatling.plugin.io.PluginLogger;
import io.gatling.plugin.model.ServerInformation;
import io.gatling.scanner.HighestJavaVersionClass;
import io.gatling.scanner.SimulationScanResult;
import java.io.File;
import java.util.List;
import java.util.UUID;

abstract class PluginClient {

  protected final EnterpriseClient enterpriseClient;
  protected final PluginLogger logger;

  public PluginClient(EnterpriseClient enterpriseClient, PluginLogger logger) {
    this.enterpriseClient = enterpriseClient;
    this.logger = logger;
  }

  protected long uploadPackageWithChecksum(UUID packageId, File file)
      throws EnterprisePluginException {
    nonNullParam(packageId, "packageId");
    nonNullParam(file, "file");
    if (enterpriseClient.uploadPackageWithChecksum(packageId, file) == -1) {
      logger.info("No code changes detected, skipping package upload");
    } else {
      logger.info("Package uploaded");
    }
    return file.length();
  }

  protected void checkSimulationByteCodeCompatibility(
      HighestJavaVersionClass highestJavaVersionClass) throws EnterprisePluginException {
    final ServerInformation serverInformation = enterpriseClient.getServerInformation();
    final int maximumJavaVersion = Integer.parseInt(serverInformation.versions.java.max);
    if (highestJavaVersionClass.javaVersion > maximumJavaVersion) {
      throw new UnsupportedJavaVersionException(
          highestJavaVersionClass.clazz, highestJavaVersionClass.javaVersion, maximumJavaVersion);
    }
  }

  protected List<String> simulationClassesFromCompatibleByteCodeFile(File file)
      throws EnterprisePluginException {
    final SimulationScanResult scanResult = simulationFullyQualifiedNamesFromFile(file);
    checkSimulationByteCodeCompatibility(scanResult.getHighestJavaVersionClass());
    return scanResult.getSimulationClasses();
  }
}
