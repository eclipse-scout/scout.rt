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
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.ActionFinder;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.menu.fixtures.Menu;
import org.eclipse.scout.rt.ui.html.json.menu.fixtures.MenuWithNonDisplayableChild;
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

    JsonMenu<IMenu> jsonMenu = m_uiSession.newJsonAdapter(menu, null);

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

}
