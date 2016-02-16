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
package org.eclipse.scout.rt.ui.html.json.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
  public void testFormOpenCloseInTwoRequests() throws Exception {
    // Create adapter for client session and desktop
    UiSessionTestUtility.newJsonAdapter(m_uiSession, IClientSession.CURRENT.get(), null);
    JsonTestUtility.processBufferedEvents(m_uiSession);
    JsonTestUtility.endRequest(m_uiSession);

    // ---------

    FormWithOneField form = new FormWithOneField();

    // --- 1 ---

    form.start();

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertFalse(JsonResponseTest.getAdapterData(m_uiSession.currentJsonResponse()).isEmpty());
    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    assertEquals("formShow", m_uiSession.currentJsonResponse().getEventList().get(0).getType());

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
  public void testFormOpenCloseInSameRequest() throws Exception {
    // Create adapter for client session and desktop
    UiSessionTestUtility.newJsonAdapter(m_uiSession, IClientSession.CURRENT.get(), null);
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
  public void testFormOpenCloseInWrappedFormField() throws Exception {
    // Create adapter for client session and desktop
    UiSessionTestUtility.newJsonAdapter(m_uiSession, IClientSession.CURRENT.get(), null);
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
}
