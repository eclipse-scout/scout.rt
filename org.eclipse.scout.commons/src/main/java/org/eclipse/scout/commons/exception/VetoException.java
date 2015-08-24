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
package org.eclipse.scout.commons.exception;

import java.io.Serializable;

import org.eclipse.scout.commons.html.IHtmlContent;

/**
 * This class is a special subclass of {@link ProcessingException} to mark a vetoed exception that is specialized from a
 * general {@link ProcessingException}.
 * <p>
 * E.g. for actions that are not allowed or invalid data
 * </p>
 */
public class VetoException extends ProcessingException implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * Html message, localized to the current locale.
   */
  private IHtmlContent m_messageHtmlBody;

  /**
   * Empty constructor is used to support auto-webservice publishing with java bean support
   */
  public VetoException() {
    super();
  }

  public VetoException(String message) {
    this(null, message);
  }

  public VetoException(IHtmlContent htmlMessage) {
    this(htmlMessage.toPlainText());
    m_messageHtmlBody = htmlMessage;
  }

  public VetoException(String message, Throwable cause) {
    this(null, message, cause);
  }

  public VetoException(String message, Throwable cause, int errorCode) {
    this(null, message, cause, errorCode);
  }

  public VetoException(String message, int errorCode, int severity) {
    this(null, message, errorCode, severity);
  }

  public VetoException(String message, Throwable cause, int errorCode, int severity) {
    this(null, message, cause, errorCode, severity);
  }

  public VetoException(String title, String message) {
    this(title, message, null);
  }

  public VetoException(String title, String message, Throwable cause) {
    this(title, message, cause, 0);
  }

  public VetoException(String title, String message, Throwable cause, int errorCode) {
    this(title, message, cause, errorCode, IProcessingStatus.ERROR);
  }

  public VetoException(String title, String message, int errorCode, int severity) {
    this(title, message, null, errorCode, severity);
  }

  public VetoException(String title, String message, Throwable cause, int errorCode, int severity) {
    this(title, message, null, cause, errorCode, severity);
  }

  public VetoException(String title, String message, IHtmlContent htmlMessage, Throwable cause, int errorCode, int severity) {
    this(new ProcessingStatus(title, message, cause, errorCode, severity));
    m_messageHtmlBody = htmlMessage;
  }

  public VetoException(IProcessingStatus status) {
    super(status);
  }

  public IHtmlContent getHtmlBody() {
    return m_messageHtmlBody;
  }

  public void setHtmlBody(IHtmlContent messageHtmlBody) {
    m_messageHtmlBody = messageHtmlBody;
  }
}
