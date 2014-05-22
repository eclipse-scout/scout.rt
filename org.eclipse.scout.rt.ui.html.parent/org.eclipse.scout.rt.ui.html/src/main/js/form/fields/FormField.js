scout.FormField = function(model, session) {
  scout.FormField.parent.call(this, model, session);
  this._$label;
  /**
   * The status label is used for error-status and mandatory info.
   */
  this._$statusLabel;
};
scout.inherits(scout.FormField, scout.ModelAdapter);

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

scout.FormField.prototype._applyModel = function() {
  this._setEnabled(this.model.enabled);
  this._setValue(this.model.value);
  this._setMandatory(this.model.mandatory);
  this._setErrorStatus(this.model.errorStatus);
};

scout.FormField.prototype._setEnabled = function(enabled) {
  // TODO AWE: (form) hier ist etwas doof, weil dieser code auch im applyModel aufgerufen wird (1)
  this.model.enabled = enabled;
};

scout.FormField.prototype._setValue = function(value) {
  // TODO AWE: (form) hier ist etwas doof, weil dieser code auch im applyModel aufgerufen wird (2)
  this.model.value = value;
};

scout.FormField.prototype._setMandatory = function(mandatory) {
  this.model.mandatory = mandatory;
  this._updateStatusLabel();
};


scout.FormField.prototype._setErrorStatus = function(errorStatus) {
  this.model.errorStatus = errorStatus;
  this._updateStatusLabel();
};

scout.FormField.prototype._updateStatusLabel = function() {
  // errorStatus has higher priority than mandatory
  var title, icon;
  if (this.model.errorStatus) {
    title = this.model.errorStatus.message;
    icon = '!';
  } else if (this.model.mandatory === true) {
    title = null;
    icon = '*';
  }

  if (icon) {
    this._$statusLabel.
      css('display', 'inline-block').
      html(icon);
    if (title) {
      this._$statusLabel.attr('title', title);
      this._$statusLabel.addClass('error-status');
    } else {
      this._$statusLabel.removeAttr('title');
      this._$statusLabel.removeClass('error-status');
    }
  } else {
    this._$statusLabel.css('display', 'none');
  }
};

// TODO AWE: (form) prinzipiell soll das model bei einem property change immer updated werden
// am besten passiert das im ModelAdapter. Untersuchen müssen wir, ob wir so nicht ein reihen-
// folge problem bekommen. Ggf. müssen wir zuerst alle properties die vom server kommen syncen
// und erst danach die changes abarbeiten.
scout.FormField.prototype.onModelPropertyChange = function(event) {
  if (event.hasOwnProperty('value')) {
    this._setValue(event.value);
  } else if (event.hasOwnProperty('enabled')) {
    this._setEnabled(event.enabled);
  } else if (event.hasOwnProperty('mandatory')) {
    this._setMandatory(event.mandatory);
  } else if (event.hasOwnProperty('errorStatus')) {
    this._setErrorStatus(event.errorStatus);
  }
};

