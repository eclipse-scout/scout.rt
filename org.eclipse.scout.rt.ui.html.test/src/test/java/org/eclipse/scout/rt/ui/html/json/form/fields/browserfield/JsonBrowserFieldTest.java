/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.browserfield;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.form.fields.browserfield.AbstractBrowserField;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonAdapterMock;
import org.eclipse.scout.rt.ui.html.json.form.fields.BaseFormFieldTest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testing: {@link AbstractBrowserField}
 */
@RunWith(PlatformTestRunner.class)
public class JsonBrowserFieldTest extends BaseFormFieldTest {

  private Object m_lastPostMessageData;
  private String m_lastPostMessageOrigin;

  private Boolean m_lastExternalWindowState;

  private AbstractBrowserField m_model = new AbstractBrowserField() {

    @Override
    protected void execPostMessage(Object data, String origin) {
      m_lastPostMessageData = data;
      m_lastPostMessageOrigin = origin;
    }

    @Override
    protected void execExternalWindowStateChanged(boolean windowState) {
      m_lastExternalWindowState = windowState;
    }
  };

  private JsonBrowserField<IBrowserField> m_browserField = new JsonBrowserField<>(m_model, m_session, m_session.createUniqueId(), new JsonAdapterMock());

  @Before
  public void setUp() {
    m_browserField.init();
  }

  @Test
  public void testToJson() throws JSONException {
    m_browserField.toJson();
  }

  @Test
  public void testPostMessage() throws JSONException {
    String origin = "https://eclipse.org/scout/";
    String data = "42";
    Map<String, String> map = new HashMap<>();
    map.put("origin", origin);
    map.put("data", data);
    m_browserField.handleUiEvent(new JsonEvent("xyz", "postMessage", new JSONObject(map)));
    Assert.assertEquals(data, m_lastPostMessageData);
    Assert.assertEquals(origin, m_lastPostMessageOrigin);
  }

  @Test
  public void testExternalWindowState() throws JSONException {
    boolean windowState = true;
    Map<String, String> map = new HashMap<>();
    map.put("windowState", Boolean.toString(windowState));
    m_browserField.handleUiEvent(new JsonEvent("xyz", "externalWindowStateChange", new JSONObject(map)));
    Assert.assertNotNull(m_lastExternalWindowState);
    Assert.assertEquals(true, m_lastExternalWindowState);
  }
}
