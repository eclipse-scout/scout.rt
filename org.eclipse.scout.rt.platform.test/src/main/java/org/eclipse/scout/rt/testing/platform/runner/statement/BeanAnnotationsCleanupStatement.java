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

/**
 * Statement to remove previously registered {@link BeanMock} fields.
 */
public class BeanAnnotationsCleanupStatement extends Statement {

  protected final Statement m_previous;

  /**
   * Creates a statement to remove previously registered {@link BeanMock} fields.
   *
   * @param previous
   *          previous {@link Statement} to be executed.
   */
  public BeanAnnotationsCleanupStatement(final Statement previous) {
    m_previous = Assertions.assertNotNull(previous, "next statement must not be null");
  }

  @Override
  public void evaluate() throws Throwable {
    m_previous.evaluate();
    BEANS.get(BeanAnnotations.class).clear();
  }
}
