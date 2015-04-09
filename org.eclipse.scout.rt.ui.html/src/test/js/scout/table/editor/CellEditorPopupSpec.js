/* global TableSpecHelper, FormSpecHelper */
describe("CellEditor", function() {
  var session;
  var helper;
  var formHelper;

  beforeEach(function() {
    setFixtures(sandbox());
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
    model.columns[0].editable = true;
    var table = helper.createTable(model);
    table.render(session.$entryPoint);

    var field = createField('StringField', table);
    table.columns[0].startCellEdit(table.rows[0], field.id);
    return findPopup();
  }

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

});
