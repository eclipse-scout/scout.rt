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

import java.util.concurrent.TimeUnit;

/**
 * Convenience methods to measure elapsed time.
 */
public final class TimingUtility {

  private TimingUtility() {
  }

  public static long msElapsed(long sinceSysNano) {
    return nanosToMs(nanosElapsed(sinceSysNano));
  }

  public static long msElapsed(long fromNanos, long toNanos) {
    return nanosToMs(nanosElapsed(fromNanos, toNanos));
  }

  public static long nanosElapsed(long sinceSysNano) {
    return nanosElapsed(sinceSysNano, System.nanoTime());
  }

  public static long nanosElapsed(long from, long to) {
    return to - from;
  }

  public static long nanosToMs(long nanos) {
    return TimeUnit.NANOSECONDS.toMillis(nanos);
  }
}
