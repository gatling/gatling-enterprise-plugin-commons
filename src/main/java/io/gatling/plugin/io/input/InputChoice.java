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

import io.gatling.plugin.exceptions.UserQuitException;
import io.gatling.plugin.io.PluginIO;
import io.gatling.plugin.io.PluginLogger;
import io.gatling.plugin.io.PluginScanner;
import io.gatling.plugin.util.LambdaExceptionUtil.ConsumerWithExceptions;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class InputChoice {

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
   * @param inclusiveMin inclusive minimum value of user entry
   */
  public int inputInt(int inclusiveMin) {
    return inputInt(inclusiveMin, Integer.MAX_VALUE);
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
              String.format("%d is not between %d and %d", read, inclusiveMin, exclusiveMax - 1));
        } else {
          result = read;
        }
      } catch (NumberFormatException e) {
        logger.error("Enter a valid number");
      }
    } while (result <= NOT_READ);
    return result;
  }

  public String inputString(ConsumerWithExceptions<String, IllegalArgumentException> validator) {
    return inputStringWithDefault(null, validator);
  }

  public String inputStringWithDefault(
      String defaultValue, ConsumerWithExceptions<String, IllegalArgumentException> validator) {
    while (true) {
      logger.info("Waiting for user input...");
      final String read = scanner.readString();
      if (read.isEmpty() && defaultValue != null) {
        return defaultValue;
      }
      try {
        validator.accept(read);
        return read;
      } catch (IllegalArgumentException e) {
        logger.error(e.getMessage());
      }
    }
  }

  /**
   * User must choose an input from the list
   *
   * @param choices possible result, should never be empty
   * @param show used to display a choice
   * @param orderBy optional, a comparator used for sorting the list of choices
   * @throws UserQuitException if the user chooses to cancel the operation
   */
  public <T> T inputFromList(List<T> choices, Function<T, String> show, Comparator<T> orderBy)
      throws UserQuitException {
    if (choices.isEmpty()) {
      throw new IllegalArgumentException("Choices list is empty");
    }
    final List<T> sortedChoices =
        orderBy != null ? choices.stream().sorted(orderBy).collect(Collectors.toList()) : choices;

    logger.info("Type the number corresponding to your choice and press enter");
    logger.info("[0] <Quit>");
    final int entriesSize = choices.size();
    IntStream.range(1, entriesSize + 1)
        .forEach(
            index ->
                logger.info(String.format("[%d] %s", index, show.apply(choices.get(index - 1)))));
    final int input = inputInt(0, entriesSize + 1);

    if (input == 0) {
      throw new UserQuitException();
    }
    return sortedChoices.get(input - 1);
  }

  public String inputFromStringList(List<String> choices, boolean sorted) throws UserQuitException {
    final Comparator<String> comparator = sorted ? Comparator.comparing(Function.identity()) : null;
    return inputFromList(choices, Function.identity(), comparator);
  }
}
