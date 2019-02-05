package org.eclipse.scout.rt.ui.html;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.eclipse.scout.rt.server.commons.HttpSessionMutex;

/**
 * Listener that "prepares" every new the HTTP. The preparation consists of the eager creation of some session
 * attributes that can then always be retrieved from the HTTP session without special synchronization.
 *
 * @see {@link HttpSessionHelper#prepareHttpSession(javax.servlet.http.HttpSession)}
 * @since 5.2
 * @deprecated use {@link HttpSessionMutex}
 */
@Deprecated
public class UiHttpSessionListener implements HttpSessionListener {
  private final HttpSessionMutex m_delegate = new HttpSessionMutex();

  @Override
  public void sessionCreated(HttpSessionEvent event) {
    m_delegate.sessionCreated(event);
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
    m_delegate.sessionDestroyed(event);
  }
}
