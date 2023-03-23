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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;

/**
 * Various utility methods dealing with {@link Throwable} and {@link Exception}.
 */
public final class ExceptionUtility {

  private static final Pattern STACK_TRACE_PATTERN = Pattern.compile("^\\s+at\\s[a-zA-Z0-9-_$.]+\\(.*:\\d+\\)$", Pattern.MULTILINE);

  private ExceptionUtility() {
  }

  /**
   * @return Root cause {@link Throwable} for given {@code throwable}.
   */
  public static Throwable getRootCause(Throwable throwable) {
    if (throwable == null) {
      return null;
    }

    Throwable cause = throwable;
    while (cause.getCause() != null) {
      cause = cause.getCause();
    }
    return cause;
  }

  /**
   * @return {@code true} if given {@code string} contains an {@link Exception} stack trace, {@code false} otherwise.
   */
  public static boolean containsStacktrace(String string) {
    return string != null && STACK_TRACE_PATTERN.matcher(string).find();
  }

  /**
   * @return String containing given {@code throwable} including its stack trace.
   */
  public static String getText(Throwable throwable) {
    if (throwable == null) {
      return null;
    }
    StringWriter stringWriter = new StringWriter();
    try (PrintWriter pw = new PrintWriter(stringWriter)) {
      throwable.printStackTrace(pw);
      return stringWriter.toString();
    }
  }
}
