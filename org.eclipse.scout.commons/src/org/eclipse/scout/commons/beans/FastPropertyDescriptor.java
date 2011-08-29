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
package org.eclipse.scout.commons.beans;

import java.lang.reflect.Method;

/**
 * The java bean FastPropertyDescriptor tries to find a read <b>and</b> write method of the
 * property, otherwise an exception is thrown.
 * <p>
 * This lenient implementation creates a FastPropertyDescriptor with an optional getter method and an optional setter
 * method so they exist.
 */
public class FastPropertyDescriptor {
  private final Class beanClass;
  private final String name;

  private Class propertyType;
  private Method readMethod;
  private Method writeMethod;

  public FastPropertyDescriptor(Class<?> beanClazz, String name, Method readMethod, Method writeMethod) {
    this.beanClass = beanClazz;
    this.name = name;
    this.readMethod = readMethod;
    this.writeMethod = writeMethod;
    this.propertyType = findPropertyType(readMethod, writeMethod);
  }

  FastPropertyDescriptor(Class<?> beanClazz, String name) {
    this.beanClass = beanClazz;
    this.name = name;
  }

  public Class getBeanClass() {
    return beanClass;
  }

  public String getName() {
    return name;
  }

  /**
   * Gets the Class object for the property.
   * 
   * @return The Java type info for the property. Note that
   *         the "Class" object may describe a built-in Java type such as "int".
   *         The result may be "null" if this is an indexed property that
   *         does not support non-indexed access.
   *         <p>
   *         This is the type that will be returned by the ReadMethod.
   */
  public Class<?> getPropertyType() {
    return propertyType;
  }

  void setPropertyType(Class type) {
    propertyType = type;
  }

  /**
   * Gets the method that should be used to read the property value.
   * 
   * @return The method that should be used to read the property value.
   *         May return null if the property can't be read.
   */
  public Method getReadMethod() {
    return readMethod;
  }

  void setReadMethod(Method readMethod) {
    this.readMethod = readMethod;
  }

  /**
   * Gets the method that should be used to write the property value.
   * 
   * @return The method that should be used to write the property value.
   *         May return null if the property can't be written.
   */
  public Method getWriteMethod() {
    return writeMethod;
  }

  void setWriteMethod(Method writeMethod) {
    this.writeMethod = writeMethod;
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
    if (obj != null && obj instanceof FastPropertyDescriptor) {
      FastPropertyDescriptor other = (FastPropertyDescriptor) obj;
      Method otherReadMethod = other.getReadMethod();
      Method otherWriteMethod = other.getWriteMethod();
      if (!compareMethods(getReadMethod(), otherReadMethod)) {
        return false;
      }
      if (!compareMethods(getWriteMethod(), otherWriteMethod)) {
        return false;
      }
      if (getPropertyType() == other.getPropertyType()) {
        return true;
      }
    }
    return false;
  }

  private static String capitalize(String s) {
    if (s == null || s.length() == 0) {
      return "";
    }
    return Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }

  private static boolean compareMethods(Method a, Method b) {
    if ((a == null) != (b == null)) {
      return false;
    }

    if (a != null && b != null) {
      if (!a.equals(b)) {
        return false;
      }
    }
    return true;
  }

  private static Class findPropertyType(Method readMethod, Method writeMethod) {
    if (readMethod != null) {
      return readMethod.getReturnType();
    }
    else if (writeMethod != null) {
      return writeMethod.getParameterTypes()[0];
    }
    else {
      return null;
    }
  }

}
