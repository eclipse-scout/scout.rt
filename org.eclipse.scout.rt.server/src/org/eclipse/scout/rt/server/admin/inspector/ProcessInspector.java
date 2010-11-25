/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.TTLCache;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.internal.Activator;

/**
 * process monitor of servlet context
 */
public class ProcessInspector {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ProcessInspector.class);

  public static synchronized ProcessInspector getDefault() {
    return Activator.getDefault().getProcessInspector();
  }

  /**
   * Instance
   */
  private boolean m_servletInspectorEnabled = false;
  private TTLCache<IServerSession, SessionInspector> m_sessionToInspectorMap;
  private Object m_sessionToInspectorMapLock;
  private Set<String> m_ignoredCallSet;

  public ProcessInspector() {
    m_sessionToInspectorMapLock = new Object();
    m_sessionToInspectorMap = new TTLCache<IServerSession, SessionInspector>(120000L);
    m_ignoredCallSet = new HashSet<String>();
  }

  public boolean isEnabled() {
    return m_servletInspectorEnabled;
  }

  public void setEnabled(boolean b) {
    m_servletInspectorEnabled = b;
    if (b) {
      // nop
    }
    else {
      clearSessionInspectors();
    }
  }

  public Set<String> getIgnoredCallSet() {
    return m_ignoredCallSet;
  }

  public boolean acceptCall(String serviceClassName, String operation) {
    String callId = serviceClassName + "." + operation;
    for (String s : m_ignoredCallSet) {
      if (callId.matches(s)) return false;
    }
    return true;
  }

  public void update() {
    for (SessionInspector i : getSessionInspectors()) {
      i.doHousekeeping(getTimeout());
    }
  }

  public long getTimeout() {
    return m_sessionToInspectorMap.getTTL();
  }

  public void setTimeout(long timeoutMillis) {
    m_sessionToInspectorMap.setTTL(timeoutMillis);
  }

  public SessionInspector[] getSessionInspectors(String user) {
    synchronized (m_sessionToInspectorMapLock) {
      ArrayList<SessionInspector> a = new ArrayList<SessionInspector>();
      for (SessionInspector si : m_sessionToInspectorMap.values()) {
        if (("" + user).equalsIgnoreCase("" + si.getInfo().getUserId())) {
          a.add(si);
        }
      }
      return a.toArray(new SessionInspector[0]);
    }
  }

  public SessionInspector[] getSessionInspectors() {
    synchronized (m_sessionToInspectorMapLock) {
      return m_sessionToInspectorMap.values().toArray(new SessionInspector[0]);
    }
  }

  public SessionInspector getSessionInspector(IServerSession session, boolean autoCreate) {
    synchronized (m_sessionToInspectorMapLock) {
      SessionInspector insp = m_sessionToInspectorMap.get(session);
      if (insp == null && isEnabled() && autoCreate) {
        insp = new SessionInspector(this, session);
        m_sessionToInspectorMap.put(session, insp);
      }
      return insp;
    }
  }

  public void clearSessionInspectors(String user) {
    for (IServerSession key : m_sessionToInspectorMap.keySet()) { // ttlcach uses
      // unlinked
      // keyset,
      // therefor we
      // can simple
      // iterate and
      // delete
      SessionInspector session = m_sessionToInspectorMap.get(key);
      if (("" + user).equalsIgnoreCase("" + session.getInfo().getUserId())) {
        m_sessionToInspectorMap.remove(key);
      }
    }
  }

  public void clearSessionInspectors() {
    synchronized (m_sessionToInspectorMapLock) {
      for (IServerSession session : m_sessionToInspectorMap.keySet()) { // ttlcach
        // uses
        // unlinked
        // keyset,
        // therefor
        // we can
        // simple
        // iterate
        // and
        // delete
        m_sessionToInspectorMap.remove(session);
      }
    }
  }
}
