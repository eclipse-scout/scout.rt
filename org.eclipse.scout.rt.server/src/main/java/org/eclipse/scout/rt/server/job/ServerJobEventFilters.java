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
package org.eclipse.scout.rt.server.job;

import java.util.Collection;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.commons.filter.NotFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobEventFilters.FutureEventFilterDelegate;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.shared.ISession;

/**
 * Filters for server job events.
 *
 * @since 5.1
 */
public final class ServerJobEventFilters {

  private static final IFilter<JobEvent> SERVER_JOB_EVENT_FILTER = new FutureEventFilterDelegate(ServerJobFutureFilters.ServerJobFilter.INSTANCE);

  private ServerJobEventFilters() {
  }

  /**
   * Filter to accept events of all server jobs that comply with the given characteristics.<br/>
   * The 'setter-methods' returns <code>this</code> in order to support for method chaining.
   *
   * @since 5.1
   */
  public static class Filter extends org.eclipse.scout.rt.platform.job.JobEventFilters.Filter {

    @Override
    protected void postConstruct() {
      andMatch(SERVER_JOB_EVENT_FILTER);
    }

    @Override
    public Filter andMatch(final IFilter<JobEvent> filter) {
      return (Filter) super.andMatch(filter);
    }

    @Override
    public Filter andMatchEventTypes(final JobEventType... eventTypes) {
      return (Filter) super.andMatchEventTypes(eventTypes);
    }

    @Override
    public Filter andMatchNames(final String... names) {
      return (Filter) super.andMatchNames(names);
    }

    @Override
    public Filter andMatchNameRegex(final Pattern regex) {
      return (Filter) super.andMatchNameRegex(regex);
    }

    @Override
    public Filter andMatchFutures(final IFuture<?>... futures) {
      return (Filter) super.andMatchFutures(futures);
    }

    @Override
    public Filter andMatchFutures(final Collection<IFuture<?>> futures) {
      return (Filter) super.andMatchFutures(futures);
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
     * To accept only events for jobs which are run on behalf of the given server session.
     */
    public Filter andMatchSession(final IServerSession session) {
      andMatch(new FutureEventFilterDelegate(new ServerJobFutureFilters.SessionFilter(session)));
      return this;
    }

    /**
     * To accept only events for jobs which are run on behalf of the current server session.
     *
     * @see ISession#CURRENT
     */
    public Filter andMatchCurrentSession() {
      andMatch(new FutureEventFilterDelegate(new ServerJobFutureFilters.SessionFilter(ISession.CURRENT.get())));
      return this;
    }

    /**
     * To accept only events for jobs which are not run on behalf of the current server session.
     *
     * @see ISession#CURRENT
     */
    public Filter andMatchNotCurrentSession() {
      andMatch(new FutureEventFilterDelegate(new NotFilter<>(new ServerJobFutureFilters.SessionFilter(ISession.CURRENT.get()))));
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
