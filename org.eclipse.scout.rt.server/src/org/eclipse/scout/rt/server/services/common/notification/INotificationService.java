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
package org.eclipse.scout.rt.server.services.common.notification;

import org.eclipse.scout.service.IService;

/**
 *
 */
public interface INotificationService extends IService {

  public boolean register();

  public boolean unregister();

  public boolean isEnabled();

  public void publishNotification(INotification notification);

  public void updateNotification(INotification notification);

  public void removeNotification(INotification notification);

  public void processNotification(IDistributedNotification notification);

  public void addDistributedNotificationListener(IDistributedNotificationListener listener);

  public void removeDistributedNotificationListener(IDistributedNotificationListener listener);

}
