/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Group, RemoteTileFilter, scout, TileAccordion, TileGrid} from '../../../src/index';

describe('TileAccordionAdapter', () => {
  let session: SandboxSession;

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

  function createTileAccordion(model): TileAccordion {
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
      let filter = scout.create(RemoteTileFilter);
      let accordion = createTileAccordion({
        groups: [{
          objectType: Group,
          body: {
            objectType: TileGrid,
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
