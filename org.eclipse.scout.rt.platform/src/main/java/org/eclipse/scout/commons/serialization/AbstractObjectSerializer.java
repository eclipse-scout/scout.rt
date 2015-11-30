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
package org.eclipse.scout.commons.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Base implementation of {@link IObjectSerializer}. Subclasses must provide a suitable {@link ObjectInputStream}.
 *
 * @since 3.8.2
 */
public abstract class AbstractObjectSerializer implements IObjectSerializer {

  private final IObjectReplacer m_objectReplacer;

  public AbstractObjectSerializer(IObjectReplacer objectReplacer) {
    m_objectReplacer = objectReplacer;
  }

  public IObjectReplacer getObjectReplacer() {
    return m_objectReplacer;
  }

  @Override
  public byte[] serialize(Object o) throws IOException {
    if (o == null) {
      return null;
    }
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    serialize(bos, o);
    return bos.toByteArray();
  }

  @Override
  public void serialize(OutputStream out, Object o) throws IOException {
    if (out == null) {
      return;
    }
    try (ObjectOutputStream oos = createObjectOutputStream(out, getObjectReplacer())) {
      oos.writeObject(o);
      oos.flush();
    }
  }

  @Override
  public <T> T deserialize(byte[] buf, Class<T> expectedType) throws IOException, ClassNotFoundException {
    if (buf == null) {
      return null;
    }
    return deserialize(new ByteArrayInputStream(buf), expectedType);
  }

  @Override
  public <T> T deserialize(InputStream in, Class<T> expectedType) throws IOException, ClassNotFoundException {
    if (in == null) {
      return null;
    }
    try (ObjectInputStream ois = createObjectInputStream(in, getObjectReplacer())) {
      Object o = ois.readObject();
      if (expectedType != null && !expectedType.isInstance(o)) {
        throw new IOException("deserialized object has unexpected type: expected '" + expectedType + "', actual '" + o.getClass() + "'.");
      }
      @SuppressWarnings("unchecked")
      T castedObject = (T) o;
      return castedObject;
    }
  }

  protected ObjectOutputStream createObjectOutputStream(OutputStream out, IObjectReplacer objectReplacer) throws IOException {
    if (objectReplacer == null) {
      return new ObjectOutputStream(out);
    }
    return new ReplacingObjectOutputStream(out, objectReplacer);
  }

  protected abstract ObjectInputStream createObjectInputStream(InputStream in, IObjectReplacer objectReplacer) throws IOException;

  private static class ReplacingObjectOutputStream extends ObjectOutputStream {

    private final IObjectReplacer m_objectReplacer;

    public ReplacingObjectOutputStream(OutputStream out, IObjectReplacer objectReplacer) throws IOException {
      super(out);
      m_objectReplacer = objectReplacer;
      enableReplaceObject(true);
    }

    @Override
    protected Object replaceObject(Object obj) throws IOException {
      return m_objectReplacer.replaceObject(obj);
    }
  }
}
