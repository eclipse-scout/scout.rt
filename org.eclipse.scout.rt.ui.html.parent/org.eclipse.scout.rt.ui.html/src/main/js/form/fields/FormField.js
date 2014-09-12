scout.FormField = function() {
  scout.FormField.parent.call(this);
  this.$label;
  this.$field;

  /**
   * The status label is used for error-status and mandatory info.
   */
  this.$status;
};
scout.inherits(scout.FormField, scout.ModelAdapter);

scout.FormField.prototype._render = function($parent) {
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
  if (!this.$container) {
    return;
  }
  this.$container.setVisible(visible);
};

scout.FormField.prototype._setLabel = function(label) {
  if (!label) {
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
  this.$label.setVisible(visible);
};

scout.FormField.prototype._setEnabled = function(enabled) {
  if (!this.$field) {
    return;
  }
  this.$field.setEnabled(enabled);
};

scout.FormField.prototype._updateStatusLabel = function() {
  if (!this.$status) {
    return;
  }

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

/**
 * Appends a LABEL element to this.$container and sets the this.$label property.
 */
scout.FormField.prototype.addLabel = function() {
  this.$label = $('<label>').
    appendTo(this.$container).
    attr('title', this.label);
};

/**
 * Appends a SPAN element for form-field status to this.$container and sets the this.$status property.
 */
scout.FormField.prototype.addStatus = function() {
  this.$status = $('<span>').
    addClass('status').
    appendTo(this.$container);
};


/**
 * Appends a DIV element as form-field container to $parent and sets the this.$container property.
 * Applies (logical) grid-data and FormFieldLayout to this.$container.
 *
 * @param $parent to which container is appended
 * @param typeName typeName of scout component used in ID attribute
 * @param layout when layout is undefined, scout.FormFieldLayout() is set
 */
scout.FormField.prototype.addContainer = function($parent, typeName, layout) {
  this.$container = $('<div>').
    appendTo($parent).
    addClass('form-field').
    attr('id', typeName + '-' + this.id);
  scout.Layout.setLogicalGridData(this.$container, this.gridData);
  if (!layout) {
    layout = new scout.FormFieldLayout();
  }
  scout.Layout.setLayout(this.$container, layout);
};




