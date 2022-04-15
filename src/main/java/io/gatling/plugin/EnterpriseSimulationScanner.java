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
import io.gatling.scanner.AsmSimulationScanner;
import io.gatling.scanner.SimulationScanResult;
import java.io.File;
import java.io.IOException;

class EnterpriseSimulationScanner {

  /**
   * @param file JAR file to collect simulation fully qualified names
   * @return simulation classes detected in file, along with class with the highest bytecode version
   * @throws SimulationScannerIOException when IOException occurred in AsmSimulationScanner
   */
  public static SimulationScanResult simulationFullyQualifiedNamesFromFile(File file)
      throws EnterprisePluginException {
    try {
      return AsmSimulationScanner.scan(file);
    } catch (IOException e) {
      throw new SimulationScannerIOException(file, e);
    }
  }
}
