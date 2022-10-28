/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ModelAdapterModel, RemoteEvent, scout, Session, TableControl, TableControlAdapter, TableControlModel, Widget} from '../../../src/index';
import {TableSpecHelper} from '../../../src/testing/index';
import {ObjectType} from '../../../src/ObjectFactory';
import SpecTable from '../../../src/testing/table/SpecTable';

describe('TableControl', () => {
  let session: SandboxSession;
  let tableHelper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    tableHelper = new TableSpecHelper(session);

    $.fx.off = true; // Open and closing of the container is animated -> disable animation in order to be able to test it
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    $.fx.off = false;
  });

  function createModel(): {
    id: string; objectType: ObjectType<TableControl>; parent: Widget; session: Session;
  } {
    return createSimpleModel('TableControl', session);
  }

  function createAction(model: TableControlModel): TableControl {
    return scout.create(TableControl, model);
  }

  function createTableControlAdapter(model: ModelAdapterModel): TableControlAdapter {
    let action = new TableControlAdapter();
    action.init(model);
    return action;
  }

  function createTable(): SpecTable {
    let tableModel = tableHelper.createModelFixture(2);
    return tableHelper.createTable(tableModel);
  }

  describe('selected', () => {
    let table: SpecTable;

    beforeEach(() => {
      table = createTable();
    });

    it('opens and closes the control container', () => {
      let action = createAction(createModel());
      table._setTableControls([action]);
      table.render();
      let $controlContainer = table.footer.$controlContainer;
      expect($controlContainer).toBeHidden();

      action.setSelected(true);
      expect($controlContainer).toBeVisible();

      action.setSelected(false);
      $controlContainer.stop(true, true); // immediately end closing animation to make toBeHidden() reliable
      expect($controlContainer).toBeHidden();
    });

    it('removes the content of the previous selected control without closing the container', () => {
      let action = createAction(createModel());
      let action2 = createAction(createModel());
      table._setTableControls([action, action2]);

      action.selected = true;
      table.render();
      let $controlContainer = table.footer.$controlContainer;

      expect($controlContainer).toBeVisible();
      expect(action.contentRendered).toBe(true);
      expect(action2.contentRendered).toBe(false);

      action2.setSelected(true);
      expect($controlContainer).toBeVisible();
      expect(action2.contentRendered).toBe(true);
      expect(action2.selected).toBe(true);
      expect(action2.contentRendered).toBe(true);
      expect(action2.selected).toBe(true);

      action.setSelected(false);
      expect($controlContainer).toBeVisible();
      expect(action.contentRendered).toBe(false);
      expect(action.selected).toBe(false);
    });

    it('sends selected events (for current and previous selection)', () => {
      let model = createModel();
      let adapter = createTableControlAdapter(model);
      let action = adapter.createWidget(model, session.desktop);
      let model2 = createModel();
      let adapter2 = createTableControlAdapter(model2);
      let action2 = adapter2.createWidget(model2, session.desktop);
      table._setTableControls([action, action2]);

      action.selected = true;
      table.render();

      action2.setSelected(true);
      sendQueuedAjaxCalls();
      let events = [
        new RemoteEvent(action.id, 'property', {
          selected: false
        }),
        new RemoteEvent(action2.id, 'property', {
          selected: true
        })
      ];
      expect(mostRecentJsonRequest()).toContainEvents(events);
    });
  });
});
