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
import java.io.File;
import java.util.UUID;

public interface BatchEnterprisePlugin extends EnterprisePlugin {

  /**
   * Upload file to the package associated with given packageId
   *
   * @param packageId Required
   * @param file File to upload and run; required
   */
  long uploadPackage(UUID packageId, File file) throws EnterprisePluginException;

  /**
   * Upload file to the package associated to the given simulationId
   *
   * @param simulationId Required
   * @param file File to upload and run; required
   */
  long uploadPackageWithSimulationId(UUID simulationId, File file) throws EnterprisePluginException;
}
