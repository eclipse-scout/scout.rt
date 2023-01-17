/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.serialization.IObjectSerializer;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CloneUtility {

  private static final Logger LOG = LoggerFactory.getLogger(CloneUtility.class);

  private CloneUtility() {
  }

  /**
   * Creates a deep copy of the object using serialize/deserialize. Other than Object.clone this will create a correct
   * copy of the object and all its references.
   */
  public static <T> T createDeepCopyBySerializing(T obj) throws IOException, ClassNotFoundException {
    if (obj == null) {
      return null;
    }
    try {
      // 1. use serialization utility
      IObjectSerializer serializer = SerializationUtility.createObjectSerializer();
      byte[] serialData = serializer.serialize(obj);
      @SuppressWarnings("unchecked")
      Class<T> expectedType = (Class<T>) obj.getClass();
      T copy = serializer.deserialize(serialData, expectedType);
      return copy;
    }
    catch (ClassNotFoundException e) {
      // 2. use fall back implementation if the object to be copied references classes that are not visible to the optimized class loader
      if (LOG.isDebugEnabled()) {
        LOG.debug("{} occurred while creating a deep copy using {}. Using fallback strategy.", ClassNotFoundException.class.getSimpleName(), SerializationUtility.class.getSimpleName(), e);
      }
      return fallBackCreateDeepCopyBySerializing(obj);
    }
  }

  /**
   * Fall back implementation used in cases the object to be deep copied references classes that are not visible to any
   * class loader used by the {@link SerializationUtility} (typically OSGi bundle and framework class loaders).
   * <p/>
   * This implementation creates a mapping of any class name (except classes in any package staring with <em>java.</em>)
   * to its class object during the serialization process. A class lookup during the deserialization process is trivial.
   *
   * @since 5.2
   */
  private static <T> T fallBackCreateDeepCopyBySerializing(T obj) throws IOException, ClassNotFoundException {
    ByteArrayOutputStream o = new ByteArrayOutputStream();
    DeepCopyObjectWriter oo = new DeepCopyObjectWriter(o);
    oo.writeObject(obj);
    oo.close();
    DeepCopyObjectReader oi = new DeepCopyObjectReader(new ByteArrayInputStream(o.toByteArray()), oo.getClassesByName());
    Object copy = oi.readObject();
    oi.close();
    @SuppressWarnings("unchecked")
    T castedCopy = (T) copy;
    return castedCopy;
  }

  private static class DeepCopyObjectWriter extends ObjectOutputStream {
    private final Map<String, Class<?>> m_classesByName = new HashMap<>();

    public DeepCopyObjectWriter(OutputStream out) throws IOException {
      super(out);
    }

    @Override
    protected void annotateClass(Class<?> c) throws IOException {
      if (c != null) {
        m_classesByName.put(c.getName(), c);
      }
    }

    public Map<String, Class<?>> getClassesByName() {
      return m_classesByName;
    }
  }

  private static class DeepCopyObjectReader extends ObjectInputStream {
    private final Map<String, Class<?>> m_classesByName;

    public DeepCopyObjectReader(InputStream in, Map<String, Class<?>> classesByName) throws IOException {
      super(in);
      m_classesByName = classesByName;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
      Class<?> c = m_classesByName.get(desc.getName());
      if (c != null) {
        return c;
      }
      return super.resolveClass(desc);
    }
  }
}
