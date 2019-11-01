/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {scout} from '../../../src/index';


describe("TileAccordionAdapter", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createTileAccordion(model) {
    var defaults = {
      parent: session.desktop,
      session: session,
      objectType: 'TileAccordion'
    };
    model = $.extend({}, defaults, model);
    var tileAccordionAdapter = session.createModelAdapter(model);
    return tileAccordionAdapter.createWidget(model, model.parent);
  }

  describe("initProperties", function() {

    it("does not take filters from tile grid in remote case", function() {
      var filter = scout.create('RemoteTileFilter');
      var accordion = createTileAccordion({
        groups: [{
          objectType: 'Group',
          body: {
            objectType: 'TileGrid',
            filters: [filter]
          }
        }]
      });
      expect(accordion.takeTileFiltersFromGroup).toBe(false);
      expect(accordion.tileFilters.length).toBe(0);
      expect(accordion.groups[0].body.filters.length).toBe(1);
      expect(accordion.groups[0].body.filters[0]).toBe(filter);
    });

  });

});
