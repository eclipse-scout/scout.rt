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
import java.security.AccessController;
import java.util.concurrent.ExecutionException;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Default exception translator to work with checked exceptions and runtime exceptions, but not with {@link Throwable}.
 * <p>
 * If given an {@link Exception}, or a {@link RuntimeException}, or if being a subclass thereof, that exception is
 * returned as given. Otherwise, a {@link PlatformException} is returned which wraps the given {@link Throwable}.
 * <p>
 * If given a wrapped exception like {@link UndeclaredThrowableException}, {@link InvocationTargetException} or
 * {@link ExecutionException}, its cause is unwrapped prior translation.
 * <p>
 * If the exception is of the type {@link Error}, it is not translated, but thrown instead. That is because an
 * {@link Error} indicates a serious problem due to a abnormal condition.
 */
public class DefaultExceptionTranslator implements IExceptionTranslator<Exception> {

  @Override
  public Exception translate(final Throwable throwable) {
    final Throwable eUnwrapped = unwrap(throwable);
    final Exception eTranslated = translateInternal(eUnwrapped);
    final Exception eDecorated = decorate(eTranslated);

    return eDecorated;
  }

  /**
   * Method invoked to translate the given {@link Throwable}.
   */
  protected Exception translateInternal(final Throwable t) {
    if (t instanceof Error) {
      throw (Error) t;
    }
    else if (t instanceof Exception) {
      return (Exception) t;
    }
    else {
      return new PlatformException(StringUtility.nvl(t.getMessage(), t.getClass().getSimpleName()), t)
          .withContextInfo("translator", DefaultExceptionTranslator.class.getName());
    }
  }

  /**
   * Returns the cause if being wrapped by another exception.
   *
   * @see #isWrapperException(Throwable)
   */
  public Throwable unwrap(final Throwable throwable) {
    Throwable t = throwable;
    while (t.getCause() != null && isWrapperException(t)) {
      t = t.getCause();
    }
    return t;
  }

  /**
   * Method invoked to decorate a translated exception, regardless of whether being translated or not.
   */
  public <EXCEPTION extends Throwable> EXCEPTION decorate(final EXCEPTION exception) {
    if (exception instanceof PlatformException) {
      // Associate the current user with this exception.
      ((PlatformException) exception).withContextInfo("user", SecurityUtility.getPrincipalNames(Subject.getSubject(AccessController.getContext())));
    }
    return exception;
  }

  /**
   * Returns <code>true</code>, if the given throwable is a wrapper for other exceptions.
   */
  protected boolean isWrapperException(final Throwable t) {
    return t instanceof UndeclaredThrowableException
        || t instanceof InvocationTargetException
        || t instanceof ExecutionException;
  }
}
