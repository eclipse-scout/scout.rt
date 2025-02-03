/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.accordion;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.group.AbstractGroup;
import org.eclipse.scout.rt.client.ui.tile.AbstractTile;
import org.eclipse.scout.rt.client.ui.tile.AbstractTileGrid;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AccordionTest {

  @Test
  public void testInitGroup() {
    P_Accordion accordion = new P_Accordion();
    List<P_Tile> tiles = new ArrayList<>();
    P_Tile tile = new P_Tile();
    assertEquals(0, tile.initCalls);
    tiles.add(tile);

    P_TileGroup group = new P_TileGroup();
    group.getBody().setTiles(tiles);
    assertEquals(1, tile.initCalls);
    assertEquals(0, group.initCalls);

    // Assert that tiles are not initialized again even though group will be initialized
    accordion.addGroup(group);
    assertEquals(1, tile.initCalls);
    assertEquals(1, group.initCalls);
  }

  @Test
  public void testDisposeGroup() {
    P_Accordion accordion = new P_Accordion();
    List<P_Tile> tiles = new ArrayList<>();
    P_Tile tile = new P_Tile();
    assertEquals(0, tile.initCalls);
    tiles.add(tile);

    P_TileGroup group = new P_TileGroup();
    group.getBody().setTiles(tiles);
    accordion.addGroup(group);
    assertEquals(0, tile.disposeCalls);
    assertEquals(0, group.disposeCalls);

    accordion.deleteGroup(group);
    assertEquals(1, tile.disposeCalls);
    assertEquals(1, group.disposeCalls);

    // Assert that tiles are not disposed again
    group.getBody().deleteAllTiles();
    assertEquals(1, tile.disposeCalls);
    assertEquals(1, group.disposeCalls);
  }

  @Test
  public void testSortGroups() {
    P_Accordion accordion = new P_Accordion();
    P_TileGroup group0 = new P_TileGroup();
    group0.setTitle("b");
    P_TileGroup group1 = new P_TileGroup();
    group1.setTitle("d");
    P_TileGroup group2 = new P_TileGroup();
    group2.setTitle("a");
    accordion.addGroups(Arrays.asList(group0, group1, group2));
    assertEquals(group0, accordion.getGroups().get(0));
    assertEquals(group1, accordion.getGroups().get(1));
    assertEquals(group2, accordion.getGroups().get(2));

    accordion.setComparator(new P_Comparator());
    assertEquals(group2, accordion.getGroups().get(0));
    assertEquals(group0, accordion.getGroups().get(1));
    assertEquals(group1, accordion.getGroups().get(2));

    P_TileGroup group3 = new P_TileGroup();
    group3.setTitle("c");
    accordion.addGroup(group3);
    assertEquals(group2, accordion.getGroups().get(0));
    assertEquals(group0, accordion.getGroups().get(1));
    assertEquals(group3, accordion.getGroups().get(2));
    assertEquals(group1, accordion.getGroups().get(3));
  }

  private static class P_Accordion extends AbstractAccordion {

  }

  private static class P_TileGroup extends AbstractGroup {
    public int initCalls = 0;
    public int disposeCalls = 0;

    @Override
    protected void execInitGroup() {
      super.execInitGroup();
      initCalls++;
    }

    @Override
    protected void execDisposeGroup() {
      super.execDisposeGroup();
      disposeCalls++;
    }

    @Override
    public TileGrid getBody() {
      return (TileGrid) super.getBody();
    }

    @ClassId("1af3bcc9-5cb0-486a-bb5a-6ef5dfc63230")
    public class TileGrid extends AbstractTileGrid<P_Tile> {

    }
  }

  private static class P_Tile extends AbstractTile {
    public int initCalls = 0;
    public int disposeCalls = 0;

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
  }

  private static class P_Comparator implements Comparator<P_TileGroup> {
    @Override
    public int compare(P_TileGroup group1, P_TileGroup group2) {
      return StringUtility.ALPHANUMERIC_COMPARATOR.compare(group1.getTitle(), group2.getTitle());
    }
  }
}
