/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
