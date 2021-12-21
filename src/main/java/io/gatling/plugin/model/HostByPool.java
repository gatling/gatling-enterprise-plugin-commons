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

package io.gatling.plugin.model;

import java.util.Objects;

public final class HostByPool {
  public final int size;
  public final int weight;

  public HostByPool(int size, int weight) {
    this.size = size;
    this.weight = weight;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    HostByPool that = (HostByPool) o;
    return size == that.size && weight == that.weight;
  }

  @Override
  public int hashCode() {
    return Objects.hash(size, weight);
  }

  @Override
  public String toString() {
    return String.format("HostByPool{size='%s',weight='%s'}", size, weight);
  }
}
