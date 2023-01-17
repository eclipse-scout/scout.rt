/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Group, InitModelOf, keys, scout, Tile, TileAccordion, TileGrid, TileModel} from '../../../src/index';
import {JQueryTesting} from '../../../src/testing/index';

describe('TileAccordionKeyStrokes', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    // Set a fixed width to prevent tiles from wrapping on small screens (e.g. PhantomJS)
    $('<style>' +
      '.tile-accordion { width: 1000px;}' +
      '.tile-grid {position: relative; border: 1px dotted black; padding: 5px;}' +
      '.tile {position: absolute; border: 1px solid black;}' +
      '.tile.selected {border-color: blue;}' +
      '.scrollbar {position: absolute;}' +
      '</style>').appendTo($('#sandbox'));
  });

  function createAccordion(numGroups, model): TileAccordion {
    let groups = [];
    for (let i = 0; i < numGroups; i++) {
      groups.push({
        objectType: Group,
        label: 'Group ' + i,
        body: {
          objectType: TileGrid,
          scrollable: false,
          layoutConfig: {
            columnWidth: 100,
            rowHeight: 100
          }
        }
      });
    }
    let defaults = {
      parent: session.desktop,
      exclusiveExpand: false,
      groups: groups
    };
    model = $.extend({}, defaults, model);
    return scout.create(TileAccordion, model);
  }

  function createTile(model: TileModel): Tile {
    let defaults = {
      parent: session.desktop
    };
    model = $.extend({}, defaults, model);
    return scout.create(Tile, model as InitModelOf<Tile>);
  }

  function createTiles(numTiles: number, model?: TileModel): Tile[] {
    let tiles = [];
    for (let i = 0; i < numTiles; i++) {
      tiles.push(createTile(model));
    }
    return tiles;
  }

  describe('ctrl + a', () => {
    it('selects all tiles', () => {
      let accordion = createAccordion(3, {
        selectable: true
      });
      let tiles0 = createTiles(2);
      let tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.A, 'ctrl');
      expect(accordion.getSelectedTiles().length).toBe(5);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.A, 'ctrl');

      accordion.selectTile(tiles[0]);
      expect(accordion.getSelectedTiles().length).toBe(1);

      accordion.selectTile(tiles[4]);
      expect(accordion.getSelectedTiles().length).toBe(1);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.A, 'ctrl');
      expect(accordion.getSelectedTiles().length).toBe(5);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.A, 'ctrl');
    });

    it('deselects all tiles if tiles are already selected', () => {
      let accordion = createAccordion(3, {
        selectable: true
      });
      let tiles0 = createTiles(2);
      let tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      accordion.render();
      accordion.validateLayout();
      accordion.selectAllTiles();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.A, 'ctrl');
      expect(accordion.getSelectedTiles().length).toBe(0);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.A, 'ctrl');

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.A, 'ctrl');
      expect(accordion.getSelectedTiles().length).toBe(5);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.A, 'ctrl');

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.A, 'ctrl');
      expect(accordion.getSelectedTiles().length).toBe(0);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.A, 'ctrl');
    });

    it('only considers tiles of expanded groups', () => {
      let accordion = createAccordion(3, {
        selectable: true
      });
      let tiles0 = createTiles(2);
      let tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[0].setCollapsed(true);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[1].body.$container, keys.A, 'ctrl');
      expect(accordion.getSelectedTiles().length).toBe(3);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[1].body.$container, keys.A, 'ctrl');

      accordion.selectTile(tiles[2]);
      expect(accordion.getSelectedTiles().length).toBe(1);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[1].body.$container, keys.A, 'ctrl');
      expect(accordion.getSelectedTiles().length).toBe(3);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[1].body.$container, keys.A, 'ctrl');

      JQueryTesting.triggerKeyDownCapture(accordion.groups[1].body.$container, keys.A, 'ctrl');
      expect(accordion.getSelectedTiles().length).toBe(0);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[1].body.$container, keys.A, 'ctrl');

      JQueryTesting.triggerKeyDownCapture(accordion.groups[1].body.$container, keys.A, 'ctrl');
      expect(accordion.getSelectedTiles().length).toBe(3);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[1].body.$container, keys.A, 'ctrl');
    });

  });

  describe('key right', () => {
    it('selects the first tile of the next group if selected tile is the last one in the current group', () => {
      let accordion = createAccordion(2, {
        selectable: true
      });
      let tiles0 = createTiles(2);
      let tiles1 = createTiles(2);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[1]);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.RIGHT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[2]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.RIGHT);
    });

    it('selects the first tile of the next group if selected tile is the last one in the current group but only if next group is not collapsed', () => {
      let accordion = createAccordion(3, {
        selectable: true
      });
      let tiles0 = createTiles(2);
      let tiles1 = createTiles(2);
      let tiles2 = createTiles(2);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      accordion.groups[1].setCollapsed(true);
      accordion.groups[2].body.insertTiles(tiles2);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[1]);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.RIGHT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[4]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.RIGHT);
    });

    it('selects the first tile if no tile is selected yet', () => {
      let accordion = createAccordion(2, {
        selectable: true
      });
      let tiles0 = createTiles(2);
      let tiles1 = createTiles(2);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.RIGHT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.RIGHT);
    });

    it('does nothing if the last tile is already selected', () => {
      let accordion = createAccordion(2, {
        selectable: true
      });
      let tiles0 = createTiles(2);
      let tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(arrays.last(tiles));

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.RIGHT);
      expect(accordion.getSelectedTiles()).toEqual([arrays.last(tiles)]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.RIGHT);
    });

    it('selects the only tile if there is only one', () => {
      let accordion = createAccordion(2, {
        selectable: true
      });
      let tiles0 = createTiles(1);
      accordion.groups[0].body.insertTiles(tiles0);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.RIGHT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.RIGHT);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.RIGHT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.RIGHT);

      accordion.deselectAllTiles();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.RIGHT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.RIGHT);
    });

    describe('with shift', () => {
      it('adds the tile of the next group to the selection if the focused tile is the last tile of the current group', () => {
        let accordion = createAccordion(2, {
          selectable: true
        });
        let tiles0 = createTiles(2);
        let tiles1 = createTiles(2);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTile(tiles[1]);

        JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.RIGHT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2]]);
        JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.RIGHT, 'shift');

        JQueryTesting.triggerKeyDownCapture(accordion.groups[1].body.$container, keys.RIGHT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2], tiles[3]]);
        JQueryTesting.triggerKeyUpCapture(accordion.groups[1].body.$container, keys.RIGHT, 'shift');
      });

      it('removes the next tile from the selection if the focused tile is the first tile of the selection', () => {
        let accordion = createAccordion(2, {
          selectable: true
        });
        let tiles0 = createTiles(2);
        let tiles1 = createTiles(3);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[1], tiles[2], tiles[3]]);
        accordion.setFocusedTile(tiles[1]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.RIGHT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[2], tiles[3]]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[1].body.$container, keys.RIGHT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[3]]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[1].body.$container, keys.RIGHT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[3], tiles[4]]);
      });

      it('does nothing if the last tile is already selected', () => {
        let accordion = createAccordion(2, {
          selectable: true
        });
        let tiles0 = createTiles(2);
        let tiles1 = createTiles(1);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTile(tiles[1]);

        JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.RIGHT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2]]);
        JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.RIGHT, 'shift');

        JQueryTesting.triggerKeyDownCapture(accordion.groups[1].body.$container, keys.RIGHT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2]]);
        JQueryTesting.triggerKeyUpCapture(accordion.groups[1].body.$container, keys.RIGHT, 'shift');
      });

      it('adds the correct tile to the selection if the focused tile gets invisible', () => {
        let accordion = createAccordion(4, {
          selectable: true
        });
        let tiles0 = createTiles(2);
        let tiles1 = createTiles(3);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[1], tiles[2]]);
        accordion.setFocusedTile(tiles[1]);

        let filter = {
          accept: tile => {
            return tile !== tiles[1]; // Make tile 0 invisible
          }
        };
        accordion.groups[0].body.addFilter(filter);
        accordion.groups[1].body.addFilter(filter);
        expect(accordion.getSelectedTiles()).toEqual([tiles[2]]);
        expect(accordion.getFocusedTile()).toBe(null);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.RIGHT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[2], tiles[3]]);
      });

      it('connects two selections blocks and sets the focused tile to the beginning of the new block', () => {
        let accordion = createAccordion(2, {
          selectable: true
        });
        let tiles0 = createTiles(3);
        let tiles1 = createTiles(3);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[1], tiles[4]]);
        accordion.setFocusedTile(tiles[1]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.RIGHT, 'shift');
        expect(arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[1], tiles[2], tiles[4]])).toBe(true);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.RIGHT, 'shift');
        expect(arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[1], tiles[2], tiles[3], tiles[4]])).toBe(true);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[1].body.$container, keys.RIGHT, 'shift');
        expect(arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[1], tiles[2], tiles[3], tiles[4], tiles[5]])).toBe(true);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[1].body.$container, keys.RIGHT, 'shift');
        expect(arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[1], tiles[2], tiles[3], tiles[4], tiles[5]])).toBe(true);
      });
    });
  });

  describe('key left', () => {
    it('selects the last tile of the previous group if selected tile is the first one in the current group', () => {
      let accordion = createAccordion(2, {
        selectable: true
      });
      let tiles0 = createTiles(2);
      let tiles1 = createTiles(2);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[2]);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.LEFT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[1]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.LEFT);
    });

    it('selects the last tile of the previous group if selected tile is the first one in the current group but only if the group is not collapsed', () => {
      let accordion = createAccordion(3, {
        selectable: true
      });
      let tiles0 = createTiles(2);
      let tiles1 = createTiles(2);
      let tiles2 = createTiles(2);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      accordion.groups[1].setCollapsed(true);
      accordion.groups[2].body.insertTiles(tiles2);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[4]);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.LEFT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[1]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.LEFT);
    });

    it('selects the last tile if no tile is selected yet', () => {
      let accordion = createAccordion(2, {
        selectable: true
      });
      let tiles0 = createTiles(2);
      let tiles1 = createTiles(2);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.LEFT);
      expect(accordion.getSelectedTiles()).toEqual([arrays.last(tiles)]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.LEFT);
    });

    it('does nothing if the first tile is already selected', () => {
      let accordion = createAccordion(2, {
        selectable: true
      });
      let tiles0 = createTiles(2);
      let tiles1 = createTiles(2);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[0]);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.LEFT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.LEFT);
    });

    it('selects the only tile if there is only one', () => {
      let accordion = createAccordion(2, {
        selectable: true
      });
      let tiles0 = createTiles(1);
      accordion.groups[0].body.insertTiles(tiles0);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.LEFT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.LEFT);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.LEFT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.LEFT);

      accordion.deselectAllTiles();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.LEFT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.LEFT);
    });

    describe('with shift', () => {
      it('adds the previous tile to the selection', () => {
        let accordion = createAccordion(2, {
          selectable: true
        });
        let tiles0 = createTiles(2);
        let tiles1 = createTiles(2);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTile(tiles[3]);

        JQueryTesting.triggerKeyDownCapture(accordion.groups[1].body.$container, keys.LEFT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[2], tiles[3]]);
        JQueryTesting.triggerKeyUpCapture(accordion.groups[1].body.$container, keys.LEFT, 'shift');

        JQueryTesting.triggerKeyDownCapture(accordion.groups[1].body.$container, keys.LEFT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2], tiles[3]]);
        JQueryTesting.triggerKeyUpCapture(accordion.groups[1].body.$container, keys.LEFT, 'shift');
      });

      it('does nothing if the first tile is already selected', () => {
        let accordion = createAccordion(2, {
          selectable: true
        });
        let tiles0 = createTiles(2);
        let tiles1 = createTiles(2);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTile(tiles[1]);

        JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.LEFT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[0], tiles[1]]);
        JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.LEFT, 'shift');

        JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.LEFT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[0], tiles[1]]);
        JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.LEFT, 'shift');
      });

      it('removes the previous tile from the selection if the next tile is already selected', () => {
        let accordion = createAccordion(2, {
          selectable: true
        });
        let tiles0 = createTiles(3);
        let tiles1 = createTiles(2);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[1], tiles[2], tiles[3]]);
        accordion.setFocusedTile(tiles[3]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[1].body.$container, keys.LEFT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2]]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.LEFT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1]]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.LEFT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[0], tiles[1]]);
      });

      it('adds the correct tile to the selection if the focused tile gets invisible', () => {
        let accordion = createAccordion(2, {
          selectable: true
        });
        let tiles0 = createTiles(2);
        let tiles1 = createTiles(2);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[1], tiles[2]]);
        accordion.setFocusedTile(tiles[2]);

        let filter = {
          accept: tile => {
            return tile !== tiles[2]; // Make tile 2 invisible
          }
        };
        accordion.groups[0].body.addFilter(filter);
        accordion.groups[1].body.addFilter(filter);
        expect(accordion.getSelectedTiles()).toEqual([tiles[1]]);
        expect(accordion.getFocusedTile()).toBe(null);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.LEFT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[0], tiles[1]]);
      });

      it('connects two selections blocks and sets the focused tile to the beginning of the new block', () => {
        let accordion = createAccordion(2, {
          selectable: true
        });
        let tiles0 = createTiles(3);
        let tiles1 = createTiles(2);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[1], tiles[4]]);
        accordion.setFocusedTile(tiles[4]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.LEFT, 'shift');
        expect(arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[1], tiles[3], tiles[4]])).toBe(true);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.LEFT, 'shift');
        expect(arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[1], tiles[2], tiles[3], tiles[4]])).toBe(true);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.LEFT, 'shift');
        expect(arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4]])).toBe(true);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.LEFT, 'shift');
        expect(arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4]])).toBe(true);
      });

    });

  });

  describe('key down', () => {
    it('selects the tile in the grid below if the selected tile is in the last line of the current group', () => {
      let accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(3);
      let tiles1 = createTiles(6);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[0]);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[3]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.DOWN);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[1].body.$container, keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[6]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[1].body.$container, keys.DOWN);
    });

    it('selects the tile in the grid below if the selected tile is in the last line of the current group but only if the group is not collapsed', () => {
      let accordion = createAccordion(3, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(3);
      let tiles1 = createTiles(3);
      let tiles2 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      accordion.groups[1].setCollapsed(true);
      accordion.groups[2].body.insertTiles(tiles2);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[0]);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[6]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.DOWN);
    });

    it('considers filtered tiles', () => {
      let accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(3);
      let tiles1 = createTiles(6);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      let filter = {
        accept: tile => {
          return tile !== tiles[3] && tile !== tiles[7]; // Make tile 3 and 7 invisible
        }
      };
      accordion.groups[0].body.addFilter(filter);
      accordion.groups[1].body.addFilter(filter);
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[0]);

      // Tile 3 is not accepted by filter -> tile 4 has to be selected
      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[4]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.DOWN);

      // Tile 7 is not accepted by filter -> tile 8 has to be selected
      JQueryTesting.triggerKeyDownCapture(accordion.groups[1].body.$container, keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[8]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[1].body.$container, keys.DOWN);
    });

    it('selects the first tile if no tile is selected yet', () => {
      let accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(3);
      let tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.DOWN);
    });

    it('selects the first tile if no tile is selected yet or the focused tile was in a collapsed group', () => {
      let accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(2);
      let tiles1 = createTiles(2);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.selectTile(tiles[1]);
      accordion.setFocusedTile(tiles[1]);
      accordion.render();
      accordion.validateLayout();

      $.fx.off = true;
      accordion.groups[0].setCollapsed(true);
      $.fx.off = false;
      expect(accordion.getFocusedTile()).toBe(null);
      expect(accordion.getSelectedTiles().length).toBe(0);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[1].body.$container, keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[2]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[1].body.$container, keys.DOWN);
    });

    it('does nothing if a tile in the last row is already selected', () => {
      let accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(3);
      let tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[3]);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[1].body.$container, keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[3]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[1].body.$container, keys.DOWN);

      accordion.selectTile(arrays.last(tiles));
      accordion.setFocusedTile(arrays.last(tiles));
      expect(accordion.getSelectedTiles()).toEqual([arrays.last(tiles)]);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[1].body.$container, keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([arrays.last(tiles)]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[1].body.$container, keys.DOWN);
    });

    it('selects the only tile if there is only one', () => {
      let accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(0);
      let tiles1 = createTiles(1);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.DOWN);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.DOWN);

      accordion.deselectAllTiles();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[1].body.$container, keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[1].body.$container, keys.DOWN);
    });

    it('selects the last tile if below the focused tile is no tile', () => {
      let accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(4);
      let tiles1 = createTiles(4);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.selectTile(tiles[2]);
      accordion.render();
      accordion.validateLayout();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[3]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.DOWN);

      accordion.selectTile(tiles[6]);
      accordion.setFocusedTile(tiles[6]);
      JQueryTesting.triggerKeyDownCapture(accordion.groups[1].body.$container, keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[7]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[1].body.$container, keys.DOWN);
    });

    describe('with shift', () => {
      it('adds the tiles between the focused and the newly focused tile to the selection', () => {
        let accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles0 = createTiles(5);
        let tiles1 = createTiles(5);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTile(tiles[1]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.DOWN, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2], tiles[3], tiles[4]]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[1].body.$container, keys.DOWN, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6]]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[1].body.$container, keys.DOWN, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7], tiles[8], tiles[9]]);
      });

      it('removes the tiles between the focused and the newly focused tiles from the selection if the focused tile is the first tile of the selection', () => {
        let accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles0 = createTiles(5);
        let tiles1 = createTiles(5);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6]]);
        accordion.setFocusedTile(tiles[1]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.DOWN, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[4], tiles[5], tiles[6]]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[1].body.$container, keys.DOWN, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[6]]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[1].body.$container, keys.DOWN, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[6], tiles[7], tiles[8], tiles[9]]);
      });

      it('does nothing if a tile in the last row is already selected', () => {
        let accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles0 = createTiles(3);
        let tiles1 = createTiles(3);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[2], tiles[3]]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.DOWN, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[2], tiles[3]]);
      });

      it('adds the correct tile to the selection if the focused tile gets invisible', () => {
        let accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles0 = createTiles(3);
        let tiles1 = createTiles(3);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[0], tiles[1], tiles[2]]);
        accordion.setFocusedTile(tiles[0]);

        let filter = {
          accept: tile => {
            return tile !== tiles[0]; // Make tile 0 invisible
          }
        };
        accordion.groups[0].body.addFilter(filter);
        accordion.groups[1].body.addFilter(filter);
        accordion.validateLayout();
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2]]);
        expect(accordion.getFocusedTile()).toBe(null);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.DOWN, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2], tiles[3], tiles[4]]);
      });

      it('connects two selections blocks and sets the focused tile to the beginning of the new block', () => {
        let accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles0 = createTiles(3);
        let tiles1 = createTiles(6);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[0], tiles[1], tiles[5]]);
        accordion.setFocusedTile(tiles[1]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.DOWN, 'shift');
        expect(arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5]])).toBe(true);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.DOWN, 'shift');
        expect(arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]])).toBe(true);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.DOWN, 'shift');
        expect(arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]])).toBe(true);
      });
    });

  });

  describe('key up', () => {
    it('selects the tile in the grid above if the selected tile is in the first line of the current group', () => {
      let accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(3);
      let tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(arrays.last(tiles));

      JQueryTesting.triggerKeyDownCapture(accordion.groups[1].body.$container, keys.UP);
      expect(accordion.getSelectedTiles()).toEqual([tiles[2]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[1].body.$container, keys.UP);
    });

    it('selects the tile in the grid above if the selected tile is in the first line of the current group but only if the group is not collapsed', () => {
      let accordion = createAccordion(3, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(3);
      let tiles1 = createTiles(3);
      let tiles2 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      accordion.groups[1].setCollapsed(true);
      accordion.groups[2].body.insertTiles(tiles2);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[8]);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[2].body.$container, keys.UP);
      expect(accordion.getSelectedTiles()).toEqual([tiles[2]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[2].body.$container, keys.UP);
    });

    it('selects the last tile if no tile is selected yet', () => {
      let accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(3);
      let tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[1].body.$container, keys.UP);
      expect(accordion.getSelectedTiles()).toEqual([arrays.last(tiles)]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[1].body.$container, keys.UP);
    });

    it('does nothing if a tile in the first row is already selected', () => {
      let accordion = createAccordion(8, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(3);
      let tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[2]);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.UP);
      expect(accordion.getSelectedTiles()).toEqual([tiles[2]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.UP);

      accordion.selectTile(tiles[0]);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.UP);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.UP);
    });

    it('selects the only tile if there is only one', () => {
      let accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(1);
      let tiles1 = createTiles(0);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.UP);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.UP);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.UP);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.UP);

      accordion.deselectAllTiles();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.UP);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.UP);
    });

    describe('with shift', () => {
      it('adds the tiles between the focused and the newly focused tile to the selection', () => {
        let accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles0 = createTiles(3);
        let tiles1 = createTiles(3);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTile(tiles[4]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.UP, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2], tiles[3], tiles[4]]);
      });

      it('removes the tiles between the focused and the newly focused tiles from the selection if the focused tile is the first tile of the selection', () => {
        let accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles0 = createTiles(2);
        let tiles1 = createTiles(3);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[1], tiles[2], tiles[3], tiles[4]]);
        accordion.setFocusedTile(tiles[4]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.UP, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1]]);
      });

      it('does nothing if a tile in the first row is already selected', () => {
        let accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles0 = createTiles(3);
        let tiles1 = createTiles(3);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[1], tiles[2], tiles[3]]);
        accordion.setFocusedTile(tiles[2]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.UP, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2], tiles[3]]);
      });

      it('adds the correct tile to the selection if the focused tile gets invisible', () => {
        let accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles0 = createTiles(3);
        let tiles1 = createTiles(3);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[2], tiles[3], tiles[4]]);
        accordion.setFocusedTile(tiles[2]);

        let filter = {
          accept: tile => {
            return tile !== tiles[2]; // Make tile 2 invisible
          }
        };
        accordion.groups[0].body.addFilter(filter);
        accordion.groups[1].body.addFilter(filter);
        accordion.validateLayout();
        expect(accordion.getSelectedTiles()).toEqual([tiles[3], tiles[4]]);
        expect(accordion.getFocusedTile()).toBe(null);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.UP, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[0], tiles[1], tiles[3], tiles[4]]);
      });

      it('connects two selections blocks and sets the focused tile to the beginning of the new block', () => {
        let accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles0 = createTiles(4);
        let tiles1 = createTiles(6);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[3], tiles[7], tiles[8]]);
        accordion.setFocusedTile(tiles[7]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.UP, 'shift');
        expect(arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[3], tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]])).toBe(true);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.UP, 'shift');
        expect(arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]])).toBe(true);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.UP, 'shift');
        expect(arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]])).toBe(true);
      });
    });
  });

  describe('home', () => {
    it('selects the first tile', () => {
      let accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(3);
      let tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[1]);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.HOME);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.HOME);
    });

    it('selects the first tile but only if the group is not collapsed', () => {
      let accordion = createAccordion(3, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(2);
      let tiles1 = createTiles(2);
      let tiles2 = createTiles(2);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[0].setCollapsed(true);
      accordion.groups[1].body.insertTiles(tiles1);
      accordion.groups[2].body.insertTiles(tiles2);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[5]);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[1].body.$container, keys.HOME);
      expect(accordion.getSelectedTiles()).toEqual([tiles[2]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[1].body.$container, keys.HOME);
    });

    it('does nothing if the first tile is already selected', () => {
      let accordion = createAccordion(2, {
        selectable: true
      });
      let tiles0 = createTiles(3);
      let tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[0]);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.HOME);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.HOME);
    });

    it('selects only the first tile if first and other tiles are selected', () => {
      let accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(3);
      let tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTiles([tiles[0], tiles[1]]);
      accordion.setFocusedTile(tiles[1]);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.HOME);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.HOME);
    });

    it('selects the only tile if there is only one', () => {
      let accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(0);
      let tiles1 = createTiles(1);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.HOME);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.HOME);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.HOME);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.HOME);

      accordion.deselectAllTiles();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.HOME);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.HOME);
    });

    describe('with shift', () => {
      it('adds the tiles between the focused and the newly focused tile to the selection', () => {
        let accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles0 = createTiles(3);
        let tiles1 = createTiles(6);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[6], tiles[7]]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.HOME, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7]]);
      });
    });

  });

  describe('end', () => {
    it('selects the last tile', () => {
      let accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(3);
      let tiles1 = createTiles(6);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.END);
      expect(accordion.getSelectedTiles()).toEqual([arrays.last(tiles)]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.END);
    });

    it('selects the last tile but only if the group is not collapsed', () => {
      let accordion = createAccordion(3, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(2);
      let tiles1 = createTiles(2);
      let tiles2 = createTiles(2);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      accordion.groups[2].body.insertTiles(tiles2);
      accordion.groups[2].setCollapsed(true);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.END);
      expect(accordion.getSelectedTiles()).toEqual([tiles[3]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.END);
    });

    it('does nothing if the last tile is already selected', () => {
      let accordion = createAccordion(4, {
        selectable: true
      });
      let tiles0 = createTiles(3);
      let tiles1 = createTiles(6);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(arrays.last(tiles));

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.END);
      expect(accordion.getSelectedTiles()).toEqual([arrays.last(tiles)]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.END);
    });

    it('selects only the last tile if last and other tiles are selected', () => {
      let accordion = createAccordion(4, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(3);
      let tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTiles([tiles[4], tiles[5]]);
      accordion.setFocusedTile(tiles[5]);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.END);
      expect(accordion.getSelectedTiles()).toEqual([tiles[4], tiles[5]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.END);
    });

    it('selects the only tile if there is only one', () => {
      let accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles0 = createTiles(1);
      let tiles1 = createTiles(0);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      let tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.END);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.END);

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.END);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.END);

      accordion.deselectAllTiles();

      JQueryTesting.triggerKeyDownCapture(accordion.groups[0].body.$container, keys.END);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      JQueryTesting.triggerKeyUpCapture(accordion.groups[0].body.$container, keys.END);
    });

    describe('with shift', () => {
      it('adds the tiles between the focused and the newly focused tile to the selection', () => {
        let accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles0 = createTiles(5);
        let tiles1 = createTiles(4);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        let tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[4], tiles[5]]);

        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.END, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]]);

        // After pressing shift home afterwards all tiles should be selected
        JQueryTesting.triggerKeyInputCapture(accordion.groups[0].body.$container, keys.HOME, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]]);
      });
    });

  });

});
