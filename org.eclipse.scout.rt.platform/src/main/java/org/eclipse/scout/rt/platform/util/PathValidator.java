/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import java.util.concurrent.Callable;
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
  protected static final FinalValue<PathValidator> VALIDATOR = new FinalValue<>();

  /**
   * Validates the specified path {@link String}.
   *
   * @param path
   *          The path that should be validated. May be {@code null}.
   * @return {@code true} if the path is valid. {@code false} otherwise.
   */
  public static boolean isValid(String path) {
    try {
      getValidator().validate(path);
      return true;
    }
    catch (Exception e) {
      LOG.info("Path validation failed", e);
      return false;
    }
  }

  protected static PathValidator getValidator() {
    return VALIDATOR.setIfAbsent(new Callable<PathValidator>() {
      @Override
      public PathValidator call() {
        return BEANS.get(PathValidator.class);
      }
    });
  }

  /**
   * Validates the specified path {@link String}.
   *
   * @param path
   *          The path that should be validated. May be {@code null}.
   * @throws Exception
   *           if the validation failed
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
