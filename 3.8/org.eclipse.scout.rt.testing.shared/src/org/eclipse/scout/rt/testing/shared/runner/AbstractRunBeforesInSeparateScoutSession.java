/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.shared.runner;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Invokes all methods of a test class annotated with {@link BeforeClass} in a separate Scout session and therefore in
 * a separate Scout transaction.
 */
public abstract class AbstractRunBeforesInSeparateScoutSession extends Statement {
  private final Statement m_statement;
  private final Object m_target;
  private final List<FrameworkMethod> m_befores;

  public AbstractRunBeforesInSeparateScoutSession(Statement statement, List<FrameworkMethod> befores, Object target) {
    m_statement = statement;
    m_befores = befores;
    m_target = target;
  }

  @Override
  public void evaluate() throws Throwable {
    evaluateBeforesInScoutSession();
    m_statement.evaluate();
  }

  /**
   * Implementors are required to call the {@link #evaluateBefores()}.
   * 
   * @throws Throwable
   */
  protected abstract void evaluateBeforesInScoutSession() throws Throwable;

  /**
   * Invokes every method annotated with {@link BeforeClass} being part of a particular test class.
   * 
   * @throws Throwable
   */
  protected void evaluateBefores() throws Throwable {
    for (FrameworkMethod before : m_befores) {
      before.invokeExplosively(m_target);
    }
  }
}
