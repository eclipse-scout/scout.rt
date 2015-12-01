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
package org.eclipse.scout.rt.platform.job.filter.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.filter.AlwaysFilter;
import org.eclipse.scout.rt.platform.filter.AndFilter;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.filter.NotFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IMutex;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.filter.future.ExecutionHintFutureFilter;
import org.eclipse.scout.rt.platform.job.filter.future.FutureFilter;
import org.eclipse.scout.rt.platform.job.filter.future.JobNameFutureFilter;
import org.eclipse.scout.rt.platform.job.filter.future.JobNameRegexFutureFilter;
import org.eclipse.scout.rt.platform.job.filter.future.MutexFutureFilter;
import org.eclipse.scout.rt.platform.job.filter.future.PeriodicExecutionFutureFilter;
import org.eclipse.scout.rt.platform.job.filter.future.RunContextFutureFilter;
import org.eclipse.scout.rt.platform.job.filter.future.SingleExecutionFutureFilter;
import org.eclipse.scout.rt.platform.job.internal.JobListeners;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.util.IAdaptable;

/**
 * This builder facilitates the creation of a {@link JobEvent} filter to match multiple criteria joined by logical 'AND'
 * operation.
 *
 * @since 5.1
 */
@Bean
public class JobEventFilterBuilder {

  private final List<IFilter<JobEvent>> m_andFilters = new ArrayList<>();

  /**
   * Builds the filter based on the criteria as configured with this builder instance. Thereby, the filter criteria are
   * joined by logical 'AND' operation.
   */
  public IFilter<JobEvent> toFilter() {
    switch (m_andFilters.size()) {
      case 0:
        return new AlwaysFilter<>();
      case 1:
        return m_andFilters.get(0);
      default:
        // Use 'AdaptableAndFilter' instead of 'AndFilter' to help 'JobListeners' to reduce contention by registering job event listeners locally on the involved Futures.
        return new AdaptableAndFilter(m_andFilters);
    }
  }

  /**
   * To match all events where the given filter evaluates to <code>true</code>.
   */
  public JobEventFilterBuilder andMatch(final IFilter<JobEvent> filter) {
    m_andFilters.add(filter);
    return this;
  }

  /**
   * To match all events where the given filter does not apply, meaning evaluates to <code>false</code>.
   */
  public JobEventFilterBuilder andMatchNot(final IFilter<JobEvent> filter) {
    m_andFilters.add(new NotFilter<>(filter));
    return this;
  }

  /**
   * To match all events of the given event type.
   */
  public JobEventFilterBuilder andMatchEventType(final JobEventType... eventTypes) {
    andMatch(new JobEventFilter(eventTypes));
    return this;
  }

  /**
   * To match all events for jobs which belong to one of the given job names.
   */
  public JobEventFilterBuilder andMatchName(final String... names) {
    andMatch(new FutureFilterWrapperJobEventFilter(new JobNameFutureFilter(names)));
    return this;
  }

  /**
   * To match all events for jobs where the given regex matches their job name.
   */
  public JobEventFilterBuilder andMatchNameRegex(final Pattern regex) {
    andMatch(new FutureFilterWrapperJobEventFilter(new JobNameRegexFutureFilter(regex)));
    return this;
  }

  /**
   * To match all events for jobs which belong to one of the given Futures.
   */
  public JobEventFilterBuilder andMatchFuture(final IFuture<?>... futures) {
    andMatch(new FutureFilterWrapperJobEventFilter(new FutureFilter(futures)));
    return this;
  }

  /**
   * To match all events for jobs which belong to one of the given Futures.
   */
  public JobEventFilterBuilder andMatchFuture(final Collection<IFuture<?>> futures) {
    andMatch(new FutureFilterWrapperJobEventFilter(new FutureFilter(futures)));
    return this;
  }

  /**
   * To match all events for jobs which belong to one of the given Futures.
   */
  public <RESULT> JobEventFilterBuilder andMatchFuture(final List<IFuture<RESULT>> futures) {
    andMatch(new FutureFilterWrapperJobEventFilter(new FutureFilter(futures.toArray(new IFuture<?>[futures.size()]))));
    return this;
  }

  /**
   * To match all events for jobs which do not belong to any of the given Futures.
   */
  public JobEventFilterBuilder andMatchNotFuture(final IFuture<?>... futures) {
    andMatchNot(new FutureFilterWrapperJobEventFilter(new FutureFilter(futures)));
    return this;
  }

  /**
   * To match all events for jobs which do not belong to any of the given Futures.
   */
  public JobEventFilterBuilder andMatchNotFuture(final Collection<IFuture<?>> futures) {
    andMatchNot(new FutureFilterWrapperJobEventFilter(new FutureFilter(futures)));
    return this;
  }

  /**
   * To match all events for jobs which do not belong to any of the given Futures.
   */
  public <RESULT> JobEventFilterBuilder andMatchNotFuture(final List<IFuture<RESULT>> futures) {
    andMatchNot(new FutureFilterWrapperJobEventFilter(new FutureFilter(futures.toArray(new IFuture<?>[futures.size()]))));
    return this;
  }

  /**
   * To match all events for jobs which have the given mutex object set.
   */
  public JobEventFilterBuilder andMatchMutex(final IMutex mutex) {
    andMatch(new FutureFilterWrapperJobEventFilter(new MutexFutureFilter(mutex)));
    return this;
  }

  /**
   * To match all events related to single executing jobs.
   *
   * @see JobInput#SCHEDULING_RULE_SINGLE_EXECUTION
   */
  public JobEventFilterBuilder andAreSingleExecuting() {
    andMatch(new FutureFilterWrapperJobEventFilter(SingleExecutionFutureFilter.INSTANCE));
    return this;
  }

  /**
   * To match all events related to periodic executing jobs.
   *
   * @see JobInput#SCHEDULING_RULE_PERIODIC_EXECUTION_AT_FIXED_RATE
   * @see JobInput#SCHEDULING_RULE_PERIODIC_EXECUTION_WITH_FIXED_DELAY
   */
  public JobEventFilterBuilder andArePeriodicExecuting() {
    andMatch(new FutureFilterWrapperJobEventFilter(PeriodicExecutionFutureFilter.INSTANCE));
    return this;
  }

  /**
   * To match all events related to jobs running on behalf of the given {@link RunContext} type.
   */
  public JobEventFilterBuilder andMatchRunContext(final Class<? extends RunContext> runContextClazz) {
    andMatch(new FutureFilterWrapperJobEventFilter(new RunContextFutureFilter(runContextClazz)));
    return this;
  }

  /**
   * To match all events related to jobs tagged with the given execution hint.
   */
  public JobEventFilterBuilder andMatchExecutionHint(final String hint) {
    andMatch(new FutureFilterWrapperJobEventFilter(new ExecutionHintFutureFilter(hint)));
    return this;
  }

  /**
   * To match all events related to jobs not tagged with the given execution hint.
   */
  public JobEventFilterBuilder andMatchNotExecutionHint(final String hint) {
    andMatchNot(new FutureFilterWrapperJobEventFilter(new ExecutionHintFutureFilter(hint)));
    return this;
  }

  /**
   * {@link AndFilter} that is adaptable to 'IFuture[]'. That functionality is used by {@link JobListeners} to reduce
   * contention by registering job event listeners locally on the involved Futures.
   */
  protected static class AdaptableAndFilter extends AndFilter<JobEvent> implements IAdaptable {

    private IFuture<?>[] m_futureIntersection = null;

    public AdaptableAndFilter(final Collection<IFilter<JobEvent>> filters) {
      super(filters);
      m_futureIntersection = calculateFutureIntersection(filters);
    }

    /**
     * Resolves each filter's futures (if filter supports adaptable mechanism), and returns the intersection of all
     * filter's futures.
     */
    protected static IFuture<?>[] calculateFutureIntersection(final Collection<IFilter<JobEvent>> filters) {
      List<IFuture<?>> intersection = null;

      for (final IFilter<JobEvent> filter : filters) {
        // Check whether the filter supports the adaptable mechanism.
        if (!(filter instanceof IAdaptable)) {
          continue;
        }

        // Check whether the filter is adaptable to 'IFuture[].class'.
        final IFuture<?>[] futures = ((IAdaptable) filter).getAdapter(IFuture[].class);
        if (futures == null) {
          continue;
        }

        // Create the intersection with the previous intersection, or use this filter's futures as initial intersection.
        if (intersection == null) {
          intersection = new ArrayList<>(Arrays.asList(futures));
        }
        else {
          intersection.retainAll(Arrays.asList(futures));
        }

        // Break the loop once the intersection gets empty.
        if (intersection.isEmpty()) {
          break;
        }
      }

      if (intersection == null || intersection.isEmpty()) {
        return null;
      }

      return intersection.toArray(new IFuture<?>[intersection.size()]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(final Class<T> adapterType) {
      if (adapterType == IFuture[].class) {
        return (T) m_futureIntersection;
      }
      return null;
    }
  }
}
