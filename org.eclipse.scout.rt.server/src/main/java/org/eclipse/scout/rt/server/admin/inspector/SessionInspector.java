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

import java.security.AccessController;
import java.util.ArrayList;
import java.util.Iterator;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.admin.inspector.info.SessionInfo;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.ui.UserAgent;

public class SessionInspector {
  private final ProcessInspector m_parent;
  private final IServerSession m_session;
  private final SessionInfo m_info;
  private final Object m_callListLock = new Object();
  private final ArrayList<CallInspector> m_callList = new ArrayList<>();

  protected SessionInspector(ProcessInspector parent, IServerSession session) {
    m_parent = parent;
    m_session = session;
    m_info = new SessionInfo();

    m_info.setSessionId(session.getId());
    m_info.setUserId(session.getUserId());
    m_info.setUserAgent(UserAgent.CURRENT.get());

    HttpServletRequest httpReq = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
    if (httpReq != null) {
      HttpSession httpSession = httpReq.getSession();
      if (httpSession != null) {
        m_info.setCreationTime(httpSession.getCreationTime());
        m_info.setLastAccessedTime(httpSession.getLastAccessedTime());
      }
    }

    try {
      m_info.setSubject(Subject.getSubject(AccessController.getContext()));
    }
    catch (Exception e) { // NOSONAR
    }
  }

  public ProcessInspector getProcessInspector() {
    return m_parent;
  }

  public IServerSession getServerSession() {
    return m_session;
  }

  public SessionInfo getInfo() {
    return m_info;
  }

  /**
   * requests on this session
   */
  public CallInspector[] getCallInspectors() {
    synchronized (m_callListLock) {
      return m_callList.toArray(new CallInspector[0]);
    }
  }

  public CallInspector requestCallInspector(ServiceTunnelRequest call) {
    synchronized (m_callListLock) {
      if (getProcessInspector().isEnabled() && getProcessInspector().acceptCall(call.getServiceInterfaceClassName(), call.getOperation())) {
        CallInspector callInspector = new CallInspector(this, call);
        m_callList.add(callInspector);
        return callInspector;
      }
    }
    return null;
  }

  public void clearCallInspectors() {
    synchronized (m_callListLock) {
      m_callList.clear();
    }
  }

  public void doHousekeeping(long timeoutMillis) {
    manageCallInspectorList(timeoutMillis);
  }

  public void update() {
    // do nothing
  }

  private void manageCallInspectorList(long timeoutMillis) {
    synchronized (m_callListLock) {
      for (Iterator it = m_callList.iterator(); it.hasNext();) {
        CallInspector c = (CallInspector) it.next();
        if (c.isTimeout(timeoutMillis)) {
          it.remove();
        }
        else {
          c.update();
        }
      }
    }
  }
}
