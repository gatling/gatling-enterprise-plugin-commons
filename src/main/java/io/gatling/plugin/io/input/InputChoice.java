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

package io.gatling.plugin.io.input;

import io.gatling.plugin.io.PluginIO;
import io.gatling.plugin.io.PluginLogger;
import io.gatling.plugin.io.PluginScanner;
import io.gatling.plugin.io.input.exceptions.ValidationException;
import io.gatling.plugin.util.LambdaExceptionUtil.FunctionWithExceptions;
import io.gatling.plugin.util.show.Showable;
import io.gatling.plugin.util.show.ShowableString;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InputChoice {

  private final PluginLogger logger;
  private final PluginScanner scanner;
  private static final int NOT_READ = -1;

  public InputChoice(PluginIO pluginIO) {
    this.scanner = pluginIO.getScanner();
    this.logger = pluginIO.getLogger();
  }

  /**
   * Read a non negative number, inferior to max
   *
   * @param excludedMax maximum value of user entry
   */
  public int inputInt(int excludedMax) {
    int result = NOT_READ;
    do {
      try {
        int read = scanner.readInt();
        if (read < 0 || read >= excludedMax) {
          logger.error(String.format("%d is not between %d and %d", read, 0, excludedMax));
        } else {
          result = read;
        }
      } catch (NumberFormatException e) {
        logger.error("Please, choose a valid number");
      }
    } while (result <= NOT_READ);
    return result;
  }

  /** Read a non empty string from the user */
  public <T> T inputString(FunctionWithExceptions<String, T, ValidationException> readCustom) {
    T result = null;
    do {
      logger.info("Waiting for user input...");
      String read = scanner.readString();
      try {
        result = readCustom.apply(read);
      } catch (ValidationException e) {
        logger.error(String.format("Invalid custom entry: %s", e.getMessage()));
      }
    } while (result == null);
    return result;
  }

  public String inputFromStringListWithCustom(Set<String> choices) {
    return inputFromStringListWithCustom(choices, value -> value);
  }

  public String inputFromStringListWithCustom(
      Set<String> choices, FunctionWithExceptions<String, String, ValidationException> readCustom) {
    Set<ShowableString> showableChoices =
        choices.stream().map(ShowableString::new).collect(Collectors.toSet());
    if (choices.isEmpty()) {
      return inputString(readCustom);
    } else {

      return inputFromList(showableChoices, value -> new ShowableString(readCustom.apply(value)))
          .value;
    }
  }

  /**
   * User must choose an input from the list
   *
   * @param choices possible result, should never be empty
   */
  public <T extends Showable> T inputFromList(Set<T> choices) {
    return inputFromList(choices, null);
  }

  /**
   * Display choices and allow an optional custom entry
   *
   * @param choices user choices, should never be empty
   * @param readCustom Optional
   */
  private <T extends Showable> T inputFromList(
      Set<T> choices, FunctionWithExceptions<String, T, ValidationException> readCustom) {
    List<T> entries = new ArrayList<>(choices);
    if (entries.isEmpty()) {
      throw new IllegalArgumentException("Choices set is empty");
    } else if (entries.size() == 1 && readCustom == null) {
      T choice = entries.get(0);
      logger.info(
          String.format(
              "%s has been choosen by default as it's the only available choice", choice.show()));
      return choice;
    } else {
      logger.info("Type the number corresponding to your choice and press enter");
      int entriesSize = entries.size();
      IntStream.range(0, entriesSize)
          .forEach(
              index -> {
                logger.info(String.format("[%d] %s", index, entries.get(index).show()));
              });

      if (readCustom != null) {
        logger.info(String.format("[%d] Custom entry", entriesSize));
        int indexSelected = inputInt(entriesSize + 1);
        if (indexSelected == entriesSize) {
          return inputString(readCustom);
        } else {
          return entries.get(indexSelected);
        }
      } else {
        int indexSelected = inputInt(entriesSize);
        return entries.get(indexSelected);
      }
    }
  }
}
