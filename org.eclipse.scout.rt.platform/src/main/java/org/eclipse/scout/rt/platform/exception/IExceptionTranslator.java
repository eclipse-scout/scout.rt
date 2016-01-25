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
@ApplicationScoped
public interface IExceptionTranslator<EXCEPTION extends Throwable> {

  /**
   * Translates the given {@link Throwable}. If of type {@link Error}, it is not translated, but re-thrown instead. That
   * is because an Error indicates a serious problem due to an abnormal condition.
   *
   * @param throwable
   *          to be translated
   */
  EXCEPTION translate(Throwable throwable);

  /**
   * Translates the given {@link Throwable}.
   *
   * @param throwable
   *          to be translated
   * @param throwOnError
   *          <code>false</code> if an {@link Error} should be translated instead of being re-thrown. By default, errors
   *          are re-thrown, because indicating a serious problem due to an abnormal condition.
   */
  EXCEPTION translate(Throwable throwable, boolean throwOnError);
}
