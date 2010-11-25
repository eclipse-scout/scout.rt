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
package org.eclipse.scout.commons.logger;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.internal.Activator;
import org.eclipse.scout.commons.logger.internal.EclipseLogWrapper;
import org.eclipse.scout.commons.logger.internal.EclipseToJavaDelegateListener;
import org.eclipse.scout.commons.logger.internal.JavaLogFormatter;
import org.eclipse.scout.commons.logger.internal.JavaLogWrapper;

/**
 * The default factory to create {@link IScoutLogger} objects.
 * <p>
 * This factory creates transparent wrappers of {@link Logger}, see {@link IScoutLogger} for more details
 * <p>
 * The system (or config.ini) property <code>org.eclipse.scout.log</code> defines the logging strategy. Valid values are
 * "java", "eclipse" and nothing. Default is "java"<br>
 * "java": all log events are passed to the java util log Logger "eclipse": all java util log Logger events are passed
 * to the eclipse log<br>
 * <p>
 * The system (or config.ini) property <code>org.eclipse.scout.log.level</code> is only valid for eclipse log and
 * defines which level of the java util log Loggers is passed to eclipse.<br>
 * The default is WARNING, since lower levels may produce substantial amount of log when using default eclipse log
 * settings. Valid values are ERROR, WARNING, INFO, DEBUG
 * <p>
 * Example settings in config.ini for eclipse log:
 * 
 * <pre>
 * eclipse.consoleLog=true
 * org.eclipse.scout.log=eclipse
 * org.eclipse.scout.log.level=WARNING
 * </pre>
 * <p>
 * Example settings in config.ini for java log:
 * 
 * <pre>
 * eclipse.consoleLog=false
 * org.eclipse.scout.log=java
 * </pre>
 */
public final class ScoutLogManager {

  public static final String STRATEGY;

  private static final String JAVA = "java";

  private ScoutLogManager() {
  }

  static {
    String strategy = null;
    if (Activator.getDefault() != null) {
      strategy = Activator.getDefault().getBundle().getBundleContext().getProperty("org.eclipse.scout.log");
    }
    else {
      strategy = System.getProperty("org.eclipse.scout.log", null);
    }
    STRATEGY = strategy != null ? strategy : JAVA;
    if (JAVA.equalsIgnoreCase(STRATEGY)) {
      setupJavaLogStrategy();
    }
  }

  private static void setupJavaLogStrategy() {
    Logger root = Logger.getLogger("");
    for (Handler h : root.getHandlers()) {
      if (h != null && h.getClass() == ConsoleHandler.class) {
        if (h.getFormatter() instanceof SimpleFormatter) {
          /*
           * install "better" log formatter than default one (when ConsoleHandler is used together with SimpleFormatter)
           */
          h.setFormatter(new JavaLogFormatter());
        }
        /*
         * set default log level for root to WARNING. INFO is insane as a default for production
         */
        if (root.getLevel() == Level.INFO) {
          root.setLevel(Level.WARNING);
        }
      }
    }
    //
    if (Platform.isRunning()) {
      Platform.addLogListener(new EclipseToJavaDelegateListener());
    }
  }

  public static IScoutLogger getLogger(Class clazz) {
    return getLogger(clazz.getName());
  }

  public static IScoutLogger getLogger(String name) {
    if (JAVA.equalsIgnoreCase(STRATEGY)) {
      return new JavaLogWrapper(name);
    }
    else {
      return new EclipseLogWrapper(name);
    }
  }
}
