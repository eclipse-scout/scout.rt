package org.eclipse.scout.rt.platform.job.internal;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

/**
 * Quartz {@link JobFactory} to access Beans via {@link IBeanManager}.
 *
 * @since 5.2
 */
class QuartzJobBeanFactory implements JobFactory {

  @Override
  public Job newJob(final TriggerFiredBundle bundle, final Scheduler scheduler) throws SchedulerException {
    try {
      return BEANS.get(bundle.getJobDetail().getJobClass());
    }
    catch (final RuntimeException e) {
      throw new SchedulerException(String.format("Failed to produce Quartz job [class=%s]", bundle.getJobDetail().getJobClass().getName()), e);
    }
  }
}
