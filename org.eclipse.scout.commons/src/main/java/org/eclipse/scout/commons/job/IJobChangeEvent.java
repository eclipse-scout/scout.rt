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

/**
 * Job change event
 *
 * @since 5.1
 * @see IJobChangeListeners#DEFAULT
 * @see IJobChangeListener
 */
public interface IJobChangeEvent {

  /**
   * @return The type of event.
   */
  int getType();

  /**
   * @return The mode in which the event has occurred.
   */
  int getMode();

  /**
   * @return The future that belongs to the event. May be <code>null</code> in some cases.
   */
  IFuture<?> getFuture();

  /**
   * @return The {@link IJobManager} that was the source of the event.
   */
  IJobManager<?> getSourceManager();

}
