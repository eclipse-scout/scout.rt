/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("CellEditor", function() {
  var session;
  var helper;
  var formHelper;

  beforeEach(function() {
    setFixtures(sandboxDesktop());
    session = sandboxSession();
    helper = new scout.TableSpecHelper(session);
    formHelper = new scout.FormSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    var popup = findPopup();
    if (popup) {
      popup.close();
    }
  });

  function createStringField() {
    return scout.create('StringField', {
      parent: session.desktop
    });
  }

  function $findPopup() {
    return $('.cell-editor-popup');
  }

  function findPopup() {
    return $findPopup().data('popup');
  }

  function assertCellEditorIsOpen(table, column, row) {
    var popup = table.cellEditorPopup;
    expect(popup.cell.field.rendered).toBe(true);
    expect(popup.column).toBe(column);
    expect(popup.row).toBe(row);
    var $popup = $findPopup();
    expect($popup.length).toBe(1);
    expect(popup.$container[0]).toBe($popup[0]);
    expect($popup.find('.form-field').length).toBe(1);
  }

  describe("mouse click", function() {
    var table, model, $rows, $cells0, $cells1, $cell0_0, $cell0_1, $cell1_0;

    beforeEach(function() {
      model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
      table.render();
      helper.applyDisplayStyle(table);
      $rows = table.$rows();
      $cells0 = $rows.eq(0).find('.table-cell');
      $cells1 = $rows.eq(1).find('.table-cell');
      $cell0_0 = $cells0.eq(0);
      $cell0_1 = $cells0.eq(1);
      $cell1_0 = $cells1.eq(0);
    });

    it("starts cell edit if cell is editable", function() {
      table.rows[0].cells[0].editable = true;
      table.rows[1].cells[0].editable = false;

      spyOn(table, 'prepareCellEdit');
      $cell1_0.triggerClick();
      expect(table.prepareCellEdit).not.toHaveBeenCalled();
      $cell0_0.triggerClick();
      expect(table.prepareCellEdit).toHaveBeenCalled();
    });

    it("does not start cell edit if cell is not editable", function() {
      table.rows[0].cells[0].editable = false;

      spyOn(table, 'prepareCellEdit');
      $cell0_0.triggerClick();
      expect(table.prepareCellEdit).not.toHaveBeenCalled();
    });

    it("does not start cell edit if row is disabled", function() {
      table.rows[0].cells[0].editable = true;
      table.rows[0].enabled = false;

      spyOn(table, 'prepareCellEdit');
      $cell0_0.triggerClick();
      expect(table.prepareCellEdit).not.toHaveBeenCalled();
    });

    it("does not start cell edit if table is disabled", function() {
      table.rows[0].cells[0].editable = true;
      table.enabled = false;

      spyOn(table, 'prepareCellEdit');
      $cell0_0.triggerClick();
      expect(table.prepareCellEdit).not.toHaveBeenCalled();
    });

    it("does not start cell edit if mouse down and up happened on different cells", function() {
      table.rows[0].cells[0].editable = true;
      table.rows[0].cells[1].editable = true;

      spyOn(table, 'prepareCellEdit');
      $cell0_1.triggerMouseDown();
      $cell0_0.triggerMouseUp();
      expect(table.prepareCellEdit).not.toHaveBeenCalled();
    });

    it("does not start cell edit if right mouse button was pressed", function() {
      table.rows[0].cells[0].editable = true;

      spyOn(table, 'prepareCellEdit');
      $cell0_0.triggerMouseDown({which: 3});
      $cell0_0.triggerMouseUp({which: 3});
      expect(table.prepareCellEdit).not.toHaveBeenCalled();
    });

    it("does not start cell edit if middle mouse button was pressed", function() {
      table.rows[0].cells[0].editable = true;

      spyOn(table, 'prepareCellEdit');
      $cell0_0.triggerMouseDown({which: 2});
      $cell0_0.triggerMouseUp({which: 2});
      expect(table.prepareCellEdit).not.toHaveBeenCalled();
    });

    it("does not open cell editor if a ctrl or shift is pressed, because the user probably wants to do row selection rather than cell editing", function() {
      table.rows[0].cells[0].editable = true;
      table.rows[1].cells[0].editable = true;

      spyOn(table, 'prepareCellEdit');
      // row 0 is selected, user presses shift and clicks row 2
      table.selectRows([table.rows[0]]);
      $cell1_0.triggerClick({modifier: 'shift'});
      expect(table.prepareCellEdit).not.toHaveBeenCalled();

      $cell1_0.triggerClick({modifier: 'ctrl'});
      expect(table.prepareCellEdit).not.toHaveBeenCalled();
    });

  });

  describe("prepareCellEdit", function() {
    var table;

    beforeEach(function() {
      var model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
      table.render();
      helper.applyDisplayStyle(table);
    });

    it("creates field and calls start", function() {
      table.columns[0].setEditable(true);
      spyOn(table, 'startCellEdit').and.callThrough();

      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick();
      expect(table.startCellEdit).toHaveBeenCalled();
      assertCellEditorIsOpen(table, table.columns[0], table.rows[0]);
    });

    it("triggers prepareCellEdit event", function() {
      var triggeredEvent;
      table.columns[0].setEditable(true);
      table.on('prepareCellEdit', function(event) {
        triggeredEvent = event;
      });
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick();
      expect(triggeredEvent.column).toBe(table.columns[0]);
      expect(triggeredEvent.row).toBe(table.rows[0]);
    });

  });

  describe("startCellEdit", function() {
    var table;

    beforeEach(function() {
      var model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
      table.render();
      helper.applyDisplayStyle(table);
    });

    it("opens popup with field", function() {
      table.columns[0].setEditable(true);
      var field = createStringField(table);
      table.startCellEdit(table.columns[0], table.rows[0], field);
      assertCellEditorIsOpen(table, table.columns[0], table.rows[0]);
      expect(table.cellEditorPopup.cell.field).toBe(field);
    });

  });

  describe("completeCellEdit", function() {
    var table;

    beforeEach(function() {
      var model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
      table.render();
      helper.applyDisplayStyle(table);
    });

    it("triggers completeCellEdit event", function() {
      var triggeredEvent;
      table.columns[0].setEditable(true);
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick();
      table.on('completeCellEdit', function(event) {
        triggeredEvent = event;
      });
      table.completeCellEdit();
      expect(triggeredEvent.column).toBe(table.columns[0]);
      expect(triggeredEvent.row).toBe(table.rows[0]);
      expect(triggeredEvent.field).toBe(table.rows[0].cells[0].field);
    });

    it("calls endCellEdit with saveEditorValue=true", function() {
      table.columns[0].setEditable(true);
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick();
      spyOn(table, 'endCellEdit').and.callThrough();
      var field = table.cellEditorPopup.cell.field;

      table.completeCellEdit();
      expect(table.endCellEdit).toHaveBeenCalledWith(field, true);
      expect($findPopup().length).toBe(0);
    });

    it("saves editor value", function() {
      table.columns[0].setEditable(true);
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick();
      table.cellEditorPopup.cell.field.setValue('my new value');

      table.completeCellEdit();
      expect(table.rows[0].cells[0].value).toBe('my new value');
    });

  });

  describe("cancelCellEdit", function() {
    var table;

    beforeEach(function() {
      var model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
      table.render();
      helper.applyDisplayStyle(table);
    });

    it("triggers cancelCellEdit event", function() {
      var triggeredEvent;
      table.columns[0].setEditable(true);
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick();
      table.on('cancelCellEdit', function(event) {
        triggeredEvent = event;
      });
      table.cancelCellEdit();
      expect(triggeredEvent.column).toBe(table.columns[0]);
      expect(triggeredEvent.row).toBe(table.rows[0]);
      expect(triggeredEvent.field).toBe(table.rows[0].cells[0].field);
    });

    it("calls endCellEdit with saveEditorValue=false", function() {
      table.columns[0].setEditable(true);
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick();
      spyOn(table, 'endCellEdit').and.callThrough();
      var field = table.cellEditorPopup.cell.field;

      table.cancelCellEdit();
      expect(table.endCellEdit).toHaveBeenCalledWith(field);
      expect($findPopup().length).toBe(0);
    });

    it("does not save editor value", function() {
      table.columns[0].setEditable(true);
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick();
      table.cellEditorPopup.cell.field.setValue('my new value');

      table.cancelCellEdit();
      expect(table.rows[0].cells[0].value).toBe('cell0_0');
    });

  });

  describe("endCellEdit", function() {
    var table;

    beforeEach(function() {
      var model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
      table.render();
      helper.applyDisplayStyle(table);
    });

    it("destroys the field", function() {
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick();
      var popup = table.cellEditorPopup;
      var field = popup.cell.field;
      expect(field.destroyed).toBe(false);

      table.endCellEdit(field);
      expect(field.destroyed).toBe(true);
    });

    it("removes the cell editor popup", function() {
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick();
      var popup = table.cellEditorPopup;
      var field = popup.cell.field;
      expect(field.destroyed).toBe(false);

      table.endCellEdit(field);
      expect($findPopup().length).toBe(0);
      expect($findPopup().find('.form-field').length).toBe(0);
      expect(popup.rendered).toBe(false);
      expect(popup.cell.field.rendered).toBe(false);
    });

  });

  describe("validation", function() {
    var table, model, cell0_0, $tooltip;

    beforeEach(function() {
      model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
      cell0_0 = table.rows[0].cells[0];
    });

    it("shows a tooltip if field has an error", function() {
      cell0_0.editable = true;
      cell0_0.errorStatus = 'Validation error';
      $tooltip = $('.tooltip');

      expect($tooltip.length).toBe(0);
      table.render();
      $tooltip = $('.tooltip');
      expect($tooltip.length).toBe(1);
    });

    it("does not sho a tooltip if field has no error", function() {
      cell0_0.editable = true;
      $tooltip = $('.tooltip');

      expect($tooltip.length).toBe(0);
      table.render();
      $tooltip = $('.tooltip');
      expect($tooltip.length).toBe(0);
    });
  });

  describe("popup recovery", function() {
    var model, table, row0, $cells0, $cell0_0;

    beforeEach(function() {
      model = helper.createModelFixture(2, 3);
      table = helper.createTable(model);
      row0 = table.rows[0];
    });

    it("reopens popup if row gets updated", function() {
      row0.cells[0].editable = true;
      table.render();
      $cells0 = table.$cellsForRow(row0.$row);
      $cell0_0 = $cells0.eq(0);
      table.prepareCellEdit(table.columns[0], row0);
      jasmine.clock().tick();
      expect(table.cellEditorPopup.row).toBe(row0);
      expect(table.cellEditorPopup.$anchor[0]).toBe($cell0_0[0]);

      var updatedRows = helper.createModelRows(2, 1);
      updatedRows[0].id = row0.id;
      table.updateRows(updatedRows);

      // Check if popup is correctly linked to updated row and new $cell
      row0 = table.rows[0];
      $cells0 = table.$cellsForRow(row0.$row);
      $cell0_0 = $cells0.eq(0);
      expect($findPopup().length).toBe(1);
      expect(table.cellEditorPopup.row).toBe(row0);
      expect(table.cellEditorPopup.$anchor[0]).toBe($cell0_0[0]);
    });

    it("closes popup if row gets deleted", function() {
      row0.cells[0].editable = true;
      table.render();
      table.prepareCellEdit(table.columns[0], row0);
      jasmine.clock().tick();
      spyOn(table, 'cancelCellEdit');

      table.deleteRows([row0]);

      // Check if popup is closed
      expect($findPopup().length).toBe(0);

      // Check whether cancel edit has been called
      expect(table.cancelCellEdit).toHaveBeenCalled();
    });

    it("closes popup if all rows get deleted", function() {
      row0.cells[0].editable = true;
      table.render();
      table.prepareCellEdit(table.columns[0], row0);
      jasmine.clock().tick();
      spyOn(table, 'cancelCellEdit');

      table.deleteAllRows();

      // Check if popup is closed
      expect($findPopup().length).toBe(0);

      // Check whether cancel edit has been called
      expect(table.cancelCellEdit).toHaveBeenCalled();
    });

    it("closes popup (before) table is detached", function() {
      row0.cells[0].editable = true;
      table.render();
      table.prepareCellEdit(table.columns[0], row0);
      jasmine.clock().tick();
      expect(table.cellEditorPopup).toBeTruthy();
      table._beforeDetach(); // called by parent.detach();
      expect(table.cellEditorPopup).toBe(null);
    });

    it("closes popup when table is removed", function() {
      row0.cells[0].editable = true;
      table.render();
      table.prepareCellEdit(table.columns[0], row0);
      jasmine.clock().tick();
      expect(table.cellEditorPopup).toBeTruthy();
      table.remove();
      expect(table.cellEditorPopup).toBe(null);
    });

  });

  describe("tooltip recovery", function() {

    var model, table, row0;

    beforeEach(function() {
      model = helper.createModelFixture(2, 3);
      table = helper.createTable(model);
      row0 = model.rows[0];
    });

    it("removes tooltip if row gets deleted", function() {
      row0.cells[0].editable = true;
      row0.cells[0].errorStatus = 'Validation error';

      table.render();
      expect($('.tooltip').length).toBe(1);
      expect(table.tooltips.length).toBe(1);

      table.deleteRows([row0]);

      expect($('.tooltip').length).toBe(0);
      expect(table.tooltips.length).toBe(0);
    });

  });
});
