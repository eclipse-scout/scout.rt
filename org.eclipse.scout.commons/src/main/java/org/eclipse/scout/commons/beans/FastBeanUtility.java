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
    if (s == null || s.length() == 0) {
      return "";
    }
    return Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }

  public static String decapitalize(String s) {
    if (s == null || s.length() == 0) {
      return "";
    }
    if (s.length() >= 2 && Character.isUpperCase(s.charAt(0)) && Character.isUpperCase(s.charAt(1))) {
      return s;
    }
    return Character.toLowerCase(s.charAt(0)) + s.substring(1);
  }

  public static boolean compareMethods(Method a, Method b) {
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
   * @return a map of property to its {@link InternalPropertyDescriptor}
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
          String name = decapitalize(matcher.group(2));
          Class<?>[] paramTypes = m.getParameterTypes();
          Class<?> retType = m.getReturnType();
          //
          if ("get".equals(kind) && paramTypes.length == 0 && retType != null && retType != Void.TYPE) {
            FastPropertyDescriptor desc = contributeMap.get(name);
            if (desc == null) {
              desc = new FastPropertyDescriptor(beanClazz, name);
              contributeMap.put(name, desc);
            }
            desc.addReadMethod(m);
          }
          else if ("is".equals(kind) && paramTypes.length == 0 && retType != null && retType == boolean.class) {
            FastPropertyDescriptor desc = contributeMap.get(name);
            if (desc == null) {
              desc = new FastPropertyDescriptor(beanClazz, name);
              contributeMap.put(name, desc);
            }
            desc.addReadMethod(m);
          }
          else if ("set".equals(kind) && paramTypes.length == 1 && (retType == null || retType == Void.TYPE)) {
            FastPropertyDescriptor desc = contributeMap.get(name);
            if (desc == null) {
              desc = new FastPropertyDescriptor(beanClazz, name);
              contributeMap.put(name, desc);
            }
            desc.addWriteMethod(m);
          }
        }
      }
    }
  }

  public static Method[] getDeclaredPublicMethods(Class c) {
    Method[] methods = null;
    final Class fc = c;
    methods = AccessController.doPrivileged(new PrivilegedAction<Method[]>() {
      @Override
      public Method[] run() {
        return fc.getDeclaredMethods();
      }
    });
    //clear non-public methods:
    List<Method> methodsList = new ArrayList<>();
    if (methods != null) {
      for (int i = 0; i < methods.length; i++) {
        Method method = methods[i];
        int mods = method.getModifiers();
        if (Modifier.isPublic(mods)) {
          methodsList.add(method);
        }
      }
    }
    return methodsList.toArray(new Method[methodsList.size()]);
  }

}
