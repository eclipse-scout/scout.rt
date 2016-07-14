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
scout.WrappedFormField = function() {
  scout.WrappedFormField.parent.call(this);
  this._addAdapterProperties(['innerForm']);

  this.initialFocusEnabled = false;
};
scout.inherits(scout.WrappedFormField, scout.FormField);

scout.WrappedFormField.prototype._render = function($parent) {
  this.addContainer($parent, 'wrapped-form-field');
  this.addLabel();
  this.addStatus();
};

scout.WrappedFormField.prototype._renderProperties = function() {
  scout.WrappedFormField.parent.prototype._renderProperties.call(this);
  this._renderInnerForm();
};

/**
 * Will also be called by model adapter on property change event
 */
scout.WrappedFormField.prototype._renderInnerForm = function() {
  if (!this.innerForm) {
    return;
  }

  this.innerForm.displayHint = scout.Form.DisplayHint.VIEW; // by definition, an inner form is a view.
  this.innerForm.modal = false; // by definition, an inner form is not modal.
  this.innerForm.renderInitialFocusEnabled = this.initialFocusEnabled; // do not render initial focus of form if disabled.

  this.innerForm.render(this.$container);

  this.addField(this.innerForm.$container);
  this.innerForm.invalidateLayoutTree();

  // required because active element is lost when 'addField' is called.
  this._renderInitialFocusEnabled();
};

scout.WrappedFormField.prototype._renderInitialFocusEnabled = function() {
  if (this.innerForm && this.initialFocusEnabled) {
    this.innerForm.renderInitialFocus();
  }
};

scout.WrappedFormField.prototype._removeInnerForm = function(oldInnerForm) {
  if (oldInnerForm) {
    oldInnerForm.remove();
  }
  this._removeField();
};
