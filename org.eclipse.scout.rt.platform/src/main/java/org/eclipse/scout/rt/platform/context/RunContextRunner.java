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

import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain.Chain;
import org.eclipse.scout.rt.platform.chain.callable.ICallableInterceptor;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;

/**
 * Processor to run the subsequent sequence of actions on behalf of the given {@link RunContext}.
 * <p>
 * Instances of this class are to be added to a {@link CallableChain} to participate in the execution of a
 * {@link Callable}.
 *
 * @since 5.1
 */
public class RunContextRunner<RESULT> implements ICallableInterceptor<RESULT> {

  private final RunContext m_runContext;

  public RunContextRunner(final RunContext runContext) {
    m_runContext = runContext;
  }

  @Override
  public RESULT intercept(final Chain<RESULT> chain) throws Exception {
    return m_runContext.call(chain::continueChain, DefaultExceptionTranslator.class);
  }

  @Override
  public boolean isEnabled() {
    return m_runContext != null;
  }
}
