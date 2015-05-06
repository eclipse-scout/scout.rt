/* global TableSpecHelper, FormSpecHelper */
describe("CellEditor", function() {
  var session;
  var helper;
  var formHelper;

  beforeEach(function() {
    setFixtures(sandboxDesktop());
    session = new scout.Session($('#sandbox'), '1.1');
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

  function startCellEdit() {
    var model = helper.createModelFixture(2, 2);
    model.rows[0].cells[0].editable = true;
    var table = helper.createTable(model);
    table.render(session.$entryPoint);

    var field = createField('StringField', table);
    table.columns[0].startCellEdit(table.rows[0], field.id);
    return findPopup();
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
      table.rows[0].cells[0].editable = true;

      spyOn(table, 'sendPrepareCellEdit');
      $cell0_1.triggerMouseDown();
      $cell0_0.triggerMouseUp();
      expect(table.sendPrepareCellEdit).not.toHaveBeenCalled();
    });

  });

  describe("startCellEdit event", function() {

    it("opens popup with field", function() {
      var popup = startCellEdit();
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
      var popup = startCellEdit();
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
      var popup = startCellEdit();
      popup.completeEdit();

      expect($findPopup().length).toBe(0);
      expect($findPopup().find('.form-field').length).toBe(0);
      expect(popup.cell.field.rendered).toBe(false);
    });

    it("sends completeCellEdit", function() {
      var popup = startCellEdit();
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
      var popup = startCellEdit();
      popup.cancelEdit();

      expect($findPopup().length).toBe(0);
      expect($findPopup().find('.form-field').length).toBe(0);
      expect(popup.cell.field.rendered).toBe(false);
    });

    it("sends cancelCellEdit", function() {
      var popup = startCellEdit();
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

});
