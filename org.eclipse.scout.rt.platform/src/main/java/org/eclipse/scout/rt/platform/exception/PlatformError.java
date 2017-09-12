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
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * Represents errors that occur during application execution which should not be caught by application logic.
 *
 * @since 6.1
 */
public class PlatformError extends Error implements IThrowableWithContextInfo {

  private static final long serialVersionUID = 1L;

  private transient boolean m_consumed;

  private final List<String> m_contextInfos = new ArrayList<>();

  /**
   * Creates a {@link PlatformError} from the given message.
   * <p>
   * Optionally, <em>formatting anchors</em> in the form of {} pairs can be used in the message, which will be replaced
   * by the respective argument.
   * <p>
   * If the last argument is of the type {@link Throwable} and not referenced as formatting anchor in the message, that
   * {@link Throwable} is used as the error's cause.
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
  public PlatformError(final String message, final Object... args) {
    this(MessageFormatter.arrayFormat(message, args));
  }

  /**
   * Creates a {@link PlatformError} with the given SLF4j format.
   */
  protected PlatformError(final FormattingTuple format) {
    super(format.getMessage(), format.getThrowable());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getContextInfos() {
    return CollectionUtility.arrayList(m_contextInfos);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PlatformError withContextInfo(final String name, final Object value, final Object... valueArgs) {
    if (value == null) {
      return this;
    }

    final String valueAsString = value.toString();
    if (!StringUtility.hasText(name) || !StringUtility.hasText(valueAsString)) {
      return this;
    }

    final String formattedValue = MessageFormatter.arrayFormat(valueAsString, valueArgs).getMessage();
    final String contextInfo = String.format("%s=%s", name, formattedValue);
    if (m_contextInfos.contains(contextInfo)) {
      return this;
    }

    m_contextInfos.add(contextInfo);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isConsumed() {
    return m_consumed;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void consume() {
    m_consumed = true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDisplayMessage() {
    return extractMessageText();
  }

  @Override
  @SuppressWarnings("bsiRulesDefinition:htmlInString")
  public String getMessage() {
    final String msg = extractMessageText();
    final String formattedMessage = StringUtility.hasText(msg) ? msg : "<empty>";

    final String contextInfos = StringUtility.join(", ", getAdditionalContextInfos(), StringUtility.join(", ", getContextInfos()));
    if (StringUtility.isNullOrEmpty(contextInfos)) {
      return formattedMessage;
    }
    return String.format("%s [%s]", formattedMessage, contextInfos);
  }

  /**
   * @return Extracts the message used to compose the message returned by {@link #getMessage()} and
   *         {@link #getDisplayMessage()}.
   */
  protected String extractMessageText() {
    return super.getMessage();
  }

  /**
   * @return Returns additional context information available on this error.
   */
  protected String getAdditionalContextInfos() {
    return null;
  }
}
