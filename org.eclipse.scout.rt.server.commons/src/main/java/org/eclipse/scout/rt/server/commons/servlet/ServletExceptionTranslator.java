/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ExecutionException;

import jakarta.servlet.ServletException;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.IExceptionTranslator;

/**
 * Exception translator to work with {@link ServletException}s.
 * <p>
 * If given a {@link ServletException}, it is returned as given. For all other exceptions, a {@link ServletException} is
 * returned which wraps the given exception.
 * <p>
 * If given a wrapped exception like {@link UndeclaredThrowableException}, {@link InvocationTargetException} or
 * {@link ExecutionException}, its cause is unwrapped prior translation.
 * <p>
 * If the exception is of the type {@link Error}, it is not translated, but thrown instead. That is because an
 * {@link Error} indicates a serious problem due to a abnormal condition.
 */
public class ServletExceptionTranslator implements IExceptionTranslator<ServletException> {

  @Override
  public ServletException translate(final Throwable throwable) {
    final Throwable eUnwrapped = BEANS.get(DefaultExceptionTranslator.class).unwrap(throwable);
    final ServletException eTranslated = translateInternal(eUnwrapped);
    final ServletException eDecorated = BEANS.get(DefaultExceptionTranslator.class).decorate(eTranslated);

    return eDecorated;
  }

  /**
   * Method invoked to translate the given {@link Throwable}.
   */
  protected ServletException translateInternal(final Throwable t) {
    if (t instanceof Error) {
      throw (Error) t;
    }
    else if (t instanceof ServletException) {
      return (ServletException) t;
    }
    else {
      return new ServletException("Internal server error", t);
    }
  }
}
