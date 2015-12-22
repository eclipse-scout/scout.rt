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
import org.slf4j.helpers.MessageFormatter;

/**
 * Use to log to the APT console.
 *
 * @since 5.1
 */
public class AptLogger {

  private final Messager m_message;
  private final boolean m_consoleLog;

  public AptLogger(final ProcessingEnvironment env) {
    m_message = env.getMessager();
    m_consoleLog = Boolean.valueOf(env.getOptions().get("consoleLog"));
  }

  /**
   * Logs as <em>info</em>.
   *
   * @param msg
   *          the message with support for <em>formatting anchors</em> in the form of {} pairs.
   * @param args
   *          optional arguments to substitute <em>formatting anchors</em> in the message.
   */
  public void info(final String msg, final Object... args) {
    final String message = MessageFormatter.arrayFormat(msg, args).getMessage();
    final String logMsg = String.format("Annotation processing: %s [processor=%s]", message, JaxWsAnnotationProcessor.class.getSimpleName());

    m_message.printMessage(Kind.NOTE, logMsg);
    if (m_consoleLog) {
      System.out.println("[INFO] " + logMsg);
    }
  }

  /**
   * Logs as <em>warning</em>.
   *
   * @param msg
   *          the message with support for <em>formatting anchors</em> in the form of {} pairs.
   * @param args
   *          optional arguments to substitute <em>formatting anchors</em> in the message.
   */
  public void warn(final String msg, final Object... args) {
    final String message = MessageFormatter.arrayFormat(msg, args).getMessage();
    final String logMsg = String.format("Annotation processing: %s [processor=%s]", message, JaxWsAnnotationProcessor.class.getSimpleName());

    m_message.printMessage(Kind.WARNING, logMsg);
    if (m_consoleLog) {
      System.out.println("[WARN] " + logMsg);
    }
  }

  /**
   * Logs as <em>error</em>.
   *
   * @param msg
   *          the message with support for <em>formatting anchors</em> in the form of {} pairs.
   * @param args
   *          optional arguments to substitute <em>formatting anchors</em> in the message, with the last argument used
   *          as the execption's cause if of type {@link Throwable} and not referenced in the message.
   */
  public void error(final String msg, final Object... args) {
    final String message = MessageFormatter.arrayFormat(msg, args).getMessage();

    final StringWriter writer = new StringWriter();
    final PrintWriter out = new PrintWriter(writer);

    // Append message
    out.printf("Annotation processing: %s [processor=%s]", message, JaxWsAnnotationProcessor.class.getSimpleName());

    // Append stack trace
    final Throwable throwable = MessageFormatter.arrayFormat(msg, args).getThrowable();
    if (throwable != null) {
      out.println();
      throwable.printStackTrace(out);
    }

    m_message.printMessage(Kind.ERROR, writer.toString()); // fails the build
    if (m_consoleLog) {
      System.err.printf("[ERROR] %s\n", writer.toString());
    }
  }
}
