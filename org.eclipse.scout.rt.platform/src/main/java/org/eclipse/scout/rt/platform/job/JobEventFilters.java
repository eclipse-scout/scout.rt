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
package org.eclipse.scout.rt.platform.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.IAdaptable;
import org.eclipse.scout.commons.filter.AndFilter;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.commons.filter.NotFilter;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;

/**
 * Filters for job events.
 *
 * @since 5.1
 */
public final class JobEventFilters {

  private static final IFilter<JobEvent> PERIODIC_JOB_EVENT_FILTER = new FutureEventFilterDelegate(JobFutureFilters.PeriodicFilter.TRUE_INSTANCE);
  private static final IFilter<JobEvent> NON_PERIODIC_JOB_EVENT_FILTER = new FutureEventFilterDelegate(JobFutureFilters.PeriodicFilter.FALSE_INSTANCE);

  private JobEventFilters() {
  }

  /**
   * Filter to accept events of all jobs that comply with the given characteristics.<br/>
   * The 'setter-methods' returns <code>this</code> in order to support for method chaining.
   *
   * @since 5.1
   */
  public static class Filter implements IFilter<JobEvent>, IAdaptable {

    private final List<IFilter<JobEvent>> m_andFilters = new ArrayList<>();

    public Filter() {
      postConstruct();
    }

    @Override
    public boolean accept(final JobEvent event) {
      return (m_andFilters.isEmpty() ? true : new AndFilter<>(m_andFilters).accept(event));
    }

    /**
     * Method invoked after construction.
     */
    protected void postConstruct() {
    }

    /**
     * To match all events where the given filter evaluates to <code>true</code>.
     */
    public Filter andMatch(final IFilter<JobEvent> filter) {
      m_andFilters.add(filter);
      return this;
    }

    /**
     * To match all events of the given event types.
     */
    public Filter andMatchEventType(final JobEventType... eventTypes) {
      andMatch(new EventTypeFilter(eventTypes));
      return this;
    }

    /**
     * To match all events for jobs which belong to one of the given job names.
     */
    public Filter andMatchName(final String... names) {
      andMatch(new FutureEventFilterDelegate(new JobFutureFilters.JobNameFilter(names)));
      return this;
    }

    /**
     * To match all events for jobs where the given regex matches their job name.
     */
    public Filter andMatchNameRegex(final Pattern regex) {
      andMatch(new FutureEventFilterDelegate(new JobFutureFilters.JobNameRegexFilter(regex)));
      return this;
    }

    /**
     * To match all events which belong to the currently running job. The currently running job is defined as the job,
     * which the caller of this method is currently running in.
     */
    public Filter andMatchCurrentFuture() {
      andMatchFuture(IFuture.CURRENT.get());
      return this;
    }

    /**
     * To match all events which do not belong to the currently running job. The currently running job is defined as the
     * job, which the caller of this method is currently running in.
     */
    public Filter andMatchNotCurrentFuture() {
      andMatch(new FutureEventFilterDelegate(new NotFilter<>(new JobFutureFilters.FutureFilter(IFuture.CURRENT.get()))));
      return this;
    }

    /**
     * To match all events for jobs which belong to one of the given Futures.
     */
    public Filter andMatchFuture(final IFuture<?>... futures) {
      andMatchFuture(Arrays.asList(futures));
      return this;
    }

    /**
     * To match all events for jobs which belong to one of the given Futures.
     */
    public Filter andMatchFuture(final Collection<IFuture<?>> futures) {
      andMatch(new FutureEventFilterDelegate(new JobFutureFilters.FutureFilter(futures)));
      return this;
    }

    /**
     * To match all events for jobs with the given mutex set.
     */
    public Filter andMatchMutex(final Object mutexObject) {
      andMatch(new FutureEventFilterDelegate(new JobFutureFilters.MutexFilter(mutexObject)));
      return this;
    }

    /**
     * To match all events for periodic running jobs.
     *
     * @see IJobManager#scheduleWithFixedDelay()
     * @see IJobManager#scheduleAtFixedRate()
     */
    public Filter andArePeriodic() {
      andMatch(PERIODIC_JOB_EVENT_FILTER);
      return this;
    }

    /**
     * To match all events for not periodic running jobs (one-shot actions).
     *
     * @see IJobManager#schedule()
     */
    public Filter andAreNotPeriodic() {
      andMatch(NON_PERIODIC_JOB_EVENT_FILTER);
      return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(final Class<T> adapterType) {
      if (adapterType == IFuture[].class) {
        return (T) getFuturesAdapter();
      }
      return null;
    }

    protected IFuture[] getFuturesAdapter() {
      List<IFuture<?>> adapters = null;

      for (final IFilter<JobEvent> andFilter : m_andFilters) {
        // Check whether the current 'andFilter' supports the adaptable mechanism.
        if (!(andFilter instanceof IAdaptable)) {
          continue;
        }

        // Check whether the current 'andFilter' is adaptable to 'IFuture[].class'.
        final IFuture<?>[] futures = ((IAdaptable) andFilter).getAdapter(IFuture[].class);
        if (futures == null) {
          continue;
        }

        // Because these filters have an 'AND' semantic, only retain futures which are contained yet.
        if (adapters == null) {
          adapters = new ArrayList<>(Arrays.asList(futures));
        }
        else {
          adapters.retainAll(Arrays.asList(futures));
        }
      }

      if (adapters == null || adapters.isEmpty()) {
        return null;
      }

      return adapters.toArray(new IFuture[adapters.size()]);
    }
  }

  /**
   * Filter which only accepts events of the given types.
   *
   * @since 5.1
   */
  public static class EventTypeFilter implements IFilter<JobEvent> {

    private final Set<JobEventType> m_eventTypes;

    public EventTypeFilter(final JobEventType... eventTypes) {
      m_eventTypes = CollectionUtility.hashSet(eventTypes);
    }

    @Override
    public boolean accept(final JobEvent event) {
      return m_eventTypes.contains(event.getType());
    }
  }

  /**
   * Event filter for events associated with futures. Evaluates to <code>true</code>, if there is no Future associated
   * with the event.
   */
  public static class FutureEventFilterDelegate implements IFilter<JobEvent>, IAdaptable {

    private final IFilter<IFuture<?>> m_delegate;

    public FutureEventFilterDelegate(final IFilter<IFuture<?>> delegate) {
      m_delegate = delegate;
    }

    @Override
    public final boolean accept(final JobEvent event) {
      return (event.getFuture() != null ? m_delegate.accept(event.getFuture()) : true);
    }

    @Override
    public <T> T getAdapter(final Class<T> type) {
      if (m_delegate instanceof IAdaptable) {
        return ((IAdaptable) m_delegate).getAdapter(type);
      }
      return null;
    }
  }
}
