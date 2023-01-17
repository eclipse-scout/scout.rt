/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.admin.inspector;

import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.server.admin.inspector.info.CallInfo;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;

public class CallInspector {
  private final SessionInspector m_parent;
  private final CallInfo m_info;

  protected CallInspector(SessionInspector parent, ServiceTunnelRequest call) {
    m_parent = parent;
    m_info = new CallInfo();
    m_info.setStartTime(System.currentTimeMillis());
    m_info.setService(call.getServiceInterfaceClassName());
    m_info.setOperation(call.getOperation());
    Object[] args = call.getArgs();
    Object[] newArgs = new Object[(args != null ? args.length : 0)];
    if (newArgs.length > 0) {
      System.arraycopy(args, 0, newArgs, 0, newArgs.length);
    }
    m_info.setArguments(newArgs);
  }

  public SessionInspector getSessionInspector() {
    return m_parent;
  }

  public boolean isTimeout(long millis) {
    if (!m_info.isActive()) {
      long dt = System.currentTimeMillis() - m_info.getEndTime();
      if (dt >= millis) {
        return true;
      }
    }
    return false;
  }

  public void close(ServiceTunnelResponse res) {
    update();
    m_info.setEndTime(System.currentTimeMillis());
    if (res != null) {
      m_info.setReturnData(res.getData());
      m_info.setReturnException(res.getException());
    }
  }

  public void update() {
    ITransaction xa = ITransaction.CURRENT.get();
    if (xa != null) {
      m_info.setXaResources(xa.getMembers());
    }
  }

  public CallInfo getInfo() {
    return m_info;
  }

}
