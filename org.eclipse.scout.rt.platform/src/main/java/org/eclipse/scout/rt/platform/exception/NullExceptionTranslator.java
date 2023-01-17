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

import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;

/**
 * Exception handler to return the {@link Throwable} as given.
 * <p>
 * Also, if given a wrapped exception like {@link UndeclaredThrowableException}, {@link InvocationTargetException} or
 * {@link ExecutionException}, that exception is returned as given without unwrapping its cause.
 * <p>
 * For instance, this translator can be used if working with the Job API, e.g. to distinguish between a
 * {@link FutureCancelledError} thrown by the job's runnable, or because the job was effectively cancelled.
 *
 * @since 5.2
 */
public class NullExceptionTranslator implements IExceptionTranslator<Throwable> {

  @Override
  public Throwable translate(final Throwable throwable) {
    return throwable;
  }
}
