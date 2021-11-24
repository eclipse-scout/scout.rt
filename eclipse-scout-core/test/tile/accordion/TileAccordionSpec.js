/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {scout, TileGridLayoutConfig} from '../../../src/index';

describe('TileAccordion', () => {
  let session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  afterEach(() => {
    // Stop all running animations to not influence other specs
    $(':animated').finish();
  });

  function createAccordion(numGroups, model) {
    let groups = [];
    for (let i = 0; i < numGroups; i++) {
      groups.push({
        objectType: 'Group',
        label: 'Group ' + i,
        body: {
          objectType: 'TileGrid',
          scrollable: false
        }
      });
    }
    let defaults = {
      parent: session.desktop,
      groups: groups
    };
    model = $.extend({}, defaults, model);
    return scout.create('TileAccordion', model);
  }

  function createGroup(model) {
    let defaults = {
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
    let defaults = {
      parent: session.desktop
    };
    model = $.extend({}, defaults, model);
    return scout.create('Tile', model);
  }

  describe('init', () => {
    it('copies properties to tile grids', () => {
      let comparator = () => true;
      let filter = {
        accept: () => true
      };
      let accordion = createAccordion(0, {
        selectable: true,
        multiSelect: false,
        tileGridLayoutConfig: {
          columnWidth: 100,
          rowHeight: 100
        },
        tileComparator: comparator,
        filters: [filter],
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
      expect(accordion.groups[0].body.layoutConfig).toEqual(TileGridLayoutConfig.ensure({
        columnWidth: 100,
        rowHeight: 100
      }));
      expect(accordion.groups[0].body.comparator).toBe(comparator);
      expect(accordion.groups[0].body.filters).toEqual([filter]);
      expect(accordion.groups[0].body.gridColumnCount).toBe(2);
      expect(accordion.groups[0].body.withPlaceholders).toBe(true);
    });

    it('does not override properties which are specified by the tile grid itself', () => {
      let comparator = () => true;
      let filter = {
        accept: () => true
      };
      let accordion = createAccordion(0);
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
          comparator: comparator,
          filters: [filter],
          gridColumnCount: 2,
          withPlaceholders: true
        }
      });
      expect(accordion.groups[0].body.selectable).toBe(accordion.selectable);
      expect(accordion.groups[0].body.multiSelect).toBe(accordion.multiSelect);
      expect(accordion.groups[0].body.layoutConfig).toEqual(accordion.tileGridLayoutConfig);
      expect(accordion.groups[0].body.comparator).toEqual(accordion.tileComparator);
      expect(accordion.groups[0].body.filters).toEqual(accordion.filters);
      expect(accordion.groups[0].body.gridColumnCount).toBe(accordion.gridColumnCount);
      expect(accordion.groups[0].body.withPlaceholders).toBe(accordion.withPlaceholders);
    });
  });

  describe('setters', () => {
    it('copy properties to tile grids', () => {
      let accordion = createAccordion(2);

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

  describe('addFilter', () => {
    it('adds the filter to every existing tile grid', () => {
      let accordion = createAccordion(2);
      let tile0 = createTile();
      let tile1 = createTile();
      let tile2 = createTile();
      let tile3 = createTile();
      accordion.groups[0].body.insertTiles([tile0, tile1]);
      accordion.groups[1].body.insertTiles([tile2, tile3]);
      let filter = {
        accept: tile => tile === tile1 || tile === tile3
      };
      let filter2 = {
        accept: tile => false
      };
      expect(accordion.filters).toEqual([]);
      accordion.addFilter(filter);
      expect(accordion.filters).toEqual([filter]);
      expect(accordion.groups[0].body.filters).toEqual([filter]);
      expect(accordion.groups[0].body.filteredTiles).toEqual([tile1]);
      expect(accordion.groups[1].body.filters).toEqual([filter]);
      expect(accordion.groups[1].body.filteredTiles).toEqual([tile3]);

      accordion.addFilter(filter2);
      expect(accordion.filters).toEqual([filter, filter2]);
      expect(accordion.groups[0].body.filters).toEqual([filter, filter2]);
      expect(accordion.groups[0].body.filteredTiles).toEqual([]);
      expect(accordion.groups[1].body.filters).toEqual([filter, filter2]);
      expect(accordion.groups[1].body.filteredTiles).toEqual([]);
    });

    it('adds the filter to future tile grids', () => {
      let accordion = createAccordion(1);
      let tile0 = createTile();
      let tile1 = createTile();
      let tile2 = createTile();
      let tile3 = createTile();
      accordion.groups[0].body.insertTiles([tile0, tile1]);
      let filter = {
        accept: tile => tile === tile1 || tile === tile3
      };
      let filter2 = {
        accept: tile => true
      };
      expect(accordion.filters).toEqual([]);
      accordion.addFilter(filter);
      accordion.addFilter(filter2);
      expect(accordion.filters).toEqual([filter, filter2]);
      expect(accordion.groups[0].body.filters).toEqual([filter, filter2]);
      expect(accordion.groups[0].body.filteredTiles).toEqual([tile1]);

      accordion.insertGroup({
        objectType: 'Group',
        body: {
          objectType: 'TileGrid',
          tiles: [tile2, tile3]
        }
      });
      expect(accordion.filters).toEqual([filter, filter2]);
      expect(accordion.groups[1].body.filters).toEqual([filter, filter2]);
      expect(accordion.groups[1].body.filteredTiles).toEqual([tile3]);
    });
  });

  describe('removeFilter', () => {
    it('removes the filter to every existing tile grid', () => {
      let accordion = createAccordion(2);
      let tile0 = createTile();
      let tile1 = createTile();
      let tile2 = createTile();
      let tile3 = createTile();
      accordion.groups[0].body.insertTiles([tile0, tile1]);
      accordion.groups[1].body.insertTiles([tile2, tile3]);
      let filter = {
        accept: tile => tile === tile1 || tile === tile3
      };
      let filter2 = {
        accept: tile => false
      };
      accordion.setFilters([filter, filter2]);
      expect(accordion.filters).toEqual([filter, filter2]);

      accordion.removeFilter(filter);
      expect(accordion.filters).toEqual([filter2]);
      expect(accordion.groups[0].body.filters).toEqual([filter2]);
      expect(accordion.groups[0].body.filteredTiles).toEqual([]);
      expect(accordion.groups[1].body.filters).toEqual([filter2]);
      expect(accordion.groups[1].body.filteredTiles).toEqual([]);

      accordion.removeFilter(filter2);
      expect(accordion.filters).toEqual([]);
      expect(accordion.groups[0].body.filters).toEqual([]);
      expect(accordion.groups[0].body.filteredTiles).toEqual([tile0, tile1]);
      expect(accordion.groups[1].body.filters).toEqual([]);
      expect(accordion.groups[1].body.filteredTiles).toEqual([tile2, tile3]);
    });

    it('makes sure the filter is not added to future tile grids', () => {
      let accordion = createAccordion(1);
      let tile0 = createTile();
      let tile1 = createTile();
      let tile2 = createTile();
      let tile3 = createTile();
      accordion.groups[0].body.insertTiles([tile0, tile1]);
      let filter = {
        accept: tile => tile === tile1 || tile === tile3
      };
      let filter2 = {
        accept: tile => false
      };
      accordion.setFilters([filter, filter2]);
      expect(accordion.filters).toEqual([filter, filter2]);

      accordion.removeFilter(filter);
      expect(accordion.filters).toEqual([filter2]);
      expect(accordion.groups[0].body.filters).toEqual([filter2]);
      expect(accordion.groups[0].body.filteredTiles).toEqual([]);

      accordion.insertGroup({
        objectType: 'Group',
        body: {
          objectType: 'TileGrid',
          tiles: [tile2, tile3]
        }
      });
      expect(accordion.filters).toEqual([filter2]);
      expect(accordion.groups[1].body.filters).toEqual([filter2]);
      expect(accordion.groups[1].body.filteredTiles).toEqual([]);
    });
  });

  describe('click', () => {
    it('triggers tileClick', () => {
      let accordion = createAccordion(3, {
        selectable: false,
        multiSelect: false
      });
      let tile0 = createTile();
      let tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.groups[1].body.insertTile(tile1);
      accordion.render();
      let clickEventCount = 0;
      accordion.on('tileClick', event => {
        if (event.tile === tile0 && event.mouseButton === 1) {
          clickEventCount++;
        }
      });
      expect(tile0.selected).toBe(false);
      expect(clickEventCount).toBe(0);

      tile0.$container.triggerClick();
      expect(tile0.selected).toBe(false);
      expect(clickEventCount).toBe(1);
    });

    it('triggers tileSelected and tileClick if selectable', () => {
      let accordion = createAccordion(3, {
        selectable: true,
        multiSelect: false
      });
      let tile0 = createTile();
      let tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.groups[1].body.insertTile(tile1);
      accordion.render();
      let clickEventCount = 0;
      let selectEventCount = 0;
      let events = [];
      accordion.on('propertyChange', event => {
        if (event.propertyName === 'selectedTiles') {
          selectEventCount++;
        }
        events.push('select');
      });
      accordion.on('tileClick', event => {
        if (event.tile === tile0 && event.mouseButton === 1) {
          clickEventCount++;
        }
        events.push('click');
      });
      expect(tile0.selected).toBe(false);
      expect(selectEventCount).toBe(0);
      expect(clickEventCount).toBe(0);

      tile0.$container.triggerClick();
      expect(tile0.selected).toBe(true);
      expect(selectEventCount).toBe(1);
      expect(clickEventCount).toBe(1);
      expect(events.length).toBe(2);
      expect(events[0]).toBe('select');
      expect(events[1]).toBe('click');
    });

    it('triggers tileAction when clicked twice', () => {
      let accordion = createAccordion(3, {
        selectable: true,
        multiSelect: false
      });
      let tile0 = createTile();
      let tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.groups[1].body.insertTile(tile1);
      accordion.render();
      let selectEventCount = 0;
      let clickEventCount = 0;
      let actionEventCount = 0;
      let events = [];
      accordion.on('propertyChange', event => {
        if (event.propertyName === 'selectedTiles') {
          selectEventCount++;
        }
        events.push('select');
      });
      accordion.on('tileClick', event => {
        if (event.tile === tile0) {
          clickEventCount++;
        }
        events.push('click');
      });
      accordion.on('tileAction', event => {
        if (event.tile === tile0) {
          actionEventCount++;
        }
        events.push('action');
      });
      expect(tile0.selected).toBe(false);
      expect(selectEventCount).toBe(0);
      expect(clickEventCount).toBe(0);
      expect(actionEventCount).toBe(0);

      tile0.$container.triggerDoubleClick();
      expect(tile0.selected).toBe(true);
      expect(selectEventCount).toBe(1);
      expect(clickEventCount).toBe(1);
      expect(actionEventCount).toBe(1);
      expect(events.length).toBe(3);
      expect(events[0]).toBe('select');
      expect(events[1]).toBe('click');
      expect(events[2]).toBe('action');
    });

    it('is not delegated anymore if group is deleted without being destroyed', () => {
      // This is a theoretical proof of concept without any known practical use cases
      let accordion = createAccordion(3, {
        selectable: false,
        multiSelect: false
      });
      let tile0 = createTile();
      let tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.groups[1].body.insertTile(tile1);
      accordion.render();
      let clickEventCount = 0;
      accordion.on('tileClick', event => {
        if (event.tile === tile0 && event.mouseButton === 1) {
          clickEventCount++;
        }
      });
      expect(clickEventCount).toBe(0);

      // Use desktop as owner to prevent destruction
      let group0 = accordion.groups[0];
      group0.setOwner(session.desktop);
      accordion.deleteGroup(group0);
      expect(group0.destroyed).toBe(false);
      expect(group0.rendered).toBe(false);

      // Move to another accordion
      let accordion2 = createAccordion(0);
      accordion2.insertGroup(group0);
      accordion2.render();
      let clickEventCount2 = 0;
      accordion2.on('tileClick', event => {
        if (event.tile === tile0 && event.mouseButton === 1) {
          clickEventCount2++;
        }
      });
      expect(group0.rendered).toBe(true);

      // First accordion must not delegate anymore but second has to
      tile0.$container.triggerClick();
      expect(clickEventCount).toBe(0);
      expect(clickEventCount2).toBe(1);
    });
  });

  describe('selectTiles', () => {
    it('selects one of the given tiles and unselects the previously selected ones', () => {
      let accordion = createAccordion(3, {
        selectable: true,
        multiSelect: false
      });
      let tile0 = createTile();
      let tile1 = createTile();
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

    it('selects all the given tiles and unselects the previously selected ones if multiSelect is true', () => {
      let accordion = createAccordion(3, {
        selectable: true,
        multiSelect: true
      });
      let tile0 = createTile();
      let tile1 = createTile();
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

    it('triggers a property change event', () => {
      let accordion = createAccordion(3, {
        selectable: true,
        multiSelect: false
      });
      let tile0 = createTile();
      let tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.groups[1].body.insertTile(tile1);
      expect(accordion.getSelectedTileCount()).toBe(0);
      let eventTriggered = false;
      let selectedTiles = [];
      accordion.on('propertyChange', event => {
        if (event.propertyName === 'selectedTiles') {
          eventTriggered = true;
          selectedTiles = accordion.getSelectedTiles();
        }
      });
      accordion.selectTiles([tile0, tile1]);
      expect(eventTriggered).toBe(true);
      expect(selectedTiles.length).toBe(1);
      expect(selectedTiles[0]).toBe(tile1);
    });

    it('triggers a property change event also if multiSelect is true', () => {
      let accordion = createAccordion(3, {
        selectable: true,
        multiSelect: true
      });
      let tile0 = createTile();
      let tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.groups[1].body.insertTile(tile1);
      expect(accordion.getSelectedTileCount()).toBe(0);
      let eventTriggered = false;
      let selectedTiles = [];
      accordion.on('propertyChange', event => {
        if (event.propertyName === 'selectedTiles') {
          eventTriggered = true;
          selectedTiles = accordion.getSelectedTiles();
        }
      });
      accordion.selectTiles([tile0, tile1]);
      expect(eventTriggered).toBe(true);
      expect(selectedTiles.length).toBe(2);
      expect(selectedTiles[0]).toBe(tile0);
      expect(selectedTiles[1]).toBe(tile1);
    });

    it('does not select tiles in a collapsed group', () => {
      let accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      let tile0 = createTile();
      let tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.groups[0].setCollapsed(true);
      accordion.groups[1].body.insertTile(tile1);
      accordion.render();

      accordion.selectTile(tile0);
      expect(accordion.groups[0].body.selectedTiles.length).toBe(0);
      expect(accordion.getSelectedTiles().length).toBe(0);
      expect(tile0.selected).toBe(false);

      accordion.groups[0].setCollapsed(false);
      accordion.selectTile(tile0);
      expect(accordion.groups[0].body.selectedTiles.length).toBe(1);
      expect(accordion.getSelectedTiles().length).toBe(1);
      expect(tile0.selected).toBe(true);
    });
  });

  describe('insertGroups', () => {
    it('triggers property change events for tiles inserted by the new group', () => {
      let accordion = createAccordion(0);
      let tile0 = createTile();
      let group0 = createGroup();
      group0.body.insertTile(tile0);
      let tileEventTriggered = false;
      let filteredTileEventTriggered = false;
      let tileCount = 0;
      let filteredTileCount = 0;
      accordion.on('propertyChange', event => {
        if (event.propertyName === 'tiles') {
          tileEventTriggered = true;
          tileCount = accordion.getTileCount();
        } else if (event.propertyName === 'filteredTiles') {
          filteredTileEventTriggered = true;
          filteredTileCount = accordion.getFilteredTileCount();
        }
      });
      expect(accordion.getTileCount()).toBe(0);
      expect(accordion.getFilteredTileCount()).toBe(0);

      accordion.insertGroups(group0);
      expect(tileEventTriggered).toBe(true);
      expect(filteredTileEventTriggered).toBe(true);
      expect(tileCount).toBe(1);
      expect(filteredTileCount).toBe(1);
      expect(accordion.getTileCount()).toBe(1);
      expect(accordion.getFilteredTileCount()).toBe(1);
    });

    it('adjusts selection if new grid contains selected tiles', () => {
      let accordion = createAccordion(1, {
        selectable: true,
        multiSelect: false
      });
      let tile0 = createTile();
      let tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.selectTile(tile0);

      let group1 = createGroup();
      group1.body.setSelectable(true);
      group1.body.insertTile(tile1);
      group1.body.selectTile(tile1);
      expect(accordion.getTileCount()).toBe(1);
      expect(accordion.getSelectedTileCount()).toBe(1);
      expect(accordion.getSelectedTiles()[0]).toBe(tile0);

      accordion.insertGroups(group1);
      expect(accordion.getTileCount()).toBe(2);
      expect(accordion.getSelectedTileCount()).toBe(1);
      expect(accordion.getSelectedTiles()[0]).toBe(tile1);
    });

    it('triggers a property change event for the new selection if new grid contains selected tiles', () => {
      let accordion = createAccordion(0, {
        selectable: true,
        multiSelect: false
      });
      let tile0 = createTile();
      let tile1 = createTile();
      let group0 = createGroup();
      group0.body.setSelectable(true);
      group0.body.insertTile(tile0);

      let group1 = createGroup();
      group1.body.setSelectable(true);
      group1.body.insertTile(tile1);
      group1.body.selectTile(tile1);

      let eventTriggered = false;
      let selectedTileCount = 0;
      accordion.on('propertyChange', event => {
        if (event.propertyName === 'selectedTiles') {
          eventTriggered = true;
          selectedTileCount = accordion.getSelectedTileCount();
        }
      });

      accordion.insertGroups(group0);
      expect(accordion.getTileCount()).toBe(1);
      expect(accordion.getSelectedTileCount()).toBe(0);
      expect(eventTriggered).toBe(false);
      expect(selectedTileCount).toBe(0);

      accordion.insertGroups(group1);
      expect(accordion.getTileCount()).toBe(2);
      expect(accordion.getSelectedTileCount()).toBe(1);
      expect(accordion.getSelectedTiles()[0]).toBe(tile1);
      expect(eventTriggered).toBe(true);
      expect(selectedTileCount).toBe(1);
    });
  });

  describe('deleteGroups', () => {
    it('triggers a property change event for tiles of the deleted group', () => {
      let accordion = createAccordion(1);
      let tile0 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      let tileEventTriggered = false;
      let filteredTileEventTriggered = false;
      let tileCount = 0;
      let filteredTileCount = 0;
      accordion.on('propertyChange', event => {
        if (event.propertyName === 'tiles') {
          tileEventTriggered = true;
          tileCount = accordion.getTileCount();
        } else if (event.propertyName === 'filteredTiles') {
          filteredTileEventTriggered = true;
          filteredTileCount = accordion.getFilteredTileCount();
        }
      });
      expect(accordion.getTileCount()).toBe(1);
      expect(accordion.getFilteredTileCount()).toBe(1);

      accordion.deleteGroups(accordion.groups[0]);
      expect(tileEventTriggered).toBe(true);
      expect(filteredTileEventTriggered).toBe(true);
      expect(tileCount).toBe(0);
      expect(filteredTileCount).toBe(0);
      expect(accordion.getTileCount()).toBe(0);
      expect(accordion.getFilteredTileCount()).toBe(0);
    });
  });
});
