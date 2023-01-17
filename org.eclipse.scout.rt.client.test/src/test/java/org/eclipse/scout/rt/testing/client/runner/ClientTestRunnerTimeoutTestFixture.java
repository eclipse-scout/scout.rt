/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
  public void testTimeoutNotExeeded() {
  }

  @Test(timeout = 500, expected = IllegalStateException.class)
  public void testTimeoutNotExceededButExceptionExpected() {
    throw new IllegalStateException();
  }

  @Test(timeout = 100)
  public void testTimeoutExceeded() throws Exception {
    Thread.sleep(500);
  }
}
