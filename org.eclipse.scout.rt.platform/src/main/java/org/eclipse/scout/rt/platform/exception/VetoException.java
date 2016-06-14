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

import org.eclipse.scout.rt.platform.html.IHtmlContent;
import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

/**
 * Represents a {@link ProcessingException} with <em>VETO</em> character. If thrown server-side, exceptions of this type
 * are transported to the client and typically visualized in the form of a message box.
 */
public class VetoException extends ProcessingException {

  private static final long serialVersionUID = 1L;

  /**
   * HTML message, localized to the current locale.
   */
  private IHtmlContent m_htmlMessage;

  /**
   * Empty constructor is used to support auto-webservice publishing with java bean support
   */
  public VetoException() {
    super();
  }

  /**
   * Creates a {@link VetoException} from the given message.
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
  public VetoException(final String message, final Object... args) {
    super(message, args);
  }

  /**
   * Creates a {@link VetoException} based on the given {@link IProcessingStatus}.
   */
  public VetoException(final IProcessingStatus status) {
    super(status);
  }

  @Override
  public VetoException withContextInfo(final String name, final Object value, final Object... valueArgs) {
    super.withContextInfo(name, value, valueArgs);
    return this;
  }

  @Override
  public VetoException withTitle(final String title, final Object... args) {
    super.withTitle(title, args);
    return this;
  }

  @Override
  public VetoException withCode(final int code) {
    super.withCode(code);
    return this;
  }

  @Override
  public VetoException withSeverity(final int severity) {
    super.withSeverity(severity);
    return this;
  }

  @Override
  public VetoException withStatus(final IProcessingStatus status) {
    super.withStatus(status);
    return this;
  }

  public IHtmlContent getHtmlMessage() {
    return m_htmlMessage;
  }

  /**
   * Associates this exception with a HTML message, which typically is preferred over the bare message.
   */
  public VetoException withHtmlMessage(final IHtmlContent htmlMessage) {
    m_htmlMessage = htmlMessage;
    return this;
  }

  @Override
  protected String extractMessageText() {
    if (m_htmlMessage != null) {
      return m_htmlMessage.toPlainText();
    }
    return super.extractMessageText();
  }
}
