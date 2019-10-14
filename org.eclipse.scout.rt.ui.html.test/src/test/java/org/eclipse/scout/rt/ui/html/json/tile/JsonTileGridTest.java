/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.tile;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.tile.AbstractTile;
import org.eclipse.scout.rt.client.ui.tile.AbstractTileGrid;
import org.eclipse.scout.rt.client.ui.tile.ITileGrid;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonPropertyChangeEvent;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonTileGridTest {
  private UiSessionMock m_uiSession;

  @Before
  public void setUp() {
    m_uiSession = new UiSessionMock();
  }

  /**
   * Tests whether complex property changes are sent correctly to the UI.
   */
  @Test
  public void testMultiplePropertyChanges() {
    P_TileGrid tileGrid = new P_TileGrid();
    tileGrid.setSelectable(true);

    JsonTileGrid<ITileGrid<?>> jsonTileGrid = UiSessionTestUtility.newJsonAdapter(m_uiSession, tileGrid, null);
    jsonTileGrid.toJson();

    P_Tile tile0 = new P_Tile();
    P_Tile tile1 = new P_Tile();
    P_Tile tile2 = new P_Tile();
    tileGrid.addTile(tile0);
    tileGrid.addTile(tile1);
    tileGrid.selectTiles(Arrays.asList(tile0, tile1));
    tileGrid.deleteTile(tile0);
    tileGrid.addTile(tile2);
    assertEquals(Arrays.asList(tile1, tile2), tileGrid.getTiles());
    assertEquals(1, tileGrid.getSelectedTileCount());
    assertEquals(Arrays.asList(tile1), tileGrid.getSelectedTiles());

    IJsonAdapter<? super P_Tile> jsonTile1 = m_uiSession.getJsonAdapter(tile1, jsonTileGrid);
    IJsonAdapter<? super P_Tile> jsonTile2 = m_uiSession.getJsonAdapter(tile2, jsonTileGrid);
    List<JsonPropertyChangeEvent> events = JsonTestUtility.extractPropertyChangeEvents(m_uiSession.currentJsonResponse(), jsonTileGrid.getId());
    JSONObject props = (JSONObject) events.get(0).toJson().get("properties");
    Iterator<String> keys = props.keys();

    // Assert that order is correct (if selected tiles was before tiles, the UI would have problems selecting the correct ones)
    assertEquals(ITileGrid.PROP_TILES, keys.next());
    assertEquals(ITileGrid.PROP_FILTERED_TILES, keys.next());
    assertEquals(ITileGrid.PROP_SELECTED_TILES, keys.next());

    // Assert that the correct model state is sent
    assertEquals(new JSONArray(Arrays.asList(jsonTile1.getId(), jsonTile2.getId())), props.getJSONArray(ITileGrid.PROP_TILES));
    assertTrue(props.isNull(ITileGrid.PROP_FILTERED_TILES));
    assertEquals(new JSONArray(Arrays.asList(jsonTile1.getId())), props.getJSONArray(ITileGrid.PROP_SELECTED_TILES));
  }

  @ClassId("a90dd588-9fa2-4972-be1e-d16750cf5031")
  private static class P_TileGrid extends AbstractTileGrid<P_Tile> {

  }

  @ClassId("3be001b5-a8f6-4ce1-a6cb-4b4b25be2d40")
  private static class P_Tile extends AbstractTile {

  }
}
