scout.CheckBoxField = function(session, model) {
  this.base(session, model);
  this._$label;
  this._$checkBox;
};

scout.CheckBoxField.inheritsFrom(scout.ModelAdapter);

scout.CheckBoxField.prototype._render = function($parent) {
  // TODO AWE: HTML aufbau / style von bsicrm.rusche.ch übernehmen
  // TODO AWE: vielleicht braucht es eine jquery methode für fields ohne ID
  // TODO AWE: prüfen, warum die anderen checkbox-fields vom CheckBoxForm fehlen.
  this.$container = $parent.appendDiv();
  this._$label = this.$container.appendDiv(undefined, undefined, this.model.displayText);
  this._$checkBox = this.$container.appendDiv(undefined, undefined, '<input type="checkbox" />');
  this._$checkBox.prop('checked', this.model.checked);

  // TODO AWE: (ask C.GU) this/that was ist hier das definitive pattern?
  var that = this;
  this._$checkBox.click(function() {
    // TODO AWE: (ask C.GU) vergleiche mit DesktopViewButton.js -> ID von DOM oder model verwenden?
    // --> vereinheitlichen
    that.session.send('click', that.model.id);
  });

};

scout.CheckBoxField.prototype.onModelPropertyChange = function(event) {
  this._$checkBox.prop('checked', event.checked);
};

