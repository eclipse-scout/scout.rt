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
package org.eclipse.scout.rt.client.mobile.ui.form.fields;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.scout.commons.beans.BasicPropertySupport;

/**
 * @since 3.9.0
 */
public class PropertyMap {
  private Map<Object, BasicPropertySupport> m_instanceProperties;

  public PropertyMap() {
    m_instanceProperties = new WeakHashMap<Object, BasicPropertySupport>();
  }

  public void setPropertyBoolean(Object bean, String name, Boolean value) {
    BasicPropertySupport basicPropertySupport = m_instanceProperties.get(bean);
    if (basicPropertySupport == null) {
      basicPropertySupport = new BasicPropertySupport(bean);
      m_instanceProperties.put(bean, basicPropertySupport);
    }

    basicPropertySupport.setProperty(name, value);
  }

  public Boolean getPropertyBoolean(Object bean, String name) {
    BasicPropertySupport basicPropertySupport = m_instanceProperties.get(bean);
    if (basicPropertySupport == null) {
      return null;
    }

    //TODO CGU: better with hasProperty? But then removing a property must be possible which is not currently
    Object prop = basicPropertySupport.getProperty(name);
    if (prop instanceof Boolean) {
      return (Boolean) prop;
    }

    return null;
  }

  public void setPropertyString(Object bean, String name, String value) {
    BasicPropertySupport basicPropertySupport = m_instanceProperties.get(bean);
    if (basicPropertySupport == null) {
      basicPropertySupport = new BasicPropertySupport(bean);
      m_instanceProperties.put(bean, basicPropertySupport);
    }

    basicPropertySupport.setProperty(name, value);
  }

  public String getPropertyString(Object bean, String name) {
    BasicPropertySupport basicPropertySupport = m_instanceProperties.get(bean);
    if (basicPropertySupport == null) {
      return null;
    }

    //TODO CGU: better with hasProperty? But then removing a property must be possible which is not currently
    Object prop = basicPropertySupport.getProperty(name);
    if (prop instanceof String) {
      return (String) prop;
    }

    return null;
  }

  public <T extends Object> void setProperty(Object bean, String name, T value) {
    BasicPropertySupport basicPropertySupport = m_instanceProperties.get(bean);
    if (basicPropertySupport == null) {
      basicPropertySupport = new BasicPropertySupport(bean);
      m_instanceProperties.put(bean, basicPropertySupport);
    }

    basicPropertySupport.setProperty(name, value);
  }

  @SuppressWarnings("unchecked")
  public <T extends Object> T getProperty(Object bean, String name) {
    BasicPropertySupport basicPropertySupport = m_instanceProperties.get(bean);
    if (basicPropertySupport == null) {
      return null;
    }

    return (T) basicPropertySupport.getProperty(name);
  }

}
