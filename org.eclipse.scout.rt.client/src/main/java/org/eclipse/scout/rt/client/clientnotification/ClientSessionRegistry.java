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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.shared.SharedConfigProperties.NotificationSubjectProperty;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnel;
import org.eclipse.scout.rt.shared.session.ISessionListener;
import org.eclipse.scout.rt.shared.session.SessionEvent;

/**
 *
 */
public class ClientSessionRegistry implements IClientSessionRegistry {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientSessionRegistry.class);

  protected final Subject NOTIFICATION_SUBJECT = CONFIG.getPropertyValue(NotificationSubjectProperty.class);

  private Object m_cacheLock = new Object();
  private final Map<String /*sessionId*/, WeakReference<IClientSession>> m_sessionIdToSession = new HashMap<>();
  private final Map<String /*userId*/, List<WeakReference<IClientSession>>> m_userToSessions = new HashMap<>();

  private final ISessionListener m_clientSessionStateListener = new P_ClientSessionStateListener();

  @Override
  public void register(IClientSession session, String sessionId) {
    synchronized (m_cacheLock) {
      m_sessionIdToSession.put(sessionId, new WeakReference<IClientSession>(session));
    }
    if (BEANS.get(IServiceTunnel.class).isActive() && BEANS.opt(IClientNotificationService.class) != null) {
      session.addListener(m_clientSessionStateListener);
      // if the client session is already started, otherwise the listener will invoke the clientSessionStated method.
      if (session.isActive()) {
        sessionStarted(session);
      }
    }
  }

  /**
   * this method is expected to be called in the context of the specific session.
   *
   * @param session
   */
  protected void clientSessionStopping(IClientSession session) {
    String userId = session.getUserId();
    LOG.debug(String.format("client session [%s] stopping", userId));
    session.removeListener(m_clientSessionStateListener);
    // unregister user remote
    try {
      RunContexts.empty().subject(NOTIFICATION_SUBJECT).run(new IRunnable() {
        @Override
        public void run() throws Exception {
          BEANS.get(IClientNotificationService.class).unregisterSession(NOTIFICATION_NODE_ID);
        }
      });
    }
    catch (ProcessingException e) {
      LOG.warn(String.format("Could not unregister session[%s] for remote notifications.", session), e);
    }
    // client session household
    synchronized (m_sessionIdToSession) {
      m_sessionIdToSession.remove(session.getId());
    }
  }

  /**
   * this method is expected to be called in the context of the specific session.
   *
   * @param session
   * @throws ProcessingException
   */
  public void sessionStarted(final IClientSession session) {

    LOG.debug(String.format("client session [sessionid=%s, userId=%s] started", session.getId(), session.getUserId()));
    // lookup the userid remote because the user is not necessarily set on the client session.
    BEANS.get(IPingService.class).ping("ensure shared context is loaded...");
    // local linking
    synchronized (m_cacheLock) {
      List<WeakReference<IClientSession>> sessionRefs = m_userToSessions.get(session.getUserId());
      if (sessionRefs != null) {
        // clean cache
        boolean toBeAdded = true;
        Iterator<WeakReference<IClientSession>> sessionRefIt = sessionRefs.iterator();
        while (sessionRefIt.hasNext()) {
          WeakReference<IClientSession> sessionRef = sessionRefIt.next();
          if (sessionRef.get() == null) {
            sessionRefIt.remove();
          }
          else if (sessionRef.get() == session) {
            // already registered
            toBeAdded = false;
          }
        }
        if (toBeAdded) {
          sessionRefs.add(new WeakReference<>(session));
        }
      }
      else {
        sessionRefs = new LinkedList<>();
        sessionRefs.add(new WeakReference<>(session));
        m_userToSessions.put(session.getUserId(), sessionRefs);
      }
    }
    // register on backend
    try {
      RunContexts.empty().subject(NOTIFICATION_SUBJECT).run(new IRunnable() {
        @Override
        public void run() throws Exception {
          BEANS.get(IClientNotificationService.class).registerSession(NOTIFICATION_NODE_ID, session.getId(), session.getUserId());
        }
      });
    }
    catch (ProcessingException e) {
      LOG.warn(String.format("Could not register session[%s] for remote notifications.", session), e);
    }
  }

  @Override
  public IClientSession getClientSession(String sessionid) {
    synchronized (m_cacheLock) {
      WeakReference<IClientSession> sessionRef = m_sessionIdToSession.get(sessionid);
      if (sessionRef.get() != null) {
        return sessionRef.get();
      }
      else {
        m_sessionIdToSession.remove(sessionid);
      }
    }
    return null;
  }

  @Override
  public List<IClientSession> getClientSessionsForUser(String userId) {
    List<IClientSession> result = new LinkedList<>();
    synchronized (m_cacheLock) {
      List<WeakReference<IClientSession>> userSessions = m_userToSessions.get(userId);
      Iterator<WeakReference<IClientSession>> refIt = userSessions.iterator();
      while (refIt.hasNext()) {
        WeakReference<IClientSession> sessionRef = refIt.next();
        if (sessionRef.get() != null) {
          result.add(sessionRef.get());
        }
        else {
          refIt.remove();
        }
      }
    }
    return result;
  }

  @Override
  public List<IClientSession> getAllClientSessions() {
    List<IClientSession> result = new LinkedList<IClientSession>();
    synchronized (m_cacheLock) {
      for (Entry<String, WeakReference<IClientSession>> e : m_sessionIdToSession.entrySet()) {
        if (e.getValue().get() != null) {
          result.add(e.getValue().get());
        }
        else {
          m_sessionIdToSession.remove(e.getKey());
        }
      }
    }
    return result;
  }

  private class P_ClientSessionStateListener implements ISessionListener {

    @Override
    public void sessionChanged(SessionEvent event) {
      switch (event.getType()) {
        case SessionEvent.TYPE_STARTED:
          sessionStarted((IClientSession) event.getSource());
          break;
        case SessionEvent.TYPE_STOPPED:
          clientSessionStopping((IClientSession) event.getSource());
        default:
          break;
      }
    }
  }

}
