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
import org.eclipse.scout.commons.filter.NotFilter;

/**
 * Factory methods to create Future filters related to jobs.
 *
 * @since 5.1
 */
public final class JobFutureFilters {

  private JobFutureFilters() {
  }

  /**
   * Creates a filter to accept only 'basic jobs' with some specific characteristics. 'Basic jobs' are jobs with a
   * concrete {@link JobInput} as their input and do not include client-, model- nor server jobs. The filter is designed
   * to support method chaining.
   */
  public static Filter newFilter() {
    return new Filter();
  }

  /**
   * Filter to accept only 'basic jobs' with the given characteristics. 'Basic jobs' are jobs with a concrete
   * {@link JobInput} as their input and do not include client-, model- nor server jobs.
   * <p/>
   * The 'setter-methods' returns <code>this</code> in order to support for method chaining.
   *
   * @since 5.1
   */
  public static class Filter implements IFilter<IFuture<?>> {

    protected final List<IFilter<IFuture<?>>> m_filters = new ArrayList<>();

    public Filter() {
      postConstruct();
    }

    @Override
    public boolean accept(final IFuture<?> future) {
      return new AndFilter<>(m_filters).accept(future);
    }

    /**
     * Method invoked after construction. The default implementation adds a filter to accept only 'basic jobs'.
     */
    protected void postConstruct() {
      m_filters.add(JobFilter.INSTANCE);
    }

    /**
     * To accept only jobs of the given job-id.
     */
    public Filter id(final String id) {
      m_filters.add(new IdFilter(id));
      return this;
    }

    /**
     * To accept only jobs which belong to the given Futures.
     */
    public Filter futures(final IFuture<?>... futures) {
      m_filters.add(new FutureFilter(futures));
      return this;
    }

    /**
     * To accept only jobs which belong to the given Futures.
     */
    public Filter futures(final Collection<IFuture<?>> futures) {
      m_filters.add(new FutureFilter(futures));
      return this;
    }

    /**
     * To accept only the current executing job.
     *
     * @see IFuture#CURRENT
     */
    public Filter currentFuture() {
      m_filters.add(new FutureFilter(IFuture.CURRENT.get()));
      return this;
    }

    /**
     * To accept all jobs except the current executing job.
     *
     * @see IFuture#CURRENT
     */
    public Filter notCurrentFuture() {
      m_filters.add(new NotFilter<>(new FutureFilter(IFuture.CURRENT.get())));
      return this;
    }

    /**
     * To accept only jobs waiting for a blocking condition to fall.
     *
     * @see IBlockingCondition
     */
    public Filter blocked() {
      m_filters.add(new BlockedFilter(true));
      return this;
    }

    /**
     * To accept only jobs which are not waiting for a blocking condition to fall.
     *
     * @see IBlockingCondition
     */
    public Filter notBlocked() {
      m_filters.add(new BlockedFilter(false));
      return this;
    }

    /**
     * To accept only periodic jobs.
     *
     * @see IJobManager#scheduleWithFixedDelay()
     * @see IJobManager#scheduleAtFixedRate()
     */
    public Filter periodic() {
      m_filters.add(new PeriodicFilter(true));
      return this;
    }

    /**
     * To accept only jobs that are executed once.
     *
     * @see IJobManager#schedule()
     */
    public Filter notPeriodic() {
      m_filters.add(new PeriodicFilter(false));
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

    private final boolean m_blocked;

    public BlockedFilter(final boolean blocked) {
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

    private final boolean m_periodic;

    public PeriodicFilter(final boolean periodic) {
      m_periodic = periodic;
    }

    @Override
    public boolean accept(final IFuture<?> future) {
      return future.isPeriodic() == m_periodic;
    }
  }

  /**
   * Filter which discards all Futures which do not belong to the given <code>job-id</code>.
   *
   * @since 5.1
   */
  public static class IdFilter implements IFilter<IFuture<?>> {

    private final Set<String> m_ids;

    public IdFilter(final String... id) {
      m_ids = CollectionUtility.hashSet(id);
    }

    @Override
    public boolean accept(final IFuture<?> future) {
      return m_ids.contains(future.getJobInput().getId());
    }
  }

  /**
   * Filter which accepts only Futures from 'basic jobs'. 'Basic jobs' are jobs with a
   * concrete {@link JobInput} as their input and do not include client-, model- nor server jobs.
   *
   * @since 5.1
   */
  public static class JobFilter implements IFilter<IFuture<?>> {

    public static final IFilter<IFuture<?>> INSTANCE = new JobFilter();

    private JobFilter() {
    }

    @Override
    public boolean accept(final IFuture<?> future) {
      return JobInput.class.equals(future.getJobInput().getClass());
    }
  }
}
