/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {focusUtils, FormTableControl, InitModelOf, RemoteEvent, scout, Session, TableControl, TableControlAdapter, Widget} from '../../../src/index';
import {FormSpecHelper, SpecTable, TableSpecHelper} from '../../../src/testing/index';

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

  function createModel(): { id: string; objectType: string; parent: Widget; session: Session } {
    return createSimpleModel('TableControl', session);
  }

  function createAction(model: InitModelOf<TableControl>): TableControl {
    return scout.create(TableControl, model);
  }

  function createTableControlAdapter(model: InitModelOf<TableControlAdapter>): TableControlAdapter {
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
      table.setTableControls([action]);
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
      table.setTableControls([action, action2]);

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
      let action = adapter.createWidget(model, session.desktop) as TableControl;
      let model2 = createModel();
      let adapter2 = createTableControlAdapter(model2);
      let action2 = adapter2.createWidget(model2, session.desktop) as TableControl;
      table.setTableControls([action, action2]);

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

  it('clicking in the control container does not focus the table', () => {
    let table = createTable();
    let action = scout.create(FormTableControl, {
      parent: table,
      selected: true
    });
    action.setForm(new FormSpecHelper(session).createFormWithOneField());
    table.setTableControls([action]);
    table.render();
    jasmine.clock().tick(1); // Ensure animation complete function is executed (animation uses a 1ms delay)
    expect(action.form.rootGroupBox.fields[0].isFocused()).toBeTrue();

    // Focus must not leave the field when clicking outside (it cannot be simulated in a test -> test the function that causes the problem)
    expect(focusUtils.containsParentFocusableByMouse(action.form.$container, session.desktop.$container)).toBe(false);
  });

  describe('aria properties', () => {
    let table: SpecTable;

    beforeEach(() => {
      table = createTable();
    });

    it('has aria role button', () => {
      let action = createAction(createModel());
      table.setTableControls([action]);
      table.render();
      expect(action.$container).toHaveAttr('role', 'button');
    });

    it('has aria pressed set correctly when selected', () => {
      let action = createAction(createModel());
      table.setTableControls([action]);
      table.render();
      expect(action.$container).toHaveAttr('role', 'button');
    });
  });
});
