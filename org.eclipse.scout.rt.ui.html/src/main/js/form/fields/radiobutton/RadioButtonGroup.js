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
import {RadioButtonGroupLayout} from '../../../index';
import {RadioButton} from '../../../index';
import {LogicalGridData} from '../../../index';
import {Status} from '../../../index';
import {LogicalGridLayoutConfig} from '../../../index';
import * as $ from 'jquery';
import {RadioButtonGroupRightKeyStroke} from '../../../index';
import {ValueField} from '../../../index';
import {RadioButtonGroupGridConfig} from '../../../index';
import {RadioButtonGroupLeftKeyStroke} from '../../../index';
import {HtmlComponent} from '../../../index';
import {scout} from '../../../index';
import {LoadingSupport} from '../../../index';
import {objects} from '../../../index';
import {TreeVisitResult} from '../../../index';
import {LookupCall} from '../../../index';
import {arrays} from '../../../index';

export default class RadioButtonGroup extends ValueField {

constructor() {
  super();
  this.logicalGrid = scout.create('scout.HorizontalGrid');
  this.layoutConfig = null;
  this.fields = [];
  this.radioButtons = [];
  this.gridColumnCount = RadioButtonGroup.DEFAULT_GRID_COLUMN_COUNT;
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
}


static DEFAULT_GRID_COLUMN_COUNT = -1;

static ErrorCode = {
  NO_DATA: 1
};

_init(model) {
  super._init( model);

  this._setLayoutConfig(this.layoutConfig);
  this._setGridColumnCount(this.gridColumnCount);
}

_initValue(value) {
  if (this.lookupCall) {
    this._setLookupCall(this.lookupCall);
  }
  // must be called before value is set
  this._setFields(this.fields);
  super._initValue( value);
}

/**
 * @override ModelAdapter.js
 */
_initKeyStrokeContext() {
  super._initKeyStrokeContext();

  this.keyStrokeContext.registerKeyStroke([
    new RadioButtonGroupLeftKeyStroke(this),
    new RadioButtonGroupRightKeyStroke(this)
  ]);
}

_initButtons() {
  this.radioButtons = this.fields.filter(function(formField) {
    return formField instanceof RadioButton;
  });
  this.radioButtons.forEach(this._initButton.bind(this));
}

_initButton(button) {
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
}

_render() {
  this.addContainer(this.$parent, 'radiobutton-group');
  this.addLabel();
  this.addMandatoryIndicator();

  this.$body = this.$container.appendDiv('radiobutton-group-body');
  this.htmlBody = HtmlComponent.install(this.$body, this.session);
  this.htmlBody.setLayout(this._createBodyLayout());

  // fields are rendered in _renderFields
  this.addField(this.$body);
  this.addStatus();
}

_renderProperties() {
  super._renderProperties();
  this._renderFields();
  this._renderLayoutConfig();
}

_createBodyLayout() {
  return new RadioButtonGroupLayout(this, this.layoutConfig);
}

/**
 * @override Widgets.js
 */
_setLogicalGrid(logicalGrid) {
  super._setLogicalGrid( logicalGrid);
  if (this.logicalGrid) {
    this.logicalGrid.setGridConfig(new RadioButtonGroupGridConfig());
  }
}

/**
 * @override Widgets.js
 */
invalidateLogicalGrid(invalidateLayout) {
  super.invalidateLogicalGrid( false);
  if (scout.nvl(invalidateLayout, true) && this.rendered) {
    this.htmlBody.invalidateLayoutTree();
  }
}

setLayoutConfig(layoutConfig) {
  this.setProperty('layoutConfig', layoutConfig);
}

_setLayoutConfig(layoutConfig) {
  if (!layoutConfig) {
    layoutConfig = new LogicalGridLayoutConfig();
  }
  this._setProperty('layoutConfig', LogicalGridLayoutConfig.ensure(layoutConfig));
}

_renderLayoutConfig() {
  this.layoutConfig.applyToLayout(this.htmlBody.layout);
  if (this.rendered) {
    this.htmlBody.invalidateLayoutTree();
  }
}

/**
 * @override ValueField.js
 */
isClearable() {
  return false;
}

getFields() {
  return this.fields;
}

/**
 * @override
 */
visitFields(visitor) {
  var treeVisitResult = super.visitFields( visitor);
  if (treeVisitResult === TreeVisitResult.TERMINATE) {
    return TreeVisitResult.TERMINATE;
  }

  if (treeVisitResult === TreeVisitResult.SKIP_SUBTREE) {
    return;
  }

  var fields = this.getFields();
  for (var i = 0; i < fields.length; i++) {
    var field = fields[i];
    treeVisitResult = field.visitFields(visitor);
    if (treeVisitResult === TreeVisitResult.TERMINATE) {
      return TreeVisitResult.TERMINATE;
    }
  }
}

/**
 * @override
 */
getFocusableElement() {
  // The first button may not be focusable because it is not selected and therefore has no tab index -> find the first focusable button
  return this.session.focusManager.findFirstFocusableElement(this.$container);
}

setFields(fields) {
  this.setProperty('fields', fields);
}

_setFields(fields) {
  this._setProperty('fields', fields);
  this._initButtons();
}

_renderFields() {
  this._ensureLookupCallExecuted();
  this.fields.forEach(function(formField) {
    formField.render(this.$body);

    // set each children layout data to logical grid data
    formField.setLayoutData(new LogicalGridData(formField));

    this._linkWithLabel(formField.$field);
  }, this);
  this._provideTabIndex(); // depends on rendered fields
  this.invalidateLogicalGrid();
  this.validateLayoutTree(); // prevent flickering
}

/**
 * @override
 */
_renderEnabled() {
  super._renderEnabled();
  this._provideTabIndex();
}

/**
 * Set the selected (or first if none is selected) to tabbable
 */
_provideTabIndex() {
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
}

setGridColumnCount(gridColumnCount) {
  this.setProperty('gridColumnCount', gridColumnCount);
}

_setGridColumnCount(gridColumnCount) {
  if (gridColumnCount < 0) {
    gridColumnCount = this._calcDefaultGridColumnCount();
  }
  if (gridColumnCount === this.gridColumnCount) {
    return false;
  }

  this._setProperty('gridColumnCount', gridColumnCount);
  this.invalidateLogicalGrid();
  return true;
}

_calcDefaultGridColumnCount() {
  var height = 1,
    hints = this.gridDataHints;
  if (hints && hints.h > 1) {
    height = hints.h;
  }
  return Math.ceil(this.fields.length / height);
}

getButtonForRadioValue(radioValue) {
  if (radioValue === null) {
    return null;
  }
  return arrays.find(this.radioButtons, function(button) {
    return objects.equals(button.radioValue, radioValue);
  });
}

/**
 * Search and then select the button with the corresponding radioValue
 */
_validateValue(value) {
  super._validateValue( value);

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
}

_valueChanged() {
  super._valueChanged();
  // Don't select button during initialization if value is null to not override selected state of a button
  if (this.value !== null || this.initialized) {
    this.selectButton(this.getButtonForRadioValue(this.value));
  }
}

selectFirstButton() {
  this.selectButtonByIndex(0);
}

selectLastButton() {
  this.selectButtonByIndex(this.radioButtons.length - 1);
}

selectButtonByIndex(index) {
  if (this.radioButtons.length && index >= 0 && index < this.radioButtons.length) {
    this.selectButton(this.radioButtons[index]);
  }
}

selectButton(radioButton) {
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
}

getTabbableButton() {
  return arrays.find(this.radioButtons, function(button) {
    return button.visible && button.isTabbable();
  });
}

insertButton(radioButton) {
  var newFields = this.fields.slice();
  newFields.push(radioButton);
  this.setFields(newFields);
}

_onButtonPropertyChange(event) {
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
}

_setLookupCall(lookupCall) {
  this._setProperty('lookupCall', LookupCall.ensure(lookupCall, this.session));
  this._lookupExecuted = false;
  if (this.rendered) {
    this._ensureLookupCallExecuted();
  }
}

/**
 * @return true if a lookup call execution has been scheduled now. false otherwise.
 */
_ensureLookupCallExecuted() {
  if (!this.lookupCall) {
    return false;
  }
  if (this._lookupExecuted) {
    return false;
  }
  this._lookupByAll();
  return true;
}

/**
 * @override
 */
_createLoadingSupport() {
  return new LoadingSupport({
    widget: this,
    $container: function() {
      return this.$body;
    }.bind(this)
  });
}

_lookupByAll() {
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
}

_clearPendingLookup() {
  if (this._pendingLookup) {
    clearTimeout(this._pendingLookup);
    this._pendingLookup = null;
  }
}

/**
 * A wrapper function around lookup calls used to set the _lookupInProgress flag, and display the state in the UI.
 */
_executeLookup(lookupCall, abortExisting) {
  if (abortExisting && this._currentLookupCall) {
    this._currentLookupCall.abort();
  }

  this._lookupInProgress = true;
  this.setLoading(true);

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
}

_lookupByAllDone(result) {
  try {

    if (result.exception) {
      // Oops! Something went wrong while the lookup has been processed.
      this.setErrorStatus(Status.error({
        message: result.exception
      }));
      return;
    }

    // 'No data' case
    if (result.lookupRows.length === 0) {
      this.setLookupStatus(Status.warning({
        message: this.session.text('SmartFieldNoDataFound'),
        code: RadioButtonGroup.ErrorCode.NO_DATA
      }));
      return;
    }

    this._populateRadioButtonGroup(result);

  } finally {
    this.trigger('lookupCallDone', {
      result: result
    });
  }
}

_populateRadioButtonGroup(result) {
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
}

_clearLookupStatus() {
  this.setLookupStatus(null);
}

setLookupStatus(lookupStatus) {
  this.setProperty('lookupStatus', lookupStatus);
  if (this.rendered) {
    this._renderErrorStatus();
  }
}

_errorStatus() {
  return this.lookupStatus || this.errorStatus;
}

_createLookupRowRadioButton(lookupRow) {
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
}

clone(model, options) {
  var clone = super.clone( model, options);
  this._deepCloneProperties(clone, 'fields', options);
  clone._initButtons();
  return clone;
}
}
