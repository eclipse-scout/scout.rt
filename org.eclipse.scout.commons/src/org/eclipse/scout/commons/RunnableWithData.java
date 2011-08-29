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
package org.eclipse.scout.commons;

import java.util.HashMap;

public class RunnableWithData implements Runnable {
  private Exception m_constructorStackTrace;
  /*
   * cached from m_constructorStackTrace
   */
  private Exception m_originatingStackTrace;
  private HashMap<String, Object> m_dataMap = new HashMap<String, Object>();

  public RunnableWithData() {
    m_constructorStackTrace = new Exception();
  }

  @Override
  public void run() {
  }

  /**
   * Logging
   */
  public Exception getOriginatingStackTrace() {
    if (m_originatingStackTrace == null) {
      StackTraceElement[] trace = m_constructorStackTrace.getStackTrace();
      int traceIndex = 0;
      // find constructor
      while (traceIndex + 1 < trace.length && !getClass().getName().equals(trace[traceIndex].getClassName())) {
        traceIndex++;
      }
      // find origin
      while (traceIndex + 1 < trace.length && getClass().getName().equals(trace[traceIndex].getClassName())) {
        traceIndex++;
      }
      StackTraceElement[] trace2 = new StackTraceElement[trace.length - traceIndex];
      System.arraycopy(trace, traceIndex, trace2, 0, trace2.length);
      m_originatingStackTrace = new Exception();
      m_originatingStackTrace.setStackTrace(trace2);
    }
    return m_originatingStackTrace;
  }

  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer(getClass().getSimpleName() + "[");
    StackTraceElement[] a = getOriginatingStackTrace().getStackTrace();
    if (a.length > 0) {
      buf.append(a[0]);
    }
    buf.append("]");
    return buf.toString();
  }

  public void setData(String key, Object data) {
    if (m_dataMap == null) {
      m_dataMap = new HashMap<String, Object>();
    }
    m_dataMap.put(key, data);
  }

  public Object getData(String key) {
    if (m_dataMap != null) {
      return m_dataMap.get(key);
    }
    else {
      return null;
    }
  }
}
