/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("TableFooterSpec", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.TableSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe("render", function() {

    it("attaches listener to the table but only once", function() {
      var model = helper.createModelFixture(2);
      model.tableStatusVisible = true;
      var table = helper.createTable(model);
      table.render(session.$entryPoint);
      expect(table.footer).not.toBeUndefined();
      var listenerCount = table.events._eventListeners.length;

      table.setTableStatusVisible(false);
      table.setTableStatusVisible(true);

      // Still same amount of listeners expected after footer visibility changed
      expect(table.events._eventListeners.length).toBe(listenerCount);
    });

  });

  describe("controls", function() {

    function createTableControl() {
      var action = new scout.TableControl();
      action.init(createSimpleModel('TableControl', session));
      return action;
    }

    it("removes old and renders new controls on property change", function() {
      var model = helper.createModelFixture(2);
      model.tableStatusVisible = true;
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var controls = [createTableControl()];
      table.setTableControls(controls);
      expect(controls[0].rendered).toBe(true);

      var newControls = [createTableControl(), createTableControl()];
      table.setTableControls(newControls);
      expect(controls[0].rendered).toBe(false);
      expect(newControls[0].rendered).toBe(true);
      expect(newControls[0].rendered).toBe(true);
    });
  });

  describe("TableStatusTooltip", function() {

    it("shows ERROR tooltip", function() {
      var model = helper.createModelFixture(2);
      model.tableStatusVisible = true;
      model.tableStatus = {
        severity: scout.Status.Severity.ERROR,
        message: 'Table has an error'
      };
      var table = helper.createTable(model);

      // Check that status and tooltip is rendered when table is rendered
      table.render(session.$entryPoint);
      expect(table.footer._tableStatusTooltip.rendered).toBe(true);
      expect(table.footer._tableStatusTooltip.$container.hasClass('tooltip-error')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('has-error')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(true);
      expect(table.tableStatus.uiState).toBe(undefined);

      // Check that status and tooltip are re-rendered when table is removed and rendered again
      table.remove();
      expect(table.footer._tableStatusTooltip).toBe(null);
      table.render(session.$entryPoint);
      expect(table.footer._tableStatusTooltip.rendered).toBe(true);
      expect(table.footer._tableStatusTooltip.$container.hasClass('tooltip-error')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('has-error')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(true);

      // Check that tooltip is hidden after mouse click on status
      table.footer._$infoTableStatusIcon.triggerMouseDownCapture();
      expect(table.footer._tableStatusTooltip).toBe(null);
      expect(table.footer._$infoTableStatus.hasClass('has-error')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(false);
      expect(table.tableStatus.uiState).toBe('user-hidden');

      // Check that tooltip stays hidden when table is removed and rendered again
      table.remove();
      table.render(session.$entryPoint);
      expect(table.footer._tableStatusTooltip).toBe(null);
      expect(table.footer._$infoTableStatus.hasClass('has-error')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(false);
      expect(table.tableStatus.uiState).toBe('user-hidden');

      // Check that tooltip is shown after second mouse click on status
      table.footer._$infoTableStatusIcon.triggerMouseDownCapture();
      expect(table.footer._tableStatusTooltip.rendered).toBe(true);
      expect(table.footer._tableStatusTooltip.$container.hasClass('tooltip-error')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('has-error')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(true);
      expect(table.tableStatus.uiState).toBe('user-shown');

      // Check that tooltip stays visible when table is removed and rendered again
      table.remove();
      table.render(session.$entryPoint);
      expect(table.footer._tableStatusTooltip.rendered).toBe(true);
      expect(table.footer._tableStatusTooltip.$container.hasClass('tooltip-error')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('has-error')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(true);
      expect(table.tableStatus.uiState).toBe('user-shown');
    });

    it("shows INFO tooltip when table is rendered", function() {
      var model = helper.createModelFixture(2);
      model.tableStatusVisible = true;
      model.tableStatus = {
        severity: scout.Status.Severity.INFO,
        message: 'Table has an information'
      };
      var table = helper.createTable(model);

      // Check that status and tooltip is rendered when table is rendered
      table.render(session.$entryPoint);
      expect(table.footer._tableStatusTooltip.rendered).toBe(true);
      expect(table.footer._tableStatusTooltip.$container.hasClass('tooltip-info')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('has-info')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(true);
      expect(table.tableStatus.uiState).toBe('auto-hidden'); // because auto-removal is already scheduled at INFO level

      // Check that tooltip is hidden automatically after 5s
      jasmine.clock().tick(5100);
      expect(table.footer._tableStatusTooltip).toBe(null);
      expect(table.footer._$infoTableStatus.hasClass('has-info')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(false);
      expect(table.tableStatus.uiState).toBe('auto-hidden');

      // Check that tooltip is not rendered automatically when table is removed and rendered again
      table.remove();
      table.render(session.$entryPoint);
      expect(table.footer._tableStatusTooltip).toBe(null);
      expect(table.footer._$infoTableStatus.hasClass('has-info')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(false);
      expect(table.tableStatus.uiState).toBe('auto-hidden');

      // Check that tooltip is shown again with a mouse click and _not_ hidden automatically again after 5s
      table.footer._$infoTableStatusIcon.triggerMouseDownCapture();
      expect(table.footer._tableStatusTooltip.rendered).toBe(true);
      expect(table.footer._tableStatusTooltip.$container.hasClass('tooltip-info')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('has-info')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(true);
      expect(table.tableStatus.uiState).toBe('user-shown');
      jasmine.clock().tick(5100);
      expect(table.footer._tableStatusTooltip.rendered).toBe(true);
      expect(table.footer._tableStatusTooltip.$container.hasClass('tooltip-info')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('has-info')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(true);
      expect(table.tableStatus.uiState).toBe('user-shown');

      // Check that tooltip is not rendered automatically when table is removed and rendered again
      table.remove();
      table.render(session.$entryPoint);
      expect(table.footer._tableStatusTooltip).toBe(null);
      expect(table.footer._$infoTableStatus.hasClass('has-info')).toBe(true);
      expect(table.footer._$infoTableStatus.hasClass('tooltip-active')).toBe(false);
      expect(table.tableStatus.uiState).toBe('auto-hidden');
    });

    it("hides INFO tooltip when user clicks in table", function() {
      var model = helper.createModelFixture(2, 5);
      model.tableStatusVisible = true;
      model.tableStatus = {
        severity: scout.Status.Severity.INFO,
        message: 'Table has an information'
      };
      var table = helper.createTable(model);
      expect(table.tableStatus.uiState).toBe(undefined);

      // Check visible
      table.render(session.$entryPoint);
      expect(table.footer._tableStatusTooltip.rendered).toBe(true);
      expect(table.tableStatus.uiState).toBe('auto-hidden'); // because auto-removal is already scheduled at INFO level

      // Click "outside" (first row)
      table.$rows().eq(0).triggerMouseDownCapture();

      // Check invisible
      expect(table.footer._tableStatusTooltip).toBe(null);
      expect(table.tableStatus.uiState).toBe('auto-hidden');
    });

    it("does not hide ERROR tooltip when user clicks in table", function() {
      var model = helper.createModelFixture(2, 5);
      model.tableStatusVisible = true;
      model.tableStatus = {
        severity: scout.Status.Severity.ERROR,
        message: 'Table has an error'
      };
      var table = helper.createTable(model);
      expect(table.tableStatus.uiState).toBe(undefined);

      // Check visible
      table.render(session.$entryPoint);
      expect(table.footer._tableStatusTooltip.rendered).toBe(true);
      expect(table.tableStatus.uiState).toBe(undefined);

      // Click "outside" (first row)
      table.$rows().eq(0).triggerMouseDownCapture();

      // Check invisible
      expect(table.footer._tableStatusTooltip.rendered).toBe(true);
      expect(table.tableStatus.uiState).toBe(undefined);
    });

  });

});
