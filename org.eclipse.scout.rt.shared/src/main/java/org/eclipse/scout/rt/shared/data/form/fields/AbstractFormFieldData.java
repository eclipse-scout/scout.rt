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
package org.eclipse.scout.rt.shared.data.form.fields;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.BeanUtility;
import org.eclipse.scout.rt.shared.data.form.FormDataUtility;
import org.eclipse.scout.rt.shared.data.form.IPropertyHolder;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;
import org.eclipse.scout.rt.shared.extension.AbstractContributionComposite;

public abstract class AbstractFormFieldData extends AbstractContributionComposite implements IPropertyHolder {
  private static final long serialVersionUID = 1L;

  private Map<Class<?>, Class<? extends AbstractFormFieldData>> m_fieldDataReplacements;
  private Map<Class<? extends AbstractPropertyData>, AbstractPropertyData> m_propertyMap;
  private Map<Class<? extends AbstractFormFieldData>, AbstractFormFieldData> m_fieldMap;
  private boolean m_valueSet;

  private List<Class<AbstractPropertyData>> getConfiguredPropertyDatas() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClasses(dca, AbstractPropertyData.class);
  }

  private List<Class<? extends AbstractFormFieldData>> getConfiguredFieldDatas() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<AbstractFormFieldData>> fca = ConfigurationUtility.filterClasses(dca, AbstractFormFieldData.class);
    return ConfigurationUtility.removeReplacedClasses(fca);
  }

  /**
   * When importing form data, this class is used as a stop class for
   * {@link BeanUtility#getProperties(Object, Class, org.eclipse.scout.rt.platform.reflect.IPropertyFilter)}.
   * <p>
   * If a subclass of {@link AbstractFormFieldData} implements this method, it must return its own class. When
   * implementing, the class must make sure that it handles the import of properties itself by providing an appropriate
   * implementation of 'AbstractFormField#importFormFieldData' (if required).
   */
  public Class<?> getFieldStopClass() {
    return AbstractFormFieldData.class;
  }

  @Override
  protected void initConfig() {
    // add properties
    List<Class<AbstractPropertyData>> configuredPropertyDatas = getConfiguredPropertyDatas();
    Map<Class<? extends AbstractPropertyData>, AbstractPropertyData> propMap = new HashMap<>(configuredPropertyDatas.size());
    for (Class<AbstractPropertyData> propertyDataClazz : configuredPropertyDatas) {
      AbstractPropertyData p = ConfigurationUtility.newInnerInstance(this, propertyDataClazz);
      propMap.put(p.getClass(), p);
    }

    // add fields
    List<Class<? extends AbstractFormFieldData>> fieldDataClasses = getConfiguredFieldDatas();
    Map<Class<? extends AbstractFormFieldData>, AbstractFormFieldData> map = new HashMap<>(fieldDataClasses.size());
    for (Class<? extends AbstractFormFieldData> formFieldDataClazz : fieldDataClasses) {
      AbstractFormFieldData f = ConfigurationUtility.newInnerInstance(this, formFieldDataClazz);
      map.put(f.getClass(), f);
    }

    if (!propMap.isEmpty()) {
      m_propertyMap = propMap;
    }

    if (!map.isEmpty()) {
      m_fieldMap = map;
      Map<Class<?>, Class<? extends AbstractFormFieldData>> replacements = ConfigurationUtility.getReplacementMapping(fieldDataClasses);
      if (!replacements.isEmpty()) {
        m_fieldDataReplacements = replacements;
      }
    }
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

  @Override
  public AbstractPropertyData getPropertyById(String id) {
    if (m_propertyMap == null) {
      return null;
    }
    for (AbstractPropertyData p : m_propertyMap.values()) {
      if (p.getPropertyId().equalsIgnoreCase(id)) {
        return p;
      }
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends AbstractPropertyData> T getPropertyByClass(Class<T> c) {
    if (m_propertyMap == null) {
      return null;
    }
    return (T) m_propertyMap.get(c);
  }

  @Override
  public <T extends AbstractPropertyData> void setPropertyByClass(Class<T> c, T v) {
    if (v == null) {
      if (m_propertyMap != null) {
        m_propertyMap.remove(c);
      }
    }
    else {
      if (m_propertyMap == null) {
        m_propertyMap = new HashMap<>();
      }
      m_propertyMap.put(c, v);
    }
  }

  @Override
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
        m_fieldMap = new HashMap<>();
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

  @Override
  public String toString() {
    return FormDataUtility.toString(this, false);
  }

}
