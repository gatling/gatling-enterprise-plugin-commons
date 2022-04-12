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

import io.gatling.plugin.exceptions.EnterprisePluginException;
import io.gatling.plugin.exceptions.SimulationScannerIOException;
import io.gatling.plugin.exceptions.UnsupportedJavaVersionException;
import io.gatling.scanner.AsmSimulationScanner;
import io.gatling.scanner.UnsupportedJavaMajorVersionException;

import java.io.File;
import java.io.IOException;
import java.util.List;

class EnterpriseSimulationScanner {

  /**
   * @param file JAR file to collect simulation fully qualified names
   * @return simulation classes detected in file
   * @throws SimulationScannerIOException when IOException occurred in AsmSimulationScanner
   */
  public static List<String> simulationFullyQualifiedNamesFromFile(File file)
      throws EnterprisePluginException {
    try {
      return AsmSimulationScanner.simulationFullyQualifiedNamesFromFile(file);
    } catch (IOException e) {
      throw new SimulationScannerIOException(file, e);
    } catch (UnsupportedJavaMajorVersionException e) {
      throw new UnsupportedJavaVersionException(e);
    }
  }
}
