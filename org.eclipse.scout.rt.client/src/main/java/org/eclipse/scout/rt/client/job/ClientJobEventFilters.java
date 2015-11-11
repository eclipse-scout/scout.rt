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
package org.eclipse.scout.rt.client.job;

import java.util.Collection;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.commons.filter.NotFilter;
import org.eclipse.scout.commons.filter.OrFilter;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.ClientJobFutureFilters.ClientJobFilter;
import org.eclipse.scout.rt.client.job.ClientJobFutureFilters.ModelJobFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobEventFilters.FutureEventFilterDelegate;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.shared.ISession;

/**
 * Filters for client-/model job events.
 *
 * @since 5.1
 */
public final class ClientJobEventFilters {

  /**
   * Filter to accept events only of client jobs, and not model jobs.
   */
  static final IFilter<JobEvent> CLIENT_JOB_EVENT_FILTER = new FutureEventFilterDelegate(ClientJobFilter.INSTANCE);

  /**
   * Filter to accept events only of model jobs, and not client jobs.
   */
  static final IFilter<JobEvent> MODEL_JOB_EVENT_FILTER = new FutureEventFilterDelegate(ModelJobFilter.INSTANCE);

  private ClientJobEventFilters() {
  }

  /**
   * Filter to accept events of all client- and model jobs that comply with the given characteristics.<br>
   * The 'setter-methods' returns <code>this</code> in order to support for method chaining.
   *
   * @since 5.1
   */
  public static class Filter extends org.eclipse.scout.rt.platform.job.JobEventFilters.Filter {

    @Override
    protected void postConstruct() {
      andMatch(new OrFilter<>(CLIENT_JOB_EVENT_FILTER, MODEL_JOB_EVENT_FILTER));
    }

    @Override
    public Filter andMatch(final IFilter<JobEvent> filter) {
      return (Filter) super.andMatch(filter);
    }

    @Override
    public Filter andMatchEventType(final JobEventType... eventTypes) {
      return (Filter) super.andMatchEventType(eventTypes);
    }

    @Override
    public Filter andMatchName(final String... names) {
      return (Filter) super.andMatchName(names);
    }

    @Override
    public Filter andMatchNameRegex(final Pattern regex) {
      return (Filter) super.andMatchNameRegex(regex);
    }

    @Override
    public Filter andMatchFuture(final IFuture<?>... futures) {
      return (Filter) super.andMatchFuture(futures);
    }

    @Override
    public Filter andMatchFuture(final Collection<IFuture<?>> futures) {
      return (Filter) super.andMatchFuture(futures);
    }

    @Override
    public Filter andMatchCurrentFuture() {
      return (Filter) super.andMatchCurrentFuture();
    }

    @Override
    public Filter andMatchNotCurrentFuture() {
      return (Filter) super.andMatchNotCurrentFuture();
    }

    @Override
    public Filter andMatchMutex(final Object mutexObject) {
      return (Filter) super.andMatchMutex(mutexObject);
    }

    /**
     * To accept only events for jobs which are run on behalf of the given client session.
     */
    public Filter andMatchSession(final IClientSession session) {
      andMatch(new FutureEventFilterDelegate(new ClientJobFutureFilters.SessionFilter(session)));
      return this;
    }

    /**
     * To accept only events for jobs which are run on behalf of the current client session.
     *
     * @see ISession#CURRENT
     */
    public Filter andMatchCurrentSession() {
      andMatch(new FutureEventFilterDelegate(new ClientJobFutureFilters.SessionFilter(ISession.CURRENT.get())));
      return this;
    }

    /**
     * To accept only events for jobs which are not run on behalf of the current server session.
     *
     * @see ISession#CURRENT
     */
    public Filter andMatchNotCurrentSession() {
      andMatch(new FutureEventFilterDelegate(new NotFilter<>(new ClientJobFutureFilters.SessionFilter(ISession.CURRENT.get()))));
      return this;
    }

    /**
     * To accept only events for model jobs, and not client jobs.
     */
    public Filter andAreModelJobs() {
      andMatch(MODEL_JOB_EVENT_FILTER);
      return this;
    }

    /**
     * To accept only events for client jobs, and not model jobs.
     */
    public Filter andAreClientJobs() {
      andMatch(CLIENT_JOB_EVENT_FILTER);
      return this;
    }

    @Override
    public Filter andArePeriodic() {
      return (Filter) super.andArePeriodic();
    }

    @Override
    public Filter andAreNotPeriodic() {
      return (Filter) super.andAreNotPeriodic();
    }
  }
}
