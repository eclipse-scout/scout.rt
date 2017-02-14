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
package org.eclipse.scout.rt.ui.html.json.menu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.ActionFinder;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.menu.fixtures.Menu;
import org.eclipse.scout.rt.ui.html.json.menu.fixtures.MenuWithNonDisplayableChild;
import org.eclipse.scout.rt.ui.html.json.menu.fixtures.MenuWithNonDisplayableChild.DisplayableMenu;
import org.eclipse.scout.rt.ui.html.json.menu.fixtures.MenuWithNonDisplayableChild.NonDisplayableMenu;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonMenuTest {

  private UiSessionMock m_uiSession;

  @Before
  public void setUp() {
    m_uiSession = new UiSessionMock();
  }

  @Test
  public void testInitialVSPropertyChange() throws Exception {
    Menu menu = new Menu();
    menu.setText("foo");

    // when adapter has been created we have the complete adapter in the adapter-data section of the JSON response
    JsonMenu<IMenu> menuAdapter = m_uiSession.getOrCreateJsonAdapter(menu, null);
    JSONObject json = m_uiSession.currentJsonResponse().toJson();
    JSONObject adpaterData = JsonTestUtility.getAdapterData(json, menuAdapter.getId());
    assertEquals("foo", adpaterData.getString("text"));

    // Simulate processRequest, which resets the current JSON response. Otherwise the property change event
    // would be ignored because a new adapter exists in the current JSON response.
    JsonTestUtility.endRequest(m_uiSession);

    // when a property change occurs we assert an event is created for the adapter to update, containing only the changed property
    menu.setText("bar");
    json = m_uiSession.currentJsonResponse().toJson();
    JSONObject event = JsonTestUtility.getPropertyChange(json, 0);
    assertEquals("bar", event.getString("text"));
  }

  /**
   * Tests whether non displayable menus are sent.
   * <p>
   * This reduces response size and also leverages security because the menus are never visible to the user, not even
   * with the dev tools of the browser
   */
  @Test
  public void testDontSendNonDisplayableMenus() throws Exception {
    IMenu menu = new MenuWithNonDisplayableChild();
    ActionUtility.initActions(CollectionUtility.arrayList(menu));

    JsonMenu<IMenu> jsonMenu = UiSessionTestUtility.newJsonAdapter(m_uiSession, menu, null);

    JsonMenu<IMenu> jsonDisplayableMenu = jsonMenu.getAdapter(new ActionFinder().findAction(menu.getChildActions(), MenuWithNonDisplayableChild.DisplayableMenu.class));
    JsonMenu<IMenu> jsonNonDisplayableMenu = jsonMenu.getAdapter(new ActionFinder().findAction(menu.getChildActions(), MenuWithNonDisplayableChild.NonDisplayableMenu.class));

    // Adapter for NonDisplayableMenu must not exist
    assertNull(jsonNonDisplayableMenu);

    // Json response must not contain NonDisplayableMenu
    JSONObject json = jsonMenu.toJson();
    JSONArray jsonMenus = json.getJSONArray("childActions");
    assertEquals(1, jsonMenus.length());
    assertEquals(jsonDisplayableMenu.getId(), jsonMenus.get(0));
  }

  /**
   * Special case: Menu is attached, then it is set to visibleGranted=false, then sent to the UI, then a property change
   * event occurs on that menu. We expect, that the adapter is automatically disposed again when setVisibleGranted=false
   * is called, because the response is still writable.
   */
  @Test
  public void testDontSendNonDisplayableMenusSpecialCase() throws Exception {
    IMenu menu = new MenuWithNonDisplayableChild();
    DisplayableMenu displayableMenu = new ActionFinder().findAction(menu.getChildActions(), MenuWithNonDisplayableChild.DisplayableMenu.class);
    NonDisplayableMenu nonDisplayableMenu = new ActionFinder().findAction(menu.getChildActions(), MenuWithNonDisplayableChild.NonDisplayableMenu.class);

    JsonMenu<IMenu> jsonMenu = UiSessionTestUtility.newJsonAdapter(m_uiSession, menu, null);
    JsonMenu<IMenu> jsonDisplayableMenu = jsonMenu.getAdapter(displayableMenu);
    JsonMenu<IMenu> jsonNonDisplayableMenu = jsonMenu.getAdapter(nonDisplayableMenu);
    // Both adapters exist
    assertNotNull(jsonDisplayableMenu);
    assertNotNull(jsonNonDisplayableMenu);

    // After attachment of adapter!
    ActionUtility.initActions(CollectionUtility.arrayList(menu));
    jsonDisplayableMenu = jsonMenu.getAdapter(displayableMenu);
    jsonNonDisplayableMenu = jsonMenu.getAdapter(nonDisplayableMenu);
    // Now only one adapter exists anymore
    assertNotNull(jsonDisplayableMenu);
    assertNull(jsonNonDisplayableMenu);

    // Check that no traces of the invisible menu are in JSON
    JSONObject json = m_uiSession.currentJsonResponse().toJson();
    assertEquals(1, json.getJSONObject("adapterData").length());
    assertNotNull(json.getJSONObject("adapterData").getJSONObject(jsonDisplayableMenu.getId()));

    // --------
    // Property change on invisible menu -> must not trigger anything in the JSON layer
    JsonTestUtility.endRequest(m_uiSession);
    nonDisplayableMenu.setTooltipText("Test-Tooltip");
    json = m_uiSession.currentJsonResponse().toJson();
    assertFalse(json.has("adapterData"));
    assertFalse(json.has("events"));

    // Property change on visible menu -> triggers event
    JsonTestUtility.endRequest(m_uiSession);
    displayableMenu.setTooltipText("Test-Tooltip");
    json = m_uiSession.currentJsonResponse().toJson();
    assertFalse(json.has("adapterData"));
    assertEquals(1, json.getJSONArray("events").length());

    // --------------------------------
    // Test case where the menu is already sent to the UI, then setVisibleGranted=false (this is too late, but it will only trigger a warning in the log)
    JsonTestUtility.endRequest(m_uiSession);
    displayableMenu.setVisibleGranted(false);
    IJsonAdapter<?> jsonDisplayableMenu2 = jsonMenu.getAdapter(displayableMenu);
    assertSame(jsonDisplayableMenu, jsonDisplayableMenu2);
    json = m_uiSession.currentJsonResponse().toJson();
    assertFalse(json.has("adapterData"));
    assertEquals(1, json.getJSONArray("events").length());
  }
}
