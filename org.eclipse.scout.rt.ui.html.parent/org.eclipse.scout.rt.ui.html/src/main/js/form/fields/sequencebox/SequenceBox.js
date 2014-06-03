scout.SequenceBox = function(model, session) {
  scout.SequenceBox.parent.call(this, model, session);
  this._gridLayout;
};

scout.inherits(scout.SequenceBox, scout.FormField);

scout.SequenceBox.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'form-field sequence-box');
  this.$container.data('gridData', this.model.gridData);
  // the sequence box has as many columns as it has fields
  this.$container.data('columns', this.model.fields.length);
  this._gridLayout = new scout.GridLayout(this.$container);
  this.$container.data('gridLayout', this._gridLayout);

  var i, fieldModel, fieldWidget;
  for (i = 0; i < this.model.fields.length; i++) {
    fieldModel = this.model.fields[i];
    fieldWidget = this.session.widgetMap[fieldModel.id];
    if (!fieldWidget) {
      fieldWidget = this.session.objectFactory.create(fieldModel);
    }
    fieldWidget.attach(this.$container);
  }

  this._gridLayout.layout();
  this.$container.on('resize', this._gridLayout.updateLayout.bind(this._gridLayout));
};

scout.SequenceBox.prototype.dispose = function() {
  scout.SequenceBox.parent.prototype.dispose.call(this);
  this.$container.off('resize', this._gridLayout.updateLayout);
};
