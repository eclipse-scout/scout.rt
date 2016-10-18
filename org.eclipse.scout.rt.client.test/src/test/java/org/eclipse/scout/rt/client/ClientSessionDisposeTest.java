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
package org.eclipse.scout.rt.client;

import static org.junit.Assert.assertTrue;

import java.lang.ref.WeakReference;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.Test;

/*
 * This test must be executed by a bare JUnit runner.
 * Reason: The PlatformTestRunner and its sub classes keep track of every job scheduled during test execution and verify that they are completed. The list of scheduled jobs
 *         are referencing a JobInput which in turn references a RunContext and a session. The tests in this class will fail because they assert that the sessions are
 *         not referenced by any other object and therefore garbage collected.
 */
public class ClientSessionDisposeTest {

  /**
   * Test might fail when manually debugged.
   */
  @Test
  public void testDispose() throws Exception {
    IBean<?> bean = null;
    try {
      bean = TestingUtility.registerBean(new BeanMetaData(TestEnvironmentClientSession.class));

      IClientSession session = BEANS.get(ClientSessionProvider.class).provide(ClientRunContexts.empty().withUserAgent(UserAgents.createDefault()));
      WeakReference<IClientSession> ref = new WeakReference<IClientSession>(session);

      session.stop();
      assertTrue(session.isStopping());
      session = null;
      TestingUtility.assertGC(ref);
    }
    finally {
      if (bean != null) {
        TestingUtility.unregisterBean(bean);
      }
    }
  }
}
