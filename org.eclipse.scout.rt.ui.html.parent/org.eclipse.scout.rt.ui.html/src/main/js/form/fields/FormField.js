scout.FormField = function(model, session) {
  scout.FormField.parent.call(this, model, session);
  this._$label;
  this._$statusLabel;
};
scout.inherits(scout.FormField, scout.ModelAdapter);

/**
 * The _render method creates the UI through DOM manipulation. At this point we should not apply model
 * properties on the UI, since sub-classes may need to contribute to the DOM first.
 */
scout.FormField.prototype._render = function($parent) {
  // TODO AWE: definitiven HTML aufbau / styles mit C.RU besprechen (vergleiche mit bsicrm.rusche.ch)
  // das normale status-label von Scout ist ein composite mit Icon. Siehe JStatusLabelEx.
  this.$container = $parent.appendDiv(undefined, 'form-field');
  var gridData = this.model.gridData;
  this.$container.addClass('inbox').
    addClass('w1').
    addClass('h1').
    addClass('x' + (gridData.x + 1)).
    addClass('y' + (gridData.y + 1));
  this._$label = this.$container.appendDiv(undefined, 'label', this.model.label);
  // TODO AWE: (ask C.GU) vermutlich wäre es besser, das statusLabel nur bei Bedarf zu erzeugen und
  // dann wieder wegzuwerfen
  this._$statusLabel = this.$container.appendDiv(undefined, 'status-label', ' ');
};

/**
 * Applies model properties on the DOM UI created by the _render() method before.
 */
scout.FormField.prototype._applyModel = function() {
  this._setEnabled(this.model.enabled);
  this._setValue(this.model.value);
  this._setErrorStatus(this.model.errorStatus);
};

scout.FormField.prototype._setEnabled = function(enabled) {
  // NOP
};

scout.FormField.prototype._setValue = function(value) {
  // NOP
};

scout.FormField.prototype._setErrorStatus = function(errorStatus) {
  if (errorStatus) {
    // TODO AWE: (ask C.RU) schauen wie tooltips angezeigt werden sollen
    this._$statusLabel.
      css('display', 'block').
      attr('title', errorStatus.message);
  } else {
    this._$statusLabel.css('display', 'none');
  }
};

scout.FormField.prototype.onModelPropertyChange = function(event) {
  if (event.hasOwnProperty('value')) {
    this._setValue(event.value);
  } else if (event.hasOwnProperty('enabled')) {
    this._setEnabled(event.enabled);
  } else if (event.hasOwnProperty('errorStatus')) {
    this._setErrorStatus(event.errorStatus);
  }
};

