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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class InputStreamUtils {
  public static String inputStreamToString(InputStream inputStream, Charset charset)
      throws IOException {
    return inputStreamToString(inputStream, charset.name());
  }

  public static String inputStreamToString(InputStream inputStream, String charset)
      throws IOException {
    try (ByteArrayOutputStream result = inputStreamToByteArrayOutputStream(inputStream)) {
      return result.toString(charset);
    }
  }

  public static ByteArrayOutputStream inputStreamToByteArrayOutputStream(InputStream inputStream)
      throws IOException {
    final ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;
    while ((length = inputStream.read(buffer)) != -1) {
      result.write(buffer, 0, length);
    }
    return result;
  }
}
