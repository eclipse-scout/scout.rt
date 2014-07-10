/**
 *
 */
package org.eclipse.scout.rt.ui.html.json;

import java.lang.ref.WeakReference;

import javax.servlet.http.HttpSessionBindingEvent;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonSession.P_ClientSessionCleanupHandler;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
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
  public void testSessionInvalidation() {
    AbstractJsonSession jsonSession = (AbstractJsonSession) JsonTestUtility.createAndInitializeJsonSession();
    IClientSession clientSession = jsonSession.getClientSession();
    WeakReference<IJsonSession> ref = new WeakReference<IJsonSession>(jsonSession);

    HttpSessionBindingEvent mockEvent = Mockito.mock(HttpSessionBindingEvent.class);
    P_ClientSessionCleanupHandler dummyCleanupHandler = new AbstractJsonSession.P_ClientSessionCleanupHandler("1", clientSession);

    jsonSession.valueUnbound(mockEvent);
    dummyCleanupHandler.valueUnbound(mockEvent);
    Assert.assertFalse(clientSession.isActive());

    jsonSession = null;
    JsonTestUtility.assertGC(ref);
  }
}
