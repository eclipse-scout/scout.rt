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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.AbstractHybridAction;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.DisposeWidgetsHybridActionDo;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.DummyDo;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.DummyForm;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridActionContextElement;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridActionContextElements;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridActionType;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridEvent;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridManager;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDataObjectHelper;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.form.JsonForm;
import org.eclipse.scout.rt.ui.html.json.form.fields.groupbox.JsonGroupBox;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTree;
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

  @Before
  public void setUp() {
    m_uiSession = new UiSessionMock();
  }

  @After
  public void tearDown() {
    UiSessionTestUtility.getJsonAdapterRegistry(m_uiSession).disposeAdapters();
    getHybridManager().clear();
  }

  private HybridManager getHybridManager() {
    return m_uiSession.getClientSession().getDesktop().getAddOn(HybridManager.class);
  }

  private JsonHybridManager<HybridManager> createJsonHybridManager() {
    return UiSessionTestUtility.newJsonAdapter(m_uiSession, getHybridManager());
  }

  private JsonHybridManager<HybridManager> createJsonHybridManagerSpy() {
    m_uiSession.setSpyOnJsonAdapter(true);
    try {
      return createJsonHybridManager();
    }
    finally {
      m_uiSession.setSpyOnJsonAdapter(false);
    }
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
    JsonHybridManager<HybridManager> jsonHybridManager = createJsonHybridManagerSpy();
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
    assertNull(event.getData().optJSONObject("data"));
    JsonTestUtility.endRequest(m_uiSession);

    // ----------
    // actionEnd event

    String id2 = createId();
    IDoEntity data = BEANS.get(DoEntityBuilder.class).put("a", 123).put("b", "456").build();

    getHybridManager().fireHybridActionEndEvent(id2, data);

    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    event = m_uiSession.currentJsonResponse().getEventList().get(0);
    assertEquals("hybridEvent", event.getType());
    assertEquals(id2, event.getData().optString("id"));
    assertEquals("hybridActionEnd", event.getData().optString("eventType"));
    assertNotNull(event.getData().optJSONObject("data"));
    assertEquals(123, event.getData().getJSONObject("data").optInt("a"));
    assertEquals("456", event.getData().getJSONObject("data").optString("b"));
    JsonTestUtility.endRequest(m_uiSession);

    // ----------
    // Widget event

    String id3 = createId();

    getHybridManager().fireHybridWidgetEvent(id3, "bar");

    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    event = m_uiSession.currentJsonResponse().getEventList().get(0);
    assertEquals("hybridWidgetEvent", event.getType());
    assertEquals(id3, event.getData().optString("id"));
    assertEquals("bar", event.getData().optString("eventType"));
    assertNull(event.getData().optJSONObject("data"));
    JsonTestUtility.endRequest(m_uiSession);
  }

  @Test
  public void testOpenForm() {
    JsonHybridManager<HybridManager> jsonHybridManager = createJsonHybridManagerSpy();

    String id = createId();
    jsonHybridManager.handleUiEvent(createHybridActionJsonEvent(jsonHybridManager, id, "openForm:Dummy"));

    assertEquals(1, getHybridManager().getWidgets().size());
    IWidget widget = getHybridManager().getWidgetById(id);
    assertTrue(widget instanceof DummyForm);

    DummyForm form = (DummyForm) widget;
    assertNull(form.getDummyField().getValue());
    form.getDummyField().setValue(42);

    DummyDo dummyDo = BEANS.get(DummyDo.class).withDummy(42);

    form.doOk();
    verify(jsonHybridManager).handleModelHybridEvent(eq(HybridEvent.createHybridWidgetEvent(getHybridManager(), id, "save", dummyDo)));
    verify(jsonHybridManager).handleModelHybridEvent(eq(HybridEvent.createHybridWidgetEvent(getHybridManager(), id, "close")));

    assertEquals(0, getHybridManager().getWidgets().size());
  }

  @Test
  public void testOpenFormWithData() {
    JsonHybridManager<HybridManager> jsonHybridManager = createJsonHybridManagerSpy();

    DummyDo dummyDo = BEANS.get(DummyDo.class).withDummy(42);
    JSONObject dummyJson = BEANS.get(JsonDataObjectHelper.class).dataObjectToJson(dummyDo);

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
  }

  @Test
  public void testCreateAndDisposeHybridWidget() {
    JsonHybridManager<HybridManager> jsonHybridManager = createJsonHybridManager();

    String id = createId();
    jsonHybridManager.handleUiEvent(createHybridActionJsonEvent(jsonHybridManager, id, "createWidget:Dummy"));

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
    DisposeWidgetsHybridActionDo disposeDo = BEANS.get(DisposeWidgetsHybridActionDo.class)
        .withIds("dummy-widget-1");
    JSONObject disposeJson = BEANS.get(JsonDataObjectHelper.class).dataObjectToJson(disposeDo);
    jsonHybridManager.handleUiEvent(createHybridActionJsonEvent(jsonHybridManager, createId(), "DisposeWidgets", disposeJson));
    assertTrue(widget.isDisposeDone());
    assertFalse(widget2.isDisposeDone());

    // Disposing the widget will remove it from the hybrid manager and dispose its json adapter as well
    assertEquals(1, getHybridManager().getWidgets().size());
    assertTrue(jsonWidget.isDisposed());
    assertFalse(jsonWidget2.isDisposed());
    assertNull(m_uiSession.getJsonAdapter(widget, jsonHybridManager));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSendEventWithContextElements() {
    // Don't use the global HybridManager for this test. Two reasons:
    // 1) It might have other UI sessions attached to it (from other tests) that don't know about our JSON adapters.
    //    This can lead to errors like this: "java.lang.IllegalStateException: Adapter not found Form 2"
    // 2) It does not allow to dynamically register an event handler.
    HybridManager hybridManager = new HybridManager();
    JsonHybridManager jsonHybridManager = m_uiSession.getRootJsonAdapter().attachAdapter(hybridManager, null);

    IGroupBox groupBox = new AbstractGroupBox() {
    };
    IForm form = new AbstractForm() {
      @Override
      public IGroupBox getRootGroupBox() {
        return groupBox;
      }
    };
    ITree tree = new AbstractTree() {
    };
    ITreeNode treeNode = new AbstractTreeNode() {
    };
    tree.addChildNode(tree.getRootNode(), treeNode);

    // Create real adapters for the model objects, because for our tests, we need the hybrid manager to be able to
    // resolve adapter IDs via the root adapter. This would be possible with UiSessionTestUtility.newJsonAdapter().
    JsonForm jsonForm = m_uiSession.getRootJsonAdapter().attachAdapter(form, null);
    JsonGroupBox jsonGroupBox = m_uiSession.getJsonAdapter(groupBox, jsonForm);
    JsonTree jsonTree = m_uiSession.getRootJsonAdapter().attachAdapter(tree, null);

    JsonTestUtility.endRequest(m_uiSession);

    // -------------------

    String id1 = createId();
    hybridManager.fireHybridEvent(id1, "foo", BEANS.get(HybridActionContextElements.class)
        .withElement("form", form)
        .withElement("node", tree, treeNode));

    JSONObject jsonResponse1 = m_uiSession.currentJsonResponse().toJson();
    assertFalse(jsonResponse1.has("adapterData"));
    assertEquals(1, jsonResponse1.getJSONArray("events").length());

    JSONObject jsonEvent1 = jsonResponse1.getJSONArray("events").getJSONObject(0);
    assertEquals(jsonHybridManager.getId(), jsonEvent1.getString("target"));
    assertEquals("hybridEvent", jsonEvent1.getString("type"));
    assertEquals(id1, jsonEvent1.getString("id"));
    assertEquals("foo", jsonEvent1.getString("eventType"));

    JSONObject jsonContextElements1 = jsonEvent1.getJSONObject("contextElements");
    assertEquals(1, jsonContextElements1.getJSONArray("form").length());
    assertEquals(1, jsonContextElements1.getJSONArray("node").length());

    JSONObject jsonFormElement = jsonContextElements1.getJSONArray("form").getJSONObject(0);
    assertEquals(1, jsonFormElement.names().length());
    assertEquals("widget", jsonFormElement.names().get(0));
    assertEquals(jsonForm.getId(), jsonFormElement.getString("widget"));

    JSONObject jsonNodeElement = jsonContextElements1.getJSONArray("node").getJSONObject(0);
    assertEquals(2, jsonNodeElement.names().length());
    assertEquals("widget", jsonNodeElement.names().get(0));
    assertEquals("element", jsonNodeElement.names().get(1));
    assertEquals(jsonTree.getId(), jsonNodeElement.getString("widget"));
    assertEquals(jsonTree.getOrCreateNodeId(treeNode), jsonNodeElement.getString("element"));

    JsonTestUtility.endRequest(m_uiSession);

    // -------------------

    // Check that it also works for new widgets
    IStringField stringField = new AbstractStringField() {
    };
    form.getRootGroupBox().addField(stringField);

    IJsonAdapter jsonStringField = jsonGroupBox.getAdapter(stringField);

    String id2 = createId();
    hybridManager.fireHybridActionEndEvent(id2,
        BEANS.get(DoEntityBuilder.class)
            .put("done", true)
            .build(),
        BEANS.get(HybridActionContextElements.class)
            .withElement("field", stringField));

    JSONObject jsonResponse2 = m_uiSession.currentJsonResponse().toJson();

    assertEquals(1, jsonResponse2.getJSONObject("adapterData").length());
    assertEquals(jsonStringField.getId(), jsonResponse2.getJSONObject("adapterData").keys().next());

    assertEquals(2, jsonResponse2.getJSONArray("events").length());
    JSONObject jsonEvent21 = jsonResponse2.getJSONArray("events").getJSONObject(0);
    JSONObject jsonEvent22 = jsonResponse2.getJSONArray("events").getJSONObject(1);

    assertEquals(jsonGroupBox.getId(), jsonEvent21.getString("target"));

    assertEquals(jsonHybridManager.getId(), jsonEvent22.getString("target"));
    assertEquals("hybridEvent", jsonEvent22.getString("type"));
    assertEquals(id2, jsonEvent22.getString("id"));
    assertEquals("hybridActionEnd", jsonEvent22.getString("eventType"));

    assertTrue(jsonEvent22.getJSONObject("data").getBoolean("done"));
    JSONObject jsonContextElements2 = jsonEvent22.getJSONObject("contextElements");
    assertEquals(1, jsonContextElements2.getJSONArray("field").length());

    JSONObject jsonStringFieldElement = jsonContextElements2.getJSONArray("field").getJSONObject(0);
    assertEquals(1, jsonStringFieldElement.names().length());
    assertEquals("widget", jsonStringFieldElement.names().get(0));
    assertEquals(jsonStringField.getId(), jsonStringFieldElement.getString("widget"));

    JsonTestUtility.endRequest(m_uiSession);

    // -------------------

    // Check that context elements are not sent when not needed
    String id3 = createId();
    hybridManager.fireHybridActionEndEvent(id3,
        BEANS.get(DoEntityBuilder.class)
            .put("done", true)
            .build());

    JSONObject jsonResponse3 = m_uiSession.currentJsonResponse().toJson();

    assertFalse(jsonResponse3.has("adapterData"));
    assertEquals(1, jsonResponse3.getJSONArray("events").length());
    JSONObject jsonEvent3 = jsonResponse3.getJSONArray("events").getJSONObject(0);

    assertEquals(jsonHybridManager.getId(), jsonEvent3.getString("target"));
    assertEquals("hybridEvent", jsonEvent3.getString("type"));
    assertEquals(id3, jsonEvent3.getString("id"));
    assertEquals("hybridActionEnd", jsonEvent3.getString("eventType"));
    assertTrue(jsonEvent22.getJSONObject("data").getBoolean("done"));
    assertFalse(jsonEvent3.has("contextElements"));

    JsonTestUtility.endRequest(m_uiSession);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testReceiveEventWithContextElements() {
    // Don't use the global HybridManager for this test. Two reasons:
    // 1) It might have other UI sessions attached to it (from other tests) that don't know about our JSON adapters.
    //    This can lead to errors like this: "java.lang.IllegalStateException: Adapter not found Form 2"
    // 2) It does not allow to dynamically register an event handler.
    HybridManager hybridManager = new HybridManager();
    JsonHybridManager jsonHybridManager = m_uiSession.getRootJsonAdapter().attachAdapter(hybridManager, null);

    IGroupBox groupBox = new AbstractGroupBox() {
    };
    IForm form = new AbstractForm() {
      @Override
      public IGroupBox getRootGroupBox() {
        return groupBox;
      }
    };
    ITree tree = new AbstractTree() {
    };
    ITreeNode treeNode = new AbstractTreeNode() {
    };
    tree.addChildNode(tree.getRootNode(), treeNode);

    JsonForm jsonForm = m_uiSession.getRootJsonAdapter().attachAdapter(form, null);
    JsonTree jsonTree = m_uiSession.getRootJsonAdapter().attachAdapter(tree, null);

    JsonTestUtility.endRequest(m_uiSession);

    // -------------------

    String id = createId();
    JSONObject hybridActionEventData = new JSONObject(BEANS.get(DoEntityBuilder.class)
        .put("id", id)
        .put("actionType", "Foo")
        .put("data", BEANS.get(DoEntityBuilder.class)
            .put("someCustomData", 123)
            .build())
        .put("contextElements", BEANS.get(DoEntityBuilder.class)
            .put("form", List.of(BEANS.get(DoEntityBuilder.class)
                .put("widget", jsonForm.getId())
                .build()))
            .put("node", List.of(BEANS.get(DoEntityBuilder.class)
                .put("widget", jsonTree.getId())
                .put("element", jsonTree.getOrCreateNodeId(treeNode))
                .build()))
            .build())
        .buildString());
    JsonEvent jsonHybridActionEvent = new JsonEvent(jsonHybridManager.getId(), "hybridAction", hybridActionEventData);

    // -------------------

    // Register an observable action instance, so we can inspect the received data below
    P_FooHybridAction hybridAction = new P_FooHybridAction();
    IBean<Object> hybridActionBean = BEANS.getBeanManager().registerBean(
        new BeanMetaData(P_FooHybridAction.class)
            .withProducer(bean -> hybridAction));
    try {
      jsonHybridManager.handleUiEvent(jsonHybridActionEvent);
    }
    finally {
      BEANS.getBeanManager().unregisterBean(hybridActionBean);
    }

    // -------------------

    assertNotNull(hybridAction.m_data);
    assertEquals(new BigDecimal("123"), hybridAction.m_data.getDecimal("someCustomData"));

    assertFalse(hybridAction.getContextElements().isEmpty());

    assertEquals(1, hybridAction.getContextElements("form").size());
    assertSame(form, hybridAction.getContextElement("form").getWidget());
    assertNull(hybridAction.getContextElement("form").optElement());

    assertEquals(1, hybridAction.getContextElements("node").size());
    assertSame(tree, hybridAction.getContextElement("node").getWidget());
    assertSame(treeNode, hybridAction.getContextElement("node").getElement());

    assertNull(hybridAction.optContextElements("doesNotExist"));
    assertNull(hybridAction.optContextElement("doesNotExist"));

    assertThrows(AssertionException.class, () -> hybridAction.getContextElements("doesNotExist"));
    assertThrows(AssertionException.class, () -> hybridAction.getContextElement("doesNotExist"));
  }

  @IgnoreBean
  @HybridActionType("Foo")
  public static class P_FooHybridAction extends AbstractHybridAction<IDoEntity> {

    public IDoEntity m_data = null;

    @Override
    public void execute(IDoEntity data) {
      m_data = data;
    }

    @Override
    protected HybridActionContextElements getContextElements() {
      return super.getContextElements();
    }

    @Override
    public List<HybridActionContextElement> getContextElements(String key) {
      return super.getContextElements(key);
    }

    @Override
    protected List<HybridActionContextElement> optContextElements(String key) {
      return super.optContextElements(key);
    }

    @Override
    protected HybridActionContextElement getContextElement(String key) {
      return super.getContextElement(key);
    }

    @Override
    protected HybridActionContextElement optContextElement(String key) {
      return super.optContextElement(key);
    }
  }
}
