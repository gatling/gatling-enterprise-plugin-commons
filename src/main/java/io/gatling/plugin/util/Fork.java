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

import io.gatling.plugin.io.PluginLogger;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public final class Fork {

  public static final class ForkException extends Exception {
    public final int exitValue;

    public ForkException(int exitValue) {
      this.exitValue = exitValue;
    }
  }

  private static final String ARG_FILE_PREFIX = "gatling-";
  private static final String ARG_FILE_SUFFIX = ".args";
  private static final String GATLING_MANIFEST_VALUE = "GATLING_ZINC";

  private final File javaExecutable;
  private final String mainClassName;
  private final List<String> classpath;
  private final boolean propagateSystemProperties;
  private final PluginLogger log;
  private final File workingDirectory;

  private final List<String> jvmArgs = new ArrayList<>();
  private final List<String> args = new ArrayList<>();

  public Fork(
      String mainClassName,
      List<String> classpath,
      List<String> jvmArgs,
      List<String> args,
      File javaExecutable,
      boolean propagateSystemProperties,
      PluginLogger log) {

    this(
        mainClassName,
        classpath,
        jvmArgs,
        args,
        javaExecutable,
        propagateSystemProperties,
        log,
        null);
  }

  public Fork(
      String mainClassName,
      List<String> classpath,
      List<String> jvmArgs,
      List<String> args,
      File javaExecutable,
      boolean propagateSystemProperties,
      PluginLogger log,
      File workingDirectory) {

    this.mainClassName = mainClassName;
    this.classpath = classpath;
    this.jvmArgs.addAll(jvmArgs);
    this.args.addAll(args);
    this.javaExecutable = javaExecutable;
    this.propagateSystemProperties = propagateSystemProperties;
    this.log = log;
    this.workingDirectory = workingDirectory;
  }

  public static String toWindowsShortName(String value) {
    if (Os.IS_WINDOWS) {
      int programFilesIndex = value.indexOf("Program Files");
      if (programFilesIndex >= 0) {
        // Could be "Program Files" or "Program Files (x86)"
        int firstSeparatorAfterProgramFiles =
            value.indexOf(File.separator, programFilesIndex + "Program Files".length());
        File longNameDir =
            firstSeparatorAfterProgramFiles < 0
                ? new File(value)
                : // C:\\Program Files with
                // trailing separator
                new File(value.substring(0, firstSeparatorAfterProgramFiles)); // chop child
        // Some other sibling dir could be PrograXXX and might shift short name index
        // so we can't be sure "Program Files" is "Progra~1" and "Program Files (x86)"
        // is "Progra~2"
        for (int i = 0; i < 10; i++) {
          File shortNameDir = new File(longNameDir.getParent(), "Progra~" + i);
          if (shortNameDir.equals(longNameDir)) {
            return shortNameDir.toString();
          }
        }
      }
    }

    return value;
  }

  private String safe(String value) {
    return value.contains(" ") ? '"' + value + '"' : value;
  }

  /**
   * Escapes any values it finds into their String form.
   *
   * <p>So a tab becomes the characters <code>'\\'</code> and <code>'t'</code>.
   *
   * @param str String to escape values in
   * @return String with escaped values
   * @throws NullPointerException if str is <code>null</code>
   */
  // forked from plexus-util
  private static String escape(String str) {
    // improved with code from cybertiger@cyberiantiger.org
    // unicode from him, and default for < 32's.
    StringBuilder buffer = new StringBuilder(2 * str.length());
    for (char ch : str.toCharArray()) {
      // handle unicode
      if (ch > 0xfff) {
        buffer.append("\\u" + Integer.toHexString(ch));
      } else if (ch > 0xff) {
        buffer.append("\\u0" + Integer.toHexString(ch));
      } else if (ch > 0x7f) {
        buffer.append("\\u00" + Integer.toHexString(ch));
      } else if (ch < 32) {
        switch (ch) {
          case '\b':
            buffer.append('\\').append('b');
            break;
          case '\n':
            buffer.append('\\').append('n');
            break;
          case '\t':
            buffer.append('\\').append('t');
            break;
          case '\f':
            buffer.append('\\').append('f');
            break;
          case '\r':
            buffer.append('\\').append('r');
            break;
          default:
            if (ch > 0xf) {
              buffer.append("\\u00" + Integer.toHexString(ch));
            } else {
              buffer.append("\\u000" + Integer.toHexString(ch));
            }
            break;
        }
      } else {
        switch (ch) {
          case '\'':
            buffer.append('\\').append('\'');
            break;
          case '"':
            buffer.append('\\').append('"');
            break;
          case '\\':
            buffer.append('\\').append('\\');
            break;
          default:
            buffer.append(ch);
            break;
        }
      }
    }
    return buffer.toString();
  }

  public void run() throws Exception {
    if (propagateSystemProperties) {
      for (Entry<Object, Object> systemProp : System.getProperties().entrySet()) {
        String name = systemProp.getKey().toString();
        String value = toWindowsShortName(systemProp.getValue().toString());
        if (isPropagatableProperty(name)) {
          if (name.contains(" ")) {
            log.error(
                "System property name '"
                    + name
                    + "' contains a whitespace and can't be propagated");

          } else if (Os.IS_WINDOWS && value.contains(" ")) {
            log.error(
                "System property value '"
                    + value
                    + "' contains a whitespace and can't be propagated on Windows");

          } else {
            this.jvmArgs.add("-D" + name + "=" + safe(escape(value)));
          }
        }
      }
    }

    this.jvmArgs.add("-jar");

    this.jvmArgs.add(
        createBooterJar(classpath, MainWithArgsInFile.class.getName()).getCanonicalPath());

    Process process =
        new ProcessBuilder(buildCommand()).directory(workingDirectory).inheritIO().start();
    process.getOutputStream().close();
    int exitValue = process.waitFor();
    if (exitValue != 0) {
      throw new ForkException(exitValue);
    }
  }

  private List<String> buildCommand() throws IOException {
    ArrayList<String> command = new ArrayList<>(jvmArgs.size() + 3);
    command.add(toWindowsShortName(javaExecutable.getCanonicalPath()));
    command.addAll(jvmArgs);
    command.add(mainClassName);
    command.add(createArgFile(args).getCanonicalPath());
    return command;
  }

  /**
   * Create a jar with just a manifest containing a Main-Class entry for BooterConfiguration and a
   * Class-Path entry for all classpath elements.
   *
   * @param classPath List of all classpath elements.
   * @param startClassName The classname to start (main-class)
   * @return The file pointing to the jar
   * @throws java.io.IOException When a file operation fails.
   */
  private static File createBooterJar(List<String> classPath, String startClassName)
      throws IOException {
    File file = File.createTempFile("gatlingbooter", ".jar");
    file.deleteOnExit();

    try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(file))) {
      jos.setLevel(JarOutputStream.STORED);
      JarEntry je = new JarEntry("META-INF/MANIFEST.MF");
      jos.putNextEntry(je);

      Manifest manifest = new Manifest();

      // we can't use StringUtils.join here since we need to add a '/' to
      // the end of directory entries - otherwise the jvm will ignore them.
      StringBuilder cp = new StringBuilder();
      for (String el : classPath) {
        // NOTE: if File points to a directory, this entry MUST end in '/'.
        cp.append(getURL(new File(el)).toExternalForm()).append(" ");
      }
      cp.setLength(cp.length() - 1);

      manifest.getMainAttributes().putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
      manifest.getMainAttributes().putValue(Attributes.Name.CLASS_PATH.toString(), cp.toString());
      manifest.getMainAttributes().putValue(Attributes.Name.MAIN_CLASS.toString(), startClassName);
      manifest.getMainAttributes().putValue(GATLING_MANIFEST_VALUE, "true");

      manifest.write(jos);
    }

    return file;
  }

  // encode any characters that do not comply with RFC 2396
  // this is primarily to handle Windows where the user's home directory contains
  // spaces
  private static URL getURL(File file) throws MalformedURLException {
    return new URL(file.toURI().toASCIIString());
  }

  private boolean isPropagatableProperty(String name) {
    return !name.startsWith("java.")
        && !name.startsWith("sun.")
        && !name.startsWith("maven.")
        && !name.startsWith("file.")
        && !name.startsWith("awt.")
        && !name.startsWith("os.")
        && !name.startsWith("user.")
        && !name.startsWith("idea.")
        && !name.startsWith("guice.")
        && !name.startsWith("hudson.")
        && !name.equals("line.separator")
        && !name.equals("path.separator")
        && !name.equals("classworlds.conf")
        && !name.equals("org.slf4j.simpleLogger.defaultLogLevel");
  }

  private File createArgFile(List<String> args) throws IOException {
    final File argFile = File.createTempFile(ARG_FILE_PREFIX, ARG_FILE_SUFFIX);
    argFile.deleteOnExit();
    try (PrintWriter out = new PrintWriter(argFile)) {
      for (String arg : args) {
        out.println(arg);
      }
      return argFile;
    }
  }
}
