/**
 *
 */
package org.eclipse.scout.rt.ui.html.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.servlet.http.HttpSessionBindingEvent;

import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonSession.P_ClientSessionCleanupHandler;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractJsonSessionTest {

  @Test
  public void testDispose() throws Exception {
    AbstractJsonSession session = (AbstractJsonSession) JsonTestUtility.createAndInitializeJsonSession();
    WeakReference<IJsonSession> ref = new WeakReference<IJsonSession>(session);

    JsonTestUtility.endRequest(session);
    session.dispose();
    session = null;
    JsonTestUtility.assertGC(ref);
  }

  @Test
  public void testLogout() throws Exception {
    IJsonSession jsonSession = JsonTestUtility.createAndInitializeJsonSession();
    jsonSession.logout();

    Mockito.verify(jsonSession.currentHttpRequest().getSession()).invalidate();
    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(jsonSession.currentJsonResponse(), "logout");
    assertTrue(responseEvents.size() == 1);
  }

  @Test
  public void testSessionInvalidation() throws Exception {
    AbstractJsonSession jsonSession = (AbstractJsonSession) JsonTestUtility.createAndInitializeJsonSession();
    IClientSession clientSession = jsonSession.getClientSession();

    // Don't waste time waiting for client jobs to finish. Test job itself runs inside a client job so we always have to wait until max time
    ((AbstractClientSession) clientSession).setMaxShutdownWaitTime(0);
    WeakReference<IJsonSession> ref = new WeakReference<IJsonSession>(jsonSession);
    HttpSessionBindingEvent mockEvent = Mockito.mock(HttpSessionBindingEvent.class);
    P_ClientSessionCleanupHandler dummyCleanupHandler = new AbstractJsonSession.P_ClientSessionCleanupHandler("1", clientSession);

    JsonTestUtility.endRequest(jsonSession);
    jsonSession.valueUnbound(mockEvent);
    dummyCleanupHandler.valueUnbound(mockEvent);
    assertFalse(clientSession.isActive());

    jsonSession = null;
    JsonTestUtility.assertGC(ref);
  }

  @Test
  public void testCreateDisposeInSameRequest() throws Exception {
    AbstractJsonSession session = new JsonSessionMock();
    IStringField model = new AbstractStringField() {
    };
    IJsonAdapter<?> adapter = session.getOrCreateJsonAdapter(model, null);

    // Note: Additionally, registry contains the "root adapter" and a context-menu
    assertEquals(3, session.getJsonAdapterRegistry().getJsonAdapterCount());
    assertEquals(2, session.currentJsonResponse().adapterMap().size());
    assertEquals(0, session.currentJsonResponse().eventList().size());

    model.setDisplayText("Test");
    assertEquals(3, session.getJsonAdapterRegistry().getJsonAdapterCount());
    assertEquals(2, session.currentJsonResponse().adapterMap().size());
    assertEquals(1, session.currentJsonResponse().eventList().size());

    adapter.dispose();
    session.flush();
    assertEquals(1, session.getJsonAdapterRegistry().getJsonAdapterCount());
    assertEquals(0, session.currentJsonResponse().adapterMap().size());
    assertEquals(0, session.currentJsonResponse().eventList().size());
  }
}
