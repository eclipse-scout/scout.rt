/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.exception;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ExecutionException;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

/**
 * Default exception translator to work with runtime exceptions.
 * <p>
 * If given a {@link RuntimeException}, it is returned as given. For a checked exception, a {@link PlatformException} is
 * returned which wraps the given checked exception.
 * <p>
 * If given a wrapped exception like {@link UndeclaredThrowableException}, {@link InvocationTargetException} or
 * {@link ExecutionException}, its cause is unwrapped prior translation.
 * <p>
 * If the exception is of the type {@link Error}, it is not translated, but thrown instead. That is because an
 * {@link Error} indicates a serious problem due to a abnormal condition.
 */
public class DefaultRuntimeExceptionTranslator implements IExceptionTranslator<RuntimeException> {

  @Override
  public RuntimeException translate(final Throwable throwable) {
    final Throwable eUnwrapped = BEANS.get(DefaultExceptionTranslator.class).unwrap(throwable);
    final RuntimeException eTranslated = translateInternal(eUnwrapped);
    final RuntimeException eDecorated = BEANS.get(DefaultExceptionTranslator.class).decorate(eTranslated);

    return eDecorated;
  }

  /**
   * Method invoked to translate the given {@link Throwable}.
   */
  protected RuntimeException translateInternal(final Throwable t) {
    if (t instanceof Error) {
      throw (Error) t;
    }
    else if (t instanceof IOException) {
      return new UncheckedIOException((IOException) t);
    }
    else if (t instanceof RuntimeException) {
      return (RuntimeException) t;
    }
    else {
      return new PlatformException(ObjectUtility.nvl(t.getMessage(), t.getClass().getSimpleName()), t)
          .withContextInfo("translator", DefaultRuntimeExceptionTranslator.class.getName());
    }
  }
}
