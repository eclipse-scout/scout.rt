scout.SequenceBox = function(model, session) {
  scout.SequenceBox.parent.call(this, model, session);
};

scout.inherits(scout.SequenceBox, scout.ModelAdapter);

scout.SequenceBox.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'sequence-box');
  var i, fieldModel, fieldWidget;
  for (i = 0; i < this.model.fields.length; i++) {
    fieldModel = this.model.fields[i];
    fieldWidget = this.session.widgetMap[fieldModel.id];
    if (!fieldWidget) {
      fieldWidget = this.session.objectFactory.create(fieldModel);
    }
    fieldWidget.attach(this.$container);
  }
};


