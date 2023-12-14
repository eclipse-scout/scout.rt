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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.session.ClientSessionStopHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.shared.session.SessionMetricsHelper;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.SessionStoreHousekeepingDelayProperty;
import org.eclipse.scout.rt.ui.html.management.SessionMonitorMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of a session store per HTTP session.
 * <p>
 * It serves also as a listener for HTTP session invalidation. If it detects an invalid HTTP session, it tries to clean
 * up all associated client and UI sessions. See {@link #valueUnbound(HttpSessionBindingEvent)}.
 * <p>
 * Instances can be obtained using the bean {@link HttpSessionHelper#getSessionStore(HttpSession)}.
 *
 * @since 5.2
 */
public class SessionStore implements ISessionStore, HttpSessionBindingListener {
  private static final Logger LOG = LoggerFactory.getLogger(SessionStore.class);

  protected static final String SESSION_TYPE = "http";

  protected final SessionMetricsHelper m_sessionMetrics = BEANS.get(SessionMetricsHelper.class);

  private final HttpSession m_httpSession;
  private final String m_httpSessionId; // because getId() cannot be called on an invalid session
  private volatile boolean m_httpSessionValid = true;

  /**
   * key = clientSessionId
   */
  protected final Map<String, IClientSession> m_clientSessionMap = new HashMap<>();

  /**
   * key = uiSessionId
   */
  protected final Map<String, IUiSession> m_uiSessionMap = new HashMap<>();

  /**
   * key = uiSessionId
   * <p>
   * The preregistered sessions of {@link #preregisterUiSession(IUiSession, String)}
   */
  protected final Map<String, IUiSession> m_preregisteredUiSessionMap = new HashMap<>();

  /**
   * key = clientSession (<i>not</i> clientSessionId!)<br>
   * value = set of UI sessions (technically there can be multiple UI sessions by client session, although usually there
   * is only one or none).
   */
  protected final Map<IClientSession, Set<IUiSession>> m_uiSessionsByClientSession = new HashMap<>();

  /**
   * key = clientSession (<i>not</i> clientSessionId!)<br>
   * value = set of UI sessions (technically there can be multiple UI sessions by client session, although usually there
   * is only one or none).
   * <p>
   * The preregistered sessions of {@link #preregisterUiSession(IUiSession, String)} that are planning to re-use a
   * currently active {@link IClientSession}
   */
  protected final Map<IClientSession, Set<IUiSession>> m_preregisteredUiSessionsByClientSession = new HashMap<>();

  /**
   * Map of scheduled housekeeping jobs (key = clientSessionId). Using this map, scheduled but not yet executed
   * housekeeping jobs can be cancelled again when the client session is still to be used.
   */
  protected final Map<String, IFuture<?>> m_housekeepingFutures = new HashMap<>();

  protected final ReadLock m_readLock;
  protected final WriteLock m_writeLock;

  /**
   * New instances can be obtained using {@link HttpSessionHelper#getSessionStore(HttpSession)}.
   */
  protected SessionStore(HttpSession httpSession) {
    Assertions.assertNotNull(httpSession);
    m_httpSession = httpSession;
    m_httpSessionId = httpSession.getId();

    final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    m_readLock = lock.readLock();
    m_writeLock = lock.writeLock();
    BEANS.get(SessionMonitorMBean.class).weakRegister(httpSession);
    m_sessionMetrics.sessionCreated(SESSION_TYPE);
  }

  @Override
  public HttpSession getHttpSession() {
    return m_httpSession;
  }

  @Override
  public String getHttpSessionId() {
    return m_httpSessionId;
  }

  @Override
  public final boolean isHttpSessionValid() {
    return m_httpSessionValid;
  }

  /**
   * Can be used by subclasses to set {@link #m_httpSessionValid} to <code>false</code> (but not back to
   * <code>true</code>).
   */
  protected final void setHttpSessionInvalid() {
    m_httpSessionValid = false;
  }

  @Override
  public Map<String, IClientSession> getClientSessionMap() {
    m_readLock.lock();
    try {
      return new HashMap<>(m_clientSessionMap);
    }
    finally {
      m_readLock.unlock();
    }
  }

  @Override
  public Map<String, IUiSession> getUiSessionMap() {
    m_readLock.lock();
    try {
      return new HashMap<>(m_uiSessionMap);
    }
    finally {
      m_readLock.unlock();
    }
  }

  @Override
  public Map<IClientSession, Set<IUiSession>> getUiSessionsByClientSession() {
    m_readLock.lock();
    try {
      Map<IClientSession, Set<IUiSession>> copy = new HashMap<>();
      for (Entry<IClientSession, Set<IUiSession>> entry : m_uiSessionsByClientSession.entrySet()) {
        copy.put(entry.getKey(), (entry.getValue() == null ? null : new HashSet<>(entry.getValue())));
      }
      return copy;
    }
    finally {
      m_readLock.unlock();
    }
  }

  @Override
  public int countUiSessions() {
    m_readLock.lock();
    try {
      return m_uiSessionMap.size();
    }
    finally {
      m_readLock.unlock();
    }
  }

  @Override
  public int countClientSessions() {
    m_readLock.lock();
    try {
      return m_clientSessionMap.size();
    }
    finally {
      m_readLock.unlock();
    }
  }

  @Override
  public boolean isEmpty() {
    m_readLock.lock();
    try {
      return m_uiSessionMap.isEmpty() && m_preregisteredUiSessionMap.isEmpty() && m_clientSessionMap.isEmpty() && m_uiSessionsByClientSession.isEmpty();
    }
    finally {
      m_readLock.unlock();
    }
  }

  @Override
  public IUiSession getUiSession(String uiSessionId) {
    if (uiSessionId == null) {
      return null;
    }
    m_readLock.lock();
    try {
      return m_uiSessionMap.get(uiSessionId);
    }
    finally {
      m_readLock.unlock();
    }
  }

  @Override
  public IClientSession preregisterUiSession(IUiSession uiSession, String clientSessionId) {
    Assertions.assertNotNull(uiSession);
    String uiSessionId = uiSession.getUiSessionId();
    Assertions.assertNotNull(uiSessionId);
    LOG.debug("Pre-register UI session with ID {}", uiSessionId);
    m_writeLock.lock();
    try {
      Assertions.assertFalse(m_uiSessionMap.containsKey(uiSessionId), "This session store already contains the uiSessionId '{}'", uiSessionId);
      m_preregisteredUiSessionMap.put(uiSessionId, uiSession);

      if (clientSessionId == null) {
        return null;
      }

      // If housekeeping is scheduled for this session, cancel it (session will be used again, so no cleanup necessary)
      IFuture<?> future = m_housekeepingFutures.get(clientSessionId);
      if (future != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Client session with ID {} reserved for use - session housekeeping cancelled!", clientSessionId);
        }
        future.cancel(false);
        m_housekeepingFutures.remove(clientSessionId);
      }

      IClientSession clientSession = m_clientSessionMap.get(clientSessionId);
      if (clientSession == null || !clientSession.isActive() || clientSession.isStopping()) {
        // only return active sessions
        return null;
      }
      // Link preregistered ui sessions to existing client session
      m_preregisteredUiSessionsByClientSession
          .computeIfAbsent(clientSession, k -> new HashSet<>())
          .add(uiSession);
      return clientSession;
    }
    finally {
      m_writeLock.unlock();
    }
  }

  @Override
  public void registerUiSession(final IUiSession uiSession) {
    Assertions.assertNotNull(uiSession);
    LOG.debug("Register UI session with ID {} in store (clientSessionId={})", uiSession.getUiSessionId(), uiSession.getClientSessionId());
    m_writeLock.lock();
    try {
      IClientSession clientSession = uiSession.getClientSession();

      // Remove preregistered mappings
      m_preregisteredUiSessionMap.remove(uiSession.getUiSessionId());
      Set<IUiSession> map = m_preregisteredUiSessionsByClientSession.get(clientSession);
      if (map != null) {
        map.remove(uiSession);
        if (map.isEmpty()) {
          m_preregisteredUiSessionsByClientSession.remove(clientSession);
        }
      }

      // Store UI session
      m_uiSessionMap.put(uiSession.getUiSessionId(), uiSession);

      // Store client session (in case it was not yet stored)
      m_clientSessionMap.put(clientSession.getId(), clientSession);

      // Link to client session
      m_uiSessionsByClientSession
          .computeIfAbsent(clientSession, k -> new HashSet<>())
          .add(uiSession);
    }
    finally {
      m_writeLock.unlock();
    }
  }

  @Override
  public void unregisterUiSession(final IUiSession uiSession) {
    if (uiSession == null) {
      return;
    }
    LOG.debug("Unregister UI session with ID {} from store (clientSessionId={})", uiSession.getUiSessionId(), uiSession.getClientSessionId());
    m_writeLock.lock();
    try {
      // Remove uiSession
      m_preregisteredUiSessionMap.remove(uiSession.getUiSessionId());
      m_uiSessionMap.remove(uiSession.getUiSessionId());

      //Note: clientSession may be null if UiSession.init failed
      final IClientSession clientSession = uiSession.getClientSession();

      // Unlink uiSession from clientSession
      Set<IUiSession> preregisteredMap = m_preregisteredUiSessionsByClientSession.get(clientSession);
      if (preregisteredMap != null) {
        preregisteredMap.remove(uiSession);
        if (preregisteredMap.isEmpty()) {
          m_preregisteredUiSessionsByClientSession.remove(clientSession);
        }
      }
      Set<IUiSession> map = m_uiSessionsByClientSession.get(clientSession);
      if (map != null) {
        map.remove(uiSession);
        if (map.isEmpty()) {
          m_uiSessionsByClientSession.remove(clientSession);
        }
      }

      // Start housekeeping
      LOG.debug("{} UI sessions and {} preregistered UI session remaining for client session {}",
          (map == null ? 0 : map.size()),
          (preregisteredMap == null ? 0 : preregisteredMap.size()),
          (clientSession == null ? null : clientSession.getId()));
      if ((map == null || map.isEmpty()) && (preregisteredMap == null || preregisteredMap.isEmpty())) {
        if (uiSession.isPersistent()) {
          // don't start housekeeping for persistent sessions to give the users more time on app switches in ios home screen mode
          return;
        }
        startHousekeepingInsideWriteLock(clientSession);
      }
    }
    finally {
      m_writeLock.unlock();
    }
  }

  /**
   * If the given client session is still active, schedule a job that checks whether it is still in use after some time
   * (see {@link SessionStoreHousekeepingDelayProperty}). If not, it will be stopped and removed from the store. If the
   * session is inactive from the beginning, it is just removed from the store.
   * <p>
   * <b>Important:</b>: This method must be called from within a lock!
   */
  protected void startHousekeepingInsideWriteLock(final IClientSession clientSession) {
    // No client session, no housekeeping necessary
    if (clientSession == null) {
      return;
    }

    // Check if client session is still used after a few moments
    LOG.debug("Session housekeeping: Schedule job for client session with ID {}", clientSession.getId());
    final IFuture<Void> future = Jobs.schedule(() -> doHousekeepingOutsideWriteLock(clientSession), Jobs.newInput()
        .withName("Performing session housekeeping for client session with ID {}", clientSession.getId())
        .withExceptionHandling(BEANS.get(SessionHousekeepingExceptionHandler.class), true)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(CONFIG.getPropertyValue(SessionStoreHousekeepingDelayProperty.class), TimeUnit.SECONDS)));

    // Put the future in a list, so we can cancel it if the session is requested again
    m_housekeepingFutures.put(clientSession.getId(), future);
  }

  /**
   * Checks if the client session is still used by a UI session. If not, it is stopped and removed from the store.
   */
  protected void doHousekeepingOutsideWriteLock(final IClientSession clientSession) {
    m_writeLock.lock();
    try {
      if (IFuture.CURRENT.get() != null && IFuture.CURRENT.get().isCancelled()) {
        return;
      }
      IFuture<?> otherFuture = m_housekeepingFutures.remove(clientSession.getId());
      if (otherFuture != null) {
        otherFuture.cancel(false);
      }

      if (!clientSession.isActive() || clientSession.isStopping()) {
        LOG.info("Session housekeeping: Client session {} is {}, removing it from store", clientSession.getId(), (!clientSession.isActive() ? "inactive" : "stopping"));
        removeClientSessionInsideWriteLock(clientSession);
        return;
      }

      // Check if the client session is referenced by any UI session
      Set<IUiSession> uiSessions = m_uiSessionsByClientSession.get(clientSession);
      Set<IUiSession> preregisteredUiSessions = m_preregisteredUiSessionsByClientSession.get(clientSession);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Session housekeeping: Client session {} referenced by {} UI sessions and {} UI session types",
            clientSession.getId(),
            (uiSessions == null ? 0 : uiSessions.size()),
            (preregisteredUiSessions == null ? 0 : preregisteredUiSessions.size()));
      }
      if ((uiSessions == null || uiSessions.isEmpty()) && (preregisteredUiSessions == null || preregisteredUiSessions.isEmpty())) {
        LOG.info("Session housekeeping: Shutting down client session with ID {} because it is not used anymore", clientSession.getId());
        removeClientSessionInsideWriteLock(clientSession);
      }
    }
    finally {
      m_writeLock.unlock();
      checkHttpSessionOutsideWriteLock();
      BEANS.get(ClientSessionStopHelper.class).scheduleStop(clientSession, true, "session housekeeping");
    }
  }

  protected void removeClientSessionInsideWriteLock(final IClientSession clientSession) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Remove client session with ID {} from session store", clientSession.getId());
    }
    m_clientSessionMap.remove(clientSession.getId());
    if (LOG.isDebugEnabled()) {
      Set<IClientSession> flatClientSessions = new HashSet<>();
      flatClientSessions.addAll(m_uiSessionsByClientSession.keySet());
      flatClientSessions.addAll(m_preregisteredUiSessionsByClientSession.keySet());

      Set<IUiSession> uiSessionsByClientSession = new HashSet<>();
      for (Set<IUiSession> s : m_uiSessionsByClientSession.values()) {
        uiSessionsByClientSession.addAll(s);
      }

      Set<IUiSession> preregisteredUiSessionsByClientSession = new HashSet<>();
      for (Set<IUiSession> s : m_preregisteredUiSessionsByClientSession.values()) {
        preregisteredUiSessionsByClientSession.addAll(s);
      }

      LOG.debug("Remaining sessions: [clientSessions: {}, clientSessionFlat: {}, uiSessions: {}, uiSessionsByClientSession: {}, preregisteredUiSessions: {}, preregisteredUiSessionsByClientSession: {}]",
          m_clientSessionMap.size(), flatClientSessions.size(), m_uiSessionMap.size(), uiSessionsByClientSession.size(), m_preregisteredUiSessionMap.size(), preregisteredUiSessionsByClientSession.size());
    }
  }

  @Override
  public void valueBound(HttpSessionBindingEvent event) {
    // ignore notifications about being bound to an HTTP session
  }

  @Override
  public void valueUnbound(final HttpSessionBindingEvent event) {
    if (!m_httpSessionValid) {
      // valueUnbound() has already been executed
      return;
    }
    m_httpSessionValid = false;
    LOG.info("Detected invalidation of HTTP session {}, cleaning up {} client sessions and {} UI sessions", m_httpSessionId, m_clientSessionMap.size(), m_uiSessionMap.size());

    List<IClientSession> clientSessionList = new ArrayList<>();
    m_writeLock.lock();
    try {
      clientSessionList.addAll(m_clientSessionMap.values());
      for (IUiSession uiSession : new ArrayList<>(m_uiSessionMap.values())) {
        uiSession.dispose();
        //this will schedule a housekeeping job that may start now or later
      }
    }
    catch (Throwable t) {
      // catch exceptions so that the container is not affected. Otherwise, the unbound for other values may not be called (container dependent).
      LOG.warn("Unable to dispose ui session for http session id {}", m_httpSessionId, t);
    }
    finally {
      m_writeLock.unlock();
      for (IClientSession clientSession : clientSessionList) {
        //the housekeeping may run immediately, this call may cancel a pending housekeeping job iff it did not start already
        doHousekeepingOutsideWriteLock(clientSession);
      }

      m_sessionMetrics.sessionDestroyed(SESSION_TYPE);
    }
  }

  protected void checkHttpSessionOutsideWriteLock() {
    m_writeLock.lock();
    try {
      if (!(m_clientSessionMap.isEmpty() && m_preregisteredUiSessionMap.isEmpty() && m_httpSessionValid)) {
        return;
      }
      // Check if everything was cleaned up correctly ("leak detection").
      int uiSessionMapSize = m_uiSessionMap.size();
      int uiSessionsByClientSessionSize = m_uiSessionsByClientSession.size();
      if (uiSessionMapSize != 0 || uiSessionsByClientSessionSize != 0) {
        LOG.warn("Leak detection - Session store not empty before HTTP session invalidation: [uiSessionMap: {}, uiSessionsByClientSession: {}]",
            uiSessionMapSize, uiSessionsByClientSessionSize);
      }
      // no more sessions -> exit lock and invalidate HTTP session
    }
    finally {
      m_writeLock.unlock();
    }

    try {
      m_httpSession.getCreationTime(); // dummy call to prevent the following log statement when the session is already invalid
      LOG.info("Invalidate HTTP session with ID {} because session store contains no more client sessions", m_httpSessionId);
      m_httpSession.invalidate();
    }
    catch (IllegalStateException e) { // NOSONAR
      // already invalid
    }
  }

  public static class SessionHousekeepingExceptionHandler extends ExceptionHandler {
    @Override
    protected void handleInterruptedException(ThreadInterruptedError e) {
      // use warn log level if the session housekeeping fails
      // otherwise session shutdown issues may not be discovered
      LOG.warn("Session housekeeping failed.", e);
    }
  }
}
