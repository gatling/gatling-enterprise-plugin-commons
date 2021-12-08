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

import java.io.File;
import java.net.URL;
import java.util.UUID;
import okhttp3.*;

public final class OkHttpEnterpriseClient implements EnterpriseClient {

  private final PrivateApiRequests privateApiRequests;
  private final PackagesApiRequests packagesApiRequests;

  public OkHttpEnterpriseClient(OkHttpClient okHttpClient, URL url, String token) {
    final HttpUrl httpUrl = HttpUrl.get(url);
    this.privateApiRequests = new PrivateApiRequests(okHttpClient, httpUrl, token);
    this.packagesApiRequests = new PackagesApiRequests(okHttpClient, httpUrl, token);
  }

  public OkHttpEnterpriseClient(URL url, String token) {
    this(new OkHttpClient(), url, token);
  }

  @Override
  public void checkVersionSupport(String client, String version)
      throws UnsupportedClientException, EnterpriseClientException {
    privateApiRequests.checkVersionSupport(client, version);
  }

  @Override
  public long uploadPackage(UUID packageId, File file) throws EnterpriseClientException {
    return packagesApiRequests.uploadPackage(packageId, file);
  }
}
