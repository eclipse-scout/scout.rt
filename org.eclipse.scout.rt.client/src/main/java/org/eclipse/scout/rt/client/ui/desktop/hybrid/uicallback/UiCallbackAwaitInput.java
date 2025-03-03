/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid.uicallback;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.scout.rt.client.ClientConfigProperties.DefaultUiCallbackTimeoutMillisProperty;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.IExceptionTranslator;

@Bean
public class UiCallbackAwaitInput {

  private String m_name;
  private Long m_timeoutInMillis;
  private IExceptionTranslator<? extends RuntimeException> m_exceptionTranslator;

  /**
   * Returns a human-readable callback name to use in log messages when the callback fails, or {@code null} if no name was specified.
   */
  public String getName() {
    return m_name;
  }

  /**
   * Sets a human-readable callback name to use in log messages when the callback fails. May be {@code null}.
   */
  public UiCallbackAwaitInput withName(String name) {
    m_name = name;
    return this;
  }

  /**
   * Returns the time in milliseconds to wait for the callback completion. After the timeout has elapsed, a {@link TimeoutException} is thrown.
   * If this value is {@code null}, the default timeout specified by the {@link DefaultUiCallbackTimeoutMillisProperty} is used. If this
   * value is 0 or negative, no timeout is used, i.e. the code waits indefinitely.
   */
  public Long getTimeoutInMillis() {
    return m_timeoutInMillis;
  }

  /**
   * Sets the time in milliseconds to wait for the callback completion. After the timeout has elapsed, a {@link TimeoutException} is thrown.
   * If this value is {@code null}, the default timeout specified by the {@link DefaultUiCallbackTimeoutMillisProperty} is used. If this
   * value is 0 or negative, no timeout is used, i.e. the code waits indefinitely.
   */
  public UiCallbackAwaitInput withTimeoutInMillis(Long timeoutInMillis) {
    m_timeoutInMillis = timeoutInMillis;
    return this;
  }

  /**
   * Convenience method to set {@link #withTimeoutInMillis(Long)} with a custom time unit.
   */
  public UiCallbackAwaitInput withTimeout(long timeout, TimeUnit timeoutUnit) {
    return withTimeoutInMillis(timeoutUnit.toMillis(timeout));
  }

  /**
   * Returns an optional exception translator to convert any errors that occurred during the wait into a runtime exception.
   * If this is {@code null}, the {@link DefaultRuntimeExceptionTranslator} is used.
   */
  public IExceptionTranslator<? extends RuntimeException> getExceptionTranslator() {
    return m_exceptionTranslator;
  }

  /**
   * Specifies an optional exception translator to convert any errors that occurred during the wait into a runtime exception.
   * If this is {@code null}, the {@link DefaultRuntimeExceptionTranslator} is used.
   */
  public UiCallbackAwaitInput withExceptionTranslator(IExceptionTranslator<? extends RuntimeException> exceptionTranslator) {
    m_exceptionTranslator = exceptionTranslator;
    return this;
  }
}
