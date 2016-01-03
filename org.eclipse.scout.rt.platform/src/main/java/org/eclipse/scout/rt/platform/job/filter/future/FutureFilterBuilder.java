/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.filter.AlwaysFilter;
import org.eclipse.scout.rt.platform.filter.AndFilter;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.filter.NotFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.JobState;

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
   * To match all jobs which are in one of the given states.
   */
  public FutureFilterBuilder andMatchState(final JobState... states) {
    andMatch(new JobStateFutureFilter(states));
    return this;
  }

  /**
   * To match all jobs which are not in one of the given states.
   */
  public FutureFilterBuilder andMatchNotState(final JobState... states) {
    andMatchNot(new JobStateFutureFilter(states));
    return this;
  }

  /**
   * To match all jobs which are assigned to the given {@link IExecutionSemaphore}.
   */
  public FutureFilterBuilder andMatchExecutionSemaphore(final IExecutionSemaphore semaphore) {
    andMatch(new ExecutionSemaphoreFutureFilter(semaphore));
    return this;
  }

  /**
   * To match all jobs which are configured to run once, meaning have just a single execution at a particular moment in
   * time.
   */
  public FutureFilterBuilder andAreSingleExecuting() {
    andMatch(SingleExecutionFutureFilter.INSTANCE);
    return this;
  }

  /**
   * To match all jobs which are configured to run multiple times, meaning which repeat one time at minimum.
   */
  public FutureFilterBuilder andAreNotSingleExecuting() {
    andMatchNot(SingleExecutionFutureFilter.INSTANCE);
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
  public FutureFilterBuilder andMatchExecutionHint(final String hint) {
    andMatch(new ExecutionHintFutureFilter(hint));
    return this;
  }

  /**
   * To match all jobs which are not tagged with the given execution hint.
   */
  public FutureFilterBuilder andMatchNotExecutionHint(final String hint) {
    andMatchNot(new ExecutionHintFutureFilter(hint));
    return this;
  }
}
