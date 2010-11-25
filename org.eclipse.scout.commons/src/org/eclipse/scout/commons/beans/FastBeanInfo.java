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

import java.beans.BeanInfo;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The java bean {@link BeanInfo} extracts all info from all beans.
 * Sometimes it is useful to have a hi-speed variant of bean info only providing and extracting bean name, type, getters
 * and setters
 * <p>
 * This lenient implementation creates SimplePropertyDescriptor objects that are also speed optimized.
 */
public class FastBeanInfo {
  private final Class beanClass;
  private final Map<String/*propertyName*/, FastPropertyDescriptor> propertyMap;
  private final FastPropertyDescriptor[] propertyArray;

  public FastBeanInfo(Class<?> beanClass, Class<?> stopClass) {
    this.beanClass = beanClass;
    this.propertyMap = Collections.unmodifiableMap(createPropertyDescriptorMap(beanClass, stopClass));
    this.propertyArray = propertyMap.values().toArray(new FastPropertyDescriptor[propertyMap.size()]);
  }

  public Class getBeanClass() {
    return beanClass;
  }

  public Map<String, FastPropertyDescriptor> getPropertyDescriptorMap() {
    return propertyMap;
  }

  /**
   * @return the property descriptor for that name
   *         <p>
   *         This method is lenient and checks for upper and lower case named properties. For example {@link
   *         #getPropertyDescriptor("vATRate")} will also check for property "VATRate" and vis versa.
   */
  public FastPropertyDescriptor getPropertyDescriptor(String name) {
    FastPropertyDescriptor p = propertyMap.get(name);
    if (p == null && name.length() > 0) {
      //be lenient with uppercase/lowercase names
      if (Character.isUpperCase(name.charAt(0))) {
        p = propertyMap.get(Character.toLowerCase(name.charAt(0)) + name.substring(1));
      }
      else {
        p = propertyMap.get(Character.toUpperCase(name.charAt(0)) + name.substring(1));
      }
    }
    return p;
  }

  public FastPropertyDescriptor[] getPropertyDescriptors() {
    return propertyArray;
  }

  private static String decapitalize(String s) {
    if (s == null || s.length() == 0) return "";
    if (s.length() >= 2 && Character.isUpperCase(s.charAt(0)) && Character.isUpperCase(s.charAt(1))) {
      return s;
    }
    return Character.toLowerCase(s.charAt(0)) + s.substring(1);
  }

  private static final Pattern BEAN_METHOD_PAT = Pattern.compile("(get|set|is)([A-Z].*)");

  /**
   * @return a map of property to its SimplePropertyDescriptor
   */
  private static Map<String/*propertyName*/, FastPropertyDescriptor> createPropertyDescriptorMap(Class beanClazz, Class stopClazz) {
    HashMap<String, FastPropertyDescriptor> map = new HashMap<String, FastPropertyDescriptor>();
    for (Class cl = beanClazz; cl != null && cl != stopClazz; cl = cl.getSuperclass()) {
      Method[] methods = getDeclaredPublicMethods(cl);
      if (methods != null) {
        for (Method m : methods) {
          if (m != null) {
            Matcher matcher = BEAN_METHOD_PAT.matcher(m.getName());
            if (matcher.matches()) {
              String kind = matcher.group(1);
              String name = decapitalize(matcher.group(2));
              Class<?>[] paramTypes = m.getParameterTypes();
              if (paramTypes == null) paramTypes = new Class<?>[0];
              Class<?> retType = m.getReturnType();
              //
              if (kind.equals("get") && paramTypes.length == 0 && retType != null && retType != Void.TYPE) {
                FastPropertyDescriptor desc = map.get(name);
                if (desc == null) {
                  desc = new FastPropertyDescriptor(beanClazz, name);
                  map.put(name, desc);
                }
                if (desc.getReadMethod() == null) {
                  desc.setReadMethod(m);
                  desc.setPropertyType(retType);
                  //check write method; read type wins
                  Method w = desc.getWriteMethod();
                  if (w != null && w.getParameterTypes()[0] != desc.getPropertyType()) {
                    desc.setWriteMethod(null);
                  }
                }
              }
              else if (kind.equals("is") && paramTypes.length == 0 && retType != null && retType == boolean.class) {
                FastPropertyDescriptor desc = map.get(name);
                if (desc == null) {
                  desc = new FastPropertyDescriptor(beanClazz, name);
                  map.put(name, desc);
                }
                if (desc.getReadMethod() == null) {
                  desc.setReadMethod(m);
                  desc.setPropertyType(retType);
                  //check write method; read type wins
                  Method w = desc.getWriteMethod();
                  if (w != null && w.getParameterTypes()[0] != desc.getPropertyType()) {
                    desc.setWriteMethod(null);
                  }
                }
              }
              else if (kind.equals("set") && paramTypes.length == 1 && (retType == null || retType == Void.TYPE)) {
                FastPropertyDescriptor desc = map.get(name);
                if (desc == null) {
                  desc = new FastPropertyDescriptor(beanClazz, name);
                  map.put(name, desc);
                }
                if (desc.getWriteMethod() == null) {
                  if (desc.getPropertyType() == null) {
                    desc.setWriteMethod(m);
                    desc.setPropertyType(paramTypes[0]);
                  }
                  else if (desc.getPropertyType() == paramTypes[0]) {
                    desc.setWriteMethod(m);
                  }
                }
              }
            }
          }
        }
      }
    }
    return map;
  }

  private static Method[] getDeclaredPublicMethods(Class c) {
    Method[] result = null;
    final Class fc = c;
    result = AccessController.doPrivileged(new PrivilegedAction<Method[]>() {
      public Method[] run() {
        return fc.getDeclaredMethods();
      }
    });
    //clear non-public methods
    if (result != null) {
      for (int i = 0; i < result.length; i++) {
        Method method = result[i];
        int mods = method.getModifiers();
        if (!Modifier.isPublic(mods)) {
          result[i] = null;
        }
      }
    }
    return result;
  }

}
