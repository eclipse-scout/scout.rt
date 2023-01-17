/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.listener;

import java.util.EventListener;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;

/**
 * Listener to be notified about job events.
 *
 * @since 5.1
 * @see JobEvent
 */
@FunctionalInterface
public interface IJobListener extends EventListener {

  /**
   * Method invoked to notify about a job event.
   * <p>
   * <strong>This method is invoked from the thread firing the given event. Hence, any long running operation should be
   * done asynchronously within a separate job. But, never wait for that job to complete, because no assumption about a
   * current {@link IExecutionSemaphore} can be made.</strong>
   * <p>
   * The implementor is responsible to handle the event in the proper {@link RunContext}.
   *
   * @param event
   *          describes the event occurred.
   */
  void changed(JobEvent event);
}
