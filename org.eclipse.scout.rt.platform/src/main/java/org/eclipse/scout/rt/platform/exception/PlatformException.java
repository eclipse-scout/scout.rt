/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.exception;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * Represents errors that occur during application execution.
 *
 * @since 5.2
 */
public class PlatformException extends RuntimeException implements IThrowableWithContextInfo {

  private static final long serialVersionUID = 1L;

  private transient boolean m_consumed;

  private final List<String> m_contextInfos = new ArrayList<>();

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
   *          as the exception's cause if of type {@link Throwable} and not referenced in the message.
   */
  public PlatformException(final String message, final Object... args) {
    this(MessageFormatter.arrayFormat(message, args));
  }

  /**
   * Creates a {@link PlatformException} with the given SLF4j format.
   */
  protected PlatformException(final FormattingTuple format) {
    super(format.getMessage(), format.getThrowable());
  }

  @Override
  public List<String> getContextInfos() {
    return CollectionUtility.arrayList(m_contextInfos);
  }

  @Override
  public PlatformException withContextInfo(final String name, final Object value, final Object... valueArgs) {
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

  @Override
  public boolean isConsumed() {
    return m_consumed;
  }

  @Override
  public void consume() {
    m_consumed = true;
  }

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
   * @return Returns additional context information available on this exception.
   */
  protected String getAdditionalContextInfos() {
    return null;
  }
}
