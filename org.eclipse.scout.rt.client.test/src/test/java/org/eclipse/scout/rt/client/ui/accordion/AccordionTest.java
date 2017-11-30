/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.accordion;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.group.AbstractGroup;
import org.eclipse.scout.rt.client.ui.tile.AbstractTile;
import org.eclipse.scout.rt.client.ui.tile.AbstractTiles;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.client.ui.tile.ITiles;
import org.eclipse.scout.rt.platform.classid.ClassId;
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
    List<ITile> tiles = new ArrayList<>();
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
    List<ITile> tiles = new ArrayList<>();
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
    public ITiles getBody() {
      return (ITiles) super.getBody();
    }

    @ClassId("1af3bcc9-5cb0-486a-bb5a-6ef5dfc63230")
    public class Tiles extends AbstractTiles {

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
}
