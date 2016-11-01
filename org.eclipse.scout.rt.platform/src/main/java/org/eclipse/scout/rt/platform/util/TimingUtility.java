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
