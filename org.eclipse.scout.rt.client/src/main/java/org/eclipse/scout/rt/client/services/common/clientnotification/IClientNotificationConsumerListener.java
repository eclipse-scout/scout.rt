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
package org.eclipse.scout.rt.client.services.common.clientnotification;

import java.util.EventListener;

import org.eclipse.scout.rt.client.job.internal.ModelJobManager;
import org.eclipse.scout.rt.servicetunnel.IServiceTunnel;

public interface IClientNotificationConsumerListener extends EventListener {

  /**
   * @param e
   *          the event
   * @param sync
   *          true if the current thread is the model thread (see {@link ModelJobManager#isModelThread()}).<br>
   *          If a model thread calls {@link IServiceTunnel#invokeService(Class, java.lang.reflect.Method, Object[])}
   *          which directly returns notifications, then these notifications are handled immediately. Otherwise this
   *          method is called within sync=false.<br>
   *          Therefore when performing operations on the model check the sync param and probably use
   *          {@link ModelJobManager} to have proper session monitors used.
   */
  void handleEvent(ClientNotificationConsumerEvent e, boolean sync);

}
