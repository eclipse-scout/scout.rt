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
  // TODO AWE: (form) remove this code when FormField is "abstract". There should be no reason to instantiate a
  // FormField directly. Currently this is required as a placeholder for un-implemented form-fields.
  this.addContainer($parent, 'FormField', new scout.FormFieldLayout());
  this.addLabel();
  this.addStatus();
  this.$field = $.makeDiv('', 'field').
    html('[not implemented yet]').
    appendTo(this.$container);

};

scout.FormField.prototype._renderProperties = function() {
  this._renderEnabled(this.enabled);
  this._renderValue(this.value);
  this._renderMandatory(this.mandatory);
  this._renderVisible(this.visible);
  this._renderErrorStatus(this.errorStatus);
  this._renderLabel(this.label);
  this._renderLabelVisible(this.labelVisible);
};

scout.FormField.prototype._renderEnabled = function(enabled) {
  // NOP
};

scout.FormField.prototype._renderValue = function(value) {
  // NOP
};

scout.FormField.prototype._renderMandatory = function(mandatory) {
  this._updateStatusLabel();
};

scout.FormField.prototype._renderErrorStatus = function(errorStatus) {
  this._updateStatusLabel();
};

scout.FormField.prototype._renderVisible = function(visible) {
  if (!this.$container) {
    return;
  }
  this.$container.setVisible(visible);
};

scout.FormField.prototype._renderLabel = function(label) {
  if (!label) {
    label = '';
  }
  if (this.$label) {
    this.$label.html(label);
  }
};

scout.FormField.prototype._renderLabelVisible = function(visible) {
  if (!this.$label) {
    return;
  }
  this.$label.setVisible(visible);
};

scout.FormField.prototype._renderEnabled = function(enabled) {
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
  this._renderEnabled(false);
};

scout.FormField.prototype.goOnline = function() {
  scout.FormField.parent.prototype.goOnline.call(this);

  if (this.enabled) {
    this._renderEnabled(true);
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
 *
 * @return The HtmlComponent created for this.$container
 */
scout.FormField.prototype.addContainer = function($parent, typeName, layout) {
  this.$container = $('<div>').
    appendTo($parent).
    addClass('form-field').
    attr('id', typeName + '-' + this.id);

  var htmlComp = new scout.HtmlComponent(this.$container);
  htmlComp.layoutData = new scout.LogicalGridData(this);
  htmlComp.setLayout(layout || new scout.FormFieldLayout());
  return htmlComp;
};
