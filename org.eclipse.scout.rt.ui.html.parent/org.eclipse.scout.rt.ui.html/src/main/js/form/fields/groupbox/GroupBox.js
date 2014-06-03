// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.GroupBox = function(model, session) {
  scout.GroupBox.parent.call(this, model, session);
  this._$label;
  this._gridLayout;
};
scout.inherits(scout.GroupBox, scout.FormField);

scout.GroupBox.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'form-field group-box');
  this.$container.data('gridData', this.model.gridData);
  this.$container.data('columns', this.model.gridColumnCount);
  this._gridLayout = new scout.GridLayout(this.$container);
  this.$container.data('gridLayout', this._gridLayout);

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

  this._gridLayout.layout();
  this.$container.on('resize', this._gridLayout.updateLayout.bind(this._gridLayout));
};

scout.GroupBox.prototype._onModelBorderVisibleChanged = function(borderVisible) {

};

scout.GroupBox.prototype.onModelPropertyChange = function(event) {
  if (event.hasOwnProperty('borderVisible')) {
    this._onModelBorderVisibleChanged(event.borderVisible);
  }
};

scout.GroupBox.prototype.dispose = function() {
  scout.GroupBox.parent.prototype.dispose.call(this);
  this.$container.off('resize', this._gridLayout.updateLayout);
};
