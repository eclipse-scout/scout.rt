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
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.shared.ISession;

/**
 * Filters for client-/model job Futures.
 *
 * @since 5.1
 */
public final class ClientJobFutureFilters {

  private ClientJobFutureFilters() {
  }

  /**
   * Filter to accept Futures of all client- and model jobs that comply with the given characteristics.<br/>
   * The 'setter-methods' returns <code>this</code> in order to support for method chaining.
   *
   * @since 5.1
   */
  public static class Filter extends org.eclipse.scout.rt.platform.job.JobFutureFilters.Filter {

    @Override
    protected void postConstruct() {
      andMatch(new OrFilter<>(ClientJobFilter.INSTANCE, ModelJobFilter.INSTANCE));
    }

    @Override
    public Filter andMatch(final IFilter<IFuture<?>> filter) {
      return (Filter) super.andMatch(filter);
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
     * To accept only jobs which are run on behalf of the given client session.
     */
    public Filter andMatchSession(final IClientSession session) {
      andMatch(new SessionFilter(session));
      return this;
    }

    /**
     * To accept only jobs which are run on behalf of the current client session.
     *
     * @see ISession#CURRENT
     */
    public Filter andMatchCurrentSession() {
      andMatch(new SessionFilter(ISession.CURRENT.get()));
      return this;
    }

    /**
     * To accept only jobs which are not run on behalf of the current client session.
     *
     * @see ISession#CURRENT
     */
    public Filter andMatchNotCurrentSession() {
      andMatch(new NotFilter<>(new SessionFilter(ISession.CURRENT.get())));
      return this;
    }

    @Override
    public Filter andAreBlocked() {
      return (Filter) super.andAreBlocked();
    }

    @Override
    public Filter andAreNotBlocked() {
      return (Filter) super.andAreNotBlocked();
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

  /**
   * Filter which accepts Futures only if belonging to the given client session.
   *
   * @since 5.1
   */
  public static class SessionFilter implements IFilter<IFuture<?>> {

    private final ISession m_session;

    public SessionFilter(final ISession session) {
      m_session = session;
    }

    @Override
    public boolean accept(final IFuture<?> future) {
      final RunContext runContext = future.getJobInput().runContext();
      if (runContext instanceof ClientRunContext) {
        return m_session == ((ClientRunContext) runContext).session();
      }
      else {
        return false;
      }
    }
  }

  /**
   * Filter which accepts only Futures from client jobs.
   *
   * @since 5.1
   */
  public static class ClientJobFilter implements IFilter<IFuture<?>> {

    public static final IFilter<IFuture<?>> INSTANCE = new ClientJobFilter();

    private ClientJobFilter() {
    }

    @Override
    public boolean accept(final IFuture<?> future) {
      return ClientJobs.isClientJob(future);
    }
  }

  /**
   * Filter which accepts only Futures from model jobs.
   *
   * @since 5.1
   */
  public static class ModelJobFilter implements IFilter<IFuture<?>> {

    public static final IFilter<IFuture<?>> INSTANCE = new ModelJobFilter();

    private ModelJobFilter() {
    }

    @Override
    public boolean accept(final IFuture<?> future) {
      return ModelJobs.isModelJob(future);
    }
  }
}
