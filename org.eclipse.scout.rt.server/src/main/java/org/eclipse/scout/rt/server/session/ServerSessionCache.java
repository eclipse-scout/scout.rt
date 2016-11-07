package org.eclipse.scout.rt.server.session;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.server.IServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h3>Cache for {@link IServerSession}.</h3>
 * <p>
 * The Scout {@link IServerSession} is cached on the {@link HttpSession} and stopped and removed, when the
 * {@link HttpSession} expires.
 * </p>
 * <p>
 * This cache avoids creating multiple {@link IServerSession}s for the same Scout sessionId, even, if the http sessions
 * are not the same. There may be different HttpSessions for the same sessionId, if the serverSession is expired and
 * there are multiple requests from in parallel by the same user (with the same client session). To achieve this, the
 * {@link ServerSessionEntry} cosisting of scout session, httpSessionIds using this session, as well as the
 * destructionCallback for the Scout server session are cached in this service.
 * </p>
 */
@ApplicationScoped
public class ServerSessionCache {

  private static final Logger LOG = LoggerFactory.getLogger(ServerSessionCache.class);

  //key to store server session on HttpSession
  public static final String SERVER_SESSION_KEY = IServerSession.class.getName();
  public static final String UNBIND_LISTENER_KEY = "scout.httpsession.binding.listener";

  private final ConcurrentMap<String, ServerSessionEntry> m_sessionContexts = new ConcurrentHashMap<>();

  /**
   * Looks up the scout session on the given {@link HttpSession} stored in an attribute with key IServerSession. Creates
   * a new scout session using the given sessionProvider, if none exists.
   *
   * @param sessionLifecycleHandler
   *          for creating and destroying scout sessions
   * @param httpSession
   *          {@link HttpSession}
   * @return new or existing {@link IServerSession}
   */
  public IServerSession getOrCreate(IServerSessionLifecycleHandler sessionLifecycleHandler, HttpSession httpSession) {
    Object scoutSession = httpSession.getAttribute(SERVER_SESSION_KEY);
    if (scoutSession instanceof IServerSession) {
      return (IServerSession) scoutSession;
    }

    //lock by scout sessionId to prevent creation of scout session more than once per scoutSessionId
    ServerSessionEntry sessionContext = getSessionContext(sessionLifecycleHandler.getId(), sessionLifecycleHandler);

    synchronized (sessionContext) {
      IServerSession session = sessionContext.getOrCreateScoutSession();
      sessionContext.addHttpSessionId(httpSession.getId());
      httpSession.setAttribute(SERVER_SESSION_KEY, session);
      httpSession.setAttribute(UNBIND_LISTENER_KEY, new ScoutSessionBindingListener(sessionLifecycleHandler.getId()));

      if (LOG.isDebugEnabled()) {
        LOG.debug("Scout ServerSession Session added to HttpSession [scoutSessionId={}, httpSessionId={}]", sessionLifecycleHandler.getId(), httpSession.getId());
      }
      return session;
    }
  }

  /**
   * Only one ScoutServerSessionContext object per sessionId must exist
   */
  protected ServerSessionEntry getSessionContext(String sessionId, IServerSessionLifecycleHandler sessionLifecycleHandle) {
    ServerSessionEntry newSessionContext = new ServerSessionEntry(sessionLifecycleHandle);
    ServerSessionEntry existingSessionContext = m_sessionContexts.putIfAbsent(sessionId, newSessionContext);
    return (existingSessionContext == null) ? newSessionContext : existingSessionContext;
  }

  /**
   * Remove httpsession and destroy the scout session, if no more {@link HttpSession}s for this scout session are
   * available.
   */
  public void removeHttpSession(String scoutSessionId, String httpSessionId) {
    ServerSessionEntry scoutSessionContext = m_sessionContexts.get(scoutSessionId);
    if (scoutSessionContext == null) {
      LOG.error("Unknown sessionContext, id={}", scoutSessionId);

    }
    else {
      synchronized (scoutSessionContext) {
        scoutSessionContext.removeHttpSession(httpSessionId);
        if (LOG.isDebugEnabled()) {
          LOG.debug("Scout ServerSession removed from HttpSession [scoutSessionId={}, httpSessionId={}]", scoutSessionId, httpSessionId);
        }

        //destroy scout session, if there is no httpsession with this scout session
        if (scoutSessionContext.hasNoMoreHttpSessions()) {
          scoutSessionContext.destroy();
          m_sessionContexts.remove(scoutSessionId);
          if (LOG.isDebugEnabled()) {
            LOG.debug("Removed scout session from cache [id={}]", scoutSessionId);
          }
        }
      }
    }
  }

}
