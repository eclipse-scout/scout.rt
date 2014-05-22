// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.GroupBox = function(model, session) {
  scout.GroupBox.parent.call(this, model, session);
  this._$label;
};
scout.inherits(scout.GroupBox, scout.ModelAdapter);

scout.GroupBox.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'group-box');
  this.$container.addClass('w2');

  if (this.model.label) {
    // TODO AWE: das geht wohl auch hübscher mit :after
    var span = '<span class="group-box-title-border"></span>';
    this._$label = this.$container.appendDiv(undefined, 'group-box-title', this.model.label + span);
  }

  if (this.model.formFields) {
    var i, formFieldModel, formFieldWidget;
    for (i = 0; i < this.model.formFields.length; i++) {
      formFieldModel = this.model.formFields[i];
      formFieldWidget = this.session.widgetMap[formFieldModel.id];
      if (!formFieldWidget) {
        formFieldWidget = this.session.objectFactory.create(formFieldModel);
      }
      formFieldWidget.attach(this.$container);
    }
  }
};

scout.GroupBox.prototype._onModelBorderVisibleChanged = function(borderVisible) {

};

scout.GroupBox.prototype.onModelPropertyChange = function(event) {
  if (event.hasOwnProperty('borderVisible')) {
    this._onModelBorderVisibleChanged(event.borderVisible);
  }
};
