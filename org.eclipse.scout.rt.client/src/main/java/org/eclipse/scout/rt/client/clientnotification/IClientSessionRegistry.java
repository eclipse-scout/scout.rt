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
package org.eclipse.scout.rt.client.clientnotification;

import java.util.List;
import java.util.UUID;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.ApplicationScoped;

@ApplicationScoped
public interface IClientSessionRegistry {
  String NOTIFICATION_NODE_ID = UUID.randomUUID().toString();

  void register(IClientSession clientSession, String sessionId);

  /**
   * @param sessionid
   * @return
   */
  IClientSession getClientSession(String sessionid);

  /**
   * @param userId
   * @return
   */
  List<IClientSession> getClientSessionsForUser(String userId);

  /**
   * @return
   */
  List<IClientSession> getAllClientSessions();

}
