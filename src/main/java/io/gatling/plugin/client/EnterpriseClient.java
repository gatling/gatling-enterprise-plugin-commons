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

package io.gatling.plugin.client;

import io.gatling.plugin.client.exceptions.*;
import io.gatling.plugin.model.*;
import java.io.File;
import java.util.List;
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

  List<Simulation> getSimulations() throws EnterpriseClientException;

  public Simulation getSimulation(UUID simulationId) throws EnterpriseClientException;

  public List<Team> getTeams() throws EnterpriseClientException;

  public List<Pool> getPools() throws EnterpriseClientException;

  List<PkgIndex> getPackages() throws EnterpriseClientException;

  public Pkg getPackage(UUID pkgId) throws EnterpriseClientException;

  /**
   * @param packageId Required
   * @param file Path to the packaged JAR file to upload; required
   * @throws PackageNotFoundException if the packageId does not exist
   */
  long uploadPackage(UUID packageId, File file) throws EnterpriseClientException;

  /**
   * @param simulationId Required
   * @param systemProperties Required (can be an empty map)
   * @throws SimulationNotFoundException if the simulationId does not exist
   */
  RunSummary startSimulation(UUID simulationId, Map<String, String> systemProperties)
      throws EnterpriseClientException;

  long uploadPackageWithChecksum(UUID packageId, File file) throws EnterpriseClientException;

  Simulation createSimulation(
      String simulationName,
      UUID teamId,
      String className,
      UUID pkgId,
      Map<UUID, HostByPool> hostsByPool)
      throws EnterpriseClientException;

  Pkg createPackage(String packageName, UUID teamId) throws EnterpriseClientException;
}
