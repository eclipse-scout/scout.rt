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
package org.eclipse.scout.rt.server.services.common.code;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.services.common.notification.IDistributedNotification;
import org.eclipse.scout.rt.server.services.common.notification.IDistributedNotificationListener;
import org.eclipse.scout.rt.shared.services.common.code.ICodeService;
import org.eclipse.scout.service.SERVICES;

/**
 *
 */
public class UnloadCodeTypeCacheNodeNotificationListener implements IDistributedNotificationListener {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(CodeService.class);

  @Override
  public void onNewNotification(IDistributedNotification notification) {
    if (isInteresting(notification)) {
      UnloadCodeTypeCacheNodeNotification n = (UnloadCodeTypeCacheNodeNotification) notification.getNotification();
      try {
        SERVICES.getService(ICodeService.class).reloadCodeTypesInternal(n.getTypes());
      }
      catch (ProcessingException e) {
        LOG.error("Unable to reload CodeTypes", e);
      }
    }
  }

  @Override
  public void onUpdateNotification(IDistributedNotification notification) {
  }

  @Override
  public void onRemoveNotification(IDistributedNotification notification) {
  }

  @Override
  public boolean isInteresting(IDistributedNotification notification) {
    return (notification instanceof UnloadCodeTypeCacheNodeNotification);
  }

}
