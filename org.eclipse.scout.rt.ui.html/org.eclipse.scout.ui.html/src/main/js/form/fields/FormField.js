scout.FormField = function(session, model) {
  this.base(session, model);
};

scout.FormField.inheritsFrom(scout.ModelAdapter);

scout.FormField.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'form-field');
  this.$label = this.$container.appendDiv(undefined, 'label', this.model.label);
};
