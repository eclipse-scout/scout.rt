/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.shared.runner;

import java.lang.reflect.Modifier;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.serialization.SerializationUtility;
import org.eclipse.scout.rt.testing.shared.Activator;

/**
 * Utility for creating a custom test environment instance.
 * 
 * @since 4.2.x
 */
public final class TestEnvironmentUtility {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TestEnvironmentUtility.class);

  private TestEnvironmentUtility() {
  }

  /**
   * Creates a new test environment instance. If a configuration value with the given property name is set, it is
   * resolved and an exception is thrown in case of an error. If the config value is not set, the default environment
   * class is loaded. If it does not exist or in case of an error, <code>null</code> is returned.
   */
  public static <T> T createTestEnvironment(Class<T> testEnvironmentClass, String customTestEnvironmentConfigPropertyName, String defaultCustomTestEnvironmentClassName) {
    if (SerializationUtility.getClassLoader() == null) {
      return null;
    }

    // check whether there is a custom test environment available
    Activator activator = Activator.getDefault();
    if (activator != null) {
      String className = StringUtility.trim(activator.getBundle().getBundleContext().getProperty(customTestEnvironmentConfigPropertyName));
      if (StringUtility.hasText(className)) {
        T environment = loadCustomTestEnvironmentClass(testEnvironmentClass, className);
        if (environment == null) {
          LOG.error("custom test environment specified by config property cannot be loaded '{}'", className);
          throw new IllegalStateException("Custom test environment cannot be loaded. Check configuration.");
        }
        return environment;
      }
    }
    return loadCustomTestEnvironmentClass(testEnvironmentClass, defaultCustomTestEnvironmentClassName);
  }

  private static <T> T loadCustomTestEnvironmentClass(Class<T> testEnvironmentClass, String customTestEnvironmentClassName) {
    T environment = null;
    try {
      Class<?> customTestEnvironment = SerializationUtility.getClassLoader().loadClass(customTestEnvironmentClassName);
      LOG.info("loaded custom test environment: [" + customTestEnvironment + "]");
      if (!testEnvironmentClass.isAssignableFrom(customTestEnvironment)) {
        LOG.warn("custom test environment is not implementing [" + testEnvironmentClass + "]");
      }
      else if (Modifier.isAbstract(customTestEnvironment.getModifiers())) {
        LOG.warn("custom test environment is an abstract class [" + customTestEnvironment + "]");
      }
      else {
        environment = testEnvironmentClass.cast(customTestEnvironment.newInstance());
      }
    }
    catch (ClassNotFoundException e) {
      LOG.debug("no custom custom test environment installed", e);
    }
    catch (Exception e) {
      LOG.warn("Unexpected problem while creating a new instance of custom test environment", e);
    }
    return environment;
  }
}
