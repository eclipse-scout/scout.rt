package org.eclipse.scout.rt.ui.html;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.client.IClientSession;

/**
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
   * Registers (adds) the given UI session <b>and</b> the associated client session to the store.
   */
  void registerUiSession(IUiSession uiSession);

  /**
   * Unregisters (removes) the given UI session from the store. If the associated client session is not used by any
   * other UI sessions, housekeeping is started. If the client session is still not used after some time, it is stopped
   * automatically to free up memory. This can also lead to HTTP session invalidation if no other client sessions are
   * active.
   */
  void unregisterUiSession(IUiSession uiSession);

  /**
   * Queries the session store for a client session with the given ID. If such a client session is currently registered
   * at the store, it is returned. Otherwise, <code>null</code> is returned.
   * <p>
   * <b>Important:</b> Any scheduled housekeeping for this specific client session is cancelled. This method is
   * therefore only intended to be used while creating and registering a new UI session.
   */
  IClientSession getClientSessionForUse(String clientSessionId);
}
