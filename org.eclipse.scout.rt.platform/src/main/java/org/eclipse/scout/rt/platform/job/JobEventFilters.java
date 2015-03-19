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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.filter.AndFilter;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;

/**
 * Factory methods to create event filters related to jobs.
 *
 * @since 5.1
 */
public final class JobEventFilters {

  private static final IFilter<JobEvent> PERIODIC_JOB_EVENT_FILTER = new FutureEventFilterDelegate(JobFutureFilters.PeriodicFilter.TRUE_INSTANCE);
  private static final IFilter<JobEvent> NON_PERIODIC_JOB_EVENT_FILTER = new FutureEventFilterDelegate(JobFutureFilters.PeriodicFilter.FALSE_INSTANCE);

  private JobEventFilters() {
  }

  /**
   * Creates a filter to accept events of all jobs that comply with some specific characteristics. By default, the
   * filter returned accepts all events. The filter is designed to support method chaining.
   */
  public static Filter allFilter() {
    return new Filter();
  }

  /**
   * Filter to accept events of all jobs that comply with the given characteristics.<br/>
   * The 'setter-methods' returns <code>this</code> in order to support for method chaining.
   *
   * @since 5.1
   */
  public static class Filter implements IFilter<JobEvent> {

    private final List<IFilter<JobEvent>> m_filters = new ArrayList<>();

    public Filter() {
      postConstruct();
    }

    @Override
    public boolean accept(final JobEvent event) {
      return (m_filters.isEmpty() ? true : new AndFilter<>(m_filters).accept(event));
    }

    /**
     * Method invoked after construction.
     */
    protected void postConstruct() {
    }

    /**
     * Registers the given filter to further constrain the events to be accepted.
     */
    public Filter andFilter(final IFilter<JobEvent> filter) {
      m_filters.add(filter);
      return this;
    }

    /**
     * To accept only events of the given event types.
     */
    public Filter eventTypes(final JobEventType... eventTypes) {
      andFilter(new EventTypeFilter(eventTypes));
      return this;
    }

    /**
     * To accept only events which belong to jobs of the given job id's.
     */
    public Filter ids(final String... ids) {
      andFilter(new FutureEventFilterDelegate(new JobFutureFilters.JobIdFilter(ids)));
      return this;
    }

    /**
     * To accept only events which belong to the given Futures.
     */
    public Filter futures(final IFuture<?>... futures) {
      andFilter(new FutureEventFilterDelegate(new JobFutureFilters.FutureFilter(futures)));
      return this;
    }

    /**
     * To accept only events which belong to the given Futures.
     */
    public Filter futures(final Collection<IFuture<?>> futures) {
      andFilter(new FutureEventFilterDelegate(new JobFutureFilters.FutureFilter(futures)));
      return this;
    }

    /**
     * To accept only events for jobs which are executed periodically.
     *
     * @see IJobManager#scheduleWithFixedDelay()
     * @see IJobManager#scheduleAtFixedRate()
     */
    public Filter periodic() {
      andFilter(PERIODIC_JOB_EVENT_FILTER);
      return this;
    }

    /**
     * To accept only events for jobs which are executed once.
     *
     * @see IJobManager#schedule()
     */
    public Filter notPeriodic() {
      andFilter(NON_PERIODIC_JOB_EVENT_FILTER);
      return this;
    }

    /**
     * To accept only events for jobs which belong to the given mutex object.
     */
    public Filter mutex(final Object mutexObject) {
      andFilter(new FutureEventFilterDelegate(new JobFutureFilters.MutexFilter(mutexObject)));
      return this;
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
   * Passes the event's Future to the given delegate to be evaluated. Evaluates to <code>true</code>, if there is no
   * Future associated with the event.
   */
  public static class FutureEventFilterDelegate implements IFilter<JobEvent> {

    private final IFilter<IFuture<?>> m_delegate;

    public FutureEventFilterDelegate(final IFilter<IFuture<?>> delegate) {
      m_delegate = delegate;
    }

    @Override
    public final boolean accept(final JobEvent event) {
      return (event.getFuture() != null ? m_delegate.accept(event.getFuture()) : true);
    }
  }
}
