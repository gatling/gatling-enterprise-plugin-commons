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

package io.gatling.plugin.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class JavaLocator {
  private JavaLocator() {}

  // inspired from org.codehaus.plexus.compiler.javac.JavacCompiler#getJavacExecutable
  public static File getJavaExecutable() {
    String javaCommand = "java" + (Os.IS_WINDOWS ? ".exe" : "");

    String javaHomeSystemProperty = System.getProperty("java.home");
    if (javaHomeSystemProperty != null) {
      Path javaHomePath = Paths.get(javaHomeSystemProperty);
      if (javaHomePath.endsWith("jre")) {
        // Old JDK versions contain a JRE. We might be pointing to that.
        // We want to try to use the JDK instead as we need javac in order to compile mixed
        // Java-Scala projects.
        File javaExec = javaHomePath.resolveSibling("bin").resolve(javaCommand).toFile();
        if (javaExec.isFile()) {
          return javaExec;
        }
      }

      // old standalone JRE or modern JDK
      File javaExec = javaHomePath.resolve("bin").resolve(javaCommand).toFile();
      if (javaExec.isFile()) {
        return javaExec;
      } else {
        throw new IllegalStateException(
            "Couldn't locate java in defined java.home system property.");
      }
    }

    // fallback: try to resolve from JAVA_HOME
    String javaHomeEnvVar = System.getenv("JAVA_HOME");
    if (javaHomeEnvVar == null) {
      throw new IllegalStateException(
          "Couldn't locate java, try setting JAVA_HOME environment variable.");
    }

    File javaExec = Paths.get(javaHomeEnvVar).resolve("bin").resolve(javaCommand).toFile();
    if (javaExec.isFile()) {
      return javaExec;
    } else {
      throw new IllegalStateException(
          "Couldn't locate java in defined JAVA_HOME environment variable.");
    }
  }
}
