/**
 *
 */
package org.eclipse.scout.rt.ui.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;

import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSession.P_ClientSessionCleanupHandler;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterRegistry;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class UiSessionTest {

  @Test
  public void testDispose() throws Exception {
    UiSession session = (UiSession) JsonTestUtility.createAndInitializeUiSession();
    WeakReference<IUiSession> ref = new WeakReference<IUiSession>(session);

    JsonTestUtility.endRequest(session);
    session.dispose();
    assertTrue(session.isDisposed());
    session = null;
    JsonTestUtility.assertGC(ref);
  }

  @Test
  public void testLogout() throws Exception {
    IUiSession uiSession = JsonTestUtility.createAndInitializeUiSession();
    HttpSession httpSession = uiSession.currentHttpSession();

    uiSession.logout();
    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(uiSession.currentJsonResponse(), "logout");
    JsonTestUtility.endRequest(uiSession);

    Mockito.verify(httpSession).invalidate();
    assertEquals(1, responseEvents.size());
  }

  @Test
  public void testSessionInvalidation() throws Exception {
    UiSession uiSession = (UiSession) JsonTestUtility.createAndInitializeUiSession();
    IClientSession clientSession = uiSession.getClientSession();
    assertFalse(uiSession.isDisposed());

    // Don't waste time waiting for client jobs to finish. Test job itself runs inside a client job so we always have to wait until max time
    ((AbstractClientSession) clientSession).setMaxShutdownWaitTime(0);
    WeakReference<IUiSession> ref = new WeakReference<IUiSession>(uiSession);
    HttpSessionBindingEvent mockEvent = Mockito.mock(HttpSessionBindingEvent.class);
    P_ClientSessionCleanupHandler dummyCleanupHandler = new UiSession.P_ClientSessionCleanupHandler("1", clientSession);

    JsonTestUtility.endRequest(uiSession);
    uiSession.valueUnbound(mockEvent);
    dummyCleanupHandler.valueUnbound(mockEvent);
    JobUtility.awaitAllModelJobs(clientSession);
    assertFalse(clientSession.isActive());
    assertTrue(uiSession.isDisposed());

    uiSession = null;
    JsonTestUtility.assertGC(ref);
  }

  /**
   * helper method for unit tests to access protected method "getJsonAdapterRegistry"
   */
  public static JsonAdapterRegistry getJsonAdapterRegistry(UiSession session) {
    return session.getJsonAdapterRegistry();
  }
}
