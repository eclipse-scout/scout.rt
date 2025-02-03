/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Replace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to validate path strings. Slash and backslash are recognized as path segment delimiters. This
 * implementation does not allow any parent path segments ("..").
 * <p>
 * The path validation may be customized by {@link Replace replacing} the bean {@link PathValidator}.
 */
@Bean
public class PathValidator {

  private static final Logger LOG = LoggerFactory.getLogger(PathValidator.class);

  /**
   * Identifier for parent folders (one level up)
   */
  protected static final String PARENT_FOLDER_IDENTIFIER = "..";

  /**
   * path delimiters are slash AND backslash because e.g. ClassLoader or File APIs understand both
   */
  protected static final Pattern REGEX_FOLDER_SPLIT = Pattern.compile("\\\\|/");

  /**
   * Cached bean instance used for validation
   */
  protected static final LazyValue<PathValidator> VALIDATOR = new LazyValue<>(PathValidator.class);

  /**
   * No custom instance. Use {@link BEANS} class to get instances.
   */
  protected PathValidator() {
  }

  /**
   * Validates the specified path {@link String}.
   *
   * @param path
   *     The path that should be validated. May be {@code null}.
   * @return {@code true} if the path is valid. {@code false} otherwise.
   */
  public static boolean isValid(String path) {
    try {
      VALIDATOR.get().validate(path);
      return true;
    }
    catch (Exception e) {
      LOG.info("Path validation failed", e);
      return false;
    }
  }

  /**
   * Validates the specified path {@link String}.
   *
   * @param path
   *     The path that should be validated. May be {@code null}.
   * @throws Exception
   *     if the validation failed
   */
  public void validate(String path) {
    if (path == null || path.isEmpty()) {
      return;
    }

    String[] folders = REGEX_FOLDER_SPLIT.split(path);
    if (folders == null || folders.length < 1) {
      return;
    }

    for (String folder : folders) {
      if (PARENT_FOLDER_IDENTIFIER.equals(folder)) {
        throw new IllegalArgumentException("Invalid path: '" + path + "'. Parent paths are not allowed.");
      }
    }
  }
}
