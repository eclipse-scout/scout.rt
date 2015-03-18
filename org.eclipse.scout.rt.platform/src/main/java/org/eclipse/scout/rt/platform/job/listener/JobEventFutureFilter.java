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
package org.eclipse.scout.rt.platform.job.listener;

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Filter which only accepts events that are associated with the given Future.
 *
 * @since 5.1
 */
public class JobEventFutureFilter implements IFilter<JobEvent> {

  private final IFuture<?> m_future;

  public JobEventFutureFilter(final IFuture<?> future) {
    m_future = future;
  }

  @Override
  public boolean accept(final JobEvent event) {
    return event.getFuture() == m_future;
  }
}
