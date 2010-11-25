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
package org.eclipse.scout.rt.shared.servicetunnel;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Locale;

import org.eclipse.scout.commons.VerboseUtility;
import org.eclipse.scout.commons.nls.NlsLocale;

public class ServiceTunnelRequest implements Serializable {
  private static final long serialVersionUID = 0L;

  private String m_serviceInterfaceClassName;
  private String m_operation;
  private Class[] m_parameterTypes;
  private Object[] m_args;
  private Locale m_nlsLocale;
  private Locale m_locale;
  private String m_version;
  private Object m_metaData;

  // for serialization
  private ServiceTunnelRequest() {
  }

  public ServiceTunnelRequest(String version, Class serviceInterfaceClass, Method operation, Object[] args) {
    m_version = version;
    m_serviceInterfaceClassName = serviceInterfaceClass.getName();
    m_operation = operation.getName();
    m_parameterTypes = operation.getParameterTypes();
    m_args = args;
    if (m_args == null) m_args = new Object[0];
    m_nlsLocale = NlsLocale.getDefault().getLocale();
    m_locale = Locale.getDefault();
  }

  public String getServiceInterfaceClassName() {
    return m_serviceInterfaceClassName;
  }

  public String getVersion() {
    return m_version;
  }

  public String getOperation() {
    return m_operation;
  }

  public Class[] getParameterTypes() {
    return m_parameterTypes;
  }

  public Object[] getArgs() {
    return m_args;
  }

  public Locale getNlsLocale() {
    return m_nlsLocale;
  }

  public Locale getLocale() {
    return m_locale;
  }

  public Object getMetaData() {
    return m_metaData;
  }

  public void setMetaData(Object o) {
    m_metaData = o;
  }

  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("Service call " + m_serviceInterfaceClassName + "." + m_operation);
    if (m_args != null && m_args.length > 0) {
      for (int i = 0; i < m_args.length; i++) {
        buf.append("\n");
        buf.append("arg[" + i + "]=" + VerboseUtility.dumpObject(m_args[i]));
      }
    }
    return buf.toString();
  }
}
