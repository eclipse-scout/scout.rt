/**
 * Abstract class for all form-fields.
 * @abstract
 */
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
  this.keyStrokes = [];
  this._addAdapterProperties('keyStrokes');
  this.refFieldId;
};
scout.inherits(scout.FormField, scout.ModelAdapter);

scout.FormField.LABEL_POSITION_DEFAULT = 0;
scout.FormField.LABEL_POSITION_LEFT = 1;
scout.FormField.LABEL_POSITION_ON_FIELD = 2;
scout.FormField.LABEL_POSITION_RIGHT = 3;
scout.FormField.LABEL_POSITION_TOP = 4;

scout.FormField.prototype.init = function(model, session) {
  scout.FormField.parent.prototype.init.call(this, model, session);
  this.refFieldId = this.session.getUniqueFieldIdPrefix() + this.id;
};

scout.FormField.prototype._createKeyStrokeAdapter = function() {
  return new scout.FormFieldKeyStrokeAdapter(this);
};

/**
 * All sub-classes of scout.FormField must implement a _render method. The default implementation
 * will throw an Error when _render is called. The _render method should call the various add*
 * methods provided by the FormField class. A possible _render implementation could look like this.
 *
 * <pre>
 * this.addContainer($parent, 'form-field');
 * this.addLabel();
 * this.addField($('&lt;div&gt;').text('foo'));
 * this.addMandatoryIndicator();
 * this.addStatus();
 * </pre>
 */
scout.FormField.prototype._render = function($parent) {
  throw new Error('sub-classes of scout.FormField must implement a _render method');
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
  this._renderCssClass(this.cssClass);
};

scout.FormField.prototype._remove = function() {
  scout.FormField.parent.prototype._remove.call(this);
  this.removeField();
  this._hideStatusMessage();
};

scout.FormField.prototype._renderMandatory = function(mandatory) {
  this.$container.toggleClass('mandatory', mandatory);
};

scout.FormField.prototype._renderErrorStatus = function(errorStatus) {
  errorStatus = this.errorStatusUi || this.errorStatus;
  this.$container.toggleClass('has-error', !!errorStatus);

  if (this.$field) {
    this.$field.toggleClass('has-error', !!errorStatus);
  }

  if (errorStatus) {
    this._showStatusMessage({
      autoRemove: false
    });
  } else {
    this._hideStatusMessage();
  }
};

scout.FormField.prototype._renderTooltipText = function(tooltipText) {
  this.$container.toggleClass('has-tooltip', !!tooltipText);

  if (this.$field) {
    this.$field.toggleClass('has-tooltip', !!tooltipText);
  }
};

scout.FormField.prototype._renderVisible = function(visible) {
  if (!this.$container) {
    return;
  }
  this.$container.setVisible(visible);
  if (this.rendered) {
    var htmlComp = scout.HtmlComponent.get(this.$container).getParent();
    if (htmlComp) { // may be null if $container is detached
      htmlComp.invalidateTree();
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
  } else if (this.$label) {
    this._removePlaceholder();
    this.$label.html(this.label ? scout.strings.removeAmpersand(this.label) : '');

    // Invalidate layout if label width depends on its content
    if (this.labelUseUiWidth && this.rendered) {
      scout.HtmlComponent.get(this.$container).invalidateTree();
    }
  }
};

scout.FormField.prototype._renderPlaceholder = function() {
  if (this.$field) {
    this.$field.placeholder(this.label);
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
    scout.HtmlComponent.get(this.$container).invalidateTree();
  }
};

// Don't include in renderProperties, it is not necessary to execute it initially because the positioning is done by _renderLabel
scout.FormField.prototype._renderLabelPosition = function(position) {
  this._renderLabel(this.label);
};

scout.FormField.prototype._renderEnabled = function() {
  this.$container.setEnabled(this.enabled);
  if (this.$field) {
    this.$field.setEnabled(this.enabled);
  }
};

scout.FormField.prototype._renderCssClass = function(cssClass, oldCssClass) {
  this.$container.removeClass(oldCssClass);
  this.$container.addClass(cssClass);
};

scout.FormField.prototype._renderGridData = function(gridData) {
  // NOP
};

scout.FormField.prototype._onStatusClick = function() {
  // Toggle tooltip
  if (this.tooltip && this.tooltip.rendered) {
    this._hideStatusMessage();
  } else {
    var opts = {};
    if (this.$container.hasClass('has-error')) {
      opts.autoRemove = false;
    }
    this._showStatusMessage(opts);
  }
};

scout.FormField.prototype._showStatusMessage = function(options) {
  var text = this.tooltipText;
  if (this.errorStatusUi) {
    text = this.errorStatusUi.message;
  } else if (this.errorStatus) {
    text = this.errorStatus.message;
  }

  if (this.tooltip && this.tooltip.rendered) {
    // update existing tooltip
    this.tooltip.renderText(text);
  } else {
    // create new tooltip
    var form = this.getForm(),
      opts = {
        text: text,
        $origin: this.$status,
        $context: (form && form.$container)
      };
    $.extend(opts, options);
    this.tooltip = new scout.Tooltip(opts);
    this.tooltip.render();
  }
};

scout.FormField.prototype.getForm = function() {
  var parent = this.parent;
  while (parent && parent.objectType !== 'Form') {
    parent = parent.parent;
  }
  return parent;
};

scout.FormField.prototype._hideStatusMessage = function() {
  if (this.tooltip) {
    this.tooltip.remove();
    this.tooltip = undefined;
  }
};

scout.FormField.prototype._goOffline = function() {
  this._renderEnabled(false);
};

scout.FormField.prototype._goOnline = function() {
  if (this.enabled) {
    this._renderEnabled(true);
  }
};

/**
 * Appends a LABEL element to this.$container and sets the this.$label property.
 */
scout.FormField.prototype.addLabel = function() {
  this.$label = $('<label>')
    .appendTo(this.$container)
    .attr('title', this.label ? scout.strings.removeAmpersand(this.label) : '')
    .attr('for', this.refFieldId);
};

/**
 * Appends the given field to the this.$container and sets the property this.$field.
 * The $field is used as $fieldContainer as long as you don't explicitly call addFieldContainer before calling addField.
 */
scout.FormField.prototype.addField = function($field) {
  if (!this.$fieldContainer) {
    this.addFieldContainer($field);
  }
  this.$field = $field;
  this.$field.attr('id', this.refFieldId);
};

/**
 * Call this method before addField if you'd like to have a different field container than $field.
 */
scout.FormField.prototype.addFieldContainer = function($fieldContainer) {
  this.$fieldContainer = $fieldContainer;
  this.$fieldContainer.addClass('field');
  $fieldContainer.appendTo(this.$container);
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
  this.$status = $('<span>')
    .addClass('status')
    .click(this._onStatusClick.bind(this))
    .appendTo(this.$container);
};

scout.FormField.prototype.addMandatoryIndicator = function() {
  this.$mandatory = $('<span>')
    .addClass('mandatory-indicator')
    .appendTo(this.$container);
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
  this.$icon = $('<span>')
    .addClass('icon')
    .click(this._onIconClick.bind(this))
    .appendTo($parent);
};

scout.FormField.prototype._onIconClick = function(event) {
  this.$field.focus();
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
  this.$container = $.makeDiv('form-field')
    .appendTo($parent);

  if (typeName) {
    this.$container
      .addClass(typeName)
      .attr('id', this._generateId(typeName));
  }

  var htmlComp = new scout.HtmlComponent(this.$container, this.session);
  htmlComp.layoutData = new scout.LogicalGridData(this);
  htmlComp.setLayout(layout || new scout.FormFieldLayout(this));
  return htmlComp;
};
