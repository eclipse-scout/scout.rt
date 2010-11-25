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
import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;

public final class VerboseUtility {

  private VerboseUtility() {
  }

  public static String dumpType(Class cls) {
    if (cls == null) return "null";
    Class compClass = cls;
    String dim = "";
    while (compClass.isArray()) {
      dim = dim + "[]";
      compClass = compClass.getComponentType();
    }
    Package pkg = compClass.getPackage();
    String s = compClass.getName();
    if (pkg != null) s = s.substring(pkg.getName().length() + 1);
    return s + dim;
  }

  public static String dumpObject(Object o) {
    if (o == null) return "null";
    if (o.getClass().isArray()) {
      StringBuffer buf = new StringBuffer();
      buf.append("[");
      int n = Array.getLength(o);
      if (n > 100) n = 100;
      for (int i = 0; i < n; i++) {
        buf.append(dumpObject(Array.get(o, i)));
        if (i + 1 < n) buf.append(",");
      }
      if (Array.getLength(o) > n) {
        buf.append(",...");
      }
      buf.append("]");
      return buf.toString();
    }
    else if (o.getClass() == Byte.class) {
      Byte b = (Byte) o;
      String s = Integer.toHexString(b != null ? (b.intValue() & 0xff) : 0);
      if (s.length() < 2) s = "0" + s;
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
    if (o == null) return "null";
    return dumpType(o.getClass()) + ":" + dumpObject(o);
  }

  public static String dumpObjects(Object[] args) {
    if (args == null) return "";
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < args.length; i++) {
      buf.append(dumpObject(args[i]));
      if (i + 1 < args.length) buf.append(", ");
    }
    return buf.toString();
  }
}
