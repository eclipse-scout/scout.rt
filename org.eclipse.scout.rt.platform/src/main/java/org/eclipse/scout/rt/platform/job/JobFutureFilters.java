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
     * Registers the given filter to further constrain the Futures to be accepted.
     */
    public Filter andFilter(final IFilter<IFuture<?>> filter) {
      m_filters.add(filter);
      return this;
    }

    /**
     * To accept only jobs of the given job names.
     */
    public Filter names(final String... names) {
      andFilter(new JobNameFilter(names));
      return this;
    }

    /**
     * To accept only jobs of the given job name regex.
     */
    public Filter nameRegex(final Pattern regex) {
      andFilter(new JobNameRegexFilter(regex));
      return this;
    }

    /**
     * To accept only jobs which belong to the given Futures.
     */
    public Filter futures(final IFuture<?>... futures) {
      andFilter(new FutureFilter(futures));
      return this;
    }

    /**
     * To accept only jobs which belong to the given Futures.
     */
    public Filter futures(final Collection<IFuture<?>> futures) {
      andFilter(new FutureFilter(futures));
      return this;
    }

    /**
     * To accept only the current executing job.
     *
     * @see IFuture#CURRENT
     */
    public Filter currentFuture() {
      andFilter(new FutureFilter(IFuture.CURRENT.get()));
      return this;
    }

    /**
     * To accept all jobs except the current executing job.
     *
     * @see IFuture#CURRENT
     */
    public Filter notCurrentFuture() {
      andFilter(new NotFilter<>(new FutureFilter(IFuture.CURRENT.get())));
      return this;
    }

    /**
     * To accept only jobs waiting for a blocking condition to fall.
     *
     * @see IBlockingCondition
     */
    public Filter blocked() {
      andFilter(BlockedFilter.TRUE_INSTANCE);
      return this;
    }

    /**
     * To accept only jobs which are not waiting for a blocking condition to fall.
     *
     * @see IBlockingCondition
     */
    public Filter notBlocked() {
      andFilter(BlockedFilter.FALSE_INSTANCE);
      return this;
    }

    /**
     * To accept only periodic jobs.
     *
     * @see IJobManager#scheduleWithFixedDelay()
     * @see IJobManager#scheduleAtFixedRate()
     */
    public Filter periodic() {
      andFilter(PeriodicFilter.TRUE_INSTANCE);
      return this;
    }

    /**
     * To accept only jobs that are executed once.
     *
     * @see IJobManager#schedule()
     */
    public Filter notPeriodic() {
      andFilter(PeriodicFilter.FALSE_INSTANCE);
      return this;
    }

    /**
     * To accept only jobs which belong to the given mutex object.
     */
    public Filter mutex(final Object mutexObject) {
      andFilter(new MutexFilter(mutexObject));
      return this;
    }
  }

  /**
   * Filter which discards all Futures except the given ones.
   *
   * @since 5.1
   */
  public static class FutureFilter implements IFilter<IFuture<?>> {

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
      return m_names.contains(future.getJobInput().name());
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
      if (future.getJobInput().name() == null) {
        return false;
      }
      return m_regex.matcher(future.getJobInput().name()).matches();
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
      return CompareUtility.equals(m_mutexObject, future.getJobInput().mutex());
    }
  }
}
