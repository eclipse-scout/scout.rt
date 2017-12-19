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
describe("TileAccordion", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  function createAccordion(numGroups, model) {
    var groups = [];
    for (var i = 0; i < numGroups; i++) {
      groups.push({
        objectType: 'Group',
        label: "Group " + i,
        body: {
          objectType: 'TileGrid',
          scrollable: false
        }
      });
    }
    var defaults = {
      parent: session.desktop,
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

  describe('init', function() {
    it('copies properties to tile grids', function() {
      var accordion = createAccordion(0, {
        selectable: true,
        multiSelect: false,
        tileGridLayoutConfig: {
          columnWidth: 100,
          rowHeight: 100
        },
        gridColumnCount: 2,
        withPlaceholders: true
      });
      accordion.insertGroup({
        objectType: 'Group',
        body: {
          objectType: 'TileGrid'
        }
      });
      expect(accordion.groups[0].body.selectable).toBe(true);
      expect(accordion.groups[0].body.multiSelect).toBe(false);
      expect(accordion.groups[0].body.layoutConfig).toEqual(scout.TileGridLayoutConfig.ensure({
        columnWidth: 100,
        rowHeight: 100
      }));
      expect(accordion.groups[0].body.gridColumnCount).toBe(2);
      expect(accordion.groups[0].body.withPlaceholders).toBe(true);
    });

    it('does not override properties which are specified by the tile grid itself', function() {
      var accordion = createAccordion(0);
      accordion.insertGroup({
        objectType: 'Group',
        body: {
          objectType: 'TileGrid',
          selectable: true,
          multiSelect: false,
          layoutConfig: {
            columnWidth: 100,
            rowHeight: 100
          },
          gridColumnCount: 2,
          withPlaceholders: true
        }
      });
      expect(accordion.groups[0].body.selectable).toBe(accordion.selectable);
      expect(accordion.groups[0].body.multiSelect).toBe(accordion.multiSelect);
      expect(accordion.groups[0].body.layoutConfig).toEqual(accordion.tileGridLayoutConfig);
      expect(accordion.groups[0].body.gridColumnCount).toBe(accordion.gridColumnCount);
      expect(accordion.groups[0].body.withPlaceholders).toBe(accordion.withPlaceholders);
    });
  });

  describe('setters', function() {
    it('copy properties to tile grids', function() {
      var accordion = createAccordion(2);

      expect(accordion.selectable).toBe(false);
      accordion.setSelectable(true);
      expect(accordion.selectable).toBe(true);
      expect(accordion.groups[0].body.selectable).toBe(true);
      expect(accordion.groups[1].body.selectable).toBe(true);

      expect(accordion.multiSelect).toBe(true);
      accordion.setMultiSelect(false);
      expect(accordion.multiSelect).toBe(false);
      expect(accordion.groups[0].body.multiSelect).toBe(false);
      expect(accordion.groups[1].body.multiSelect).toBe(false);
    });
  });

  describe('selectTiles', function() {
    it('selects one of the given tiles and unselects the previously selected ones', function() {
      var accordion = createAccordion(3, {
        selectable: true,
        multiSelect: false
      });
      var tile0 = createTile();
      var tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.groups[1].body.insertTile(tile1);
      expect(accordion.getSelectedTileCount()).toBe(0);

      accordion.selectTiles(tile0);
      expect(accordion.getSelectedTileCount()).toBe(1);
      expect(accordion.getSelectedTile()).toBe(tile0);

      accordion.selectTile(tile1);
      expect(accordion.getSelectedTileCount()).toBe(1);
      expect(accordion.getSelectedTile()).toBe(tile1);

      accordion.selectTiles([tile0, tile1]);
      expect(accordion.getSelectedTileCount()).toBe(1);
      expect(accordion.getSelectedTile()).toBe(tile1);
    });

    it('selects all the given tiles and unselects the previously selected ones if multiSelect is true', function() {
      var accordion = createAccordion(3, {
        selectable: true,
        multiSelect: true
      });
      var tile0 = createTile();
      var tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.groups[1].body.insertTile(tile1);
      expect(accordion.getSelectedTileCount()).toBe(0);

      accordion.selectTiles(tile0);
      expect(accordion.getSelectedTileCount()).toBe(1);
      expect(accordion.getSelectedTile()).toBe(tile0);

      accordion.selectTile(tile1);
      expect(accordion.getSelectedTileCount()).toBe(1);
      expect(accordion.getSelectedTile()).toBe(tile1);

      accordion.selectTiles([tile0, tile1]);
      expect(accordion.getSelectedTileCount()).toBe(2);
      expect(accordion.getSelectedTiles()[0]).toBe(tile0);
      expect(accordion.getSelectedTiles()[1]).toBe(tile1);
    });

    it('triggers a property change event', function() {
      var accordion = createAccordion(3, {
        selectable: true,
        multiSelect: false
      });
      var tile0 = createTile();
      var tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.groups[1].body.insertTile(tile1);
      expect(accordion.getSelectedTileCount()).toBe(0);
      var eventTriggered = false;
      var selectedTiles = [];
      accordion.on('propertyChange', function(event) {
        if (event.propertyName === 'selectedTiles') {
          eventTriggered = true;
          selectedTiles = event.newValue;
        }
      });
      accordion.selectTiles([tile0, tile1]);
      expect(eventTriggered).toBe(true);
      expect(selectedTiles.length).toBe(1);
      expect(selectedTiles[0]).toBe(tile1);
    });

    it('triggers a property change event also if multiSelect is true', function() {
      var accordion = createAccordion(3, {
        selectable: true,
        multiSelect: true
      });
      var tile0 = createTile();
      var tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.groups[1].body.insertTile(tile1);
      expect(accordion.getSelectedTileCount()).toBe(0);
      var eventTriggered = false;
      var selectedTiles = [];
      accordion.on('propertyChange', function(event) {
        if (event.propertyName === 'selectedTiles') {
          eventTriggered = true;
          selectedTiles = event.newValue;
        }
      });
      accordion.selectTiles([tile0, tile1]);
      expect(eventTriggered).toBe(true);
      expect(selectedTiles.length).toBe(2);
      expect(selectedTiles[0]).toBe(tile0);
      expect(selectedTiles[1]).toBe(tile1);
    });
  });
});
