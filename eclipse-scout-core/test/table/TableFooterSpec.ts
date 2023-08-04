/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {scout, Status, TableControl} from '../../src/index';
import {JQueryTesting, TableSpecHelper} from '../../src/testing/index';
import $ from 'jquery';

describe('TableFooterSpec', () => {
  let session: SandboxSession;
  let helper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createTableControl(): TableControl {
    return scout.create(TableControl, {
      parent: session.desktop
    });
  }

  describe('render', () => {

    it('attaches listener to the table but only once', () => {
      let model = helper.createModelFixture(2);
      model.tableStatusVisible = true;
      let table = helper.createTable(model);
      table.render();
      expect(table.footer).not.toBeUndefined();
      let listenerCount = table.events._eventListeners.length;

      table.setTableStatusVisible(false);
      table.setTableStatusVisible(true);

      // Still same amount of listeners expected after footer visibility changed
      expect(table.events._eventListeners.length).toBe(listenerCount);
    });

  });

  describe('remove', () => {

    it('stops the open animation of the selected control', () => {
      let model = helper.createModelFixture(2);
      let table = helper.createTable(model);
      let control = createTableControl();
      table.setTableControls([control]);
      control.setSelected(true);
      table.render();
      expect(table.footer.animating).toBe(true);
      expect(table.footer.open).toBe(true);

      // Remove before open animation has been finished
      table.remove();
      expect(table.footer.animating).toBe(false);
      expect(table.footer.open).toBe(false);

      // Expect that it may be opened again
      table.render();
      expect(table.footer.animating).toBe(true);
      expect(table.footer.open).toBe(true);
    });

    it('stops the close animation of the selected control', () => {
      let model = helper.createModelFixture(2);
      let table = helper.createTable(model);
      let control = createTableControl();
      table.setTableControls([control]);
      control.setSelected(true);
      table.render();
      expect(table.footer.animating).toBe(true);
      expect(table.footer.open).toBe(true);
      // Give some time to open the container
      jasmine.clock().tick(500);

      // Start close animation
      control.setSelected(false);
      expect(table.footer.animating).toBe(true);
      expect(table.footer.open).toBe(false);

      // Remove before close animation has been finished
      table.remove();
      expect(table.footer.animating).toBe(false);
      expect(table.footer.open).toBe(false);

      // Expect that it is still closed after re-rendering
      table.render();
      expect(table.footer.animating).toBe(false);
      expect(table.footer.open).toBe(false);
    });

  });

  describe('controls', () => {

    it('removes old and renders new controls on property change', () => {
      let model = helper.createModelFixture(2);
      model.tableStatusVisible = true;
      let table = helper.createTable(model);
      table.render();

      let controls = [createTableControl()];
      table.setTableControls(controls);
      expect(controls[0].rendered).toBe(true);

      let newControls = [createTableControl(), createTableControl()];
      table.setTableControls(newControls);
      expect(controls[0].rendered).toBe(false);
      expect(newControls[0].rendered).toBe(true);
      expect(newControls[0].rendered).toBe(true);
    });
  });

  describe('TableStatusTooltip', () => {

    it('shows ERROR tooltip', () => {
      let model = helper.createModelFixture(2);
      model.tableStatusVisible = true;
      model.tableStatus = {
        severity: Status.Severity.ERROR,
        message: 'Table has an error'
      };
      let table = helper.createTable(model);

      // Check that status and tooltip is rendered when table is rendered
      table.render();
      expect(table.footer._tableStatusTooltip.rendered).toBe(true);
      expect(table.footer._tableStatusTooltip.$container.hasClass('error')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('error')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(true);
      expect(table.tableStatus.uiState).toBe(undefined);

      // Check that status and tooltip are re-rendered when table is removed and rendered again
      table.remove();
      expect(table.footer._tableStatusTooltip).toBe(null);
      table.render();
      expect(table.footer._tableStatusTooltip.rendered).toBe(true);
      expect(table.footer._tableStatusTooltip.$container.hasClass('error')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('error')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(true);

      // Check that tooltip is hidden after mouse click on status
      JQueryTesting.triggerMouseDownCapture(table.footer._$infoTableStatusIcon);
      expect(table.footer._tableStatusTooltip).toBe(null);
      expect(table.footer._$infoTableStatus.hasClass('error')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(false);
      expect(table.tableStatus.uiState).toBe('user-hidden');

      // Check that tooltip stays hidden when table is removed and rendered again
      table.remove();
      table.render();
      expect(table.footer._tableStatusTooltip).toBe(null);
      expect(table.footer._$infoTableStatus.hasClass('error')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(false);
      expect(table.tableStatus.uiState).toBe('user-hidden');

      // Check that tooltip is shown after second mouse click on status
      JQueryTesting.triggerMouseDownCapture(table.footer._$infoTableStatusIcon);
      expect(table.footer._tableStatusTooltip.rendered).toBe(true);
      expect(table.footer._tableStatusTooltip.$container.hasClass('error')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('error')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(true);
      expect(table.tableStatus.uiState).toBe('user-shown');

      // Check that tooltip stays visible when table is removed and rendered again
      table.remove();
      table.render();
      expect(table.footer._tableStatusTooltip.rendered).toBe(true);
      expect(table.footer._tableStatusTooltip.$container.hasClass('error')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('error')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(true);
      expect(table.tableStatus.uiState).toBe('user-shown');
    });

    it('shows INFO tooltip when table is rendered', () => {
      let model = helper.createModelFixture(2);
      model.tableStatusVisible = true;
      model.tableStatus = {
        severity: Status.Severity.INFO,
        message: 'Table has an information'
      };
      let table = helper.createTable(model);

      // Check that status and tooltip is rendered when table is rendered
      table.render();
      expect(table.footer._tableStatusTooltip.rendered).toBe(true);
      expect(table.footer._tableStatusTooltip.$container.hasClass('info')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('info')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(true);
      expect(table.tableStatus.uiState).toBe('auto-hidden'); // because auto-removal is already scheduled at INFO level

      // Check that tooltip is hidden automatically after 5s
      jasmine.clock().tick(5100);
      expect(table.footer._tableStatusTooltip).toBe(null);
      expect(table.footer._$infoTableStatus.hasClass('info')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(false);
      expect(table.tableStatus.uiState).toBe('auto-hidden');

      // Check that tooltip is not rendered automatically when table is removed and rendered again
      table.remove();
      table.render();
      expect(table.footer._tableStatusTooltip).toBe(null);
      expect(table.footer._$infoTableStatus.hasClass('info')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(false);
      expect(table.tableStatus.uiState).toBe('auto-hidden');

      // Check that tooltip is shown again with a mouse click and _not_ hidden automatically again after 5s
      JQueryTesting.triggerMouseDownCapture(table.footer._$infoTableStatusIcon);
      expect(table.footer._tableStatusTooltip.rendered).toBe(true);
      expect(table.footer._tableStatusTooltip.$container.hasClass('info')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('info')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(true);
      expect(table.tableStatus.uiState).toBe('user-shown');
      jasmine.clock().tick(5100);
      expect(table.footer._tableStatusTooltip.rendered).toBe(true);
      expect(table.footer._tableStatusTooltip.$container.hasClass('info')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('info')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(true);
      expect(table.tableStatus.uiState).toBe('user-shown');

      // Check that tooltip is not rendered automatically when table is removed and rendered again
      table.remove();
      table.render();
      expect(table.footer._tableStatusTooltip).toBe(null);
      expect(table.footer._$infoTableStatus.hasClass('info')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(false);
      expect(table.tableStatus.uiState).toBe('auto-hidden');
    });

    it('hides INFO tooltip when user clicks in table', () => {
      let model = helper.createModelFixture(2, 5);
      model.tableStatusVisible = true;
      model.tableStatus = {
        severity: Status.Severity.INFO,
        message: 'Table has an information'
      };
      let table = helper.createTable(model);
      expect(table.tableStatus.uiState).toBe(undefined);

      // Check visible
      table.render();
      expect(table.footer._tableStatusTooltip.rendered).toBe(true);
      expect(table.tableStatus.uiState).toBe('auto-hidden'); // because auto-removal is already scheduled at INFO level

      // Click "outside" (first row)
      JQueryTesting.triggerMouseDownCapture(table.$rows().eq(0));

      // Check invisible
      expect(table.footer._tableStatusTooltip).toBe(null);
      expect(table.tableStatus.uiState).toBe('auto-hidden');
    });

    it('does not hide ERROR tooltip when user clicks in table', () => {
      let model = helper.createModelFixture(2, 5);
      model.tableStatusVisible = true;
      model.tableStatus = {
        severity: Status.Severity.ERROR,
        message: 'Table has an error'
      };
      let table = helper.createTable(model);
      expect(table.tableStatus.uiState).toBe(undefined);

      // Check visible
      table.render();
      expect(table.footer._tableStatusTooltip.rendered).toBe(true);
      expect(table.tableStatus.uiState).toBe(undefined);

      // Click "outside" (first row)
      JQueryTesting.triggerMouseDownCapture(table.$rows().eq(0));

      // Check invisible
      expect(table.footer._tableStatusTooltip.rendered).toBe(true);
      expect(table.tableStatus.uiState).toBe(undefined);
    });

  });

  describe('aria properties', () => {

    it('has aria role status set on the info sections', () => {
      let model = helper.createModelFixture(2);
      model.tableStatusVisible = true;
      let table = helper.createTable(model);
      table.render();
      let infoItems = table.footer._$info.children('.table-info-item').not('.table-info-status');
      expect(infoItems.length).toBeGreaterThan(0);
      infoItems.each((index, infoItem) => {
        expect($(infoItem)).toHaveAttr('role', 'status');
      });
    });
  });
});
