/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client;

import java.util.EventListener;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;

public interface IJobChangeListenerEx extends IJobChangeListener, EventListener {

  /**
   * Notification that a job entered a blocking condition
   * 
   * @param event
   *          the event details
   */
  void blockingConditionStart(IJobChangeEvent event);

  /**
   * Notification that a job left a blocking condition
   * 
   * @param event
   *          the event details
   */
  void blockingConditionEnd(IJobChangeEvent event);

}
