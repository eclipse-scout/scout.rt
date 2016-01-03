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
package org.eclipse.scout.rt.client.job;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ModelJobValidatorTest {

  public IClientSession m_clientSession;

  @Before
  public void before() {
    m_clientSession = mock(IClientSession.class);
    when(m_clientSession.getModelJobSemaphore()).thenReturn(Jobs.newExecutionSemaphore(1));
  }

  @Test
  public void test() {
    new ModelJobValidator().validateJobInput(new JobInput().withExecutionSemaphore(m_clientSession.getModelJobSemaphore()).withRunContext(ClientRunContexts.empty().withSession(m_clientSession, true)));
    assertTrue(true);
  }

  @Test(expected = AssertionException.class)
  public void testNullRunContext() {
    new ModelJobValidator().validateJobInput(new JobInput().withExecutionSemaphore(m_clientSession.getModelJobSemaphore()).withRunContext(null));
  }

  @Test(expected = AssertionException.class)
  public void testWrongRunContext() {
    new ModelJobValidator().validateJobInput(new JobInput().withExecutionSemaphore(m_clientSession.getModelJobSemaphore()).withRunContext(RunContexts.empty()));
  }

  @Test(expected = AssertionException.class)
  public void testNullMutex() {
    new ModelJobValidator().validateJobInput(new JobInput().withExecutionSemaphore(null).withRunContext(ClientRunContexts.empty().withSession(m_clientSession, false)));
  }

  @Test(expected = AssertionException.class)
  public void testWrongMutexType() {
    new ModelJobValidator().validateJobInput(new JobInput().withExecutionSemaphore(Jobs.newExecutionSemaphore(1)).withRunContext(ClientRunContexts.empty().withSession(m_clientSession, true)));
  }

  @Test(expected = AssertionException.class)
  public void testNullClientSession() {
    new ModelJobValidator().validateJobInput(new JobInput().withExecutionSemaphore(m_clientSession.getModelJobSemaphore()).withRunContext(ClientRunContexts.empty().withSession(null, false)));
  }
}
