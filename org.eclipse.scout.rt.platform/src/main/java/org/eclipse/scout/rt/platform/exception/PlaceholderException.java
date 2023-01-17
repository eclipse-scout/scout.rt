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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * This exception class is used as place holder for exceptions that are most likely not known by external callers. The
 * class carries the common exception properties as well as the original exception's fully qualified class name. The
 * stack trace of this class looks exactly the same as the stack trace of the replaced exception.
 */
public class PlaceholderException extends Exception {
  private static final long serialVersionUID = 1L;

  private final String m_origThrowableClassName;

  public PlaceholderException(Throwable t, Throwable cause) {
    super(t.getMessage(), cause);
    setStackTrace(t.getStackTrace());
    m_origThrowableClassName = t.getClass().getName();
  }

  @Override
  public String toString() {
    String s = m_origThrowableClassName;
    String message = getLocalizedMessage();
    return (message != null) ? (s + ": " + message) : s;
  }

  public String getOrigThrowableClassName() {
    return m_origThrowableClassName;
  }

  /**
   * Transforms the given throwable and its recursively referenced throwables into a new exception hierarchy. Exceptions
   * that are most likely not known by an external calling party are replaced by a {@link PlaceholderException} object:
   * <ul>
   * <li>{@link ProcessingException} are mapped to itself if possible, otherwise to a {@link ProcessingException}</li>
   * <li>Exceptions in package <code>java.*</code> are mapped to itself if possible, otherwise to a
   * {@link PlaceholderException}</li>
   * <li>All other exceptions are transformed to {@link PlaceholderException}</li>
   * </ul>
   *
   * @param t
   * @return
   */
  public static Throwable transformException(Throwable t) {
    // first, go to exception stack and reverse it
    List<Throwable> throwableStack = new ArrayList<>();
    while (t != null) {
      throwableStack.add(0, t);
      t = t.getCause();
    }

    // second, transform each exception so that they do not use probably
    // proprietary exception classes
    Throwable cause = null;
    for (Throwable throwable : throwableStack) {
      Throwable transformedThrowable = null;

      if (throwable instanceof ProcessingException) {
        ProcessingException pe = null;
        IProcessingStatus oldStatus = ((ProcessingException) throwable).getStatus();
        ProcessingStatus newStatus = (oldStatus instanceof ProcessingStatus ? (ProcessingStatus) oldStatus : new ProcessingStatus(oldStatus));
        newStatus.setException(cause);
        try {
          pe = (ProcessingException) transformWellKnownException(throwable, cause, newStatus);
        }
        catch (RuntimeException fatal) { // NOSONAR
          // nop
        }
        if (pe == null) {
          pe = new ProcessingException();
        }
        transformedThrowable = pe.withStatus(newStatus);
      }
      else if (throwable.getClass().getPackage() != null && throwable.getClass().getPackage().getName().startsWith("java.")) {
        transformedThrowable = transformWellKnownException(throwable, cause, null);
      }

      if (transformedThrowable == null) {
        transformedThrowable = new PlaceholderException(throwable, cause);
      }

      transformedThrowable.setStackTrace(throwable.getStackTrace());
      cause = transformedThrowable;
    }
    return cause;
  }

  /**
   * Creates a new instance of the given throwable using the given cause. The {@link IProcessingStatus} of the given
   * throwable is preserved if the given throwable is a {@link ProcessingException}.
   *
   * @param throwable
   * @param cause
   * @param processingStatus
   * @return
   */
  private static Throwable transformWellKnownException(Throwable throwable, Throwable cause, IProcessingStatus processingStatus) {
    Class<? extends Throwable> clazz = throwable.getClass();
    Throwable transformedThrowable = null;

    if (throwable instanceof ProcessingException) {
      // 0. IProcessingStatus
      try {
        Constructor<? extends Throwable> ctor = clazz.getConstructor(IProcessingStatus.class);
        if (ctor != null) {
          transformedThrowable = ctor.newInstance(processingStatus);
        }
      }
      catch (Exception e) { // NOSONAR
        // nop
      }
    }

    final String message;
    if (processingStatus != null && processingStatus.getBody() != null) {
      message = processingStatus.getBody();
    }
    else {
      message = throwable.getMessage();
    }

    // 1. String, Throwable constructor
    if (transformedThrowable == null) {
      try {
        Constructor<? extends Throwable> ctor = clazz.getConstructor(String.class, Throwable.class);
        if (ctor != null) {
          transformedThrowable = ctor.newInstance(message, cause);
        }
      }
      catch (Exception e) { // NOSONAR
        // nop
      }
    }

    // 2. String constructor
    if (transformedThrowable == null && cause == null) {
      try {
        Constructor<? extends Throwable> ctor = clazz.getConstructor(String.class);
        if (ctor != null) {
          transformedThrowable = ctor.newInstance(message);
        }
      }
      catch (Exception e) { // NOSONAR
        // nop
      }
    }

    // 3. Cause constructor
    if (transformedThrowable == null && message == null) {
      try {
        Constructor<? extends Throwable> ctor = clazz.getConstructor(Throwable.class);
        if (ctor != null) {
          transformedThrowable = ctor.newInstance(cause);
        }
      }
      catch (Exception e) { // NOSONAR
        // nop
      }
    }

    // 4. default constructor
    if (transformedThrowable == null && cause == null) {
      try {
        transformedThrowable = clazz.getConstructor().newInstance();
      }
      catch (Exception e) { // NOSONAR
        // nop
      }
    }
    return transformedThrowable;
  }
}
