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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.internal.Activator;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Utility for serializing and deserializing java objects. The utility works in a standard java environment as well as
 * in an OSGi environment. All classes that are available on the class path or within an OSGi bundle can be
 * deserialized.
 * <p/>
 * The utility uses an environment-dependent {@link IObjectSerializerFactory} for creating {@link IObjectSerializer}
 * instances. An optional {@link IObjectReplacer} can be used for replacing and resolving objects during the
 * serialization and deserialization process, respectively.
 * <p/>
 * <h3>OSGi Environments</h3> In OSGi Environments, the ordered list of bundle name prefixes used for loading classes
 * should be configured in the config.ini file or as system property:
 * <p/>
 * <code>org.eclipse.scout.commons.serialization.bundleOrderPrefixes</code>
 * <h3>Custom Strategy</h3> A custom {@link IObjectSerializerFactory} can be provided by a fragment bundle using
 * <em>org.eclipse.scout.commons</em> as host bundle. The custom {@link IObjectSerializerFactory} class must use the
 * following fully qualified class name:
 * <p/>
 * <code>org.eclipse.scout.commons.serialization.CustomObjectSerializerFactory</code>
 * 
 * @since 3.8.2
 */
public final class SerializationUtility {

  public static final String BUNDLE_ORDER_PREFIX_PROPERTY_NAME = "org.eclipse.scout.commons.serialization.bundleOrderPrefixes";
  private static final IScoutLogger LOG;
  private static final IObjectSerializerFactory FACTORY;

  static {
    LOG = ScoutLogManager.getLogger(SerializationUtility.class);
    FACTORY = createObjectSerializerFactory();
  }

  private SerializationUtility() {
    // nop
  }

  private static IObjectSerializerFactory createObjectSerializerFactory() {
    IObjectSerializerFactory factory = null;
    if (Activator.getDefault() != null) {
      // check whether there is a custom object serializer factory available
      try {
        Class<?> customSerializerFactory = Activator.getDefault().getBundle().loadClass("org.eclipse.scout.commons.serialization.CustomObjectSerializerFactory");
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

      // no custom object serializer factory. Use bundle based serializer factory
      if (factory == null) {
        factory = new BundleObjectSerializerFactory();
      }
    }
    else {
      // running outside eclipse. Use basic strategy.
      factory = new BasicObjectSerializerFactory();
    }

    return factory;
  }

  /**
   * In OSGi Environments, this method returns the ordered list of bundle name prefixes used for loading classes as it
   * is configured in the config.ini file or as system property:
   * <p/>
   * <code>org.eclipse.scout.commons.serialization.bundleOrderPrefixes</code>
   * <p/>
   * If the config.ini or system property is not set or if the method is invoked outside an OSGi environment, a default
   * value is computed and returned.
   */
  public static String[] getBundleOrderPrefixes() {
    String rawBundlePrefixes = null;
    if (Activator.getDefault() != null) {
      rawBundlePrefixes = Activator.getDefault().getBundle().getBundleContext().getProperty(BUNDLE_ORDER_PREFIX_PROPERTY_NAME);
    }
    if (!StringUtility.hasText(rawBundlePrefixes)) {
      rawBundlePrefixes = System.getProperty(BUNDLE_ORDER_PREFIX_PROPERTY_NAME, null);
    }
    if (!StringUtility.hasText(rawBundlePrefixes)) {
      rawBundlePrefixes = "org.eclipse.scout";
      if (Activator.getDefault() != null) {
        IProduct product = Platform.getProduct();
        if (product != null && product.getDefiningBundle() != null && product.getDefiningBundle().getSymbolicName() != null) {
          String prefix = product.getDefiningBundle().getSymbolicName().replaceAll("^(.*\\.)(client|shared|server|ui)(\\.core)?.*$", "$1");
          rawBundlePrefixes = StringUtility.join(",", prefix, rawBundlePrefixes);
        }
      }
      LOG.warn("bundle order prefixes are neither defined in config.ini nor as a system property. Using default value: " + BUNDLE_ORDER_PREFIX_PROPERTY_NAME + "=" + rawBundlePrefixes + "");
    }
    List<String> bundlePrefixes = new ArrayList<String>();
    for (String s : StringUtility.split(rawBundlePrefixes, ",")) {
      s = StringUtility.trim(s);
      if (StringUtility.hasText(s)) {
        bundlePrefixes.add(s);
      }
    }
    return bundlePrefixes.toArray(new String[bundlePrefixes.size()]);
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
