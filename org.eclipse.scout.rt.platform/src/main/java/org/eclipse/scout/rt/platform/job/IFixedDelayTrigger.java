package org.eclipse.scout.rt.platform.job;

import org.quartz.Trigger;

/**
 * {@link Trigger} strategy to run a job periodically with a fixed delay between the termination of one execution and
 * the commencement of the next execution.
 *
 * @since 5.2
 */
public interface IFixedDelayTrigger extends Trigger {

  /**
   * Indicates to repeat infinitely, unless the end time of the trigger is reached.
   */
  long REPEAT_INDEFINITELY = -1;

  /**
   * Returns the maximal repetition count, after which it will be automatically deleted.
   */
  long getRepeatCount();

  /**
   * Returns the fixed delay between successive runs.
   */
  long getFixedDelay();

  /**
   * Computes the next fire time, and is to be invoked at the end of a round.
   *
   * @return <code>true</code> if the trigger may fire again, or else <code>false</code>.
   */
  boolean computeNextTriggerFireTime();
}
