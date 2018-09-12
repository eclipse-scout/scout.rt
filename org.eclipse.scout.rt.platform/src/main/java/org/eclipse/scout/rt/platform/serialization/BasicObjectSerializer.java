/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;

/**
 * This {@link IObjectSerializer} implementation is designed to be used outside an OSGi environment. All classes are
 * expected to be loaded by the application class loader.
 *
 * @since 3.8.2
 */
public class BasicObjectSerializer extends AbstractObjectSerializer {

  public BasicObjectSerializer(IObjectReplacer objectReplacer) {
    super(objectReplacer);
  }

  @Override
  protected ObjectInputStream createObjectInputStream(InputStream in, IObjectReplacer objectReplacer) throws IOException {
    if (objectReplacer == null) {
      return new NonReplaceObjectInputStream(in);
    }
    return new ResolvingObjectInputStream(in, objectReplacer);
  }

  public static class NonReplaceObjectInputStream extends ObjectInputStream {

    public NonReplaceObjectInputStream(InputStream in) throws IOException {
      super(in);
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
      Class<?> c = super.resolveClass(desc);
      checkSuid(c, desc);
      return c;
    }
  }

  public static class ResolvingObjectInputStream extends ObjectInputStream {

    private final IObjectReplacer m_objectReplacer;

    public ResolvingObjectInputStream(InputStream in, IObjectReplacer objectReplacer) throws IOException {
      super(in);
      m_objectReplacer = objectReplacer;
      enableResolveObject(true);
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
      Class<?> c = super.resolveClass(desc);
      checkSuid(c, desc);
      return c;
    }

    @Override
    protected Object resolveObject(Object obj) throws IOException {
      return m_objectReplacer.resolveObject(obj);
    }
  }

  /**
   * Since java 8 interfaces may have default methods. When using jacoco or cglib this can have the effect that the
   * default serialVersionUID for interfaces can be different with or without instrumentation.
   * <p>
   * This affects only interface literals such as IFoo.class, Bar.class, etc.
   * <p>
   * This fix makes sure that interface literals ignore the serialVersionUID
   */
  protected static void checkSuid(Class<?> c, ObjectStreamClass desc) throws IOException {
    if (c != null && c.isInterface()) {
      long expectedSuid = ObjectStreamClass.lookupAny(c).getSerialVersionUID();
      long actualSuid = desc.getSerialVersionUID();
      if (expectedSuid != actualSuid) {
        //replace the input object serialVersionUID by the target class serialVersionUID
        try {
          Field suidField = ObjectStreamClass.class.getDeclaredField("suid");
          suidField.setAccessible(true);
          suidField.set(desc, expectedSuid);
        }
        catch (Exception e) {
          throw new IOException(e);
        }
      }
    }
  }

}
