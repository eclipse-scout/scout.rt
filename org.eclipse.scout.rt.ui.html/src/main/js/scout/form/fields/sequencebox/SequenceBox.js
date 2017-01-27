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
scout.SequenceBox = function() {
  scout.SequenceBox.parent.call(this);
  this._addAdapterProperties('fields');

  this.fields = [];
};
scout.inherits(scout.SequenceBox, scout.CompositeField);

scout.SequenceBox.prototype._render = function($parent) {
  var field, i;
  this.addContainer($parent, 'sequence-box');
  this.addLabel();
  this.addField($parent.makeDiv());
  this.addStatus();
  this._handleStatus();
  var htmlComp = scout.HtmlComponent.install(this.$field, this.session);
  htmlComp.setLayout(new scout.LogicalGridLayout(scout.HtmlEnvironment.smallColumnGap, 0));
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

scout.SequenceBox.prototype._onFieldPropertyChange = function(event) {
  var visibiltyChanged = (event.changedProperties.indexOf('visible') !== -1);
  if (scout.arrays.containsAny(event.changedProperties, ['errorStatus', 'tooltipText', 'visible', 'menus', 'menusVisible'])) {
    this._handleStatus(visibiltyChanged);
  }
};

/**
 * Moves the status relevant properties from the last visible field to the sequencebox. This makes sure that the fields inside the sequencebox have the same size.
 */
scout.SequenceBox.prototype._handleStatus = function(visibilityChanged) {
  //FIXME cgu: what if sequencebox itself has a tooltip or errorstatus? probably field has higher prio -> override status of seq box
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

// TODO [6.2] awe: (scout, sequence-box) remove _modifyLabel when CheckboxForm uses SequenceBox5
// The new sequence-box sets the label to invisible on the model.
scout.SequenceBox.prototype._modifyLabel = function(field) {
  if (field instanceof scout.CheckBoxField) {
    field.labelVisible = false;
  }
};

/**
 * @override CompositeField.js
 */
scout.SequenceBox.prototype.getFields = function() {
  return this.fields;
};
