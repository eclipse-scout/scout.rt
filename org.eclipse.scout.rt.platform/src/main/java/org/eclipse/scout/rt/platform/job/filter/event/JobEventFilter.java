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
