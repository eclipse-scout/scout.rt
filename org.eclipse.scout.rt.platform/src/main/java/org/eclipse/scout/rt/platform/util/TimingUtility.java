/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
