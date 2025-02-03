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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ExecutionException;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Exception translators are used to translate an exception into another exception.
 * <p>
 * Also, they unwrap the cause of wrapper exceptions, like {@link UndeclaredThrowableException}, or
 * {@link InvocationTargetException}, or {@link ExecutionException}.
 * <p>
 * If the exception is of the type {@link Error}, it is typically not translated, but re-thrown instead. That is because
 * an {@link Error} indicates a serious problem due to an abnormal condition.
 *
 * @since 5.2
 */
@FunctionalInterface
@ApplicationScoped
public interface IExceptionTranslator<EXCEPTION extends Throwable> {

  /**
   * Translates the given {@link Throwable}. If of type {@link Error}, it is not translated, but re-thrown instead. That
   * is because an Error indicates a serious problem due to an abnormal condition.
   *
   * @param throwable
   *     to be translated
   */
  EXCEPTION translate(Throwable throwable);
}
