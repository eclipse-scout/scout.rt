/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonResponseTest;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.form.fixtures.FormWithOneField;
import org.eclipse.scout.rt.ui.html.json.form.fixtures.FormWithWrappedFormField;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonFormTest {
  private UiSessionMock m_uiSession;

  @Before
  public void before() {
    m_uiSession = new UiSessionMock();
  }

  /**
   * Form disposal is controlled by the model and must not be triggered by the parent
   */
  @Test
  public void testFormDisposalOnClose() {
    FormWithOneField form = new FormWithOneField();
    UiSessionTestUtility.newJsonAdapter(m_uiSession, form, m_uiSession.getRootJsonAdapter());

    form.start();
    assertNotNull(m_uiSession.getJsonAdapter(form, m_uiSession.getRootJsonAdapter()));

    form.doClose();
    assertNull(m_uiSession.getJsonAdapter(form, m_uiSession.getRootJsonAdapter()));
  }

  /**
   * {@link IForm#requestFocus(org.eclipse.scout.rt.client.ui.form.fields.IFormField)} in
   * {@link AbstractFormHandler#execPostLoad()} must set <i>initialFocus</i> property.
   */
  @Test
  public void testRequestFocusInPostLoad() {
    FormWithOneField form = new FormWithOneField();
    IJsonAdapter<?> adapter = UiSessionTestUtility.newJsonAdapter(m_uiSession, form, m_uiSession.getRootJsonAdapter());
    form.start();
    assertNotNull(adapter.toJson().get(JsonForm.PROP_INITIAL_FOCUS));
    form.doClose();
  }

  @Test
  public void testFormOpenCloseInTwoRequests() {
    // Create adapter for client session and desktop
    UiSessionTestUtility.newJsonAdapter(m_uiSession, IClientSession.CURRENT.get());
    JsonTestUtility.processBufferedEvents(m_uiSession);
    JsonTestUtility.endRequest(m_uiSession);

    // ---------

    FormWithOneField form = new FormWithOneField();

    // --- 1 ---

    form.start();

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertFalse(JsonResponseTest.getAdapterData(m_uiSession.currentJsonResponse()).isEmpty());
    assertEquals(2, m_uiSession.currentJsonResponse().getEventList().size());
    assertEquals("requestFocus", m_uiSession.currentJsonResponse().getEventList().get(0).getType());
    assertEquals("formShow", m_uiSession.currentJsonResponse().getEventList().get(1).getType());

    JsonTestUtility.endRequest(m_uiSession);

    // --- 2 ---

    form.doClose();

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertTrue(JsonResponseTest.getAdapterData(m_uiSession.currentJsonResponse()).isEmpty());
    assertEquals(2, m_uiSession.currentJsonResponse().getEventList().size());
    assertEquals("formHide", m_uiSession.currentJsonResponse().getEventList().get(0).getType());
    assertEquals("disposeAdapter", m_uiSession.currentJsonResponse().getEventList().get(1).getType());
  }

  @Test
  public void testFormOpenCloseInSameRequest() {
    // Create adapter for client session and desktop
    UiSessionTestUtility.newJsonAdapter(m_uiSession, IClientSession.CURRENT.get());
    JsonTestUtility.processBufferedEvents(m_uiSession);
    JsonTestUtility.endRequest(m_uiSession);

    // ---------

    FormWithOneField form = new FormWithOneField();
    form.start();
    form.doClose();

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertTrue(JsonResponseTest.getAdapterData(m_uiSession.currentJsonResponse()).isEmpty());
    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    assertEquals("disposeAdapter", m_uiSession.currentJsonResponse().getEventList().get(0).getType());
  }

  @Test
  public void testFormOpenCloseInWrappedFormField() {
    // Create adapter for client session and desktop
    UiSessionTestUtility.newJsonAdapter(m_uiSession, IClientSession.CURRENT.get());
    JsonTestUtility.processBufferedEvents(m_uiSession);
    JsonTestUtility.endRequest(m_uiSession);

    // ---------

    FormWithWrappedFormField form = new FormWithWrappedFormField();
    form.start();
    form.doClose();

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertTrue(JsonResponseTest.getAdapterData(m_uiSession.currentJsonResponse()).isEmpty());
    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    assertEquals("disposeAdapter", m_uiSession.currentJsonResponse().getEventList().get(0).getType());
  }

  @Test
  public void testFormVisibleGrantedFalse() {
    // Create adapter for client session and desktop
    UiSessionTestUtility.newJsonAdapter(m_uiSession, IClientSession.CURRENT.get());
    JsonTestUtility.processBufferedEvents(m_uiSession);
    JsonTestUtility.endRequest(m_uiSession);

    // ---------

    FormWithOneField form = new FormWithOneField();

    IJsonAdapter<?> formAdapter = m_uiSession.getJsonAdapter(form, m_uiSession.getRootJsonAdapter());
    assertNull(formAdapter);

    // --- 1 ---

    // Assume the adapter was somehow attached (including the child adapters)
    formAdapter = m_uiSession.getOrCreateJsonAdapter(form, m_uiSession.getRootJsonAdapter());

    // Now set the field visibleGranted=false, then start it -> field should not be included in response
    form.getStringField().setVisibleGranted(false);
    form.start();

    formAdapter = m_uiSession.getJsonAdapter(form, null);
    assertEquals(1, m_uiSession.getJsonChildAdapters(formAdapter).size());
    IJsonAdapter<?> mainBoxAdapter = m_uiSession.getJsonChildAdapters(formAdapter).get(0);
    assertEquals(0, m_uiSession.getJsonChildAdapters(mainBoxAdapter).size()); // field not sent to UI

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertEquals(2, JsonResponseTest.getAdapterData(m_uiSession.currentJsonResponse()).size()); // form, mainbox
    assertEquals(3, m_uiSession.currentJsonResponse().getEventList().size());
    assertEquals("property", m_uiSession.currentJsonResponse().getEventList().get(0).getType()); // mainbox visible=false
    assertEquals("formActivate", m_uiSession.currentJsonResponse().getEventList().get(1).getType());
    assertEquals("formShow", m_uiSession.currentJsonResponse().getEventList().get(2).getType());
    // "requestFocus" event not sent

    JsonTestUtility.endRequest(m_uiSession);

    // --- 2 ---

    form.getStringField().setValue("test");

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size()); // event not sent to UI

    JsonTestUtility.endRequest(m_uiSession);

    // --- 3 ---

    form.doReset();

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertTrue(JsonResponseTest.getAdapterData(m_uiSession.currentJsonResponse()).isEmpty());
    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size()); // no event for invisible field

    // --- 4 ---

    form.doClose();

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertTrue(JsonResponseTest.getAdapterData(m_uiSession.currentJsonResponse()).isEmpty());
    assertEquals(2, m_uiSession.currentJsonResponse().getEventList().size());
    assertEquals("formHide", m_uiSession.currentJsonResponse().getEventList().get(0).getType());
    assertEquals("disposeAdapter", m_uiSession.currentJsonResponse().getEventList().get(1).getType());
  }
}
