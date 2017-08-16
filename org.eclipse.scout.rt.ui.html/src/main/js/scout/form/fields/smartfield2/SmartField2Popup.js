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
scout.SmartField2Popup = function() {
  scout.SmartField2Popup.parent.call(this);
  this.animateRemoval = true;
};
scout.inherits(scout.SmartField2Popup, scout.Popup);

scout.SmartField2Popup.prototype._init = function(options) {
  options.withFocusContext = false;
  scout.SmartField2Popup.parent.prototype._init.call(this, options);

  this.smartField = this.parent;
  this.proposalChooser = this._createProposalChooser();
  this.proposalChooser.on('lookupRowSelected', this._triggerEvent.bind(this));
  this.proposalChooser.on('activeFilterSelected', this._triggerEvent.bind(this));
  this.smartField.on('remove', this._onRemoveSmartField.bind(this));

  this.setLookupResult(options.lookupResult);
  this.setStatus(options.status);
};

scout.SmartField2Popup.prototype._createProposalChooser = function() {
  var objectType = this.smartField.browseHierarchy ? 'TreeProposalChooser2' : 'TableProposalChooser2';
  return scout.create(objectType, {
    parent: this
  });
};

scout.SmartField2Popup.prototype._createLayout = function() {
  return new scout.SmartField2PopupLayout(this, this.proposalChooser);
};

scout.SmartField2Popup.prototype._render = function() {
  var cssClass = this.smartField.cssClassName() + '-popup';
  scout.SmartField2Popup.parent.prototype._render.call(this);
  this.$container
    .addClass(cssClass)
    .on('mousedown', this._onContainerMouseDown.bind(this));
  this.proposalChooser.render();
};

scout.SmartField2Popup.prototype.setLookupResult = function(result) {
  this.proposalChooser.setLookupResult(result);
};

/**
 * @returns the selected lookup row from the proposal chooser. If the row is disabled this function returns null.
 */
scout.SmartField2Popup.prototype.getSelectedLookupRow = function() {
  var lookupRow = this.proposalChooser.getSelectedLookupRow();
  if (lookupRow && lookupRow.enabled) {
    return lookupRow;
  } else {
    return null;
  }
};

scout.SmartField2Popup.prototype.setStatus = function(status) {
  this.proposalChooser.setStatus(status);
};

scout.SmartField2Popup.prototype.selectFirstLookupRow = function() {
  this.proposalChooser.selectFirstLookupRow();
};

scout.SmartField2Popup.prototype.selectLookupRow = function() {
  this.proposalChooser.triggerLookupRowSelected();
};

/**
 * Delegates the key event to the proposal chooser.
 */
scout.SmartField2Popup.prototype.delegateKeyEvent = function(event) {
  event.originalEvent.smartFieldEvent = true;
  this.proposalChooser.delegateKeyEvent(event);
};

scout.SmartField2Popup.prototype._triggerEvent = function(event) {
  this.trigger(event.type, event);
};

/**
 * This event handler is called before the mousedown handler on the _document_ is triggered
 * This allows us to prevent the default, which is important for the CellEditorPopup which
 * should stay open when the SmartField popup is closed. It also prevents the focus blur
 * event on the SmartField input-field.
 */
scout.SmartField2Popup.prototype._onContainerMouseDown = function(event) {
  // when user clicks on proposal popup with table or tree (prevent default,
  // so input-field does not lose the focus, popup will be closed by the
  // proposal chooser impl.
  return false;
};

// when smart-field is removed, also remove popup. Don't animate removal in that case
scout.SmartField2Popup.prototype._onRemoveSmartField = function(event) {
  this.animateRemoval = false;
  this.remove();
};

/**
 * @override because the icon is not in the $anchor container.
 */
scout.SmartField2Popup.prototype._isMouseDownOnAnchor = function(event) {
  return this.field.$field.isOrHas(event.target) || this.field.$icon.isOrHas(event.target);
};

scout.SmartField2Popup.prototype._onAnimationEnd = function() {
  this.proposalChooser.updateScrollbars();
};
