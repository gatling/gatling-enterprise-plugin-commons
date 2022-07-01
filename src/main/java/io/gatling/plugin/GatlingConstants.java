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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GatlingConstants {

  private GatlingConstants() {}

  public static final int JAVA_MAJOR_VERSION;
  public static final List<String> DEFAULT_JVM_OPTIONS_BASE;
  public static final List<String> DEFAULT_JVM_OPTIONS_GATLING;

  static {
    String[] javaSpecVersionComponents =
        System.getProperty("java.specification.version").split("\\.");
    JAVA_MAJOR_VERSION =
        Integer.parseInt(
            javaSpecVersionComponents[0].equals("1")
                ? javaSpecVersionComponents[1]
                : javaSpecVersionComponents[0]);
  }

  static {
    List<String> base = new ArrayList<>();
    base.add("-server");
    base.add("-XX:+HeapDumpOnOutOfMemoryError");
    base.add("-XX:MaxInlineLevel=20");
    base.add("-XX:MaxTrivialSize=12");
    if (JAVA_MAJOR_VERSION < 9) {
      base.add("-XX:+UseG1GC");
    }
    if (JAVA_MAJOR_VERSION < 11) {
      base.add("-XX:+ParallelRefProcEnabled");
    }
    if (JAVA_MAJOR_VERSION < 15) {
      base.add("-XX:-UseBiasedLocking");
    }
    DEFAULT_JVM_OPTIONS_BASE = Collections.unmodifiableList(base);

    List<String> gatlingDefaultJvmOptions = new ArrayList<>(base);
    gatlingDefaultJvmOptions.add("-Xmx1G");
    DEFAULT_JVM_OPTIONS_GATLING = Collections.unmodifiableList(gatlingDefaultJvmOptions);
  }
}
