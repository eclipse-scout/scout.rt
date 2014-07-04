scout.FormField = function() {
  scout.FormField.parent.call(this);
  this.$label;

  /**
   * The status label is used for error-status and mandatory info.
   */
  this.$status;
};
scout.inherits(scout.FormField, scout.ModelAdapter);

scout.FormField.prototype._render = function($parent) {
  // TODO AWE: definitiven HTML aufbau / styles mit C.RU besprechen (vergleiche mit bsicrm.rusche.ch)
  // das normale status-label von Scout ist ein composite mit Icon. Siehe JStatusLabelEx.

  /*
  this._$label = this.$container.appendDiv(undefined, 'label', this.model.label);
  // TODO AWE: (ask C.GU) vermutlich wäre es besser, das statusLabel nur bei Bedarf zu erzeugen und
  // dann wieder wegzuwerfen
  this._$statusLabel = this.$container.appendDiv(undefined, 'status-label', ' ');
  */
};

scout.FormField.prototype._callSetters = function() {
  this._setEnabled(this.enabled);
  this._setValue(this.value);
  this._setMandatory(this.mandatory);
  this._setVisible(this.visible);
  this._setErrorStatus(this.errorStatus);
  this._setLabel(this.label);
  this._setLabelVisible(this.labelVisible);
};


scout.FormField.prototype._setEnabled = function(enabled) {
  // NOP
};

scout.FormField.prototype._setValue = function(value) {
  // NOP
};

scout.FormField.prototype._setMandatory = function(mandatory) {
  this._updateStatusLabel();
};


scout.FormField.prototype._setErrorStatus = function(errorStatus) {
  this._updateStatusLabel();
};

scout.FormField.prototype._setVisible = function(visible) {
  // NOP
};

scout.FormField.prototype._setLabel = function(label) {
  if(!label) {
    label = '';
  }

  if (this.$label) {
    this.$label.html(label);
  }
};

scout.FormField.prototype._setLabelVisible = function(visible) {
  if (!this.$label) {
    return;
  }

  if (visible) {
    this.$label.show();
  }
  else {
    this.$label.hide();
  }
};

scout.FormField.prototype._setDisplayText = function(label) {
  // NOP
};

scout.FormField.prototype._updateStatusLabel = function() {
  if (this.$status) {
    // errorStatus has higher priority than mandatory
    var title, icon = ' ';
    if (this.errorStatus) {
      title = this.errorStatus.message;
      icon = '!';
    } else if (this.mandatory === true) {
      icon = '*';
    }

    this.$status.html(icon);
    if (title) {
      this.$status.attr('title', title);
      this.$status.addClass('error-status');
    } else {
      this.$status.removeAttr('title');
      this.$status.removeClass('error-status');
    }
  }
};

scout.FormField.prototype.goOffline = function() {
  scout.FormField.parent.prototype.goOffline.call(this);
  this._setEnabled(false);
};

scout.FormField.prototype.goOnline = function() {
  scout.FormField.parent.prototype.goOnline.call(this);

  if (this.enabled) {
    this._setEnabled(true);
  }
};
