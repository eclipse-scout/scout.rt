/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {
  arrays,
  clipboard,
  Device,
  dragAndDrop,
  Event,
  fields,
  FormFieldLayout,
  GridData,
  GroupBox,
  HtmlComponent,
  KeyStrokeContext,
  LoadingSupport,
  menus as menuUtil,
  objects,
  scout,
  Status,
  strings,
  styles,
  tooltips,
  Widget
} from '../../index';
import $ from 'jquery';

/**
 * Base class for all form-fields.
 */
export default class FormField extends Widget {
  constructor() {
    super();

    this.dropType = 0;
    this.dropMaximumSize = dragAndDrop.DEFAULT_DROP_MAXIMUM_SIZE;
    this.empty = true;
    /**
     * @type {Status}
     */
    this.errorStatus = null;
    this.fieldStyle = FormField.DEFAULT_FIELD_STYLE;
    this.gridData = null;
    this.gridDataHints = new GridData();
    this.mode = FormField.Mode.DEFAULT;
    this.keyStrokes = [];
    this.label = null;
    this.labelVisible = true;
    this.labelPosition = FormField.LabelPosition.DEFAULT;
    this.labelWidthInPixel = 0;
    this.labelUseUiWidth = false;
    this.labelHtmlEnabled = false;
    this.mandatory = false;
    this.statusMenuMappings = [];
    this.menus = [];
    this.menusVisible = true;
    this.currentMenuTypes = [];
    this.preventInitialFocus = false;
    this.requiresSave = false;
    this.statusPosition = FormField.StatusPosition.DEFAULT;
    this.statusVisible = true;
    this.suppressStatus = false;
    this.touched = false;
    this.tooltipText = null;
    this.tooltipAnchor = FormField.TooltipAnchor.DEFAULT;
    this.onFieldTooltipOptionsCreator = null;
    this.suppressStatus = null;

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
    this._addCloneProperties(['dropType', 'dropMaximumSize', 'errorStatus', 'fieldStyle', 'gridDataHints', 'gridData', 'label', 'labelVisible', 'labelPosition',
      'labelWidthInPixel', 'labelUseUiWidth', 'mandatory', 'mode', 'preventInitialFocus', 'requiresSave', 'touched', 'statusVisible', 'statusPosition', 'statusMenuMappings',
      'tooltipText', 'tooltipAnchor']);

    this._menuPropertyChangeHandler = this._onMenuPropertyChange.bind(this);
  }

  static FieldStyle = {
    CLASSIC: 'classic',
    ALTERNATIVE: 'alternative'
  };

  static SuppressStatus = {
    /**
     * Suppress status on icon and field (CSS class).
     */
    ALL: 'all',
    /**
     * Suppress status on icon, but still show status on field (CSS class).
     */
    ICON: 'icon',
    /**
     * Suppress status on field (CSS class), but still show status as icon.
     */
    FIELD: 'field'
  };

  /** Global variable to make it easier to adjust the default field style for all fields */
  static DEFAULT_FIELD_STYLE = FormField.FieldStyle.ALTERNATIVE;

  static StatusPosition = {
    DEFAULT: 'default',
    TOP: 'top'
  };

  static LabelPosition = {
    DEFAULT: 0,
    LEFT: 1,
    ON_FIELD: 2,
    RIGHT: 3,
    TOP: 4,
    BOTTOM: 5
  };

  static TooltipAnchor = {
    DEFAULT: 'default',
    ON_FIELD: 'onField'
  };

  static LabelWidth = {
    DEFAULT: 0,
    UI: -1
  };

  // see org.eclipse.scout.rt.client.ui.form.fields.IFormField.FULL_WIDTH
  static FULL_WIDTH = 0;

  static Mode = {
    DEFAULT: 'default',
    CELLEDITOR: 'celleditor'
  };

  static SEVERITY_CSS_CLASSES = 'has-error has-warning has-info has-ok';

  /**
   * @override
   * @returns {KeyStrokeContext}
   */
  _createKeyStrokeContext() {
    return new KeyStrokeContext();
  }

  /**
   * @override
   */
  _createLoadingSupport() {
    return new LoadingSupport({
      widget: this
    });
  }

  _init(model) {
    super._init(model);
    this.resolveConsts([{
      property: 'labelPosition',
      constType: FormField.LabelPosition
    }]);
    this.resolveTextKeys(['label', 'tooltipText']);
    this._setKeyStrokes(this.keyStrokes);
    this._setMenus(this.menus);
    this._setErrorStatus(this.errorStatus);
    this._setGridDataHints(this.gridDataHints);
    this._setGridData(this.gridData);
    this._updateEmpty();
  }

  _initProperty(propertyName, value) {
    if ('gridDataHints' === propertyName) {
      this._initGridDataHints(value);
    } else {
      super._initProperty(propertyName, value);
    }
  }

  /**
   * This function <strong>extends</strong> the default grid data hints of the form field.
   * The default values for grid data hints are set in the constructor of the FormField and its subclasses.
   * When the given gridDataHints is a plain object, we extend our default values. When gridDataHints is
   * already instanceof GridData we overwrite default values completely.
   * @param gridDataHints
   * @private
   */
  _initGridDataHints(gridDataHints) {
    if (gridDataHints instanceof GridData) {
      this.gridDataHints = gridDataHints;
    } else if (objects.isPlainObject(gridDataHints)) {
      $.extend(this.gridDataHints, gridDataHints);
    } else {
      this.gridDataHints = gridDataHints;
    }
  }

  /**
   * All sub-classes of FormField must implement a _render method. The default implementation
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
  _render() {
    throw new Error('sub-classes of FormField must implement a _render method');
  }

  _renderProperties() {
    super._renderProperties();
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
  }

  _remove() {
    super._remove();
    this._removeField();
    this._removeStatus();
    this._removeLabel();
    this._removeIcon();
    this.removeMandatoryIndicator();
    this._removeDisabledCopyOverlay();
    dragAndDrop.uninstallDragAndDropHandler(this);
  }

  setFieldStyle(fieldStyle) {
    this.setProperty('fieldStyle', fieldStyle);
  }

  _renderFieldStyle() {
    this._renderFieldStyleInternal(this.$container);
    this._renderFieldStyleInternal(this.$fieldContainer);
    this._renderFieldStyleInternal(this.$field);
    if (this.rendered) {
      // See _renderLabelPosition why it is necessary to invalidate parent as well.
      let htmlCompParent = this.htmlComp.getParent();
      if (htmlCompParent) {
        htmlCompParent.invalidateLayoutTree();
      }
      this.invalidateLayoutTree();
    }
  }

  _renderFieldStyleInternal($element) {
    if (!$element) {
      return;
    }
    $element.toggleClass('alternative', this.fieldStyle === FormField.FieldStyle.ALTERNATIVE);
  }

  setMandatory(mandatory) {
    this.setProperty('mandatory', mandatory);
  }

  _renderMandatory() {
    this.$container.toggleClass('mandatory', this.mandatory);
  }

  /**
   * Override this function to return another error status property.
   * The default implementation returns the property 'errorStatus'.
   *
   * @return {Status}
   */
  _errorStatus() {
    return this.errorStatus;
  }

  setErrorStatus(errorStatus) {
    this.setProperty('errorStatus', errorStatus);
  }

  _setErrorStatus(errorStatus) {
    errorStatus = Status.ensure(errorStatus);
    this._setProperty('errorStatus', errorStatus);
  }

  /**
   * Adds the given (functional) error status to the list of error status. Prefer this function over #setErrorStatus
   * when you don't want to mess with the internal error states of the field (parsing, validation).
   *
   * @param errorStatus
   */
  addErrorStatus(errorStatus) {
    if (!(errorStatus instanceof Status)) {
      throw new Error('errorStatus is not a Status');
    }
    let status = this._errorStatus();
    if (status) {
      status = status.ensureChildren(); // new instance is required for property change
    } else {
      status = Status.ok('Root');
    }
    status.addStatus(errorStatus);
    this.setErrorStatus(status);
  }

  /**
   * Whether or not the error status is or has the given status type.
   * @param statusType
   * @returns {boolean}
   */
  containsStatus(statusType) {
    if (!this.errorStatus) {
      return false;
    }
    return this.errorStatus.containsStatus(statusType);
  }

  setSuppressStatus(suppressStatus) {
    this.setProperty('suppressStatus', suppressStatus);
  }

  _renderSuppressStatus() {
    this._renderErrorStatus();
  }

  /**
   * @returns {boolean} Whether or not error status icon is suppressed
   */
  _isSuppressStatusIcon() {
    return scout.isOneOf(this.suppressStatus, FormField.SuppressStatus.ALL, FormField.SuppressStatus.ICON);
  }

  /**
   * @returns {boolean} Whether or not error status CSS class is suppressed on field
   */
  _isSuppressStatusField() {
    return scout.isOneOf(this.suppressStatus, FormField.SuppressStatus.ALL, FormField.SuppressStatus.FIELD);
  }

  /**
   * Removes all status (incl. children) with the given type.
   * @param {object} statusType
   */
  removeErrorStatus(statusType) {
    this.removeErrorStatusByPredicate(status => {
      return status instanceof statusType;
    });
  }

  removeErrorStatusByPredicate(predicate) {
    let status = this._errorStatus();
    if (!status) {
      return;
    }
    if (status.containsStatusByPredicate(predicate)) {
      let newStatus = status.clone();
      newStatus.removeAllStatusByPredicate(predicate);
      // If no other status remains -> clear error status
      if (newStatus.hasChildren()) {
        this.setErrorStatus(newStatus);
      } else {
        this.clearErrorStatus();
      }
    }
  }

  clearErrorStatus() {
    this.setErrorStatus(null);
  }

  _renderErrorStatus() {
    let status = this._errorStatus(),
      hasStatus = !!status,
      statusClass = (hasStatus && !this._isSuppressStatusField()) ? 'has-' + status.cssClass() : '';

    this._updateErrorStatusClasses(statusClass, hasStatus);
    this._updateFieldStatus();
  }

  _updateErrorStatusClasses(statusClass, hasStatus) {
    this._updateErrorStatusClassesOnElement(this.$container, statusClass, hasStatus);
    this._updateErrorStatusClassesOnElement(this.$field, statusClass, hasStatus);
  }

  _updateErrorStatusClassesOnElement($element, statusClass, hasStatus) {
    if (!$element) {
      return;
    }
    $element
      .removeClass(FormField.SEVERITY_CSS_CLASSES)
      .addClass(statusClass, hasStatus);
  }

  setTooltipText(tooltipText) {
    this.setProperty('tooltipText', tooltipText);
  }

  _renderTooltipText() {
    this._updateTooltip();
  }

  setTooltipAnchor(tooltipAnchor) {
    this.setProperty('tooltipAnchor', tooltipAnchor);
  }

  _renderTooltipAnchor() {
    this._updateTooltip();
  }

  _updateTooltip() {
    let hasTooltipText = this.hasStatusTooltip();
    this.$container.toggleClass('has-tooltip', hasTooltipText);
    if (this.$field) {
      this.$field.toggleClass('has-tooltip', hasTooltipText);
    }
    this._updateFieldStatus();

    if (this.$fieldContainer) {
      if (this.hasOnFieldTooltip()) {
        let creatorFunc = this.onFieldTooltipOptionsCreator || this._createOnFieldTooltipOptions;
        tooltips.install(this.$fieldContainer, creatorFunc.call(this));
      } else {
        tooltips.uninstall(this.$fieldContainer);
      }
    }
  }

  hasStatusTooltip() {
    return this.tooltipAnchor === FormField.TooltipAnchor.DEFAULT &&
      strings.hasText(this.tooltipText);
  }

  hasOnFieldTooltip() {
    return this.tooltipAnchor === FormField.TooltipAnchor.ON_FIELD &&
      strings.hasText(this.tooltipText);
  }

  setOnFieldTooltipOptionsCreator(onFieldTooltipOptionsCreator) {
    this.onFieldTooltipOptionsCreator = onFieldTooltipOptionsCreator;
  }

  _createOnFieldTooltipOptions() {
    return {
      parent: this,
      text: this.tooltipText,
      arrowPosition: 50
    };
  }

  /**
   * @override
   */
  _renderVisible() {
    super._renderVisible();
    if (this.rendered) {
      // Make sure error status is hidden / shown when visibility changes
      this._renderErrorStatus();
    }
  }

  setLabel(label) {
    this.setProperty('label', label);
  }

  _renderLabel() {
    let label = this.label;
    if (this.labelPosition === FormField.LabelPosition.ON_FIELD) {
      this._renderPlaceholder();
      if (this.$label) {
        this.$label.text('');
      }
    } else if (this.$label) {
      this._removePlaceholder();
      // Make sure an empty label has the same height as the other labels, especially important for top labels
      this.$label
        .contentOrNbsp(this.labelHtmlEnabled, label, 'empty')
        .toggleClass('top', this.labelPosition === FormField.LabelPosition.TOP);

      // Invalidate layout if label width depends on its content
      if (this.labelUseUiWidth || this.labelWidthInPixel === FormField.LabelWidth.UI) {
        this.invalidateLayoutTree();
      }
    }
  }

  /**
   * Renders an empty label for button-like fields that don't have a regular label but which do want to support the 'labelVisible'
   * property in order to provide some layout-flexibility. Makes sure the empty label has the same height as the other labels,
   * which is especially important for top labels.
   */
  _renderEmptyLabel() {
    this.$label
      .html('&nbsp;')
      .toggleClass('top', this.labelPosition === FormField.LabelPosition.TOP);
  }

  _renderPlaceholder($field) {
    $field = scout.nvl($field, this.$field);
    if ($field) {
      $field.placeholder(this.label);
    }
  }

  /**
   * @param $field (optional) argument is required by DateField.js, when not set this.$field is used
   */
  _removePlaceholder($field) {
    $field = scout.nvl($field, this.$field);
    if ($field) {
      $field.placeholder('');
    }
  }

  setLabelVisible(visible) {
    this.setProperty('labelVisible', visible);
  }

  _renderLabelVisible() {
    let visible = this.labelVisible;
    this._renderChildVisible(this.$label, visible);
    this.$container.toggleClass('label-hidden', !visible);
    if (this.rendered && this.labelPosition === FormField.LabelPosition.TOP) {
      // See _renderLabelPosition why it is necessary to invalidate parent as well.
      let htmlCompParent = this.htmlComp.getParent();
      if (htmlCompParent) {
        htmlCompParent.invalidateLayoutTree();
      }
    }
  }

  setLabelWidthInPixel(labelWidthInPixel) {
    this.setProperty('labelWidthInPixel', labelWidthInPixel);
  }

  _renderLabelWidthInPixel() {
    this.invalidateLayoutTree();
  }

  setLabelUseUiWidth(labelUseUiWidth) {
    this.setProperty('labelUseUiWidth', labelUseUiWidth);
  }

  _renderLabelUseUiWidth() {
    this.invalidateLayoutTree();
  }

  setStatusVisible(visible) {
    this.setProperty('statusVisible', visible);
  }

  _renderStatusVisible() {
    this._updateFieldStatus();
  }

  setStatusPosition(statusPosition) {
    this.setProperty('statusPosition', statusPosition);
  }

  _renderStatusPosition(statusPosition) {
    this._updateFieldStatus();
  }

  _tooltip() {
    if (this.fieldStatus) {
      return this.fieldStatus.tooltip;
    }
    return null;
  }

  _updateFieldStatus() {
    if (!this.fieldStatus) {
      return;
    }
    // compute status
    let menus,
      errorStatus = this._errorStatus(),
      status = null,
      statusVisible = this._computeStatusVisible(),
      autoRemove = true;

    this.fieldStatus.setPosition(this.statusPosition);
    this.fieldStatus.setVisible(statusVisible);
    if (!statusVisible) {
      return;
    }

    if (errorStatus) {
      // If the field is used as a cell editor in a editable table, then no validation errors should be shown.
      // (parsing and validation will be handled by the cell/column itself)
      if (this.mode === FormField.Mode.CELLEDITOR) {
        return;
      }
      status = errorStatus;
      autoRemove = !status.isError();
      menus = this._getMenusForStatus(errorStatus);
    } else if (this.hasStatusTooltip()) {
      status = scout.create('Status', {
        message: this.tooltipText,
        severity: Status.Severity.INFO
      });
      // If there are menus, show them in the tooltip. But only if there is a tooltipText, don't do it if there is an error status.
      // Menus make most likely no sense if an error status is displayed
      menus = this.getContextMenuItems();
    } else {
      // If there are menus, show them in the tooltip. But only if there is a tooltipText, don't do it if there is an error status.
      // Menus make most likely no sense if an error status is displayed
      menus = this.getContextMenuItems();
    }

    this.fieldStatus.update(status, menus, autoRemove, this._isInitialShowStatus());
  }

  _isInitialShowStatus() {
    return !!this._errorStatus();
  }

  /**
   * Computes whether the $status should be visible based on statusVisible, errorStatus and tooltip.
   * -> errorStatus and tooltip override statusVisible, so $status may be visible event though statusVisible is set to false
   */
  _computeStatusVisible() {
    let status = this._errorStatus(),
      statusVisible = this.statusVisible,
      hasStatus = !!status,
      hasTooltip = this.hasStatusTooltip();

    return !this._isSuppressStatusIcon() && this.visible && (statusVisible || hasStatus || hasTooltip || (this._hasMenus() && this.menusVisible));
  }

  _renderChildVisible($child, visible) {
    if (!$child) {
      return;
    }
    if ($child.isVisible() !== visible) {
      $child.setVisible(visible);
      this.invalidateLayoutTree();
      return true;
    }
  }

  setLabelPosition(labelPosition) {
    this.setProperty('labelPosition', labelPosition);
  }

  // Don't include in renderProperties, it is not necessary to execute it initially because the positioning is done by _renderLabel
  _renderLabelPosition(position) {
    this._renderLabel();
    if (this.rendered) {
      // Necessary to invalidate parent as well if parent uses the logical grid.
      // LogicalGridData uses another row height depending of the label position
      let htmlCompParent = this.htmlComp.getParent();
      if (htmlCompParent) {
        htmlCompParent.invalidateLayoutTree();
      }
      this.invalidateLayoutTree();
    }
  }

  setLabelHtmlEnabled(labelHtmlEnabled) {
    this.setProperty('labelHtmlEnabled', labelHtmlEnabled);
  }

  _renderLabelHtmlEnabled() {
    // Render the label again when html enabled changes dynamically
    this._renderLabel();
  }

  /**
   * @override
   */
  _renderEnabled() {
    super._renderEnabled();
    if (this.$field) {
      this.$field.setEnabled(this.enabledComputed);
    }
    this._updateDisabledCopyOverlay();
    this._installOrUninstallDragAndDropHandler();
  }

  /**
   * @override Wigdet.js
   */
  _renderDisabledStyle() {
    this._renderDisabledStyleInternal(this.$container);
    this._renderDisabledStyleInternal(this.$fieldContainer);
    this._renderDisabledStyleInternal(this.$field);
    this._renderDisabledStyleInternal(this.$mandatory);
  }

  setFont(font) {
    this.setProperty('font', font);
  }

  _renderFont() {
    styles.legacyFont(this, this.$field);
  }

  setForegroundColor(foregroundColor) {
    this.setProperty('foregroundColor', foregroundColor);
  }

  _renderForegroundColor() {
    styles.legacyForegroundColor(this, this.$field);
  }

  setBackgroundColor(backgroundColor) {
    this.setProperty('backgroundColor', backgroundColor);
  }

  _renderBackgroundColor() {
    styles.legacyBackgroundColor(this, this.$field);
  }

  setLabelFont(labelFont) {
    this.setProperty('labelFont', labelFont);
  }

  _renderLabelFont() {
    styles.legacyFont(this, this.$label, 'label');
  }

  setLabelForegroundColor(labelForegroundColor) {
    this.setProperty('labelForegroundColor', labelForegroundColor);
  }

  _renderLabelForegroundColor() {
    styles.legacyForegroundColor(this, this.$label, 'label');
  }

  setLabelBackgroundColor(labelBackgroundColor) {
    this.setProperty('labelBackgroundColor', labelBackgroundColor);
  }

  _renderLabelBackgroundColor() {
    styles.legacyBackgroundColor(this, this.$label, 'label');
  }

  setGridDataHints(gridData) {
    this.setProperty('gridDataHints', gridData);
  }

  _setGridDataHints(gridData) {
    if (!gridData) {
      gridData = new GridData();
    }
    this._setProperty('gridDataHints', GridData.ensure(gridData));
  }

  _renderGridDataHints() {
    this.parent.invalidateLogicalGrid();
  }

  _setGridData(gridData) {
    if (!gridData) {
      gridData = new GridData();
    }
    this._setProperty('gridData', GridData.ensure(gridData));
  }

  _renderGridData() {
    if (this.rendered) {
      let htmlCompParent = this.htmlComp.getParent();
      if (htmlCompParent) { // may be null if $container is detached
        htmlCompParent.invalidateLayoutTree();
      }
    }
  }

  setMenus(menus) {
    this.setProperty('menus', menus);
  }

  _setMenus(menus) {
    menus = arrays.ensure(menus);
    this.menus.forEach(function(menu) {
      menu.off('propertyChange', this._menuPropertyChangeHandler);
    }, this);

    this.updateKeyStrokes(menus, this.menus);
    this._setProperty('menus', menus);

    this.menus.forEach(function(menu) {
      menu.on('propertyChange', this._menuPropertyChangeHandler);
    }, this);
  }

  insertMenu(menuToInsert) {
    this.insertMenus([menuToInsert]);
  }

  insertMenus(menusToInsert) {
    menusToInsert = arrays.ensure(menusToInsert);
    if (menusToInsert.length === 0) {
      return;
    }
    this.setMenus(this.menus.concat(menusToInsert));
  }

  deleteMenu(menuToDelete) {
    this.deleteMenus([menuToDelete]);
  }

  deleteMenus(menusToDelete) {
    menusToDelete = arrays.ensure(menusToDelete);
    if (menusToDelete.length === 0) {
      return;
    }
    let menus = this.menus.slice();
    arrays.removeAll(menus, menusToDelete);
    this.setMenus(menus);
  }

  _onMenuPropertyChange(event) {
    if (event.propertyName === 'visible' && this.rendered) {
      this._updateMenus();
    }
  }

  getContextMenuItems(onlyVisible = true) {
    if (this.currentMenuTypes.length) {
      return menuUtil.filter(this.menus, this.currentMenuTypes, onlyVisible);
    } else if (onlyVisible) {
      return this.menus.filter(menu => menu.visible);
    }
    return this.menus;
  }

  _getMenusForStatus(status) {
    return this.statusMenuMappings.filter(mapping => {
      if (!mapping.menu || !mapping.menu.visible) {
        return false;
      }
      // Show the menus which are mapped to the status code and severity (if set)
      return (mapping.codes.length === 0 || mapping.codes.indexOf(status.code) > -1) &&
        (mapping.severities.length === 0 || mapping.severities.indexOf(status.severity) > -1);
    }).map(mapping => {
      return mapping.menu;
    });
  }

  _hasMenus() {
    return !!(this.menus && this.getContextMenuItems().length > 0);
  }

  _updateMenus() {
    this.$container.toggleClass('has-menus', this._hasMenus() && this.menusVisible);
    this._updateFieldStatus();
  }

  _renderMenus() {
    this._updateMenus();
  }

  _renderStatusMenuMappings() {
    this._updateMenus();
  }

  setMenusVisible(menusVisible) {
    this.setProperty('menusVisible', menusVisible);
  }

  /**
   * override by TabItem
   **/
  _setMenusVisible(menusVisible) {
    this._setProperty('menusVisible', menusVisible);
  }

  _renderMenusVisible() {
    this._updateMenus();
  }

  setCurrentMenuTypes(currentMenuTypes) {
    this.setProperty('currentMenuTypes', currentMenuTypes);
  }

  _renderCurrentMenuTypes() {
    // If a tooltip is shown, update it with the new menus
    this._updateFieldStatus();
  }

  _setKeyStrokes(keyStrokes) {
    this.updateKeyStrokes(keyStrokes, this.keyStrokes);
    this._setProperty('keyStrokes', keyStrokes);
  }

  /**
   * May be overridden to explicitly provide a tooltip $parent
   */
  _$tooltipParent() {
    // Will be determined by the tooltip itself
    return undefined;
  }

  _hideStatusMessage() {
    if (this.fieldStatus) {
      this.fieldStatus.hideTooltip();
    }
  }

  _renderPreventInitialFocus() {
    this.$container.toggleClass('prevent-initial-focus', !!this.preventInitialFocus);
  }

  /**
   * Sets the focus on this field. If the field is not rendered, the focus will be set as soon as it is rendered.
   *
   * @override
   */
  focus() {
    if (!this.rendered) {
      this.session.layoutValidator.schedulePostValidateFunction(this.focus.bind(this));
      return false;
    }

    if (!this.enabledComputed) {
      return false;
    }

    let focusableElement = this.getFocusableElement();
    if (focusableElement) {
      return this.session.focusManager.requestFocus(focusableElement);
    }
    return false;
  }

  /**
   * This method returns the HtmlElement to be used as initial focus element or when {@link #focus()} is called.
   * It can be overridden, in case the FormField needs to return something other than this.$field[0].
   *
   * @override
   */
  getFocusableElement() {
    if (this.rendered && this.$field) {
      return this.$field[0];
    }
    return null;
  }

  _onFieldFocus(event) {
    this.setFocused(true);
  }

  _onFieldBlur() {
    this.setFocused(false);
  }

  /**
   * When calling this function, the same should happen as when clicking into the field. It is used when the label is clicked.<br>
   * The most basic action is focusing the field but this may differ from field to field.
   */
  activate() {
    if (!this.enabledComputed || !this.rendered) {
      return;
    }
    // Explicitly don't use this.focus() because this.focus uses the focus manager which may be disabled (e.g. on mobile devices)
    let focusableElement = this.getFocusableElement();
    if (focusableElement) {
      $.ensure(focusableElement).focus();
    }
  }

  /**
   * @override
   */
  get$Scrollable() {
    return this.$field;
  }

  getParentGroupBox() {
    let parent = this.parent;
    while (parent && !(parent instanceof GroupBox)) {
      parent = parent.parent;
    }
    return parent;
  }

  getParentField() {
    return this.parent;
  }

  /**
   * Appends a LABEL element to this.$container and sets the this.$label property.
   */
  addLabel() {
    this.$label = this.$container.appendElement('<label>');
    tooltips.installForEllipsis(this.$label, {
      parent: this
    });

    // Setting the focus programmatically does not work in a mousedown listener on mobile devices,
    // that is why a click listener is used instead
    this.$label.on('click', this._onLabelClick.bind(this));
  }

  _onLabelClick(event) {
    if (!strings.hasText(this.label)) {
      // Clicking on "invisible" labels should not have any effect since it is confusing
      return;
    }
    this.activate();
  }

  _removeLabel() {
    if (!this.$label) {
      return;
    }
    tooltips.uninstall(this.$label);
    this.$label.remove();
    this.$label = null;
  }

  /**
   * Links the given element with the label by setting aria-labelledby.<br>
   * This allows screen readers to build a catalog of the elements on the screen and their relationships, for example, to read the label when the input is focused.
   */
  _linkWithLabel($element) {
    if (!this.$label || !$element) {
      return;
    }

    fields.linkElementWithLabel($element, this.$label);
  }

  _removeIcon() {
    if (!this.$icon) {
      return;
    }
    this.$icon.remove();
    this.$icon = null;
  }

  /**
   * Appends the given field to the this.$container and sets the property this.$field.
   * The $field is used as $fieldContainer as long as you don't explicitly call addFieldContainer before calling addField.
   */
  addField($field) {
    if (!this.$fieldContainer) {
      this.addFieldContainer($field);
    }
    this.$field = $field;
    this._linkWithLabel($field);
    this.$field.on('blur', this._onFieldBlur.bind(this))
      .on('focus', this._onFieldFocus.bind(this));
  }

  /**
   * Call this method before addField if you'd like to have a different field container than $field.
   */
  addFieldContainer($fieldContainer) {
    this.$fieldContainer = $fieldContainer
      .addClass('field');

    // Only append if not already appended or it is not the last element so that append would move it to the end
    // This can be important for some widgets, e.g. iframe which would cancel and restart the request on every dom insertion
    if (this.$container.has($fieldContainer).length === 0 || $fieldContainer.next().length > 0) {
      $fieldContainer.appendTo(this.$container);
    }
  }

  /**
   * Removes this.$field and this.$fieldContainer and sets the properties to null.
   */
  _removeField() {
    if (this.$field) {
      this.$field.remove();
      this.$field = null;
    }
    if (this.$fieldContainer) {
      this.$fieldContainer.remove();
      this.$fieldContainer = null;
    }
  }

  /**
   * Appends a SPAN element for form-field status to this.$container and sets the this.$status property.
   */
  addStatus() {
    if (this.fieldStatus) {
      return;
    }
    this.fieldStatus = scout.create('FieldStatus', {
      parent: this,
      position: this.statusPosition,
      // This will be done by _updateFieldStatus again, but doing it here prevents unnecessary layout invalidations later on
      visible: this._computeStatusVisible()
    });
    this.fieldStatus.render();
    this.$status = this.fieldStatus.$container;
    this._updateFieldStatus();
  }

  _removeStatus() {
    if (!this.fieldStatus) {
      return;
    }
    this.fieldStatus.destroy();
    this.$status = null;
    this.fieldStatus = null;
  }

  /**
   * Appends a SPAN element to this.$container and sets the this.$pseudoStatus property.
   * The purpose of a pseudo status is to consume the space an ordinary status would.
   * This makes it possible to make components without a status as width as components with a status.
   */
  addPseudoStatus() {
    this.$pseudoStatus = this.$container.appendSpan('status');
  }

  addMandatoryIndicator() {
    this.$mandatory = this.$container.appendSpan('mandatory-indicator');
  }

  removeMandatoryIndicator() {
    if (!this.$mandatory) {
      return;
    }
    this.$mandatory.remove();
    this.$mandatory = null;
  }

  /**
   * Adds a SPAN element with class 'icon' the the given optional $parent.
   * When $parent is not set, the element is added to this.$container.
   * @param $parent (optional)
   */
  addIcon($parent) {
    if (!$parent) {
      $parent = this.$container;
    }
    this.$icon = fields.appendIcon($parent)
      .on('mousedown', this._onIconMouseDown.bind(this));
  }

  _onIconMouseDown(event) {
    if (!this.enabledComputed) {
      return;
    }
    this.$field.focus();
  }

  /**
   * Appends a DIV element as form-field container to $parent and sets the this.$container property.
   * Applies FormFieldLayout to this.$container (if container does not define another layout).
   * Sets this.htmlComp to the HtmlComponent created for this.$container.
   *
   * @param $parent to which container is appended
   * @param cssClass cssClass to add to the new container DIV
   * @param layout when layout is undefined, this#_createLayout() is called
   *
   */
  addContainer($parent, cssClass, layout) {
    this.$container = $parent.appendDiv('form-field');
    if (cssClass) {
      this.$container.addClass(cssClass);
    }
    let htmlComp = HtmlComponent.install(this.$container, this.session);
    htmlComp.setLayout(layout || this._createLayout());
    this.htmlComp = htmlComp;
  }

  /**
   * @return {FormFieldLayout|AbstractLayout} the default layout FormFieldLayout. Override this function if your field needs another layout.
   */
  _createLayout() {
    return new FormFieldLayout(this);
  }

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
  updateInnerAlignment(opts) {
    opts = opts || {};
    let $fieldContainer = opts.$fieldContainer || this.$fieldContainer;

    this._updateElementInnerAlignment(opts, $fieldContainer);
    if ($fieldContainer !== this.$container) {
      // also set the styles to the container
      this._updateElementInnerAlignment(opts, this.$container);
    }
  }

  _updateElementInnerAlignment(opts, $field) {
    opts = opts || {};
    let useHorizontalAlignment = scout.nvl(opts.useHorizontalAlignment, true);
    let useVerticalAlignment = scout.nvl(opts.useVerticalAlignment, true);

    if (!$field) {
      return;
    }

    $field.removeClass('has-inner-alignment halign-left halign-center halign-right valign-top valign-middle valign-bottom');
    if (useHorizontalAlignment || useVerticalAlignment) {
      // Set horizontal and vertical alignment (from gridData)
      $field.addClass('has-inner-alignment');
      let gridData = this.gridData;
      if (this.parent.logicalGrid) {
        // If the logical grid is calculated by JS, use the hints instead of the calculated grid data
        gridData = this.gridDataHints;
      }
      if (useHorizontalAlignment) {
        let hAlign = gridData.horizontalAlignment;
        $field.addClass(hAlign < 0 ? 'halign-left' : (hAlign > 0 ? 'halign-right' : 'halign-center'));
      }
      if (useVerticalAlignment) {
        let vAlign = gridData.verticalAlignment;
        $field.addClass(vAlign < 0 ? 'valign-top' : (vAlign > 0 ? 'valign-bottom' : 'valign-middle'));
      }
      // Alignment might have affected inner elements (e.g. clear icon)
      this.invalidateLayout();
    }
  }

  addCellEditorFieldCssClasses($field, opts) {
    $field
      .addClass('cell-editor-field')
      .addClass(Device.get().cssClassForEdge());
    if (opts.cssClass) {
      $field.addClass(opts.cssClass);
    }
  }

  prepareForCellEdit(opts) {
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
      this.addCellEditorFieldCssClasses(this.$field, opts);
    }
  }

  setDropType(dropType) {
    this.setProperty('dropType', dropType);
  }

  _renderDropType() {
    this._installOrUninstallDragAndDropHandler();
  }

  setDropMaximumSize(dropMaximumSize) {
    this.setProperty('dropMaximumSize', dropMaximumSize);
  }

  _installOrUninstallDragAndDropHandler() {
    dragAndDrop.installOrUninstallDragAndDropHandler(this._getDragAndDropHandlerOptions());
  }

  /**
   *
   * @return {DragAndDropOptions}
   * @private
   */
  _getDragAndDropHandlerOptions() {
    return {
      target: this,
      doInstall: () => this.dropType && this.enabledComputed,
      container: () => this.$field || this.$container,
      dropType: () => this.dropType,
      onDrop: event => this.trigger('drop', event)
    };
  }

  _updateDisabledCopyOverlay() {
    if (this.disabledCopyOverlay && !Device.get().supportsCopyFromDisabledInputFields()) {
      if (this.enabledComputed) {
        this._removeDisabledCopyOverlay();
      } else {
        this._renderDisabledCopyOverlay();
        this.revalidateLayout(); // because bounds of overlay is set in FormFieldLayout
      }
    }
  }

  _renderDisabledCopyOverlay() {
    if (!this.$disabledCopyOverlay) {
      this.$disabledCopyOverlay = this.$container
        .appendDiv('disabled-overlay')
        .on('contextmenu', this._createCopyContextMenu.bind(this));
    }
  }

  _removeDisabledCopyOverlay() {
    if (this.$disabledCopyOverlay) {
      this.$disabledCopyOverlay.remove();
      this.$disabledCopyOverlay = null;
    }
  }

  _createCopyContextMenu(event) {
    if (!this.visible || strings.empty(this.displayText)) {
      return;
    }

    let menu = scout.create('Menu', {
      parent: this,
      text: this.session.text('ui.Copy'),
      inheritAccessibility: false
    });
    menu.on('action', event => {
      this.exportToClipboard();
    });

    let popup = scout.create('ContextMenuPopup', {
      parent: this,
      menuItems: [menu],
      cloneMenuItems: false,
      location: {
        x: event.pageX,
        y: event.pageY
      }
    });
    popup.open();
  }

  /**
   * Visits this field and all child form fields in pre-order (top-down).
   *
   * @param {function(FormField):string|TreeVisitResult|null} visitor
   * @returns {string} the TreeVisitResult, or nothing to continue.
   */
  visitFields(visitor) {
    return visitor(this);
  }

  /**
   * Visit all parent form fields. The visit stops if the parent is no form field anymore (e.g. a form, desktop or session).
   *
   * @param {function(FormField)} visitor
   */
  visitParentFields(visitor) {
    let curParent = this.parent;
    while (curParent instanceof FormField) {
      visitor(curParent);
      curParent = curParent.parent;
    }
  }

  markAsSaved() {
    this.setProperty('touched', false);
    this.updateRequiresSave();
  }

  touch() {
    this.setProperty('touched', true);
    this.updateRequiresSave();
  }

  /**
   * Updates the requiresSave property by checking if the field is touched or if computeRequiresSave() returns true.
   */
  updateRequiresSave() {
    if (!this.initialized) {
      return;
    }
    this.requiresSave = this.touched || this.computeRequiresSave();
  }

  /**
   * Override this function in order to return whether or not this field requires to be saved.
   * The default impl. returns false.
   *
   * @returns {boolean}
   */
  computeRequiresSave() {
    return false;
  }

  /**
   * @typedef ValidationResult
   * @property {boolean} valid
   * @property {boolean} validByErrorStatus
   * @property {boolean} validByMandatory
   * @property {FormField} field
   * @property {String} label
   * @property {function} reveal
   */

  /**
   * @returns {ValidationResult}
   */
  getValidationResult() {
    let validByErrorStatus = !this._errorStatus();
    let validByMandatory = !this.mandatory || !this.empty;
    let valid = validByErrorStatus && validByMandatory;
    // noinspection JSValidateTypes
    return {
      valid: valid,
      validByErrorStatus: validByErrorStatus,
      validByMandatory: validByMandatory,
      field: this,
      label: this.label,
      reveal: () => {
        fields.selectAllParentTabsOf(this);
        this.focus();
      }
    };
  }

  _updateEmpty() {
    // NOP
  }

  requestInput() {
    if (this.enabledComputed && this.rendered) {
      this.focus();
    }
  }

  clone(model, options) {
    let clone = super.clone(model, options);
    this._deepCloneProperties(clone, 'menus', options);
    return clone;
  }

  exportToClipboard() {
    if (!this.displayText) {
      return;
    }
    let event = new Event({
      text: this.displayText
    });
    this.trigger('clipboardExport', event);
    if (!event.defaultPrevented) {
      this._exportToClipboard(event.text);
    }
  }

  _exportToClipboard(text) {
    clipboard.copyText({
      parent: this,
      text: text
    });
  }
}
