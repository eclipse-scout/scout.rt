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
package org.eclipse.scout.commons.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Array;
import java.util.HashMap;

import org.eclipse.scout.commons.internal.Activator;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.osgi.framework.Bundle;

/**
 * Serialization specialization to be used in osgi environments with bundle class loading instead of flat class loading<br>
 * <p>
 * see also {@link BundleObjectOutputStream}
 */
public class BundleObjectInputStream extends ObjectInputStream {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BundleObjectInputStream.class);

  /** table mapping primitive type names to corresponding class objects */
  private static final HashMap<String, Class> PRIMITIVE_TYPES;
  static {
    PRIMITIVE_TYPES = new HashMap<String, Class>(8, 1f);
    PRIMITIVE_TYPES.put("boolean", boolean.class);
    PRIMITIVE_TYPES.put("byte", byte.class);
    PRIMITIVE_TYPES.put("char", char.class);
    PRIMITIVE_TYPES.put("short", short.class);
    PRIMITIVE_TYPES.put("int", int.class);
    PRIMITIVE_TYPES.put("long", long.class);
    PRIMITIVE_TYPES.put("float", float.class);
    PRIMITIVE_TYPES.put("double", double.class);
    PRIMITIVE_TYPES.put("void", void.class);
    //
    PRIMITIVE_TYPES.put("Z", boolean.class);
    PRIMITIVE_TYPES.put("B", byte.class);
    PRIMITIVE_TYPES.put("C", char.class);
    PRIMITIVE_TYPES.put("S", short.class);
    PRIMITIVE_TYPES.put("I", int.class);
    PRIMITIVE_TYPES.put("J", long.class);
    PRIMITIVE_TYPES.put("F", float.class);
    PRIMITIVE_TYPES.put("D", double.class);
    PRIMITIVE_TYPES.put("V", void.class);
  }

  private Bundle[] m_bundleList;

  public BundleObjectInputStream(InputStream in, Bundle[] bundleList) throws IOException {
    super(in);
    m_bundleList = bundleList;
    enableResolveObject(true);
  }

  @Override
  protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
    return defaultResolveClass(desc.getName());
  }

  /*
   * Interface IObjectInputStream
   */

  private Class<?> defaultResolveClass(String className) throws ClassNotFoundException, IOException {
    Class c = PRIMITIVE_TYPES.get(className);
    if (c != null) {
      return c;
    }
    try {
      int arrayDim = 0;
      while (className.startsWith("[")) {
        className = className.substring(1);
        arrayDim++;
      }
      if (className.matches("L.*;")) {
        className = className.substring(1, className.length() - 1);
      }
      if (arrayDim > 0) {
        c = defaultResolveClass(className);
        int[] dimensions = new int[arrayDim];
        c = Array.newInstance(c, dimensions).getClass();
      }
      else {
        for (Bundle b : m_bundleList) {
          try {
            c = b.loadClass(className);
            break;
          }
          catch (ClassNotFoundException e) {
            // nop
          }
        }
        if (c == null) {
          if (Activator.getDefault() == null) {
            //outside osgi
            c = Class.forName(className);
          }
          else {
            throw new ClassNotFoundException(className);
          }
        }
      }
      return c;
    }
    catch (ClassNotFoundException e) {
      LOG.error("reading serialized object from http proxy tunnel: " + e.getMessage(), e);
      throw e;
    }
  }

}
