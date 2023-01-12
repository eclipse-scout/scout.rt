/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.runner.statement;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.testing.platform.runner.SafeStatementInvoker;
import org.junit.runners.model.Statement;

/**
 * Statement to run on the given {@link RunContext}.
 *
 * @since 6.1
 */
public class RunContextStatement extends Statement {

  @FunctionalInterface
  public interface IRunContextProvider {
    RunContext create();
  }

  protected final Statement m_next;
  private final IRunContextProvider m_runContextProvider;

  public RunContextStatement(final Statement next, final IRunContextProvider runContextProvider) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
    m_runContextProvider = runContextProvider;
  }

  @Override
  public void evaluate() throws Throwable {
    final SafeStatementInvoker invoker = new SafeStatementInvoker(m_next);
    m_runContextProvider.create().run(invoker);
    invoker.throwOnError();
  }
}
