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
   * Filter to accept events of all client- and model jobs that comply with the given characteristics.<br/>
   * The 'setter-methods' returns <code>this</code> in order to support for method chaining.
   *
   * @since 5.1
   */
  public static class Filter extends org.eclipse.scout.rt.platform.job.JobEventFilters.Filter {

    @Override
    protected void postConstruct() {
      andFilter(new OrFilter<>(CLIENT_JOB_EVENT_FILTER, MODEL_JOB_EVENT_FILTER));
    }

    @Override
    public Filter andFilter(final IFilter<JobEvent> filter) {
      return (Filter) super.andFilter(filter);
    }

    @Override
    public Filter eventTypes(final JobEventType... eventTypes) {
      return (Filter) super.eventTypes(eventTypes);
    }

    @Override
    public Filter names(final String... names) {
      return (Filter) super.names(names);
    }

    @Override
    public Filter nameRegex(final Pattern regex) {
      return (Filter) super.nameRegex(regex);
    }

    @Override
    public Filter futures(final IFuture<?>... futures) {
      return (Filter) super.futures(futures);
    }

    @Override
    public Filter futures(final Collection<IFuture<?>> futures) {
      return (Filter) super.futures(futures);
    }

    @Override
    public Filter currentFuture() {
      return (Filter) super.currentFuture();
    }

    @Override
    public Filter notCurrentFuture() {
      return (Filter) super.notCurrentFuture();
    }

    @Override
    public Filter periodic() {
      return (Filter) super.periodic();
    }

    @Override
    public Filter notPeriodic() {
      return (Filter) super.notPeriodic();
    }

    @Override
    public Filter mutex(final Object mutexObject) {
      return (Filter) super.mutex(mutexObject);
    }

    /**
     * To accept only events for jobs which are run on behalf of the given client session.
     */
    public Filter session(final IClientSession session) {
      andFilter(new FutureEventFilterDelegate(new ClientJobFutureFilters.SessionFilter(session)));
      return this;
    }

    /**
     * To accept only events for jobs which are run on behalf of the current client session.
     *
     * @see ISession#CURRENT
     */
    public Filter currentSession() {
      andFilter(new FutureEventFilterDelegate(new ClientJobFutureFilters.SessionFilter(ISession.CURRENT.get())));
      return this;
    }

    /**
     * To accept only events for jobs which are not run on behalf of the current server session.
     *
     * @see ISession#CURRENT
     */
    public Filter notCurrentSession() {
      andFilter(new FutureEventFilterDelegate(new NotFilter<>(new ClientJobFutureFilters.SessionFilter(ISession.CURRENT.get()))));
      return this;
    }

    /**
     * To accept only events for model jobs, and not client jobs.
     */
    public Filter modelJobsOnly() {
      andFilter(MODEL_JOB_EVENT_FILTER);
      return this;
    }

    /**
     * To accept only events for client jobs, and not model jobs.
     */
    public Filter clientJobsOnly() {
      andFilter(CLIENT_JOB_EVENT_FILTER);
      return this;
    }
  }
}
