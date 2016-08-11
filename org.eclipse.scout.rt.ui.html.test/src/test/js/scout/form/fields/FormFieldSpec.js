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
describe("FormField", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
  });

  describe("inheritance", function() {
    var formField, model;

    beforeEach(function() {
      model = helper.createFieldModel();
      formField = new scout.FormField();
      formField.init(model);
    });

    it("inherits from Widget", function() {
      expect(scout.Widget.prototype.isPrototypeOf(formField)).toBe(true);
    });

  });

  describe("property label position", function() {
    var formField, model;

    beforeEach(function() {
      model = helper.createFieldModel();
      formField = new scout.StringField();
      formField.init(model);
    });

    describe("position on_field", function() {

      beforeEach(function() {
        formField.label = 'labelName';
        formField.labelPosition = scout.FormField.LABEL_POSITION_ON_FIELD;
      });

      it("sets the label as placeholder", function() {
        formField.render(session.$entryPoint);
        expect(formField.$label.html()).toBeFalsy();
        expect(formField.$field.attr('placeholder')).toBe(formField.label);
      });

      it("does not call field._renderLabelPosition initially", function() {
        formField.render(session.$entryPoint);
        expect(formField.$label.html()).toBeFalsy();
        expect(formField.$field.attr('placeholder')).toBe(formField.label);

        spyOn(formField, '_renderLabelPosition');
        expect(formField._renderLabelPosition).not.toHaveBeenCalled();
      });

    });

    describe("position top", function() {

      beforeEach(function() {
        formField.label = 'labelName';
        formField.labelPosition = scout.FormField.LABEL_POSITION_TOP;
      });

      it("guarantees a minimum height if label is empty", function() {
        formField.label = '';
        formField.render(session.$entryPoint);
        expect(formField.$label.html()).toBe('&nbsp;');
        expect(formField.$label).toBeVisible();
      });

    });

    it("does not display a status if status visible = false", function() {
      formField.statusVisible = false;
      formField.render(session.$entryPoint);

      expect(formField.$status.isVisible()).toBe(false);
    });

  });

  describe("property status visible", function() {
    var formField, model;

    beforeEach(function() {
      model = helper.createFieldModel();
      formField = new scout.FormField();
      formField._render = function($parent) {
        this.addContainer($parent, 'form-field');
        this.addStatus();
      };
      formField.init(model);
    });

    it("shows a status if status visible = true", function() {
      formField.statusVisible = true;
      formField.render(session.$entryPoint);

      expect(formField.$status.isVisible()).toBe(true);
    });

    it("does not show a status if status visible = false", function() {
      formField.statusVisible = false;
      formField.render(session.$entryPoint);

      expect(formField.$status.isVisible()).toBe(false);
    });

    it("shows a status even though status visible is false but tooltipText is set", function() {
      formField.statusVisible = false;
      formField.tooltipText = 'hello';
      formField.render(session.$entryPoint);

      expect(formField.$status.isVisible()).toBe(true);

      var event = createPropertyChangeEvent(formField, {
        tooltipText: ''
      });
      formField.onModelPropertyChange(event);
      expect(formField.$status.isVisible()).toBe(false);
    });

    it("shows a status even though status visible is false but errorStatus is set", function() {
      formField.statusVisible = false;
      formField.errorStatus = new scout.Status({message: 'error', severity: scout.Status.Severity.ERROR});
      formField.render(session.$entryPoint);

      expect(formField.$status.isVisible()).toBe(true);

      var event = createPropertyChangeEvent(formField, {
        errorStatus: ''
      });
      formField.onModelPropertyChange(event);
      expect(formField.$status.isVisible()).toBe(false);
    });

  });

  describe("onModelPropertyChange", function() {
    var formField, model;

    beforeEach(function() {
      model = helper.createFieldModel();
      formField = new scout.FormField();
      formField._render = function($parent) {
        this.addContainer($parent, 'form-field');
      };
      formField.init(model);
    });

    it("event should update model", function() {
      // Note: normally an event for ID 123 would never be applied
      // to an adpater with ID 2! We only do this here in order to
      // check whether or not the onModelPropertyChange method applies
      // the ID of the event by error (should not happen).
      var event = {
        id:'123',
        type:'property',
        properties: {
          errorStatus: {message: 'foo'}
        }
      };
      // required
      formField._$statusLabel = $('<div></div>');
      formField.onModelPropertyChange(event);
      expect(formField.errorStatus).toEqual(new scout.Status({message: 'foo'}));
      // never apply id, type, properties on model
      expect(formField.id).toBe(model.id);
      expect(formField.hasOwnProperty('type')).toBe(false);
      expect(formField.hasOwnProperty('properties')).toBe(false);
    });

    it("considers custom css class", function() {
      formField.render(session.$entryPoint);

      var event = createPropertyChangeEvent(formField, {cssClass: 'custom-class'});
      formField.onModelPropertyChange(event);
      expect(formField.$container).toHaveClass('custom-class');

      event = createPropertyChangeEvent(formField, {cssClass: ''});
      formField.onModelPropertyChange(event);
      expect(formField.$container).not.toHaveClass('custom-class');
    });

  });

});
