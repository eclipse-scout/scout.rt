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
package org.eclipse.scout.rt.server.commons.internal;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

public class FilterConfigImpl implements FilterConfig {
  private ServletContext m_servletContext;
  private final IConfigurationElement m_serviceElement;
  private final Bundle m_definingBundle;
  private final HashMap<String, String> m_initParams;
  private final String m_alias;
  // cache
  private Filter m_filter;
  private boolean m_destroyed;
  private final String m_equalityCode;

  public FilterConfigImpl(String alias, IConfigurationElement serviceElement, Bundle definingBundle) {
    m_alias = alias;
    m_serviceElement = serviceElement;
    m_definingBundle = definingBundle;
    String className = serviceElement.getAttribute("class");
    //
    m_initParams = new HashMap<String, String>();
    for (IConfigurationElement initParam : serviceElement.getChildren("init-param")) {
      String name = initParam.getAttribute("name");
      String value = initParam.getAttribute("value");
      m_initParams.put(name, value);
    }
    //
    m_equalityCode = m_definingBundle.getSymbolicName() + "/" + className + alias;
  }

  public boolean isFiltering(String servletPath) {
    if (servletPath != null) {
      if (servletPath.startsWith(m_alias)) {
        return true;
      }
    }
    return false;
  }

  public Filter getFilter(ServletContext servletContext) throws ServletException {
    if (m_filter == null && (!m_destroyed)) {
      try {
        m_servletContext = servletContext;
        m_filter = (Filter) m_serviceElement.createExecutableExtension("class");
        if (Activator.DEBUG) {
          Activator.getDefault().getLog().log(new Status(Status.INFO, Activator.PLUGIN_ID, "ServletFilterConfig: init " + m_filter.getClass().getSimpleName()));
        }
        m_filter.init(this);
      }
      catch (Throwable t) {
        String className = m_serviceElement.getAttribute("class");
        throw new ServletException("init " + className, t);
      }
    }
    return m_filter;
  }

  public void destroy() {
    if (m_filter != null) {
      try {
        if (Activator.DEBUG) {
          Activator.getDefault().getLog().log(new Status(Status.INFO, Activator.PLUGIN_ID, "ServletFilterConfig: destroy " + m_filter.getClass().getSimpleName()));
        }
        m_filter.destroy();
      }
      catch (Throwable t) {
        String className = m_serviceElement.getAttribute("class");
        Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, "destroy " + className, t));
      }
      finally {
        m_filter = null;
        m_destroyed = true;
      }
    }
  }

  public Bundle getDefiningBundle() {
    return m_definingBundle;
  }

  @Override
  public int hashCode() {
    return m_equalityCode.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    return this.m_equalityCode.equals(((FilterConfigImpl) o).m_equalityCode);
  }

  @Override
  public String getFilterName() {
    String className = m_serviceElement.getAttribute("class");
    return className;
  }

  @Override
  public String getInitParameter(String name) {
    return m_initParams.get(name);
  }

  @Override
  public Enumeration getInitParameterNames() {
    return new Vector<String>(m_initParams.keySet()).elements();
  }

  @Override
  public ServletContext getServletContext() {
    return m_servletContext;
  }

  @Override
  public String toString() {
    return getFilterName() + "[" + m_alias + "]";
  }

}
