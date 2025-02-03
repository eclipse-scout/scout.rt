/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.context;

import org.eclipse.scout.rt.platform.chain.callable.ICallableInterceptor;

/**
 * A {@link IRunContextChainInterceptor} should always be produced of a {@link IRunContextChainInterceptorProducer} to
 * add a certain variable (e.g. ThreadLocal) to a {@link RunContext}.
 */
public interface IRunContextChainInterceptor<RESULT> extends ICallableInterceptor<RESULT> {

  /**
   * is called when from {@link RunContext#fillEmpty()} to apply default values to the current {@link RunContext}. This
   * method is called in the caller environment most likely a outer {@link RunContext}.
   */
  void fillEmtpy();

  /**
   * is called from {@link RunContext#fillCurrentValues()} to apply current environment variables to the current
   * {@link RunContext}. This method is called in the caller environment most likely a outer {@link RunContext}.
   */
  void fillCurrent();
}
