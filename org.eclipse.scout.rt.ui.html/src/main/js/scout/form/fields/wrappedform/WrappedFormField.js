// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.WrappedFormField = function() {
  scout.WrappedFormField.parent.call(this);
  this._addAdapterProperties(['innerForm']);
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
  if (this.innerForm) {
    this.innerForm.displayHint = 'view'; // by definition, an inner form is a view.
    this.innerForm.modal = false; // by definition, an inner form is not modal.
    this.innerForm.renderInitialFocusEnabled = false; // do not render initial focus of form

    this.innerForm.render(this.$container);

    this.addField(this.innerForm.$container);
    this.innerForm.invalidateLayoutTree();
  }
};

scout.WrappedFormField.prototype._removeInnerForm = function(oldInnerForm) {
  if (oldInnerForm) {
    oldInnerForm.remove();
  }
  this.removeField();
};
