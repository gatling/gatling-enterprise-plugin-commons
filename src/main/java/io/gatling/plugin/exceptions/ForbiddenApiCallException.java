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

package io.gatling.plugin.exceptions;

public final class ForbiddenApiCallException extends EnterprisePluginException {
  public ForbiddenApiCallException() {
    super(
        "API token valid but lacks required privileges: please configure a token with the role 'Configure' (see https://gatling.io/docs/enterprise/cloud/reference/admin/api_tokens/)");
  }
}
