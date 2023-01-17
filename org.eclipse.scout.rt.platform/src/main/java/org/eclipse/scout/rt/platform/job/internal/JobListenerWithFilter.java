/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.internal;

import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JobListenerWithFilter implements IJobListener {

  private static final Logger LOG = LoggerFactory.getLogger(JobListenerWithFilter.class);

  private final IJobListener m_listener;
  private final Predicate<JobEvent> m_filter;

  JobListenerWithFilter(final IJobListener listener, final Predicate<JobEvent> filter) {
    m_listener = listener;
    m_filter = filter;
  }

  @Override
  public void changed(final JobEvent event) {
    if (m_filter == null || m_filter.test(event)) {
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

  public Predicate<JobEvent> getFilter() {
    return m_filter;
  }
}
