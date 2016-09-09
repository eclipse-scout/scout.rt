package org.eclipse.scout.rt.ui.html;

import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
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
   * Creates some objects and adds them as attributes to the HTTP session. After that, the objects can always be
   * retrieved from the HTTP session without checking for their existence or the need for explicit synchronization.
   *
   * @throws AssertionException
   *           if the given HTTP session is <code>null</code>.
   * @throws IllegalStateException
   *           if the given HTTP session is invalid.
   */
  @SuppressWarnings("findbugs:J2EE_STORE_OF_NON_SERIALIZABLE_OBJECT_INTO_SESSION")
  public void prepareHttpSession(HttpSession httpSession) {
    Assertions.assertNotNull(httpSession);

    ISessionStore sessionStore = createSessionStore(httpSession);
    httpSession.setAttribute(SESSION_STORE_ATTRIBUTE_NAME, sessionStore);

    LOG.debug("Prepared new HTTP session {}", httpSession.getId());
  }

  /**
   * Creates a new instance of the session store.
   */
  protected ISessionStore createSessionStore(HttpSession httpSession) {
    return new SessionStore(httpSession);
  }

  /**
   * Gets the session store from the given HTTP session. If the session was not "prepared" beforehand, <code>null</code>
   * is returned (see {@link #prepareHttpSession(HttpSession)}). An exception is thrown if the session is not valid.
   *
   * @throws AssertionException
   *           if the given HTTP session is <code>null</code>.
   * @throws IllegalStateException
   *           if the given HTTP session is invalid.
   */
  public ISessionStore getSessionStore(HttpSession httpSession) {
    Assertions.assertNotNull(httpSession);
    ISessionStore sessionStore = (ISessionStore) httpSession.getAttribute(SESSION_STORE_ATTRIBUTE_NAME);
    if (sessionStore == null) {
      warnMissingAttribute(httpSession, SESSION_STORE_ATTRIBUTE_NAME);
    }
    return sessionStore;
  }

  protected void warnMissingAttribute(HttpSession httpSession, String attributeName) {
    LOG.warn("Could not find the expected session attribute '{}' on the HTTP session with ID {}. Check that {} is correctly "
        + "registered as listener in your web.xml, or ensure that the HTTP session will be prepared by other means.",
        attributeName, httpSession.getId(), UiHttpSessionListener.class.getName());
  }
}
