/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.admin.inspector.info;

import org.eclipse.scout.rt.platform.transaction.ITransactionMember;

public class CallInfo {
  private String m_service;
  private String m_operation;
  private Object[] m_arguments;
  private ITransactionMember[] m_xaResources;
  private long m_startTime;
  private long m_endTime;
  private Object m_returnData;
  private Throwable m_returnException;
  private Object[] m_outVariables;

  public String getService() {
    return m_service;
  }

  public void setService(String s) {
    m_service = s;
  }

  public String getOperation() {
    return m_operation;
  }

  public void setOperation(String op) {
    m_operation = op;
  }

  public Object[] getArguments() {
    return m_arguments;
  }

  public void setArguments(Object[] arguments) {
    m_arguments = arguments;
  }

  public ITransactionMember[] getXaResources() {
    return m_xaResources;
  }

  public void setXaResources(ITransactionMember[] xaResources) {
    m_xaResources = xaResources;
  }

  public long getStartTime() {
    return m_startTime;
  }

  public void setStartTime(long t) {
    m_startTime = t;
  }

  public boolean isActive() {
    return m_endTime == 0L;
  }

  public long getEndTime() {
    return m_endTime;
  }

  public void setEndTime(long t) {
    m_endTime = t;
  }

  public long getDuration() {
    if (m_endTime == 0 || isActive()) {
      return System.currentTimeMillis() - m_startTime;
    }
    else {
      return m_endTime - m_startTime;
    }
  }

  public Throwable getReturnException() {
    return m_returnException;
  }

  public void setReturnException(Throwable t) {
    m_returnException = t;
  }

  public Object getReturnData() {
    return m_returnData;
  }

  public void setReturnData(Object o) {
    m_returnData = o;
  }

  public Object[] getOutVariables() {
    return m_outVariables;
  }

  public void setOutVariables(Object[] a) {
    m_outVariables = a;
  }
}
