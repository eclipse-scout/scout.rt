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
package org.eclipse.scout.rt.platform.job.listener;

import java.util.EventListener;

/**
 * Listener to be notified about job events.
 *
 * @since 5.1
 * @see JobEvent
 */
public interface IJobListener extends EventListener {

  /**
   * Method invoked to notify about a job event.
   * <p>
   * <strong>This method is invoked from the thread firing the given event. Hence, any long running operation should be
   * done asynchronously within a separate job. Also, never wait for that job to complete, because no assumption about
   * the current mutex can be made.</strong>
   *
   * @param event
   *          describes the event occurred.
   */
  void changed(JobEvent event);
}
