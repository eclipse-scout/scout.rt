/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.internal;

import org.eclipse.scout.rt.platform.chain.callable.ICallableDecorator;
import org.eclipse.scout.rt.platform.chain.callable.ICallableInterceptor;
import org.eclipse.scout.rt.platform.util.Assertions;

/**
 * This exception is used to mark exceptions through all {@link ICallableDecorator} and {@link ICallableInterceptor} as
 * handled by the {@link ExceptionProcessor}.
 */
public class CallableChainHandledException extends Exception {
  private static final long serialVersionUID = 1L;

  public CallableChainHandledException(Throwable original) {
    super(original.getMessage(), original);
    if (!(original instanceof Error || original instanceof Exception)) {
      Assertions.fail("{} is not an instance of Error or Exceptions, others are not allowed.", original);
    }
  }
}
