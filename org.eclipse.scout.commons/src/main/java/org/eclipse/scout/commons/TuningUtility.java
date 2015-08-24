/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * <p>
 * Class to help getting timing information without the need of adding attaching a profiler.
 * </p>
 * <h2>Example 1</h2>
 *
 * <pre>
 * TuningUtility.startTimer();
 * // my code
 * // more of my code
 * TuningUtility.stopTimer(&quot;myCode&quot;);
 * </pre>
 *
 * results in the output
 *
 * <pre>
 * #TuningUtility myCode took 30ms
 * </pre>
 *
 * <h2>Example 2</h2>
 *
 * <pre>
 * for (int i = 0; i &lt; 100; i++) {
 *   TuningUtility.startTimer();
 *   // my repeated code A
 *   TuningUtility.stopTimer(&quot;repeatCodeA&quot;, false, true);
 *   TuningUtility.startTimer();
 *   // my repeated code B
 *   TuningUtility.stopTimer(&quot;repeatCodeB&quot;, false, true);
 * }
 * TuningUtility.finishAll();
 * </pre>
 *
 * results in the output
 *
 * <pre>
 * #TUNING: repeatCodeA[100] sum=1449.755488 min=1.941867ms avg=14.497554ms median=14.665551ms max=30.327598ms [without 1 smallest and 1 largest: sum=1417.486023 min=1.943543ms avg=14.613257ms median=14.665551ms max=30.265579ms]
 * #TUNING: repeatCodeB[100] sum=6893.764704 min=4.891404ms avg=68.937647ms median=68.354218ms max=143.545897ms [without 1 smallest and 1 largest: sum=6745.327403 min=7.807417ms avg=69.539457ms median=68.354218ms max=142.573987ms]
 * </pre>
 */
public final class TuningUtility {

  private TuningUtility() {
  }

  // current timer
  private static final Stack<Long> TIMER_STACK;
  // analysis
  private static final Object ANALYSIS_MAP_LOCK;
  private static final TreeMap<String, TreeSet<CompositeObject>> ANALYSIS_MAP;

  static {
    TIMER_STACK = new Stack<Long>();
    // analysis
    ANALYSIS_MAP_LOCK = new Object();
    ANALYSIS_MAP = new TreeMap<String, TreeSet<CompositeObject>>();
  }

  /**
   * Starts a timer by pushing the current time onto a stack.
   */
  public static void startTimer() {
    TIMER_STACK.push(System.nanoTime());
  }

  /**
   * <p>
   * Stops a timer and prints the result.
   * </p>
   * <p>
   * If no timer was started the output is suppressed.
   * </p>
   *
   * @param name
   *          of the timer, used in the output
   * @return time spent in nanoseconds, -1 if no timer was started
   */
  public static long stopTimer(String name) {
    return stopTimer(name, true, false);
  }

  /**
   * Stops a timer
   *
   * @param name
   *          of the timer, used to store the result for multiple measurements and for output
   * @param print
   *          <code>true</code> prints as output how much time was spent since the start, the output is also suppressed
   *          if there is not timer to stop
   * @param addToBatch
   *          <code>true</code> the measurement is stored internally
   * @return time spent in nanoseconds, -1 if no timer was started
   */
  public static long stopTimer(String name, boolean print, boolean addToBatch) {
    long dtNanos;
    if (!TIMER_STACK.isEmpty()) {
      dtNanos = System.nanoTime() - TIMER_STACK.pop();
    }
    else {
      dtNanos = -1;
    }
    if (print && dtNanos != -1) {
      printSingle(name, dtNanos);
    }
    if (addToBatch) {
      synchronized (ANALYSIS_MAP_LOCK) {
        TreeSet<CompositeObject> set = ANALYSIS_MAP.get(name);
        if (set == null) {
          set = new TreeSet<CompositeObject>();
          ANALYSIS_MAP.put(name, set);
        }
        set.add(new CompositeObject(dtNanos, set.size()));
      }
    }
    return dtNanos;
  }

  /**
   * print out a list of all timers that were stored (added to a batch)
   *
   * @param clearTimers
   *          <code>true</code> if any timers have not been stopped yet, they will be removed first. A warning message
   *          is still printed.<code>false</code> only a note will be printed, but unfinished timers will not be stopped
   */
  public static void finishAll(boolean clearTimers) {
    if (!TIMER_STACK.isEmpty()) {
      System.out.println("#TUNING: there are " + TIMER_STACK.size() + " non-finished timers (start/stop mismatch)");
    }
    while (clearTimers && !TIMER_STACK.isEmpty()) {
      TIMER_STACK.pop();
    }
    synchronized (ANALYSIS_MAP_LOCK) {
      for (Map.Entry<String, TreeSet<CompositeObject>> e : ANALYSIS_MAP.entrySet()) {
        String name = e.getKey();
        TreeSet<CompositeObject> set = e.getValue();
        long[] seriesSorted = new long[set.size()];
        int index = 0;
        for (CompositeObject o : set) {
          seriesSorted[index] = (Long) o.getComponent(0);
          index++;
        }
        printMulti(name, seriesSorted);
      }
      ANALYSIS_MAP.clear();
    }
  }

  /**
   * print out a list of all timers that were stored (added to a batch)
   */
  public static void finishAll() {
    finishAll(false);
  }

  private static void printSingle(String label, long dtNanos) {
    int level = TIMER_STACK.size();
    StringBuilder b = new StringBuilder();
    b.append("#TUNING: ");
    for (int i = 0; i < level; i++) {
      b.append("  ");
    }
    b.append(label);
    b.append(" took ");
    b.append(StringUtility.formatNanos(dtNanos));
    b.append("ms");
    System.out.println(b);
  }

  private static void printMulti(String name, long[] seriesSorted) {
    StringBuilder b = new StringBuilder();
    b.append("#TUNING: ");
    b.append(name);
    b.append("[" + seriesSorted.length + "]");
    if (seriesSorted.length > 0) {
      double sum = 0;
      for (long n : seriesSorted) {
        sum += n;
      }
      b.append(" sum=" + StringUtility.formatNanos((long) sum));
      long avg = (long) (sum / seriesSorted.length);
      b.append("ms");
      b.append(" min=");
      b.append(StringUtility.formatNanos(seriesSorted[0]));
      b.append("ms");
      b.append(" avg=");
      b.append(StringUtility.formatNanos(avg));
      b.append("ms");
      b.append(" median=");
      b.append(StringUtility.formatNanos(seriesSorted[seriesSorted.length / 2]));
      b.append("ms");
      b.append(" max=");
      b.append(StringUtility.formatNanos(seriesSorted[seriesSorted.length - 1]));
      b.append("ms");
      // remove smallest and largest 1%
      int start = Math.max(1, seriesSorted.length / 100);
      int end = Math.min(seriesSorted.length - 2, seriesSorted.length - 1 - seriesSorted.length / 100);
      if (start < end) {
        b.append("  [without " + start + " smallest and " + (seriesSorted.length - 1 - end) + " largest: ");
        sum = 0;
        for (int i = start; i <= end; i++) {
          sum += seriesSorted[i];
        }
        b.append(" sum=" + StringUtility.formatNanos((long) sum));
        avg = (long) (sum / (end - start));
        b.append(" min=");
        b.append(StringUtility.formatNanos(seriesSorted[start]));
        b.append("ms");
        b.append(" avg=");
        b.append(StringUtility.formatNanos(avg));
        b.append("ms");
        b.append(" median=");
        b.append(StringUtility.formatNanos(seriesSorted[seriesSorted.length / 2]));
        b.append("ms");
        b.append(" max=");
        b.append(StringUtility.formatNanos(seriesSorted[end]));
        b.append("ms");
        b.append("]");
      }
    }
    System.out.println(b);
  }
}
