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

import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.filter.IFilter;

/**
 * Filter which only accepts events of the given types.
 *
 * @since 5.1
 */
public class JobEventTypeFilter implements IFilter<JobEvent> {

  private final Set<JobEventType> m_eventTypes;

  public JobEventTypeFilter(final JobEventType... eventTypes) {
    m_eventTypes = CollectionUtility.hashSet(eventTypes);
  }

  @Override
  public boolean accept(final JobEvent event) {
    return m_eventTypes.contains(event.getType());
  }
}
