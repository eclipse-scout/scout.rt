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
package org.eclipse.scout.rt.server.job;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.transaction.TransactionScope;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ServerJobInputValidatorTest {

  @Test
  public void test() {
    new ServerJobInputValidator().validate(new JobInput().withRunContext(ServerRunContexts.empty().withSession(mock(IServerSession.class))));
    new ServerJobInputValidator().validate(new JobInput().withRunContext(ServerRunContexts.empty().withSession(null)));
    new ServerJobInputValidator().validate(new JobInput().withMutex(new Object()).withRunContext(ServerRunContexts.empty()));
    new ServerJobInputValidator().validate(new JobInput().withMutex(mock(IServerSession.class)).withRunContext(ServerRunContexts.empty()));
    assertTrue(true);
  }

  @Test(expected = AssertionException.class)
  public void testNullServerRunContext() {
    new ServerJobInputValidator().validate(new JobInput().withRunContext(null));
  }

  @Test(expected = AssertionException.class)
  public void testWrongRunContextType() {
    new ServerJobInputValidator().validate(new JobInput().withRunContext(RunContexts.empty()));
  }

  @Test(expected = AssertionException.class)
  public void testWrongTxScope1() {
    new ServerJobInputValidator().validate(new JobInput().withRunContext(ServerRunContexts.empty().withTransactionScope(TransactionScope.MANDATORY)));
  }

  @Test(expected = AssertionException.class)
  public void testWrongTxScope2() {
    new ServerJobInputValidator().validate(new JobInput().withRunContext(ServerRunContexts.empty().withTransactionScope(TransactionScope.REQUIRED)));
  }
}
