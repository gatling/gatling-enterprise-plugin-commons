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

import static io.gatling.plugin.util.ObjectsUtil.nonEmptyParam;
import static io.gatling.plugin.util.ObjectsUtil.nonNullParam;

import io.gatling.plugin.util.exceptions.EnterpriseClientException;
import io.gatling.plugin.util.exceptions.UnsupportedClientException;
import io.gatling.plugin.util.model.*;
import io.gatling.plugin.util.model.Package;
import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import okhttp3.*;

public final class OkHttpEnterpriseClient implements EnterpriseClient {

  private static final Map<String, String> DEFAULT_SYSTEM_PROPERTIES = Collections.emptyMap();
  private static final MeaningfulTimeWindow DEFAULT_TIME_WINDOW = new MeaningfulTimeWindow(0, 0);

  private final PrivateApiRequests privateApiRequests;
  private final PackagesApiRequests packagesApiRequests;
  private final PoolsApiRequests poolsApiRequests;
  private final SimulationsApiRequests simulationsApiRequests;
  private final TeamsApiRequests teamsApiRequests;

  public OkHttpEnterpriseClient(OkHttpClient okHttpClient, URL url, String token) {
    nonNullParam(okHttpClient, "okHttpClient");
    nonNullParam(url, "url");
    nonEmptyParam(token, "token");

    final HttpUrl httpUrl = HttpUrl.get(url);
    if (httpUrl == null) {
      throw new IllegalArgumentException(
          String.format("'%s' is not a valid HTTP or HTTPS URL", url));
    }

    this.privateApiRequests = new PrivateApiRequests(okHttpClient, httpUrl, token);
    this.packagesApiRequests = new PackagesApiRequests(okHttpClient, httpUrl, token);
    this.poolsApiRequests = new PoolsApiRequests(okHttpClient, httpUrl, token);
    this.simulationsApiRequests = new SimulationsApiRequests(okHttpClient, httpUrl, token);
    this.teamsApiRequests = new TeamsApiRequests(okHttpClient, httpUrl, token);
  }

  public OkHttpEnterpriseClient(URL url, String token) {
    this(new OkHttpClient(), url, token);
  }

  @Override
  public void checkVersionSupport(String client, String version)
      throws UnsupportedClientException, EnterpriseClientException {
    nonEmptyParam(client, "client");
    nonEmptyParam(version, "version");
    privateApiRequests.checkVersionSupport(client, version);
  }

  @Override
  public long uploadPackage(UUID packageId, File file) throws EnterpriseClientException {
    nonNullParam(packageId, "packageId");
    nonNullParam(file, "file");
    return doUploadPackage(packageId, file);
  }

  @Override
  public RunSummary startSimulation(
      UUID simulationId, Map<String, String> systemProperties, File file)
      throws EnterpriseClientException {
    nonNullParam(simulationId, "simulationId");
    nonNullParam(systemProperties, "systemProperties");
    nonNullParam(file, "file");

    final List<SystemProperty> sysPropsList =
        systemProperties.entrySet().stream()
            .map(entry -> new SystemProperty(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

    final Simulation simulation = simulationsApiRequests.getSimulation(simulationId);
    doUploadPackage(simulation.pkgId, file);
    return simulationsApiRequests.startSimulation(simulationId, sysPropsList);
  }

  @Override
  public Simulation createSimulation(String groupId, String artifactId, String className)
      throws EnterpriseClientException {
    nonEmptyParam(artifactId, "artifactId");
    nonEmptyParam(className, "className");

    final String packageName = groupId != null ? groupId + ":" + artifactId : artifactId;
    final String[] classNameParts = className.split("\\.");
    final String simulationName = classNameParts[classNameParts.length - 1];

    final Teams teams = teamsApiRequests.listTeams();
    final Team team;
    if (teams.data.size() == 1) {
      team = teams.data.get(0);
    } else {
      throw new UnsupportedOperationException(
          "Cannot automatically create a simulation if several teams are available (configuration prompts will be added in a later plugin version)");
    }

    final Pools pools = poolsApiRequests.listPools();
    final Pool pool;
    if (pools.data.size() > 0) {
      pool = pools.data.get(0);
    } else {
      // Should never happen on Gatling Enterprise Cloud
      throw new IllegalStateException(
          "Cannot automatically create a simulation if no pool is available");
    }

    final Package pkg =
        packagesApiRequests.createPackage(new PackageCreationPayload(packageName, team.id));

    final Map<UUID, HostByPool> hostsByPool = new HashMap<>();
    hostsByPool.put(pool.id, new HostByPool(1, 0));
    return simulationsApiRequests.createSimulation(
        new SimulationCreationPayload(
            simulationName,
            team.id,
            className,
            pkg.id,
            /* jvmOptions */ null,
            DEFAULT_SYSTEM_PROPERTIES,
            /* ignoreGlobalProperties */ false,
            DEFAULT_TIME_WINDOW,
            hostsByPool,
            /* usePoolWeights */ false,
            /* usePoolDedicatedIps */ false));
  }

  private long doUploadPackage(UUID packageId, File file) throws EnterpriseClientException {
    // TODO MISC-293 Add checksum verification to avoid unnecessary uploads
    return packagesApiRequests.uploadPackage(packageId, file);
  }
}
