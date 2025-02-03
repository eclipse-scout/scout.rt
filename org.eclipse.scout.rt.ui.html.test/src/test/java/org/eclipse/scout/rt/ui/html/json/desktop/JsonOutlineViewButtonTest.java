/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop;

import static org.junit.Assert.*;

import java.util.Collections;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineViewButton;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.OutlineViewButton;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.OutlineWithOneNode;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonOutlineViewButtonTest {

  private UiSessionMock m_uiSession;

  @Before
  public void setUp() {
    m_uiSession = new UiSessionMock();
  }

  @Test
  public void testLazyLoadingOutline_onModelSelectionChanged() throws JSONException {
    OutlineWithOneNode outline = new OutlineWithOneNode();
    IDesktop desktop = Mockito.mock(IDesktop.class);
    Mockito.when(desktop.getAvailableOutlines()).thenReturn(Collections.singletonList(outline));
    IOutlineViewButton button = new OutlineViewButton(desktop, outline.getClass());
    JsonOutlineViewButton<IOutlineViewButton> jsonViewButton = UiSessionTestUtility.newJsonAdapter(m_uiSession, button);
    assertNull(jsonViewButton.getAdapter(outline));

    button.setSelected(true);

    IJsonAdapter<?> outlineAdapter = jsonViewButton.getAdapter(outline);
    assertNotNull(outlineAdapter);
    String outlineId = JsonTestUtility.extractProperty(m_uiSession.currentJsonResponse(), jsonViewButton.getId(), "outline");
    assertEquals(outlineAdapter.getId(), outlineId);
  }

  @Test
  public void testLazyLoadingOutline_onUiSelectionChanged() {
    OutlineWithOneNode outline = new OutlineWithOneNode();
    IDesktop desktop = Mockito.mock(IDesktop.class);
    Mockito.when(desktop.getAvailableOutlines()).thenReturn(Collections.singletonList(outline));
    IOutlineViewButton button = new OutlineViewButton(desktop, outline.getClass());
    JsonOutlineViewButton<IOutlineViewButton> jsonViewButton = UiSessionTestUtility.newJsonAdapter(m_uiSession, button);
    assertNull(jsonViewButton.getAdapter(outline));

    JsonEvent event = createJsonActionEvent(jsonViewButton.getId());
    assertEquals("action", event.getType());
    jsonViewButton.handleUiEvent(event);

    // Outline needs to be created and sent if selection changes to true
    IJsonAdapter<?> outlineAdapter = jsonViewButton.getAdapter(outline);
    assertNotNull(outlineAdapter);
    String outlineId = JsonTestUtility.extractProperty(m_uiSession.currentJsonResponse(), jsonViewButton.getId(), "outline");
    assertEquals(outlineAdapter.getId(), outlineId);
  }

  @Test
  public void testNonLazyLoadingOutlineWhenSelected() throws JSONException {
    OutlineWithOneNode outline = new OutlineWithOneNode();
    IDesktop desktop = Mockito.mock(IDesktop.class);
    Mockito.when(desktop.getAvailableOutlines()).thenReturn(Collections.singletonList(outline));
    IOutlineViewButton button = new OutlineViewButton(desktop, outline.getClass());
    button.setSelected(true);
    JsonOutlineViewButton<IOutlineViewButton> jsonViewButton = UiSessionTestUtility.newJsonAdapter(m_uiSession, button);

    IJsonAdapter<?> outlineAdapter = jsonViewButton.getAdapter(outline);
    assertNotNull(outlineAdapter);

    // Expects outlineId is sent along with the button and not with a separate property change event
    String outlineId = JsonTestUtility.extractProperty(m_uiSession.currentJsonResponse(), jsonViewButton.getId(), "outline");
    assertNull(outlineId);
  }

  private static JsonEvent createJsonActionEvent(String adapterId) throws JSONException {
    return new JsonEvent(adapterId, "action", null);
  }
}
