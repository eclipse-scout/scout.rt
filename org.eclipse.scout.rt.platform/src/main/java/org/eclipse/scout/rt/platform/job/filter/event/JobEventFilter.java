/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.filter.event;

import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Filter to accept events of some types.
 *
 * @since 5.1
 */
public class JobEventFilter implements Predicate<JobEvent> {

  private final Set<JobEventType> m_eventTypes;

  public JobEventFilter(final JobEventType... eventTypes) {
    m_eventTypes = CollectionUtility.hashSet(eventTypes);
  }

  @Override
  public boolean test(final JobEvent event) {
    return m_eventTypes.contains(event.getType());
  }
}
