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
import java.security.AccessController;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

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
 * {@link Error} indicates a serious problem due to an abnormal condition.
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
      return new PlatformException(ObjectUtility.nvl(t.getMessage(), t.getClass().getSimpleName()), t)
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
   * Visits the given {@link Throwable} and all of its causes calling the given {@link Predicate visitor} for each
   * {@link Throwable cause} (including the start throwable itself).
   *
   * @param start
   *     The starting {@link Throwable}. Will be visited first.
   * @param visitor
   *     The visitor to call. Each {@link Throwable} discovered will be passed to the {@link Predicate}. The given
   *     Throwable is never {@code null}. The start {@link Throwable} will be the first argument continued by all
   *     its causes. The visiting stops as soon as the {@link Predicate} returns {@code true} for the first time.
   * @return {@code true} if the visitor returned {@code true} for a {@link Throwable} (which means visiting was
   * aborted) or {@code false} otherwise (if all causes have been visited without the visitor to return
   * {@code true} for any of the causes).
   */
  public boolean throwableCausesAccept(Throwable start, Predicate<Throwable> visitor) {
    if (start == null || visitor == null) {
      return false;
    }

    Function<Throwable, Boolean> v = t -> {
      if (visitor.test(t)) {
        return true;
      }
      return null; // continue visiting
    };
    return visitThrowableCauses(start, v) != null;
  }

  /**
   * Visits the given {@link Throwable} and all of its causes calling the given {@link Function visitor} for each
   * {@link Throwable cause} (including the start throwable itself).
   *
   * @param start
   *     The starting {@link Throwable}. Will be visited first.
   * @param visitor
   *     The visitor to call. Each {@link Throwable} discovered will be passed to the function. The given Throwable
   *     is never {@code null}. The start {@link Throwable} will be the first argument continued by all its causes.
   *     The visiting stops as soon as the {@link Function} returns a non-null value for the first time.
   * @return The first non-null value returned by the visitor or {@code null} otherwise.
   */
  public <T> T visitThrowableCauses(Throwable start, Function<Throwable, T> visitor) {
    if (start == null || visitor == null) {
      return null;
    }

    Throwable cause = start;
    Throwable previousCause = null;
    while (cause != null && cause != previousCause) { // second check avoids endless loops
      T result = visitor.apply(cause);
      if (result != null) {
        return result; // early abort requested by visitor (result available)
      }
      // set previous cause
      previousCause = cause;
      // set next cause
      cause = cause.getCause();
    }
    return null; // completely visited (visitor never returned a non-null value)
  }

  /**
   * Method invoked to decorate a translated exception, regardless of whether being translated or not.
   */
  public <EXCEPTION extends Throwable> EXCEPTION decorate(final EXCEPTION exception) {
    if (exception instanceof IThrowableWithContextInfo) {
      // Associate the current user with this exception.
      ((IThrowableWithContextInfo) exception).withContextInfo("user", SecurityUtility.getPrincipalNames(Subject.getSubject(AccessController.getContext())));
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
