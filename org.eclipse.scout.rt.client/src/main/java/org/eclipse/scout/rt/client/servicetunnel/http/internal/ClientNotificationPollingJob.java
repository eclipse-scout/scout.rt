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

import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;
import org.eclipse.scout.service.SERVICES;

public class ClientNotificationPollingJob implements IRunnable {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientNotificationPollingJob.class);

  @Override
  public void run() throws Exception {
    if (IProgressMonitor.CURRENT.get().isCancelled()) {
      return;
    }

    IPingService pingService = SERVICES.getService(IPingService.class);
    try {
      // side-effect of every service call (whether ping or any other) is to get client notifications
      pingService.ping("GetClientNotifications");
    }
    catch (Throwable t) {
      if (LOG.isInfoEnabled()) {
        LOG.info("polling", t);
      }
    }
  }
}
