/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("TileAccordionKeyStrokes", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    $('<style>' +
        '.tile-grid {position: relative; border: 1px dotted black; padding: 5px;}' +
        '.tile {position: absolute; border: 1px solid black;}' +
        '.tile.selected {border-color: blue;}' +
        '.scrollbar {position: absolute;}' +
        '</style>').appendTo($('#sandbox'));
  });

  function createAccordion(numGroups, model) {
    var groups = [];
    for (var i = 0; i < numGroups; i++) {
      groups.push({
        objectType: 'Group',
        label: "Group " + i,
        body: {
          objectType: 'TileGrid',
          scrollable: false,
          layoutConfig: {
            columnWidth: 100,
            rowHeight: 100
          }
        }
      });
    }
    var defaults = {
      parent: session.desktop,
      exclusiveExpand: false,
      groups: groups
    };
    model = $.extend({}, defaults, model);
    return scout.create('TileAccordion', model);
  }

  function createGroup(model) {
    var defaults = {
      parent: session.desktop,
      body: {
        objectType: 'TileGrid',
        scrollable: false
      }
    };
    model = $.extend({}, defaults, model);
    return scout.create('Group', model);
  }

  function createTile(model) {
    var defaults = {
      parent: session.desktop
    };
    model = $.extend({}, defaults, model);
    return scout.create('Tile', model);
  }

  function createTiles(numTiles, model) {
    var tiles = [];
    for (var i = 0; i < numTiles; i++) {
      tiles.push(createTile(model));
    }
    return tiles;
  }

  describe('ctrl + a', function() {
    it('selects all tiles', function() {
      var accordion = createAccordion(3, {
        selectable: true
      });
      var tiles0 = createTiles(2);
      var tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.A, 'ctrl');
      expect(accordion.getSelectedTiles().length).toBe(5);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.A, 'ctrl');

      accordion.selectTile(tiles[0]);
      expect(accordion.getSelectedTiles().length).toBe(1);

      accordion.selectTile(tiles[4]);
      expect(accordion.getSelectedTiles().length).toBe(1);

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.A, 'ctrl');
      expect(accordion.getSelectedTiles().length).toBe(5);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.A, 'ctrl');
    });

    it('deselects all tiles if tiles are already selected', function() {
      var accordion = createAccordion(3, {
        selectable: true
      });
      var tiles0 = createTiles(2);
      var tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      accordion.render();
      accordion.validateLayout();
      accordion.selectAllTiles();

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.A, 'ctrl');
      expect(accordion.getSelectedTiles().length).toBe(0);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.A, 'ctrl');

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.A, 'ctrl');
      expect(accordion.getSelectedTiles().length).toBe(5);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.A, 'ctrl');

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.A, 'ctrl');
      expect(accordion.getSelectedTiles().length).toBe(0);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.A, 'ctrl');
    });

  });

  describe('key right', function() {
    it("selects the first tile of the next group if selected tile is the last one in the current group", function() {
      var accordion = createAccordion(2, {
        selectable: true
      });
      var tiles0 = createTiles(2);
      var tiles1 = createTiles(2);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[1]);

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.RIGHT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[2]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.RIGHT);
    });

    it("selects the first tile of the next group if selected tile is the last one in the current group but only if next group is not collapsed", function() {
      var accordion = createAccordion(3, {
        selectable: true
      });
      var tiles0 = createTiles(2);
      var tiles1 = createTiles(2);
      var tiles2 = createTiles(2);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      accordion.groups[1].setCollapsed(true);
      accordion.groups[2].body.insertTiles(tiles2);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[1]);

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.RIGHT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[4]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.RIGHT);
    });

    it("selects the first tile if no tile is selected yet", function() {
      var accordion = createAccordion(2, {
        selectable: true
      });
      var tiles0 = createTiles(2);
      var tiles1 = createTiles(2);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.RIGHT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.RIGHT);
    });

    it("does nothing if the last tile is already selected", function() {
      var accordion = createAccordion(2, {
        selectable: true
      });
      var tiles0 = createTiles(2);
      var tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(scout.arrays.last(tiles));

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.RIGHT);
      expect(accordion.getSelectedTiles()).toEqual([scout.arrays.last(tiles)]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.RIGHT);
    });

    it("selects the only tile if there is only one", function() {
      var accordion = createAccordion(2, {
        selectable: true
      });
      var tiles0 = createTiles(1);
      accordion.groups[0].body.insertTiles(tiles0);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.RIGHT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.RIGHT);

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.RIGHT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.RIGHT);

      accordion.deselectAllTiles();

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.RIGHT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.RIGHT);
    });

    describe('with shift', function() {
      it("adds the tile of the next group to the selection if the focused tile is the last tile of the current group", function() {
        var accordion = createAccordion(2, {
          selectable: true
        });
        var tiles0 = createTiles(2);
        var tiles1 = createTiles(2);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTile(tiles[1]);

        accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.RIGHT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2]]);
        accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.RIGHT, 'shift');

        accordion.groups[1].body.$container.triggerKeyDownCapture(scout.keys.RIGHT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2], tiles[3]]);
        accordion.groups[1].body.$container.triggerKeyUpCapture(scout.keys.RIGHT, 'shift');
      });

      it("removes the next tile from the selection if the focused tile is the first tile of the selection", function() {
        var accordion = createAccordion(2, {
          selectable: true
        });
        var tiles0 = createTiles(2);
        var tiles1 = createTiles(3);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[1], tiles[2], tiles[3]]);
        accordion.setFocusedTile(tiles[1]);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.RIGHT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[2], tiles[3]]);

        accordion.groups[1].body.$container.triggerKeyInputCapture(scout.keys.RIGHT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[3]]);

        accordion.groups[1].body.$container.triggerKeyInputCapture(scout.keys.RIGHT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[3], tiles[4]]);
      });

      it("does nothing if the last tile is already selected", function() {
        var accordion = createAccordion(2, {
          selectable: true
        });
        var tiles0 = createTiles(2);
        var tiles1 = createTiles(1);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTile(tiles[1]);

        accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.RIGHT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2]]);
        accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.RIGHT, 'shift');

        accordion.groups[1].body.$container.triggerKeyDownCapture(scout.keys.RIGHT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2]]);
        accordion.groups[1].body.$container.triggerKeyUpCapture(scout.keys.RIGHT, 'shift');
      });

      it("adds the correct tile to the selection if the focused tile gets invisible", function() {
        var accordion = createAccordion(4, {
          selectable: true
        });
        var tiles0 = createTiles(2);
        var tiles1 = createTiles(3);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[1], tiles[2]]);
        accordion.setFocusedTile(tiles[1]);

        var filter = {
          accept: function(tile) {
            return tile !== tiles[1]; // Make tile 0 invisible
          }
        };
        accordion.groups[0].body.addFilter(filter);
        accordion.groups[1].body.addFilter(filter);
        accordion.groups[0].body.filter();
        accordion.groups[1].body.filter();
        expect(accordion.getSelectedTiles()).toEqual([tiles[2]]);
        expect(accordion.getFocusedTile()).toBe(null);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.RIGHT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[2], tiles[3]]);
      });

      it("connects two selections blocks and sets the focused tile to the beginning of the new block", function() {
        var accordion = createAccordion(2, {
          selectable: true
        });
        var tiles0 = createTiles(3);
        var tiles1 = createTiles(3);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[1], tiles[4]]);
        accordion.setFocusedTile(tiles[1]);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.RIGHT, 'shift');
        expect(scout.arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[1], tiles[2], tiles[4]])).toBe(true);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.RIGHT, 'shift');
        expect(scout.arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[1], tiles[2], tiles[3], tiles[4]])).toBe(true);

        accordion.groups[1].body.$container.triggerKeyInputCapture(scout.keys.RIGHT, 'shift');
        expect(scout.arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[1], tiles[2], tiles[3], tiles[4], tiles[5]])).toBe(true);

        accordion.groups[1].body.$container.triggerKeyInputCapture(scout.keys.RIGHT, 'shift');
        expect(scout.arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[1], tiles[2], tiles[3], tiles[4], tiles[5]])).toBe(true);
      });
    });
  });

  describe('key left', function() {
    it("selects the last tile of the previous group if selected tile is the first one in the current group", function() {
      var accordion = createAccordion(2, {
        selectable: true
      });
      var tiles0 = createTiles(2);
      var tiles1 = createTiles(2);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[2]);

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.LEFT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[1]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.LEFT);
    });

    it("selects the last tile of the previous group if selected tile is the first one in the current group but only if the group is not collapsed", function() {
      var accordion = createAccordion(3, {
        selectable: true
      });
      var tiles0 = createTiles(2);
      var tiles1 = createTiles(2);
      var tiles2 = createTiles(2);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      accordion.groups[1].setCollapsed(true);
      accordion.groups[2].body.insertTiles(tiles2);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[4]);

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.LEFT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[1]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.LEFT);
    });

    it("selects the last tile if no tile is selected yet", function() {
      var accordion = createAccordion(2, {
        selectable: true
      });
      var tiles0 = createTiles(2);
      var tiles1 = createTiles(2);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.LEFT);
      expect(accordion.getSelectedTiles()).toEqual([scout.arrays.last(tiles)]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.LEFT);
    });

    it("does nothing if the first tile is already selected", function() {
      var accordion = createAccordion(2, {
        selectable: true
      });
      var tiles0 = createTiles(2);
      var tiles1 = createTiles(2);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[0]);

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.LEFT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.LEFT);
    });

    it("selects the only tile if there is only one", function() {
      var accordion = createAccordion(2, {
        selectable: true
      });
      var tiles0 = createTiles(1);
      accordion.groups[0].body.insertTiles(tiles0);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.LEFT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.LEFT);

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.LEFT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.LEFT);

      accordion.deselectAllTiles();

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.LEFT);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.LEFT);
    });

    describe('with shift', function() {
      it("adds the previous tile to the selection", function() {
        var accordion = createAccordion(2, {
          selectable: true
        });
        var tiles0 = createTiles(2);
        var tiles1 = createTiles(2);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTile(tiles[3]);

        accordion.groups[1].body.$container.triggerKeyDownCapture(scout.keys.LEFT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[2], tiles[3]]);
        accordion.groups[1].body.$container.triggerKeyUpCapture(scout.keys.LEFT, 'shift');

        accordion.groups[1].body.$container.triggerKeyDownCapture(scout.keys.LEFT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2], tiles[3]]);
        accordion.groups[1].body.$container.triggerKeyUpCapture(scout.keys.LEFT, 'shift');
      });

      it("does nothing if the first tile is already selected", function() {
        var accordion = createAccordion(2, {
          selectable: true
        });
        var tiles0 = createTiles(2);
        var tiles1 = createTiles(2);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTile(tiles[1]);

        accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.LEFT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[0], tiles[1]]);
        accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.LEFT, 'shift');

        accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.LEFT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[0], tiles[1]]);
        accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.LEFT, 'shift');
      });

      it("removes the previous tile from the selection if the next tile is already selected", function() {
        var accordion = createAccordion(2, {
          selectable: true
        });
        var tiles0 = createTiles(3);
        var tiles1 = createTiles(2);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[1], tiles[2], tiles[3]]);
        accordion.setFocusedTile(tiles[3]);

        accordion.groups[1].body.$container.triggerKeyInputCapture(scout.keys.LEFT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2]]);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.LEFT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1]]);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.LEFT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[0], tiles[1]]);
      });

      it("adds the correct tile to the selection if the focused tile gets invisible", function() {
        var accordion = createAccordion(2, {
          selectable: true
        });
        var tiles0 = createTiles(2);
        var tiles1 = createTiles(2);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[1], tiles[2]]);
        accordion.setFocusedTile(tiles[2]);

        var filter = {
          accept: function(tile) {
            return tile !== tiles[2]; // Make tile 2 invisible
          }
        };
        accordion.groups[0].body.addFilter(filter);
        accordion.groups[1].body.addFilter(filter);
        accordion.groups[0].body.filter();
        accordion.groups[1].body.filter();
        expect(accordion.getSelectedTiles()).toEqual([tiles[1]]);
        expect(accordion.getFocusedTile()).toBe(null);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.LEFT, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[0], tiles[1]]);
      });

      it("connects two selections blocks and sets the focused tile to the beginning of the new block", function() {
        var accordion = createAccordion(2, {
          selectable: true
        });
        var tiles0 = createTiles(3);
        var tiles1 = createTiles(2);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[1], tiles[4]]);
        accordion.setFocusedTile(tiles[4]);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.LEFT, 'shift');
        expect(scout.arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[1], tiles[3], tiles[4]])).toBe(true);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.LEFT, 'shift');
        expect(scout.arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[1], tiles[2], tiles[3], tiles[4]])).toBe(true);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.LEFT, 'shift');
        expect(scout.arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4]])).toBe(true);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.LEFT, 'shift');
        expect(scout.arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4]])).toBe(true);
      });

    });

  });

  describe('key down', function() {
    it("selects the tile in the grid below if the selected tile is in the last line of the current group", function() {
      var accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      var tiles0 = createTiles(3);
      var tiles1 = createTiles(6);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[0]);

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[3]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.DOWN);

      accordion.groups[1].body.$container.triggerKeyDownCapture(scout.keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[6]]);
      accordion.groups[1].body.$container.triggerKeyUpCapture(scout.keys.DOWN);
    });

    it("selects the tile in the grid below if the selected tile is in the last line of the current group but only if the group is not collapsed", function() {
      var accordion = createAccordion(3, {
        selectable: true,
        gridColumnCount: 3
      });
      var tiles0 = createTiles(3);
      var tiles1 = createTiles(3);
      var tiles2 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      accordion.groups[1].setCollapsed(true);
      accordion.groups[2].body.insertTiles(tiles2);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[0]);

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[6]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.DOWN);
    });

    it("selects the first tile if no tile is selected yet", function() {
      var accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      var tiles0 = createTiles(3);
      var tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.DOWN);
    });

    it("selects the first tile if no tile is selected yet or the focused tile was in a collapsed group", function() {
      var accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      var tiles0 = createTiles(2);
      var tiles1 = createTiles(2);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.selectTile(tiles[1]);
      accordion.setFocusedTile(tiles[1]);
      accordion.render();
      accordion.validateLayout();

      $.fx.off = true;
      accordion.groups[0].setCollapsed(true);
      $.fx.off = false;
      expect(accordion.getFocusedTile()).toBe(null);
      expect(accordion.getSelectedTiles().length).toBe(0);

      accordion.groups[1].body.$container.triggerKeyDownCapture(scout.keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[2]]);
      accordion.groups[1].body.$container.triggerKeyUpCapture(scout.keys.DOWN);
    });

    it("does nothing if a tile in the last row is already selected", function() {
      var accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      var tiles0 = createTiles(3);
      var tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[3]);

      accordion.groups[1].body.$container.triggerKeyDownCapture(scout.keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[3]]);
      accordion.groups[1].body.$container.triggerKeyUpCapture(scout.keys.DOWN);

      accordion.selectTile(scout.arrays.last(tiles));
      accordion.setFocusedTile(scout.arrays.last(tiles));
      expect(accordion.getSelectedTiles()).toEqual([scout.arrays.last(tiles)]);

      accordion.groups[1].body.$container.triggerKeyDownCapture(scout.keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([scout.arrays.last(tiles)]);
      accordion.groups[1].body.$container.triggerKeyUpCapture(scout.keys.DOWN);
    });

    it("selects the only tile if there is only one", function() {
      var accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      var tiles0 = createTiles(0);
      var tiles1 = createTiles(1);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.DOWN);

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.DOWN);

      accordion.deselectAllTiles();

      accordion.groups[1].body.$container.triggerKeyDownCapture(scout.keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[1].body.$container.triggerKeyUpCapture(scout.keys.DOWN);
    });

    it("selects the last tile if below the focused tile is no tile", function() {
      var accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      var tiles0 = createTiles(4);
      var tiles1 = createTiles(4);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.selectTile(tiles[2]);
      accordion.render();
      accordion.validateLayout();

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[3]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.DOWN);

      accordion.selectTile(tiles[6]);
      accordion.setFocusedTile(tiles[6]);
      accordion.groups[1].body.$container.triggerKeyDownCapture(scout.keys.DOWN);
      expect(accordion.getSelectedTiles()).toEqual([tiles[7]]);
      accordion.groups[1].body.$container.triggerKeyUpCapture(scout.keys.DOWN);
    });

    describe('with shift', function() {
      it("adds the tiles between the focused and the newly focused tile to the selection", function() {
        var accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        var tiles0 = createTiles(5);
        var tiles1 = createTiles(5);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTile(tiles[1]);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.DOWN, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2], tiles[3], tiles[4]]);

        accordion.groups[1].body.$container.triggerKeyInputCapture(scout.keys.DOWN, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6]]);

        accordion.groups[1].body.$container.triggerKeyInputCapture(scout.keys.DOWN, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7], tiles[8], tiles[9]]);
      });

      it("removes the tiles between the focused and the newly focused tiles from the selection if the focused tile is the first tile of the selection", function() {
        var accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        var tiles0 = createTiles(5);
        var tiles1 = createTiles(5);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6]]);
        accordion.setFocusedTile(tiles[1]);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.DOWN, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[4], tiles[5], tiles[6]]);

        accordion.groups[1].body.$container.triggerKeyInputCapture(scout.keys.DOWN, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[6]]);

        accordion.groups[1].body.$container.triggerKeyInputCapture(scout.keys.DOWN, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[6], tiles[7], tiles[8], tiles[9]]);
      });

      it("does nothing if a tile in the last row is already selected", function() {
        var accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        var tiles0 = createTiles(3);
        var tiles1 = createTiles(3);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[2], tiles[3]]);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.DOWN, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[2], tiles[3]]);
      });

      it("adds the correct tile to the selection if the focused tile gets invisible", function() {
        var accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        var tiles0 = createTiles(3);
        var tiles1 = createTiles(3);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[0], tiles[1], tiles[2]]);
        accordion.setFocusedTile(tiles[0]);

        var filter = {
          accept: function(tile) {
            return tile !== tiles[0]; // Make tile 2 invisible
          }
        };
        accordion.groups[0].body.addFilter(filter);
        accordion.groups[1].body.addFilter(filter);
        accordion.groups[0].body.filter();
        accordion.groups[1].body.filter();
        accordion.validateLayout();
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2]]);
        expect(accordion.getFocusedTile()).toBe(null);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.DOWN, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2], tiles[3], tiles[4]]);
      });

      it("connects two selections blocks and sets the focused tile to the beginning of the new block", function() {
        var accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        var tiles0 = createTiles(3);
        var tiles1 = createTiles(6);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[0], tiles[1], tiles[5]]);
        accordion.setFocusedTile(tiles[1]);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.DOWN, 'shift');
        expect(scout.arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5]])).toBe(true);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.DOWN, 'shift');
        expect(scout.arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]])).toBe(true);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.DOWN, 'shift');
        expect(scout.arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]])).toBe(true);
      });
    });

  });

  describe('key up', function() {
    it("selects the tile in the grid above if the selected tile is in the first line of the current group", function() {
      var accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      var tiles0 = createTiles(3);
      var tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(scout.arrays.last(tiles));

      accordion.groups[1].body.$container.triggerKeyDownCapture(scout.keys.UP);
      expect(accordion.getSelectedTiles()).toEqual([tiles[2]]);
      accordion.groups[1].body.$container.triggerKeyUpCapture(scout.keys.UP);
    });

    it("selects the tile in the grid above if the selected tile is in the first line of the current group but only if the group is not collapsed", function() {
      var accordion = createAccordion(3, {
        selectable: true,
        gridColumnCount: 3
      });
      var tiles0 = createTiles(3);
      var tiles1 = createTiles(3);
      var tiles2 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      accordion.groups[1].setCollapsed(true);
      accordion.groups[2].body.insertTiles(tiles2);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[8]);

      accordion.groups[2].body.$container.triggerKeyDownCapture(scout.keys.UP);
      expect(accordion.getSelectedTiles()).toEqual([tiles[2]]);
      accordion.groups[2].body.$container.triggerKeyUpCapture(scout.keys.UP);
    });

    it("selects the last tile if no tile is selected yet", function() {
      var accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      var tiles0 = createTiles(3);
      var tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      accordion.groups[1].body.$container.triggerKeyDownCapture(scout.keys.UP);
      expect(accordion.getSelectedTiles()).toEqual([scout.arrays.last(tiles)]);
      accordion.groups[1].body.$container.triggerKeyUpCapture(scout.keys.UP);
    });

    it("does nothing if a tile in the first row is already selected", function() {
      var accordion = createAccordion(8, {
        selectable: true,
        gridColumnCount: 3
      });
      var tiles0 = createTiles(3);
      var tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[2]);

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.UP);
      expect(accordion.getSelectedTiles()).toEqual([tiles[2]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.UP);

      accordion.selectTile(tiles[0]);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.UP);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.UP);
    });

    it("selects the only tile if there is only one", function() {
      var accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      var tiles0 = createTiles(1);
      var tiles1 = createTiles(0);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.UP);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.UP);

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.UP);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.UP);

      accordion.deselectAllTiles();

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.UP);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.UP);
    });

    describe('with shift', function() {
      it("adds the tiles between the focused and the newly focused tile to the selection", function() {
        var accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        var tiles0 = createTiles(3);
        var tiles1 = createTiles(3);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTile(tiles[4]);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.UP, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2], tiles[3], tiles[4]]);
      });

      it("removes the tiles between the focused and the newly focused tiles from the selection if the focused tile is the first tile of the selection", function() {
        var accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        var tiles0 = createTiles(2);
        var tiles1 = createTiles(3);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[1], tiles[2], tiles[3], tiles[4]]);
        accordion.setFocusedTile(tiles[4]);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.UP, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1]]);
      });

      it("does nothing if a tile in the first row is already selected", function() {
        var accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        var tiles0 = createTiles(3);
        var tiles1 = createTiles(3);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[1], tiles[2], tiles[3]]);
        accordion.setFocusedTile(tiles[2]);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.UP, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[1], tiles[2], tiles[3]]);
      });

      it("adds the correct tile to the selection if the focused tile gets invisible", function() {
        var accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        var tiles0 = createTiles(3);
        var tiles1 = createTiles(3);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[2], tiles[3], tiles[4]]);
        accordion.setFocusedTile(tiles[2]);

        var filter = {
          accept: function(tile) {
            return tile !== tiles[2]; // Make tile 2 invisible
          }
        };
        accordion.groups[0].body.addFilter(filter);
        accordion.groups[1].body.addFilter(filter);
        accordion.groups[0].body.filter();
        accordion.groups[1].body.filter();
        accordion.validateLayout();
        expect(accordion.getSelectedTiles()).toEqual([tiles[3], tiles[4]]);
        expect(accordion.getFocusedTile()).toBe(null);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.UP, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[0], tiles[1], tiles[3], tiles[4]]);
      });

      it("connects two selections blocks and sets the focused tile to the beginning of the new block", function() {
        var accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        var tiles0 = createTiles(4);
        var tiles1 = createTiles(6);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[3], tiles[7], tiles[8]]);
        accordion.setFocusedTile(tiles[7]);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.UP, 'shift');
        expect(scout.arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[3], tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]])).toBe(true);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.UP, 'shift');
        expect(scout.arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]])).toBe(true);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.UP, 'shift');
        expect(scout.arrays.equalsIgnoreOrder(accordion.getSelectedTiles(), [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]])).toBe(true);
      });
    });
  });

  describe('home', function() {
    it("selects the first tile", function() {
      var accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      var tiles0 = createTiles(3);
      var tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[1]);

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.HOME);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.HOME);
    });

    it("selects the first tile but only if the group is not collapsed", function() {
      var accordion = createAccordion(3, {
        selectable: true,
        gridColumnCount: 3
      });
      var tiles0 = createTiles(2);
      var tiles1 = createTiles(2);
      var tiles2 = createTiles(2);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[0].setCollapsed(true);
      accordion.groups[1].body.insertTiles(tiles1);
      accordion.groups[2].body.insertTiles(tiles2);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[5]);

      accordion.groups[1].body.$container.triggerKeyDownCapture(scout.keys.HOME);
      expect(accordion.getSelectedTiles()).toEqual([tiles[2]]);
      accordion.groups[1].body.$container.triggerKeyUpCapture(scout.keys.HOME);
    });

    it("does nothing if the first tile is already selected", function() {
      var accordion = createAccordion(2, {
        selectable: true
      });
      var tiles0 = createTiles(3);
      var tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(tiles[0]);

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.HOME);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.HOME);
    });

    it("selects only the first tile if first and other tiles are selected", function() {
      var accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      var tiles0 = createTiles(3);
      var tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTiles([tiles[0], tiles[1]]);
      accordion.setFocusedTile(tiles[1]);

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.HOME);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.HOME);
    });

    it("selects the only tile if there is only one", function() {
      var accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      var tiles0 = createTiles(0);
      var tiles1 = createTiles(1);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.HOME);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.HOME);

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.HOME);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.HOME);

      accordion.deselectAllTiles();

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.HOME);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.HOME);
    });

    describe('with shift', function() {
      it("adds the tiles between the focused and the newly focused tile to the selection", function() {
        var accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        var tiles0 = createTiles(3);
        var tiles1 = createTiles(6);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[6], tiles[7]]);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.HOME, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7]]);
      });
    });

  });

  describe('end', function() {
    it("selects the last tile", function() {
      var accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      var tiles0 = createTiles(3);
      var tiles1 = createTiles(6);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.END);
      expect(accordion.getSelectedTiles()).toEqual([scout.arrays.last(tiles)]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.END);
    });

    it("selects the last tile but only if the group is not collapsed", function() {
      var accordion = createAccordion(3, {
        selectable: true,
        gridColumnCount: 3
      });
      var tiles0 = createTiles(2);
      var tiles1 = createTiles(2);
      var tiles2 = createTiles(2);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      accordion.groups[2].body.insertTiles(tiles2);
      accordion.groups[2].setCollapsed(true);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.END);
      expect(accordion.getSelectedTiles()).toEqual([tiles[3]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.END);
    });

    it("does nothing if the last tile is already selected", function() {
      var accordion = createAccordion(4, {
        selectable: true
      });
      var tiles0 = createTiles(3);
      var tiles1 = createTiles(6);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTile(scout.arrays.last(tiles));

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.END);
      expect(accordion.getSelectedTiles()).toEqual([scout.arrays.last(tiles)]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.END);
    });

    it("selects only the last tile if last and other tiles are selected", function() {
      var accordion = createAccordion(4, {
        selectable: true,
        gridColumnCount: 3
      });
      var tiles0 = createTiles(3);
      var tiles1 = createTiles(3);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();
      accordion.selectTiles([tiles[4], tiles[5]]);
      accordion.setFocusedTile(tiles[5]);

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.END);
      expect(accordion.getSelectedTiles()).toEqual([tiles[4], tiles[5]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.END);
    });

    it("selects the only tile if there is only one", function() {
      var accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      var tiles0 = createTiles(1);
      var tiles1 = createTiles(0);
      accordion.groups[0].body.insertTiles(tiles0);
      accordion.groups[1].body.insertTiles(tiles1);
      var tiles = accordion.getTiles();
      accordion.render();
      accordion.validateLayout();

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.END);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.END);

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.END);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.END);

      accordion.deselectAllTiles();

      accordion.groups[0].body.$container.triggerKeyDownCapture(scout.keys.END);
      expect(accordion.getSelectedTiles()).toEqual([tiles[0]]);
      accordion.groups[0].body.$container.triggerKeyUpCapture(scout.keys.END);
    });

    describe('with shift', function() {
      it("adds the tiles between the focused and the newly focused tile to the selection", function() {
        var accordion = createAccordion(2, {
          selectable: true,
          gridColumnCount: 3
        });
        var tiles0 = createTiles(5);
        var tiles1 = createTiles(4);
        accordion.groups[0].body.insertTiles(tiles0);
        accordion.groups[1].body.insertTiles(tiles1);
        var tiles = accordion.getTiles();
        accordion.render();
        accordion.validateLayout();
        accordion.selectTiles([tiles[4], tiles[5]]);

        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.END, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]]);

        // After pressing shift home afterwards all tiles should be selected
        accordion.groups[0].body.$container.triggerKeyInputCapture(scout.keys.HOME, 'shift');
        expect(accordion.getSelectedTiles()).toEqual([tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]]);
      });
    });

  });

});
