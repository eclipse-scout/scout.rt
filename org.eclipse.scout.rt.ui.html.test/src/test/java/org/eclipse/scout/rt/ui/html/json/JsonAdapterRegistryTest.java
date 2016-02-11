/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSession;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonDesktop;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.form.JsonForm;
import org.eclipse.scout.rt.ui.html.json.form.fixtures.FormWithOneField;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonAdapterRegistryTest {

  @Test
  public void testCreateDisposeInSameRequest() throws Exception {
    UiSession session = new UiSessionMock();
    IStringField model = new AbstractStringField() {
    };
    IJsonAdapter<?> adapter = session.getOrCreateJsonAdapter(model, null);

    // Note: Additionally, registry contains the "root adapter" and a context-menu
    assertEquals(3, UiSessionTestUtility.getJsonAdapterRegistry(session).size());
    assertEquals(2, session.currentJsonResponse().adapterMap().size());
    assertEquals(0, session.currentJsonResponse().eventList().size());

    model.setDisplayText("Test");
    assertEquals(3, UiSessionTestUtility.getJsonAdapterRegistry(session).size());
    assertEquals(2, session.currentJsonResponse().adapterMap().size());
    assertEquals(1, session.currentJsonResponse().eventList().size());

    adapter.dispose();
    assertEquals(1, UiSessionTestUtility.getJsonAdapterRegistry(session).size());
    assertEquals(0, session.currentJsonResponse().adapterMap().size());
    assertEquals(0, session.currentJsonResponse().eventList().size());
  }

  public static void testFormOpenedAndClosedInSameRequest(UiSession uiSession) throws JSONException {
    JsonDesktop<IDesktop> jsonDesktop = UiSessionTestUtility.newJsonAdapter(uiSession, uiSession.getClientSession().getDesktop(), null);
    FormWithOneField form = new FormWithOneField();

    form.start();
    JsonForm formAdapter = (JsonForm) jsonDesktop.getAdapter(form);
    assertNotNull(formAdapter);

    form.doClose();
    formAdapter = (JsonForm) jsonDesktop.getAdapter(form);
    assertNull(formAdapter);
    assertEquals(1, uiSession.currentJsonResponse().eventList().size());
    assertEquals(uiSession.getUiSessionId(), uiSession.currentJsonResponse().eventList().get(0).getTarget());
    assertEquals("disposeAdapter", uiSession.currentJsonResponse().eventList().get(0).getType());
    assertEquals(0, uiSession.currentJsonResponse().adapterMap().size());
  }

}
