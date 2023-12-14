/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.session;

import java.io.Serializable;

import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;
import jakarta.servlet.http.HttpSessionListener;

import org.eclipse.scout.rt.platform.BEANS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HttpSessionListener} to call sessionDestroyer on valueUnbound
 */
public class ScoutSessionBindingListener implements HttpSessionBindingListener, Serializable {
  private static final long serialVersionUID = -7050061432903624702L;
  private static final Logger LOG = LoggerFactory.getLogger(ScoutSessionBindingListener.class);
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
    try {
      BEANS.get(ServerSessionCache.class).removeHttpSession(m_scoutSessionId, event.getSession().getId() /* do not pass the session here as it is invalid already and should not be used anymore */);
    }
    catch (Throwable e) {
      // catch exceptions so that the container is not affected. Otherwise, the unbound for other values may not be called (container dependent).
      LOG.warn("Unable to remove http session for scout session id {}.", m_scoutSessionId, e);
    }
  }
}
