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
/* global FormSpecHelper, scout.MenuSpecHelper */
describe("SequenceBox", function() {
  var session, helper, menuHelper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    menuHelper = new scout.MenuSpecHelper(session);
  });

  function createField(model) {
    return helper.createCompositeField(session, model);
  }

  function createModel() {
    var model = helper.createFieldModel('SequenceBox');
    model.fields = [helper.createFieldModel('StringField'), helper.createFieldModel('DateField')];
    model.fields[0].statusVisible = false;
    model.fields[1].statusVisible = false;
    return model;
  }

  describe("mandatory indicator", function() {

    // Must not contain an indicator to prevent a double indicator if the first field is mandatory too
    it("does not exist", function() {
      var model = createModel();
      model.mandatory = true;
      var field = createField(model);
      field.render(session.$entryPoint);

      expect(field.$mandatory).toBeUndefined();
    });

  });

  describe("label width", function() {

    it("is 0 if it is empty", function() {
      var model = createModel();
      var field = createField(model);
      field.render(session.$entryPoint);
      // css is not applied, therefore we need to adjust display style here
      field.fields[0].$label.css('display', 'inline-block');
      field.validateLayout();

      expect(field.fields[0].$label.outerWidth(true)).toBe(0);
    });

  });

  describe("status handling", function() {

    it("moves the error status of the last field to the seq box", function() {
      var model = createModel();
      var field = createField(model);
      field.statusVisible = false;
      field.render(session.$entryPoint);

      expect(field.$status.isVisible()).toBe(false);
      expect(field.errorStatus).toBeFalsy();
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].errorStatus).toBeFalsy();

      var event = createPropertyChangeEvent(field.fields[1], {
        errorStatus: {message:'foo'}
      });
      field.fields[1].onModelPropertyChange(event);

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('foo');
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].errorStatus.message).toBe('foo');
    });

    it("moves the tooltip of the last field to the seq box", function() {
      var model = createModel();
      var field = createField(model);
      field.statusVisible = false;
      field.render(session.$entryPoint);

      expect(field.$status.isVisible()).toBe(false);
      expect(field.tooltipText).toBeFalsy();
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].tooltipText).toBeFalsy();

      var event = createPropertyChangeEvent(field.fields[1], {
        tooltipText: 'foo'
      });
      field.fields[1].onModelPropertyChange(event);

      expect(field.$status.isVisible()).toBe(true);
      expect(field.tooltipText).toBe('foo');
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].tooltipText).toBe('foo');
    });

    it("moves the menus of the last field to the seq box", function() {
      var model = createModel();
      var field = createField(model);
      var menu0 = menuHelper.createMenu(menuHelper.createModel());
      field.fields[1].menus = [menu0];
      field.fields[1].menusVisible = false;
      field.statusVisible = false;
      field.render(session.$entryPoint);

      expect(field.$status.isVisible()).toBe(false);
      expect(field.menus.length).toBe(1);
      expect(field.$container).not.toHaveClass('has-menus');
      expect(field.fields[1].$status.isVisible()).toBe(false);

      var event = createPropertyChangeEvent(field.fields[1], {
        menusVisible: true
      });
      field.fields[1].onModelPropertyChange(event);

      expect(field.$status.isVisible()).toBe(true);
      expect(field.menus.length).toBe(1);
      expect(field.$container).toHaveClass('has-menus');
      expect(field.fields[1].$status.isVisible()).toBe(false);
    });

    it("does not display the error message of the last field, only the one of the seq box", function() {
      var model = createModel();
      var field = createField(model);
      field.statusVisible = false;
      field.render(session.$entryPoint);

      expect(field.fields[1].tooltip).toBeFalsy();
      expect(field.tooltip).toBeFalsy();

      var event = createPropertyChangeEvent(field.fields[1], {
        errorStatus: {message:'foo'}
      });
      field.fields[1].onModelPropertyChange(event);

      expect(field.fields[1].tooltip).toBeFalsy();
      expect(field.tooltip.rendered).toBe(true);
    });

    it("removes the tooltip from the seq box if last field gets invisible", function() {
      var model = createModel();
      var field = createField(model);
      field.fields[1].tooltipText = 'foo';
      field.statusVisible = false;
      field.render(session.$entryPoint);

      expect(field.$status.isVisible()).toBe(true);
      expect(field.tooltipText).toBe('foo');
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].tooltipText).toBe('foo');

      var event = createPropertyChangeEvent(field.fields[1], {
        visible: false
      });
      field.fields[1].onModelPropertyChange(event);

      expect(field.$status.isVisible()).toBe(false);
      expect(field.tooltipText).toBeFalsy();
      expect(field.fields[1].$container.isVisible()).toBe(false);
      expect(field.fields[1].tooltipText).toBe('foo');
    });

    it("moves the tooltip from the first field to the seq box if it gets the last field after a visibility change", function() {
      var model = createModel();
      var field = createField(model);
      field.fields[0].tooltipText = 'foo';
      field.statusVisible = false;
      field.render(session.$entryPoint);

      expect(field.$status.isVisible()).toBe(false);
      expect(field.tooltipText).toBeFalsy();
      expect(field.fields[0].$status.isVisible()).toBe(true);
      expect(field.fields[0].tooltipText).toBe('foo');

      var event = createPropertyChangeEvent(field.fields[1], {
        visible: false
      });
      field.fields[1].onModelPropertyChange(event);

      expect(field.$status.isVisible()).toBe(true);
      expect(field.tooltipText).toBe('foo');
      expect(field.fields[0].$status.isVisible()).toBe(false);
      expect(field.fields[0].tooltipText).toBe('foo');
    });

    it("moves the error from the first field to the seq box if it gets the last field after a visibility change", function() {
      var model = createModel();
      var field = createField(model);
      field.fields[0].errorStatus = new scout.Status({message: 'foo'});
      field.statusVisible = false;
      field.render(session.$entryPoint);

      expect(field.$status.isVisible()).toBe(false);
      expect(field.errorStatus).toBeFalsy();
      expect(field.tooltip).toBeFalsy();
      expect(field.fields[0].$status.isVisible()).toBe(true);
      expect(field.fields[0].tooltip.rendered).toBe(true);
      expect(field.fields[0].errorStatus.message).toBe('foo');

      var event = createPropertyChangeEvent(field.fields[1], {
        visible: false
      });
      field.fields[1].onModelPropertyChange(event);

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('foo');
      expect(field.fields[0].$status.isVisible()).toBe(false);
      expect(field.fields[0].tooltip.rendered).toBe(false);
      expect(field.fields[0].errorStatus.message).toBe('foo');
    });

    it("makes sure the status may be displayed on the field again if the field was the last visible field once", function() {
      var model = createModel();
      var field = createField(model);
      field.fields[0].errorStatus = new scout.Status({message: 'foo'});
      field.statusVisible = false;
      field.render(session.$entryPoint);

      expect(field.$status.isVisible()).toBe(false);
      expect(field.errorStatus).toBeFalsy();
      expect(field.tooltip).toBeFalsy();
      expect(field.fields[0].$status.isVisible()).toBe(true);
      expect(field.fields[0].tooltip.rendered).toBe(true);
      expect(field.fields[0].errorStatus.message).toBe('foo');

      var event = createPropertyChangeEvent(field.fields[1], {
        visible: false
      });
      field.fields[1].onModelPropertyChange(event);

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('foo');
      expect(field.fields[0].$status.isVisible()).toBe(false);
      expect(field.fields[0].tooltip.rendered).toBe(false);
      expect(field.fields[0].errorStatus.message).toBe('foo');

      event = createPropertyChangeEvent(field.fields[1], {
        visible: true
      });
      field.fields[1].onModelPropertyChange(event);

      expect(field.$status.isVisible()).toBe(false);
      expect(field.errorStatus).toBeFalsy();
      expect(field.tooltip).toBeFalsy();
      expect(field.fields[0].$status.isVisible()).toBe(true);
      expect(field.fields[0].tooltip.rendered).toBe(true);
      expect(field.fields[0].errorStatus.message).toBe('foo');
    });

  });

});
