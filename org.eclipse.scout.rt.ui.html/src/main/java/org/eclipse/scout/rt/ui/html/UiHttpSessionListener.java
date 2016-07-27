package org.eclipse.scout.rt.ui.html;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.eclipse.scout.rt.platform.BEANS;

/**
 * Listener that "prepares" every new the HTTP. The preparation consists of the eager creation of some session
 * attributes that can then always be retrieved from the HTTP session without special synchronization.
 *
 * @see {@link HttpSessionHelper#prepareHttpSession(javax.servlet.http.HttpSession)}
 * @since 5.2
 */
public class UiHttpSessionListener implements HttpSessionListener {

  @Override
  public void sessionCreated(HttpSessionEvent event) {
    BEANS.get(HttpSessionHelper.class).prepareHttpSession(event.getSession());
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
    // ignore notifications about destroyed HTTP sessions. SessionStore is responsible for acting on them
  }
}
