/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('FormField', function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
  });

  function createFormField(model) {
    var formField = new scout.FormField();
    formField._render = function() {
      this.addContainer(this.$parent, 'form-field');
      this.addLabel();
      this.addMandatoryIndicator();
      this.addField(this.$parent.makeDiv());
      this.addStatus();
    };
    formField.init(model);
    return formField;
  }

  describe('inheritance', function() {
    var formField, model;

    beforeEach(function() {
      model = helper.createFieldModel();
      formField = new scout.FormField();
      formField.init(model);
    });

    it('inherits from Widget', function() {
      expect(scout.Widget.prototype.isPrototypeOf(formField)).toBe(true);
    });

  });

  describe('_initProperty', function() {
    var formField, model;

    beforeEach(function() {
      model = helper.createFieldModel();
      formField = new scout.FormField();
    });

    it('gridDataHints are extended (not replaced) on init when gridDataHints is a plain object', function() {
      var defaultGridDataHints = formField.gridDataHints;
      expect(defaultGridDataHints instanceof scout.GridData).toBe(true);
      // expect one of the many default values of scout.GridData
      expect(defaultGridDataHints.fillHorizontal).toBe(true);

      model.gridDataHints = {
        fillHorizontal: false
      };
      formField.init(model);

      // we expect to have still the same instance
      expect(defaultGridDataHints).toBe(formField.gridDataHints);
      // expect that the default gridDataHints property has been overridden with the property passed to the init function
      expect(formField.gridDataHints.fillHorizontal).toBe(false);
    });

    it('gridDataHints are replaced when gridDataHints is instanceof GridData', function() {
      var gridDataHints = new scout.GridData();
      model.gridDataHints = gridDataHints;
      formField.init(model);
      expect(formField.gridDataHints).toBe(gridDataHints);
    });

  });

  describe('property label position', function() {
    var formField, model;

    beforeEach(function() {
      model = helper.createFieldModel();
      formField = new scout.StringField();
      formField.init(model);
    });

    describe('position on_field', function() {

      beforeEach(function() {
        formField.label = 'labelName';
        formField.labelPosition = scout.FormField.LabelPosition.ON_FIELD;
      });

      it('sets the label as placeholder', function() {
        formField.render();
        expect(formField.$label.html()).toBeFalsy();
        expect(formField.$field.attr('placeholder')).toBe(formField.label);
      });

      it('does not call field._renderLabelPosition initially', function() {
        formField.render();
        expect(formField.$label.html()).toBeFalsy();
        expect(formField.$field.attr('placeholder')).toBe(formField.label);

        spyOn(formField, '_renderLabelPosition');
        expect(formField._renderLabelPosition).not.toHaveBeenCalled();
      });

    });

    describe('position top', function() {

      beforeEach(function() {
        formField.label = 'labelName';
        formField.labelPosition = scout.FormField.LabelPosition.TOP;
      });

      it('guarantees a minimum height if label is empty', function() {
        formField.label = '';
        formField.render();
        expect(formField.$label.html()).toBe('&nbsp;');
        expect(formField.$label).toBeVisible();
      });

    });

    it('does not display a status if status visible = false', function() {
      formField.statusVisible = false;
      formField.render();

      expect(formField.$status.isVisible()).toBe(false);
    });

  });

  describe('disabled style read-only', function() {

    var formField;

    beforeEach(function() {
      formField = helper.createField('StringField', session.desktop);
    });

    it('sets css class \'read-only\' when field is disabled and setDisabledStyle has been called ', function() {
      formField.render();
      formField.setDisabledStyle(scout.Widget.DisabledStyle.READ_ONLY);
      formField.setEnabled(false);
      expect(formField.$field.attr('class')).toContain('read-only');
      formField.setEnabled(true);
      expect(formField.$field.attr('class')).not.toContain('read-only');
    });

  });

  describe('property tooltipText', function() {
    var formField, model;

    beforeEach(function() {
      model = helper.createFieldModel();
      formField = createFormField(model);
    });

    it('adds class has-tooltip if there is a tooltip text', function() {
      formField.tooltipText = 'hello';
      formField.render();
      expect(formField.$container).toHaveClass('has-tooltip');

      formField.setTooltipText(null);
      expect(formField.$container).not.toHaveClass('has-tooltip');
    });

  });

  describe('property menus', function() {
    var formField, model;

    beforeEach(function() {
      model = helper.createFieldModel();
      formField = createFormField(model);
    });

    it('adds class has-menus if there are menus', function() {
      var menu = scout.create('Menu', {
        parent: formField
      });
      formField.setMenusVisible(true);
      formField.setMenus([menu]);
      formField.render();
      expect(formField.$container).toHaveClass('has-menus');

      formField.setMenus([]);
      expect(formField.$container).not.toHaveClass('has-menus');
    });

    it('adds class has-menus has-tooltip if there are menus and a tooltip', function() {
      var menu = scout.create('Menu', {
        parent: formField
      });
      formField.setMenusVisible(true);
      formField.setMenus([menu]);
      formField.setTooltipText('hello');
      formField.render();
      expect(formField.$container).toHaveClass('has-menus');
      expect(formField.$container).toHaveClass('has-tooltip');

      formField.setMenus([]);
      formField.setTooltipText(null);
      expect(formField.$container).not.toHaveClass('has-menus');
      expect(formField.$container).not.toHaveClass('has-tooltip');
    });

  });

  describe('property status visible', function() {
    var formField, model;

    beforeEach(function() {
      model = helper.createFieldModel();
      formField = createFormField(model);
    });

    it('shows a status if status visible = true', function() {
      formField.statusVisible = true;
      formField.render();

      expect(formField.$status.isVisible()).toBe(true);
    });

    it('does not show a status if status visible = false', function() {
      formField.statusVisible = false;
      formField.render();

      expect(formField.$status.isVisible()).toBe(false);
    });

    it('shows a status even though status visible is false but tooltipText is set', function() {
      formField.statusVisible = false;
      formField.tooltipText = 'hello';
      formField.render();

      expect(formField.$status.isVisible()).toBe(true);
      formField.setTooltipText(null);
      expect(formField.$status.isVisible()).toBe(false);
    });

    it('shows a status even though status visible is false but errorStatus is set', function() {
      formField.statusVisible = false;
      formField.errorStatus = new scout.Status({
        message: 'error',
        severity: scout.Status.Severity.ERROR
      });
      formField.render();

      expect(formField.$status.isVisible()).toBe(true);
      formField.setErrorStatus(null);
      expect(formField.$status.isVisible()).toBe(false);
    });

  });

  describe('property visible', function() {
    var formField, model;

    beforeEach(function() {
      model = helper.createFieldModel();
      formField = createFormField(model);
    });

    it('shows the field if visible = true', function() {
      formField.visible = true;
      formField.render();

      expect(formField.$container.isVisible()).toBe(true);
    });

    it('does not show the field if visible = false', function() {
      formField.visible = false;
      formField.render();

      expect(formField.$container.isVisible()).toBe(false);
    });

    it('hides the status message if field is made invisible', function() {
      formField.errorStatus = new scout.Status({
        message: 'error',
        severity: scout.Status.Severity.ERROR
      });
      formField.render();

      expect(formField.$container.isVisible()).toBe(true);
      expect(formField.tooltip.rendered).toBe(true);
      expect($('.tooltip').length).toBe(1);

      formField.setVisible(false);
      expect(formField.tooltip).toBe(null);
      expect($('.tooltip').length).toBe(0);
    });

    it('shows the status message if field is made visible', function() {
      formField.errorStatus = new scout.Status({
        message: 'error',
        severity: scout.Status.Severity.ERROR
      });
      formField.visible = false;
      formField.render();

      expect(formField.$container.isVisible()).toBe(false);
      expect(formField.tooltip).toBe(null);
      expect($('.tooltip').length).toBe(0);

      formField.setVisible(true);
      expect(formField.tooltip.rendered).toBe(true);
      expect($('.tooltip').length).toBe(1);
    });

  });

});
