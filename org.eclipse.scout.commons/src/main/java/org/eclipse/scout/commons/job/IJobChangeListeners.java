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

import org.eclipse.scout.commons.job.internal.JobChangeListeners;

/**
 * Global job change listener list valid for all {@link IJobManager}s.
 *
 * @since 5.1
 * @see IJobChangeListener
 * @see IJobChangeEventFilter
 * @see IJobManager
 */
public interface IJobChangeListeners {

  /**
   * The global listener list instance.
   */
  IJobChangeListeners DEFAULT = new JobChangeListeners();

  /**
   * Adds the given {@link IJobChangeListener} to the list of listeners. If the same listener is already in the list, it
   * is added another time and will therefore be notified multiple times.
   *
   * @param listener
   *          The listener to add.
   */
  void add(IJobChangeListener listener);

  /**
   * Adds the given {@link IJobChangeListener} that will be notified about all {@link IJobChangeEvent}s that match the
   * given {@link IJobChangeEventFilter}. If the same listener-filter-combination is already in the list, it is added
   * another time.
   * 
   * @param listener
   *          The listener to add.
   * @param eventFilter
   *          The filter that should limit the events the given listener will be notified about.
   */
  void add(IJobChangeListener listener, IJobChangeEventFilter eventFilter);

  /**
   * Removes the given listener from the list. If the given listener has been registered multiple times, all are removed
   * that have been registered without a filter.
   * 
   * @param listener
   *          The listener to remove.
   */
  void remove(IJobChangeListener listener);

  /**
   * Removes the given listener and filter from the list. If the given listener and filter have been registered multiple
   * times, all are removed.
   * 
   * @param listener
   *          The listener
   * @param eventFilter
   *          The filter
   */
  void remove(IJobChangeListener listener, IJobChangeEventFilter eventFilter);

  /**
   * Fires the given {@link IJobChangeEvent} to all listeners for which the {@link IJobChangeEventFilter} accepts the
   * given event.
   * 
   * @param eventToFire
   *          The event to fire.
   */
  void fireEvent(IJobChangeEvent eventToFire);
}
