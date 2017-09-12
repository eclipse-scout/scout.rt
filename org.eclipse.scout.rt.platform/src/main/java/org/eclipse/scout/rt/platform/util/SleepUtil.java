/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to causes the currently executing thread to sleep.
 *
 * @since 5.2
 */
public final class SleepUtil {

  private static final Logger LOG = LoggerFactory.getLogger(SleepUtil.class);

  private SleepUtil() {
  }

  /**
   * Like {@link Thread#sleep(long)}, but does not throw {@link ThreadInterruptedError} If interrupted.
   * <p>
   * If interrupted, the thread's interruption status is not cleared.
   *
   * @param time
   *          the length of time to sleep.
   * @param unit
   *          unit of the timeout.
   */
  public static void sleepSafe(final long time, final TimeUnit unit) {
    try {
      Thread.sleep(unit.toMillis(time));
    }
    catch (final InterruptedException e) {
      restoreInterruptionStatus();
    }
  }

  /**
   * Like {@link Thread#sleep(long)}, but logs If interrupted.
   * <p>
   * If interrupted, the thread's interruption status is not cleared.
   *
   * @param time
   *          the length of time to sleep.
   * @param unit
   *          unit of the timeout.
   */
  public static void sleepElseLog(final long time, final TimeUnit unit) {
    sleepElseLog(time, unit, "Interrupted");
  }

  /**
   * Like {@link Thread#sleep(long)}, but logs the given log message If interrupted.
   * <p>
   * If interrupted, the thread's interruption status is not cleared.
   *
   * @param time
   *          the length of time to sleep.
   * @param unit
   *          unit of the timeout.
   * @param msg
   *          the log message with support for <em>formatting anchors</em> in the form of {} pairs.
   * @param msgArgs
   *          optional arguments to substitute <em>formatting anchors</em> in the message.
   * @throws ThreadInterruptedError
   *           if the sleeping thread is interrupted.
   */
  public static void sleepElseLog(final long time, final TimeUnit unit, final String msg, final Object... msgArgs) {
    try {
      Thread.sleep(unit.toMillis(time));
    }
    catch (final InterruptedException e) {
      restoreInterruptionStatus();

      final Object[] args = Arrays.copyOf(msgArgs, msgArgs.length + 1);
      args[args.length - 1] = e; // add the exception to the last index to be interpreted as cause
      LOG.info(msg, args);
    }
  }

  /**
   * Like {@link Thread#sleep(long)}, but throws Scout {@link ThreadInterruptedError} If interrupted.
   * <p>
   * If interrupted, the thread's interruption status is not cleared.
   *
   * @param time
   *          the length of time to sleep.
   * @param unit
   *          unit of the timeout.
   * @throws ThreadInterruptedError
   *           if the sleeping thread is interrupted.
   */
  public static void sleepElseThrow(final long time, final TimeUnit unit) {
    sleepElseThrow(time, unit, "Interrupted");
  }

  /**
   * Like {@link Thread#sleep(long)}, but throws Scout {@link ThreadInterruptedError} If interrupted.
   * <p>
   * If interrupted, the thread's interruption status is not cleared.
   *
   * @param time
   *          the length of time to sleep.
   * @param unit
   *          unit of the timeout.
   * @param msg
   *          the exception message with support for <em>formatting anchors</em> in the form of {} pairs.
   * @param msgArgs
   *          optional arguments to substitute <em>formatting anchors</em> in the message.
   * @throws ThreadInterruptedError
   *           if the sleeping thread is interrupted.
   */
  public static void sleepElseThrow(final long time, final TimeUnit unit, final String msg, final Object... msgArgs) {
    try {
      Thread.sleep(unit.toMillis(time));
    }
    catch (final InterruptedException e) {
      restoreInterruptionStatus();

      final Object[] args = Arrays.copyOf(msgArgs, msgArgs.length + 1);
      args[args.length - 1] = e; // add the exception to the last index to be interpreted as cause
      throw new ThreadInterruptedError(msg, args);
    }
  }

  /**
   * Restores the thread's interrupted status because cleared by catching {@link InterruptedException}.
   */
  private static void restoreInterruptionStatus() {
    Thread.currentThread().interrupt();
  }
}
