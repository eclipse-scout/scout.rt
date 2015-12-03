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
package org.eclipse.scout.rt.testing.client.runner;

import org.eclipse.scout.rt.testing.client.runner.ClientTestRunnerDifferentSubjectTest.JUnitClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 5.1
 */
@Ignore("this is only a test fixture for the ClientTestRunnerTimeoutTest")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(JUnitClientSession.class)
@RunWithSubject("anna")
public class ClientTestRunnerTimeoutTestFixture {

  @Test(timeout = 500)
  public void testTimeoutNotExeeded() throws Exception {
  }

  @Test(timeout = 500, expected = IllegalStateException.class)
  public void testTimeoutNotExceededButExceptionExpected() throws Exception {
    throw new IllegalStateException();
  }

  @Test(timeout = 100)
  public void testTimeoutExceeded() throws Exception {
    Thread.sleep(500);
  }
}
