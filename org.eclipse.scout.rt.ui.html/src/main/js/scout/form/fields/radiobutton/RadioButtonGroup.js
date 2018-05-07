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
scout.RadioButtonGroup = function() {
  scout.RadioButtonGroup.parent.call(this);
  this._addWidgetProperties('fields');
  this.logicalGrid = scout.create('scout.HorizontalGrid');
  this.layoutConfig = null;
  this.fields = [];
  this.radioButtons = [];
  this.gridColumnCount = scout.RadioButtonGroup.DEFAULT_GRID_COLUMN_COUNT;
  this.selectedButton = null;
  this.$body = null;
  this._selectButtonLocked = false;
  this._buttonPropertyChangeHandler = this._onButtonPropertyChange.bind(this);
};
scout.inherits(scout.RadioButtonGroup, scout.ValueField);

scout.RadioButtonGroup.DEFAULT_GRID_COLUMN_COUNT = -1;

scout.RadioButtonGroup.prototype._init = function(model) {
  scout.RadioButtonGroup.parent.prototype._init.call(this, model);

  this._setLayoutConfig(this.layoutConfig);
  this._setGridColumnCount(this.gridColumnCount);
};

scout.RadioButtonGroup.prototype._initValue = function(value) {
  // Initialize buttons first before calling set value, otherwise value could not be synchronized to the buttons
  this.fields.forEach(function(formField) {
    if (formField instanceof scout.RadioButton) {
      this.radioButtons.push(formField);
      this._initButton(formField);
    }
  }, this);
  scout.RadioButtonGroup.parent.prototype._initValue.call(this, value);
};

/**
 * @override ModelAdapter.js
 */
scout.RadioButtonGroup.prototype._initKeyStrokeContext = function() {
  scout.RadioButtonGroup.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke([
    new scout.RadioButtonGroupLeftKeyStroke(this),
    new scout.RadioButtonGroupRightKeyStroke(this)
  ]);
};

scout.RadioButtonGroup.prototype._initButton = function(button) {
  button.on('propertyChange', this._buttonPropertyChangeHandler);
  if (button.selected) {
    this.setValue(button.radioValue);
    this.selectButton(button);
  }
};

scout.RadioButtonGroup.prototype._createBodyLayout = function() {
  return new scout.LogicalGridLayout(this, this.layoutConfig);
};

/**
 * @override Widgets.js
 */
scout.RadioButtonGroup.prototype._setLogicalGrid = function(logicalGrid) {
  scout.RadioButtonGroup.parent.prototype._setLogicalGrid.call(this, logicalGrid);
  if (this.logicalGrid) {
    this.logicalGrid.setGridConfig(new scout.RadioButtonGroupGridConfig());
  }
};

/**
 * @override Widgets.js
 */
scout.RadioButtonGroup.prototype.invalidateLogicalGrid = function(invalidateLayout) {
  scout.RadioButtonGroup.parent.prototype.invalidateLogicalGrid.call(this, false);
  if (scout.nvl(invalidateLayout, true) && this.rendered) {
    this.htmlBody.invalidateLayoutTree();
  }
};

scout.RadioButtonGroup.prototype.setLayoutConfig = function(layoutConfig) {
  this.setProperty('layoutConfig', layoutConfig);
};

scout.RadioButtonGroup.prototype._setLayoutConfig = function(layoutConfig) {
  if (!layoutConfig) {
    layoutConfig = new scout.RadioButtonGroupLayoutConfig();
  }
  this._setProperty('layoutConfig', scout.RadioButtonGroupLayoutConfig.ensure(layoutConfig));
};

scout.RadioButtonGroup.prototype._renderLayoutConfig = function() {
  this.layoutConfig.applyToLayout(this.htmlBody.layout);
  if (this.rendered) {
    this.htmlBody.invalidateLayoutTree();
  }
};

/**
 * @override ValueField.js
 */
scout.RadioButtonGroup.prototype.isClearable = function() {
  return false;
};

scout.RadioButtonGroup.prototype.getFields = function() {
  return this.fields;
};

/**
 * @override FormField.js
 */
scout.RadioButtonGroup.prototype.visitFields = function(visitor) {
  scout.RadioButtonGroup.parent.prototype.visitFields.call(this, visitor);
  this.fields.forEach(function(field) {
    field.visitFields(visitor);
  });
};

/**
 * @override
 */
scout.RadioButtonGroup.prototype.activate = function() {
  // The first button may not be focusable because it is not selected and therefore has no tab index -> find the first focusable button
  var element = this.session.focusManager.findFirstFocusableElement(this.$container);
  if (element) {
    element.focus();
  }
};

scout.RadioButtonGroup.prototype._render = function() {
  this.addContainer(this.$parent, 'radiobutton-group');
  this.addLabel();
  this.addMandatoryIndicator();

  this.$body = this.$container.appendDiv('radiobutton-group-body');
  this.htmlBody = scout.HtmlComponent.install(this.$body, this.session);
  this.htmlBody.setLayout(this._createBodyLayout());

  this.fields.forEach(function(formField) {
    formField.render(this.$body);

    // set each children layout data to logical grid data
    formField.setLayoutData(new scout.LogicalGridData(formField));

    this._linkWithLabel(formField.$field);
  }, this);

  this.addField(this.$body);
  this.addStatus();
};

scout.RadioButtonGroup.prototype._renderProperties = function() {
  scout.RadioButtonGroup.parent.prototype._renderProperties.call(this);
  this._renderLayoutConfig();
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
    if (radioButton.enabledComputed && this.enabledComputed && !tabSet) {
      radioButton.setTabbable(true);
      tabSet = radioButton;
    } else if (tabSet && this.enabledComputed && radioButton.enabledComputed && radioButton.selected) {
      tabSet.setTabbable(false);
      radioButton.setTabbable(true);
      tabSet = radioButton;
    } else {
      radioButton.setTabbable(false);
    }
  }, this);
};

scout.RadioButtonGroup.prototype.setGridColumnCount = function(gridColumnCount) {
  this.setProperty('gridColumnCount', gridColumnCount);
};

scout.RadioButtonGroup.prototype._setGridColumnCount = function(gridColumnCount) {
  if (gridColumnCount < 0) {
    gridColumnCount = this._calcDefaultGridColumnCount();
  }
  if (gridColumnCount === this.gridColumnCount) {
    return false;
  }

  this._setProperty('gridColumnCount', gridColumnCount);
  this.invalidateLogicalGrid();
  return true;
};

scout.RadioButtonGroup.prototype._calcDefaultGridColumnCount = function() {
  var height = 1,
    hints = this.gridDataHints;
  if (hints && hints.h > 1) {
    height = hints.h;
  }
  return Math.ceil(this.fields.length / height);
};

scout.RadioButtonGroup.prototype.getButtonForRadioValue = function(radioValue) {
  if (radioValue === null) {
    return null;
  }
  return scout.arrays.find(this.radioButtons, function(button) {
    return scout.objects.equals(button.radioValue, radioValue);
  });
};

/**
 * Search and then select the button with the corresponding radioValue
 */
scout.RadioButtonGroup.prototype._validateValue = function(value) {
  scout.RadioButtonGroup.parent.prototype._validateValue.call(this, value);

  // only show error if value is not null or undefined
  var buttonToSelect = this.getButtonForRadioValue(value);
  if (!buttonToSelect && value !== null && value !== undefined) {
    throw this.session.text("InvalidValueMessageX", value);
  }
  return value;
};

scout.RadioButtonGroup.prototype._valueChanged = function() {
  scout.RadioButtonGroup.parent.prototype._valueChanged.call(this);

  // Don't select button during initialization if value is null to not override selected state of a button
  if (this.value !== null || this.initialized) {
    this.selectButton(this.getButtonForRadioValue(this.value));
  }
};

scout.RadioButtonGroup.prototype.selectButton = function(radioButton) {
  if (this.selectedButton === radioButton) {
    // Already selected
    return;
  }
  if (this._selectButtonLocked) {
    // Don't execute when triggered by this function to make sure the states are updated before firing the selectButton event
    return;
  }
  this._selectButtonLocked = true;

  // Deselect previously selected button
  if (this.selectedButton) {
    // Do not unset tabbable here, because at least one button has to be tabbable even if the button is deselected
    this.selectedButton.setSelected(false);
  }

  // Select new button
  if (radioButton) {
    var tabbableButton = this.getTabbableButton();
    var needsFocus = false;
    if (tabbableButton) {
      // Only one button in the group should have a tab index -> remove it from the current tabbable button after the new button is tabbable.
      // If that button is focused the newly selected button needs to gain the focus otherwise the focus would fall back to the body.
      needsFocus = tabbableButton.isFocused();
    }
    radioButton.setSelected(true);
    radioButton.setTabbable(true);
    if (needsFocus) {
      radioButton.focus();
    }
    if (tabbableButton && tabbableButton !== radioButton) {
      tabbableButton.setTabbable(false);
    }
  }
  this._selectButtonLocked = false;
  this.setProperty('selectedButton', radioButton);
};

scout.RadioButtonGroup.prototype.getTabbableButton = function() {
  return scout.arrays.find(this.radioButtons, function(button) {
    return button.visible && button.isTabbable();
  });
};

scout.RadioButtonGroup.prototype.insertButton = function(radioButton) {
  if (this.rendered) {
    throw new Error('Inserting buttons is not supported if group is already rendered.');
  }
  this.fields.push(radioButton);
  this.radioButtons.push(radioButton);
  this._initButton(radioButton);
};

scout.RadioButtonGroup.prototype._onButtonPropertyChange = function(event) {
  if (event.propertyName === 'selected') {
    var selected = event.newValue;
    if (selected) {
      this.setValue(event.source.radioValue);
      this.selectButton(event.source);
    } else if (event.source === this.selectedButton) {
      this.selectButton(null);
    }
  }
};
