/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
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
import org.eclipse.scout.rt.client.ui.desktop.hybrid.DummyDo;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.DummyForm;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridEvent;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridManager;
import org.eclipse.scout.rt.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.JsonDataObjectHelper;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.json.JSONObject;
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

  private JsonEvent createHybridActionJsonEvent(JsonHybridManager<HybridManager> jsonHybridManager, String id, String eventType) {
    return createHybridActionJsonEvent(jsonHybridManager, id, eventType, null);
  }

  private JsonEvent createHybridActionJsonEvent(JsonHybridManager<HybridManager> jsonHybridManager, String id, String eventType, JSONObject data) {
    JSONObject eventData = new JSONObject();
    eventData.put("id", id);
    eventData.put("eventType", eventType);
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
  public void testEventTypes() {
    JsonHybridManager<HybridManager> jsonHybridManager = createJsonHybridManager();
    jsonHybridManager.toJson();
    JsonTestUtility.endRequest(m_uiSession);

    // ----------
    // Generic event

    String id1 = createId();

    getHybridManager().fireHybridEvent(id1, "foo");

    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    JsonEvent event = m_uiSession.currentJsonResponse().getEventList().get(0);
    assertEquals("hybridEvent", event.getType());
    assertEquals(id1, event.getData().optString("id"));
    assertEquals("foo", event.getData().optString("eventType"));
    assertNull(event.getData().opt("data"));
    JsonTestUtility.endRequest(m_uiSession);

    // ----------
    // actionEnd event

    String id2 = createId();
    IDoEntity data = BEANS.get(DoEntityBuilder.class).put("a", 123).put("b", "456").build();
    JSONObject dataJson = new JSONObject();
    dataJson.put("a", 123);
    dataJson.put("b", "456");
    registerJsonDo(dataJson, data);

    getHybridManager().fireHybridActionEndEvent(id2, data);

    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    event = m_uiSession.currentJsonResponse().getEventList().get(0);
    assertEquals("hybridEvent", event.getType());
    assertEquals(id2, event.getData().optString("id"));
    assertEquals("hybridActionEnd", event.getData().optString("eventType"));
    assertEquals(dataJson, event.getData().opt("data"));
    JsonTestUtility.endRequest(m_uiSession);
    unregisterJsonDo(dataJson);

    // ----------
    // Widget event

    String id3 = createId();

    getHybridManager().fireHybridWidgetEvent(id3, "bar");

    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    event = m_uiSession.currentJsonResponse().getEventList().get(0);
    assertEquals("hybridWidgetEvent", event.getType());
    assertEquals(id3, event.getData().optString("id"));
    assertEquals("bar", event.getData().optString("eventType"));
    assertNull(event.getData().opt("data"));
    JsonTestUtility.endRequest(m_uiSession);
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
    verify(jsonHybridManager).handleModelHybridEvent(eq(HybridEvent.createHybridWidgetEvent(getHybridManager(), id, "reset")));

    form.doOk();
    verify(jsonHybridManager, never()).handleModelHybridEvent(eq(HybridEvent.createHybridWidgetEvent(getHybridManager(), id, "save", dummyDo)));
    verify(jsonHybridManager).handleModelHybridEvent(eq(HybridEvent.createHybridWidgetEvent(getHybridManager(), id, "close")));

    assertEquals(0, getHybridManager().getWidgets().size());

    unregisterJsonDo(dummyJson);
  }
}
