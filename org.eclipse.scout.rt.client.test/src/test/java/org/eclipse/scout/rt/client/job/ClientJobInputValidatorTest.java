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
import static org.mockito.Mockito.mock;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.junit.Test;

public class ClientJobInputValidatorTest {

  @Test
  public void test1() {
    new ClientJobInputValidator().validate(new JobInput().runContext(new ClientRunContext().sessionRequired(false).session(null)));
    assertTrue(true);
  }

  @Test
  public void test2() {
    new ClientJobInputValidator().validate(new JobInput().runContext(new ClientRunContext().sessionRequired(true).session(mock(IClientSession.class))));
    assertTrue(true);
  }

  @Test
  public void test3() {
    new ClientJobInputValidator().validate(new JobInput().mutex(new Object()).runContext(new ClientRunContext().sessionRequired(true).session(mock(IClientSession.class))));
    assertTrue(true);
  }

  @Test(expected = AssertionException.class)
  public void testNullClientRunContext() {
    new ClientJobInputValidator().validate(new JobInput());
  }

  @Test(expected = AssertionException.class)
  public void testWrongRunContext() {
    new ClientJobInputValidator().validate(new JobInput().runContext(new RunContext()));
  }

  @Test(expected = AssertionException.class)
  public void testNullSession() {
    new ClientJobInputValidator().validate(new JobInput().runContext(new ClientRunContext().sessionRequired(true).session(null)));
  }

  @Test(expected = AssertionException.class)
  public void testSessionMutex() {
    new ClientJobInputValidator().validate(new JobInput().mutex(mock(IClientSession.class)).runContext(new ClientRunContext().sessionRequired(true).session(mock(IClientSession.class))));
  }
}
