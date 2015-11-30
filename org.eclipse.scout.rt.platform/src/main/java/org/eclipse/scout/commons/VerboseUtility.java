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
package org.eclipse.scout.commons;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

public final class VerboseUtility {

  private VerboseUtility() {
  }

  public static String dumpGenerics(Type... types) {
    HashMap<Type, String> longDesc = new HashMap<Type, String>();
    StringBuffer buf = new StringBuffer();
    buf.append(dumpGenericsRec(new HashMap<Type, String>(), longDesc, types));
    longDesc.remove(Object.class);
    if (longDesc.size() > 0) {
      buf.append(" WITH ");
      for (String s : longDesc.values()) {
        buf.append(" ");
        buf.append(s);
      }
    }
    return buf.toString();
  }

  private static String dumpGenericsRec(Map<Type, String> shortDecl, Map<Type, String> longDecl, Type... types) {
    if (types == null || types.length == 0) {
      return "";
    }
    if (types.length == 1) {
      return dumpGenericsImpl(shortDecl, longDecl, types[0]);
    }
    StringBuffer buf = new StringBuffer();
    buf.append("{");
    for (Type t : types) {
      if (buf.length() > 1) {
        buf.append(", ");
      }
      buf.append(dumpGenericsImpl(shortDecl, longDecl, t));
    }
    buf.append("}");
    return buf.toString();
  }

  private static String dumpGenericsImpl(Map<Type, String> shortDecl, Map<Type, String> longDecl, Type type) {
    String shortText = shortDecl.get(type);
    if (shortText != null) {
      return shortText;
    }
    //register short declaration first
    if (type == null) {
      shortText = "null";
    }
    else if (type instanceof Class) {
      Class<?> c = (Class<?>) type;
      shortText = "Class[" + c.getSimpleName() + "]";
    }
    else if (type instanceof GenericArrayType) {
      GenericArrayType g = (GenericArrayType) type;
      shortText = "GenericArrayType[" + dumpGenericsRec(shortDecl, longDecl, g.getGenericComponentType()) + "]";
    }
    else if (type instanceof ParameterizedType) {
      ParameterizedType p = (ParameterizedType) type;
      shortText = "ParameterizedType[" + dumpGenericsRec(shortDecl, longDecl, p.getActualTypeArguments()) + "]";
    }
    else if (type instanceof TypeVariable<?>) {
      TypeVariable v = (TypeVariable) type;
      shortText = "TypeVariable[" + v.getName() + "]";
    }
    else if (type instanceof WildcardType) {
      WildcardType w = (WildcardType) type;
      shortText = "WildcardType[" + dumpGenericsRec(shortDecl, longDecl, w.getLowerBounds()) + ", " + dumpGenericsRec(shortDecl, longDecl, w.getUpperBounds()) + "]";
    }
    else {
      shortText = "UNKNOWN[" + type.getClass().getSimpleName() + "]";
    }
    shortDecl.put(type, shortText);
    //add long declaration
    String longText = null;
    if (type == null) {
      //nop
    }
    else if (type instanceof Class) {
      Class<?> c = (Class<?>) type;
      longText = "Class[name=" + c.getName() + ", typeParameters=" + dumpGenericsRec(shortDecl, longDecl, c.getTypeParameters()) + "]";
    }
    else if (type instanceof GenericArrayType) {
      //nop
    }
    else if (type instanceof ParameterizedType) {
      //nop
    }
    else if (type instanceof TypeVariable<?>) {
      //nop
    }
    else if (type instanceof WildcardType) {
      WildcardType w = (WildcardType) type;
      longText = "WildcardType[lowerBounds=" + dumpGenericsRec(shortDecl, longDecl, w.getLowerBounds()) + ", upperBounds=" + dumpGenericsRec(shortDecl, longDecl, w.getUpperBounds()) + "]";
    }
    if (longText != null) {
      longDecl.put(type, longText);
    }
    return shortText;
  }

  public static String dumpType(Class cls) {
    if (cls == null) {
      return "null";
    }
    Class compClass = cls;
    String dim = "";
    while (compClass.isArray()) {
      dim = dim + "[]";
      compClass = compClass.getComponentType();
    }
    Package pkg = compClass.getPackage();
    String s = compClass.getName();
    if (pkg != null) {
      s = s.substring(pkg.getName().length() + 1);
    }
    return s + dim;
  }

  public static String dumpObject(Object o) {
    if (o == null) {
      return "null";
    }
    if (o.getClass().isArray()) {
      StringBuffer buf = new StringBuffer();
      buf.append("[");
      int n = Array.getLength(o);
      if (n > 100) {
        n = 100;
      }
      for (int i = 0; i < n; i++) {
        buf.append(dumpObject(Array.get(o, i)));
        if (i + 1 < n) {
          buf.append(",");
        }
      }
      if (Array.getLength(o) > n) {
        buf.append(",...");
      }
      buf.append("]");
      return buf.toString();
    }
    else if (o.getClass() == Byte.class) {
      Byte b = (Byte) o;
      String s = Integer.toHexString(b.intValue() & 0xff);
      if (s.length() < 2) {
        s = "0" + s;
      }
      return s;
    }
    else if (o instanceof Subject) {
      Subject s = (Subject) o;
      Set<Principal> set = s.getPrincipals();
      if (set.size() > 0) {
        return set.iterator().next().getName();
      }
      else {
        return s.toString();
      }
    }
    else {
      return "" + o;
    }
  }

  public static String dumpTypeAndObject(Object o) {
    if (o == null) {
      return "null";
    }
    return dumpType(o.getClass()) + ":" + dumpObject(o);
  }

  public static String dumpObjects(Object[] args) {
    if (args == null) {
      return "";
    }
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < args.length; i++) {
      buf.append(dumpObject(args[i]));
      if (i + 1 < args.length) {
        buf.append(", ");
      }
    }
    return buf.toString();
  }
}
