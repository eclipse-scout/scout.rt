/*******************************************************************************
 * Copyright (c) 2010,2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

public final class CloneUtility {

  private CloneUtility() {
  }

  /**
   * Creates a deep copy of the object using serialize/deserialize.
   * Other than Object.clone this will create a correct copy of the object and all its references.
   */
  public static <T> T createDeepCopyBySerializing(T obj) throws Exception {
    ByteArrayOutputStream o = new ByteArrayOutputStream();
    DeepCopyObjectWriter oo = new DeepCopyObjectWriter(o);
    oo.writeObject(obj);
    oo.close();
    DeepCopyObjectReader oi = new DeepCopyObjectReader(new ByteArrayInputStream(o.toByteArray()), oo.getUsedClassLoaders());
    Object copy = oi.readObject();
    oi.close();
    @SuppressWarnings("unchecked")
    T castedCopy = (T) copy;
    return castedCopy;
  }

  private static class DeepCopyObjectWriter extends ObjectOutputStream {
    private Set<ClassLoader> m_usedClassLoaders = new HashSet<ClassLoader>();

    public DeepCopyObjectWriter(OutputStream out) throws IOException {
      super(out);
    }

    @Override
    protected void annotateClass(Class<?> c) throws IOException {
      if (c != null) {
        ClassLoader cl = c.getClassLoader();
        if (cl != null) {
          m_usedClassLoaders.add(cl);
        }
      }
    }

    public Set<ClassLoader> getUsedClassLoaders() {
      return m_usedClassLoaders;
    }
  }

  private static class DeepCopyObjectReader extends ObjectInputStream {
    private Set<ClassLoader> m_classLoaders;

    public DeepCopyObjectReader(InputStream in, Set<ClassLoader> classLoaders) throws IOException {
      super(in);
      m_classLoaders = classLoaders;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
      try {
        return super.resolveClass(desc);
      }
      catch (Exception e1) {
        for (ClassLoader cl : m_classLoaders) {
          try {
            return Class.forName(desc.getName(), true, cl);
          }
          catch (Exception e2) {
            //nop
          }
        }
      }
      //repeat original path
      return super.resolveClass(desc);
    }
  }
}
