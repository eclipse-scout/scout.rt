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
package org.eclipse.scout.rt.shared.clientnotification;

import java.util.List;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.shared.TunnelToServer;

@ApplicationScoped
@TunnelToServer
public interface IClientNotificationService {

  void registerSession(String notificationNodeId, String sessionId, String userId);

  void unregisterSession(String notificationNodeId, String sessionId, String userId);

  List<ClientNotificationMessage> getNotifications(String notificationNodeId);

}
