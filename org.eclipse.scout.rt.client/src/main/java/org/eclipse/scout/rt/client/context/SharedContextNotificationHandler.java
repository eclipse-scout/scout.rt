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
package org.eclipse.scout.rt.client.context;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.shared.notification.INotificationHandler;
import org.eclipse.scout.rt.shared.services.common.context.SharedContextChangedNotification;

/**
 * Handler for {@link SharedContextChangedNotification}
 */
public class SharedContextNotificationHandler implements INotificationHandler<SharedContextChangedNotification> {

  @Override
  public void handleNotification(SharedContextChangedNotification notification) {
    // the client session must be available for shared context variable updates otherwise it is a wrong usage of the notification.
    IClientSession session = (IClientSession) Assertions.assertNotNull(IClientSession.CURRENT.get());
    session.replaceSharedVariableMapInternal(notification.getSharedVariableMap());
  }
}
