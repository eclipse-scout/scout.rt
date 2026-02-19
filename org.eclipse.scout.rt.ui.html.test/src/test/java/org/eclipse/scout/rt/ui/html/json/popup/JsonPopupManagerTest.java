/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.popup;

import static org.eclipse.scout.rt.platform.util.Assertions.assertInstance;
import static org.junit.Assert.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.popup.AbstractPopup;
import org.eclipse.scout.rt.client.ui.popup.PopupManager;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterUtility;
import org.eclipse.scout.rt.ui.html.json.JsonPropertyChangeEvent;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.json.JSONArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonPopupManagerTest {

  private UiSessionMock m_uiSession;

  @Before
  public void setUp() {
    m_uiSession = new UiSessionMock();
    JsonTestUtility.endRequest(m_uiSession);
  }

  @After
  public void tearDown() {
    JsonTestUtility.endRequest(m_uiSession);
    getPopupManager().getPopups().forEach(getPopupManager()::close);
  }

  private PopupManager getPopupManager() {
    return m_uiSession.getClientSession().getDesktop().getAddOn(PopupManager.class);
  }

  @Test
  public void testPropertyChangeEvent() {
    JsonPopupManager<PopupManager> jsonPopupManager = m_uiSession.createJsonAdapter(getPopupManager(), m_uiSession.getRootJsonAdapter());

    MyPopup popupWithoutAnchor = new MyPopup();

    MyPopup popupWithAnchorWithoutJsonAdapter = new MyPopup();
    popupWithAnchorWithoutJsonAdapter.setAnchor(new MyStringField());

    MyPopup popupWithAnchorWithJsonAdapter = new MyPopup();
    MyStringField anchor = new MyStringField();
    m_uiSession.createJsonAdapter(anchor, m_uiSession.getRootJsonAdapter());
    popupWithAnchorWithJsonAdapter.setAnchor(anchor);

    getPopupManager().open(popupWithoutAnchor);
    getPopupManager().open(popupWithAnchorWithoutJsonAdapter);
    getPopupManager().open(popupWithAnchorWithJsonAdapter);

    // property change event does not contain popups with anchors that do not have a json adapter
    assertEquals(Set.of(popupWithAnchorWithoutJsonAdapter.getAnchor()), jsonPopupManager.getAnchorsWithoutJsonAdapter());
    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    JsonPropertyChangeEvent event = assertInstance(m_uiSession.currentJsonResponse().getEventList().get(0), JsonPropertyChangeEvent.class);
    assertEquals("property", event.getType());
    assertEquals(jsonPopupManager.getId(), event.getTarget());
    assertEquals(1, event.getProperties().size());
    assertTrue(event.getProperties().containsKey(PopupManager.PROP_POPUPS));
    JSONArray jsonPopups = assertInstance(event.getProperties().get(PopupManager.PROP_POPUPS), JSONArray.class);
    assertEquals(2, jsonPopups.length());
    assertEquals(
        Set.of(
            JsonAdapterUtility.findChildAdapter(jsonPopupManager, popupWithoutAnchor).getId(),
            JsonAdapterUtility.findChildAdapter(jsonPopupManager, popupWithAnchorWithJsonAdapter).getId()
        ),
        IntStream.range(0, jsonPopups.length())
            .mapToObj(jsonPopups::getString)
            .collect(Collectors.toSet())
    );
  }

  @Test
  public void testPropertyChangeEventOnAnchorChanges() {
    JsonPopupManager<PopupManager> jsonPopupManager = m_uiSession.createJsonAdapter(getPopupManager(), m_uiSession.getRootJsonAdapter());

    // no anchor -> popup is part of property change event

    MyPopup popup = new MyPopup();
    getPopupManager().open(popup);

    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());
    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    JsonPropertyChangeEvent event = assertInstance(m_uiSession.currentJsonResponse().getEventList().get(0), JsonPropertyChangeEvent.class);
    assertEquals("property", event.getType());
    assertEquals(jsonPopupManager.getId(), event.getTarget());
    assertEquals(1, event.getProperties().size());
    assertTrue(event.getProperties().containsKey(PopupManager.PROP_POPUPS));
    JSONArray jsonPopups = assertInstance(event.getProperties().get(PopupManager.PROP_POPUPS), JSONArray.class);
    assertEquals(1, jsonPopups.length());
    assertEquals(JsonAdapterUtility.findChildAdapter(jsonPopupManager, popup).getId(), jsonPopups.getString(0));

    // anchor without json adapter -> popup is not part of property change event

    popup.setAnchor(new MyStringField());

    assertEquals(Set.of(popup.getAnchor()), jsonPopupManager.getAnchorsWithoutJsonAdapter());
    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    assertEquals(event, m_uiSession.currentJsonResponse().getEventList().get(0));
    jsonPopups = assertInstance(event.getProperties().get(PopupManager.PROP_POPUPS), JSONArray.class);
    assertEquals(0, jsonPopups.length());

    // anchor with json adapter -> popup is part of property change event

    MyStringField anchor = new MyStringField();
    m_uiSession.createJsonAdapter(anchor, m_uiSession.getRootJsonAdapter());
    popup.setAnchor(anchor);

    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());
    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    assertEquals(event, m_uiSession.currentJsonResponse().getEventList().get(0));
    jsonPopups = assertInstance(event.getProperties().get(PopupManager.PROP_POPUPS), JSONArray.class);
    assertEquals(1, jsonPopups.length());
    assertEquals(JsonAdapterUtility.findChildAdapter(jsonPopupManager, popup).getId(), jsonPopups.getString(0));

    // popup closed -> popup is not part of property change event, no matter what its anchor looks like

    getPopupManager().close(popup);

    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());
    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    assertEquals(event, m_uiSession.currentJsonResponse().getEventList().get(0));
    jsonPopups = assertInstance(event.getProperties().get(PopupManager.PROP_POPUPS), JSONArray.class);
    assertEquals(0, jsonPopups.length());

    popup.setAnchor(new MyStringField());

    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());
    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    assertEquals(event, m_uiSession.currentJsonResponse().getEventList().get(0));
    jsonPopups = assertInstance(event.getProperties().get(PopupManager.PROP_POPUPS), JSONArray.class);
    assertEquals(0, jsonPopups.length());

    popup.setAnchor(null);

    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());
    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    assertEquals(event, m_uiSession.currentJsonResponse().getEventList().get(0));
    jsonPopups = assertInstance(event.getProperties().get(PopupManager.PROP_POPUPS), JSONArray.class);
    assertEquals(0, jsonPopups.length());
  }

  @Test
  public void testPropertyChangeEventOnAnchorJsonAdapterChanges() {
    JsonPopupManager<PopupManager> jsonPopupManager = m_uiSession.createJsonAdapter(getPopupManager(), m_uiSession.getRootJsonAdapter());

    // anchor without json adapter -> popup is not part of property change event

    MyPopup popup = new MyPopup();
    MyStringField anchor = new MyStringField();
    popup.setAnchor(anchor);

    getPopupManager().open(popup);

    assertEquals(Set.of(popup.getAnchor()), jsonPopupManager.getAnchorsWithoutJsonAdapter());
    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    JsonPropertyChangeEvent event = assertInstance(m_uiSession.currentJsonResponse().getEventList().get(0), JsonPropertyChangeEvent.class);
    assertEquals("property", event.getType());
    assertEquals(jsonPopupManager.getId(), event.getTarget());
    assertEquals(1, event.getProperties().size());
    assertTrue(event.getProperties().containsKey(PopupManager.PROP_POPUPS));
    JSONArray jsonPopups = assertInstance(event.getProperties().get(PopupManager.PROP_POPUPS), JSONArray.class);
    assertEquals(0, jsonPopups.length());

    // anchor with json adapter -> popup is part of property change event

    IJsonAdapter<MyStringField> jsonAnchor = m_uiSession.createJsonAdapter(anchor, m_uiSession.getRootJsonAdapter());

    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());
    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    assertEquals(event, m_uiSession.currentJsonResponse().getEventList().get(0));
    jsonPopups = assertInstance(event.getProperties().get(PopupManager.PROP_POPUPS), JSONArray.class);
    assertEquals(1, jsonPopups.length());
    assertEquals(JsonAdapterUtility.findChildAdapter(jsonPopupManager, popup).getId(), jsonPopups.getString(0));

    // anchor without json adapter that was part of property change event -> popup is still part of property change event (no need to remove it as the browser does not try to render it again)

    jsonAnchor.dispose();

    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());
    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    assertEquals(event, m_uiSession.currentJsonResponse().getEventList().get(0));
    jsonPopups = assertInstance(event.getProperties().get(PopupManager.PROP_POPUPS), JSONArray.class);
    assertEquals(1, jsonPopups.length());
    assertEquals(JsonAdapterUtility.findChildAdapter(jsonPopupManager, popup).getId(), jsonPopups.getString(0));

    // popup closed -> popup is not part of property change event, no matter what its anchor looks like

    getPopupManager().close(popup);

    jsonAnchor = m_uiSession.createJsonAdapter(anchor, m_uiSession.getRootJsonAdapter());

    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());
    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    assertEquals(event, m_uiSession.currentJsonResponse().getEventList().get(0));
    jsonPopups = assertInstance(event.getProperties().get(PopupManager.PROP_POPUPS), JSONArray.class);
    assertEquals(0, jsonPopups.length());

    jsonAnchor.dispose();

    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());
    assertEquals(1, m_uiSession.currentJsonResponse().getEventList().size());
    assertEquals(event, m_uiSession.currentJsonResponse().getEventList().get(0));
    jsonPopups = assertInstance(event.getProperties().get(PopupManager.PROP_POPUPS), JSONArray.class);
    assertEquals(0, jsonPopups.length());
  }

  @Test
  public void testAnchorWithoutJsonAdapter() {
    JsonPopupManager<PopupManager> jsonPopupManager = m_uiSession.createJsonAdapter(getPopupManager(), m_uiSession.getRootJsonAdapter());

    MyPopup popup1 = new MyPopup();
    MyPopup popup2 = new MyPopup();
    MyPopup popup3 = new MyPopup();
    MyPopup popup4 = new MyPopup();

    MyStringField anchor1 = new MyStringField();
    MyStringField anchor2 = new MyStringField();

    IJsonAdapter<MyStringField> jsonAnchor1;
    IJsonAdapter<MyStringField> jsonAnchor2;

    // open and close a popup without an anchor
    getPopupManager().open(popup1);
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    getPopupManager().close(popup1);
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    // open and close a popup with an anchor without a json adapter
    popup1.setAnchor(anchor1);

    getPopupManager().open(popup1);
    assertEquals(Set.of(anchor1), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    getPopupManager().close(popup1);
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    popup1.setAnchor(null);

    // open and close a popup with an anchor with a json adapter
    popup1.setAnchor(anchor1);
    jsonAnchor1 = m_uiSession.createJsonAdapter(anchor1, m_uiSession.getRootJsonAdapter());

    getPopupManager().open(popup1);
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    getPopupManager().close(popup1);
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    jsonAnchor1.dispose();
    popup1.setAnchor(null);

    // open and close multiple popups without an anchor
    getPopupManager().open(popup1);
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    getPopupManager().open(popup2);
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    getPopupManager().close(popup1);
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    getPopupManager().close(popup2);
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    // open and close multiple popups with the same anchor without a json adapter
    popup1.setAnchor(anchor1);
    popup2.setAnchor(anchor1);

    getPopupManager().open(popup1);
    assertEquals(Set.of(anchor1), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    getPopupManager().open(popup2);
    assertEquals(Set.of(anchor1), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    getPopupManager().close(popup1);
    assertEquals(Set.of(anchor1), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    getPopupManager().close(popup2);
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    popup1.setAnchor(null);
    popup2.setAnchor(null);

    // open and close multiple popups with the same anchor with a json adapter
    popup1.setAnchor(anchor1);
    popup2.setAnchor(anchor1);
    jsonAnchor1 = m_uiSession.createJsonAdapter(anchor1, m_uiSession.getRootJsonAdapter());

    getPopupManager().open(popup1);
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    getPopupManager().open(popup2);
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    getPopupManager().close(popup1);
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    getPopupManager().close(popup2);
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    jsonAnchor1.dispose();
    popup1.setAnchor(null);
    popup2.setAnchor(null);

    // update the anchor of an open popup
    jsonAnchor1 = m_uiSession.createJsonAdapter(anchor1, m_uiSession.getRootJsonAdapter());

    getPopupManager().open(popup1);
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    popup1.setAnchor(anchor2);
    assertEquals(Set.of(anchor2), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    popup1.setAnchor(anchor1);
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    popup1.setAnchor(anchor2);
    assertEquals(Set.of(anchor2), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    popup1.setAnchor(null);
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    getPopupManager().close(popup1);
    jsonAnchor1.dispose();

    // update the anchors of multiple open popups
    jsonAnchor1 = m_uiSession.createJsonAdapter(anchor1, m_uiSession.getRootJsonAdapter());

    getPopupManager().open(popup1);
    getPopupManager().open(popup2);
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    popup1.setAnchor(anchor2);
    assertEquals(Set.of(anchor2), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    popup2.setAnchor(anchor2);
    assertEquals(Set.of(anchor2), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    popup1.setAnchor(anchor1);
    assertEquals(Set.of(anchor2), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    popup2.setAnchor(anchor1);
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    popup1.setAnchor(anchor2);
    assertEquals(Set.of(anchor2), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    popup2.setAnchor(anchor2);
    assertEquals(Set.of(anchor2), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    popup1.setAnchor(null);
    assertEquals(Set.of(anchor2), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    popup2.setAnchor(null);
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    getPopupManager().close(popup1);
    getPopupManager().close(popup2);
    jsonAnchor1.dispose();

    // create json adapters for multiple open popups
    popup1.setAnchor(anchor1);
    popup2.setAnchor(anchor2);
    popup3.setAnchor(anchor2);

    getPopupManager().open(popup1);
    getPopupManager().open(popup2);
    getPopupManager().open(popup3);
    getPopupManager().open(popup4);
    assertEquals(Set.of(anchor1, anchor2), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    jsonAnchor1 = m_uiSession.createJsonAdapter(anchor1, m_uiSession.getRootJsonAdapter());
    assertEquals(Set.of(anchor2), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    jsonAnchor2 = m_uiSession.createJsonAdapter(anchor2, m_uiSession.getRootJsonAdapter());
    assertEquals(Set.of(), jsonPopupManager.getAnchorsWithoutJsonAdapter());

    getPopupManager().close(popup1);
    getPopupManager().close(popup2);
    getPopupManager().close(popup3);
    getPopupManager().close(popup4);
    jsonAnchor1.dispose();
    jsonAnchor2.dispose();
  }

  protected static class MyStringField extends AbstractStringField {
  }

  protected static class MyPopup extends AbstractPopup {
  }
}
