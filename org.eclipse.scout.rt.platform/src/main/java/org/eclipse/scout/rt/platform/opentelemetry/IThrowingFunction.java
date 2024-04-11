/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.opentelemetry;

import java.util.function.Function;

/**
 * The {@link Function} interface of the Java standart lib does not support exceptions because of its signature. This
 * interface adds the possibility, to throw exceptions in the {@link #apply(T)} function.
 *
 * @param <T>
 *     type accpted by the fuction
 * @param <R>
 *     return type of the function
 */
@FunctionalInterface
public interface IThrowingFunction<T, R> {

  /**
   * Accepts a value and returns a result. The execution of this method may throw an exception.
   * @param t object the function accepts
   * @return the result of the function
   * @throws Exception possible exception during execution
   */
  R apply(T t) throws Exception;
}
