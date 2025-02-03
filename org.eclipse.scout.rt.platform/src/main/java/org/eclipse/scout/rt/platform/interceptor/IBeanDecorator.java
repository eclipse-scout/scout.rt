/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.interceptor;

import org.eclipse.scout.rt.platform.IBeanDecorationFactory;

/**
 * Interface for bean decorations used in {@link IBeanDecorationFactory}.
 */
@FunctionalInterface
public interface IBeanDecorator<T> {

  /**
   * Callback executed when a method is called on a bean.
   *
   * @param context
   *     The context of the method call. Contains information about the method and the arguments being invoked.
   * @return the result of the call
   */
  Object invoke(IBeanInvocationContext<T> context);
}
