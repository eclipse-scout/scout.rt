/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
