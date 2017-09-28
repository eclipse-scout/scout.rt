/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.logger;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.AnnotationFactory;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.BeanUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Determines and installs the appropriate {@link ILoggerSupport} for the current slf4j environment. This implementation
 * supports the following logging frameworks:
 * <ul>
 * <li>Logback</li>
 * <li>Log4j</li>
 * <li>java.util.logging</li>
 * </ul>
 * In any other case the {@link NullLoggerSupport} is installed. Hence <code>BEANS.get(ILoggerSupport.class)</code>
 * returns always an object.
 * <p>
 * You can override {@link #registerLoggerSupportMappings(Map)} for adding more or different {@link ILoggerSupport}
 * mappings or you can register another bean with lower {@link org.eclipse.scout.rt.platform.Order}.
 *
 * @since 5.2
 */
public class LoggerPlatformListener implements IPlatformListener {

  public static final String LOGGER_FACTORY_CLASS_STR_LOGBACK = "ch.qos.logback.classic.util.ContextSelectorStaticBinder";
  public static final String LOGGER_FACTORY_CLASS_STR_LOG4J = "org.slf4j.impl.Log4jLoggerFactory";
  public static final String LOGGER_FACTORY_CLASS_STR_JUL = "org.slf4j.impl.JDK14LoggerFactory";
  public static final String LOGGER_SUPPORT_PACKAGE_NAME_PREFIX = "org.eclipse.scout.rt.platform.logger.";
  public static final String LOGGER_SUPPORT_CLASS_NAME_LOGBACK = LOGGER_SUPPORT_PACKAGE_NAME_PREFIX + "LogbackLoggerSupport";
  public static final String LOGGER_SUPPORT_CLASS_NAME_LOG4J = LOGGER_SUPPORT_PACKAGE_NAME_PREFIX + "Log4jLoggerSupport";
  public static final String LOGGER_SUPPORT_CLASS_NAME_JUL = LOGGER_SUPPORT_PACKAGE_NAME_PREFIX + "JulLoggerSupport";

  private static final Logger LOG = LoggerFactory.getLogger(LoggerPlatformListener.class);

  private final Map<String, String> m_loggerSupportByLoggerFactoryId;

  public LoggerPlatformListener() {
    m_loggerSupportByLoggerFactoryId = new HashMap<>();
    registerLoggerSupportMappings(m_loggerSupportByLoggerFactoryId);
  }

  @Override
  public void stateChanged(PlatformEvent event) {
    if (event.getState() == State.BeanManagerPrepared) {
      registerLoggerSupportBean(event.getSource().getBeanManager());
    }
  }

  /**
   * Initialized the mapping of slf4j logger factory class ids to the fully qualified {@link ILoggerSupport}. Subclasses
   * may extend or replace the mapping.
   *
   * @param mapping
   */
  protected void registerLoggerSupportMappings(Map<String, String> mapping) {
    mapping.put(LOGGER_FACTORY_CLASS_STR_LOGBACK, LOGGER_SUPPORT_CLASS_NAME_LOGBACK);
    mapping.put(LOGGER_FACTORY_CLASS_STR_LOG4J, LOGGER_SUPPORT_CLASS_NAME_LOG4J);
    mapping.put(LOGGER_FACTORY_CLASS_STR_JUL, LOGGER_SUPPORT_CLASS_NAME_JUL);
  }

  protected void registerLoggerSupportBean(IBeanManager beanManager) {
    ILoggerSupport loggerSupport = null;
    String loggerFactoryClassStr = null;
    try {
      StaticLoggerBinder loggerBinder = StaticLoggerBinder.getSingleton();
      loggerFactoryClassStr = loggerBinder.getLoggerFactoryClassStr();
      LOG.debug("Found slf4j logger factory [class={}, classStr={}]", loggerBinder.getLoggerFactory(), loggerFactoryClassStr);

      String loggerSupportFqcn = getLoggerSupportFqcn(loggerFactoryClassStr);
      LOG.debug("Determined scout logger support FQCN {}", loggerSupportFqcn);

      if (loggerSupportFqcn != null) {
        loggerSupport = createLoggerSupport(loggerSupportFqcn);
      }
    }
    catch (Exception | NoClassDefFoundError e) { // catch NoClassDefFoundError by intention (threw if no slf4j binding is available)
      LOG.warn("Could not determine or install factory speicific logger support. Falling back to {}", NullLoggerSupport.class.getName(), e);
    }
    if (loggerSupport == null) {
      loggerSupport = createNullLoggerSupport(loggerFactoryClassStr);
    }
    beanManager.registerBean(new BeanMetaData(ILoggerSupport.class, loggerSupport).withAnnotation(AnnotationFactory.createApplicationScoped()));
    LOG.info("Registered logger support {}", loggerSupport.getClass().getName());
  }

  /**
   * Returns the fully qualified class name of the logger support identified for the given logger factory id.
   */
  protected String getLoggerSupportFqcn(String loggerFactoryId) {
    return m_loggerSupportByLoggerFactoryId.get(loggerFactoryId);
  }

  /**
   * Creates a new instance of the given logger support fully qualified class name and checks its type.
   */
  protected ILoggerSupport createLoggerSupport(String loggerSupportFqcn) throws ClassNotFoundException {
    Class<?> clazz = SerializationUtility.getClassLoader().loadClass(loggerSupportFqcn);
    Assertions.assertTrue(ILoggerSupport.class.isAssignableFrom(clazz));
    return ILoggerSupport.class.cast(BeanUtility.createInstance(clazz));
  }

  /**
   * Creates a new {@link NullLoggerSupport} that is used as fall back strategy.
   */
  protected ILoggerSupport createNullLoggerSupport(String loggerFactoryClassStr) {
    return new NullLoggerSupport(loggerFactoryClassStr);
  }
}
