/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.reflect;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This utility class is only public for external junit testing reasons, do not use this API.
 */
public final class FastBeanUtility {

  private FastBeanUtility() {
  }

  public static String capitalize(String s) {
    if (s == null || s.isEmpty()) {
      return "";
    }
    return Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }

  public static boolean compareMethods(Method a, Method b) {
    if ((a == null) != (b == null)) {
      return false;
    }
    return a == null || a.equals(b);
  }

  public static Class findPropertyType(Method readMethod, Method writeMethod) {
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

  private static final Pattern BEAN_METHOD_PAT = Pattern.compile("(get|set|is)([A-Z].*)");

  /**
   * @return a map of property to its {@link FastPropertyDescriptor}
   */
  public static Map<String/*propertyName*/, FastPropertyDescriptor> createPropertyDescriptorMap(Class<?> beanClazz, Class<?> stopClazz) {
    Map<String, FastPropertyDescriptor> map = new HashMap<>();
    for (Class cl = beanClazz; cl != null && cl != stopClazz; cl = cl.getSuperclass()) {
      Method[] methods = getDeclaredPublicMethods(cl);
      contributePropertyDescriptors(beanClazz, cl, methods, map);
    }
    return map;
  }

  public static void contributePropertyDescriptors(Class<?> beanClazz, Class<?> aSuperClass, Method[] methods, Map<String/*propertyName*/, FastPropertyDescriptor> contributeMap) {
    if (methods == null || methods.length == 0) {
      return;
    }
    for (Method m : methods) {
      if (m != null) {
        Matcher matcher = BEAN_METHOD_PAT.matcher(m.getName());
        if (matcher.matches()) {
          String kind = matcher.group(1);
          String name = Introspector.decapitalize(matcher.group(2));
          Class<?>[] paramTypes = m.getParameterTypes();
          Class<?> retType = m.getReturnType();
          //
          if ("get".equals(kind) && paramTypes.length == 0 && retType != null && retType != Void.TYPE) {
            FastPropertyDescriptor desc = contributeMap.computeIfAbsent(name, n -> new FastPropertyDescriptor(beanClazz, n));
            desc.addReadMethod(m);
          }
          else if ("is".equals(kind) && paramTypes.length == 0 && retType != null && retType == boolean.class) {
            FastPropertyDescriptor desc = contributeMap.computeIfAbsent(name, n -> new FastPropertyDescriptor(beanClazz, n));
            desc.addReadMethod(m);
          }
          else if ("set".equals(kind) && paramTypes.length == 1 && (retType == null || retType == Void.TYPE)) {
            FastPropertyDescriptor desc = contributeMap.computeIfAbsent(name, n -> new FastPropertyDescriptor(beanClazz, n));
            desc.addWriteMethod(m);
          }
        }
      }
    }
  }

  public static Method[] getDeclaredPublicMethods(Class c) {
    Method[] methods = null;
    final Class fc = c;
    methods = AccessController.doPrivileged((PrivilegedAction<Method[]>) fc::getDeclaredMethods);
    //clear non-public methods:
    List<Method> methodsList = new ArrayList<>();
    if (methods != null) {
      for (Method method : methods) {
        int mods = method.getModifiers();
        if (Modifier.isPublic(mods)) {
          methodsList.add(method);
        }
      }
    }
    return methodsList.toArray(new Method[0]);
  }

}
