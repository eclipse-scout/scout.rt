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
package org.eclipse.scout.jaxws.apt.internal.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic.Kind;

import org.eclipse.scout.jaxws.apt.JaxWsAnnotationProcessor;

/**
 * Logger used to log messages to the APT console.
 *
 * @since 5.1
 */
public class Logger {

  private final Messager m_message;
  private final boolean m_consoleLog;

  public Logger(final ProcessingEnvironment env) {
    m_message = env.getMessager();
    m_consoleLog = Boolean.valueOf(env.getOptions().get("consoleLog"));
  }

  public void logInfo(final String msg, final Object... args) {
    final String logMsg = String.format("Annotation processing: %s [processor=%s]", String.format(msg, args), JaxWsAnnotationProcessor.class.getSimpleName());
    m_message.printMessage(Kind.NOTE, logMsg);
    if (m_consoleLog) {
      System.out.println("[INFO] " + logMsg);
    }
  }

  public void logWarn(final String msg, final Object... args) {
    final String logMsg = String.format("Annotation processing: %s [processor=%s]", String.format(msg, args), JaxWsAnnotationProcessor.class.getSimpleName());
    m_message.printMessage(Kind.WARNING, logMsg);
    if (m_consoleLog) {
      System.out.println("[WARN] " + logMsg);
    }
  }

  public void logError(final String msg, final Object... args) {
    final String logMsg = String.format("Annotation processing: %s [processor=%s]", String.format(msg, args), JaxWsAnnotationProcessor.class.getSimpleName());

    m_message.printMessage(Kind.ERROR, logMsg); // fails the build
    if (m_consoleLog) {
      System.err.println(String.format("[ERROR] %s", logMsg));
    }
  }

  public void logError(final Throwable t, final String msg, final Object... args) {
    final String logMsg = String.format("Annotation processing: %s [processor=%s]", String.format(msg, args), JaxWsAnnotationProcessor.class.getSimpleName());

    final StringWriter stacktrace = new StringWriter();
    t.printStackTrace(new PrintWriter(stacktrace));
    m_message.printMessage(Kind.ERROR, logMsg + stacktrace); // for fail the build
    if (m_consoleLog) {
      System.err.println(String.format("[ERROR] %s\n%s:%s", logMsg, t.getMessage(), stacktrace));
    }
  }
}
