/* global TableSpecHelper, FormSpecHelper */
describe("CellEditor", function() {
  var session;
  var helper;
  var formHelper;

  beforeEach(function() {
    setFixtures(sandboxDesktop());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    formHelper = new FormSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    var popup = findPopup();
    if (popup) {
      popup.remove();
    }
  });

  function createField (objectType, table) {
    var field = formHelper.createFieldModel(objectType);
    field.owner = table.id;
    return createAdapter(field, session);
  }

  function $findPopup() {
    return $('.cell-editor-popup');
  }

  function findPopup() {
    return $findPopup().data('popup');
  }

  function createTableAndStartCellEdit() {
    var model = helper.createModelFixture(2, 2);
    model.rows[0].cells[0].editable = true;
    var table = helper.createTable(model);
    table.render(session.$entryPoint);

    var field = createField('StringField', table);
    table._startCellEdit(table.columns[0], table.rows[0], field.id);
    return findPopup();
  }

  function startAndAssertCellEdit(table, column, row) {
    var field = createField('StringField', table);
    var popup = table._startCellEdit(column, row, field.id);
    expect($findPopup().length).toBe(1);
    expect($findPopup().find('.form-field').length).toBe(1);
    expect(popup.cell.field.rendered).toBe(true);
  }

  describe("mouse click", function() {
    var table, model, $rows, $cells0, $cells1, $cell0_0, $cell0_1, $cell1_0;

    beforeEach(function() {
      model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
      table.render(session.$entryPoint);
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

      spyOn(table, 'sendPrepareCellEdit');
      $cell1_0.triggerClick();
      expect(table.sendPrepareCellEdit).not.toHaveBeenCalled();
      $cell0_0.triggerClick();
      expect(table.sendPrepareCellEdit).toHaveBeenCalled();
    });

    it("does not start cell edit if cell is not editable", function() {
      table.rows[0].cells[0].editable = false;

      spyOn(table, 'sendPrepareCellEdit');
      $cell0_0.triggerClick();
      expect(table.sendPrepareCellEdit).not.toHaveBeenCalled();
    });

    it("does not start cell edit if row is disabled", function() {
      table.rows[0].cells[0].editable = true;
      table.rows[0].enabled = false;

      spyOn(table, 'sendPrepareCellEdit');
      $cell0_0.triggerClick();
      expect(table.sendPrepareCellEdit).not.toHaveBeenCalled();
    });

    it("does not start cell edit if table is disabled", function() {
      table.rows[0].cells[0].editable = true;
      table.enabled = false;

      spyOn(table, 'sendPrepareCellEdit');
      $cell0_0.triggerClick();
      expect(table.sendPrepareCellEdit).not.toHaveBeenCalled();
    });

    it("does not start cell edit if mouse down and up happened on different cells", function() {
      table.rows[0].cells[0].editable = true;
      table.rows[0].cells[1].editable = true;

      spyOn(table, 'sendPrepareCellEdit');
      $cell0_1.triggerMouseDown();
      $cell0_0.triggerMouseUp();
      expect(table.sendPrepareCellEdit).not.toHaveBeenCalled();
    });

    it("does not start cell edit if right mouse button was pressed", function() {
      table.rows[0].cells[0].editable = true;

      spyOn(table, 'sendPrepareCellEdit');
      $cell0_0.triggerMouseDown({which: 3});
      $cell0_0.triggerMouseUp({which: 3});
      expect(table.sendPrepareCellEdit).not.toHaveBeenCalled();
    });

    it("does not start cell edit if middle mouse button was pressed", function() {
      table.rows[0].cells[0].editable = true;

      spyOn(table, 'sendPrepareCellEdit');
      $cell0_0.triggerMouseDown({which: 2});
      $cell0_0.triggerMouseUp({which: 2});
      expect(table.sendPrepareCellEdit).not.toHaveBeenCalled();
    });

    it("does not open cell editor if a ctrl or shift is pressed, because the user probably wants to do row selection rather than cell editing", function() {
      table.rows[0].cells[0].editable = true;
      table.rows[1].cells[0].editable = true;

      spyOn(table, 'sendPrepareCellEdit');
      // row 0 is selected, user presses shift and clicks row 2
      table.selectRows([table.rows[0]]);
      $cell1_0.triggerClick({modifier: 'shift'});
      expect(table.sendPrepareCellEdit).not.toHaveBeenCalled();

      $cell1_0.triggerClick({modifier: 'ctrl'});
      expect(table.sendPrepareCellEdit).not.toHaveBeenCalled();
    });

  });

  describe("startCellEdit event", function() {

    it("opens popup with field", function() {
      var popup = createTableAndStartCellEdit();
      expect($findPopup().length).toBe(1);
      expect($findPopup().find('.form-field').length).toBe(1);
      expect(popup.cell.field.rendered).toBe(true);
    });

  });

  describe("endCellEdit event", function() {

    function createEndCellEditEvent(model, fieldId) {
      return {
        target: model.id,
        fieldId: fieldId,
        type: 'endCellEdit'
      };
    }

    it("destroys the field", function() {
      var popup = createTableAndStartCellEdit();
      var field = popup.cell.field;

      var message = {
        events: [createEndCellEditEvent(popup.table, field.id)]
      };
      session._processSuccessResponse(message);

      expect(field.destroyed).toBe(true);
      expect(session.getModelAdapter(field.id)).toBeFalsy();
    });

  });

  describe("completeEdit", function() {

    it("removes the popup and its field", function() {
      var popup = createTableAndStartCellEdit();
      popup.completeEdit();

      expect($findPopup().length).toBe(0);
      expect($findPopup().find('.form-field').length).toBe(0);
      expect(popup.cell.field.rendered).toBe(false);
    });

    it("sends completeCellEdit", function() {
      var popup = createTableAndStartCellEdit();
      popup.completeEdit();
      sendQueuedAjaxCalls();

      var event = new scout.Event(popup.table.id, 'completeCellEdit', {
        fieldId: popup.cell.field.id
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

  });

  describe("cancelEdit", function() {

    it("removes the popup and its field", function() {
      var popup = createTableAndStartCellEdit();
      popup.cancelEdit();

      expect($findPopup().length).toBe(0);
      expect($findPopup().find('.form-field').length).toBe(0);
      expect(popup.cell.field.rendered).toBe(false);
    });

    it("sends cancelCellEdit", function() {
      var popup = createTableAndStartCellEdit();
      popup.cancelEdit();
      sendQueuedAjaxCalls();

      var event = new scout.Event(popup.table.id, 'cancelCellEdit', {
        fieldId: popup.cell.field.id
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
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
      table.render(session.$entryPoint);
      $tooltip = $('.tooltip');
      expect($tooltip.length).toBe(1);
    });

    it("does not sho a tooltip if field has no error", function() {
      cell0_0.editable = true;
      $tooltip = $('.tooltip');

      expect($tooltip.length).toBe(0);
      table.render(session.$entryPoint);
      $tooltip = $('.tooltip');
      expect($tooltip.length).toBe(0);
    });
  });

  describe("popup recovery", function() {

    var model, table, row0, $cells0, $cell0_0;

    function createRowsUpdatedEvent(model, rows) {
      return {
        target: model.id,
        rows: rows,
        type: 'rowsUpdated'
      };
    }

    beforeEach(function() {
      model = helper.createModelFixture(2, 3);
      table = helper.createTable(model);
      row0 = model.rows[0];
    });

    it("reopens popup if row gets updated", function() {
      row0.cells[0].editable = true;
      table.render(session.$entryPoint);
      $cells0 = table.$cellsForRow(row0.$row);
      $cell0_0 = $cells0.eq(0);
      startAndAssertCellEdit(table, table.columns[0], row0);
      expect(table.cellEditorPopup.row).toBe(row0);
      expect(table.cellEditorPopup.$anchor[0]).toBe($cell0_0[0]);

      var updatedRows = helper.createModelRows(2, 1);
      updatedRows[0].id = row0.id;
      var message = {
        events: [createRowsUpdatedEvent(model, updatedRows)]
      };
      session._processSuccessResponse(message);

      // Check if popup is correctly linked to updated row and new $cell
      row0 = updatedRows[0];
      $cells0 = table.$cellsForRow(row0.$row);
      $cell0_0 = $cells0.eq(0);
      expect($findPopup().length).toBe(1);
      expect(table.cellEditorPopup.row).toBe(row0);
      expect(table.cellEditorPopup.$anchor[0]).toBe($cell0_0[0]);
    });

    it("closes popup if row gets deleted", function() {
      row0.cells[0].editable = true;
      table.render(session.$entryPoint);
      startAndAssertCellEdit(table, table.columns[0], row0);
      spyOn(table, 'sendCancelCellEdit');

      table._deleteRows([row0]);

      // Check if popup is closed
      expect($findPopup().length).toBe(0);

      // Check whether cancel edit has been sent
      expect(table.sendCancelCellEdit).toHaveBeenCalled();
    });

    it("closes popup if all rows get deleted", function() {
      row0.cells[0].editable = true;
      table.render(session.$entryPoint);
      startAndAssertCellEdit(table, table.columns[0], row0);
      spyOn(table, 'sendCancelCellEdit');

      table._deleteAllRows();

      // Check if popup is closed
      expect($findPopup().length).toBe(0);

      // Check whether cancel edit has been sent
      expect(table.sendCancelCellEdit).toHaveBeenCalled();
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

      table.render(session.$entryPoint);
      expect($('.tooltip').length).toBe(1);
      expect(table.tooltips.length).toBe(1);

      table._deleteRows([row0]);

      expect($('.tooltip').length).toBe(0);
      expect(table.tooltips.length).toBe(0);
    });

  });
});
