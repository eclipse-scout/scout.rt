/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.reflect;

import java.beans.BeanInfo;
import java.util.Collections;
import java.util.Map;

/**
 * The java bean {@link BeanInfo} extracts all info from all beans. Sometimes it is useful to have a hi-speed variant of
 * bean info only providing and extracting bean name, type, getters and setters
 * <p>
 * This lenient implementation creates SimplePropertyDescriptor objects that are also speed optimized.
 */
public class FastBeanInfo {
  private final Class m_beanClass;
  private final Map<String/*propertyName*/, FastPropertyDescriptor> m_propertyMap;
  private final FastPropertyDescriptor[] m_propertyArray;

  public FastBeanInfo(Class<?> beanClass, Class<?> stopClass) {
    m_beanClass = beanClass;
    m_propertyMap = Collections.unmodifiableMap(FastBeanUtility.createPropertyDescriptorMap(beanClass, stopClass));
    m_propertyArray = m_propertyMap.values().toArray(new FastPropertyDescriptor[m_propertyMap.size()]);
  }

  public Class getBeanClass() {
    return m_beanClass;
  }

  public Map<String, FastPropertyDescriptor> getPropertyDescriptorMap() {
    return m_propertyMap;
  }

  /**
   * @return the property descriptor for that name
   *         <p>
   *         This method is lenient and checks for upper and lower case named properties. For example
   *         {@link #getPropertyDescriptor("vATRate")} will also check for property "VATRate" and vis versa.
   */
  public FastPropertyDescriptor getPropertyDescriptor(String name) {
    FastPropertyDescriptor p = m_propertyMap.get(name);
    if (p == null && !name.isEmpty()) {
      //be lenient with uppercase/lowercase names
      if (Character.isUpperCase(name.charAt(0))) {
        p = m_propertyMap.get(Character.toLowerCase(name.charAt(0)) + name.substring(1));
      }
      else {
        p = m_propertyMap.get(Character.toUpperCase(name.charAt(0)) + name.substring(1));
      }
    }
    return p;
  }

  public FastPropertyDescriptor[] getPropertyDescriptors() {
    return m_propertyArray;
  }
}
