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

import java.util.function.Consumer;

/**
 * The {@link Consumer} interface of the Java standart lib does not support exceptions because of its signature. This
 * interface adds the possibility, to throw exceptions in the {@link #accept(T)} function.
 *
 * @param <T>
 *     type of the object, the consumer accepts
 */
@FunctionalInterface
public interface IThrowingConsumer<T> {

  /**
   * Accepts a value. The execution of this method may throw an exception.
   *
   * @param t
   *     value to accept
   * @throws Exception
   *     possible exception during execution
   */
  void accept(T t) throws Exception;
}
