/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.filter.future;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobState;

/**
 * Filter to accept Futures in a specific {@link JobState}.
 *
 * @since 5.2
 */
public class JobStateFutureFilter implements Predicate<IFuture<?>> {

  private final Set<JobState> m_states;

  public JobStateFutureFilter(final JobState... states) {
    m_states = new HashSet<>(Arrays.asList(states));
  }

  @Override
  public boolean test(final IFuture<?> future) {
    return m_states.contains(future.getState());
  }
}
