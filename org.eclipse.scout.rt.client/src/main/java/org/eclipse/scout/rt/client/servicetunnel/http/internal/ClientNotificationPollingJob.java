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
package org.eclipse.scout.rt.client.servicetunnel.http.internal;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;

public class ClientNotificationPollingJob implements IRunnable {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientNotificationPollingJob.class);

  @Override
  public void run() throws Exception {
    if (RunMonitor.CURRENT.get().isCancelled()) {
      return;
    }

    IPingService pingService = BEANS.get(IPingService.class);
    try {
      // side-effect of every service call (whether ping or any other) is to get client notifications
      pingService.ping("GetClientNotifications");
    }
    catch (Exception t) {
      if (LOG.isInfoEnabled()) {
        LOG.info("polling", t);
      }
    }
  }
}
