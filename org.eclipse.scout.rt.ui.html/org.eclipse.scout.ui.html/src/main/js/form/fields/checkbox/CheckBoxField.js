scout.CheckBoxField = function(session, model) {
  this.base(session, model);
  this._$label;
  this._$checkBox;
};

scout.CheckBoxField.inheritsFrom(scout.ModelAdapter);

scout.CheckBoxField.prototype._render = function($parent) {
  // TODO AWE: mit C.GU reden wie der aufbau sein soll und welche class/IDs vergeben werden
  // TODO AWE: vielleicht braucht es eine jquery methode für fields ohne ID
  this.$container = $parent.appendDiv();
  this._$label = this.$container.appendDiv(undefined, undefined, this.model.displayText);
  this._$checkBox = this.$container.appendDiv(undefined, undefined, '<input type="checkbox" />');
  this._$checkBox.prop('checked', this.model.checked);
};

