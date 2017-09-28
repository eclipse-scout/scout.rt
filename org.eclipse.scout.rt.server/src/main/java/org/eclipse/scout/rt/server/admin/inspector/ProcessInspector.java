/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.admin.inspector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.collection.ConcurrentExpiringMap;
import org.eclipse.scout.rt.server.IServerSession;

/**
 * process monitor of servlet context
 */
@ApplicationScoped
public class ProcessInspector {

  /**
   * Convenience for {@link BEANS#get(Class)}
   */
  public static ProcessInspector instance() {
    return BEANS.get(ProcessInspector.class);
  }

  /**
   * Instance
   */
  private boolean m_servletInspectorEnabled = false;
  private final Object m_servletInspectorEnabledLock = new Object();
  private volatile ConcurrentExpiringMap<IServerSession, SessionInspector> m_sessionToInspectorMap;
  private final Set<String> m_ignoredCallSet;

  public ProcessInspector() {
    m_sessionToInspectorMap = new ConcurrentExpiringMap<>(2, TimeUnit.MINUTES);
    m_ignoredCallSet = Collections.synchronizedSet(new HashSet<String>());
  }

  public boolean isEnabled() {
    synchronized (m_servletInspectorEnabledLock) {
      return m_servletInspectorEnabled;
    }
  }

  public void setEnabled(boolean b) {
    synchronized (m_servletInspectorEnabledLock) {
      m_servletInspectorEnabled = b;
      if (!b) {
        clearSessionInspectors();
      }
    }
  }

  public Set<String> getIgnoredCallSet() {
    return m_ignoredCallSet;
  }

  public boolean acceptCall(String serviceClassName, String operation) {
    String callId = serviceClassName + "." + operation;
    for (String s : m_ignoredCallSet) {
      if (callId.matches(s)) {
        return false;
      }
    }
    return true;
  }

  public void update() {
    for (SessionInspector i : getSessionInspectors()) {
      i.doHousekeeping(getTimeout());
    }
  }

  public long getTimeout() {
    return m_sessionToInspectorMap.getTimeToLive();
  }

  public void setTimeout(long timeoutMillis) {
    m_sessionToInspectorMap = new ConcurrentExpiringMap<>(m_sessionToInspectorMap, timeoutMillis, TimeUnit.MILLISECONDS);
  }

  public SessionInspector[] getSessionInspectors(String user) {
    ArrayList<SessionInspector> a = new ArrayList<>();
    for (SessionInspector si : m_sessionToInspectorMap.values()) {
      if (("" + user).equalsIgnoreCase("" + si.getInfo().getUserId())) {
        a.add(si);
      }
    }
    return a.toArray(new SessionInspector[0]);
  }

  public SessionInspector[] getSessionInspectors() {
    return m_sessionToInspectorMap.values().toArray(new SessionInspector[0]);
  }

  public SessionInspector getSessionInspector(IServerSession session, boolean autoCreate) {
    SessionInspector insp = m_sessionToInspectorMap.get(session);
    if (insp == null && isEnabled() && autoCreate) {
      insp = new SessionInspector(this, session);
      SessionInspector previousInsp = m_sessionToInspectorMap.putIfAbsent(session, insp);
      if (previousInsp != null) {
        // some other thread just created an other inspector and put it in the map. Use this one
        insp = previousInsp;
      }
    }
    return insp;
  }

  public void clearSessionInspectors(String user) {
    for (Iterator<Entry<IServerSession, SessionInspector>> iterator = m_sessionToInspectorMap.entrySet().iterator(); iterator.hasNext();) {
      Entry<IServerSession, SessionInspector> entry = iterator.next();
      SessionInspector session = entry.getValue();
      if (("" + user).equalsIgnoreCase("" + session.getInfo().getUserId())) {
        iterator.remove();
      }
    }
  }

  public void clearSessionInspectors() {
    m_sessionToInspectorMap.clear();
  }
}
