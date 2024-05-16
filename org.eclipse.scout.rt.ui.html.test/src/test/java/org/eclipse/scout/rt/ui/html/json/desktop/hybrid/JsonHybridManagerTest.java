/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop.hybrid;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.DisposeWidgetsHybridActionDo;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.DummyDo;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.DummyForm;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridEvent;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridManager;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDataObjectHelper;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonHybridManagerTest {

  private UiSessionMock m_uiSession;
  @BeanMock
  private JsonDataObjectHelper m_jsonDataObjectHelper;
  private final Map<JSONObject, IDoEntity> m_jsonDos = new HashMap<>();

  @Before
  public void setUp() {
    m_uiSession = new UiSessionMock();
    when(m_jsonDataObjectHelper.dataObjectToJson(isNull())).thenReturn(null);
    when(m_jsonDataObjectHelper.dataObjectToJson(any())).then(invocation -> m_jsonDos.entrySet().stream()
        .filter(entry -> ObjectUtility.equals(entry.getValue(), invocation.getArgument(0, IDoEntity.class)))
        .map(Entry::getKey)
        .findAny()
        .orElse(null));
    when(m_jsonDataObjectHelper.jsonToDataObject(isNull(), eq(IDoEntity.class))).thenReturn(null);
    when(m_jsonDataObjectHelper.jsonToDataObject(any(), eq(IDoEntity.class))).then(invocation -> m_jsonDos.get(invocation.getArgument(0, JSONObject.class)));
  }

  @After
  public void tearDown() {
    getHybridManager().clear();
  }

  private void registerJsonDo(JSONObject jsonObject, IDoEntity doEntity) {
    if (jsonObject == null || doEntity == null) {
      return;
    }

    m_jsonDos.put(jsonObject, doEntity);
  }

  private void unregisterJsonDo(JSONObject jsonObject) {
    if (jsonObject == null) {
      return;
    }
    m_jsonDos.remove(jsonObject);
  }

  private HybridManager getHybridManager() {
    return m_uiSession.getClientSession().getDesktop().getAddOn(HybridManager.class);
  }

  private JsonHybridManager<HybridManager> createJsonHybridManager() {
    m_uiSession.setSpyOnJsonAdapter(true);
    JsonHybridManager<HybridManager> jsonHybridManager = UiSessionTestUtility.newJsonAdapter(m_uiSession, getHybridManager());
    m_uiSession.setSpyOnJsonAdapter(false);
    when(jsonHybridManager.jsonDoHelper()).thenReturn(m_jsonDataObjectHelper);
    return jsonHybridManager;
  }

  private String createId() {
    return UUID.randomUUID().toString();
  }

  private JsonEvent createHybridActionJsonEvent(JsonHybridManager<HybridManager> jsonHybridManager, String id, String actionType) {
    return createHybridActionJsonEvent(jsonHybridManager, id, actionType, null);
  }

  private JsonEvent createHybridActionJsonEvent(JsonHybridManager<HybridManager> jsonHybridManager, String id, String actionType, JSONObject data) {
    JSONObject eventData = new JSONObject();
    eventData.put("id", id);
    eventData.put("actionType", actionType);
    if (data != null) {
      eventData.put("data", data);
    }
    return new JsonEvent(jsonHybridManager.getId(), "hybridAction", eventData);
  }

  @Test
  public void testPing() {
    JsonHybridManager<HybridManager> jsonHybridManager = createJsonHybridManager();
    String id = createId();
    jsonHybridManager.handleUiEvent(createHybridActionJsonEvent(jsonHybridManager, id, "Ping"));
    verify(jsonHybridManager).handleModelHybridEvent(eq(HybridEvent.createHybridActionEndEvent(getHybridManager(), id)));
  }

  @Test
  public void testOpenForm() {
    JsonHybridManager<HybridManager> jsonHybridManager = createJsonHybridManager();

    String id = createId();
    jsonHybridManager.handleUiEvent(createHybridActionJsonEvent(jsonHybridManager, id, "openForm:Dummy"));

    assertEquals(1, getHybridManager().getWidgets().size());
    IWidget widget = getHybridManager().getWidgetById(id);
    assertTrue(widget instanceof DummyForm);

    DummyForm form = (DummyForm) widget;
    assertNull(form.getDummyField().getValue());
    form.getDummyField().setValue(42);

    JSONObject dummyJson = new JSONObject();
    dummyJson.put("dummy", 42);
    DummyDo dummyDo = BEANS.get(DummyDo.class).withDummy(42);
    registerJsonDo(dummyJson, dummyDo);

    form.doOk();
    verify(jsonHybridManager).handleModelHybridEvent(eq(HybridEvent.createHybridWidgetEvent(getHybridManager(), id, "save", dummyDo)));
    verify(jsonHybridManager).handleModelHybridEvent(eq(HybridEvent.createHybridWidgetEvent(getHybridManager(), id, "close")));

    assertEquals(0, getHybridManager().getWidgets().size());

    unregisterJsonDo(dummyJson);
  }

  @Test
  public void testOpenFormWithData() {
    JsonHybridManager<HybridManager> jsonHybridManager = createJsonHybridManager();

    JSONObject dummyJson = new JSONObject();
    dummyJson.put("dummy", 42);
    DummyDo dummyDo = BEANS.get(DummyDo.class).withDummy(42);
    registerJsonDo(dummyJson, dummyDo);

    String id = createId();
    jsonHybridManager.handleUiEvent(createHybridActionJsonEvent(jsonHybridManager, id, "openForm:Dummy", dummyJson));

    assertEquals(1, getHybridManager().getWidgets().size());
    IWidget widget = getHybridManager().getWidgetById(id);
    assertTrue(widget instanceof DummyForm);

    DummyForm form = (DummyForm) widget;
    assertEquals((Integer) 42, form.getDummyField().getValue());
    form.getDummyField().setValue(null);

    form.doReset();
    verify(jsonHybridManager).handleModelHybridEvent(eq(HybridEvent.createHybridWidgetEvent(getHybridManager(), id, "reset", dummyDo)));

    form.doOk();
    verify(jsonHybridManager, never()).handleModelHybridEvent(eq(HybridEvent.createHybridWidgetEvent(getHybridManager(), id, "save", dummyDo)));
    verify(jsonHybridManager).handleModelHybridEvent(eq(HybridEvent.createHybridWidgetEvent(getHybridManager(), id, "close")));

    assertEquals(0, getHybridManager().getWidgets().size());

    unregisterJsonDo(dummyJson);
  }

  @Test
  public void testCreateAndDisposeHybridWidget() {
    JsonHybridManager<HybridManager> jsonHybridManager = createJsonHybridManager();

    String id = createId();
    jsonHybridManager.handleUiEvent(createHybridActionJsonEvent(jsonHybridManager, id, "createWidget:Dummy", new JSONObject()));

    // Create Widget Action created two widgets
    assertEquals(2, getHybridManager().getWidgets().size());
    IWidget widget = getHybridManager().getWidgetById("dummy-widget-1");
    assertTrue(widget instanceof ILabelField);
    IWidget widget2 = getHybridManager().getWidgetById("dummy-widget-2");
    assertTrue(widget instanceof ILabelField);

    // There are two json adapters created, one for each widget
    IJsonAdapter<IWidget> jsonWidget = m_uiSession.getJsonAdapter(widget, jsonHybridManager);
    assertNotNull(jsonWidget);
    IJsonAdapter<IWidget> jsonWidget2 = m_uiSession.getJsonAdapter(widget2, jsonHybridManager);
    assertNotNull(jsonWidget2);

    // Dispose Widget Action disposes one widget
    JSONObject disposeJson = new JSONObject();
    JSONArray jsonWidgets = new JSONArray();
    jsonWidgets.put("dummy-widget-");
    disposeJson.put("ids", jsonWidgets);
    DisposeWidgetsHybridActionDo disposeDo = BEANS.get(DisposeWidgetsHybridActionDo.class).withIds("dummy-widget-1");
    registerJsonDo(disposeJson, disposeDo);
    jsonHybridManager.handleUiEvent(createHybridActionJsonEvent(jsonHybridManager, createId(), "DisposeWidgets", disposeJson));
    assertTrue(widget.isDisposeDone());
    assertFalse(widget2.isDisposeDone());

    // Disposing the widget will remove it from the hybrid manager and dispose its json adapter as well
    assertEquals(1, getHybridManager().getWidgets().size());
    assertTrue(jsonWidget.isDisposed());
    assertFalse(jsonWidget2.isDisposed());
    assertNull(m_uiSession.getJsonAdapter(widget, jsonHybridManager));

    unregisterJsonDo(disposeJson);
  }
}
