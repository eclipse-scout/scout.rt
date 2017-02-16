package org.eclipse.scout.rt.ui.html;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.server.commons.HttpSessionMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 5.2
 */
@ApplicationScoped
public class HttpSessionHelper {
  private static final Logger LOG = LoggerFactory.getLogger(HttpSessionHelper.class);

  public static final String SESSION_STORE_ATTRIBUTE_NAME = "scout.htmlui.httpsession.sessionstore";

  /**
   * Gets the session store from the given HTTP session. If there is no store available for the given session a new one
   * will be created.
   *
   * @return The {@link ISessionStore}. If there is no {@link ISessionStore} registered for the given
   *         {@link HttpSession} a new one will be created and registered. Never returns {@code null}.
   * @throws AssertionException
   *           if the given HTTP session is <code>null</code>.
   * @throws IllegalStateException
   *           if the given HTTP session is invalid.
   */
  @SuppressWarnings("findbugs:J2EE_STORE_OF_NON_SERIALIZABLE_OBJECT_INTO_SESSION")
  public ISessionStore getSessionStore(HttpSession httpSession) {
    ISessionStore sessionStore = getSessionStoreFromHttpSession(assertNotNull(httpSession));
    if (sessionStore != null) {
      return sessionStore;
    }

    synchronized (HttpSessionMutex.of(httpSession)) {
      sessionStore = getSessionStoreFromHttpSession(httpSession);
      if (sessionStore != null) {
        return sessionStore;
      }

      sessionStore = createSessionStore(httpSession);
      httpSession.setAttribute(SESSION_STORE_ATTRIBUTE_NAME, sessionStore);
      LOG.debug("Created new session store for HTTP session with ID {}", httpSession.getId());

      return sessionStore;
    }
  }

  /**
   * Gets the {@link ISessionStore} associated with the given {@link HttpSession}.
   *
   * @param httpSession
   *          The {@link HttpSession} for which the store should be returned.
   * @return The {@link ISessionStore} for the given {@link HttpSession} or {@code null} if there is no store associated
   *         yet.
   */
  protected ISessionStore getSessionStoreFromHttpSession(HttpSession httpSession) {
    return (ISessionStore) httpSession.getAttribute(SESSION_STORE_ATTRIBUTE_NAME);
  }

  /**
   * Creates a new instance of the session store.
   */
  protected ISessionStore createSessionStore(HttpSession httpSession) {
    return new SessionStore(httpSession);
  }
}
