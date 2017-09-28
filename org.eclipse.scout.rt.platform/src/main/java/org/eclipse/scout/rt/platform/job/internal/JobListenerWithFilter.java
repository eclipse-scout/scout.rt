/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job.internal;

import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JobListenerWithFilter implements IJobListener {

  private static final Logger LOG = LoggerFactory.getLogger(JobListenerWithFilter.class);

  private final IJobListener m_listener;
  private final IFilter<JobEvent> m_filter;

  JobListenerWithFilter(final IJobListener listener, final IFilter<JobEvent> filter) {
    m_listener = listener;
    m_filter = filter;
  }

  @Override
  public void changed(final JobEvent event) {
    if (m_filter == null || m_filter.accept(event)) {
      try {
        m_listener.changed(event);
      }
      catch (final RuntimeException e) {
        LOG.error("Listener or filter threw exception while accepting or handling job lifecycle event [listener={}, filter={}, event={}]",
            m_listener.getClass().getName(),
            m_filter == null ? "null" : m_filter.getClass().getName(),
            event,
            e);
      }
    }
  }

  public IJobListener getListener() {
    return m_listener;
  }

  public IFilter<JobEvent> getFilter() {
    return m_filter;
  }
}
