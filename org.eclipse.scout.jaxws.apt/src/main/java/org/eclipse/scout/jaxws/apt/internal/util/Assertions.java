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
 * Similar to {@link org.eclipse.scout.rt.platform.util.Assertions}, but with no usage of SLF4J Message formatter.
 *
 * @TODO [7.0] abr: Remove this class when building with Maven newer than 3.3.3. In older Maven versions, there is a
 * bug, that SLF4J classes are not found. See https://issues.apache.org/jira/browse/MNG-5842.
 */
public final class Assertions {

  private Assertions() {
  }

  /**
   * @see org.eclipse.scout.rt.platform.util.Assertions#assertNotNullOrEmpty(String, String, Object...)
   */
  public static <T> T assertNotNull(final T value, final String msg, final Object... msgArgs) {
    if (value == null) {
      fail(msg, msgArgs);
    }
    return value;
  }

  /**
   * @see org.eclipse.scout.rt.platform.util.Assertions#fail(String, Object...)
   */
  public static <T> T fail(final String msg, final Object... msgArgs) {
    throw new IllegalArgumentException(SLF4JMessageFormatter.format(msg, msgArgs).getMessage());
  }
}
