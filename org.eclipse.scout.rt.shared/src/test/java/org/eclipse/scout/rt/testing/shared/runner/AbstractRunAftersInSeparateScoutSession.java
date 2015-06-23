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

import java.util.ArrayList;
import java.util.List;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

/**
 * Invokes all methods of a test class annotated with {@link org.junit.AfterClass} in a separate Scout session and
 * therefore in a separate Scout transaction.
 */
public abstract class AbstractRunAftersInSeparateScoutSession extends Statement {
  private final Statement m_statement;
  private final Object m_target;
  private final List<FrameworkMethod> m_afters;

  public AbstractRunAftersInSeparateScoutSession(Statement statement, List<FrameworkMethod> afters, Object target) {
    m_statement = statement;
    m_afters = afters;
    m_target = target;
  }

  @Override
  public void evaluate() throws Throwable {
    List<Throwable> exceptions = new ArrayList<Throwable>();
    try {
      m_statement.evaluate();
    }
    catch (Throwable e) {
      exceptions.add(e);
    }
    try {
      evaluateAftersInScoutSession();
    }
    catch (MultipleFailureException e) {
      exceptions.addAll(e.getFailures());
    }
    catch (Throwable t) {
      exceptions.add(t);
    }
    throwOnExceptions(exceptions);
  }

  /**
   * Implementors are required to call the {@link #evaluateAfters()}.
   * 
   * @throws Throwable
   */
  protected abstract void evaluateAftersInScoutSession() throws Throwable;

  /**
   * Invokes every method annotated with {@link org.junit.AfterClass} being part of a particular test class.
   * 
   * @throws Throwable
   */
  protected void evaluateAfters() throws Throwable {
    List<Throwable> exceptions = new ArrayList<Throwable>();
    for (FrameworkMethod after : m_afters) {
      try {
        after.invokeExplosively(m_target);
      }
      catch (Throwable e) {
        exceptions.add(e);
      }
    }
    throwOnExceptions(exceptions);
  }

  private static void throwOnExceptions(List<Throwable> errors) throws Throwable, MultipleFailureException {
    if (!errors.isEmpty()) {
      if (errors.size() == 1) {
        throw errors.get(0);
      }
      throw new MultipleFailureException(errors);
    }
  }
}
