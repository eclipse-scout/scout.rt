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
  return new scout.LogicalGridLayout(this, this.layoutConfig);
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
    layoutConfig = new scout.SequenceBoxLayoutConfig();
  }
  this._setProperty('layoutConfig', scout.SequenceBoxLayoutConfig.ensure(layoutConfig));
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
};

/**
 * Moves the status relevant properties from the last visible field to the sequencebox. This makes sure that the fields inside the sequencebox have the same size.
 */
scout.SequenceBox.prototype._handleStatus = function(visibilityChanged) {
  // TODO [7.0] cgu: what if sequencebox itself has a tooltip or errorstatus? probably field has higher prio -> override status of seq box
  if (visibilityChanged && this._lastVisibleField) {
    // if there is a new last visible field, make sure the status is shown on the previously last one
    this._lastVisibleField.suppressStatus = false;
    this._lastVisibleField._renderErrorStatus();
    this._lastVisibleField._renderTooltipText();
    this._lastVisibleField._renderMenus();
  }
  this._lastVisibleField = this._getLastVisibleField();
  if (!this._lastVisibleField) {
    return;
  }

  // Update the sequencebox with the status relevant flags
  this.setErrorStatus(this._lastVisibleField.errorStatus);
  this.setTooltipText(this._lastVisibleField.tooltipText);
  this.setMenus(this._lastVisibleField.menus);
  this.setMenusVisible(this._lastVisibleField.menusVisible);

  // Make sure the last field won't display a status
  this._lastVisibleField.suppressStatus = true;
  if (visibilityChanged) {
    // If the last field got invisible, make sure the new last field does not display a status anymore (now done by the seq box)
    this._lastVisibleField._renderErrorStatus();
    this._lastVisibleField._renderTooltipText();
    this._lastVisibleField._renderMenus();
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

/**
 * override
 */
scout.SequenceBox.prototype._updateStatusVisible = function() {
  this._renderStatusVisible();
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
