/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.platform.runner.statement;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.testing.platform.mock.BeanAnnotations;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Statement to initialize fields annotated with {@link BeanMock} or {@link Mock}
 */
public class BeanAnnotationsInitStatement extends Statement {

  protected final Statement m_next;
  private final Object m_target;

  /**
   * Creates a statement to register fields annotated with {@link BeanMock} or {@link Mock}.
   *
   * @param next
   *          next {@link Statement} to be executed.
   * @param target
   *          the target test object
   */
  public BeanAnnotationsInitStatement(final Statement next, final Object target) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
    m_target = Assertions.assertNotNull(target);
  }

  @Override
  public void evaluate() throws Throwable {
    BEANS.get(BeanAnnotations.class).init(m_target);
    MockitoAnnotations.initMocks(m_target);
    m_next.evaluate();
  }
}
