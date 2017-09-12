package org.eclipse.scout.rt.platform.util.concurrent;

import java.io.OutputStream;

/**
 * Facilitates temporary clearance of the thread's interrupted status.
 * <p>
 * For instance, the thread's interrupted status requires to be cleared before writing to some {@link OutputStream}
 * instances.
 *
 * @since 6.0
 */
public final class ThreadInterruption {

  private ThreadInterruption() {
  }

  /**
   * Clears the current thread's interrupted status if applicable.
   * <p>
   * Use {@link IRestorer} returned by this method to restore the interruption status.
   */
  public static IRestorer clear() {
    final boolean interrupted = Thread.interrupted(); // clears and returns the current interruption status

    return () -> {
      if (interrupted) {
        Thread.currentThread().interrupt();
      }
    };
  }

  /**
   * Handle to restore the thread's interrupted status.
   */
  @FunctionalInterface
  public interface IRestorer {

    /**
     * Interrupts the current thread if has already been interrupted before calling {@link ThreadInterruption#clear()}.
     */
    void restore();
  }
}
