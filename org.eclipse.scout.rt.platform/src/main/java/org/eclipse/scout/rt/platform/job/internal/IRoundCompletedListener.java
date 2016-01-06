package org.eclipse.scout.rt.platform.job.internal;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * Listener notified once a round of a repetitive task completes.
 *
 * @since 5.2
 */
public interface IRoundCompletedListener {

  /**
   * Method invoked once a round of a repetitive task completes.
   */
  void onRoundCompleted(Scheduler quartz) throws SchedulerException;
}
