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
import java.util.regex.Pattern;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.IAdaptable;
import org.eclipse.scout.commons.filter.AndFilter;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.commons.filter.NotFilter;

/**
 * Filters for job Futures.
 *
 * @since 5.1
 */

public final class JobFutureFilters {
  private JobFutureFilters() {
  }

  /**
   * Filter to accept Futures of all jobs that comply with the given characteristics.<br/>
   * The 'setter-methods' returns <code>this</code> in order to support for method chaining.
   *
   * @since 5.1
   */
  public static class Filter implements IFilter<IFuture<?>> {

    private final List<IFilter<IFuture<?>>> m_filters = new ArrayList<>();

    public Filter() {
      postConstruct();
    }

    @Override
    public boolean accept(final IFuture<?> future) {
      return (m_filters.isEmpty() ? true : new AndFilter<>(m_filters).accept(future));
    }

    /**
     * Method invoked after construction.
     */
    protected void postConstruct() {
    }

    /**
     * To match all jobs where the given filter evaluates to <code>true</code>.
     */
    public Filter andMatch(final IFilter<IFuture<?>> filter) {
      m_filters.add(filter);
      return this;
    }

    /**
     * To match all jobs of the given names.
     */
    public Filter andMatchName(final String... names) {
      andMatch(new JobNameFilter(names));
      return this;
    }

    /**
     * To match all jobs where the given regex matches their name.
     */
    public Filter andMatchNameRegex(final Pattern regex) {
      andMatch(new JobNameRegexFilter(regex));
      return this;
    }

    /**
     * To match all jobs which are represented by the given Futures.
     */
    public Filter andMatchFuture(final IFuture<?>... futures) {
      andMatch(new FutureFilter(futures));
      return this;
    }

    /**
     * To match all jobs which are represented by the given Futures.
     */
    public Filter andMatchFuture(final Collection<IFuture<?>> futures) {
      andMatch(new FutureFilter(futures));
      return this;
    }

    /**
     * To match the currently running job. The currently running job is defined as the job, which the caller of this
     * method is currently running in.
     */
    public Filter andMatchCurrentFuture() {
      andMatch(new FutureFilter(IFuture.CURRENT.get()));
      return this;
    }

    /**
     * To not match the currently running job. The currently running job is defined as the job, which the caller of this
     * method is currently running in.
     */
    public Filter andMatchNotCurrentFuture() {
      andMatch(new NotFilter<>(new FutureFilter(IFuture.CURRENT.get())));
      return this;
    }

    /**
     * To match all jobs with the given mutex set.
     */
    public Filter andMatchMutex(final Object mutexObject) {
      andMatch(new MutexFilter(mutexObject));
      return this;
    }

    /**
     * To match all jobs which are waiting for a blocking condition to fall.
     */
    public Filter andAreBlocked() {
      andMatch(BlockedFilter.TRUE_INSTANCE);
      return this;
    }

    /**
     * To match all jobs which are not waiting for a blocking condition to fall.
     */
    public Filter andAreNotBlocked() {
      andMatch(BlockedFilter.FALSE_INSTANCE);
      return this;
    }

    /**
     * To match all jobs which are configured to run periodically.
     *
     * @see IJobManager#scheduleWithFixedDelay()
     * @see IJobManager#scheduleAtFixedRate()
     */
    public Filter andArePeriodic() {
      andMatch(PeriodicFilter.TRUE_INSTANCE);
      return this;
    }

    /**
     * To match all jobs which are not configured to run periodically (one-shot actions).
     *
     * @see IJobManager#schedule()
     */
    public Filter andAreNotPeriodic() {
      andMatch(PeriodicFilter.FALSE_INSTANCE);
      return this;
    }
  }

  /**
   * Filter which accepts all of the given Futures.
   *
   * @since 5.1
   */
  public static class FutureFilter implements IFilter<IFuture<?>>, IAdaptable {

    private final Set<IFuture<?>> m_futures;

    public FutureFilter(final IFuture<?>... futures) {
      m_futures = CollectionUtility.hashSet(futures);
    }

    public FutureFilter(final Collection<IFuture<?>> futures) {
      m_futures = CollectionUtility.hashSet(futures);
    }

    @Override
    public boolean accept(final IFuture<?> future) {
      return m_futures.contains(future);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(final Class<T> type) {
      if (type == IFuture[].class) {
        return (T) m_futures.toArray(new IFuture[m_futures.size()]);
      }
      return null;
    }
  }

  /**
   * Depending on the given 'blocked' argument, this filter accepts only blocked or non-blocked Futures.
   *
   * @see IBlockingCondition
   * @since 5.1
   */
  public static class BlockedFilter implements IFilter<IFuture<?>> {

    public static final IFilter<IFuture<?>> TRUE_INSTANCE = new BlockedFilter(true);
    public static final IFilter<IFuture<?>> FALSE_INSTANCE = new BlockedFilter(false);

    private final boolean m_blocked;

    private BlockedFilter(final boolean blocked) {
      m_blocked = blocked;
    }

    @Override
    public boolean accept(final IFuture<?> future) {
      return future.isBlocked() == m_blocked;
    }
  }

  /**
   * Depending on the given 'periodic' argument, this filter accepts only periodic or non-periodic Futures.
   *
   * @since 5.1
   */
  public static class PeriodicFilter implements IFilter<IFuture<?>> {

    public static final IFilter<IFuture<?>> TRUE_INSTANCE = new PeriodicFilter(true);
    public static final IFilter<IFuture<?>> FALSE_INSTANCE = new PeriodicFilter(false);

    private final boolean m_periodic;

    private PeriodicFilter(final boolean periodic) {
      m_periodic = periodic;
    }

    @Override
    public boolean accept(final IFuture<?> future) {
      return future.isPeriodic() == m_periodic;
    }
  }

  /**
   * Filter which accepts all Futures which do belong to the given job names.
   *
   * @since 5.1
   */
  public static class JobNameFilter implements IFilter<IFuture<?>> {

    private final Set<String> m_names;

    public JobNameFilter(final String... names) {
      m_names = CollectionUtility.hashSet(names);
    }

    @Override
    public boolean accept(final IFuture<?> future) {
      return m_names.contains(future.getJobInput().getName());
    }
  }

  /**
   * Filter which accepts all Futures which have a name matching the regex.
   *
   * @since 5.1
   */
  public static class JobNameRegexFilter implements IFilter<IFuture<?>> {

    private final Pattern m_regex;

    public JobNameRegexFilter(final Pattern regex) {
      m_regex = regex;
    }

    @Override
    public boolean accept(final IFuture<?> future) {
      if (future.getJobInput().getName() == null) {
        return false;
      }
      return m_regex.matcher(future.getJobInput().getName()).matches();
    }
  }

  /**
   * Filter which accepts all Futures that belong to the given mutex object.
   *
   * @since 5.1
   */
  public static class MutexFilter implements IFilter<IFuture<?>> {

    private final Object m_mutexObject;

    public MutexFilter(final Object mutexObject) {
      m_mutexObject = mutexObject;
    }

    @Override
    public boolean accept(final IFuture<?> future) {
      return CompareUtility.equals(m_mutexObject, future.getJobInput().getMutex());
    }
  }
}
