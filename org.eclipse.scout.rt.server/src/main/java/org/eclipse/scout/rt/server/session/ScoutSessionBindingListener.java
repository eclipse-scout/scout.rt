package org.eclipse.scout.rt.server.session;

import java.io.Serializable;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionListener;

import org.eclipse.scout.rt.platform.BEANS;

/**
 * {@link HttpSessionListener} to call sessionDestroyer on valueUnbound
 */
public class ScoutSessionBindingListener implements HttpSessionBindingListener, Serializable {
  private static final long serialVersionUID = -7050061432903624702L;
  private final String m_scoutSessionId;

  public ScoutSessionBindingListener(String scoutSessionId) {
    m_scoutSessionId = scoutSessionId;
  }

  @Override
  public void valueBound(final HttpSessionBindingEvent event) {
    // NOOP
  }

  @Override
  public void valueUnbound(final HttpSessionBindingEvent event) {
    BEANS.get(ServerSessionCache.class).removeHttpSession(m_scoutSessionId, event.getSession().getId());
  }
}
