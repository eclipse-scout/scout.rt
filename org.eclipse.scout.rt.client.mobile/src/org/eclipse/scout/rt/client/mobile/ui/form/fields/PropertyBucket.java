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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;

/**
 * @since 3.9.0
 */
public class PropertyBucket {
  private static final String SESSION_DATA_KEY = "PropertyBucketSessionData";

  /**
   * Properties are stored per class to make it more robust against wrong implementations of equals and hashcode.
   */
  private Map<Class, PropertyMap> m_propertiesForClass;

  public PropertyBucket() {
    m_propertiesForClass = new HashMap<Class, PropertyMap>();
  }

  public static PropertyBucket getInstance(IClientSession clientSession) {
    Object data = clientSession.getData(SESSION_DATA_KEY);

    PropertyBucket propertyStore = null;
    if (data instanceof PropertyBucket) {
      propertyStore = (PropertyBucket) data;
    }

    if (propertyStore == null) {
      propertyStore = new PropertyBucket();
      clientSession.setData(SESSION_DATA_KEY, propertyStore);
    }

    return propertyStore;
  }

  public static PropertyBucket getInstance() {
    return getInstance(ClientSyncJob.getCurrentSession());
  }

  private PropertyMap getOrCreatePropertyMap(Object bean) {
    PropertyMap propertyMap = m_propertiesForClass.get(bean.getClass());
    if (propertyMap == null) {
      propertyMap = new PropertyMap();
      m_propertiesForClass.put(bean.getClass(), propertyMap);
    }

    return propertyMap;
  }

  public void setPropertyBoolean(Object bean, String name, Boolean value) {
    PropertyMap propertyMap = getOrCreatePropertyMap(bean);
    propertyMap.setPropertyBoolean(bean, name, value);
  }

  public Boolean getPropertyBoolean(Object bean, String name) {
    PropertyMap propertyMap = getOrCreatePropertyMap(bean);
    return propertyMap.getPropertyBoolean(bean, name);
  }

  public void setPropertyString(Object bean, String name, String value) {
    PropertyMap propertyMap = getOrCreatePropertyMap(bean);
    propertyMap.setPropertyString(bean, name, value);
  }

  public String getPropertyString(Object bean, String name) {
    PropertyMap propertyMap = getOrCreatePropertyMap(bean);
    return propertyMap.getPropertyString(bean, name);
  }

  public <T extends Object> void setProperty(Object bean, String name, T value) {
    PropertyMap propertyMap = getOrCreatePropertyMap(bean);
    propertyMap.setProperty(bean, name, value);
  }

  public <T extends Object> T getProperty(Object bean, String name) {
    PropertyMap propertyMap = getOrCreatePropertyMap(bean);
    return propertyMap.getProperty(bean, name);
  }
}
