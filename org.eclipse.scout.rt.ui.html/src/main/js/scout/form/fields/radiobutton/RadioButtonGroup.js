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
scout.RadioButtonGroup = function() {
  scout.RadioButtonGroup.parent.call(this);
  this._addAdapterProperties('formFields');
  this.formFields = [];
  this.radioButtons = [];
  this.selectedButton = null;
  this.$body;
};

scout.inherits(scout.RadioButtonGroup, scout.ValueField);

/**
 * @override ModelAdapter.js
 */
scout.RadioButtonGroup.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.RadioButtonGroup.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  keyStrokeContext.registerKeyStroke([
    new scout.RadioButtonGroupLeftKeyStroke(this),
    new scout.RadioButtonGroupRightKeyStroke(this)
  ]);
};

scout.RadioButtonGroup.prototype._init = function(model) {
  scout.RadioButtonGroup.parent.prototype._init.call(this, model);

  this.formFields.forEach(function(formField) {
    if (formField instanceof scout.RadioButton) {
      this.radioButtons.push(formField);
      if (formField.selected) {
        this.selectedButton = formField;
      }
    }
  }, this);
};

scout.RadioButtonGroup.prototype._render = function($parent) {
  var env = scout.HtmlEnvironment,
    htmlBodyContainer;

  this.addContainer($parent, 'radiobutton-group');

  this.$body = this.$container.appendDiv('radiobutton-group-body');
  htmlBodyContainer = new scout.HtmlComponent(this.$body, this.session);
  htmlBodyContainer.setLayout(new scout.LogicalGridLayout(env.smallColumnGap, env.formRowGap));

  this.formFields.forEach(function(formField) {
    formField.render(this.$body);
  }, this);

  this.addLabel();
  this.addMandatoryIndicator();
  this.addField(this.$body);
  this.addStatus();
};

/**
 * @override
 */
scout.RadioButtonGroup.prototype._renderEnabled = function() {
  scout.RadioButtonGroup.parent.prototype._renderEnabled.call(this);
  this._provideTabIndex();
};

scout.RadioButtonGroup.prototype._provideTabIndex = function() {
  var tabSet;
  this.radioButtons.forEach(function(radioButton) {
    if (radioButton.enabled && this.enabled && !tabSet) {
      radioButton.setTabbable(true);
      tabSet = radioButton;
    } else if (tabSet && this.enabled && radioButton.enabled && radioButton.selected) {
      tabSet.setTabbable(false);
      radioButton.setTabbable(true);
      tabSet = radioButton;
    } else {
      radioButton.setTabbable(false);
    }
  }, this);
};

scout.RadioButtonGroup.prototype.selectButton = function(radioButtonToSelect) {
  this.selectedButton = null;
  this.radioButtons.forEach(function(radioButton) {
    if (radioButton === radioButtonToSelect) {
      if (!radioButton.enabled) {
        return;
      }
      radioButton.setSelected(true);
      radioButton.setTabbable(true);
      this.selectedButton = radioButton;
    } else {
      radioButton.setSelected(false);
      radioButton.setTabbable(false);
    }
  }, this);
};

scout.RadioButtonGroup.prototype.addButton = function(radioButton) {
  this.formFields.push(radioButton);
  this.radioButtons.push(radioButton);
};
