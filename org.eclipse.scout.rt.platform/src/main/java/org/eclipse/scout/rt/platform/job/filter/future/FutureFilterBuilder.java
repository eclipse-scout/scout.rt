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
package org.eclipse.scout.rt.platform.job.filter.future;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.filter.AlwaysFilter;
import org.eclipse.scout.commons.filter.AndFilter;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.commons.filter.NotFilter;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IMutex;
import org.eclipse.scout.rt.platform.job.JobInput;

/**
 * This builder facilitates the creation of a {@link IFuture} filter to match multiple criteria joined by logical 'AND'
 * operation.
 *
 * @since 5.1
 */
@Bean
public class FutureFilterBuilder {

  private final List<IFilter<IFuture<?>>> m_andFilters = new ArrayList<>();

  /**
   * Builds the filter based on the criteria as configured with this builder instance. Thereby, the filter criteria are
   * joined by logical 'AND' operation.
   */
  public IFilter<IFuture<?>> toFilter() {
    switch (m_andFilters.size()) {
      case 0:
        return new AlwaysFilter<>();
      case 1:
        return m_andFilters.get(0);
      default:
        return new AndFilter<>(m_andFilters);
    }
  }

  /**
   * To match all jobs where the given filter evaluates to <code>true</code>.
   */
  public FutureFilterBuilder andMatch(final IFilter<IFuture<?>> filter) {
    m_andFilters.add(filter);
    return this;
  }

  /**
   * To match all jobs where the given filter does not apply, meaning evaluates to <code>false</code>.
   */
  public FutureFilterBuilder andMatchNot(final IFilter<IFuture<?>> filter) {
    m_andFilters.add(new NotFilter<>(filter));
    return this;
  }

  /**
   * To match all jobs of one of the given names.
   */
  public FutureFilterBuilder andMatchName(final String... names) {
    andMatch(new JobNameFutureFilter(names));
    return this;
  }

  /**
   * To match all jobs where the given regex matches their name.
   */
  public FutureFilterBuilder andMatchNameRegex(final Pattern regex) {
    andMatch(new JobNameRegexFutureFilter(regex));
    return this;
  }

  /**
   * To match all jobs which are represented by one of the given Futures.
   */
  public FutureFilterBuilder andMatchFuture(final IFuture... futures) {
    andMatch(new FutureFilter(futures));
    return this;
  }

  /**
   * To match all jobs which are represented by one of the given Futures.
   */
  public FutureFilterBuilder andMatchFuture(final Collection<IFuture<?>> futures) {
    andMatch(new FutureFilter(futures));
    return this;
  }

  /**
   * To match all jobs which are represented by one of the given Futures.
   */
  public <RESULT> FutureFilterBuilder andMatchFuture(final List<IFuture<RESULT>> futures) {
    andMatch(new FutureFilter(futures.toArray(new IFuture<?>[futures.size()])));
    return this;
  }

  /**
   * To match all jobs which are not represented by any of the given Futures.
   */
  public FutureFilterBuilder andMatchNotFuture(final IFuture<?>... futures) {
    andMatchNot(new FutureFilter(futures));
    return this;
  }

  /**
   * To match all jobs which are not represented by any of the given Futures.
   */
  public FutureFilterBuilder andMatchNotFuture(final Collection<IFuture<?>> futures) {
    andMatchNot(new FutureFilter(futures));
    return this;
  }

  /**
   * To match all jobs which are not represented by any of the given Futures.
   */
  public <RESULT> FutureFilterBuilder andMatchNotFuture(final List<IFuture<RESULT>> futures) {
    andMatchNot(new FutureFilter(futures.toArray(new IFuture<?>[futures.size()])));
    return this;
  }

  /**
   * To match all jobs which have the given mutex object set.
   */
  public FutureFilterBuilder andMatchMutex(final IMutex mutex) {
    andMatch(new MutexFutureFilter(mutex));
    return this;
  }

  /**
   * To match all jobs which are waiting for a blocking condition to fall.
   */
  public FutureFilterBuilder andAreBlocked() {
    andMatch(BlockedFutureFilter.INSTANCE_BLOCKED);
    return this;
  }

  /**
   * To match all jobs which are not waiting for a blocking condition to fall.
   */
  public FutureFilterBuilder andAreNotBlocked() {
    andMatch(BlockedFutureFilter.INSTANCE_NOT_BLOCKED);
    return this;
  }

  /**
   * To match all jobs which are configured to run once.
   *
   * @see JobInput#SCHEDULING_RULE_SINGLE_EXECUTION
   */
  public FutureFilterBuilder andAreSingleExecuting() {
    andMatch(SingleExecutionFutureFilter.INSTANCE);
    return this;
  }

  /**
   * To match all jobs which are configured to run periodically.
   *
   * @see JobInput#SCHEDULING_RULE_PERIODIC_EXECUTION_AT_FIXED_RATE
   * @see JobInput#SCHEDULING_RULE_PERIODIC_EXECUTION_WITH_FIXED_DELAY
   */
  public FutureFilterBuilder andArePeriodicExecuting() {
    andMatch(PeriodicExecutionFutureFilter.INSTANCE);
    return this;
  }

  /**
   * To match all jobs which are running on behalf of the given {@link RunContext} type.
   */
  public FutureFilterBuilder andMatchRunContext(final Class<? extends RunContext> runContextClazz) {
    andMatch(new RunContextFutureFilter(runContextClazz));
    return this;
  }

  /**
   * To match all jobs which are tagged with the given execution hint.
   */
  public FutureFilterBuilder andMatchExecutionHint(final Object executionHint) {
    andMatch(new ExecutionHintFutureFilter(executionHint));
    return this;
  }

  /**
   * To match all jobs which are not tagged with the given execution hint.
   */
  public FutureFilterBuilder andMatchNotExecutionHint(final Object hint) {
    andMatchNot(new ExecutionHintFutureFilter(hint));
    return this;
  }
}
