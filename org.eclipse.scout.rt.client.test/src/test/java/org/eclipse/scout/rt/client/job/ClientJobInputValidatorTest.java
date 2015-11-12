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
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ClientJobInputValidatorTest {

  @Test
  public void test() {
    new ClientJobInputValidator().validate(new JobInput().withRunContext(ClientRunContexts.empty().withSession(mock(IClientSession.class), true)));
    new ClientJobInputValidator().validate(new JobInput().withRunContext(ClientRunContexts.empty().withSession(null, false)));
    new ClientJobInputValidator().validate(new JobInput().withMutex(new Object()).withRunContext(ClientRunContexts.empty()));
    new ClientJobInputValidator().validate(new JobInput().withMutex(mock(IClientSession.class)).withRunContext(ClientRunContexts.empty()));
    assertTrue(true);
  }

  @Test(expected = AssertionException.class)
  public void testNullClientRunContext() {
    new ClientJobInputValidator().validate(new JobInput().withRunContext(null));
  }

  @Test(expected = AssertionException.class)
  public void testWrongRunContextType() {
    new ClientJobInputValidator().validate(new JobInput().withRunContext(RunContexts.empty()));
  }
}
