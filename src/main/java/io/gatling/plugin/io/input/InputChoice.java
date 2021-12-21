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

package io.gatling.plugin.io.input;

import io.gatling.plugin.io.PluginIO;
import io.gatling.plugin.io.PluginLogger;
import io.gatling.plugin.io.PluginScanner;
import io.gatling.plugin.util.LambdaExceptionUtil.ConsumerWithExceptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;

public class InputChoice {

  private final PluginLogger logger;
  private final PluginScanner scanner;
  private static final int NOT_READ = -1;
  private static final String CUSTOM_ENTRY = "Custom entry";

  public InputChoice(PluginIO pluginIO) {
    this.scanner = pluginIO.getScanner();
    this.logger = pluginIO.getLogger();
  }

  /**
   * Read a non negative number, inferior to max
   *
   * @param inclusiveMin inclusive minimum value of user entry
   * @param exclusiveMax exclusive maximum value of user entry
   */
  public int inputInt(int inclusiveMin, int exclusiveMax) {
    int result = NOT_READ;
    do {
      try {
        int read = scanner.readInt();
        if (read < inclusiveMin || read >= exclusiveMax) {
          logger.error(
              String.format("%d is not between %d and %d", read, inclusiveMin, exclusiveMax));
        } else {
          result = read;
        }
      } catch (NumberFormatException e) {
        logger.error("Please, choose a valid number");
      }
    } while (result <= NOT_READ);
    return result;
  }

  public String inputString(ConsumerWithExceptions<String, IllegalArgumentException> readCustom) {
    String result = null;
    do {
      logger.info("Waiting for user input...");
      String read = scanner.readString();
      try {
        readCustom.accept(read);
        result = read;
      } catch (IllegalArgumentException e) {
        logger.error(String.format("Invalid custom entry: %s", e.getMessage()));
      }
    } while (result == null);
    return result;
  }

  public String inputFromStringListWithCustom(
      Set<String> choices, ConsumerWithExceptions<String, IllegalArgumentException> readCustom) {
    if (choices.isEmpty()) {
      return inputString(readCustom);
    } else {
      List<String> entries = new ArrayList<>(choices);
      int customEntryIndex = entries.size();
      entries.add(CUSTOM_ENTRY);
      int index = indexFromList(entries, Function.identity());
      if (index == customEntryIndex) {
        return inputString(readCustom);
      } else {
        return entries.get(index);
      }
    }
  }

  /**
   * User must choose an input from the list
   *
   * @param choices possible result, should never be empty
   * @param show used to display a choice
   */
  public <T> T inputFromList(Set<T> choices, Function<T, String> show) {
    List<T> entries = new ArrayList<>(choices);
    return entries.get(indexFromList(entries, show));
  }

  /**
   * Display choices and allow an optional custom entry
   *
   * @param entries user choices, should never be empty
   * @param show used to display a choice
   * @return chosen index
   */
  private <T> int indexFromList(List<T> entries, Function<T, String> show) {
    if (entries.isEmpty()) {
      throw new IllegalArgumentException("Choices set is empty");
    } else if (entries.size() == 1) {
      T choice = entries.get(0);
      logger.info(
          show.apply(choice) + " has been choosen by default as it's the only available choice");
      return 0;
    } else {
      logger.info("Type the number corresponding to your choice and press enter");
      int entriesSize = entries.size();
      IntStream.range(0, entriesSize)
          .forEach(
              index ->
                  logger.info(String.format("[%d] %s", index, show.apply(entries.get(index)))));
      return inputInt(0, entriesSize);
    }
  }
}
