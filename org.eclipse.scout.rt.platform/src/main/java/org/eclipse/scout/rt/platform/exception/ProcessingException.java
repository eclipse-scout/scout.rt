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
package org.eclipse.scout.rt.platform.exception;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

/**
 * Represents processing errors that occur during application execution.
 */
public class ProcessingException extends PlatformException {
  private static final long serialVersionUID = 1L;

  private ProcessingStatus m_status;

  /**
   * Empty constructor is used to support auto-webservice publishing with java bean support
   */
  public ProcessingException() {
    this("undefined");
  }

  /**
   * Creates a {@link PlatformException} from the given message.
   * <p>
   * Optionally, <em>formatting anchors</em> in the form of {} pairs can be used in the message, which will be replaced
   * by the respective argument.
   * <p>
   * If the last argument is of the type {@link Throwable} and not referenced as formatting anchor in the message, that
   * {@link Throwable} is used as the exception's cause.
   * <p>
   * Internally, {@link MessageFormatter} is used to provide substitution functionality. Hence, The format is the very
   * same as if using {@link Logger SLF4j Logger}.
   *
   * @param message
   *          the message with support for <em>formatting anchors</em> in the form of {} pairs.
   * @param args
   *          optional arguments to substitute <em>formatting anchors</em> in the message, with the last argument used
   *          as the execption's cause if of type {@link Throwable} and not referenced in the message.
   */
  public ProcessingException(final String message, final Object... args) {
    super(message, args);
    m_status = new ProcessingStatus(null, super.getDisplayMessage(), this, 0, IProcessingStatus.ERROR);
  }

  /**
   * Creates a {@link ProcessingException} based on the given {@link IProcessingStatus}.
   */
  public ProcessingException(final IProcessingStatus status) {
    super(status.getBody(), status.getException());
    withStatus(status);
  }

  @Override
  public ProcessingException withContextInfo(final String name, final Object value, final Object... valueArgs) {
    super.withContextInfo(name, value, valueArgs);
    return this;
  }

  /**
   * Associates this exception's status with a title.
   * <p>
   * Optionally, <em>formatting anchors</em> in the form of {} pairs can be used in the title, which will be replaced by
   * the respective argument.
   */
  public ProcessingException withTitle(final String title, final Object... args) {
    m_status.setTitle(MessageFormatter.arrayFormat(title, args).getMessage());
    return this;
  }

  /**
   * Associates this exception's status with a code.
   */
  public ProcessingException withCode(final int code) {
    m_status.setCode(code);
    return this;
  }

  /**
   * Associates this exception's status with a severity.
   *
   * @see IStatus#OK
   * @see IStatus#INFO
   * @see IStatus#WARNING
   * @see IStatus#ERROR
   */
  public ProcessingException withSeverity(final int severity) {
    m_status.setSeverity(severity);
    return this;
  }

  /**
   * Returns the status; is never <code>null</code>.
   */
  public IProcessingStatus getStatus() {
    return m_status;
  }

  /**
   * Associates this exception with a status. The status must not be <code>null</code>.
   */
  public ProcessingException withStatus(final IProcessingStatus status) {
    Assertions.assertNotNull(status);
    m_status = status instanceof ProcessingStatus ? (ProcessingStatus) status : new ProcessingStatus(status);
    if (m_status.getException() == null) {
      m_status.setException(this);
    }
    return this;
  }

  @Override
  protected String getAdditionalContextInfos() {
    return StringUtility.join(", ",
        String.format("severity=%s", m_status.getSeverityName()),
        m_status.getCode() == 0 ? null : String.format("code=%s", m_status.getCode()));
  }

  /**
   * @return the complete stacktrace of the Throwable and all its causes (recursive)
   */
  public static StackTraceElement[] unionStackTrace(Throwable t) {
    final ArrayList<StackTraceElement> list = new ArrayList<>();
    while (t != null) {
      list.addAll(0, Arrays.asList(t.getStackTrace()));
      t = t.getCause();
    }
    return list.toArray(new StackTraceElement[list.size()]);
  }

  @Override
  protected String extractMessageText() {
    if (m_status != null) {
      return m_status.getMessage();
    }
    return super.extractMessageText();
  }
}
