scout.CheckBoxField = function(session, model) {
  this.base(session, model);
  this._$label;
  this._$checkBox;
};

scout.CheckBoxField.inheritsFrom(scout.ModelAdapter);

scout.CheckBoxField.prototype._render = function($parent) {
  // TODO AWE: definitiven HTML aufbau / styles mit C.RU besprechen (vergleiche mit bsicrm.rusche.ch)
  // TODO AWE: prüfen, warum die anderen checkbox-fields vom CheckBoxForm fehlen.
  this.$container = $parent.appendDiv(undefined, 'form-field', undefined);
  this._$label = this.$container.appendDiv(undefined, 'label', this.model.displayText);
  this._$checkBox = this.$container.appendDiv(undefined, 'field checkbox', ' ');
  this._setChecked(this.model.checked);

  // TODO AWE: (ask C.GU) this/that: was ist das definitive pattern?
  var that = this;
  this._$checkBox.click(function() {
    // TODO AWE: (ask C.GU) vergleiche mit DesktopViewButton.js -> ID von DOM oder model verwenden?
    // --> vereinheitlichen
    that.session.send('click', that.model.id);
  });

};

// TODO AWE: (ask C.GU) definieren wir private methoden auch auf dem prototype?
scout.CheckBoxField.prototype._setChecked = function(checked) {
  if (checked) {
    this._$checkBox.addClass('checkbox_checked');
  } else {
    this._$checkBox.removeClass('checkbox_checked');
  }
};

scout.CheckBoxField.prototype.onModelPropertyChange = function(event) {
  this._setChecked(event.checked);
};

