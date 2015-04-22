/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.config;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.eclipse.scout.rt.platform.config.IConfigPropertyWithStatus;

/**
 *
 */
public class WebXmlConfigManager {
  private final ServletConfig m_servletConfig;
  private final FilterConfig m_filterConfig;

  public WebXmlConfigManager(ServletConfig cfg) {
    m_servletConfig = cfg;
    m_filterConfig = null;
  }

  public WebXmlConfigManager(FilterConfig cfg) {
    m_servletConfig = null;
    m_filterConfig = cfg;
  }

  @SuppressWarnings("unchecked")
  public synchronized <DATA_TYPE> IConfigProperty<DATA_TYPE> getProperty(Class<? extends IConfigProperty<DATA_TYPE>> clazz) {
    if (clazz == null) {
      throw new IllegalArgumentException("No class parameter specified.");
    }

    IConfigProperty instance = BEANS.get(clazz);

    // apply data source
    if (instance instanceof IFilterConfigProperty<?> && m_filterConfig != null) {
      ((IFilterConfigProperty<?>) instance).setConfig(m_filterConfig);
    }
    else if (instance instanceof IServletConfigProperty<?> && m_servletConfig != null) {
      ((IServletConfigProperty<?>) instance).setConfig(m_servletConfig);
    }

    // if it has status: validate
    if (instance instanceof IConfigPropertyWithStatus<?>) {
      AbstractConfigProperty.checkStatus((IConfigPropertyWithStatus<?>) instance);
    }

    return instance;
  }

  public <DATA_TYPE> DATA_TYPE getPropertyValue(Class<? extends IConfigProperty<DATA_TYPE>> clazz) {
    return getPropertyValue(clazz, null);
  }

  public <DATA_TYPE> DATA_TYPE getPropertyValue(Class<? extends IConfigProperty<DATA_TYPE>> clazz, DATA_TYPE defaultValue) {
    IConfigProperty<DATA_TYPE> property = getProperty(clazz);
    if (property == null) {
      return defaultValue;
    }
    DATA_TYPE value = property.getValue();
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
