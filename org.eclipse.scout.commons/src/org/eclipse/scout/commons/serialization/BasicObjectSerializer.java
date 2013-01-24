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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

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
      return new ObjectInputStream(in);
    }
    return new ResolvingObjectInputStream(in, objectReplacer);
  }

  public static class ResolvingObjectInputStream extends ObjectInputStream {

    private final IObjectReplacer m_objectReplacer;

    public ResolvingObjectInputStream(InputStream in, IObjectReplacer objectReplacer) throws IOException {
      super(in);
      m_objectReplacer = objectReplacer;
      enableResolveObject(true);
    }

    @Override
    protected Object resolveObject(Object obj) throws IOException {
      return m_objectReplacer.resolveObject(obj);
    }
  }
}
