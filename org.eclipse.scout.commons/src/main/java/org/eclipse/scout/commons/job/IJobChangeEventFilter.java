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
package org.eclipse.scout.commons.job;

import org.eclipse.scout.commons.filter.IFilter;

/**
 * Job change event filter that allows to limit the events a listener will be notified for.
 * 
 * @since 5.1
 * @see {@link IJobChangeListeners#add(IJobChangeListener, IJobChangeEventFilter)}
 */
public interface IJobChangeEventFilter extends IFilter<IJobChangeEvent> {

  /**
   * Specifies if an {@link IJobChangeEvent} is of interest for a {@link IJobChangeListener}.
   * 
   * @param event
   *          The event
   * @return <code>true</code> if the given event should be accepted, <code>false</code> otherwise.
   */
  @Override
  boolean accept(IJobChangeEvent event);

}
