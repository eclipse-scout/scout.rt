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
import {scout} from '../../../src/index';

describe('TileAccordionAdapter', () => {
  let session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createTileAccordion(model) {
    let defaults = {
      parent: session.desktop,
      session: session,
      objectType: 'TileAccordion'
    };
    model = $.extend({}, defaults, model);
    let tileAccordionAdapter = session.createModelAdapter(model);
    return tileAccordionAdapter.createWidget(model, model.parent);
  }

  describe('initProperties', () => {

    it('does not take filters from tile grid in remote case', () => {
      let filter = scout.create('RemoteTileFilter');
      let accordion = createTileAccordion({
        groups: [{
          objectType: 'Group',
          body: {
            objectType: 'TileGrid',
            filters: [filter]
          }
        }]
      });
      expect(accordion.takeTileFiltersFromGroup).toBe(false);
      expect(accordion.filters.length).toBe(0);
      expect(accordion.groups[0].body.filters.length).toBe(1);
      expect(accordion.groups[0].body.filters[0]).toBe(filter);
    });

  });

});
