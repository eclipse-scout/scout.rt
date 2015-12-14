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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobState;

/**
 * Filter to accept Futures in a specific {@link JobState}.
 *
 * @since 5.2
 */
public class JobStateFutureFilter implements IFilter<IFuture<?>> {

  private final Set<JobState> m_states;

  public JobStateFutureFilter(final JobState... states) {
    m_states = new HashSet<>(Arrays.asList(states));
  }

  @Override
  public boolean accept(final IFuture<?> future) {
    return m_states.contains(future.getState());
  }
}
