/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.serialization;

/**
 * Utility for serializing and deserializing java objects. The utility works in a standard java environment.
 * <p/>
 * The utility uses an environment-dependent {@link IObjectSerializerFactory} for creating {@link IObjectSerializer}
 * instances. An optional {@link IObjectReplacer} can be used for replacing and resolving objects during the
 * serialization and deserialization process, respectively.
 * <p/>
 *
 * @since 3.8.2
 */
public final class SerializationUtility {

  private static final IObjectSerializerFactory FACTORY = new BasicObjectSerializerFactory();

  private SerializationUtility() {
    // nop
  }

  /**
   * Uses a {@link IObjectSerializerFactory} for creating a new {@link IObjectSerializer}.
   *
   * @return Returns a new {@link IObjectSerializer}.
   */
  public static IObjectSerializer createObjectSerializer() {
    return createObjectSerializer(null);
  }

  /**
   * Uses a {@link IObjectSerializerFactory} for creating a new {@link IObjectSerializer} which uses the given
   * {@link IObjectReplacer} for substituting objects during the serializing and deserializing process.
   *
   * @return Returns a new {@link IObjectSerializer}.
   */
  public static IObjectSerializer createObjectSerializer(IObjectReplacer objectReplacer) {
    return FACTORY.createObjectSerializer(objectReplacer);
  }

  /**
   * @return Returns an environment-dependent {@link ClassLoader} that is able to load all classes that are available in
   *         the running environment.
   */
  public static ClassLoader getClassLoader() {
    return FACTORY.getClassLoader();
  }
}
