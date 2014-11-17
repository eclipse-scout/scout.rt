/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.server.runner;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.server.ServerJobFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;

/**
 * Test for {@link ScoutServerJobWrapperStatement}
 */
@RunWith(ScoutServerTestRunner.class)
public class ScoutServerJobWrapperStatementTest {

  @Test(expected = ProcessingException.class)
  public void testEvaluate1() throws Throwable {
    ScoutServerJobWrapperStatement statement = new ScoutServerJobWrapperStatement(new ServerJobFactory(ServerJob.getCurrentSession(), ServerJob.getCurrentSession().getSubject()), new Statement() {
      @Override
      public void evaluate() throws Throwable {
        throw new ProcessingException("TestException");
      }
    });
    statement.evaluate();
  }

  @Test(expected = Throwable.class)
  public void testEvaluate2() throws Throwable {
    ScoutServerJobWrapperStatement statement = new ScoutServerJobWrapperStatement(new ServerJobFactory(ServerJob.getCurrentSession(), ServerJob.getCurrentSession().getSubject()), new Statement() {
      @Override
      public void evaluate() throws Throwable {
        throw new Throwable("Test");
      }
    });
    statement.evaluate();
  }

  @Test(expected = VetoException.class)
  public void testEvaluate3() throws Exception {
    throw new VetoException("Test");
  }

  @Test(expected = ProcessingException.class)
  public void testEvaluate4() throws Exception {
    throw new ProcessingException("Test");
  }

}
