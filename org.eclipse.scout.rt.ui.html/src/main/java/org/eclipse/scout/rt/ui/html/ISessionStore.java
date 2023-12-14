/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.client.IClientSession;

/**
 * Per-HTTP-session store for {@link IUiSession}s and {@link IClientSession}s.
 * <p>
 * <b>About Serialization</b>
 * <p>
 * Currently, this class does not extend the {@link Serializable} interface. It can therefore be added as a session
 * attribute, but if the HTTP session is passivated or moved to another cluster node, it will not work. There is no
 * solution for this problem at the moment. Even when this class was serializable, its content would not be
 * (ClientSession, UiSession, HttpSession). Serializing an empty SessionStore object would not be any better.
 * <p>
 * Once there is a solution for serializing {@link IClientSession} (including all forms, outlines etc.), this decision
 * may be re-thought. See also the related <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=458145">Scout bug
 * 458145</a>.
 *
 * @since 5.2
 */
public interface ISessionStore {

  HttpSession getHttpSession();

  String getHttpSessionId();

  boolean isHttpSessionValid();

  /**
   * @return a copy (!) of the client session map (key = clientSessionId)
   */
  Map<String, IClientSession> getClientSessionMap();

  /**
   * @return a copy (!) of the UI session map (key = uiSessionId)
   */
  Map<String, IUiSession> getUiSessionMap();

  /**
   * @return a copy (!) of the "UI Sessions by client session" map
   */
  Map<IClientSession, Set<IUiSession>> getUiSessionsByClientSession();

  int countUiSessions();

  int countClientSessions();

  boolean isEmpty();

  /**
   * Queries the session store for a UI session with the given ID. If such a UI session is currently registered at the
   * store, it is returned. Otherwise, <code>null</code> is returned.
   */
  IUiSession getUiSession(String uiSessionId);

  /**
   * Pre-registration of a new {@link UiSession} that is currently being built. This call ensures that the
   * {@link HttpSession} is not invalidated due to long {@link IClientSession#start(String)} delays. The effect is in
   * {@link SessionStore#removeClientSessionInsideWriteLock(IClientSession)}
   * <p>
   * This pre-registration queries the session store for a client session with the given ID. If such a client session is
   * currently registered at the store, it is returned. Otherwise, <code>null</code> is returned.
   * <p>
   * <b>Important:</b> Any scheduled housekeeping for this specific client session is cancelled. This method is
   * therefore only intended to be used while creating and registering a new UI session.
   *
   * @param uiSession
   *          is the new UI session that runs
   *          {@link UiSession#init(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, org.eclipse.scout.rt.ui.html.json.JsonStartupRequest)}
   * @param queryClientSessionId
   *          is the id of a possibly existing and still valid clientSession
   * @return the associated and still valid, active {@link IClientSession} or null
   */
  IClientSession preregisterUiSession(IUiSession uiSession, String queryClientSessionId);

  /**
   * Registers (adds) the given UI session <b>and</b> the associated client session to the store.
   */
  void registerUiSession(IUiSession uiSession);

  /**
   * Unregisters (removes) the given UI session from the store. If the associated client session is not used by any
   * other UI sessions, housekeeping is started. If the client session is still not used after some time, it is stopped
   * automatically to free up memory. This can also lead to HTTP session invalidation if no other client sessions are
   * active.
   * <p>
   * If this uiSession was registered with {@link #preregisterUiSession(IUiSession, String)} then it is handled the same
   * way.
   */
  void unregisterUiSession(IUiSession uiSession);
}
