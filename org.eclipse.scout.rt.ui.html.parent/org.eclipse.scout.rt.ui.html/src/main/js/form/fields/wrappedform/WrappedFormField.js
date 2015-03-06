// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.WrappedFormField = function() {
  scout.CalendarField.parent.call(this);
  this._addAdapterProperties(['innerForm']);
};
scout.inherits(scout.WrappedFormField, scout.FormField);

scout.WrappedFormField.prototype._render = function($parent) {
  this.addContainer($parent, 'wrapped-form-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addStatus();

  this._renderInnerForm();
};

/**
 * Will also be called by model adapter on property change event
 */
scout.WrappedFormField.prototype._renderInnerForm = function() {
  if (this.innerForm) {
    this.innerForm.displayHint = 'wrappedForm'; // TODO BSH Check this
    this.innerForm.render(this.$container);
    this.addField(this.innerForm.$container);
    scout.HtmlComponent.get(this.innerForm.$container).invalidateTree();
  }
};

scout.WrappedFormField.prototype._removeInnerForm = function(oldInnerForm) {
  if (oldInnerForm) {
    oldInnerForm.remove();
  }
  this.removeField();
};
