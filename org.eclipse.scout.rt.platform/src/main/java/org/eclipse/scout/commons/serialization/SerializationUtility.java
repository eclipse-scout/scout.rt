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

import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for serializing and deserializing java objects. The utility works in a standard java environment as well as
 * in an OSGi environment. All classes that are available on the class path or within an OSGi bundle can be
 * deserialized.
 * <p/>
 * The utility uses an environment-dependent {@link IObjectSerializerFactory} for creating {@link IObjectSerializer}
 * instances. An optional {@link IObjectReplacer} can be used for replacing and resolving objects during the
 * serialization and deserialization process, respectively.
 * <p/>
 * <h3>Custom Strategy</h3> A custom {@link IObjectSerializerFactory} can be provided by a fragment bundle using
 * <em>org.eclipse.scout.commons</em> as host bundle. The custom {@link IObjectSerializerFactory} class must use the
 * following fully qualified class name:
 * <p/>
 * <code>org.eclipse.scout.commons.serialization.CustomObjectSerializerFactory</code>
 *
 * @since 3.8.2
 */
public final class SerializationUtility {
  private static final Logger LOG;

  private static final IObjectSerializerFactory FACTORY;

  static {
    LOG = LoggerFactory.getLogger(SerializationUtility.class);
    FACTORY = createObjectSerializerFactory();
  }

  private SerializationUtility() {
    // nop
  }

  private static IObjectSerializerFactory createObjectSerializerFactory() {
    IObjectSerializerFactory factory = null;
    // check whether there is a custom object serializer factory available
    try {
      Class<?> customSerializerFactory = Class.forName("org.eclipse.scout.commons.serialization.CustomObjectSerializerFactory");
      LOG.info("loaded custom object serializer factory: [" + customSerializerFactory + "]");
      if (!IObjectSerializerFactory.class.isAssignableFrom(customSerializerFactory)) {
        LOG.warn("custom object serializer factory is not implementing [" + IObjectSerializerFactory.class + "]");
      }
      else if (Modifier.isAbstract(customSerializerFactory.getModifiers())) {
        LOG.warn("custom object serializer factory is an abstract class [" + customSerializerFactory + "]");
      }
      else {
        factory = (IObjectSerializerFactory) customSerializerFactory.newInstance();
      }
    }
    catch (ClassNotFoundException e) {
      // no custom object serializer factory installed
    }
    catch (Exception e) {
      LOG.warn("Unexpected problem while creating a new instance of custom object serializer factory", e);
    }

    if (factory == null) {
      factory = new BasicObjectSerializerFactory();
    }

    return factory;
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
