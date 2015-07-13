/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class ModelJobInputValidatorTest {

  @Mock
  public IClientSession m_clientSession;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void test() {
    new ModelJobInputValidator().validate(new JobInput().mutex(m_clientSession).runContext(ClientRunContexts.empty().withSession(m_clientSession, true)));
    assertTrue(true);
  }

  @Test(expected = AssertionException.class)
  public void testNullClientRunContext() {
    new ModelJobInputValidator().validate(new JobInput());
  }

  @Test(expected = AssertionException.class)
  public void testWrongRunContext() {
    new ModelJobInputValidator().validate(new JobInput().runContext(ClientRunContexts.empty()));
  }

  @Test(expected = AssertionException.class)
  public void testNullSession() {
    new ModelJobInputValidator().validate(new JobInput().mutex(null).runContext(ClientRunContexts.empty().withSession(null, true)));
  }

  @Test(expected = AssertionException.class)
  public void testWrongMutex() {
    new ModelJobInputValidator().validate(new JobInput().mutex(new Object()).runContext(ClientRunContexts.empty().withSession(m_clientSession, true)));
  }

  @Test(expected = AssertionException.class)
  public void testNullClientSession1() {
    new ModelJobInputValidator().validate(new JobInput().runContext(ClientRunContexts.empty()));
  }

  @Test(expected = AssertionException.class)
  public void testNullClientSession2() {
    new ModelJobInputValidator().validate(new JobInput().runContext(ClientRunContexts.empty().withSession(null, true)));
  }
}
