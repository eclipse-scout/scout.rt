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
scout.FormTableControl = function() {
  scout.FormTableControl.parent.call(this);
  this._addAdapterProperties('form');

  this._formDestroyedHandler = this._onFormDestroyed.bind(this);
};
scout.inherits(scout.FormTableControl, scout.TableControl);

scout.FormTableControl.prototype._init = function(model) {
  scout.FormTableControl.parent.prototype._init.call(this, model);
  this._setForm(this.form);
};

scout.FormTableControl.prototype._createLayout = function() {
  return new scout.FormTableControlLayout(this);
};

scout.FormTableControl.prototype._renderContent = function($parent) {
  this.form.renderInitialFocusEnabled = false;
  this.form.render($parent);

  // Tab box gets a special style if it is the first field in the root group box
  var rootGroupBox = this.form.rootGroupBox;
  if (rootGroupBox.fields[0] instanceof scout.TabBox) {
    rootGroupBox.fields[0].$container.addClass('in-table-control');
  }

  this.form.$container.height($parent.height());
  this.form.$container.width($parent.width());
  this.form.htmlComp.pixelBasedSizing = true;
  this.form.htmlComp.validateRoot = true;
  this.form.htmlComp.validateLayout();
};

scout.FormTableControl.prototype._removeContent = function() {
  if (this.form) {
    this.form.remove();
  }
};

scout.FormTableControl.prototype._removeForm = function() {
  this.removeContent();
};

scout.FormTableControl.prototype._renderForm = function(form) {
  this.renderContent();
};

/**
 * Returns true if the table control may be displayed (opened).
 */
scout.FormTableControl.prototype.isContentAvailable = function() {
  return !!this.form;
};

scout.FormTableControl.prototype._setForm = function(form) {
  if (this.form) {
    this.form.off('destroy', this._formDestroyedHandler);
  }
  if (form) {
    form.on('destroy', this._formDestroyedHandler);
    form.rootGroupBox.menuBar.bottom();
  }
  this._setProperty('form', form);
};

scout.FormTableControl.prototype.onControlContainerOpened = function() {
  this.form.renderInitialFocus();
};

scout.FormTableControl.prototype._onFormDestroyed = function(event) {
  // Called when the inner form is destroyed --> unlink it from this table control
  this._removeForm();
  this._setForm(null);
};
