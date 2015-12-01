package org.eclipse.scout.rt.client;

import static org.junit.Assert.assertTrue;

import java.lang.ref.WeakReference;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.util.TestUtility;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ClientSessionTest {

  /**
   * Test might fail when manually debugged.
   */
  @Test
  public void testDispose() throws Exception {
    IClientSession session = BEANS.get(ClientSessionProvider.class).provide(ClientRunContexts.empty().withUserAgent(UserAgent.createDefault()));
    WeakReference<IClientSession> ref = new WeakReference<IClientSession>(session);

    session.stop();
    assertTrue(session.isStopping());
    session = null;
    TestUtility.assertGC(ref);
  }
}
