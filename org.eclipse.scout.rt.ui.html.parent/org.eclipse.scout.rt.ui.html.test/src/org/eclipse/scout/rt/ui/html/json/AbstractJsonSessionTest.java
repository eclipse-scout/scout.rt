/**
 *
 */
package org.eclipse.scout.rt.ui.html.json;

import static org.junit.Assert.assertFalse;

import java.lang.ref.WeakReference;

import javax.servlet.http.HttpSessionBindingEvent;

import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonSession.P_ClientSessionCleanupHandler;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(ScoutClientTestRunner.class)
public class AbstractJsonSessionTest {

  @Test
  public void testDispose() {
    AbstractJsonSession object = (AbstractJsonSession) JsonTestUtility.createAndInitializeJsonSession();
    WeakReference<IJsonSession> ref = new WeakReference<IJsonSession>(object);

    object.dispose();
    object = null;
    JsonTestUtility.assertGC(ref);
  }

  @Test
  public void testSessionInvalidation() throws InterruptedException {
    AbstractJsonSession jsonSession = (AbstractJsonSession) JsonTestUtility.createAndInitializeJsonSession();
    IClientSession clientSession = jsonSession.getClientSession();

    //Don't waste time waiting for client jobs to finish. Test job itself runs inside a client job so we always have to wait until max time
    ((AbstractClientSession) clientSession).setMaxShutdownWaitTime(0);

    WeakReference<IJsonSession> ref = new WeakReference<IJsonSession>(jsonSession);

    HttpSessionBindingEvent mockEvent = Mockito.mock(HttpSessionBindingEvent.class);
    P_ClientSessionCleanupHandler dummyCleanupHandler = new AbstractJsonSession.P_ClientSessionCleanupHandler("1", clientSession);

    jsonSession.valueUnbound(mockEvent);
    dummyCleanupHandler.valueUnbound(mockEvent);
    synchronized (jsonSession.getClientSession().getStateLock()) {
      if (clientSession.isActive()) {
        jsonSession.getClientSession().getStateLock().wait(5000);
      }
    }
    assertFalse(clientSession.isActive());

    jsonSession = null;
    JsonTestUtility.assertGC(ref);
  }
}
