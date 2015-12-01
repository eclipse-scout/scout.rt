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
package org.eclipse.scout.rt.platform.reflect;

import java.lang.reflect.Method;

/**
 * The java bean FastPropertyDescriptor tries to find a read <b>and</b> write method of the property, otherwise an
 * exception is thrown.
 * <p>
 * This lenient implementation creates a FastPropertyDescriptor with an optional getter method and an optional setter
 * method so they exist.
 */
public class FastPropertyDescriptor {
  private final Class<?> m_beanClass;
  private final String m_name;
  private Class<?> m_propertyType;
  private Method m_readMethod;
  private Method m_writeMethod;

  public FastPropertyDescriptor(Class<?> beanClazz, String name, Method readMethod, Method writeMethod) {
    m_beanClass = beanClazz;
    m_name = name;
    m_readMethod = readMethod;
    m_writeMethod = writeMethod;
    m_propertyType = FastBeanUtility.findPropertyType(readMethod, writeMethod);
  }

  FastPropertyDescriptor(Class<?> beanClazz, String name) {
    m_beanClass = beanClazz;
    m_name = name;
  }

  public Class getBeanClass() {
    return m_beanClass;
  }

  public String getName() {
    return m_name;
  }

  /**
   * Gets the Class object for the property.
   *
   * @return The Java type info for the property. Note that the "Class" object may describe a built-in Java type such as
   *         "int". The result may be "null" if this is an indexed property that does not support non-indexed access.
   *         <p>
   *         This is the type that will be returned by the ReadMethod.
   */
  public Class<?> getPropertyType() {
    return m_propertyType;
  }

  /**
   * Gets the method that should be used to read the property value.
   *
   * @return The method that should be used to read the property value. May return null if the property can't be read.
   */
  public Method getReadMethod() {
    return m_readMethod;
  }

  void addReadMethod(Method newMethod) {
    //is new method less specific than existing
    if (m_readMethod != null && m_propertyType != null && newMethod.getReturnType().isAssignableFrom(m_propertyType)) {
      return;
    }
    m_readMethod = newMethod;
    m_propertyType = m_readMethod.getReturnType();
    //check write method; read type wins
    if (m_writeMethod != null && !m_propertyType.isAssignableFrom(m_writeMethod.getParameterTypes()[0])) {
      m_writeMethod = null;
    }
  }

  /**
   * Gets the method that should be used to write the property value.
   *
   * @return The method that should be used to write the property value. May return null if the property can't be
   *         written.
   */
  public Method getWriteMethod() {
    return m_writeMethod;
  }

  void addWriteMethod(Method newMethod) {
    //is new method less specific than existing
    if (m_writeMethod != null && newMethod.getParameterTypes()[0].isAssignableFrom(m_writeMethod.getParameterTypes()[0])) {
      return;
    }
    if (m_propertyType == null) {
      m_writeMethod = newMethod;
      m_propertyType = m_writeMethod.getParameterTypes()[0];
    }
    else if (m_propertyType.isAssignableFrom(newMethod.getParameterTypes()[0])) {
      m_writeMethod = newMethod;
    }
  }

  @Override
  public int hashCode() {
    int result = 0;
    result = result ^ getName().hashCode();
    result = result ^ ((getPropertyType() == null) ? 0 : getPropertyType().hashCode());
    result = result ^ ((getReadMethod() == null) ? 0 : getReadMethod().hashCode());
    result = result ^ ((getWriteMethod() == null) ? 0 : getWriteMethod().hashCode());
    return result;
  }

  /**
   * @return true if both <code>FastPropertyDescriptor</code>s have equal read method, write method and property type
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof FastPropertyDescriptor) {
      FastPropertyDescriptor other = (FastPropertyDescriptor) obj;
      Method otherReadMethod = other.getReadMethod();
      Method otherWriteMethod = other.getWriteMethod();
      if (!FastBeanUtility.compareMethods(getReadMethod(), otherReadMethod)) {
        return false;
      }
      if (!FastBeanUtility.compareMethods(getWriteMethod(), otherWriteMethod)) {
        return false;
      }
      if (getPropertyType() == other.getPropertyType()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "FastPropertyDescriptor [" + m_beanClass + " / " + m_name + "]";
  }
}
