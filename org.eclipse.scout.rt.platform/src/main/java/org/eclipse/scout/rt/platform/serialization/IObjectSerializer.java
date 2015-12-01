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
package org.eclipse.scout.rt.platform.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This interfaces provides methods for serializing and deserializing java objects.
 * 
 * @since 3.8.2
 */
public interface IObjectSerializer {

  /**
   * Serializes the given object and returns its binary data.
   */
  byte[] serialize(Object o) throws IOException;

  /**
   * Serializes the given object into the given {@link OutputStream}.
   */
  void serialize(OutputStream out, Object o) throws IOException;

  /**
   * Deserializes the given binary data into a java object. The deserialized object's type is tested with the given
   * expected type, if it is not null, using {@link Class#isAssignableFrom(Class)}.
   */
  <T> T deserialize(byte[] buf, Class<T> expectedType) throws IOException, ClassNotFoundException;

  /**
   * Deserializes a java object from the given {@link InputStream}. The deserialized object's type is tested with the
   * given expected type, if it is not null, using {@link Class#isAssignableFrom(Class)}.
   */
  <T> T deserialize(InputStream stream, Class<T> expectedType) throws IOException, ClassNotFoundException;
}
