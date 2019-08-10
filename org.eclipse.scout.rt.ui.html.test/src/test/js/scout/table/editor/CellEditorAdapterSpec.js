/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
describe("CellEditorAdapter", function() {
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

  function createStringField(table) {
    var model = formHelper.createFieldModel('StringField', session.desktop);
    var adapter = new scout.StringFieldAdapter();
    adapter.init(model);
    return adapter.createWidget(model, session.desktop);
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
    var adapter = helper.createTableAdapter(model) ;
    var table = adapter.createWidget(model, session.desktop);
    table.render();

    var field = createStringField(table);
    table.startCellEdit(table.columns[0], table.rows[0], field);
    return findPopup();
  }

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

    it("removes the cell editor popup", function() {
      var popup = createTableAndStartCellEdit();
      var field = popup.cell.field;

      var message = {
        events: [createEndCellEditEvent(popup.table, field.id)]
      };
      session._processSuccessResponse(message);

      jasmine.clock().tick();
      expect($findPopup().length).toBe(0);
      expect($findPopup().find('.form-field').length).toBe(0);
      expect(popup.rendered).toBe(false);
      expect(popup.cell.field.rendered).toBe(false);
    });

  });

  describe("completeEdit", function() {

    it("sends completeCellEdit", function(done) {
      var popup = createTableAndStartCellEdit();
      popup.completeEdit()
        .then(function() {
          sendQueuedAjaxCalls();
          var event = new scout.RemoteEvent(popup.table.id, 'completeCellEdit', {
            fieldId: popup.cell.field.id
          });
          expect(mostRecentJsonRequest()).toContainEvents(event);
          done();
        });
      jasmine.clock().tick(5);
    });

    it("sends completeCellEdit only once", function(done) {
      var popup = createTableAndStartCellEdit();
      var doneFunc = function() {
        sendQueuedAjaxCalls();

        expect(jasmine.Ajax.requests.count()).toBe(1);
        expect(mostRecentJsonRequest().events.length).toBe(1);
        var event = new scout.RemoteEvent(popup.table.id, 'completeCellEdit', {
          fieldId: popup.cell.field.id
        });
        expect(mostRecentJsonRequest()).toContainEvents(event);
        done();
      };

      popup.completeEdit().then(doneFunc);
      popup.completeEdit();
      jasmine.clock().tick(5);
    });

    it("does not remove the popup and its field (will be done by endCellEdit)", function() {
      var popup = createTableAndStartCellEdit();
      popup.completeEdit();

      expect($findPopup().length).toBe(1);
      expect($findPopup().find('.form-field').length).toBe(1);
      expect(popup.rendered).toBe(true);
      expect(popup.cell.field.rendered).toBe(true);
    });

  });

  describe("cancelEdit", function() {

    it("sends cancelCellEdit", function() {
      var popup = createTableAndStartCellEdit();
      popup.cancelEdit();
      sendQueuedAjaxCalls();

      var event = new scout.RemoteEvent(popup.table.id, 'cancelCellEdit', {
        fieldId: popup.cell.field.id
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it("removes the popup and its field", function() {
      var popup = createTableAndStartCellEdit();
      popup.cancelEdit();

      expect($findPopup().length).toBe(0);
      expect($findPopup().find('.form-field').length).toBe(0);
      expect(popup.rendered).toBe(false);
      expect(popup.cell.field.rendered).toBe(false);
    });
  });
});
