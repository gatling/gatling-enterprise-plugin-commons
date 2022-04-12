package io.gatling.plugin.exceptions;

import io.gatling.scanner.UnsupportedJavaMajorVersionException;

public class UnsupportedJavaVersionException extends EnterprisePluginException {

  public UnsupportedJavaVersionException(UnsupportedJavaMajorVersionException e) {
    super("Unsupported java version detected", e);
  }
}
