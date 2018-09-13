/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * Abstract class for all form-fields.
 * @abstract
 */
scout.FormField = function() {
  scout.FormField.parent.call(this);

  this.dropType = 0;
  this.dropMaximumSize = scout.dragAndDrop.DEFAULT_DROP_MAXIMUM_SIZE;
  this.empty = true;
  this.errorStatus = null;
  this.fieldStyle = scout.FormField.DEFAULT_FIELD_STYLE;
  this.gridData = null;
  this.gridDataHints = new scout.GridData();
  this.mode = scout.FormField.Mode.DEFAULT;
  this.keyStrokes = [];
  this.label = null;
  this.labelVisible = true;
  this.labelPosition = scout.FormField.LabelPosition.DEFAULT;
  this.labelWidthInPixel = 0;
  this.mandatory = false;
  this.statusMenuMappings = [];
  this.menus = [];
  this.menusVisible = true;
  this.preventInitialFocus = false;
  this.requiresSave = false;
  this.statusPosition = scout.FormField.StatusPosition.DEFAULT;
  this.statusVisible = true;
  this.touched = false;
  this.tooltipText = null;
  this.tooltip = null;

  this.$label = null;
  /**
   * Note the difference between $field and $fieldContainer:
   * - $field points to the input-field (typically a browser-text field)
   * - $fieldContainer could point to the same input-field or when the field is a composite,
   *   to the parent DIV of that composite. For instance: the multi-line-smartfield is a
   *   composite with a input-field and a DIV showing the additional lines. In that case $field
   *   points to the input-field and $fieldContainer to the parent DIV of the input-field.
   *   This property should be used primarily for layout-functionality.
   */
  this.$field = null;
  this.$fieldContainer = null;
  this.$icon = null;
  /**
   * The status is used for error-status, tooltip-icon and menus.
   */
  this.$status = null;

  /**
   * Some browsers don't support copying text from disabled input fields. If such a browser is detected
   * and this flag is true (default is false), an overlay DIV is rendered over disabled fields which
   * provides a custom copy context menu that opens the ClipboardForm.
   */
  this.disabledCopyOverlay = false;
  this.$disabledCopyOverlay = null;

  this._addWidgetProperties(['keyStrokes', 'menus', 'statusMenuMappings']);
  this._addCloneProperties(['dropType', 'dropMaximumSize', 'errorStatus', 'fieldStyle', 'gridDataHints', 'gridData', 'label', 'labelVisible', 'labelPosition', 'labelWidthInPixel', 'mandatory', 'mode', 'preventInitialFocus', 'requiresSave', 'touched', 'statusVisible', 'statusPosition', 'statusMenuMappings', 'tooltipText']);
};
scout.inherits(scout.FormField, scout.Widget);

scout.FormField.FieldStyle = {
  CLASSIC: 'classic',
  ALTERNATIVE: 'alternative'
};

/** Global variable to make it easier to adjust the default field style for all fields */
scout.FormField.DEFAULT_FIELD_STYLE = scout.FormField.FieldStyle.ALTERNATIVE;

scout.FormField.LabelPosition = {
  DEFAULT: 0,
  LEFT: 1,
  ON_FIELD: 2,
  RIGHT: 3,
  TOP: 4
};

scout.FormField.LabelWidth = {
  DEFAULT: 0,
  UI: -1
};

scout.FormField.StatusPosition = {
  DEFAULT: 'default',
  TOP: 'top'
};

// see org.eclipse.scout.rt.client.ui.form.fields.IFormField.FULL_WIDTH
scout.FormField.FULL_WIDTH = 0;

scout.FormField.Mode = {
  DEFAULT: 'default',
  CELLEDITOR: 'celleditor'
};

scout.FormField.SEVERITY_CSS_CLASSES = 'has-error has-warning has-info has-ok';

/**
 * @override
 */
scout.FormField.prototype._createKeyStrokeContext = function() {
  return new scout.KeyStrokeContext();
};

/**
 * @override
 */
scout.FormField.prototype._createLoadingSupport = function() {
  return new scout.LoadingSupport({
    widget: this
  });
};

scout.FormField.prototype._init = function(model) {
  scout.FormField.parent.prototype._init.call(this, model);
  this.resolveConsts([{
    property: 'labelPosition',
    constType: scout.FormField.LabelPosition}]);
  this.resolveTextKeys(['label', 'tooltipText']);
  this._setKeyStrokes(this.keyStrokes);
  this._setMenus(this.menus);
  this._setErrorStatus(this.errorStatus);
  this._setGridDataHints(this.gridDataHints);
  this._setGridData(this.gridData);
  this._updateEmpty();
};

scout.FormField.prototype._initProperty = function(propertyName, value) {
  if ('gridDataHints' === propertyName) {
    this._initGridDataHints(value);
  } else {
    scout.FormField.parent.prototype._initProperty.call(this, propertyName, value);
  }
};

/**
 * This function <strong>extends</strong> the default grid data hints of the form field.
 * The default values for grid data hints are set in the constructor of the FormField and its subclasses.
 * When the given gridDataHints is a plain object, we extend our default values. When gridDataHints is
 * already instanceof GridData we overwrite default values completely.
 * @param gridDataHints
 * @private
 */
scout.FormField.prototype._initGridDataHints = function(gridDataHints) {
  if (gridDataHints instanceof scout.GridData) {
    this.gridDataHints = gridDataHints;
  } else if (scout.objects.isPlainObject(gridDataHints)) {
    $.extend(this.gridDataHints, gridDataHints);
  } else {
    this.gridDataHints = gridDataHints;
  }
};

/**
 * All sub-classes of scout.FormField must implement a _render method. The default implementation
 * will throw an Error when _render is called. The _render method should call the various add*
 * methods provided by the FormField class. A possible _render implementation could look like this.
 *
 * <pre>
 * this.addContainer(this.$parent, 'form-field');
 * this.addLabel();
 * this.addField(this.$parent.makeDiv('foo', 'bar'));
 * this.addMandatoryIndicator();
 * this.addStatus();
 * </pre>
 */
scout.FormField.prototype._render = function() {
  throw new Error('sub-classes of scout.FormField must implement a _render method');
};

scout.FormField.prototype._renderProperties = function() {
  scout.FormField.parent.prototype._renderProperties.call(this);
  this._renderMandatory();
  this._renderTooltipText();
  this._renderErrorStatus();
  this._renderMenus();
  this._renderLabel();
  this._renderLabelVisible();
  this._renderStatusVisible();
  this._renderStatusPosition();
  this._renderFont();
  this._renderForegroundColor();
  this._renderBackgroundColor();
  this._renderLabelFont();
  this._renderLabelForegroundColor();
  this._renderLabelBackgroundColor();
  this._renderGridData();
  this._renderPreventInitialFocus();
  this._renderFieldStyle();
};

scout.FormField.prototype._remove = function() {
  scout.FormField.parent.prototype._remove.call(this);
  this._removeField();
  this._removeStatus();
  this._removeLabel();
  this._removeIcon();
  this.removeMandatoryIndicator();
  this._removeDisabledCopyOverlay();
  this._uninstallDragAndDropHandler();
};

scout.FormField.prototype.setFieldStyle = function(fieldStyle) {
  this.setProperty('fieldStyle', fieldStyle);
};

scout.FormField.prototype._renderFieldStyle = function() {
  this._renderFieldStyleInternal(this.$container);
  this._renderFieldStyleInternal(this.$fieldContainer);
  this._renderFieldStyleInternal(this.$field);
  if (this.rendered) {
    // See _renderLabelPosition why it is necessary to invalidate parent as well.
    var htmlCompParent = this.htmlComp.getParent();
    if (htmlCompParent) {
      htmlCompParent.invalidateLayoutTree();
    }
    this.invalidateLayoutTree();
  }
};

scout.FormField.prototype._renderFieldStyleInternal = function($element) {
  if (!$element) {
    return;
  }
  $element.toggleClass('alternative', this.fieldStyle === scout.FormField.FieldStyle.ALTERNATIVE);
};

scout.FormField.prototype.setMandatory = function(mandatory) {
  this.setProperty('mandatory', mandatory);
};

scout.FormField.prototype._renderMandatory = function() {
  this.$container.toggleClass('mandatory', this.mandatory);
};

/**
 * Override this function to return another error status property.
 * The default implementation returns the property 'errorStatus'.
 *
 * @return {scout.Status}
 */
scout.FormField.prototype._errorStatus = function() {
  return this.errorStatus;
};

scout.FormField.prototype.setErrorStatus = function(errorStatus) {
  this.setProperty('errorStatus', errorStatus);
};

scout.FormField.prototype._setErrorStatus = function(errorStatus) {
  errorStatus = scout.Status.ensure(errorStatus);
  this._setProperty('errorStatus', errorStatus);
};

scout.FormField.prototype.clearErrorStatus = function() {
  this.setErrorStatus(null);
};

scout.FormField.prototype._renderErrorStatus = function() {
  var status = this._errorStatus(),
    hasStatus = !!status,
    statusClass = hasStatus ? 'has-' + status.cssClass() : '';

  this._updateErrorStatusClasses(statusClass, hasStatus);

  this._updateFieldStatus();

};

scout.FormField.prototype._updateErrorStatusClasses = function(statusClass, hasStatus) {
  this.$container.removeClass(scout.FormField.SEVERITY_CSS_CLASSES);
  this.$container.addClass(statusClass, hasStatus);
  if (this.$field) {
    this.$field.removeClass(scout.FormField.SEVERITY_CSS_CLASSES);
    this.$field.addClass(statusClass, hasStatus);
  }
};

scout.FormField.prototype.setTooltipText = function(tooltipText) {
  this.setProperty('tooltipText', tooltipText);
};

scout.FormField.prototype._renderTooltipText = function() {
  var tooltipText = this.tooltipText;
  var hasTooltipText = scout.strings.hasText(tooltipText);
  this.$container.toggleClass('has-tooltip', hasTooltipText);
  if (this.$field) {
    this.$field.toggleClass('has-tooltip', hasTooltipText);
  }
  this._updateFieldStatus();
};

/**
 * @override
 */
scout.FormField.prototype._renderVisible = function() {
  scout.FormField.parent.prototype._renderVisible.call(this);
  if (this.rendered) {
    // Make sure error status is hidden / shown when visibility changes
    this._renderErrorStatus();
  }
};

scout.FormField.prototype.setLabel = function(label) {
  this.setProperty('label', label);
};

scout.FormField.prototype._renderLabel = function() {
  var label = this.label;
  if (this.labelPosition === scout.FormField.LabelPosition.ON_FIELD) {
    this._renderPlaceholder();
    if (this.$label) {
      this.$label.text('');
    }
  } else if (this.$label) {
    this._removePlaceholder();
    // Make sure an empty label is as height as the other labels, especially important for top labels
    this.$label.textOrNbsp(label, 'empty');
    this.$label.toggleClass('top', this.labelPosition === scout.FormField.LabelPosition.TOP);

    // Invalidate layout if label width depends on its content
    if (this.labelUseUiWidth || this.labelWidthInPixel === scout.FormField.LabelWidth.UI) {
      this.invalidateLayoutTree();
    }
  }
};

scout.FormField.prototype._renderPlaceholder = function($field) {
  $field = scout.nvl($field, this.$field);
  if ($field) {
    $field.placeholder(this.label);
  }
};

/**
 * @param $field (optional) argument is required by DateField.js, when not set this.$field is used
 */
scout.FormField.prototype._removePlaceholder = function($field) {
  $field = scout.nvl($field, this.$field);
  if ($field) {
    $field.placeholder('');
  }
};

scout.FormField.prototype.setLabelVisible = function(visible) {
  this.setProperty('labelVisible', visible);
};

scout.FormField.prototype._renderLabelVisible = function() {
  var visible = this.labelVisible;
  this._renderChildVisible(this.$label, visible);
  this.$container.toggleClass('label-hidden', !visible);
  if (this.rendered && this.labelPosition === scout.FormField.LabelPosition.TOP) {
    // See _renderLabelPosition why it is necessary to invalidate parent as well.
    var htmlCompParent = this.htmlComp.getParent();
    if (htmlCompParent) {
      htmlCompParent.invalidateLayoutTree();
    }
  }
};

scout.FormField.prototype.setLabelWidthInPixel = function(labelWidthInPixel) {
  this.setProperty('labelWidthInPixel', labelWidthInPixel);
};

scout.FormField.prototype._renderLabelWidthInPixel = function() {
  this.invalidateLayoutTree();
};

scout.FormField.prototype.setStatusVisible = function(visible) {
  this.setProperty('statusVisible', visible);
};

scout.FormField.prototype._renderStatusVisible = function() {
  this._updateFieldStatus();
};

scout.FormField.prototype.setStatusPosition = function(statusPosition) {
  this.setProperty('statusPosition', statusPosition);
};

scout.FormField.prototype._renderStatusPosition = function(statusPosition) {
  this._updateFieldStatus();
};

scout.FormField.prototype._tooltip = function() {
  if (this.fieldStatus) {
    return this.fieldStatus.tooltip;
  }
  return null;
};

scout.FormField.prototype._updateFieldStatus = function() {
  if (!this.fieldStatus) {
    return;
  }
  // compute status
  var menus,
    errorStatus = this._errorStatus(),
    status = null,
    statusVisible = this._computeStatusVisible(),
    autoRemove = false,
    showStatus = false;

  this.fieldStatus.setPosition(this.statusPosition);
  this.fieldStatus.setVisible(statusVisible);
  if (!statusVisible) {
    return;
  }

  if (errorStatus) {
    // If the field is used as a cell editor in a editable table, then no validation errors should be shown.
    // (parsing and validation will be handled by the cell/column itself)
    if (this.mode === scout.FormField.Mode.CELLEDITOR) {
      return;
    }
    status = errorStatus;
    autoRemove = !status.isError();
    menus = this._getMenusForStatus(errorStatus);
    showStatus = true;
  } else if (!scout.strings.empty(this.tooltipText)) {
    status = scout.create('Status', {
      message: this.tooltipText,
      severity: scout.Status.Severity.OK
    });
    // If there are menus, show them in the tooltip. But only if there is a tooltipText, don't do it if there is an error status.
    // Menus make most likely no sense if an error status is displayed
    menus = this._getCurrentMenus();

  } else {
    // If there are menus, show them in the tooltip. But only if there is a tooltipText, don't do it if there is an error status.
    // Menus make most likely no sense if an error status is displayed
    menus = this._getCurrentMenus();
  }

  this.fieldStatus.update(status, menus, autoRemove, showStatus);
};

/**
 * Computes whether the $status should be visible based on statusVisible, errorStatus and tooltip.
 * -> errorStatus and tooltip override statusVisible, so $status may be visible event though statusVisible is set to false
 */
scout.FormField.prototype._computeStatusVisible = function() {
  var status = this._errorStatus(),
    statusVisible = this.statusVisible,
    hasStatus = !!status,
    hasTooltip = !!this.tooltipText;

  return !this.suppressStatus && (statusVisible || hasStatus || hasTooltip || (this._hasMenus() && this.menusVisible));
};

scout.FormField.prototype._renderChildVisible = function($child, visible) {
  if (!$child) {
    return;
  }
  if ($child.isVisible() !== visible) {
    $child.setVisible(visible);
    this.invalidateLayoutTree();
    return true;
  }
};

scout.FormField.prototype.setLabelPosition = function(labelPosition) {
  this.setProperty('labelPosition', labelPosition);
};

// Don't include in renderProperties, it is not necessary to execute it initially because the positioning is done by _renderLabel
scout.FormField.prototype._renderLabelPosition = function(position) {
  this._renderLabel();
  if (this.rendered) {
    // Necessary to invalidate parent as well if parent uses the logical grid.
    // LogicalGridData uses another row height depending of the label position
    var htmlCompParent = this.htmlComp.getParent();
    if (htmlCompParent) {
      htmlCompParent.invalidateLayoutTree();
    }
    // Validate now to prevent flickering
    this.revalidateLayoutTree();
  }
};

/**
 * @override
 */
scout.FormField.prototype._renderEnabled = function() {
  scout.FormField.parent.prototype._renderEnabled.call(this);
  if (this.$field) {
    this.$field.setEnabled(this.enabledComputed);
  }
  this._updateDisabledCopyOverlay();
};

/**
 * @override Wigdet.js
 */
scout.FormField.prototype._renderDisabledStyle = function() {
  this._renderDisabledStyleInternal(this.$container);
  this._renderDisabledStyleInternal(this.$fieldContainer);
  this._renderDisabledStyleInternal(this.$field);
  this._renderDisabledStyleInternal(this.$mandatory);
};

scout.FormField.prototype.setFont = function(font) {
  this.setProperty('font', font);
};

scout.FormField.prototype._renderFont = function() {
  scout.styles.legacyFont(this, this.$field);
};

scout.FormField.prototype.setForegroundColor = function(foregroundColor) {
  this.setProperty('foregroundColor', foregroundColor);
};

scout.FormField.prototype._renderForegroundColor = function() {
  scout.styles.legacyForegroundColor(this, this.$field);
};

scout.FormField.prototype.setBackgroundColor = function(backgroundColor) {
  this.setProperty('backgroundColor', backgroundColor);
};

scout.FormField.prototype._renderBackgroundColor = function() {
  scout.styles.legacyBackgroundColor(this, this.$field);
};

scout.FormField.prototype.setLabelFont = function(labelFont) {
  this.setProperty('labelFont', labelFont);
};

scout.FormField.prototype._renderLabelFont = function() {
  scout.styles.legacyFont(this, this.$label, 'label');
};

scout.FormField.prototype.setLabelForegroundColor = function(labelForegroundColor) {
  this.setProperty('labelForegroundColor', labelForegroundColor);
};

scout.FormField.prototype._renderLabelForegroundColor = function() {
  scout.styles.legacyForegroundColor(this, this.$label, 'label');
};

scout.FormField.prototype.setLabelBackgroundColor = function(labelBackgroundColor) {
  this.setProperty('labelBackgroundColor', labelBackgroundColor);
};

scout.FormField.prototype._renderLabelBackgroundColor = function() {
  scout.styles.legacyBackgroundColor(this, this.$label, 'label');
};

scout.FormField.prototype.setGridDataHints = function(gridData) {
  this.setProperty('gridDataHints', gridData);
};

scout.FormField.prototype._setGridDataHints = function(gridData) {
  if (!gridData) {
    gridData = new scout.GridData();
  }
  this._setProperty('gridDataHints', scout.GridData.ensure(gridData));
};

scout.FormField.prototype._renderGridDataHints = function() {
  this.parent.invalidateLogicalGrid();
};

scout.FormField.prototype._setGridData = function(gridData) {
  if (!gridData) {
    gridData = new scout.GridData();
  }
  this._setProperty('gridData', scout.GridData.ensure(gridData));
};

scout.FormField.prototype._renderGridData = function() {
  if (this.rendered) {
    var htmlCompParent = this.htmlComp.getParent();
    if (htmlCompParent) { // may be null if $container is detached
      htmlCompParent.invalidateLayoutTree();
    }
  }
};

scout.FormField.prototype.setMenus = function(menus) {
  this.setProperty('menus', menus);
};

scout.FormField.prototype._setMenus = function(menus) {
  this.updateKeyStrokes(menus, this.menus);
  this._setProperty('menus', menus);
};

scout.FormField.prototype._getCurrentMenus = function() {
  return this.menus.filter(function(menu) {
    return menu.visible;
  });
};

scout.FormField.prototype._getMenusForStatus = function(status) {
  return this.statusMenuMappings.filter(function(mapping) {
    if (!mapping.menu || !mapping.menu.visible) {
      return;
    }
    // Show the menus which are mapped to the status code and severity (if set)
    return (mapping.codes.length === 0 || mapping.codes.indexOf(status.code) > -1) &&
      (mapping.severities.length === 0 || mapping.severities.indexOf(status.severity) > -1);
  }).map(function(mapping) {
    return mapping.menu;
  });
};

scout.FormField.prototype._hasMenus = function() {
  return !!(this.menus && this._getCurrentMenus().length > 0);
};

scout.FormField.prototype._updateMenus = function() {
  this.$container.toggleClass('has-menus', this._hasMenus() && this.menusVisible);
  this._updateFieldStatus();
};

scout.FormField.prototype._renderMenus = function() {
  this._updateMenus();
};

scout.FormField.prototype._renderStatusMenuMappings = function() {
  if (this.tooltip) {
    // If tooltip is visible call showStatusMessage to update the menus
    this._showStatusMessage();
  }
};

scout.FormField.prototype.setMenusVisible = function(menusVisible) {
  this.setProperty('menusVisible', menusVisible);
};

/**
 * override by TabItem
 **/
scout.FormField.prototype._setMenusVisible = function(menusVisible) {
  this._setProperty('menusVisible', menusVisible);
};

scout.FormField.prototype._renderMenusVisible = function() {
  this._updateMenus();

};

scout.FormField.prototype._setKeyStrokes = function(keyStrokes) {
  this.updateKeyStrokes(keyStrokes, this.keyStrokes);
  this._setProperty('keyStrokes', keyStrokes);
};

scout.FormField.prototype._onStatusMouseDown = function(event) {
  var hasStatus = !!this._errorStatus(),
    hasTooltip = !!this.tooltipText,
    hasMenus = this.menusVisible && this._hasMenus();

  // Either show the tooltip or a context menu
  // If the field has both, a tooltip and menus, the tooltip will be shown and the menus rendered into the tooltip
  if (hasStatus || hasTooltip) {
    // Toggle tooltip
    if (this.tooltip) {
      this._hideStatusMessage();
    } else {
      this._showStatusMessage();
    }
  } else if (hasMenus) {
    var func = function func(event) {
      if (!this.rendered || !this.attached) { // check needed because function is called asynchronously
        return;
      }
      // Toggle menu
      if (this.contextPopup && this.contextPopup.rendered) {
        this._hideContextMenu();
      } else {
        this._showContextMenu();
      }
    }.bind(this);

    this.session.onRequestsDone(func, event);
  }
};

scout.FormField.prototype._showStatusMessage = function() {
  // Don't show a tooltip if there is no visible $status (tooltip points to the status)
  if (!this.$status || !this.$status.isVisible()) {
    return;
  }

  var status = this._errorStatus(),
    text = this.tooltipText,
    severity = scout.Status.OK,
    autoRemove = true,
    menus = [];

  if (status) {
    text = status.message;
    severity = status.severity;
    autoRemove = !status.isError();
    if (this.tooltip && this.tooltip.autoRemove !== autoRemove) {
      // AutoRemove may not be changed dynamically -> Remove and reopen tooltip
      this.tooltip.destroy();
    }

    // If the field is used as a cell editor in a editable table, then no validation errors should be shown.
    // (parsing and validation will be handled by the cell/column itself)
    if (this.mode === scout.FormField.Mode.CELLEDITOR) {
      return;
    }
  }

  if (status) {
    // There may be menus which should be displayed in the tooltip
    menus = this._getMenusForStatus(status);
  } else if (this.menusVisible && this._hasMenus()) {
    // If there are menus, show them in the tooltip. But only if there is a tooltipText, don't do it if there is an error status.
    // Menus make most likely no sense if an error status is displayed
    menus = this._getCurrentMenus();
  }

  if (scout.strings.empty(text) && menus.length === 0) {
    // Refuse to show empty tooltip
    return;
  }

  // If a context menu is open, close it first
  this._hideContextMenu();

  if (this.tooltip) {
    // update existing tooltip
    this.tooltip.setText(text);
    this.tooltip.setSeverity(severity);
    this.tooltip.setMenus(menus);
  } else {
    // create new tooltip
    this.tooltip = this._createTooltip({
      parent: this,
      text: text,
      severity: severity,
      autoRemove: autoRemove,
      $anchor: this.$status,
      menus: menus
    });
    this.tooltip.one('destroy', function() {
      this.tooltip = null;
    }.bind(this));
    this.tooltip.render(this._$tooltipParent());
  }
};

scout.FormField.prototype._createTooltip = function(model) {
  return scout.create('Tooltip', model);
};

/**
 * May be overridden to explicitly provide a tooltip $parent
 */
scout.FormField.prototype._$tooltipParent = function() {
  // Will be determined by the tooltip itself
  return undefined;
};

scout.FormField.prototype._hideStatusMessage = function() {
  if (this.tooltip) {
    this.tooltip.destroy();
  }
};

scout.FormField.prototype._showContextMenu = function() {
  var menus = this._getCurrentMenus();
  if (menus.length === 0) {
    // at least one menu item must be visible
    return;
  }

  // Make sure tooltip is closed first
  this._hideStatusMessage();

  this.contextPopup = scout.create('ContextMenuPopup', {
    parent: this,
    $anchor: this.$status,
    menuItems: menus,
    cloneMenuItems: false,
    closeOnAnchorMouseDown: false
  });
  this.contextPopup.open();
};

scout.FormField.prototype._hideContextMenu = function() {
  if (this.contextPopup) {
    this.contextPopup.close();
    this.contextPopup = null;
  }
};

scout.FormField.prototype._renderPreventInitialFocus = function() {
  this.$container.toggleClass('prevent-initial-focus', !!this.preventInitialFocus);
};

/**
 * Sets the focus on this field. If the field is not rendered, the focus will be set as soon as it is rendered.
 *
 * @override
 */
scout.FormField.prototype.focus = function() {
  if (!this.rendered) {
    this.session.layoutValidator.schedulePostValidateFunction(this.focus.bind(this));
    return false;
  }

  var focusableElement = this.getFocusableElement();
  if (focusableElement) {
    return this.session.focusManager.requestFocus(focusableElement);
  }
  return false;
};

/**
 * This method returns the HtmlElement to be used as initial focus element or when {@link #focus()} is called.
 * It can be overridden, in case the FormField needs to return something other than this.$field[0].
 *
 * @override
 */
scout.FormField.prototype.getFocusableElement = function() {
  if (this.rendered && this.$field) {
    return this.$field[0];
  }
  return null;
};

scout.FormField.prototype._onFieldFocus = function(event) {
  this.setFocused(true);
};

scout.FormField.prototype._onFieldBlur = function() {
  this.setFocused(false);
};

/**
 * When calling this function, the same should happen as when clicking into the field. It is used when the label is clicked.<br>
 * The most basic action is focusing the field but this may differ from field to field.
 */
scout.FormField.prototype.activate = function() {
  if (!this.enabledComputed || !this.rendered) {
    return;
  }
  // Explicitly don't use this.focus() because this.focus uses the focus manager which may be disabled (e.g. on mobile devices)
  var focusableElement = this.getFocusableElement();
  if (focusableElement) {
    $.ensure(focusableElement).focus();
  }
};

/**
 * @override
 */
scout.FormField.prototype.get$Scrollable = function() {
  return this.$field;
};

scout.FormField.prototype.getParentGroupBox = function() {
  var parent = this.parent;
  while (parent && !(parent instanceof scout.GroupBox)) {
    parent = parent.parent;
  }
  return parent;
};

scout.FormField.prototype.getParentField = function() {
  return this.parent;
};

/**
 * Appends a LABEL element to this.$container and sets the this.$label property.
 */
scout.FormField.prototype.addLabel = function() {
  this.$label = this.$container.appendElement('<label>');
  scout.tooltips.installForEllipsis(this.$label, {
    parent: this
  });

  // Setting the focus programmatically does not work in a mousedown listener on mobile devices,
  // that is why a click listener is used instead
  this.$label.on('click', this._onLabelClick.bind(this));
};

scout.FormField.prototype._onLabelClick = function(event) {
  if (!scout.strings.hasText(this.label)) {
    // Clicking on "invisible" labels should not have any effect since it is confusing
    return;
  }
  this.activate();
};

scout.FormField.prototype._removeLabel = function() {
  if (!this.$label) {
    return;
  }
  scout.tooltips.uninstall(this.$label);
  this.$label.remove();
  this.$label = null;
};

/**
 * Links the given element with the label by setting aria-labelledby.<br>
 * This allows screen readers to build a catalog of the elements on the screen and their relationships, for example, to read the label when the input is focused.
 */
scout.FormField.prototype._linkWithLabel = function($element) {
  if (!this.$label || !$element) {
    return;
  }

  scout.fields.linkElementWithLabel($element, this.$label);
};

scout.FormField.prototype._removeIcon = function() {
  if (!this.$icon) {
    return;
  }
  this.$icon.remove();
  this.$icon = null;
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
  this._linkWithLabel($field);
  this.$field.on('blur', this._onFieldBlur.bind(this))
    .on('focus', this._onFieldFocus.bind(this));
};

/**
 * Call this method before addField if you'd like to have a different field container than $field.
 */
scout.FormField.prototype.addFieldContainer = function($fieldContainer) {
  this.$fieldContainer = $fieldContainer
    .addClass('field')
    .appendTo(this.$container);
};

/**
 * Removes this.$field and this.$fieldContainer and sets the properties to null.
 */
scout.FormField.prototype._removeField = function() {
  if (this.$field) {
    this.$field.remove();
    this.$field = null;
  }
  if (this.$fieldContainer) {
    this.$fieldContainer.remove();
    this.$fieldContainer = null;
  }
};

/**
 * Appends a SPAN element for form-field status to this.$container and sets the this.$status property.
 */
scout.FormField.prototype.addStatus = function() {
  if (this.fieldStatus) {
    return;
  }
  this.fieldStatus = scout.create('FieldStatus', {
    parent: this
  });
  this.fieldStatus.render();
  this.$status = this.fieldStatus.$container;
  this._updateFieldStatus();
};

scout.FormField.prototype._removeStatus = function() {
  if (!this.fieldStatus) {
    return;
  }
  this.fieldStatus.remove();
  this.$status = null;
  this.fieldStatus = null;
};

/**
 * Appends a SPAN element to this.$container and sets the this.$pseudoStatus property.
 * The purpose of a pseudo status is to consume the space an ordinary status would.
 * This makes it possible to make components without a status as width as components with a status.
 */
scout.FormField.prototype.addPseudoStatus = function() {
  this.$pseudoStatus = this.$container.appendSpan('status');
};

scout.FormField.prototype.addMandatoryIndicator = function() {
  this.$mandatory = this.$container.appendSpan('mandatory-indicator');
};

scout.FormField.prototype.removeMandatoryIndicator = function() {
  if (!this.$mandatory) {
    return;
  }
  this.$mandatory.remove();
  this.$mandatory = null;
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
  this.$icon = scout.fields.appendIcon($parent)
    .on('mousedown', this._onIconMouseDown.bind(this));
};

scout.FormField.prototype._onIconMouseDown = function(event) {
  if (!this.enabledComputed) {
    return;
  }
  this.$field.focus();
};

/**
 * Appends a DIV element as form-field container to $parent and sets the this.$container property.
 * Applies FormFieldLayout to this.$container (if container does not define another layout).
 * Sets this.htmlComp to the HtmlComponent created for this.$container.
 *
 * @param $parent to which container is appended
 * @param cssClass cssClass to add to the new container DIV
 * @param layout when layout is undefined, scout.FormFieldLayout() is set
 *
 */
scout.FormField.prototype.addContainer = function($parent, cssClass, layout) {
  this.$container = $parent.appendDiv('form-field');
  if (cssClass) {
    this.$container.addClass(cssClass);
  }
  var htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  htmlComp.setLayout(layout || new scout.FormFieldLayout(this));
  this.htmlComp = htmlComp;
};

/**
 * Updates the "inner alignment" of a field. Usually, the GridData hints only have influence on the
 * LogicalGridLayout. However, the properties "horizontalAlignment" and "verticalAlignment" are
 * sometimes used differently. Instead of controlling the field alignment in case fillHorizontal/
 * fillVertical is false, the developer expects the _contents_ of the field to be aligned correspondingly
 * inside the field. Technically, this is not correct, but is supported for legacy and convenience
 * reasons for some of the Scout fields. Those who support the behavior may override _renderGridData()
 * and call this method. Some CSS classes are then added to the field.
 *
 * opts:
 *   useHorizontalAlignment:
 *     When this option is true, "halign-" classes are added according to gridData.horizontalAlignment.
 *   useVerticalAlignment:
 *     When this option is true, "valign-" classes are added according to gridData.verticalAlignment.
 *   $fieldContainer:
 *     Specifies the div where the classes should be added. If omitted, this.$fieldContainer is used.
 */
scout.FormField.prototype.updateInnerAlignment = function(opts) {
  opts = opts || {};
  var $fieldContainer = opts.$fieldContainer || this.$fieldContainer;

  this._updateElementInnerAlignment(opts, $fieldContainer);
  if ($fieldContainer !== this.$container) {
    // also set the styles to the container
    this._updateElementInnerAlignment(opts, this.$container);
  }
};

scout.FormField.prototype._updateElementInnerAlignment = function(opts, $field) {
  opts = opts || {};
  var useHorizontalAlignment = scout.nvl(opts.useHorizontalAlignment, true);
  var useVerticalAlignment = scout.nvl(opts.useVerticalAlignment, true);

  if (!$field) {
    return;
  }

  $field.removeClass('has-inner-alignment halign-left halign-center halign-right valign-top valign-middle valign-bottom');
  if (useHorizontalAlignment || useVerticalAlignment) {
    // Set horizontal and vertical alignment (from gridData)
    $field.addClass('has-inner-alignment');
    var gridData = this.gridData;
    if (this.parent.logicalGrid) {
      // If the logical grid is calculated by JS, use the hints instead of the calculated grid data
      gridData = this.gridDataHints;
    }
    if (useHorizontalAlignment) {
      var hAlign = gridData.horizontalAlignment;
      $field.addClass(hAlign < 0 ? 'halign-left' : (hAlign > 0 ? 'halign-right' : 'halign-center'));
    }
    if (useVerticalAlignment) {
      var vAlign = gridData.verticalAlignment;
      $field.addClass(vAlign < 0 ? 'valign-top' : (vAlign > 0 ? 'valign-bottom' : 'valign-middle'));
    }
  }
};

scout.FormField.prototype.prepareForCellEdit = function(opts) {
  opts = opts || {};

  // remove mandatory and status indicators (popup should 'fill' the whole cell)
  if (this.$mandatory) {
    this.removeMandatoryIndicator();
  }
  if (this.$status) {
    this.$status.remove();
    this.$status = null;
  }
  if (this.$container) {
    this.$container.addClass('cell-editor-form-field');
  }
  if (this.$field) {
    this.$field.addClass('cell-editor-field');
    if (opts.firstCell) {
      this.$field.addClass('first');
    }
  }
};

scout.FormField.prototype._renderDropType = function() {
  if (this.dropType) {
    this._installDragAndDropHandler();
  } else {
    this._uninstallDragAndDropHandler();
  }
};

scout.FormField.prototype._createDragAndDropHandler = function() {
  return scout.dragAndDrop.handler(this, {
    supportedScoutTypes: scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER,
    dropType: function() {
      return this.dropType;
    }.bind(this),
    dropMaximumSize: function() {
      return this.dropMaximumSize;
    }.bind(this)
  });
};

scout.FormField.prototype._installDragAndDropHandler = function(event) {
  if (this.dragAndDropHandler) {
    return;
  }
  this.dragAndDropHandler = this._createDragAndDropHandler();
  this.dragAndDropHandler.install(this.$field);
};

scout.FormField.prototype._uninstallDragAndDropHandler = function(event) {
  if (!this.dragAndDropHandler) {
    return;
  }
  this.dragAndDropHandler.uninstall();
  this.dragAndDropHandler = null;
};

scout.FormField.prototype._updateDisabledCopyOverlay = function() {
  if (this.disabledCopyOverlay && !scout.device.supportsCopyFromDisabledInputFields()) {
    if (this.enabledComputed) {
      this._removeDisabledCopyOverlay();
    } else {
      this._renderDisabledCopyOverlay();
      this.revalidateLayout(); // because bounds of overlay is set in FormFieldLayout
    }
  }
};

scout.FormField.prototype._renderDisabledCopyOverlay = function() {
  if (!this.$disabledCopyOverlay) {
    this.$disabledCopyOverlay = this.$container
      .appendDiv('disabled-overlay')
      .on('contextmenu', this._createCopyContextMenu.bind(this));
  }
};

scout.FormField.prototype._removeDisabledCopyOverlay = function() {
  if (this.$disabledCopyOverlay) {
    this.$disabledCopyOverlay.remove();
    this.$disabledCopyOverlay = null;
  }
};

scout.FormField.prototype._createCopyContextMenu = function(event) {
  if (!this.visible || scout.strings.empty(this.displayText)) {
    return;
  }

  var field = this;
  var menu = scout.create('Menu', {
    parent: this,
    text: this.session.text('ui.Copy')
  });
  menu.on('action', function(event) {
    if (field instanceof scout.ValueField) {
      // TODO [7.0] cgu offline?
      field.trigger('clipboardExport');
    }
  });

  var popup = scout.create('ContextMenuPopup', {
    parent: this,
    menuItems: [menu],
    cloneMenuItems: false,
    location: {
      x: event.pageX,
      y: event.pageY
    },
    $anchor: this._$disabledOverlay
  });
  popup.open();
};

/**
 * Visits this field and all child formfields in pre-order (top-down).
 */
scout.FormField.prototype.visitFields = function(visitor) {
  visitor(this);
};

/**
 * Visit all parent form fields. The visit stops if the parent is no form field anymore (e.g. a form, desktop or session).
 */
scout.FormField.prototype.visitParents = function(visitor) {
  var curParent = this.parent;
  while (curParent instanceof scout.FormField) {
    visitor(curParent);
    curParent = curParent.parent;
  }
};

scout.FormField.prototype.markAsSaved = function() {
  this.setProperty('touched', false);
  this.updateRequiresSave();
};

scout.FormField.prototype.touch = function() {
  this.setProperty('touched', true);
  this.updateRequiresSave();
};

/**
 * Updates the requiresSave property by checking if the field is touched or if computeRequiresSave() returns true.
 */
scout.FormField.prototype.updateRequiresSave = function() {
  if (!this.initialized) {
    return;
  }
  this.requiresSave = this.touched || this.computeRequiresSave();
};

/**
 * Override this function in order to return whether or not this field requires to be saved.
 * The default impl. returns false.
 *
 * @returns {boolean}
 */
scout.FormField.prototype.computeRequiresSave = function() {
  return false;
};

/**
 * @returns {object} which contains 3 properties: valid, validByErrorStatus and validByMandatory
 */
scout.FormField.prototype.getValidationResult = function() {
  var validByErrorStatus = !this._errorStatus();
  var validByMandatory = !this.mandatory || !this.empty;
  var valid = validByErrorStatus && validByMandatory;
  return {
    valid: valid,
    validByErrorStatus: validByErrorStatus,
    validByMandatory: validByMandatory
  };
};

scout.FormField.prototype._updateEmpty = function() {
  // NOP
};

scout.FormField.prototype.requestInput = function() {
  if (this.enabledComputed && this.rendered) {
    this.focus();
  }
};

scout.FormField.prototype.clone = function(model, options) {
  var clone = scout.FormField.parent.prototype.clone.call(this, model, options);
  this._deepCloneProperties(clone, 'menus', options);
  return clone;
};
