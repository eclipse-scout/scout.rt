scout.FormField = function() {
  scout.FormField.parent.call(this);
  this.$label;

  /**
   * Note the difference between $field and $fieldContainer:
   * - $field points to the input-field (typically a browser-text field)
   * - $fieldContainer could point to the same input-field or when the field is a composite,
   *   to the parent DIV of that composite. For instance: the multi-line-smartfield is a
   *   composite with a input-field and a DIV showing the additional lines. In that case $field
   *   points to the input-field and $fieldContainer to the parent DIV of the input-field.
   *   This property should be used primarily for layout-functionality.
   */
  this.$field;
  this.$fieldContainer;

  /**
   * The status label is used for error-status and tooltip-icon.
   */
  this.$status;
};
scout.inherits(scout.FormField, scout.ModelAdapter);

scout.FormField.LABEL_POSITION_DEFAULT = 0;
scout.FormField.LABEL_POSITION_LEFT = 1;
scout.FormField.LABEL_POSITION_ON_FIELD = 2;
scout.FormField.LABEL_POSITION_RIGHT = 3;
scout.FormField.LABEL_POSITION_TOP = 4;

scout.FormField.prototype._render = function($parent) {
  // TODO AWE: (form) remove this code when FormField is "abstract". There should be no reason to instantiate a
  // FormField directly. Currently this is required as a placeholder for un-implemented form-fields.
  this.addContainer($parent, 'form-field');
  this.addLabel();
  this.addField($('<div>'));
    // TODO AWE: (UGM) temporär auskommentiert
    // text('not implemented yet').
    // addClass('not-implemented'));
  this.addMandatoryIndicator();
  this.addStatus();
};

scout.FormField.prototype._renderProperties = function() {
  this._renderEnabled(this.enabled);
  this._renderMandatory(this.mandatory);
  this._renderVisible(this.visible);
  this._renderTooltipText(this.tooltipText);
  this._renderErrorStatus(this.errorStatus);
  this._renderLabel(this.label);
  this._renderLabelVisible(this.labelVisible);
  this._renderStatusVisible(this.statusVisible);
};

scout.FormField.prototype._renderMandatory = function(mandatory) {
  this.$container.updateClass(mandatory, 'mandatory');
};

scout.FormField.prototype._renderErrorStatus = function(errorStatus) {
  errorStatus = this.errorStatusUi || this.errorStatus;
  this.$container.updateClass(errorStatus, 'has-error');

  if (this.$field) {
    this.$field.updateClass(errorStatus, 'has-error');
  }

  if (errorStatus) {
    this._showStatusMessage({autoRemove: false});
  } else {
    this._hideStatusMessage();
  }
};

scout.FormField.prototype._renderTooltipText = function(tooltipText) {
  this.$container.updateClass(tooltipText, 'has-tooltip');

  if (this.$field) {
    this.$field.updateClass(tooltipText, 'has-tooltip');
  }
};

scout.FormField.prototype._renderVisible = function(visible) {
  if (!this.$container) {
    return;
  }
  this.$container.setVisible(visible);

  if (this.rendered) {
    var htmlComp = scout.HtmlComponent.get(this.$container).getParent();
    if (htmlComp) { //may be null if $container is detached
      htmlComp.revalidate();
    }
  }
};

scout.FormField.prototype._renderLabel = function(label) {
  if (!label) {
    label = '';
  }
  if (this.labelPosition === scout.FormField.LABEL_POSITION_ON_FIELD) {
    this._renderPlaceholder();
    if (this.$label) {
      this.$label.html('');
    }
  }
  else if (this.$label) {
    this._removePlaceholder();
    this.$label.html(label);
  }
};

scout.FormField.prototype._renderPlaceholder = function() {
  if (this.$field) {
    this.$field.attr('placeholder', this.label);
  }
};

scout.FormField.prototype._removePlaceholder = function() {
  if (this.$field) {
    this.$field.removeAttr('placeholder');
  }
};

scout.FormField.prototype._renderLabelVisible = function(visible) {
  this._renderChildVisible(this.$label, visible);
};

scout.FormField.prototype._renderStatusVisible = function(visible) {
  this._renderChildVisible(this.$status, visible);
};

scout.FormField.prototype._renderChildVisible = function($child, visible) {
  if (!$child) {
    return;
  }
  $child.setVisible(visible);
  if (this.rendered) {
    scout.HtmlComponent.get(this.$container).revalidate();
  }
};

// Don't include in renderProperties, it is not necessary to execute it initially because the positioning is done by _renderLabel
scout.FormField.prototype._renderLabelPosition = function(position) {
  this._renderLabel(this.label);
};

scout.FormField.prototype._renderEnabled = function(enabled) {
  this.$container.setEnabled(enabled);
  if (this.$field) {
    this.$field.setEnabled(enabled);
  }
};

scout.FormField.prototype._renderGridData = function(gridData) {
  // NOP
};

scout.FormField.prototype._onStatusClick = function() {
  this._showStatusMessage();
};

scout.FormField.prototype._showStatusMessage = function(options) {
  var opts, text, form, $formContainer;
  if (this.tooltip && this.tooltip.rendered) {
    return;
  }

  if (this.errorStatusUi) {
    text = this.errorStatusUi.message;
  } else if (this.errorStatus) {
    text = this.errorStatus.message;
  } else {
    text = this.tooltipText;
  }

  form = this.getForm();
  if (form) {
    $formContainer = form.$container;
  }

  opts = {
    text: text,
    $origin: this.$status,
    $context: $formContainer
  };
  $.extend(opts, options);
  this.tooltip = new scout.Tooltip(opts);
  this.tooltip.render();
};

scout.FormField.prototype.getForm = function() {
  var parent = this.parent;
  while (parent && parent.objectType !== 'Form') {
    parent = parent.parent;
  }
  return parent;
};

scout.FormField.prototype._showStatusMessageOnError = function() {
  if (this.$container.hasClass('has-error')) {
    this._showStatusMessage({autoRemove: false});
  }
};

scout.FormField.prototype._hideStatusMessage = function() {
  if (this.tooltip) {
    this.tooltip.remove();
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
 * Appends the given field to the this.$container and sets the properties this.$field and
 * this.$fieldContainer referencing the field. When $fieldContainer should point to another
 * element you must do this explicitly after calling this method.
 *
 * @param $field
 * @param $fieldContainer (optional)
 */
scout.FormField.prototype.addField = function($field, $fieldContainer) {
  if ($fieldContainer) {
    this.$field = $field;
    this.$fieldContainer = $fieldContainer;
    $fieldContainer.appendTo(this.$container);
  } else {
    this.$field = $field;
    this.$fieldContainer = $field;
    $field.appendTo(this.$container);
  }
  this.$fieldContainer.addClass('field');
};

/**
 * Sets the properties this.$field, this.$fieldContainer to null.
 */
scout.FormField.prototype.removeField = function() {
  this.$field = null;
  this.$fieldContainer = null;
};

/**
 * Appends a SPAN element for form-field status to this.$container and sets the this.$status property.
 */
scout.FormField.prototype.addStatus = function() {
  this.$status = $('<span>').
    addClass('status').
    click(this._onStatusClick.bind(this)).
    appendTo(this.$container);
};

scout.FormField.prototype.addMandatoryIndicator = function() {
  this.$mandatory = $('<span>').
    addClass('mandatory-indicator').
    appendTo(this.$container);
};

/**
 * Adds a SPAN element with class 'icon' the the given optional $parent.
 * When $parent is not set, the element is added to this.$container.
 * @param $parent (optional)
 */
scout.FormField.prototype.addIcon = function($parent) {
  if (!$parent) {
    $parent = this.$container;
  }
  this.$icon = $('<span>').
    addClass('icon').
    click(function() {
      this.$field.focus();
    }.bind(this)).
    appendTo($parent);
};

/**
 * Appends a DIV element as form-field container to $parent and sets the this.$container property.
 * Applies (logical) grid-data and FormFieldLayout to this.$container.
 *
 * @param $parent to which container is appended
 * @param typeName typeName of scout component used as class name and ID attribute. ID is only used for debug purpose so don't rely on it.
 * @param layout when layout is undefined, scout.FormFieldLayout() is set
 *
 * @return The HtmlComponent created for this.$container
 */
scout.FormField.prototype.addContainer = function($parent, typeName, layout) {
  this.$container =
    $.makeDIV('form-field').
      appendTo($parent);

  if (typeName) {
    this.$container.
      addClass(typeName).
      attr('id', this._generateId(typeName));
  }

  var htmlComp = new scout.HtmlComponent(this.$container, this.session);
  htmlComp.layoutData = new scout.LogicalGridData(this);
  htmlComp.setLayout(layout || new scout.FormFieldLayout(this));
  return htmlComp;
};

/**
 * Creates an id based on a class name. E.g.: smart-field -> SmartField-13
 */
scout.FormField.prototype._generateId = function(cssClass) {
  var i,
    idParts = cssClass.split('-');

  for (i=0; i < idParts.length; i++) {
    idParts[i] = idParts[i].charAt(0).toUpperCase() + idParts[i].substring(1);
  }
  return idParts.join('') + '-' + this.id;
};
