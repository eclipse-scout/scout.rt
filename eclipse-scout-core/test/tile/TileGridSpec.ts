/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {GridData, Group, InitModelOf, PlaceholderTile, RemoteTileFilter, scout, Tile, TileGrid, TileGridModel, TileModel} from '../../src/index';
import {JQueryTesting} from '../../src/testing/index';

describe('TileGrid', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  afterEach(() => {
    // Stop all running animations to not influence other specs
    $(':animated').finish();
  });

  function createTileGrid(numTiles?: number, model?: TileGridModel): TileGrid {
    let tiles = [];
    for (let i = 0; i < numTiles; i++) {
      tiles.push({
        objectType: Tile,
        label: 'Tile ' + i
      });
    }
    let defaults = {
      parent: session.desktop,
      tiles: tiles
    };
    model = $.extend({}, defaults, model);
    return scout.create(TileGrid, model as InitModelOf<TileGrid>);
  }

  function createTile(model?: TileModel): Tile {
    let defaults = {
      parent: session.desktop
    };
    model = $.extend({}, defaults, model);
    return scout.create(Tile, model as InitModelOf<Tile>);
  }

  describe('selectTiles', () => {
    it('selects the given tiles and unselects the previously selected ones', () => {
      let tileGrid = createTileGrid(3, {
        selectable: true
      });
      tileGrid.selectTiles(tileGrid.tiles[0]);
      expect(tileGrid.selectedTiles.length).toBe(1);
      expect(tileGrid.selectedTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.tiles[0].selected).toBe(true);
      expect(tileGrid.tiles[1].selected).toBe(false);
      expect(tileGrid.tiles[2].selected).toBe(false);

      tileGrid.selectTiles(tileGrid.tiles[1]);
      expect(tileGrid.selectedTiles.length).toBe(1);
      expect(tileGrid.selectedTiles[0]).toBe(tileGrid.tiles[1]);
      expect(tileGrid.tiles[0].selected).toBe(false);
      expect(tileGrid.tiles[1].selected).toBe(true);
      expect(tileGrid.tiles[2].selected).toBe(false);

      tileGrid.selectTiles([tileGrid.tiles[0], tileGrid.tiles[2]]);
      expect(tileGrid.selectedTiles.length).toBe(2);
      expect(tileGrid.selectedTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.selectedTiles[1]).toBe(tileGrid.tiles[2]);
      expect(tileGrid.tiles[0].selected).toBe(true);
      expect(tileGrid.tiles[1].selected).toBe(false);
      expect(tileGrid.tiles[2].selected).toBe(true);

      tileGrid.selectTiles([tileGrid.tiles[0], tileGrid.tiles[1], tileGrid.tiles[2]]);
      expect(tileGrid.selectedTiles.length).toBe(3);
      expect(tileGrid.selectedTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.selectedTiles[1]).toBe(tileGrid.tiles[1]);
      expect(tileGrid.selectedTiles[2]).toBe(tileGrid.tiles[2]);
      expect(tileGrid.tiles[0].selected).toBe(true);
      expect(tileGrid.tiles[1].selected).toBe(true);
      expect(tileGrid.tiles[2].selected).toBe(true);

      tileGrid.selectTiles([]);
      expect(tileGrid.selectedTiles.length).toBe(0);
      expect(tileGrid.tiles[0].selected).toBe(false);
      expect(tileGrid.tiles[1].selected).toBe(false);
      expect(tileGrid.tiles[2].selected).toBe(false);
    });

    it('does not select if selectable is false', () => {
      let tileGrid = createTileGrid(3, {
        selectable: false
      });
      tileGrid.selectTiles(tileGrid.tiles[0]);
      expect(tileGrid.tiles[0].selected).toBe(false);

      tileGrid.tiles[0].setSelected(true);
      expect(tileGrid.tiles[0].selected).toBe(false);
    });

    it('does not select tiles excluded by filter', () => {
      let tileGrid = createTileGrid(3, {
        selectable: true
      });

      let filter = {
        accept: tile => tile.label.indexOf('1') < 0
      };
      tileGrid.addFilter(filter);

      tileGrid.selectTiles(tileGrid.tiles[1]);
      expect(tileGrid.tiles[1].selected).toBe(false);

      tileGrid.selectTiles(tileGrid.tiles[0]);
      expect(tileGrid.tiles[0].selected).toBe(true);

      tileGrid.removeFilter(filter);

      tileGrid.selectTiles(tileGrid.tiles[1]);
      expect(tileGrid.tiles[1].selected).toBe(true);
    });

    it('does not select placeholder tiles', () => {
      let tileGrid = createTileGrid(3, {
        selectable: true,
        withPlaceholders: true
      });
      tileGrid.validateLogicalGrid();

      let tile = tileGrid.tiles[0];
      let placeholder = tileGrid.getFilteredTilesWithPlaceholders()[3];
      expect(placeholder).toBeInstanceOf(PlaceholderTile);

      tileGrid.selectTiles([tile, placeholder]);
      expect(tile.selected).toBe(true);
      expect(placeholder.selected).toBe(false);
      expect(tileGrid.selectedTiles).toEqual([tile]);
    });

    it('triggers a property change event', () => {
      let tileGrid = createTileGrid(3, {
        selectable: true
      });
      let eventTriggered = false;
      tileGrid.on('propertyChange', event => {
        if (event.propertyName === 'selectedTiles') {
          eventTriggered = true;
        }
      });
      tileGrid.selectTiles(tileGrid.tiles[0]);
      expect(eventTriggered).toBe(true);
    });
  });

  describe('deselectTiles', () => {
    it('deselects the given tiles', () => {
      let tileGrid = createTileGrid(3, {
        selectable: true
      });
      tileGrid.selectAllTiles();
      expect(tileGrid.selectedTiles.length).toBe(3);
      expect(tileGrid.selectedTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.selectedTiles[1]).toBe(tileGrid.tiles[1]);
      expect(tileGrid.selectedTiles[2]).toBe(tileGrid.tiles[2]);
      expect(tileGrid.tiles[0].selected).toBe(true);
      expect(tileGrid.tiles[1].selected).toBe(true);
      expect(tileGrid.tiles[2].selected).toBe(true);

      tileGrid.deselectTiles(tileGrid.tiles[1]);
      expect(tileGrid.selectedTiles.length).toBe(2);
      expect(tileGrid.selectedTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.selectedTiles[1]).toBe(tileGrid.tiles[2]);
      expect(tileGrid.tiles[0].selected).toBe(true);
      expect(tileGrid.tiles[1].selected).toBe(false);
      expect(tileGrid.tiles[2].selected).toBe(true);

      tileGrid.deselectAllTiles();
      expect(tileGrid.selectedTiles.length).toBe(0);
      expect(tileGrid.tiles[0].selected).toBe(false);
      expect(tileGrid.tiles[1].selected).toBe(false);
      expect(tileGrid.tiles[2].selected).toBe(false);
    });

    it('triggers a property change event', () => {
      let tileGrid = createTileGrid(3, {
        selectable: true
      });
      let eventTriggered = false;
      tileGrid.selectAllTiles();
      tileGrid.on('propertyChange', event => {
        if (event.propertyName === 'selectedTiles') {
          eventTriggered = true;
        }
      });
      tileGrid.deselectTile(tileGrid.tiles[0]);
      expect(eventTriggered).toBe(true);
    });
  });

  describe('insertTiles', () => {
    it('inserts the given tiles', () => {
      let tileGrid = createTileGrid(0);
      let tile0 = createTile();
      let tile1 = createTile();
      let tile2 = createTile();
      expect(tileGrid.tiles.length).toBe(0);

      tileGrid.insertTiles(tile0);
      expect(tileGrid.tiles.length).toBe(1);
      expect(tileGrid.tiles[0]).toBe(tile0);

      tileGrid.insertTiles([tile1, tile2]);
      expect(tileGrid.tiles.length).toBe(3);
      expect(tileGrid.tiles[0]).toBe(tile0);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile2);
    });

    it('triggers a property change event', () => {
      let tileGrid = createTileGrid(0);
      let tile0 = createTile();
      let eventTriggered = false;
      tileGrid.on('propertyChange', event => {
        if (event.propertyName === 'tiles') {
          eventTriggered = true;
        }
      });
      tileGrid.insertTiles(tile0);
      expect(eventTriggered).toBe(true);
    });

    it('links the inserted tiles with the tileGrid', () => {
      let tileGrid = createTileGrid(0);
      let tile0 = createTile();
      let tile1 = createTile();
      let tile2 = createTile();
      expect(tileGrid.tiles.length).toBe(0);
      expect(tile0.parent).toBe(session.desktop);
      expect(tile1.parent).toBe(session.desktop);
      expect(tile2.parent).toBe(session.desktop);

      tileGrid.insertTile(tile0);
      expect(tile0.parent).toBe(tileGrid);

      tileGrid.insertTiles([tile1, tile2]);
      expect(tile1.parent).toBe(tileGrid);
      expect(tile2.parent).toBe(tileGrid);
    });
  });

  describe('deleteTiles', () => {
    it('deletes the given tiles', () => {
      let tileGrid = createTileGrid(0);
      let tile0 = createTile();
      let tile1 = createTile();
      let tile2 = createTile();
      tileGrid.insertTiles([tile0, tile1, tile2]);
      expect(tileGrid.tiles.length).toBe(3);

      tileGrid.deleteTiles(tile1);
      expect(tileGrid.tiles.length).toBe(2);
      expect(tileGrid.tiles[0]).toBe(tile0);
      expect(tileGrid.tiles[1]).toBe(tile2);

      tileGrid.deleteTiles([tile0, tile2]);
      expect(tileGrid.tiles.length).toBe(0);
    });

    it('deselects the deleted tiles', () => {
      let tileGrid = createTileGrid(0, {
        selectable: true
      });
      let tile0 = createTile();
      let tile1 = createTile();
      let tile2 = createTile();
      tileGrid.insertTiles([tile0, tile1, tile2]);
      expect(tileGrid.tiles.length).toBe(3);

      tileGrid.selectAllTiles();
      expect(tileGrid.selectedTiles.length).toBe(3);

      tileGrid.deleteTiles(tile1);
      expect(tileGrid.selectedTiles.length).toBe(2);
      expect(tileGrid.selectedTiles[0]).toBe(tile0);
      expect(tileGrid.selectedTiles[1]).toBe(tile2);

      tileGrid.deleteTiles([tile0, tile2]);
      expect(tileGrid.selectedTiles.length).toBe(0);
    });

    it('triggers a property change event', () => {
      let tileGrid = createTileGrid(3);
      let eventTriggered = false;
      tileGrid.on('propertyChange', event => {
        if (event.propertyName === 'tiles') {
          eventTriggered = true;
        }
      });
      tileGrid.deleteTiles(tileGrid.tiles[0]);
      expect(eventTriggered).toBe(true);
    });

    it('destroys the deleted tiles', () => {
      let tileGrid = createTileGrid(0, {
        animateTileRemoval: false
      });
      let tile0 = createTile({
        parent: tileGrid
      });
      let tile1 = createTile({
        parent: tileGrid
      });
      let tile2 = createTile({
        parent: tileGrid
      });
      tileGrid.render();
      tileGrid.insertTiles([tile0, tile1, tile2]);
      expect(tile0.destroyed).toBe(false);
      expect(tile0.rendered).toBe(true);
      expect(tile1.destroyed).toBe(false);
      expect(tile1.rendered).toBe(true);
      expect(tile2.destroyed).toBe(false);
      expect(tile2.rendered).toBe(true);

      tileGrid.deleteTile(tile1);
      expect(tile1.destroyed).toBe(true);
      expect(tile1.rendered).toBe(false);

      tileGrid.deleteTiles([tile0, tile2]);
      expect(tile0.destroyed).toBe(true);
      expect(tile0.rendered).toBe(false);
      expect(tile2.destroyed).toBe(true);
      expect(tile2.rendered).toBe(false);
    });

    /**
     * This spec is important if a tile should be moved from one tileGrid to another.
     */
    it('does not destroy the deleted tiles if the tileGrid is not the owner', () => {
      let tileGrid = createTileGrid(0, {
        animateTileRemoval: false
      });
      let tile0 = createTile({
        owner: session.desktop
      });
      let tile1 = createTile({
        owner: session.desktop
      });
      let tile2 = createTile({
        owner: session.desktop
      });
      tileGrid.render();
      tileGrid.insertTiles([tile0, tile1, tile2]);
      expect(tile0.destroyed).toBe(false);
      expect(tile0.rendered).toBe(true);
      expect(tile1.destroyed).toBe(false);
      expect(tile1.rendered).toBe(true);
      expect(tile2.destroyed).toBe(false);
      expect(tile2.rendered).toBe(true);

      tileGrid.deleteTile(tile1);
      expect(tile1.destroyed).toBe(false);
      expect(tile1.rendered).toBe(false);

      tileGrid.deleteTiles([tile0, tile2]);
      expect(tile0.destroyed).toBe(false);
      expect(tile0.rendered).toBe(false);
      expect(tile2.destroyed).toBe(false);
      expect(tile2.rendered).toBe(false);
    });
  });

  describe('deleteAllTiles', () => {
    it('deletes all tiles', () => {
      let tileGrid = createTileGrid(0);
      let tile0 = createTile();
      let tile1 = createTile();
      let tile2 = createTile();
      tileGrid.insertTiles([tile0, tile1, tile2]);
      expect(tileGrid.tiles.length).toBe(3);

      tileGrid.deleteAllTiles();
      expect(tileGrid.tiles.length).toBe(0);
    });

    it('deselects the deleted tiles', () => {
      let tileGrid = createTileGrid(0, {
        selectable: true
      });
      let tile0 = createTile();
      let tile1 = createTile();
      let tile2 = createTile();
      tileGrid.insertTiles([tile0, tile1, tile2]);
      expect(tileGrid.tiles.length).toBe(3);

      tileGrid.selectAllTiles();
      expect(tileGrid.selectedTiles.length).toBe(3);

      tileGrid.deleteAllTiles();
      expect(tileGrid.selectedTiles.length).toBe(0);
    });

    it('adds empty marker', () => {
      let tileGrid = createTileGrid(0);
      let tile0 = createTile();
      let tile1 = createTile();
      let tile2 = createTile();
      tileGrid.render();
      expect(tileGrid.$container).toHaveClass('empty');

      tileGrid.insertTiles([tile0, tile1, tile2]);
      expect(tileGrid.$container).not.toHaveClass('empty');

      tileGrid.deleteAllTiles();
      expect(tileGrid.$container).toHaveClass('empty');
    });
  });

  describe('setTiles', () => {

    it('applies the order of the new tiles to tiles and filteredTiles', () => {
      let tileGrid = createTileGrid(0);
      let tile0 = createTile();
      let tile1 = createTile();
      let tile2 = createTile();
      tileGrid.insertTiles([tile0, tile1, tile2]);

      tileGrid.setTiles([tile2, tile1, tile0]);
      expect(tileGrid.tiles[0]).toBe(tile2);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile0);
      expect(tileGrid.filteredTiles[0]).toBe(tile2);
      expect(tileGrid.filteredTiles[1]).toBe(tile1);
      expect(tileGrid.filteredTiles[2]).toBe(tile0);
    });

    it('applies the order of the new tiles to the rendered elements', () => {
      let tileGrid = createTileGrid(0);
      let tile0 = createTile();
      let tile1 = createTile();
      let tile2 = createTile();
      tileGrid.insertTiles([tile0, tile1, tile2]);
      tileGrid.render();
      let $tiles = tileGrid.$container.children('.tile');
      expect($tiles.eq(0).data('widget')).toBe(tile0);
      expect($tiles.eq(1).data('widget')).toBe(tile1);
      expect($tiles.eq(2).data('widget')).toBe(tile2);

      tileGrid.setTiles([tile2, tile1, tile0]);
      $tiles = tileGrid.$container.children('.tile');
      expect($tiles.eq(0).data('widget')).toBe(tile2);
      expect($tiles.eq(1).data('widget')).toBe(tile1);
      expect($tiles.eq(2).data('widget')).toBe(tile0);
      expect(tileGrid.tiles[0]).toBe(tile2);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile0);
      expect(tileGrid.filteredTiles[0]).toBe(tile2);
      expect(tileGrid.filteredTiles[1]).toBe(tile1);
      expect(tileGrid.filteredTiles[2]).toBe(tile0);
    });

    it('applies the order of the new tiles to the filteredTiles if a filter is active', () => {
      let tileGrid = createTileGrid(3);
      let tile0 = tileGrid.tiles[0];
      let tile1 = tileGrid.tiles[1];
      let tile2 = tileGrid.tiles[2];

      let filter = {
        accept: tile => tile.label.indexOf('1') < 0
      };
      tileGrid.addFilter(filter);

      tileGrid.render();
      let $tiles = tileGrid.$container.children('.tile');
      expect($tiles.eq(0).data('widget')).toBe(tile0);
      expect($tiles.eq(1).data('widget')).toBe(tile1);
      expect($tiles.eq(2).data('widget')).toBe(tile2);
      expect(tileGrid.tiles[0]).toBe(tile0);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile2);
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tile0);
      expect(tileGrid.filteredTiles[1]).toBe(tile2);

      tileGrid.setTiles([tile2, tile1, tile0]);
      $tiles = tileGrid.$container.children('.tile');
      expect($tiles.eq(0).data('widget')).toBe(tile2);
      expect($tiles.eq(1).data('widget')).toBe(tile1);
      expect($tiles.eq(2).data('widget')).toBe(tile0);
      expect(tileGrid.tiles[0]).toBe(tile2);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile0);
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tile2);
      expect(tileGrid.filteredTiles[1]).toBe(tile0);
    });

  });

  describe('sort', () => {

    it('works if tiles are passed in model', () => {
      let model: InitModelOf<TileGrid> = {
        parent: session.desktop,
        comparator: (t0, t1) => {
          // desc
          return (t0['label'] < t1['label'] ? 1 : ((t0['label'] > t1['label']) ? -1 : 0));
        },
        tiles: [{
          objectType: Tile,
          label: 'Tile 0'
        }, {
          objectType: Tile,
          label: 'Tile 2'
        }, {
          objectType: Tile,
          label: 'Tile 1'
        }]
      };
      let tileGrid = scout.create(TileGrid, model);
      expect(tileGrid.tiles[0]['label']).toBe('Tile 2');
      expect(tileGrid.tiles[1]['label']).toBe('Tile 1');
      expect(tileGrid.tiles[2]['label']).toBe('Tile 0');
    });

    it('uses the comparator to sort the tiles and filteredTiles', () => {
      let tileGrid = createTileGrid(0);
      let tile0 = createTile({
        label: 'a'
      });
      let tile1 = createTile({
        label: 'b'
      });
      let tile2 = createTile({
        label: 'c'
      });
      tileGrid.insertTiles([tile0, tile1, tile2]);

      tileGrid.setComparator((t0, t1) => {
        // desc
        return (t0['label'] < t1['label'] ? 1 : ((t0['label'] > t1['label']) ? -1 : 0));
      });
      tileGrid.sort();
      expect(tileGrid.tiles[0]).toBe(tile2);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile0);
      expect(tileGrid.filteredTiles[0]).toBe(tile2);
      expect(tileGrid.filteredTiles[1]).toBe(tile1);
      expect(tileGrid.filteredTiles[2]).toBe(tile0);

      tileGrid.setComparator((t0, t1) => {
        // asc
        return (t0['label'] < t1['label'] ? -1 : ((t0['label'] > t1['label']) ? 1 : 0));
      });
      tileGrid.sort();
      expect(tileGrid.tiles[0]).toBe(tile0);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile2);
      expect(tileGrid.filteredTiles[0]).toBe(tile0);
      expect(tileGrid.filteredTiles[1]).toBe(tile1);
      expect(tileGrid.filteredTiles[2]).toBe(tile2);
    });

    it('is executed when new tiles are added', () => {
      let tileGrid = createTileGrid(0);
      let tile0 = createTile({
        label: 'a'
      });
      let tile1 = createTile({
        label: 'b'
      });
      let tile2 = createTile({
        label: 'c'
      });
      tileGrid.insertTiles([tile0, tile1]);

      tileGrid.setComparator((t0, t1) => {
        // desc
        return (t0['label'] < t1['label'] ? 1 : ((t0['label'] > t1['label']) ? -1 : 0));
      });
      tileGrid.sort();
      expect(tileGrid.tiles[0]).toBe(tile1);
      expect(tileGrid.tiles[1]).toBe(tile0);
      expect(tileGrid.filteredTiles[0]).toBe(tile1);
      expect(tileGrid.filteredTiles[1]).toBe(tile0);

      tileGrid.insertTiles([tile2]);
      expect(tileGrid.tiles[0]).toBe(tile2);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile0);
      expect(tileGrid.filteredTiles[0]).toBe(tile2);
      expect(tileGrid.filteredTiles[1]).toBe(tile1);
      expect(tileGrid.filteredTiles[2]).toBe(tile0);
    });

    it('reorders the DOM elements accordingly', () => {
      let tileGrid = createTileGrid(0);
      let tile0 = createTile({
        label: 'a'
      });
      let tile1 = createTile({
        label: 'b'
      });
      let tile2 = createTile({
        label: 'c'
      });
      tileGrid.insertTiles([tile0, tile1, tile2]);

      tileGrid.setComparator((t0, t1) => {
        // desc
        return (t0['label'] < t1['label'] ? 1 : ((t0['label'] > t1['label']) ? -1 : 0));
      });
      tileGrid.render();
      tileGrid.sort();
      expect(tileGrid.tiles[0]).toBe(tile2);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile0);
      expect(tileGrid.filteredTiles[0]).toBe(tile2);
      expect(tileGrid.filteredTiles[1]).toBe(tile1);
      expect(tileGrid.filteredTiles[2]).toBe(tile0);
      let $tiles = tileGrid.$container.children('.tile');
      expect($tiles.eq(0).data('widget')).toBe(tile2);
      expect($tiles.eq(1).data('widget')).toBe(tile1);
      expect($tiles.eq(2).data('widget')).toBe(tile0);
    });

  });

  describe('mouseDown', () => {

    describe('with multiSelect = false', () => {

      it('on a deselected tile selects the tile', () => {
        let tileGrid = createTileGrid(3, {
          selectable: true
        });
        tileGrid.render();
        let tile0 = tileGrid.tiles[0];

        JQueryTesting.triggerMouseDown(tile0.$container);
        expect(tile0.selected).toBe(true);
        expect(tileGrid.selectedTiles.length).toBe(1);
      });

      it('on a deselected tile selects the tile and unselects others', () => {
        let tileGrid = createTileGrid(3, {
          selectable: true
        });
        tileGrid.render();
        let tile0 = tileGrid.tiles[0];
        let tile1 = tileGrid.tiles[1];
        tileGrid.selectTile(tile1);
        expect(tile1.selected).toBe(true);

        JQueryTesting.triggerMouseDown(tile0.$container);
        expect(tile0.selected).toBe(true);
        expect(tile1.selected).toBe(false);
        expect(tileGrid.selectedTiles.length).toBe(1);
      });

      it('on a selected tile does nothing', () => {
        let tileGrid = createTileGrid(3, {
          selectable: true
        });
        tileGrid.render();
        let tile0 = tileGrid.tiles[0];
        tileGrid.selectTile(tile0);
        expect(tile0.selected).toBe(true);

        let eventTriggered = false;
        tileGrid.on('propertyChange', event => {
          if (event.propertyName === 'selectedTiles') {
            eventTriggered = true;
          }
        });

        JQueryTesting.triggerMouseDown(tile0.$container);
        expect(tile0.selected).toBe(true);
        expect(tileGrid.selectedTiles.length).toBe(1);
        expect(eventTriggered).toBe(false);
      });

      it('sets focusedTile property to clicked tile when selected', () => {
        let tileGrid = createTileGrid(3, {
          selectable: true
        });
        tileGrid.render();
        let tile0 = tileGrid.tiles[0];

        JQueryTesting.triggerMouseDown(tile0.$container);
        expect(tile0.selected).toBe(true);
        expect(tileGrid.focusedTile).toBe(tile0);
      });

    });

    describe('with multiSelect = true', () => {

      it('on a deselected tile selects the tile', () => {
        let tileGrid = createTileGrid(3, {
          selectable: true,
          multiSelect: true
        });
        tileGrid.render();
        let tile0 = tileGrid.tiles[0];

        JQueryTesting.triggerMouseDown(tile0.$container);
        expect(tile0.selected).toBe(true);
        expect(tileGrid.selectedTiles.length).toBe(1);
      });

      it('on a deselected tile selects the tile and unselects others', () => {
        let tileGrid = createTileGrid(3, {
          selectable: true,
          multiSelect: true
        });
        tileGrid.render();
        let tile0 = tileGrid.tiles[0];
        let tile1 = tileGrid.tiles[1];
        tileGrid.selectTile(tile1);
        expect(tile1.selected).toBe(true);

        JQueryTesting.triggerMouseDown(tile0.$container);
        expect(tile0.selected).toBe(true);
        expect(tile1.selected).toBe(false);
        expect(tileGrid.selectedTiles.length).toBe(1);
      });

      it('on a selected tile does nothing', () => {
        let tileGrid = createTileGrid(3, {
          selectable: true,
          multiSelect: true
        });
        tileGrid.render();
        let tile0 = tileGrid.tiles[0];
        tileGrid.selectTile(tile0);
        expect(tile0.selected).toBe(true);

        let eventTriggered = false;
        tileGrid.on('propertyChange', event => {
          if (event.propertyName === 'selectedTiles') {
            eventTriggered = true;
          }
        });

        JQueryTesting.triggerMouseDown(tile0.$container);
        expect(tile0.selected).toBe(true);
        expect(tileGrid.selectedTiles.length).toBe(1);
        expect(eventTriggered).toBe(false);
      });

      it('on a selected tile keeps the selection but deselects others if other tiles are selected', () => {
        let tileGrid = createTileGrid(3, {
          selectable: true,
          multiSelect: true
        });
        tileGrid.render();
        let tile0 = tileGrid.tiles[0];
        let tile1 = tileGrid.tiles[1];
        tileGrid.selectTiles([tile0, tile1]);
        expect(tile0.selected).toBe(true);
        expect(tile1.selected).toBe(true);

        JQueryTesting.triggerMouseDown(tile0.$container);
        expect(tile0.selected).toBe(true);
        expect(tile1.selected).toBe(false);
        expect(tileGrid.selectedTiles.length).toBe(1);
      });

      describe('with CTRL pressed', () => {

        it('on a deselected tile adds the tile to the selection', () => {
          let tileGrid = createTileGrid(3, {
            selectable: true,
            multiSelect: true
          });
          tileGrid.render();
          let tile0 = tileGrid.tiles[0];
          let tile1 = tileGrid.tiles[1];
          tileGrid.selectTile(tile1);
          expect(tile0.selected).toBe(false);
          expect(tile1.selected).toBe(true);

          JQueryTesting.triggerMouseDown(tile0.$container, {
            modifier: 'ctrl'
          });
          expect(tile0.selected).toBe(true);
          expect(tile1.selected).toBe(true);
          expect(tileGrid.selectedTiles.length).toBe(2);
        });

        it('on a selected tile removes the tile from the selection', () => {
          let tileGrid = createTileGrid(3, {
            selectable: true,
            multiSelect: true
          });
          tileGrid.render();
          let tile0 = tileGrid.tiles[0];
          let tile1 = tileGrid.tiles[1];
          tileGrid.selectTiles([tile0, tile1]);
          expect(tile0.selected).toBe(true);
          expect(tile1.selected).toBe(true);

          JQueryTesting.triggerMouseDown(tile0.$container, {
            modifier: 'ctrl'
          });
          expect(tile0.selected).toBe(false);
          expect(tile1.selected).toBe(true);
          expect(tileGrid.selectedTiles.length).toBe(1);
        });

        it('sets focusedTile property to null when when clicked tile is unselected', () => {
          let tileGrid = createTileGrid(3, {
            selectable: true
          });
          tileGrid.render();
          let tile0 = tileGrid.tiles[0];
          tileGrid.selectTile(tile0);
          expect(tile0.selected).toBe(true);

          JQueryTesting.triggerMouseDown(tile0.$container, {
            modifier: 'ctrl'
          });
          expect(tile0.selected).toBe(false);
          expect(tileGrid.focusedTile).toBe(null);
        });

      });

    });

  });

  describe('click', () => {
    it('triggers tileClick', () => {
      let tileGrid = createTileGrid(3, {
        selectable: false
      });
      tileGrid.render();
      let tile0 = tileGrid.tiles[0];
      let clickEventCount = 0;
      tileGrid.on('tileClick', event => {
        if (event.tile === tile0 && event.mouseButton === 1) {
          clickEventCount++;
        }
      });
      expect(tile0.selected).toBe(false);
      expect(clickEventCount).toBe(0);

      JQueryTesting.triggerClick(tile0.$container);
      expect(tile0.selected).toBe(false);
      expect(clickEventCount).toBe(1);
    });

    it('triggers tileSelected and tileClick if selectable', () => {
      let tileGrid = createTileGrid(3, {
        selectable: true
      });
      tileGrid.render();
      let tile0 = tileGrid.tiles[0];
      let clickEventCount = 0;
      let selectEventCount = 0;
      let events = [];
      tileGrid.on('propertyChange', event => {
        if (event.propertyName === 'selectedTiles') {
          selectEventCount++;
        }
        events.push('select');
      });
      tileGrid.on('tileClick', event => {
        if (event.tile === tile0 && event.mouseButton === 1) {
          clickEventCount++;
        }
        events.push('click');
      });
      expect(tile0.selected).toBe(false);
      expect(selectEventCount).toBe(0);
      expect(clickEventCount).toBe(0);

      JQueryTesting.triggerClick(tile0.$container);
      expect(tile0.selected).toBe(true);
      expect(selectEventCount).toBe(1);
      expect(clickEventCount).toBe(1);
      expect(events.length).toBe(2);
      expect(events[0]).toBe('select');
      expect(events[1]).toBe('click');
    });

    it('triggers tileAction when clicked twice', () => {
      let tileGrid = createTileGrid(3, {
        selectable: true
      });
      tileGrid.render();
      let tile0 = tileGrid.tiles[0];
      let selectEventCount = 0;
      let clickEventCount = 0;
      let actionEventCount = 0;
      let events = [];
      tileGrid.on('propertyChange', event => {
        if (event.propertyName === 'selectedTiles') {
          selectEventCount++;
        }
        events.push('select');
      });
      tileGrid.on('tileClick', event => {
        if (event.tile === tile0) {
          clickEventCount++;
        }
        events.push('click');
      });
      tileGrid.on('tileAction', event => {
        if (event.tile === tile0) {
          actionEventCount++;
        }
        events.push('action');
      });
      expect(tile0.selected).toBe(false);
      expect(selectEventCount).toBe(0);
      expect(clickEventCount).toBe(0);
      expect(actionEventCount).toBe(0);

      JQueryTesting.triggerDoubleClick(tile0.$container);
      expect(tile0.selected).toBe(true);
      expect(selectEventCount).toBe(1);
      expect(clickEventCount).toBe(1);
      expect(actionEventCount).toBe(1);
      expect(events.length).toBe(3);
      expect(events[0]).toBe('select');
      expect(events[1]).toBe('click');
      expect(events[2]).toBe('action');
    });
  });

  describe('filter', () => {

    it('filters the tiles according to the added filters', () => {
      let tileGrid = createTileGrid(3);
      expect(tileGrid.filteredTiles.length).toBe(3);

      let filter1 = {
        accept: tile => tile.label.indexOf('1') < 0
      };
      tileGrid.addFilter(filter1);
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.filteredTiles[1]).toBe(tileGrid.tiles[2]);
      expect(tileGrid.tiles[0].filterAccepted).toBe(true);
      expect(tileGrid.tiles[1].filterAccepted).toBe(false);
      expect(tileGrid.tiles[2].filterAccepted).toBe(true);

      let filter2 = {
        accept: tile => tile.label.indexOf('2') < 0
      };
      tileGrid.addFilter(filter2);
      expect(tileGrid.filteredTiles.length).toBe(1);
      expect(tileGrid.filteredTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.tiles[0].filterAccepted).toBe(true);
      expect(tileGrid.tiles[1].filterAccepted).toBe(false);
      expect(tileGrid.tiles[2].filterAccepted).toBe(false);

      tileGrid.removeFilter(filter1);
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.filteredTiles[1]).toBe(tileGrid.tiles[1]);
      expect(tileGrid.tiles[0].filterAccepted).toBe(true);
      expect(tileGrid.tiles[1].filterAccepted).toBe(true);
      expect(tileGrid.tiles[2].filterAccepted).toBe(false);

      tileGrid.removeFilter(filter2);
      expect(tileGrid.filteredTiles.length).toBe(3);
      expect(tileGrid.filters.length).toBe(0);
      expect(tileGrid.filteredTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.filteredTiles[1]).toBe(tileGrid.tiles[1]);
      expect(tileGrid.filteredTiles[2]).toBe(tileGrid.tiles[2]);
      expect(tileGrid.tiles[0].filterAccepted).toBe(true);
      expect(tileGrid.tiles[1].filterAccepted).toBe(true);
      expect(tileGrid.tiles[2].filterAccepted).toBe(true);

      // Add same first filter again
      tileGrid.addFilter(filter1);
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.filteredTiles[1]).toBe(tileGrid.tiles[2]);
      expect(tileGrid.tiles[0].filterAccepted).toBe(true);
      expect(tileGrid.tiles[1].filterAccepted).toBe(false);
      expect(tileGrid.tiles[2].filterAccepted).toBe(true);
    });

    it('considers newly inserted tiles', () => {
      let tileGrid = createTileGrid(3);
      let tile3 = createTile({
        label: 'Tile 3'
      });
      let tile4 = createTile({
        label: 'Tile 4'
      });
      expect(tileGrid.tiles.length).toBe(3);

      let filter = {
        accept: tile => {
          // Accept tile 1 and 4 only
          return tile.label.indexOf('1') >= 0 || tile.label.indexOf('4') >= 0;
        }
      };
      tileGrid.addFilter(filter);
      expect(tileGrid.filteredTiles.length).toBe(1);

      // Insert tile 3 which is not accepted -> still only tile 1 visible
      tileGrid.insertTiles(tile3);
      expect(tileGrid.tiles.length).toBe(4);
      expect(tileGrid.filteredTiles.length).toBe(1);

      // Insert tile 4 which is accepted -> tile 1 and 4 are visible
      tileGrid.insertTiles(tile4);
      expect(tileGrid.tiles.length).toBe(5);
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tileGrid.tiles[1]);
      expect(tileGrid.filteredTiles[1]).toBe(tileGrid.tiles[4]);
    });

    it('deselects not accepted tiles', () => {
      let tileGrid = createTileGrid(3, {
        selectable: true
      });
      tileGrid.selectTiles([tileGrid.tiles[0], tileGrid.tiles[1]]);
      expect(tileGrid.filteredTiles.length).toBe(3);
      expect(tileGrid.selectedTiles.length).toBe(2);
      expect(tileGrid.selectedTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.selectedTiles[1]).toBe(tileGrid.tiles[1]);

      let filter = {
        accept: tile => {
          // Accept tile 1 only
          return tile.label.indexOf('1') >= 0;
        }
      };
      tileGrid.addFilter(filter);
      expect(tileGrid.filteredTiles.length).toBe(1);
      expect(tileGrid.selectedTiles.length).toBe(1);
      expect(tileGrid.selectedTiles[0]).toBe(tileGrid.tiles[1]);
    });

    it('applies the filters initially, if there is one', () => {
      let tileGrid = createTileGrid(3, {
        filters: [{
          accept: tile => {
            // Accept tile 1 only
            return tile['label'].indexOf('1') >= 0;
          }
        }]
      });
      expect(tileGrid.tiles.length).toBe(3);
      expect(tileGrid.filteredTiles.length).toBe(1);
      expect(tileGrid.filteredTiles[0]).toBe(tileGrid.tiles[1]);
    });

    it('applies the filters initially even if every tile is accepted', () => {
      let tileGrid = createTileGrid(3, {
        filters: [{
          accept: tile => {
            // Accept all
            return true;
          }
        }]
      });
      expect(tileGrid.tiles.length).toBe(3);
      expect(tileGrid.filteredTiles.length).toBe(3);
      expect(tileGrid.filteredTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.filteredTiles[1]).toBe(tileGrid.tiles[1]);
      expect(tileGrid.filteredTiles[2]).toBe(tileGrid.tiles[2]);
    });

    it('updates empty marker', () => {
      let tileGrid = createTileGrid(3);
      tileGrid.render();
      expect(tileGrid.$container).not.toHaveClass('empty');

      let filter = {
        accept: tile => {
          // Accept none
          return false;
        }
      };
      tileGrid.addFilter(filter);
      expect(tileGrid.$container).toHaveClass('empty');

      tileGrid.removeFilter(filter);
      expect(tileGrid.$container).not.toHaveClass('empty');
    });

    it('still works if moved from one grid to another', () => {
      let tileGrid = createTileGrid();
      let tile0 = createTile({
        owner: session.desktop,
        label: 'Tile 0'
      });
      let tile1 = createTile({
        owner: session.desktop,
        label: 'Tile 1'
      });
      let tile2 = createTile({
        owner: session.desktop,
        label: 'Tile 2'
      });

      let filter1 = {
        accept: tile => tile.label.indexOf('1') < 0
      };
      tileGrid.setTiles([tile0, tile1, tile2]);
      tileGrid.addFilter(filter1);
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tile0);
      expect(tileGrid.filteredTiles[1]).toBe(tile2);
      expect(tileGrid.tiles[0].filterAccepted).toBe(true);
      expect(tileGrid.tiles[1].filterAccepted).toBe(false);
      expect(tileGrid.tiles[2].filterAccepted).toBe(true);

      tileGrid.deleteAllTiles();
      expect(tileGrid.filteredTiles.length).toBe(0);

      let tileGrid2 = createTileGrid(3);
      tileGrid2.setTiles([tile0, tile1, tile2]);
      tileGrid2.addFilter(filter1);
      expect(tileGrid2.filteredTiles.length).toBe(2);
      expect(tileGrid2.filteredTiles[0]).toBe(tile0);
      expect(tileGrid2.filteredTiles[1]).toBe(tile2);
      expect(tileGrid2.tiles[0].filterAccepted).toBe(true);
      expect(tileGrid2.tiles[1].filterAccepted).toBe(false);
      expect(tileGrid2.tiles[2].filterAccepted).toBe(true);
    });

    it('ignores placeholder tiles', () => {
      let tileGrid = createTileGrid(1, {
        withPlaceholders: true
      });
      let filter = {
        accept: tile => !(tile instanceof PlaceholderTile)
      };
      tileGrid.addFilter(filter);
      tileGrid.validateLogicalGrid();

      let tile = tileGrid.tiles[0];
      let placeholder = tileGrid.getFilteredTilesWithPlaceholders()[1];
      expect(placeholder).toBeInstanceOf(PlaceholderTile);
      expect(tileGrid.tiles).toEqual([tile]);
      expect(tileGrid.filteredTiles).toEqual([tile]);
    });
  });

  describe('addFilter', () => {

    it('adds the given filters', () => {
      let tileGrid = createTileGrid(3);

      let filter0 = {
        accept: tile => tile.label.indexOf('0') < 0
      };
      tileGrid.addFilter(filter0);
      expect(tileGrid.filters.length).toBe(1);
      expect(tileGrid.filters).toEqual([filter0]);

      let filter1 = {
        accept: tile => tile.label.indexOf('1') < 0
      };
      let filter2 = {
        accept: tile => tile.label.indexOf('2') < 0
      };
      tileGrid.addFilter([filter1, filter2]);
      expect(tileGrid.filters.length).toBe(3);
      expect(tileGrid.filters).toEqual([filter0, filter1, filter2]);

      // Does nothing if same filters are added again
      tileGrid.addFilter([filter1, filter2]);
      expect(tileGrid.filters.length).toBe(3);
      expect(tileGrid.filters).toEqual([filter0, filter1, filter2]);
    });

  });

  describe('removeFilter', () => {

    it('invalidates the logical grid', () => {
      let model = {
        parent: session.desktop,
        objectType: Group,
        body: {
          objectType: TileGrid,
          tiles: []
        }
      };
      let group = scout.create(model) as Group<TileGrid>;
      let tileGrid = group.body;
      let tileFilter = scout.create(RemoteTileFilter);
      tileFilter.setTileIds(['4', '5', '6']);
      tileGrid.addFilter(tileFilter);
      group.render();
      tileGrid.setTiles([{
        objectType: Tile,
        label: 'Tile 1"',
        id: '1'
      }, {
        objectType: Tile,
        label: 'Tile 2',
        id: '2'
      }, {
        objectType: Tile,
        label: 'Tile 3',
        id: '3'
      }]);

      expect(tileGrid.filters.length).toBe(1);
      expect(tileGrid.filteredTiles.length).toBe(0);

      group.bodyAnimating = true; // simulate existing animation
      tileGrid.removeFilter(tileFilter);
      expect(tileGrid.filters.length).toBe(0);
      expect(tileGrid.filteredTiles.length).toBe(3);
    });

    it('removes the given filters', () => {
      let tileGrid = createTileGrid(3);

      let filter0 = {
        accept: tile => tile.label.indexOf('0') < 0
      };
      let filter1 = {
        accept: tile => tile.label.indexOf('1') < 0
      };
      let filter2 = {
        accept: tile => tile.label.indexOf('2') < 0
      };
      tileGrid.setFilters([filter0, filter1, filter2]);
      expect(tileGrid.filters.length).toBe(3);
      expect(tileGrid.filters).toEqual([filter0, filter1, filter2]);

      tileGrid.removeFilter([filter1, filter2]);
      expect(tileGrid.filters.length).toBe(1);
      expect(tileGrid.filters).toEqual([filter0]);

      tileGrid.removeFilter([filter0, filter1]);
      expect(tileGrid.filters.length).toBe(0);
      expect(tileGrid.filters).toEqual([]);
    });

  });

  describe('tile visibility', () => {
    beforeEach(() => {
      $(`<style>
      @keyframes nop { 0% { opacity: 1; } 100% { opacity: 1; } }
      .tile.animate-visible { animation: nop; animation-duration: 100ms;}
      .tile.animate-invisible { animation: nop; animation-duration: 100ms;}
      .tile.animate-insert { animation: nop; animation-duration: 100ms;}
      .tile.animate-remove { animation: nop; animation-duration: 100ms;}
      .tile.newly-rendered { visibility: hidden !important;}
      .tile.before-animate-insert { visibility: hidden !important;}

      </style>`).appendTo($('#sandbox'));
    });

    it('is correct after insert animation', async () => {
      let tileGrid = createTileGrid(0);
      tileGrid.render();
      tileGrid.insertTile({objectType: Tile});
      let tile = tileGrid.tiles[0];
      expect(tile.$container).not.toHaveClass('animate-insert');
      expect(tile.$container.isVisibilityHidden()).toBe(true); // not visible until layout is done
      expect(tile.$container.isVisible()).toBe(true);

      tileGrid.validateLayout();
      expect(tile.$container).toHaveClass('animate-insert');
      expect(tile.$container.isVisibilityHidden()).toBe(false);
      expect(tile.$container.isVisible()).toBe(true);

      await JQueryTesting.whenAnimationEnd(tile.$container);
      expect(tile.$container).not.toHaveClass('animate-insert');
      expect(tile.$container.isVisibilityHidden()).toBe(false);
      expect(tile.$container.isVisible()).toBe(true);
    });

    it('is correct after hide animation', async () => {
      let tileGrid = createTileGrid(1);
      let tile = tileGrid.tiles[0];
      tileGrid.render();
      tileGrid.validateLayout();
      expect(tile.$container).not.toHaveClass('animate-invisible');
      expect(tile.$container.isVisibilityHidden()).toBe(false);
      expect(tile.$container.isVisible()).toBe(true);

      tile.setVisible(false);
      expect(tile.$container).toHaveClass('animate-invisible');
      expect(tile.$container.isVisibilityHidden()).toBe(false);
      expect(tile.$container.isVisible()).toBe(true);

      await JQueryTesting.whenAnimationEnd(tile.$container);
      expect(tile.$container).not.toHaveClass('animate-invisible');
      expect(tile.$container.isVisibilityHidden()).toBe(false);
      expect(tile.$container.isVisible()).toBe(false);
    });

    it('is correct after show animation', async () => {
      let tileGrid = createTileGrid(1);
      let tile = tileGrid.tiles[0];
      tile.setVisible(false);
      tileGrid.render();
      tileGrid.validateLayout();
      expect(tile.$container).not.toHaveClass('animate-visible');
      expect(tile.$container.isVisibilityHidden()).toBe(false);
      expect(tile.$container.isVisible()).toBe(false);

      tile.setVisible(true);
      expect(tile.$container).not.toHaveClass('animate-visible');
      expect(tile.$container.isVisibilityHidden()).toBe(true); // not visible until layout is done
      expect(tile.$container.isVisible()).toBe(true);

      tileGrid.validateLayoutTree();
      expect(tile.$container).toHaveClass('animate-visible');
      expect(tile.$container.isVisibilityHidden()).toBe(false);
      expect(tile.$container.isVisible()).toBe(true);

      await JQueryTesting.whenAnimationEnd(tile.$container);
      expect(tile.$container).not.toHaveClass('animate-visible');
      expect(tile.$container.isVisibilityHidden()).toBe(false);
      expect(tile.$container.isVisible()).toBe(true);
    });

    it('is correct after hide > show animation', async () => {
      let tileGrid = createTileGrid(1);
      let tile = tileGrid.tiles[0];
      tileGrid.render();
      tileGrid.validateLayout();

      tile.setVisible(false);
      expect(tile.$container).toHaveClass('animate-invisible');
      expect(tile.$container.isVisibilityHidden()).toBe(false);
      expect(tile.$container.isVisible()).toBe(true);

      // Animation is not complete yet
      await sleep(10);
      expect(tile.$container).toHaveClass('animate-invisible');
      expect(tile.$container.isVisibilityHidden()).toBe(false);
      expect(tile.$container.isVisible()).toBe(true);

      // Make it visible again while hide animation still runs
      tile.setVisible(true);
      expect(tile.$container).not.toHaveClass('animate-visible');
      expect(tile.$container.isVisibilityHidden()).toBe(true); // not visible until layout is done
      expect(tile.$container.isVisible()).toBe(true);

      tileGrid.validateLayoutTree();
      expect(tile.$container).toHaveClass('animate-visible');
      expect(tile.$container).not.toHaveClass('animate-invisible');
      expect(tile.$container.isVisibilityHidden()).toBe(false);
      expect(tile.$container.isVisible()).toBe(true);

      await JQueryTesting.whenAnimationEnd(tile.$container);
      expect(tile.$container).not.toHaveClass('animate-visible');
      expect(tile.$container.isVisibilityHidden()).toBe(false);
      expect(tile.$container.isVisible()).toBe(true);
    });

    it('is correct after show > hide animation', async () => {
      let tileGrid = createTileGrid(1);
      let tile = tileGrid.tiles[0];
      tile.setVisible(false);
      tileGrid.render();
      tileGrid.validateLayout();
      tile.setVisible(true);

      expect(tile.$container).not.toHaveClass('animate-visible');
      expect(tile.$container.isVisibilityHidden()).toBe(true); // not visible until layout is done
      expect(tile.$container.isVisible()).toBe(true);

      tileGrid.validateLayoutTree();
      expect(tile.$container).toHaveClass('animate-visible');
      expect(tile.$container.isVisibilityHidden()).toBe(false);
      expect(tile.$container.isVisible()).toBe(true);

      // Animation is not complete yet
      await sleep(10);
      expect(tile.$container).toHaveClass('animate-visible');
      expect(tile.$container.isVisibilityHidden()).toBe(false);
      expect(tile.$container.isVisible()).toBe(true);

      tile.setVisible(false);
      expect(tile.$container).toHaveClass('animate-invisible');
      expect(tile.$container).not.toHaveClass('animate-visible');
      expect(tile.$container.isVisibilityHidden()).toBe(false);
      expect(tile.$container.isVisible()).toBe(true);

      await JQueryTesting.whenAnimationEnd(tile.$container);
      expect(tile.$container).not.toHaveClass('animate-invisible');
      expect(tile.$container.isVisibilityHidden()).toBe(false);
      expect(tile.$container.isVisible()).toBe(false);
    });

    it('is correct after insert > hide > show animation', async () => {
      let tileGrid = createTileGrid(1);
      tileGrid.render();
      tileGrid.validateLayout();

      tileGrid.setTiles([{objectType: Tile}, tileGrid.tiles[0]]);
      let tile = tileGrid.tiles[0];
      expect(tile.$container).not.toHaveClass('animate-insert');
      expect(tile.$container.isVisibilityHidden()).toBe(true);

      tile.setVisible(false);
      tile.setVisible(true);
      expect(tile.$container.isVisibilityHidden()).toBe(true);

      // During the tile grid layout, the inserted tile must not be visible because the insert animation has not been started yet, even if tile.setVisible(true) was called
      // The layout animation needs a real viewport and sizes -> To make it easier in the test setup we suppress the layoutAnimationDone event to delay the start of the insert animation
      let origTrigger = tileGrid.trigger.bind(tileGrid);
      let triggerSpy = spyOn(tileGrid, 'trigger');
      let suppressedEvent;
      triggerSpy.and.callFake((type, event): any => {
        if (type === 'layoutAnimationDone') {
          suppressedEvent = event;
        } else {
          origTrigger(type, event);
        }
      });
      tileGrid.validateLayout();
      tileGrid.validateLayoutTree(); // Triggers the scheduled post validate task in Tile._renderVisible
      expect(tile.$container.isVisibilityHidden()).toBe(true);
      expect(tile.$container.isVisible()).toBe(true);

      // Finish TileGridLayout -> Insert animation will start
      triggerSpy.and.callThrough();
      tileGrid.trigger('layoutAnimationDone', suppressedEvent);
      expect(tile.$container.isVisibilityHidden()).toBe(false);
      expect(tile.$container).toHaveClass('animate-insert');
      expect(tile.$container.isVisible()).toBe(true);

      await JQueryTesting.whenAnimationEnd(tile.$container);
      expect(tile.$container).not.toHaveClass('animate-insert');
      expect(tile.$container.isVisibilityHidden()).toBe(false);
      expect(tile.$container.isVisible()).toBe(true);
    });
  });

  describe('aria properties', () => {

    it('has aria-activedescendant set if a tile receives focus', () => {
      let tileGrid = createTileGrid(3, {
        selectable: true
      });
      tileGrid.render();
      expect(tileGrid.$container.attr('aria-activedescendant')).toBeFalsy();
      let tile0 = tileGrid.tiles[0];
      tileGrid.setFocusedTile(tile0);
      expect(tileGrid.$container.attr('aria-activedescendant')).toBe(tile0.$container.attr('id'));
    });
  });

  describe('logical grid', () => {
    it('triggers a property change event if grid data of a tile changes', () => {
      let tileGrid = createTileGrid(3);
      tileGrid.render();
      tileGrid.validateLogicalGrid();
      let tile = tileGrid.tiles[0];
      let triggered = false;
      tile.on('propertyChange:gridData', event => {
        triggered = true;
      });
      tile.setGridDataHints(new GridData({w: 10}));
      expect(triggered).toBe(false); // Real grid data not computed yet

      tileGrid.validateLogicalGrid();
      expect(triggered).toBe(true);
    });

    it('does not trigger a property change event if new grid data is equal to the old one', () => {
      let tileGrid = createTileGrid(3);
      tileGrid.render();
      tileGrid.validateLogicalGrid();
      let tile = tileGrid.tiles[0];
      let triggered = false;
      tile.on('propertyChange:gridData', event => {
        triggered = true;
      });
      tile.setGridDataHints(new GridData(tile.gridDataHints));
      tileGrid.validateLogicalGrid();
      expect(triggered).toBe(false);

      // Verify that it works without using hints
      tile._setGridData(new GridData(tile.gridData));
      expect(triggered).toBe(false);
    });
  });
});
