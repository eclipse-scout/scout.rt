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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

/**
 * Represents errors that occur during application execution.
 *
 * @since 5.2
 */
public class PlatformException extends RuntimeException implements Serializable {

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
   *          as the execption's cause if of type {@link Throwable} and not referenced in the message.
   */
  public PlatformException(final String message, final Object... args) {
    super(MessageFormatter.arrayFormat(message, args).getMessage(), MessageFormatter.arrayFormat(message, args).getThrowable());
  }

  /**
   * Returns the context info associated with this exception.
   */
  public List<String> getContextInfos() {
    return CollectionUtility.arrayList(m_contextInfos);
  }

  /**
   * Associates this exception with some contextual information, which help to diagnose the root cause of the exception,
   * and to provide some information about the current calling context. If the same 'name-value' pair is already
   * associated, or the <code>name</code> or <code>key</code> is <code>null</code> or empty, this method call does
   * nothing.
   * <p>
   * Optionally, <em>formatting anchors</em> in the form of {} pairs can be used in the value, which will be replaced by
   * the respective argument.
   * <p>
   * Internally, {@link MessageFormatter} is used to provide substitution functionality. Hence, The format is the very
   * same as if using {@link Logger SLF4j Logger}.
   *
   * @param name
   *          the name of the context info.
   * @param value
   *          the value with support for <em>formatting anchors</em> in the form of {} pairs.
   * @param valueArgs
   *          optional arguments to substitute <em>formatting anchors</em> in the value.
   */
  public PlatformException withContextInfo(final String name, final Object value, final Object... valueArgs) {
    if (value == null) {
      return this;
    }

    final String valueAsString = value.toString();
    if (!StringUtility.hasText(name) || !StringUtility.hasText(valueAsString)) {
      return this;
    }

    final String formattedValue = MessageFormatter.arrayFormat(valueAsString.toString(), valueArgs).getMessage();
    final String contextInfo = String.format("%s=%s", name, formattedValue);
    if (m_contextInfos.contains(contextInfo)) {
      return this;
    }

    m_contextInfos.add(contextInfo);
    return this;
  }

  /**
   * Returns whether this exception was already consumed.
   */
  public boolean isConsumed() {
    return m_consumed;
  }

  /**
   * Marks this exception as <em>consumed</em>.
   */
  public void consume() {
    m_consumed = true;
  }

  /**
   * Returns the bare message without context messages. This method should be used to show the exception to the user.
   */
  public String getDisplayMessage() {
    return extractMessageText();
  }

  @Override
  public String getMessage() {
    final String msg = extractMessageText();
    final String formattedMessage = StringUtility.hasText(msg) ? msg : "<empty>";

    String contextInfos = StringUtility.join(", ", getAdditionalContextInfos(), StringUtility.join(", ", getContextInfos()));
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
