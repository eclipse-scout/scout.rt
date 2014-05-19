scout.FormField = function(model, session) {
  this.base(model, session);
};

scout.FormField.inheritsFrom(scout.ModelAdapter);

scout.FormField.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'form-field');
  this.$label = this.$container.appendDiv(undefined, 'label', this.model.label);
};
