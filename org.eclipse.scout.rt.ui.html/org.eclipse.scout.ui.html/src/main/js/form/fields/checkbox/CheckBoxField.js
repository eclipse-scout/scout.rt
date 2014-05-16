scout.CheckBoxField = function(session, model) {
  this.base(session, model);
  this._$label;
  this._$checkBox;
  this._$statusLabel;
};

scout.CheckBoxField.inheritsFrom(scout.ModelAdapter);

scout.CheckBoxField.prototype._render = function($parent) {
  // TODO AWE: definitiven HTML aufbau / styles mit C.RU besprechen (vergleiche mit bsicrm.rusche.ch)
  // das normale status-label von Scout ist ein composite mit Icon. Siehe JStatusLabelEx.
  this.$container = $parent.appendDiv(undefined, 'form-field', undefined);
  this._$label = this.$container.appendDiv(undefined, 'label', this.model.label);
  // TODO AWE: (ask C.GU) vermutlich wäre es besser, das statusLabel nur bei Bedarf zu erzeugen und
  // dann wieder wegzuwerfen
  this._$statusLabel = this.$container.appendDiv(undefined, 'status-label', ' ');
  this._$checkBox = this.$container.appendDiv(undefined, 'field checkbox', ' ');

  this._setEnabled(this.model.enabled);
  this._setValue(this.model.value);
  this._setErrorStatus(this.model.errorStatus);

  var that = this;
  this._$checkBox.on('click', function() {
    // TODO AWE: (ask C.GU) vergleiche mit DesktopViewButton.js -> ID von DOM oder model verwenden?
    // --> vereinheitlichen
    that.session.send('click', that.model.id);
  });

};

scout.CheckBoxField.prototype._setEnabled = function(enabled) {
  if (enabled) {
    this._$checkBox.removeAttr('disabled');
  } else {
    this._$checkBox.attr('disabled', 'disabled');
  }
};

scout.CheckBoxField.prototype._setValue = function(value) {
  if (value) {
    this._$checkBox.addClass('checkbox_checked');
  } else {
    this._$checkBox.removeClass('checkbox_checked');
  }
};

scout.CheckBoxField.prototype._setErrorStatus = function(errorStatus) {
  if (errorStatus) {
    // TODO AWE: (ask C.RU) schauen wie tooltips angezeigt werden sollen
    this._$statusLabel.
      css('display', 'block').
      attr('title', errorStatus.message);
  } else {
    this._$statusLabel.css('display', 'none');
  }
};

scout.CheckBoxField.prototype.onModelPropertyChange = function(event) {
  if (event.hasOwnProperty('value')) {
    this._setValue(event.value);
  } else if (event.hasOwnProperty('enabled')) {
    this._setEnabled(event.enabled);
  } else if (event.hasOwnProperty('errorStatus')) {
    this._setErrorStatus(event.errorStatus);
  }
};

