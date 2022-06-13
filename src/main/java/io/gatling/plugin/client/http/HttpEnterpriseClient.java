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

package io.gatling.plugin.client.http;

import io.gatling.plugin.client.EnterpriseClient;
import io.gatling.plugin.exceptions.ApiCallIOException;
import io.gatling.plugin.exceptions.EnterprisePluginException;
import io.gatling.plugin.exceptions.InvalidBaseUrlException;
import io.gatling.plugin.exceptions.PackageNotFoundException;
import io.gatling.plugin.model.*;
import io.gatling.plugin.util.checksum.PkgChecksum;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public final class HttpEnterpriseClient implements EnterpriseClient {

  private static final Map<String, String> DEFAULT_SYSTEM_PROPERTIES = Collections.emptyMap();
  private static final Map<String, String> DEFAULT_ENVIRONMENT_VARIABLES = Collections.emptyMap();
  private static final MeaningfulTimeWindow DEFAULT_TIME_WINDOW = new MeaningfulTimeWindow(0, 0);

  private final InfoApiRequests infoApiRequests;
  private final PackagesApiRequests packagesApiRequests;
  private final PoolsApiRequests poolsApiRequests;
  private final SimulationsApiRequests simulationsApiRequests;
  private final TeamsApiRequests teamsApiRequests;

  public HttpEnterpriseClient(URL baseUrl, String token, String client, String version)
      throws EnterprisePluginException {
    if (!"http".equals(baseUrl.getProtocol()) && !"https".equals(baseUrl.getProtocol())) {
      throw new InvalidBaseUrlException(baseUrl);
    }

    infoApiRequests = new InfoApiRequests(baseUrl, token);
    packagesApiRequests = new PackagesApiRequests(baseUrl, token);
    poolsApiRequests = new PoolsApiRequests(baseUrl, token);
    simulationsApiRequests = new SimulationsApiRequests(baseUrl, token);
    teamsApiRequests = new TeamsApiRequests(baseUrl, token);

    new PrivateApiRequests(baseUrl, token).checkVersionSupport(client, version);
  }

  @Override
  public ServerInformation getServerInformation() throws EnterprisePluginException {
    return infoApiRequests.getServerInformation();
  }

  @Override
  public List<Simulation> getSimulations() throws EnterprisePluginException {
    return simulationsApiRequests.listSimulations().data;
  }

  @Override
  public Simulation getSimulation(UUID simulationId) throws EnterprisePluginException {
    return simulationsApiRequests.getSimulation(simulationId);
  }

  @Override
  public List<Team> getTeams() throws EnterprisePluginException {
    return teamsApiRequests.listTeams().data;
  }

  @Override
  public List<Pool> getPools() throws EnterprisePluginException {
    return poolsApiRequests.listPools().data;
  }

  @Override
  public List<PkgIndex> getPackages() throws EnterprisePluginException {
    return packagesApiRequests.listPackages().data;
  }

  @Override
  public Pkg getPackage(UUID pkgId) throws EnterprisePluginException {
    return packagesApiRequests.readPackage(pkgId);
  }

  @Override
  public long uploadPackage(UUID packageId, File file) throws EnterprisePluginException {
    return packagesApiRequests.uploadPackage(packageId, file);
  }

  @Override
  public RunSummary startSimulation(
      UUID simulationId,
      Map<String, String> systemProperties,
      Map<String, String> environmentVariables)
      throws EnterprisePluginException {

    final StartOptions options = new StartOptions(systemProperties, environmentVariables);

    return simulationsApiRequests.startSimulation(simulationId, options);
  }

  private boolean checksumComparison(UUID packageId, File file) throws EnterprisePluginException {
    try {
      Pkg pkg = getPackage(packageId);
      return pkg.file != null && PkgChecksum.computeChecksum(file).equals(pkg.file.checksum);
    } catch (PackageNotFoundException e) {
      return false;
    } catch (IOException e) {
      throw new ApiCallIOException(e);
    }
  }

  @Override
  public long uploadPackageWithChecksum(UUID packageId, File file)
      throws EnterprisePluginException {
    return checksumComparison(packageId, file) ? -1 : uploadPackage(packageId, file);
  }

  @Override
  public SimulationClassName updateSimulationClassName(UUID simulationId, String className)
      throws EnterprisePluginException {
    return simulationsApiRequests.updateSimulationClassName(simulationId, className);
  }

  @Override
  public Simulation createSimulation(
      String simulationName,
      UUID teamId,
      String className,
      UUID pkgId,
      Map<UUID, HostByPool> hostsByPool)
      throws EnterprisePluginException {
    return simulationsApiRequests.createSimulation(
        new SimulationCreationPayload(
            simulationName,
            teamId,
            className,
            pkgId,
            DEFAULT_SYSTEM_PROPERTIES,
            DEFAULT_ENVIRONMENT_VARIABLES,
            /* ignoreGlobalProperties */ false,
            DEFAULT_TIME_WINDOW,
            hostsByPool,
            /* usePoolWeights */ false,
            /* usePoolDedicatedIps */ false));
  }

  @Override
  public Pkg createPackage(String packageName, UUID teamId) throws EnterprisePluginException {
    return packagesApiRequests.createPackage(new PackageCreationPayload(packageName, teamId));
  }
}
