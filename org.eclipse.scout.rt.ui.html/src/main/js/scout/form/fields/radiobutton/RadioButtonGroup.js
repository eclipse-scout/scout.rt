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
scout.RadioButtonGroup = function() {
  scout.RadioButtonGroup.parent.call(this);
  this.logicalGrid = scout.create('scout.HorizontalGrid');
  this.layoutConfig = null;
  this.fields = [];
  this.radioButtons = [];
  this.gridColumnCount = scout.RadioButtonGroup.DEFAULT_GRID_COLUMN_COUNT;
  this.selectedButton = null;
  this.$body = null;
  this.lookupStatus = null;
  this.lookupCall = null;

  this._lookupExecuted = false;
  this._selectButtonLocked = false;
  this._pendingLookup = null;
  this._lookupInProgress = false;
  this._currentLookupCall = null;

  this._addWidgetProperties(['fields']);
  this._addCloneProperties(['lookupCall', 'layoutConfig', 'gridColumnCount']);
  this._buttonPropertyChangeHandler = this._onButtonPropertyChange.bind(this);
};
scout.inherits(scout.RadioButtonGroup, scout.ValueField);

scout.RadioButtonGroup.DEFAULT_GRID_COLUMN_COUNT = -1;

scout.RadioButtonGroup.ErrorCode = {
  NO_DATA: 1
};

scout.RadioButtonGroup.prototype._init = function(model) {
  scout.RadioButtonGroup.parent.prototype._init.call(this, model);

  this._setLayoutConfig(this.layoutConfig);
  this._setGridColumnCount(this.gridColumnCount);
};

scout.RadioButtonGroup.prototype._initValue = function(value) {
  if (this.lookupCall) {
    this._setLookupCall(this.lookupCall);
  }
  // must be called before value is set
  this._setFields(this.fields);
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

scout.RadioButtonGroup.prototype._initButtons = function() {
  this.radioButtons = this.fields.filter(function(formField) {
    return formField instanceof scout.RadioButton;
  });
  this.radioButtons.forEach(this._initButton.bind(this));
};

scout.RadioButtonGroup.prototype._initButton = function(button) {
  if (button.events.count('propertyChange', this._buttonPropertyChangeHandler) === 0) {
    button.on('propertyChange', this._buttonPropertyChangeHandler);
  }
  if (button.selected) {
    this.setValue(button.radioValue);
    this.selectButton(button);
    if (button.focused) {
      this.setFocused(true);
    }
  }
};

scout.RadioButtonGroup.prototype._render = function() {
  this.addContainer(this.$parent, 'radiobutton-group');
  this.addLabel();
  this.addMandatoryIndicator();

  this.$body = this.$container.appendDiv('radiobutton-group-body');
  this.htmlBody = scout.HtmlComponent.install(this.$body, this.session);
  this.htmlBody.setLayout(this._createBodyLayout());

  // fields are rendered in _renderFields
  this.addField(this.$body);
  this.addStatus();
};

scout.RadioButtonGroup.prototype._renderProperties = function() {
  scout.RadioButtonGroup.parent.prototype._renderProperties.call(this);
  this._renderFields();
  this._renderLayoutConfig();
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
 * @override
 */
scout.RadioButtonGroup.prototype.visitFields = function(visitor) {
  var treeVisitResult = scout.RadioButtonGroup.parent.prototype.visitFields.call(this, visitor);
  if(treeVisitResult === scout.TreeVisitResult.TERMINATE){
    return scout.TreeVisitResult.TERMINATE;
  }

  if(treeVisitResult === scout.TreeVisitResult.SKIP_SUBTREE){
    return;
  }

  var fields = this.getFields();
  for(var i = 0; i < fields.length; i++){
    var field = fields[i];
    treeVisitResult = field.visitFields(visitor);
    if(treeVisitResult === scout.TreeVisitResult.TERMINATE){
      return scout.TreeVisitResult.TERMINATE;
    }
  }
};

/**
 * @override
 */
scout.RadioButtonGroup.prototype.getFocusableElement = function() {
  // The first button may not be focusable because it is not selected and therefore has no tab index -> find the first focusable button
  return this.session.focusManager.findFirstFocusableElement(this.$container);
};

scout.RadioButtonGroup.prototype.setFields = function(fields) {
  this.setProperty('fields', fields);
};

scout.RadioButtonGroup.prototype._setFields = function(fields) {
  this._setProperty('fields', fields);
  this._initButtons();
};

scout.RadioButtonGroup.prototype._renderFields = function() {
  this._ensureLookupCallExecuted();
  this.fields.forEach(function(formField) {
    formField.render(this.$body);

    // set each children layout data to logical grid data
    formField.setLayoutData(new scout.LogicalGridData(formField));

    this._linkWithLabel(formField.$field);
  }, this);
  this._provideTabIndex(); // depends on rendered fields
  this.invalidateLogicalGrid(false);
  this.validateLayoutTree(false); // prevent flickering
};

/**
 * @override
 */
scout.RadioButtonGroup.prototype._renderEnabled = function() {
  scout.RadioButtonGroup.parent.prototype._renderEnabled.call(this);
  this._provideTabIndex();
};

/**
 * Set the selected (or first if none is selected) to tabbable
 */
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

  if (!this.initialized && this.lookupCall) {
    // lookup call may not be started during field initialization. otherwise lookup prepare listeners cannot be attached.
    // do not validate now (as there are no buttons yet, because the lookup call has not yet been executed).
    // validation will be done later again when the lookup call is executed.
    return value;
  }

  var lookupScheduled = this._ensureLookupCallExecuted();
  if (lookupScheduled) {
    // the first lookup was scheduled now: buttons are not yet available, not possible to select one. will be done later as soon as the lookup call is finished.
    return value;
  }

  // only show error if value is not null or undefined
  var buttonToSelect = this.getButtonForRadioValue(value);
  if (!buttonToSelect && value !== null && value !== undefined && !this._lookupInProgress) {
    throw this.session.text('InvalidValueMessageX', value);
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

scout.RadioButtonGroup.prototype.selectFirstButton = function() {
  this.selectButtonByIndex(0);
};

scout.RadioButtonGroup.prototype.selectLastButton = function() {
  this.selectButtonByIndex(this.radioButtons.length - 1);
};

scout.RadioButtonGroup.prototype.selectButtonByIndex = function(index) {
  if (this.radioButtons.length && index >= 0 && index < this.radioButtons.length) {
    this.selectButton(this.radioButtons[index]);
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
  var newFields = this.fields.slice();
  newFields.push(radioButton);
  this.setFields(newFields);
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
  } else if (event.propertyName === 'focused') {
    this.setFocused(event.newValue);
  }
};

scout.RadioButtonGroup.prototype._setLookupCall = function(lookupCall) {
  this._setProperty('lookupCall', scout.LookupCall.ensure(lookupCall, this.session));
  this._lookupExecuted = false;
  if (this.rendered) {
    this._ensureLookupCallExecuted();
  }
};

/**
 * @return true if a lookup call execution has been scheduled now. false otherwise.
 */
scout.RadioButtonGroup.prototype._ensureLookupCallExecuted = function() {
  if (!this.lookupCall) {
    return false;
  }
  if (this._lookupExecuted) {
    return false;
  }
  this._lookupByAll();
  return true;
};

/**
 * @override
 */
scout.RadioButtonGroup.prototype._createLoadingSupport = function() {
  return new scout.LoadingSupport({
    widget: this,
    $container: function() {
      return this.$body;
    }.bind(this)
  });
};

scout.RadioButtonGroup.prototype._lookupByAll = function() {
  if (!this.lookupCall) {
    return;
  }

  this._clearPendingLookup();

  var deferred = $.Deferred();
  var doneHandler = function(result) {
    this._lookupByAllDone(result);
    deferred.resolve(result);
  }.bind(this);

  this._executeLookup(this.lookupCall.cloneForAll(), true)
    .done(doneHandler);

  return deferred.promise();
};

scout.RadioButtonGroup.prototype._clearPendingLookup = function() {
  if (this._pendingLookup) {
    clearTimeout(this._pendingLookup);
    this._pendingLookup = null;
  }
};

/**
 * A wrapper function around lookup calls used to set the _lookupInProgress flag, and display the state in the UI.
 */
scout.RadioButtonGroup.prototype._executeLookup = function(lookupCall, abortExisting) {
  this._lookupInProgress = true;
  this.setLoading(true);

  if (abortExisting && this._currentLookupCall) {
    this._currentLookupCall.abort();
  }
  this._currentLookupCall = lookupCall;
  this.trigger('prepareLookupCall', {
    lookupCall: lookupCall
  });

  return lookupCall
    .execute()
    .always(function() {
      this._lookupInProgress = false;
      this._lookupExecuted = true;
      this._currentLookupCall = null;
      this.setLoading(false);
      this._clearLookupStatus();
    }.bind(this));
};

scout.RadioButtonGroup.prototype._lookupByAllDone = function(result) {
  try {

    if (result.exception) {
      // Oops! Something went wrong while the lookup has been processed.
      this.setErrorStatus(scout.Status.error({
        message: result.exception
      }));
      return;
    }

    // 'No data' case
    if (result.lookupRows.length === 0) {
      this.setLookupStatus(scout.Status.warning({
        message: this.session.text('SmartFieldNoDataFound'),
        code: scout.RadioButtonGroup.ErrorCode.NO_DATA
      }));
      return;
    }

    this._populateRadioButtonGroup(result);

  } finally {
    this.trigger('lookupCallDone', {
      result: result
    });
  }
};

scout.RadioButtonGroup.prototype._populateRadioButtonGroup = function(result) {
  var lookupRows = result.lookupRows;
  var newFields = this.fields.slice();
  lookupRows.forEach(function(lookupRow) {
    newFields.push(this._createLookupRowRadioButton(lookupRow));
  }, this);
  this.setFields(newFields);

  // because the lookup call is asynchronus, reset the value so that it is revalidated.
  this.setValue(this.value);
  // also select the button (the line above does not change the value, therefore _valueChanged is not called)
  this.selectButton(this.getButtonForRadioValue(this.value));
};

scout.RadioButtonGroup.prototype._clearLookupStatus = function() {
  this.setLookupStatus(null);
};

scout.RadioButtonGroup.prototype.setLookupStatus = function(lookupStatus) {
  this.setProperty('lookupStatus', lookupStatus);
  if (this.rendered) {
    this._renderErrorStatus();
  }
};

scout.RadioButtonGroup.prototype._errorStatus = function() {
  return this.lookupStatus || this.errorStatus;
};

scout.RadioButtonGroup.prototype._createLookupRowRadioButton = function(lookupRow) {
  var button = {
    parent: this,
    label: lookupRow.text,
    radioValue: lookupRow.key,
    lookupRow: lookupRow
  };

  if (lookupRow.iconId) {
    button.iconId = lookupRow.iconId;
  }
  if (lookupRow.tooltipText) {
    button.tooltipText = lookupRow.tooltipText;
  }
  if (lookupRow.backgroundColor) {
    button.backgroundColor = lookupRow.backgroundColor;
  }
  if (lookupRow.foregroundColor) {
    button.foregroundColor = lookupRow.foregroundColor;
  }
  if (lookupRow.font) {
    button.font = lookupRow.font;
  }
  if (lookupRow.enabled === false) {
    button.enabled = false;
  }
  if (lookupRow.active === false) {
    button.visible = false;
  }
  if (lookupRow.cssClass) {
    button.cssClass = lookupRow.cssClass;
  }

  return scout.create('RadioButton', button);
};

scout.RadioButtonGroup.prototype.clone = function(model, options) {
  var clone = scout.RadioButtonGroup.parent.prototype.clone.call(this, model, options);
  this._deepCloneProperties(clone, 'fields', options);
  clone._initButtons();
  return clone;
};
