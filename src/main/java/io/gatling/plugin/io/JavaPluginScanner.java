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

package io.gatling.plugin.io;

import java.util.Scanner;

public class JavaPluginScanner implements PluginScanner {

  private final Scanner scanner;

  public JavaPluginScanner(Scanner scanner) {
    this.scanner = scanner;
  }

  @Override
  public String readString() {
    return scanner.nextLine();
  }

  @Override
  public int readInt() throws NumberFormatException {
    return Integer.parseInt(readString());
  }
}
