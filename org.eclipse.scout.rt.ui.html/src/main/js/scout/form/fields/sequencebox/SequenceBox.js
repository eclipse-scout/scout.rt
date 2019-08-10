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
scout.SequenceBox = function() {
  scout.SequenceBox.parent.call(this);
  this._addWidgetProperties('fields');
  this._addCloneProperties(['layoutConfig']);
  this.logicalGrid = scout.create('scout.HorizontalGrid');
  this.layoutConfig = null;
  this.fields = [];
};
scout.inherits(scout.SequenceBox, scout.CompositeField);

scout.SequenceBox.prototype._init = function(model) {
  scout.SequenceBox.parent.prototype._init.call(this, model);

  this._setLayoutConfig(this.layoutConfig);

  this._initDateFields();

  this.setErrorStatus(this.errorStatus);
  this.setTooltipText(this.tooltipText);
  this.setMenus(this.menus);
  this.setMenusVisible(this.menusVisible);
};

/**
 * Initialize all DateFields in this SequenceBox with a meaningful autoDate, except fields which already have an autoDate provided by the model.
 */
scout.SequenceBox.prototype._initDateFields = function() {
  var dateFields = this._getDateFields();
  var newAutoDate = null;
  for (var i = 0; i < dateFields.length; i++) {
    var currField = dateFields[i];
    if (currField.autoDate) {
      // is the autoDate already set by the field's model remember to not change this value.
      currField.hasModelAutoDateSet = true;
    }
    if (!currField.hasModelAutoDateSet) {
      currField.setAutoDate(newAutoDate);
    }
    newAutoDate = this._getAutoDateProposal(currField);
  }
};

scout.SequenceBox.prototype._render = function() {
  var field, i;
  this.addContainer(this.$parent, 'sequence-box');
  this.addLabel();
  this.addField(this.$parent.makeDiv());
  this.addStatus();
  this._handleStatus();
  this.htmlBody = scout.HtmlComponent.install(this.$field, this.session);
  this.htmlBody.setLayout(this._createBodyLayout());
  for (i = 0; i < this.fields.length; i++) {
    field = this.fields[i];
    field.labelUseUiWidth = true;
    field.on('propertyChange', this._onFieldPropertyChange.bind(this));
    field.render(this.$field);
    this._modifyLabel(field);

    // set each children layout data to logical grid data
    field.setLayoutData(new scout.LogicalGridData(field));
  }
};

scout.SequenceBox.prototype._renderProperties = function() {
  scout.SequenceBox.parent.prototype._renderProperties.call(this);
  this._renderLayoutConfig();
};

scout.SequenceBox.prototype._createBodyLayout = function() {
  return new scout.SequenceBoxLayout(this, this.layoutConfig);
};

/**
 * @override Widgets.js
 */
scout.SequenceBox.prototype.invalidateLogicalGrid = function(invalidateLayout) {
  scout.SequenceBox.parent.prototype.invalidateLogicalGrid.call(this, false);
  if (scout.nvl(invalidateLayout, true) && this.rendered) {
    this.htmlBody.invalidateLayoutTree();
  }
};

/**
 * @override Widgets.js
 */
scout.SequenceBox.prototype._setLogicalGrid = function(logicalGrid) {
  scout.SequenceBox.parent.prototype._setLogicalGrid.call(this, logicalGrid);
  if (this.logicalGrid) {
    this.logicalGrid.setGridConfig(new scout.SequenceBoxGridConfig());
  }
};

scout.SequenceBox.prototype.setLayoutConfig = function(layoutConfig) {
  this.setProperty('layoutConfig', layoutConfig);
};

scout.SequenceBox.prototype._setLayoutConfig = function(layoutConfig) {
  if (!layoutConfig) {
    layoutConfig = new scout.LogicalGridLayoutConfig();
  }
  this._setProperty('layoutConfig', scout.LogicalGridLayoutConfig.ensure(layoutConfig));
};

scout.SequenceBox.prototype._renderLayoutConfig = function() {
  this.layoutConfig.applyToLayout(this.htmlBody.layout);
  if (this.rendered) {
    this.htmlBody.invalidateLayoutTree();
  }
};

scout.SequenceBox.prototype._onFieldPropertyChange = function(event) {
  var visibiltyChanged = (event.propertyName === 'visible');
  if (scout.isOneOf(event.propertyName, ['errorStatus', 'tooltipText', 'visible', 'menus', 'menusVisible'])) {
    this._handleStatus(visibiltyChanged);
  }
  if (event.propertyName === 'value') {
    this._onFieldValueChange(event);
  }
};

/**
 * Moves the status relevant properties from the last visible field to the sequencebox. This makes sure that the fields inside the sequencebox have the same size.
 */
scout.SequenceBox.prototype._handleStatus = function(visibilityChanged) {
  if (visibilityChanged && this._lastVisibleField) {
    // if there is a new last visible field, make sure the status is shown on the previously last one
    this._lastVisibleField.suppressStatus = false;
    if (this._lastVisibleField.rendered) {
      this._lastVisibleField._renderErrorStatus();
      this._lastVisibleField._renderTooltipText();
      this._lastVisibleField._renderMenus();
    }
  }
  this._lastVisibleField = this._getLastVisibleField();
  if (!this._lastVisibleField) {
    return;
  }

  // Update the sequencebox with the status relevant flags
  this._isOverwritingStatusFromField = true;
  if (this._lastVisibleField.errorStatus) {
    this.setErrorStatus(this._lastVisibleField.errorStatus);
    this._isErrorStatusOverwritten = true;
  } else {
    this.setErrorStatus(this.boxErrorStatus);
    this._isErrorStatusOverwritten = false;
  }

  if (this._lastVisibleField.tooltipText) {
    this.setTooltipText(this._lastVisibleField.tooltipText);
    this._isTooltipTextOverwritten = true;
  } else {
    this.setTooltipText(this.boxTooltipText);
    this._isTooltipTextOverwritten = false;
  }

  if (this._lastVisibleField.menus && this._lastVisibleField.menus.length > 0) {
    this.setMenus(this._lastVisibleField.menus);
    this.setMenusVisible(this._lastVisibleField.menusVisible);
    this._isMenusOverwritten = true;
  } else {
    this.setMenus(this.boxMenus);
    this.setMenusVisible(this.boxMenusVisible);
    this._isMenusOverwritten = false;
  }
  this._isOverwritingStatusFromField = false;

  // Make sure the last field won't display a status
  this._lastVisibleField.suppressStatus = true;
  if (visibilityChanged) {
    // If the last field got invisible, make sure the new last field does not display a status anymore (now done by the seq box)
    if (this._lastVisibleField.rendered) {
      this._lastVisibleField._renderErrorStatus();
      this._lastVisibleField._renderTooltipText();
      this._lastVisibleField._renderMenus();
    }
  }
};

scout.SequenceBox.prototype.setErrorStatus = function(errorStatus) {
  if (this._isOverwritingStatusFromField && !this._isErrorStatusOverwritten) {
    // was not overwritten, will be overwritten now -> backup old value
    this.boxErrorStatus = this.errorStatus;
  } else if (!this._isOverwritingStatusFromField) {
    // directly changed on seq box -> update backed-up value
    this.boxErrorStatus = errorStatus;
  }
  if (this._isOverwritingStatusFromField || !this._isErrorStatusOverwritten) {
    // prevent setting value if directly changed on seq box and is already overwritten
    scout.SequenceBox.parent.prototype.setErrorStatus.call(this, errorStatus);
  }
};

scout.SequenceBox.prototype.setTooltipText = function(tooltipText) {
  if (this._isOverwritingStatusFromField && !this._isTooltipTextOverwritten) {
    // was not overwritten, will be overwritten now -> backup old value
    this.boxTooltipText = this.tooltipText;
  } else if (!this._isOverwritingStatusFromField) {
    // directly changed on seq box -> update backed-up value
    this.boxTooltipText = tooltipText;
  }
  if (this._isOverwritingStatusFromField || !this._isTooltipTextOverwritten) {
    // prevent setting value if directly changed on seq box and is already overwritten
    scout.SequenceBox.parent.prototype.setTooltipText.call(this, tooltipText);
  }
};

scout.SequenceBox.prototype.setMenus = function(menus) {
  if (this._isOverwritingStatusFromField && !this._isMenusOverwritten) {
    // was not overwritten, will be overwritten now -> backup old value
    this.boxMenus = this.menus;
  } else if (!this._isOverwritingStatusFromField) {
    // directly changed on seq box -> update backed-up value
    this.boxMenus = menus;
  }
  if (this._isOverwritingStatusFromField || !this._isMenusOverwritten) {
    // prevent setting value if directly changed on seq box and is already overwritten
    scout.SequenceBox.parent.prototype.setMenus.call(this, menus);
  }
};

scout.SequenceBox.prototype.setMenusVisible = function(menusVisible) {
  if (this._isOverwritingStatusFromField && !this._isMenusOverwritten) {
    // was not overwritten, will be overwritten now -> backup old value
    this.boxMenusVisible = this.menusVisible;
  } else if (!this._isOverwritingStatusFromField) {
    // directly changed on seq box -> update backed-up value
    this.boxMenusVisible = menusVisible;
  }
  if (this._isOverwritingStatusFromField || !this._isMenusOverwritten) {
    // prevent setting value if directly changed on seq box and is already overwritten
    scout.SequenceBox.parent.prototype.setMenusVisible.call(this, menusVisible);
  }
};

scout.SequenceBox.prototype._getLastVisibleField = function() {
  var visibleFields = this.fields.filter(function(field) {
    return field.visible;
  });
  if (visibleFields.length === 0) {
    return;
  }

  return visibleFields[visibleFields.length - 1];
};

scout.SequenceBox.prototype._onFieldValueChange = function(event) {
  if (event.source instanceof scout.DateField) {
    this._onDateFieldValueChange(event);
  }
};

scout.SequenceBox.prototype._onDateFieldValueChange = function(event) {
  // For a better user experience preselect a meaningful date on all following DateFields in the sequence box.
  var field = event.source;
  var dateFields = this._getDateFields();
  var newAutoDate = this._getAutoDateProposal(field);
  for (var i = dateFields.indexOf(field) + 1; i < dateFields.length; i++) {
    var currField = dateFields[i];
    if (!currField.hasModelAutoDateSet) {
      currField.setAutoDate(newAutoDate);
    }
    if (currField.value) {
      // only update fields in between the current field and the next field with a value set. Otherwise already set autoDates would be overwritten.
      break;
    }
  }
};

scout.SequenceBox.prototype._getDateFields = function() {
  var dateFields = this.fields.filter(function(field) {
    return field instanceof scout.DateField;
  });
  return dateFields;
};

scout.SequenceBox.prototype._getAutoDateProposal = function(field) {
  var newAutoDate = null;
  // if it's only a time field, add one hour, otherwise add one day
  if (field && field.value) {
    if (!field.hasDate && field.hasTime) {
      newAutoDate = scout.dates.shiftTime(field.value, 1, 0, 0);
    } else {
      newAutoDate = scout.dates.shift(field.value, 0, 0, 1);
    }
  }
  return newAutoDate;
};

// TODO [7.0] awe: (scout, sequence-box) remove _modifyLabel when CheckboxForm uses SequenceBox5
// The new sequence-box sets the label to invisible on the model.
scout.SequenceBox.prototype._modifyLabel = function(field) {
  if (field instanceof scout.CheckBoxField) {
    field.labelVisible = false;
  }

  if (field instanceof scout.DateField) {
    // The DateField has two inputs ($dateField and $timeField), field.$field refers to the composite which is irrelevant here
    // In order to support aria-labelledby for date fields also, the individual inputs have to be linked with the label rather than the composite
    if (field.$dateField) {
      this._linkWithLabel(field.$dateField);
    }
    if (field.$timeField) {
      this._linkWithLabel(field.$timeField);
    }
  } else if (field.$field) { // If $field is set depends on the concrete field e.g. a group box does not have a $field
    this._linkWithLabel(field.$field);
  }
};

scout.SequenceBox.prototype.setFields = function(fields) {
  if (this.rendered) {
    throw new Error('Setting fields is not supported if sequence box is already rendered.');
  }
  this.setProperty('fields', fields);
};

/**
 * @override CompositeField.js
 */
scout.SequenceBox.prototype.getFields = function() {
  return this.fields;
};

scout.SequenceBox.prototype.clone = function(model, options) {
  var clone = scout.SequenceBox.parent.prototype.clone.call(this, model, options);
  this._deepCloneProperties(clone, 'fields', options);
  return clone;
};
