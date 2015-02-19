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

import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.scout.rt.client.ClientAsyncJob;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientRule;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.servicetunnel.IServiceTunnel;

public interface IClientNotificationConsumerListener extends EventListener {

  /**
   * @param e
   *          the event
   * @param sync
   *          true if {@link IJobManager#currentJob()} instanceof {@link ClientJob} and {@link ClientJob#isSync()}
   *          <p>
   *          If a {@link ClientSyncJob} calls
   *          {@link IServiceTunnel#invokeService(Class, java.lang.reflect.Method, Object[])} which directly returns
   *          notifications, then these notifications are handled immediately.
   *          <p>
   *          Otherwise this method is called within a {@link ClientAsyncJob}.
   *          <p>
   *          Therefore when performing operations on the model check {@link ClientJob#isSync()} and eventually use
   *          {@link ClientSyncJob} to have proper session monitors used. see {@link ClientSyncJob}, {@link ClientRule}
   */
  void handleEvent(ClientNotificationConsumerEvent e, boolean sync);

}
