/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.shared.ISession;

/**
 * Registry to keep track of {@link IClientSession}s and its users in order to dispatch client notifications.
 */
@ApplicationScoped
public interface IClientSessionRegistry {

  /**
   * Register a new client session. The session does not need to be started. The id might not be available on the
   * session yet.
   *
   * @param session
   *          {@link IClientSession}
   * @param sessionId
   *          {@link} id of the session
   */
  void register(IClientSession session, String sessionId);

  /**
   * @param sessionid
   *          the id of the session, see {@link ISession#getId()}
   * @return the session for a given id
   */
  IClientSession getClientSession(String sessionid);

  /**
   * @param userId
   *          the user of the session, see {@link ISession#getUserId()}
   * @return the session for a given userid
   */
  List<IClientSession> getClientSessionsForUser(String userId);

  /**
   * @return all registered client sessions
   */
  List<IClientSession> getAllClientSessions();

}
