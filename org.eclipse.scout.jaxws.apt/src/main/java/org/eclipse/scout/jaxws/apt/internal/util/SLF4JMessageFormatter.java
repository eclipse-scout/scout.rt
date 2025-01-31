/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.jaxws.apt.internal.util;

/**
 * Formatter according to SLF4J MessageFormatter.
 *
 * @TODO [7.0] abr: Remove this class when building with Maven newer than 3.3.3. In older Maven versions, there is a
 * bug, that SLF4J classes are not found. See https://issues.apache.org/jira/browse/MNG-5842.
 */
public final class SLF4JMessageFormatter {

  private SLF4JMessageFormatter() {
    // private constructor for utility classes
  }

  public static FormattingTuple format(final String msg, final Object... args) {
    if (args.length == 0) {
      return new FormattingTuple(msg);
    }

    final FormattingTuple format = new FormattingTuple(msg == null ? null : String.format(msg.replaceAll("\\{\\}", "%s"), args));
    final Object lastArg = args[args.length - 1];
    if (lastArg instanceof Throwable) {
      format.setThrowable((Throwable) lastArg);
    }

    return format;
  }

  /**
   * Same as {@link org.slf4j.helpers.FormattingTuple}.
   */
  public static class FormattingTuple {

    private final String m_message;
    private Throwable m_throwable;

    public FormattingTuple(final String message) {
      m_message = message;
    }

    public String getMessage() {
      return m_message;
    }

    public void setThrowable(final Throwable throwable) {
      m_throwable = throwable;
    }

    public Throwable getThrowable() {
      return m_throwable;
    }
  }
}
