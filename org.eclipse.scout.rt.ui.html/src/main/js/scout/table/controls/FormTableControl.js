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
scout.FormTableControl = function() {
  scout.FormTableControl.parent.call(this);
  this._addWidgetProperties('form');

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
  if (rootGroupBox.controls[0] instanceof scout.TabBox) {
    rootGroupBox.controls[0].$container.addClass('in-table-control');
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
    this._adaptForm(form);
  }
  this._setProperty('form', form);
};

scout.FormTableControl.prototype._adaptForm = function(form) {
  form.rootGroupBox.setMenuBarPosition(scout.GroupBox.MenuBarPosition.BOTTOM);
  form.setDisplayHint(scout.Form.DisplayHint.VIEW);
  form.setModal(false);
  form.setAskIfNeedSave(false);
  if (this.session.userAgent.deviceType !== scout.Device.Type.MOBILE && form.rootGroupBox.fieldStyle === scout.FormField.FieldStyle.ALTERNATIVE) {
    // Use default style because alternative style does not look as good with a background color
    form.rootGroupBox.setFieldStyle(scout.FormField.FieldStyle.CLASSIC);
  }
};

scout.FormTableControl.prototype.onControlContainerOpened = function() {
  if (!this.form.rendered) {
    return;
  }
  this.form.renderInitialFocus();
};

scout.FormTableControl.prototype._onFormDestroyed = function(event) {
  // Called when the inner form is destroyed --> unlink it from this table control
  this._removeForm();
  this._setForm(null);
};
