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

import java.util.EventListener;

/**
 * Job change listener that will be notified about events occurred in a {@link IJobManager}.
 *
 * @since 5.1
 * @see IJobChangeListeners#DEFAULT
 * @see IJobChangeEvent
 */
public interface IJobChangeListener extends EventListener {

  /**
   * Will be called when an event occurs.
   * 
   * @param event
   *          Event meta information.
   */
  void jobChanged(IJobChangeEvent event);
}
