/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.tile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.holders.BooleanHolder;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class TileGridTest {

  @Test
  public void testAddTile() {
    P_TileGrid tileGrid = createTestTileGrid();
    P_Tile tile0 = createTestTile();
    P_Tile tile1 = createTestTile();
    P_Tile tile2 = createTestTile();
    assertTrue(tileGrid.getTiles().isEmpty());

    tileGrid.addTile(tile0);
    assertEquals(1, tileGrid.getTiles().size());
    assertEquals(tile0, tileGrid.getTiles().get(0));

    tileGrid.addTiles(Arrays.asList(tile1, tile2));
    assertEquals(3, tileGrid.getTiles().size());
    assertEquals(tile0, tileGrid.getTiles().get(0));
    assertEquals(tile1, tileGrid.getTiles().get(1));
    assertEquals(tile2, tileGrid.getTiles().get(2));
  }

  @Test
  public void testDeleteTile() {
    P_TileGrid tileGrid = createTestTileGrid();
    P_Tile tile0 = createTestTile();
    P_Tile tile1 = createTestTile();
    P_Tile tile2 = createTestTile();
    tileGrid.addTiles(Arrays.asList(tile0, tile1, tile2));
    assertEquals(3, tileGrid.getTiles().size());

    tileGrid.deleteTile(tile1);
    assertEquals(2, tileGrid.getTiles().size());
    assertEquals(tile0, tileGrid.getTiles().get(0));
    assertEquals(tile2, tileGrid.getTiles().get(1));

    tileGrid.deleteTiles(Arrays.asList(tile0, tile2));
    assertEquals(0, tileGrid.getTiles().size());
  }

  @Test
  public void testDeleteAllTiles() {
    P_TileGrid tileGrid = createTestTileGrid();
    P_Tile tile0 = createTestTile();
    P_Tile tile1 = createTestTile();
    P_Tile tile2 = createTestTile();
    tileGrid.addTiles(Arrays.asList(tile0, tile1, tile2));
    assertEquals(3, tileGrid.getTiles().size());

    tileGrid.deleteAllTiles();
    assertEquals(0, tileGrid.getTiles().size());
  }

  @Test
  public void testSetTiles() {
    P_TileGrid tileGrid = createTestTileGrid();
    P_Tile tile0 = createTestTile();
    P_Tile tile1 = createTestTile();
    P_Tile tile2 = createTestTile();
    P_Tile tile3 = createTestTile();
    tileGrid.setTiles(Arrays.asList(tile0, tile1));
    assertEquals(1, tile0.initCalls);
    assertEquals(0, tile0.disposeCalls);
    assertEquals(1, tile1.initCalls);
    assertEquals(0, tile1.disposeCalls);

    tileGrid.setTiles(Arrays.asList(tile2, tile3));
    assertEquals(1, tile0.initCalls);
    assertEquals(1, tile0.disposeCalls);
    assertEquals(1, tile1.initCalls);
    assertEquals(1, tile1.disposeCalls);
    assertEquals(1, tile2.initCalls);
    assertEquals(0, tile2.disposeCalls);
    assertEquals(1, tile3.initCalls);
    assertEquals(0, tile3.disposeCalls);

    // Set same elements again -> nothing should happen
    tileGrid.setTiles(Arrays.asList(tile2, tile3));
    assertEquals(1, tile0.initCalls);
    assertEquals(1, tile0.disposeCalls);
    assertEquals(1, tile1.initCalls);
    assertEquals(1, tile1.disposeCalls);
    assertEquals(1, tile2.initCalls);
    assertEquals(0, tile2.disposeCalls);
    assertEquals(1, tile3.initCalls);
    assertEquals(0, tile3.disposeCalls);
  }

  @Test
  public void testSetTiles_checkOrder() {
    P_TileGrid tileGrid = createTestTileGrid();
    P_Tile tile0 = createTestTile();
    P_Tile tile1 = createTestTile();
    P_Tile tile2 = createTestTile();

    tileGrid.addTiles(Arrays.asList(tile0, tile1, tile2));
    assertEquals(3, tileGrid.getTiles().size());
    assertEquals(tile0, tileGrid.getTiles().get(0));
    assertEquals(tile1, tileGrid.getTiles().get(1));
    assertEquals(tile2, tileGrid.getTiles().get(2));
    assertEquals(3, tileGrid.getFilteredTiles().size());
    assertEquals(tile0, tileGrid.getFilteredTiles().get(0));
    assertEquals(tile1, tileGrid.getFilteredTiles().get(1));
    assertEquals(tile2, tileGrid.getFilteredTiles().get(2));

    tileGrid.setTiles(Arrays.asList(tile2, tile1, tile0));
    assertEquals(3, tileGrid.getTiles().size());
    assertEquals(tile2, tileGrid.getTiles().get(0));
    assertEquals(tile1, tileGrid.getTiles().get(1));
    assertEquals(tile0, tileGrid.getTiles().get(2));
    assertEquals(3, tileGrid.getFilteredTiles().size());
    assertEquals(tile2, tileGrid.getFilteredTiles().get(0));
    assertEquals(tile1, tileGrid.getFilteredTiles().get(1));
    assertEquals(tile0, tileGrid.getFilteredTiles().get(2));
  }

  @Test
  public void testSetTiles_checkOrderWithFilter() {
    P_TileGrid tileGrid = createTestTileGrid();
    P_Tile tile0 = createTestTile();
    P_Tile tile1 = createTestTile();
    P_Tile tile2 = createTestTile();
    tileGrid.addTiles(Arrays.asList(tile0, tile1, tile2));

    ITileFilter<P_Tile> filter = (tile) -> tile != tile1; // accept tile0 and tile2
    tileGrid.addFilter(filter);
    assertEquals(3, tileGrid.getTiles().size());
    assertEquals(tile0, tileGrid.getTiles().get(0));
    assertEquals(tile1, tileGrid.getTiles().get(1));
    assertEquals(tile2, tileGrid.getTiles().get(2));
    assertEquals(2, tileGrid.getFilteredTiles().size());
    assertEquals(tile0, tileGrid.getFilteredTiles().get(0));
    assertEquals(tile2, tileGrid.getFilteredTiles().get(1));

    tileGrid.setTiles(Arrays.asList(tile2, tile1, tile0));
    assertEquals(3, tileGrid.getTiles().size());
    assertEquals(tile2, tileGrid.getTiles().get(0));
    assertEquals(tile1, tileGrid.getTiles().get(1));
    assertEquals(tile0, tileGrid.getTiles().get(2));
    assertEquals(2, tileGrid.getFilteredTiles().size());
    assertEquals(tile2, tileGrid.getFilteredTiles().get(0));
    assertEquals(tile0, tileGrid.getFilteredTiles().get(1));
  }

  @Test
  public void testSelectTiles() {
    P_TileGrid tileGrid = createTestTileGrid();
    tileGrid.setSelectable(true);
    tileGrid.setMultiSelect(false);
    P_Tile tile0 = createTestTile();
    P_Tile tile1 = createTestTile();
    P_Tile tile2 = createTestTile();
    tileGrid.addTiles(Arrays.asList(tile0, tile1, tile2));

    assertTrue(tileGrid.getSelectedTiles().isEmpty());
    tileGrid.selectTile(tile0);
    assertEquals(1, tileGrid.getSelectedTiles().size());
    assertEquals(tile0, tileGrid.getSelectedTile());

    tileGrid.selectTile(tile1);
    assertEquals(1, tileGrid.getSelectedTiles().size());
    assertEquals(tile1, tileGrid.getSelectedTile());

    tileGrid.selectTiles(Arrays.asList(tile0, tile1));
    assertEquals(1, tileGrid.getSelectedTiles().size());
    assertEquals(tile0, tileGrid.getSelectedTiles().get(0));
  }

  @Test
  public void testMultiSelectTiles() {
    P_TileGrid tileGrid = createTestTileGrid();
    tileGrid.setSelectable(true);
    tileGrid.setMultiSelect(true);
    P_Tile tile0 = createTestTile();
    P_Tile tile1 = createTestTile();
    P_Tile tile2 = createTestTile();
    tileGrid.addTiles(Arrays.asList(tile0, tile1, tile2));

    assertTrue(tileGrid.getSelectedTiles().isEmpty());
    tileGrid.selectTile(tile0);
    assertEquals(1, tileGrid.getSelectedTiles().size());
    assertEquals(tile0, tileGrid.getSelectedTile());

    tileGrid.selectTile(tile1);
    assertEquals(1, tileGrid.getSelectedTiles().size());
    assertEquals(tile1, tileGrid.getSelectedTile());

    tileGrid.selectTiles(Arrays.asList(tile0, tile1));
    assertEquals(2, tileGrid.getSelectedTiles().size());
    assertEquals(tile0, tileGrid.getSelectedTiles().get(0));
    assertEquals(tile1, tileGrid.getSelectedTiles().get(1));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSelectTilesEvent() {
    P_TileGrid tileGrid = createTestTileGrid();
    tileGrid.setSelectable(true);
    tileGrid.setMultiSelect(false);
    P_Tile tile0 = createTestTile();
    P_Tile tile1 = createTestTile();
    P_Tile tile2 = createTestTile();
    tileGrid.addTiles(Arrays.asList(tile0, tile1, tile2));

    final List<ITile> newSelection = new ArrayList<ITile>();
    tileGrid.addPropertyChangeListener(event -> {
      newSelection.addAll((List<ITile>) event.getNewValue());
    });
    tileGrid.selectTile(tile0);
    assertEquals(1, newSelection.size());
    assertEquals(tile0, newSelection.get(0));
  }

  @Test
  public void testSelectTiles_filtered() {
    P_TileGrid tileGrid = createTestTileGrid();
    tileGrid.setSelectable(true);
    P_Tile tile0 = createTestTile();
    P_Tile tile1 = createTestTile();
    P_Tile tile2 = createTestTile();
    tileGrid.addTiles(Arrays.asList(tile0, tile1, tile2));
    assertEquals(3, tileGrid.getTiles().size());
    assertEquals(3, tileGrid.getFilteredTiles().size());
    assertEquals(0, tileGrid.getSelectedTiles().size());

    // Only tile1 is visible -> only tile1 may be selected
    ITileFilter<P_Tile> filter = (tile) -> tile == tile1; // accept tile1
    tileGrid.addFilter(filter);
    tileGrid.selectTiles(Arrays.asList(tile0, tile1));
    assertEquals(3, tileGrid.getTiles().size());
    assertEquals(1, tileGrid.getFilteredTiles().size());
    assertEquals(1, tileGrid.getSelectedTiles().size());
    assertEquals(tile1, tileGrid.getFilteredTiles().get(0));
    assertEquals(tile1, tileGrid.getSelectedTiles().get(0));

    tileGrid.removeFilter(filter);
    tileGrid.selectTiles(Arrays.asList(tile0, tile1));
    assertEquals(3, tileGrid.getTiles().size());
    assertEquals(3, tileGrid.getFilteredTiles().size());
    assertEquals(2, tileGrid.getSelectedTiles().size());
    assertEquals(tile0, tileGrid.getSelectedTiles().get(0));
    assertEquals(tile1, tileGrid.getSelectedTiles().get(1));
  }

  @Test
  public void testDeselectTiles() {
    P_TileGrid tileGrid = createTestTileGrid();
    tileGrid.setSelectable(true);
    tileGrid.setMultiSelect(false);
    P_Tile tile0 = createTestTile();
    P_Tile tile1 = createTestTile();
    P_Tile tile2 = createTestTile();
    tileGrid.addTiles(Arrays.asList(tile0, tile1, tile2));

    assertTrue(tileGrid.getSelectedTiles().isEmpty());
    tileGrid.selectTile(tile0);
    assertEquals(1, tileGrid.getSelectedTiles().size());
    assertEquals(tile0, tileGrid.getSelectedTile());

    tileGrid.deselectTile(tile0);
    assertEquals(0, tileGrid.getSelectedTiles().size());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDeselectTilesEvent() {
    P_TileGrid tileGrid = createTestTileGrid();
    tileGrid.setSelectable(true);
    tileGrid.setMultiSelect(false);
    P_Tile tile0 = createTestTile();
    P_Tile tile1 = createTestTile();
    P_Tile tile2 = createTestTile();
    tileGrid.addTiles(Arrays.asList(tile0, tile1, tile2));
    tileGrid.selectTile(tile0);
    assertEquals(1, tileGrid.getSelectedTiles().size());
    assertEquals(tile0, tileGrid.getSelectedTile());

    final List<ITile> newSelection = new ArrayList<ITile>();
    BooleanHolder eventFired = new BooleanHolder(false);
    tileGrid.addPropertyChangeListener(event -> {
      newSelection.addAll((List<ITile>) event.getNewValue());
      eventFired.setValue(true);
    });
    tileGrid.deselectTile(tile0);
    assertEquals(0, newSelection.size());
    assertTrue(eventFired.getValue());
  }

  @Test
  public void testDeselectTilesWhenDeleted() {
    P_TileGrid tileGrid = createTestTileGrid();
    tileGrid.setSelectable(true);
    tileGrid.setMultiSelect(true);
    P_Tile tile0 = createTestTile();
    P_Tile tile1 = createTestTile();
    P_Tile tile2 = createTestTile();
    tileGrid.addTiles(Arrays.asList(tile0, tile1, tile2));

    assertTrue(tileGrid.getSelectedTiles().isEmpty());
    tileGrid.selectTiles(Arrays.asList(tile0, tile1));
    assertEquals(2, tileGrid.getSelectedTiles().size());
    assertEquals(tile0, tileGrid.getSelectedTile());
    assertEquals(tile0, tileGrid.getSelectedTiles().get(0));
    assertEquals(tile1, tileGrid.getSelectedTiles().get(1));

    tileGrid.deleteTile(tile0);
    assertEquals(1, tileGrid.getSelectedTiles().size());
    assertEquals(tile1, tileGrid.getSelectedTiles().get(0));
  }

  @Test
  public void testSelectAllTiles() {
    P_TileGrid tileGrid = createTestTileGrid();
    tileGrid.setSelectable(true);
    tileGrid.setMultiSelect(true);
    P_Tile tile0 = createTestTile();
    P_Tile tile1 = createTestTile();
    P_Tile tile2 = createTestTile();
    tileGrid.addTiles(Arrays.asList(tile0, tile1, tile2));

    assertTrue(tileGrid.getSelectedTiles().isEmpty());
    tileGrid.selectAllTiles();
    assertEquals(3, tileGrid.getSelectedTiles().size());
    assertEquals(tile0, tileGrid.getSelectedTiles().get(0));
    assertEquals(tile1, tileGrid.getSelectedTiles().get(1));
    assertEquals(tile2, tileGrid.getSelectedTiles().get(2));
  }

  @Test
  public void testDeselectAllTiles() {
    P_TileGrid tileGrid = createTestTileGrid();
    tileGrid.setSelectable(true);
    tileGrid.setMultiSelect(true);
    P_Tile tile0 = createTestTile();
    P_Tile tile1 = createTestTile();
    P_Tile tile2 = createTestTile();
    tileGrid.addTiles(Arrays.asList(tile0, tile1, tile2));

    assertTrue(tileGrid.getSelectedTiles().isEmpty());
    tileGrid.selectAllTiles();
    assertEquals(3, tileGrid.getSelectedTiles().size());

    tileGrid.deselectAllTiles();
    assertEquals(0, tileGrid.getSelectedTiles().size());
  }

  @Test
  public void testDeselectAllTilesWhenAllDeleted() {
    P_TileGrid tileGrid = createTestTileGrid();
    tileGrid.setSelectable(true);
    tileGrid.setMultiSelect(true);
    P_Tile tile0 = createTestTile();
    P_Tile tile1 = createTestTile();
    P_Tile tile2 = createTestTile();
    tileGrid.addTiles(Arrays.asList(tile0, tile1, tile2));

    assertTrue(tileGrid.getSelectedTiles().isEmpty());
    tileGrid.selectAllTiles();
    assertEquals(3, tileGrid.getSelectedTiles().size());

    tileGrid.deleteAllTiles();
    assertEquals(0, tileGrid.getTiles().size());
    assertEquals(0, tileGrid.getSelectedTiles().size());
  }

  @Test
  public void testFilterTiles() {
    P_TileGrid tileGrid = createTestTileGrid();
    P_Tile tile0 = createTestTile();
    P_Tile tile1 = createTestTile();
    P_Tile tile2 = createTestTile();
    tileGrid.addTiles(Arrays.asList(tile0, tile1, tile2));
    assertEquals(3, tileGrid.getTiles().size());
    assertEquals(3, tileGrid.getFilteredTiles().size());

    ITileFilter<P_Tile> filter1 = (tile) -> tile != tile1; // accept tile0 and tile2
    tileGrid.addFilter(filter1);
    assertEquals(3, tileGrid.getTiles().size());
    assertEquals(2, tileGrid.getFilteredTiles().size());
    assertEquals(tile0, tileGrid.getFilteredTiles().get(0));
    assertEquals(tile2, tileGrid.getFilteredTiles().get(1));
    assertEquals(true, tileGrid.getTiles().get(0).isFilterAccepted());
    assertEquals(false, tileGrid.getTiles().get(1).isFilterAccepted());
    assertEquals(true, tileGrid.getTiles().get(2).isFilterAccepted());

    ITileFilter<P_Tile> filter2 = (tile) -> tile != tile0; // accept tile1 and tile2
    tileGrid.addFilter(filter2);
    assertEquals(3, tileGrid.getTiles().size());
    assertEquals(1, tileGrid.getFilteredTiles().size());
    assertEquals(tile2, tileGrid.getFilteredTiles().get(0));
    assertEquals(false, tileGrid.getTiles().get(0).isFilterAccepted());
    assertEquals(false, tileGrid.getTiles().get(1).isFilterAccepted());
    assertEquals(true, tileGrid.getTiles().get(2).isFilterAccepted());

    tileGrid.removeFilter(filter1);
    assertEquals(3, tileGrid.getTiles().size());
    assertEquals(2, tileGrid.getFilteredTiles().size());
    assertEquals(tile1, tileGrid.getFilteredTiles().get(0));
    assertEquals(tile2, tileGrid.getFilteredTiles().get(1));
    assertEquals(false, tileGrid.getTiles().get(0).isFilterAccepted());
    assertEquals(true, tileGrid.getTiles().get(1).isFilterAccepted());
    assertEquals(true, tileGrid.getTiles().get(2).isFilterAccepted());

    tileGrid.removeFilter(filter2);
    assertEquals(3, tileGrid.getTiles().size());
    assertEquals(3, tileGrid.getFilteredTiles().size());
    assertEquals(tile0, tileGrid.getFilteredTiles().get(0));
    assertEquals(tile1, tileGrid.getFilteredTiles().get(1));
    assertEquals(tile2, tileGrid.getFilteredTiles().get(2));
    assertEquals(true, tileGrid.getTiles().get(0).isFilterAccepted());
    assertEquals(true, tileGrid.getTiles().get(1).isFilterAccepted());
    assertEquals(true, tileGrid.getTiles().get(2).isFilterAccepted());
  }

  @Test
  public void testFilterTiles_deselectTiles() {
    P_TileGrid tileGrid = createTestTileGrid();
    tileGrid.setSelectable(true);
    P_Tile tile0 = createTestTile();
    P_Tile tile1 = createTestTile();
    P_Tile tile2 = createTestTile();
    tileGrid.addTiles(Arrays.asList(tile0, tile1, tile2));
    tileGrid.selectTiles(Arrays.asList(tile0, tile1));
    assertEquals(3, tileGrid.getTiles().size());
    assertEquals(3, tileGrid.getFilteredTiles().size());
    assertEquals(2, tileGrid.getSelectedTiles().size());
    assertEquals(tile0, tileGrid.getSelectedTiles().get(0));
    assertEquals(tile1, tileGrid.getSelectedTiles().get(1));

    ITileFilter<P_Tile> filter = (tile) -> tile == tile1; // accept tile1
    tileGrid.addFilter(filter);
    assertEquals(3, tileGrid.getTiles().size());
    assertEquals(1, tileGrid.getFilteredTiles().size());
    assertEquals(1, tileGrid.getSelectedTiles().size());
    assertEquals(tile1, tileGrid.getSelectedTiles().get(0));
  }

  @Test
  public void testFilterTiles_insertTiles() {
    P_TileGrid tileGrid = createTestTileGrid();
    P_Tile tile0 = createTestTile();
    P_Tile tile1 = createTestTile();
    P_Tile tile2 = createTestTile();
    P_Tile tile3 = createTestTile();
    P_Tile tile4 = createTestTile();
    tileGrid.addTiles(Arrays.asList(tile0, tile1, tile2));
    assertEquals(3, tileGrid.getTiles().size());
    assertEquals(3, tileGrid.getFilteredTiles().size());

    ITileFilter<P_Tile> filter = (tile) -> tile == tile1 || tile == tile4; // accept tile1 and 4
    tileGrid.addFilter(filter);
    assertEquals(3, tileGrid.getTiles().size());
    assertEquals(1, tileGrid.getFilteredTiles().size());
    assertEquals(tile1, tileGrid.getFilteredTiles().get(0));

    // Insert tile 3 -> not accepted
    tileGrid.addTile(tile3);
    assertEquals(4, tileGrid.getTiles().size());
    assertEquals(1, tileGrid.getFilteredTiles().size());
    assertEquals(tile1, tileGrid.getFilteredTiles().get(0));

    // Insert tile 4 -> accepted
    tileGrid.addTile(tile4);
    assertEquals(5, tileGrid.getTiles().size());
    assertEquals(2, tileGrid.getFilteredTiles().size());
    assertEquals(tile1, tileGrid.getFilteredTiles().get(0));
    assertEquals(tile4, tileGrid.getFilteredTiles().get(1));
  }

  @Test
  public void testSortTiles() {
    P_TileGrid tileGrid = createTestTileGrid();
    P_Tile tile0 = createTestTile();
    tile0.text = "b";
    P_Tile tile1 = createTestTile();
    tile1.text = "d";
    P_Tile tile2 = createTestTile();
    tile2.text = "a";
    tileGrid.addTiles(Arrays.asList(tile0, tile1, tile2));
    assertEquals(tile0, tileGrid.getTiles().get(0));
    assertEquals(tile1, tileGrid.getTiles().get(1));
    assertEquals(tile2, tileGrid.getTiles().get(2));
    assertEquals(tile0, tileGrid.getFilteredTiles().get(0));
    assertEquals(tile1, tileGrid.getFilteredTiles().get(1));
    assertEquals(tile2, tileGrid.getFilteredTiles().get(2));

    tileGrid.setComparator(new P_Comparator());
    assertEquals(tile2, tileGrid.getTiles().get(0));
    assertEquals(tile0, tileGrid.getTiles().get(1));
    assertEquals(tile1, tileGrid.getTiles().get(2));
    assertEquals(tile2, tileGrid.getFilteredTiles().get(0));
    assertEquals(tile0, tileGrid.getFilteredTiles().get(1));
    assertEquals(tile1, tileGrid.getFilteredTiles().get(2));

    P_Tile tile3 = createTestTile();
    tile3.text = "c";
    tileGrid.addTile(tile3);
    assertEquals(tile2, tileGrid.getTiles().get(0));
    assertEquals(tile0, tileGrid.getTiles().get(1));
    assertEquals(tile3, tileGrid.getTiles().get(2));
    assertEquals(tile1, tileGrid.getTiles().get(3));
    assertEquals(tile2, tileGrid.getFilteredTiles().get(0));
    assertEquals(tile0, tileGrid.getFilteredTiles().get(1));
    assertEquals(tile3, tileGrid.getFilteredTiles().get(2));
    assertEquals(tile1, tileGrid.getFilteredTiles().get(3));
  }

  @Test
  public void testSortFilteredTiles() {
    P_TileGrid tileGrid = createTestTileGrid();
    P_Tile tile0 = createTestTile();
    tile0.text = "c";
    P_Tile tile1 = createTestTile();
    tile1.text = "d";
    P_Tile tile2 = createTestTile();
    tile2.text = "a";
    tileGrid.addTiles(Arrays.asList(tile0, tile1, tile2));
    assertEquals(tile0, tileGrid.getTiles().get(0));
    assertEquals(tile1, tileGrid.getTiles().get(1));
    assertEquals(tile2, tileGrid.getTiles().get(2));

    ITileFilter<P_Tile> filter = (tile) -> !((P_Tile) tile).text.equals("d"); // accept tile0, tile2 and tile3
    tileGrid.addFilter(filter);
    assertEquals(2, tileGrid.getFilteredTiles().size());
    assertEquals(tile0, tileGrid.getFilteredTiles().get(0));
    assertEquals(tile2, tileGrid.getFilteredTiles().get(1));

    tileGrid.setComparator(new P_Comparator());
    assertEquals(tile2, tileGrid.getTiles().get(0));
    assertEquals(tile0, tileGrid.getTiles().get(1));
    assertEquals(tile2, tileGrid.getFilteredTiles().get(0));
    assertEquals(tile0, tileGrid.getFilteredTiles().get(1));

    P_Tile tile3 = createTestTile();
    tile3.text = "b";
    tileGrid.addTile(tile3);
    assertEquals(tile2, tileGrid.getTiles().get(0));
    assertEquals(tile3, tileGrid.getTiles().get(1));
    assertEquals(tile0, tileGrid.getTiles().get(2));
    assertEquals(tile2, tileGrid.getFilteredTiles().get(0));
    assertEquals(tile3, tileGrid.getFilteredTiles().get(1));
    assertEquals(tile0, tileGrid.getFilteredTiles().get(2));
  }

  /**
   * Moves tiles from one grid to another
   */
  @Test
  public void test_ReuseTiles() {
    P_TileGrid tileGrid = createTestTileGrid();
    P_Tile tile0 = createTestTile();
    P_Tile tile1 = createTestTile();
    P_Tile tile2 = createTestTile();
    tileGrid.addTiles(Arrays.asList(tile0, tile1, tile2));
    assertEquals(tile0.getParent(), tileGrid);
    assertEquals(tile1.getParent(), tileGrid);
    assertEquals(tile2.getParent(), tileGrid);

    ITileFilter<P_Tile> filter1 = (tile) -> tile != tile1; // accept tile0 and tile2
    tileGrid.addFilter(filter1);
    assertEquals(3, tileGrid.getTiles().size());
    assertEquals(2, tileGrid.getFilteredTiles().size());
    assertEquals(tile0, tileGrid.getFilteredTiles().get(0));
    assertEquals(tile2, tileGrid.getFilteredTiles().get(1));
    assertEquals(true, tileGrid.getTiles().get(0).isFilterAccepted());
    assertEquals(false, tileGrid.getTiles().get(1).isFilterAccepted());
    assertEquals(true, tileGrid.getTiles().get(2).isFilterAccepted());

    // Delete tiles from grid and add them to another one
    tileGrid.deleteAllTiles();

    P_TileGrid tileGrid2 = createTestTileGrid();
    tileGrid2.setTiles(Arrays.asList(tile0, tile1, tile2));
    assertEquals(tile0.getParent(), tileGrid2);
    assertEquals(tile1.getParent(), tileGrid2);
    assertEquals(tile2.getParent(), tileGrid2);

    // Assert that filter works correctly
    tileGrid2.addFilter(filter1);
    assertEquals(3, tileGrid2.getTiles().size());
    assertEquals(2, tileGrid2.getFilteredTiles().size());
    assertEquals(tile0, tileGrid2.getFilteredTiles().get(0));
    assertEquals(tile2, tileGrid2.getFilteredTiles().get(1));
    assertEquals(true, tileGrid2.getTiles().get(0).isFilterAccepted());
    assertEquals(false, tileGrid2.getTiles().get(1).isFilterAccepted());
    assertEquals(true, tileGrid2.getTiles().get(2).isFilterAccepted());
  }

  /**
   * Creates an empty tiles element.
   */
  private P_TileGrid createTestTileGrid() {
    P_TileGrid tileGrid = new P_TileGrid();
    tileGrid.init();
    return tileGrid;
  }

  private P_Tile createTestTile() {
    return new P_Tile();
  }

  public static class P_TileGrid extends AbstractTileGrid<P_Tile> {

  }

  public static class P_Tile extends AbstractTile {
    public int initCalls = 0;
    public int disposeCalls = 0;
    public String text;

    @Override
    protected void execInitTile() {
      super.execInitTile();
      initCalls++;
    }

    @Override
    protected void execDisposeTile() {
      super.execDisposeTile();
      disposeCalls++;
    }

    @Override
    public String toString() {
      if (text != null) {
        return text;
      }
      return super.toString();
    }
  }

  private static class P_Comparator implements Comparator<P_Tile> {
    @Override
    public int compare(P_Tile tile1, P_Tile tile2) {
      return StringUtility.ALPHANUMERIC_COMPARATOR.compare(tile1.text, tile2.text);
    }
  }
}
