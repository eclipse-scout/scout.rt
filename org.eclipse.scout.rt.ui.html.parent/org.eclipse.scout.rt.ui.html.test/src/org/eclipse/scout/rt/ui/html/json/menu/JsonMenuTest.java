/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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

import java.lang.reflect.Field;

import org.eclipse.scout.rt.ui.html.json.AbstractJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.eclipse.scout.rt.ui.html.json.menu.fixtures.Menu;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class JsonMenuTest {

  JsonSessionMock jsonSession = new JsonSessionMock();

  @Test
  public void testInitialVSPropertyChange() throws Exception {
    Menu menu = new Menu();
    menu.setText("foo");

    // when adapter has been created we have the complete adapter in the adapter-data section of the JSON response
    JsonMenu menuAdapter = jsonSession.getOrCreateJsonAdapter(menu);
    JSONObject json = jsonSession.currentJsonResponse().toJson();
    JSONObject adpaterData = JsonTestUtility.getAdapterData(json, menuAdapter.getId());
    assertEquals("foo", adpaterData.getString("text"));

    simulateProcessRequestOnJsonSession();

    // when a property change occurs we assert an event is created for the adapter to update, containing only the changed property
    menu.setText("bar");
    json = jsonSession.currentJsonResponse().toJson();
    JSONObject event = JsonTestUtility.getPropertyChange(json, 0);
    assertEquals("bar", event.getString("text"));
  }

  /**
   * Simulate processRequest, which resets the current JSON response. Otherwise the property change event
   * would be ignored because a new adapter exists in the current JSON response.
   */
  private void simulateProcessRequestOnJsonSession() throws Exception {
    Field field = AbstractJsonSession.class.getDeclaredField("m_currentJsonResponse");
    field.setAccessible(true);
    field.set(jsonSession, new JsonResponse());
  }

}
