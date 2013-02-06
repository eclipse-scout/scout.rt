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
package org.eclipse.scout.rt.shared.data.form.fields;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.data.form.FormDataUtility;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;

public abstract class AbstractFormFieldData implements Serializable {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractFormFieldData.class);
  private static final long serialVersionUID = 1L;

  private Map<Class<?>, Class<? extends AbstractFormFieldData>> m_fieldDataReplacements;
  private Map<Class<? extends AbstractPropertyData>, AbstractPropertyData> m_propertyMap;
  private Map<Class<? extends AbstractFormFieldData>, AbstractFormFieldData> m_fieldMap;
  private boolean m_valueSet;

  public AbstractFormFieldData() {
    initConfig();
  }

  private Class<? extends AbstractPropertyData>[] getConfiguredPropertyDatas() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClasses(dca, AbstractPropertyData.class);
  }

  private Class<? extends AbstractFormFieldData>[] getConfiguredFieldDatas() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    Class<AbstractFormFieldData>[] fca = ConfigurationUtility.filterClasses(dca, AbstractFormFieldData.class);
    return ConfigurationUtility.removeReplacedClasses(fca);
  }

  protected void initConfig() {
    // add properties
    m_propertyMap = new HashMap<Class<? extends AbstractPropertyData>, AbstractPropertyData>();
    Class<? extends AbstractPropertyData>[] propArray = getConfiguredPropertyDatas();
    for (int i = 0; i < propArray.length; i++) {
      AbstractPropertyData p;
      try {
        p = ConfigurationUtility.newInnerInstance(this, propArray[i]);
        m_propertyMap.put(p.getClass(), p);
      }// end try
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }// end for
     // add fields
    HashMap<Class<? extends AbstractFormFieldData>, AbstractFormFieldData> map = new HashMap<Class<? extends AbstractFormFieldData>, AbstractFormFieldData>();
    Class<? extends AbstractFormFieldData>[] fieldArray = getConfiguredFieldDatas();
    for (int i = 0; i < fieldArray.length; i++) {
      AbstractFormFieldData f;
      try {
        f = ConfigurationUtility.newInnerInstance(this, fieldArray[i]);
        map.put(f.getClass(), f);
      }// end try
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }// end for
    if (map.size() > 0) {
      m_fieldMap = map;
      Map<Class<?>, Class<? extends AbstractFormFieldData>> replacements = ConfigurationUtility.getReplacementMapping(fieldArray);
      if (!replacements.isEmpty()) {
        m_fieldDataReplacements = replacements;
      }
    }
  }

  public Map<String/*rule name*/, Object/*rule value*/> getValidationRules() {
    HashMap<String, Object> ruleMap = new HashMap<String, Object>();
    initValidationRules(ruleMap);
    return ruleMap;
  }

  protected void initValidationRules(Map<String/*rule name*/, Object/*rule value*/> ruleMap) {
  }

  public String getFieldId() {
    Class<?> c = getClass();
    while (c.isAnnotationPresent(Replace.class)) {
      c = c.getSuperclass();
    }
    String s = c.getName();
    int i = Math.max(s.lastIndexOf('$'), s.lastIndexOf('.'));
    s = s.substring(i + 1);
    return s;
  }

  public boolean isValueSet() {
    return m_valueSet;
  }

  public void setValueSet(boolean b) {
    m_valueSet = b;
  }

  public AbstractPropertyData getPropertyById(String id) {
    for (AbstractPropertyData p : m_propertyMap.values()) {
      if (p.getPropertyId().equalsIgnoreCase(id)) {
        return p;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <T extends AbstractPropertyData> T getPropertyByClass(Class<T> c) {
    return (T) m_propertyMap.get(c);
  }

  public <T extends AbstractPropertyData> void setPropertyByClass(Class<T> c, T v) {
    if (v == null) {
      m_propertyMap.remove(c);
    }
    else {
      m_propertyMap.put(c, v);
    }
  }

  public AbstractPropertyData[] getAllProperties() {
    return m_propertyMap != null ? m_propertyMap.values().toArray(new AbstractPropertyData[m_propertyMap.size()]) : new AbstractPropertyData[0];
  }

  public AbstractFormFieldData getFieldById(String id) {
    if (m_fieldMap == null) {
      return null;
    }
    String fieldDataId = FormDataUtility.getFieldDataId(id);
    for (AbstractFormFieldData f : m_fieldMap.values()) {
      if (f.getFieldId().equals(fieldDataId)) {
        return f;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <T extends AbstractFormFieldData> T getFieldByClass(Class<T> c) {
    if (m_fieldMap == null) {
      return null;
    }
    Class<? extends T> clazz = getReplacingFieldDataClass(c);
    return (T) m_fieldMap.get(clazz);
  }

  public <T extends AbstractFormFieldData> void setFieldByClass(Class<T> c, T v) {
    Class<? extends T> clazz = getReplacingFieldDataClass(c);
    if (v == null) {
      if (m_fieldMap != null) {
        m_fieldMap.remove(clazz);
      }
    }
    else {
      if (m_fieldMap == null) {
        m_fieldMap = new HashMap<Class<? extends AbstractFormFieldData>, AbstractFormFieldData>();
      }
      m_fieldMap.put(clazz, v);
    }
  }

  /**
   * Checks whether the form field data with the given class has been replaced by another field. If so, the replacing
   * form field data's class is returned. Otherwise the given class itself.
   * 
   * @param c
   * @return Returns the possibly available replacing field data class for the given class.
   * @see Replace
   * @since 3.8.2
   */
  private <T> Class<? extends T> getReplacingFieldDataClass(Class<T> c) {
    if (m_fieldDataReplacements != null) {
      @SuppressWarnings("unchecked")
      Class<? extends T> replacingFieldClass = (Class<? extends T>) m_fieldDataReplacements.get(c);
      if (replacingFieldClass != null) {
        return replacingFieldClass;
      }
    }
    return c;
  }

  public AbstractFormFieldData[] getFields() {
    if (m_fieldMap == null) {
      return new AbstractFormFieldData[0];
    }
    return m_fieldMap.values().toArray(new AbstractFormFieldData[m_fieldMap.size()]);
  }

}
