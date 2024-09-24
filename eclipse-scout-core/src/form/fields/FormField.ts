/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  AbstractLayout, Action, aria, arrays, clipboard, CloneOptions, ContextMenuPopup, Device, dragAndDrop, DragAndDropHandler, DragAndDropOptions, DropType, EnumObject, EventHandler, fields, FieldStatus, FormFieldClipboardExportEvent,
  FormFieldEventMap, FormFieldLayout, FormFieldModel, FormFieldValidationResultProvider, GridData, GroupBox, HierarchyChangeEvent, HtmlComponent, InitModelOf, KeyStrokeContext, LoadingSupport, Menu, menus as menuUtil, ObjectOrChildModel,
  ObjectOrModel, objects, ObjectType, Predicate, PropertyChangeEvent, scout, Status, StatusMenuMapping, StatusOrModel, strings, styles, Tooltip, tooltips, TooltipSupport, TreeVisitor, TreeVisitResult, Widget
} from '../../index';
import $ from 'jquery';

/**
 * Base class for all form-fields.
 */
export class FormField extends Widget implements FormFieldModel {
  declare model: FormFieldModel;
  declare eventMap: FormFieldEventMap;
  declare self: FormField;

  dropType: DropType;
  dropMaximumSize: number;
  empty: boolean;
  errorStatus: Status;
  fieldStyle: FormFieldStyle;
  gridData: GridData;
  gridDataHints: GridData;
  mode: FormFieldMode;
  fieldStatus: FieldStatus;
  keyStrokes: Action[];
  displayText: string;
  label: string;
  labelVisible: boolean;
  labelPosition: FormFieldLabelPosition;
  labelWidthInPixel: number;
  labelUseUiWidth: boolean;
  labelHtmlEnabled: boolean;
  mandatory: boolean;
  statusMenuMappings: StatusMenuMapping[];
  menus: Menu[];
  menusVisible: boolean;
  defaultMenuTypes: string[];
  preventInitialFocus: boolean;
  /** If set to true, the field needs to be saved. This will be computed by {@link computeSaveNeeded}. */
  saveNeeded: boolean;
  checkSaveNeeded: boolean;
  lifecycleBoundary: boolean;
  statusPosition: FormFieldStatusPosition;
  statusVisible: boolean;
  suppressStatus: FormFieldSuppressStatus;
  /** If set to true, {@link saveNeeded} will return true as well, even if the value has not been changed. */
  touched: boolean;
  tooltipText: string;
  font: string;
  foregroundColor: string;
  backgroundColor: string;
  labelFont: string;
  labelForegroundColor: string;
  labelBackgroundColor: string;
  tooltipAnchor: FormFieldTooltipAnchor;
  onFieldTooltipOptionsCreator: (this: FormField) => InitModelOf<TooltipSupport>;
  dragAndDropHandler: DragAndDropHandler;
  validationResultProvider: FormFieldValidationResultProvider;
  /**
   * Some browsers don't support copying text from disabled input fields. If such a browser is detected
   * and this flag is true (default is false), an overlay DIV is rendered over disabled fields which
   * provides a custom copy context menu that opens the ClipboardForm.
   */
  disabledCopyOverlay: boolean;

  $label: JQuery;
  /**
   * Note the difference between $field and $fieldContainer:
   * - $field points to the input-field (typically a browser-text field)
   * - $fieldContainer could point to the same input-field or when the field is a composite,
   *   to the parent DIV of that composite. For instance: the multi-line-smartfield is a
   *   composite with an input-field and a DIV showing the additional lines. In that case $field
   *   points to the input-field and $fieldContainer to the parent DIV of the input-field.
   *   This property should be used primarily for layout-functionality.
   */
  $field: JQuery;
  $clearIcon: JQuery;
  $fieldContainer: JQuery;
  $icon: JQuery;
  $pseudoStatus: JQuery;
  /**
   * The status is used for error-status, tooltip-icon and menus.
   */
  $status: JQuery;
  $mandatory: JQuery;
  $disabledCopyOverlay: JQuery;
  protected _menuPropertyChangeHandler: EventHandler<PropertyChangeEvent<any, Menu>>;
  protected _hierarchyChangeHandler: EventHandler<HierarchyChangeEvent>;

  constructor() {
    super();

    this.dropType = DropType.NONE;
    this.dropMaximumSize = dragAndDrop.DEFAULT_DROP_MAXIMUM_SIZE;
    this.empty = true;
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
    this.defaultMenuTypes = [];
    this.preventInitialFocus = false;
    this.saveNeeded = false;
    this.checkSaveNeeded = true;
    this.lifecycleBoundary = false;
    this.statusPosition = FormField.StatusPosition.DEFAULT;
    this.statusVisible = true;
    this.suppressStatus = null;
    this.touched = false;
    this.tooltipText = null;
    this.tooltipAnchor = FormField.TooltipAnchor.DEFAULT;
    this.onFieldTooltipOptionsCreator = null;
    this.validationResultProvider = this._createValidationResultProvider();

    this.$label = null;
    this.$field = null;
    this.$fieldContainer = null;
    this.$icon = null;
    this.$status = null;

    this.disabledCopyOverlay = false;
    this.$disabledCopyOverlay = null;

    this._addWidgetProperties(['keyStrokes', 'menus', 'statusMenuMappings']);
    this._addCloneProperties(['dropType', 'dropMaximumSize', 'errorStatus', 'fieldStyle', 'gridDataHints', 'gridData', 'label', 'labelVisible', 'labelPosition',
      'labelWidthInPixel', 'labelUseUiWidth', 'mandatory', 'mode', 'preventInitialFocus', 'saveNeeded', 'touched', 'statusVisible', 'statusPosition', 'statusMenuMappings',
      'tooltipText', 'tooltipAnchor']);

    this._menuPropertyChangeHandler = this._onMenuPropertyChange.bind(this);
    this._hierarchyChangeHandler = this._onHierarchyChange.bind(this);
  }

  static FieldStyle = {
    CLASSIC: 'classic',
    ALTERNATIVE: 'alternative'
  } as const;

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
  } as const;

  /** Global variable to make it easier to adjust the default field style for all fields */
  static DEFAULT_FIELD_STYLE = FormField.FieldStyle.ALTERNATIVE;

  static StatusPosition = {
    DEFAULT: 'default',
    TOP: 'top'
  } as const;

  static LabelPosition = {
    DEFAULT: 0,
    LEFT: 1,
    ON_FIELD: 2,
    RIGHT: 3,
    TOP: 4,
    BOTTOM: 5
  } as const;

  static TooltipAnchor = {
    DEFAULT: 'default',
    ON_FIELD: 'onField'
  } as const;

  static LabelWidth = {
    DEFAULT: 0,
    UI: -1
  } as const;

  // see org.eclipse.scout.rt.client.ui.form.fields.IFormField.FULL_WIDTH
  static FULL_WIDTH = 0;

  static Mode = {
    DEFAULT: 'default',
    CELLEDITOR: 'celleditor'
  } as const;

  static SEVERITY_CSS_CLASSES = 'has-error has-warning has-info has-ok';

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _createLoadingSupport(): LoadingSupport {
    return new LoadingSupport({
      widget: this
    });
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.resolveConsts([{
      property: 'labelPosition',
      constType: FormField.LabelPosition
    }]);
    this.resolveTextKeys(['label', 'tooltipText']);
    this._setValidationResultProvider(this.validationResultProvider);
    this._setKeyStrokes(this.keyStrokes);
    this._setMenus(this.menus);
    this._setErrorStatus(this.errorStatus);
    this._setGridDataHints(this.gridDataHints);
    this._setGridData(this.gridData);
    this._updateEmpty();
    this._watchFieldHierarchy();
  }

  protected override _destroy() {
    this._unwatchFieldHierarchy();
    super._destroy();
  }

  protected override _initProperty(propertyName: string, value: any) {
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
   */
  private _initGridDataHints(gridDataHints: GridData) {
    if (gridDataHints instanceof GridData) {
      this.gridDataHints = gridDataHints;
    } else if (objects.isPlainObject(gridDataHints)) {
      $.extend(this.gridDataHints, gridDataHints);
    } else {
      this.gridDataHints = gridDataHints;
    }
  }

  /**
   * All subclasses of FormField should implement a _render method. It should call the various add* methods provided by the FormField class.
   *
   * A possible _render implementation could look like this.
   * <pre>
   * this.addContainer(this.$parent, 'form-field');
   * this.addLabel();
   * this.addField(this.$parent.makeDiv('foo', 'bar'));
   * this.addMandatoryIndicator();
   * this.addStatus();
   * </pre>
   */
  protected override _render() {
    // Render all the necessary parts of a form field.
    // Subclasses typically override _render completely and add these parts by themselves
    this.addContainer(this.$parent);
    this.addLabel();
    this.addField(this.$parent.makeDiv());
    this.addMandatoryIndicator();
    this.addStatus();
  }

  protected override _renderProperties() {
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

  protected override _remove() {
    super._remove();
    this._removeField();
    this._removeStatus();
    this._removeLabel();
    this._removeIcon();
    this.removeMandatoryIndicator();
    this._removeDisabledCopyOverlay();
    dragAndDrop.uninstallDragAndDropHandler(this);
  }

  /** @see FormFieldModel.fieldStyle */
  setFieldStyle(fieldStyle: FormFieldStyle) {
    this.setProperty('fieldStyle', fieldStyle);
  }

  protected _renderFieldStyle() {
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

  protected _renderFieldStyleInternal($element: JQuery) {
    if (!$element) {
      return;
    }
    $element.toggleClass('alternative', this.fieldStyle === FormField.FieldStyle.ALTERNATIVE);
  }

  /** @see FormFieldModel.mandatory */
  setMandatory(mandatory: boolean) {
    this.setProperty('mandatory', mandatory);
  }

  protected _renderMandatory() {
    this.$container.toggleClass('mandatory', this.mandatory);
    aria.required(this.$field, this.mandatory || null);
  }

  /**
   * Override this function to return another error status property.
   * The default implementation returns the property 'errorStatus'.
   */
  protected _errorStatus(): Status {
    return this.errorStatus;
  }

  /** @see FormFieldModel.errorStatus */
  setErrorStatus(errorStatus: StatusOrModel) {
    this.setProperty('errorStatus', errorStatus);
  }

  protected _setErrorStatus(errorStatus: StatusOrModel) {
    errorStatus = Status.ensure(errorStatus);
    this._setProperty('errorStatus', errorStatus);
  }

  /**
   * Adds the given (functional) error status to the list of error status. Prefer this function over #setErrorStatus
   * when you don't want to mess with the internal error states of the field (parsing, validation).
   */
  addErrorStatus(errorStatus: string | Status) {
    if (typeof errorStatus === 'string') {
      errorStatus = this._createErrorStatus(errorStatus);
    }
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
   * Create an error status with severity {@link Status.Severity.ERROR} containing the given message.
   *
   * @param message The message for the error status.
   * @returns containing the given message.
   */
  protected _createErrorStatus(message: string): Status {
    return Status.error(message);
  }

  /**
   * Whether the error status is or has the given status type.
   */
  containsStatus(statusType: abstract new() => Status): boolean {
    if (!this.errorStatus) {
      return false;
    }
    return this.errorStatus.containsStatus(statusType);
  }

  /** @see FormFieldModel.suppressStatus */
  setSuppressStatus(suppressStatus: FormFieldSuppressStatus) {
    this.setProperty('suppressStatus', suppressStatus);
  }

  protected _renderSuppressStatus() {
    this._renderErrorStatus();
  }

  /**
   * @returns Whether or not error status icon is suppressed
   */
  protected _isSuppressStatusIcon(): boolean {
    return scout.isOneOf(this.suppressStatus, FormField.SuppressStatus.ALL, FormField.SuppressStatus.ICON);
  }

  /**
   * @returns Whether or not error status CSS class is suppressed on field
   */
  protected _isSuppressStatusField(): boolean {
    return scout.isOneOf(this.suppressStatus, FormField.SuppressStatus.ALL, FormField.SuppressStatus.FIELD);
  }

  /**
   * Removes all status (incl. children) with the given type.
   */
  removeErrorStatus(statusType: new() => Status) {
    this.removeErrorStatusByPredicate(status => status instanceof statusType);
  }

  removeErrorStatusByPredicate(predicate: Predicate<Status>) {
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

  /** @internal */
  _renderErrorStatus() {
    let status = this._errorStatus(),
      hasStatus = !!status,
      statusClass = (hasStatus && !this._isSuppressStatusField()) ? 'has-' + status.cssClass() : '';

    this._updateErrorStatusClasses(statusClass, hasStatus);
    this._updateFieldStatus();
  }

  protected _updateErrorStatusClasses(statusClass: string, hasStatus: boolean) {
    this._updateErrorStatusClassesOnElement(this.$container, statusClass, hasStatus);
    this._updateErrorStatusClassesOnElement(this.$field, statusClass, hasStatus);
  }

  protected _updateErrorStatusClassesOnElement($element: JQuery, statusClass: string, hasStatus: boolean) {
    if (!$element) {
      return;
    }
    $element
      .removeClass(FormField.SEVERITY_CSS_CLASSES)
      .addClass(statusClass);
  }

  /** @see FormFieldModel.tooltipText */
  setTooltipText(tooltipText: string) {
    this.setProperty('tooltipText', tooltipText);
  }

  /** @internal */
  _renderTooltipText() {
    this._updateTooltip();
  }

  /** @see FormFieldModel.tooltipAnchor */
  setTooltipAnchor(tooltipAnchor: FormFieldTooltipAnchor) {
    this.setProperty('tooltipAnchor', tooltipAnchor);
  }

  protected _renderTooltipAnchor() {
    this._updateTooltip();
  }

  protected _updateTooltip() {
    let hasTooltipText = this.hasStatusTooltip();
    this.$container.toggleClass('has-tooltip', hasTooltipText);
    if (this.$field) {
      this.$field.toggleClass('has-tooltip', hasTooltipText);
    }

    this._updateFieldStatus();
    aria.description(this.$field, this.tooltipText);

    if (this.$fieldContainer) {
      if (this.hasOnFieldTooltip()) {
        let creatorFunc = this.onFieldTooltipOptionsCreator || this._createOnFieldTooltipOptions;
        tooltips.install(this.$fieldContainer, creatorFunc.call(this));
      } else {
        tooltips.uninstall(this.$fieldContainer);
      }
    }
  }

  hasStatusTooltip(): boolean {
    return this.tooltipAnchor === FormField.TooltipAnchor.DEFAULT && strings.hasText(this.tooltipText);
  }

  hasOnFieldTooltip(): boolean {
    return this.tooltipAnchor === FormField.TooltipAnchor.ON_FIELD && strings.hasText(this.tooltipText);
  }

  /** @see FormFieldModel.onFieldTooltipOptionsCreator */
  setOnFieldTooltipOptionsCreator(onFieldTooltipOptionsCreator: (this: FormField) => InitModelOf<TooltipSupport>) {
    this.onFieldTooltipOptionsCreator = onFieldTooltipOptionsCreator;
  }

  protected _createOnFieldTooltipOptions(): InitModelOf<TooltipSupport> {
    return {
      parent: this,
      text: this.tooltipText,
      arrowPosition: 50
    };
  }

  protected override _renderVisible() {
    super._renderVisible();
    if (this.rendered) {
      // Make sure error status is hidden / shown when visibility changes
      this._renderErrorStatus();
    }
  }

  /** @see FormFieldModel.label */
  setLabel(label: string) {
    this.setProperty('label', label);
  }

  protected _renderLabel() {
    let label = this.label;
    if (this.labelPosition === FormField.LabelPosition.ON_FIELD) {
      this._renderPlaceholder();
      if (this.$label) {
        this.$label.text('');
      }
      aria.label(this.$field, this.label);
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
  protected _renderEmptyLabel() {
    this.$label
      .html('&nbsp;')
      .toggleClass('top', this.labelPosition === FormField.LabelPosition.TOP);
  }

  protected _renderPlaceholder($field?: JQuery) {
    $field = scout.nvl($field, this.$field);
    if ($field) {
      $field.placeholder(this.label);
    }
  }

  /**
   * @param $field argument is required by DateField.js, when not set this.$field is used
   */
  protected _removePlaceholder($field?: JQuery) {
    $field = scout.nvl($field, this.$field);
    if ($field) {
      $field.placeholder('');
    }
  }

  /** @see FormFieldModel.labelVisible */
  setLabelVisible(visible: boolean) {
    this.setProperty('labelVisible', visible);
  }

  protected _renderLabelVisible() {
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

  /** @see FormFieldModel.labelWidthInPixel */
  setLabelWidthInPixel(labelWidthInPixel: number) {
    this.setProperty('labelWidthInPixel', labelWidthInPixel);
  }

  protected _renderLabelWidthInPixel() {
    this.invalidateLayoutTree();
  }

  /** @see FormFieldModel.labelUseUiWidth */
  setLabelUseUiWidth(labelUseUiWidth: number) {
    this.setProperty('labelUseUiWidth', labelUseUiWidth);
  }

  protected _renderLabelUseUiWidth() {
    this.invalidateLayoutTree();
  }

  /** @see FormFieldModel.statusVisible */
  setStatusVisible(visible: boolean) {
    this.setProperty('statusVisible', visible);
  }

  protected _renderStatusVisible() {
    this._updateFieldStatus();
  }

  /** @see FormFieldModel.statusPosition */
  setStatusPosition(statusPosition: FormFieldStatusPosition) {
    this.setProperty('statusPosition', statusPosition);
  }

  protected _renderStatusPosition() {
    this._updateFieldStatus();
  }

  /**
   * The tooltip of the {@link fieldStatus}, if it is shown.
   */
  tooltip(): Tooltip {
    if (this.fieldStatus) {
      return this.fieldStatus.tooltip;
    }
    return null;
  }

  protected _updateFieldStatus() {
    if (!this.fieldStatus) {
      return;
    }
    // compute status
    let menus: Menu[],
      errorStatus = this._errorStatus(),
      status: Status = null,
      statusVisible = this._computeStatusVisible(),
      autoRemove = true;

    this.fieldStatus.setPosition(this.statusPosition);
    this.fieldStatus.setVisible(statusVisible);
    if (!statusVisible) {
      return;
    }

    if (errorStatus) {
      // If the field is used as a cell editor in an editable table, then no validation errors should be shown.
      // (parsing and validation will be handled by the cell/column itself)
      if (this.mode === FormField.Mode.CELLEDITOR) {
        return;
      }
      status = errorStatus;
      autoRemove = !status.isError();
      menus = this._getMenusForStatus(errorStatus);
    } else if (this.hasStatusTooltip()) {
      status = scout.create(Status, {
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

  protected _isInitialShowStatus(): boolean {
    return !!this._errorStatus();
  }

  /**
   * Computes whether the $status should be visible based on statusVisible, errorStatus and tooltip.
   * -> errorStatus and tooltip override statusVisible, so $status may be visible event though statusVisible is set to false
   */
  protected _computeStatusVisible(): boolean {
    let status = this._errorStatus(),
      statusVisible = this.statusVisible,
      hasStatus = !!status,
      hasTooltip = this.hasStatusTooltip();

    return !this._isSuppressStatusIcon() && this.visible && (statusVisible || hasStatus || hasTooltip || (this._hasMenus() && this.menusVisible));
  }

  protected _renderChildVisible($child: JQuery, visible: boolean): boolean {
    if (!$child) {
      return;
    }
    if ($child.isVisible() !== visible) {
      $child.setVisible(visible);
      this.invalidateLayoutTree();
      return true;
    }
  }

  /** @see FormFieldModel.labelPosition */
  setLabelPosition(labelPosition: FormFieldLabelPosition) {
    this.setProperty('labelPosition', labelPosition);
  }

  // Don't include in renderProperties, it is not necessary to execute it initially because the positioning is done by _renderLabel
  protected _renderLabelPosition() {
    this._renderLabel();
    if (this.rendered) {
      // Necessary to invalidate parent as well if parent uses the logical grid.
      // LogicalGridData uses another row height depending on the label position
      let htmlCompParent = this.htmlComp.getParent();
      if (htmlCompParent) {
        htmlCompParent.invalidateLayoutTree();
      }
      this.invalidateLayoutTree();
    }
  }

  /** @see FormFieldModel.labelHtmlEnabled */
  setLabelHtmlEnabled(labelHtmlEnabled: boolean) {
    this.setProperty('labelHtmlEnabled', labelHtmlEnabled);
  }

  protected _renderLabelHtmlEnabled() {
    // Render the label again when html enabled changes dynamically
    this._renderLabel();
  }

  protected override _renderEnabled() {
    super._renderEnabled();
    if (this.$field) {
      this.$field.setEnabled(this.enabledComputed);
    }
    this._updateDisabledCopyOverlay();
    this._installOrUninstallDragAndDropHandler();
  }

  protected override _renderDisabledStyle() {
    this._renderDisabledStyleInternal(this.$container);
    this._renderDisabledStyleInternal(this.$fieldContainer);
    this._renderDisabledStyleInternal(this.$field);
    this._renderDisabledStyleInternal(this.$mandatory);
  }

  /** @see FormFieldModel.font */
  setFont(font: string) {
    this.setProperty('font', font);
  }

  protected _renderFont() {
    styles.legacyFont(this, this.$field);
  }

  /** @see FormFieldModel.foregroundColor */
  setForegroundColor(foregroundColor: string) {
    this.setProperty('foregroundColor', foregroundColor);
  }

  protected _renderForegroundColor() {
    styles.legacyForegroundColor(this, this.$field);
  }

  /** @see FormFieldModel.backgroundColor */
  setBackgroundColor(backgroundColor: string) {
    this.setProperty('backgroundColor', backgroundColor);
  }

  protected _renderBackgroundColor() {
    styles.legacyBackgroundColor(this, this.$field);
  }

  /** @see FormFieldModel.labelFont */
  setLabelFont(labelFont: string) {
    this.setProperty('labelFont', labelFont);
  }

  protected _renderLabelFont() {
    styles.legacyFont(this, this.$label, 'label');
  }

  /** @see FormFieldModel.labelForegroundColor */
  setLabelForegroundColor(labelForegroundColor: string) {
    this.setProperty('labelForegroundColor', labelForegroundColor);
  }

  protected _renderLabelForegroundColor() {
    styles.legacyForegroundColor(this, this.$label, 'label');
  }

  /** @see FormFieldModel.labelBackgroundColor */
  setLabelBackgroundColor(labelBackgroundColor: string) {
    this.setProperty('labelBackgroundColor', labelBackgroundColor);
  }

  protected _renderLabelBackgroundColor() {
    styles.legacyBackgroundColor(this, this.$label, 'label');
  }

  /** @see FormFieldModel.gridDataHints */
  setGridDataHints(gridData: ObjectOrModel<GridData>) {
    this.setProperty('gridDataHints', gridData);
  }

  protected _setGridDataHints(gridData: ObjectOrModel<GridData>) {
    this._setProperty('gridDataHints', GridData.ensure(gridData || new GridData()));
  }

  protected _renderGridDataHints() {
    this.parent.invalidateLogicalGrid();
  }

  /** @internal */
  _setGridData(gridData: ObjectOrModel<GridData>) {
    this._setProperty('gridData', GridData.ensure(gridData || new GridData()));
  }

  protected _renderGridData() {
    if (this.rendered) {
      let htmlCompParent = this.htmlComp.getParent();
      if (htmlCompParent) { // may be null if $container is detached
        htmlCompParent.invalidateLayoutTree();
      }
    }
  }

  /** @see FormFieldModel.menus */
  setMenus(menus: ObjectOrChildModel<Menu>[]) {
    this.setProperty('menus', menus);
  }

  protected _setMenus(menus: Menu | Menu[]) {
    menus = arrays.ensure(menus);
    this.menus.forEach(menu => menu.off('propertyChange', this._menuPropertyChangeHandler));
    this.updateKeyStrokes(menus, this.menus);
    this._setProperty('menus', menus);
    this.menus.forEach(menu => menu.on('propertyChange', this._menuPropertyChangeHandler));
  }

  insertMenu(menuToInsert: ObjectOrChildModel<Menu>) {
    this.insertMenus([menuToInsert]);
  }

  insertMenus(menusToInsert: ObjectOrChildModel<Menu>[]) {
    menusToInsert = arrays.ensure(menusToInsert);
    if (menusToInsert.length === 0) {
      return;
    }
    let menus = this.menus as ObjectOrChildModel<Menu>[];
    this.setMenus(menus.concat(menusToInsert));
  }

  deleteMenu(menuToDelete: Menu) {
    this.deleteMenus([menuToDelete]);
  }

  deleteMenus(menusToDelete: Menu[]) {
    menusToDelete = arrays.ensure(menusToDelete);
    if (menusToDelete.length === 0) {
      return;
    }
    let menus = this.menus.slice();
    arrays.removeAll(menus, menusToDelete);
    this.setMenus(menus);
  }

  protected _onMenuPropertyChange(event: PropertyChangeEvent<any, Menu>) {
    if (event.propertyName === 'visible' && this.rendered) {
      this._updateMenus();
    }
  }

  getContextMenuItems(onlyVisible = true): Menu[] {
    let currentMenuTypes = this.getCurrentMenuTypes();
    if (currentMenuTypes.length) {
      return menuUtil.filter(this.menus, currentMenuTypes, {onlyVisible: onlyVisible, defaultMenuTypes: this.defaultMenuTypes});
    } else if (onlyVisible) {
      return this.menus.filter(menu => menu.visible);
    }
    return this.menus;
  }

  protected _getMenusForStatus(status: Status): Menu[] {
    return this.statusMenuMappings.filter(mapping => {
      if (!mapping.menu || !mapping.menu.visible) {
        return false;
      }
      // Show the menus which are mapped to the status code and severity (if set)
      return (mapping.codes.length === 0 || mapping.codes.indexOf(status.code) > -1)
        && (mapping.severities.length === 0 || mapping.severities.indexOf(status.severity) > -1);
    }).map(mapping => mapping.menu);
  }

  protected _hasMenus(): boolean {
    return !!(this.menus && this.getContextMenuItems().length > 0);
  }

  /** @internal */
  _updateMenus() {
    if (!this.rendered && !this.rendering) {
      return;
    }
    this.$container.toggleClass('has-menus', this._hasMenus() && this.menusVisible);
    this._updateFieldStatus();
  }

  /** @internal */
  _renderMenus() {
    this._updateMenus();
  }

  protected _renderStatusMenuMappings() {
    this._updateMenus();
  }

  setMenusVisible(menusVisible: boolean) {
    this.setProperty('menusVisible', menusVisible);
  }

  protected _setMenusVisible(menusVisible: boolean) {
    this._setProperty('menusVisible', menusVisible);
  }

  protected _renderMenusVisible() {
    this._updateMenus();
  }

  getCurrentMenuTypes(): string[] {
    return this._getCurrentMenuTypes();
  }

  protected _getCurrentMenuTypes(): string[] {
    return [];
  }

  protected _setKeyStrokes(keyStrokes: Action[]) {
    this.updateKeyStrokes(keyStrokes, this.keyStrokes);
    this._setProperty('keyStrokes', keyStrokes);
  }

  /**
   * May be overridden to explicitly provide a tooltip $parent
   * @internal
   */
  _$tooltipParent(): JQuery {
    // Will be determined by the tooltip itself
    return undefined;
  }

  /** @internal */
  _hideStatusMessage() {
    if (this.fieldStatus) {
      this.fieldStatus.hideTooltip();
    }
  }

  protected _renderPreventInitialFocus() {
    this.$container.toggleClass('prevent-initial-focus', !!this.preventInitialFocus);
  }

  /**
   * Sets the focus on this field. If the field is not rendered, the focus will be set as soon as it is rendered.
   * @returns true if the element could be focused, false if not
   */
  override focus(): boolean {
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
   */
  override getFocusableElement(): HTMLElement | JQuery {
    if (this.rendered && this.$field) {
      return this.$field[0];
    }
    return null;
  }

  protected _onFieldFocus(event: JQuery.FocusEvent) {
    this.setFocused(true);
  }

  protected _onFieldBlur(event: JQuery.BlurEvent) {
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

  override get$Scrollable(): JQuery {
    return this.$field;
  }

  getParentGroupBox(): GroupBox {
    return this.findParent(GroupBox);
  }

  getParentField(): FormField {
    return this.findParent(FormField);
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

  protected _onLabelClick(event: JQuery.ClickEvent) {
    if (!strings.hasText(this.label)) {
      // Clicking on "invisible" labels should not have any effect since it is confusing
      return;
    }
    this.activate();
  }

  protected _removeLabel() {
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
  protected _linkWithLabel($element: JQuery) {
    if (strings.empty(this.label)) { // no label, do not link field to nbsp
      return;
    }

    if (this.labelPosition !== FormField.LabelPosition.ON_FIELD) {
      aria.linkElementWithLabel($element, this.$label);
    }
  }

  protected _removeIcon() {
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
  addField($field: JQuery) {
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
  addFieldContainer($fieldContainer: JQuery) {
    this.$fieldContainer = $fieldContainer
      .addClass('field');

    // Only append if not already appended, or it is not the last element so that append would move it to the end
    // This can be important for some widgets, e.g. iframe which would cancel and restart the request on every dom insertion
    if (this.$container.has($fieldContainer[0]).length === 0 || $fieldContainer.next().length > 0) {
      $fieldContainer.appendTo(this.$container);
    }
  }

  /**
   * Removes this.$field and this.$fieldContainer and sets the properties to null.
   */
  protected _removeField() {
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
   * Appends a span element for form-field status to this.$container and sets the this.$status property.
   */
  addStatus() {
    if (this.fieldStatus) {
      return;
    }
    this.fieldStatus = scout.create(FieldStatus, {
      parent: this,
      position: this.statusPosition,
      // This will be done by _updateFieldStatus again, but doing it here prevents unnecessary layout invalidations later on
      visible: this._computeStatusVisible()
    });
    this.fieldStatus.render();
    this.$status = this.fieldStatus.$container;
    this._updateFieldStatus();
  }

  protected _removeStatus() {
    if (!this.fieldStatus) {
      return;
    }
    this.fieldStatus.destroy();
    this.$status = null;
    this.fieldStatus = null;
  }

  /**
   * Appends a span element to this.$container and sets the this.$pseudoStatus property.
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
   * Adds a span element with class 'icon' the given optional $parent.
   * When $parent is not set, the element is added to this.$container.
   */
  addIcon($parent?: JQuery) {
    if (!$parent) {
      $parent = this.$container;
    }
    this.$icon = fields.appendIcon($parent)
      .on('mousedown', this._onIconMouseDown.bind(this));
    aria.hidden(this.$icon, true);
  }

  protected _onIconMouseDown(event: JQuery.MouseDownEvent) {
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
   * @param layout when layout is undefined, {@link _createLayout} is called
   *
   */
  addContainer($parent: JQuery, cssClass?: string, layout?: AbstractLayout) {
    this.$container = $parent.appendDiv('form-field');
    if (cssClass) {
      this.$container.addClass(cssClass);
    }
    let htmlComp = HtmlComponent.install(this.$container, this.session);
    htmlComp.setLayout(layout || this._createLayout());
    this.htmlComp = htmlComp;
  }

  /**
   * @returns the default layout FormFieldLayout. Override this function if your field needs another layout.
   */
  protected _createLayout(): AbstractLayout {
    return new FormFieldLayout(this);
  }

  /**
   * Updates the "inner alignment" of a field. Usually, the GridData hints only have influence on the LogicalGridLayout.
   * However, the properties "horizontalAlignment" and "verticalAlignment" are sometimes used differently.
   * Instead of controlling the field alignment in case fillHorizontal/fillVertical is false, the developer expects the _contents_ of the field to be aligned correspondingly inside the field.
   * Technically, this is not correct, but is supported for legacy and convenience reasons for some Scout fields.
   * Those who support the behavior may override _renderGridData() and call this method.
   * Some CSS classes are then added to the field.
   */
  updateInnerAlignment(opts?: FormFieldAlignmentUpdateOptions) {
    opts = opts || {};
    let $fieldContainer = opts.$fieldContainer || this.$fieldContainer;

    this._updateElementInnerAlignment(opts, $fieldContainer);
    if ($fieldContainer !== this.$container) {
      // also set the styles to the container
      this._updateElementInnerAlignment(opts, this.$container);
    }
  }

  protected _updateElementInnerAlignment(opts: FormFieldAlignmentUpdateOptions, $field: JQuery) {
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

  addCellEditorFieldCssClasses($field: JQuery, opts: AddCellEditorFieldCssClassesOptions) {
    $field
      .addClass('cell-editor-field')
      .addClass(Device.get().cssClassForEdge());
    if (opts.cssClass) {
      $field.addClass(opts.cssClass);
    }
  }

  prepareForCellEdit(opts?: AddCellEditorFieldCssClassesOptions) {
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

  /** @see FormFieldModel.dropType */
  setDropType(dropType: DropType) {
    this.setProperty('dropType', dropType);
  }

  protected _renderDropType() {
    this._installOrUninstallDragAndDropHandler();
  }

  /** @see FormFieldModel.dropMaximumSize */
  setDropMaximumSize(dropMaximumSize: number) {
    this.setProperty('dropMaximumSize', dropMaximumSize);
  }

  protected _installOrUninstallDragAndDropHandler() {
    dragAndDrop.installOrUninstallDragAndDropHandler(this._getDragAndDropHandlerOptions());
  }

  protected _getDragAndDropHandlerOptions(): DragAndDropOptions {
    return {
      target: this,
      doInstall: () => this.dropType && this.enabledComputed,
      container: () => this.$field || this.$container,
      dropType: () => this.dropType,
      onDrop: event => this.trigger('drop', event)
    };
  }

  protected _updateDisabledCopyOverlay() {
    if (this.disabledCopyOverlay && !Device.get().supportsCopyFromDisabledInputFields()) {
      if (this.enabledComputed) {
        this._removeDisabledCopyOverlay();
      } else {
        this._renderDisabledCopyOverlay();
        this.revalidateLayout(); // because bounds of overlay is set in FormFieldLayout
      }
    }
  }

  protected _renderDisabledCopyOverlay() {
    if (!this.$disabledCopyOverlay) {
      this.$disabledCopyOverlay = this.$container
        .appendDiv('disabled-overlay')
        .on('contextmenu', this._createCopyContextMenu.bind(this));
    }
  }

  protected _removeDisabledCopyOverlay() {
    if (this.$disabledCopyOverlay) {
      this.$disabledCopyOverlay.remove();
      this.$disabledCopyOverlay = null;
    }
  }

  protected _createCopyContextMenu(event: JQuery.ContextMenuEvent) {
    if (!this.visible || strings.empty(this.displayText)) {
      return;
    }

    let menu = scout.create(Menu, {
      parent: this,
      text: this.session.text('ui.Copy'),
      inheritAccessibility: false
    });
    menu.on('action', event => this.exportToClipboard());

    let popup = scout.create(ContextMenuPopup, {
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
   * Visits this field and all child {@link FormField}s in pre-order (top-down).
   */
  visitFields(visitor: TreeVisitor<FormField>, options: VisitFieldsOptions = {}): TreeVisitResult {
    return this.visit(child => {
      if (child instanceof FormField) {
        let visitResult = visitor(child);
        return scout.nvl(options.firstLevelFieldsOnly, false) && this !== child ? TreeVisitResult.SKIP_SUBTREE : visitResult;
      }
      if (scout.nvl(options.limitToSameFieldTree, false)) {
        return TreeVisitResult.SKIP_SUBTREE;
      }
      return TreeVisitResult.CONTINUE;
    }, {visitSelf: options.visitSelf});
  }

  visitFirstChildFields(visitor: TreeVisitor<FormField>) {
    this.visitFields(field => visitor(field), {firstLevelFieldsOnly: true, visitSelf: false});
  }

  /**
   * Visits all parent form fields.
   * To stop the visiting if the parent field is no form field anymore (e.g. a form or the desktop), you can set {@link VisitParentFieldsOptions.limitToSameFieldTree} to true.
   */
  visitParentFields(visitor: (parent: FormField) => void, options: VisitParentFieldsOptions = {}) {
    let parent = this.parent;
    while (parent) {
      if (parent instanceof FormField) {
        visitor(parent);
      } else if (scout.nvl(options.limitToSameFieldTree, false)) {
        return;
      }
      parent = parent.parent;
    }
  }

  /**
   * Sets {@link saveNeeded} and {@link touched} to false on this field and every child field.
   **/
  markAsSaved() {
    this.visitFirstChildFields(field => {
      field.markAsSaved();
    });
    this._markAsSaved();
    this.updateSaveNeeded();
  }

  protected _markAsSaved() {
    this.setProperty('touched', false);
  }

  /**
   * Marks the field as {@link touched} which means {@link saveNeeded} will return true even if the value has not been changed.
   **/
  touch() {
    this.setProperty('touched', true);
    this.updateSaveNeeded();
  }

  /**
   * Updates {@link saveNeeded} depending on whether the field was {@link touched} using {@link touch} or {@link computeSaveNeeded} returns true.
   *
   * @param child the child updating its save needed state and informing its parent about it. If passed and `child.saveNeeded` is true, {@link computeSaveNeeded} will be skipped.
   */
  updateSaveNeeded(child?: FormField) {
    if (!this.initialized || this.destroying) {
      return;
    }
    this._setSaveNeeded(this.touched || (this.checkSaveNeeded && (child?.saveNeeded || this.computeSaveNeeded())));
  }

  protected _setSaveNeeded(saveNeeded: boolean) {
    if (this._setProperty('saveNeeded', saveNeeded)) {
      this.getParentField()?.updateSaveNeeded(this);
    }
  }

  setCheckSaveNeeded(checkSaveNeeded: boolean) {
    if (this.setProperty('checkSaveNeeded', checkSaveNeeded)) {
      this.updateSaveNeeded();
    }
  }

  /**
   * Used by {@link updateSaveNeeded} to update the {@link saveNeeded} property.
   *
   * By default, all first level child fields are checked. The method returns true, if one of these fields needs to be saved.
   */
  computeSaveNeeded(): boolean {
    let saveNeeded = false;
    this.visitFirstChildFields(field => {
      if (!field.destroying && field.saveNeeded) {
        saveNeeded = true;
        return true;
      }
    });
    return saveNeeded;
  }

  getValidationResult(): ValidationResult {
    return this.validationResultProvider.provide(this._errorStatus());
  }

  protected _createValidationResultProvider() {
    return scout.create(FormFieldValidationResultProvider, {field: this});
  }

  /** @see FormFieldModel.validationResultProvider */
  setValidationResultProvider(provider: FormFieldValidationResultProvider | ObjectType<FormFieldValidationResultProvider>) {
    this.setProperty('validationResultProvider', provider);
  }

  protected _setValidationResultProvider(provider: FormFieldValidationResultProvider | ObjectType<FormFieldValidationResultProvider>) {
    scout.assertParameter('provider', provider);
    if (typeof provider === 'string' || typeof provider === 'function') {
      provider = scout.create(provider, {field: this});
    }
    this._setProperty('validationResultProvider', provider);
  }

  protected _updateEmpty() {
    // NOP
  }

  requestInput() {
    if (this.enabledComputed && this.rendered) {
      this.focus();
    }
  }

  override clone(model: FormFieldModel, options?: CloneOptions): this {
    let clone = super.clone(model, options);
    this._deepCloneProperties(clone, 'menus', options);
    return clone;
  }

  exportToClipboard() {
    if (!this.displayText) {
      return;
    }
    let event = this.trigger('clipboardExport', {
      text: this.displayText
    }) as FormFieldClipboardExportEvent;
    if (!event.defaultPrevented) {
      this._exportToClipboard(event.text);
    }
  }

  protected _exportToClipboard(text: string) {
    clipboard.copyText({
      parent: this,
      text: text
    });
  }

  /**
   * Updates save needed state on parent field if the hierarchy changed, e.g. if the field was moved into another composite field.
   * Also handles the case where a field is not directly connected to a form field parent, but has a non-form-field in between
   * (e.g. FormFieldMenu has a Menu as parent -> if the menu is moved the state needs to be recomputed as well).
   */
  protected _watchFieldHierarchy(parent?: Widget) {
    if (!this.initialized) {
      this.on('hierarchyChange', this._hierarchyChangeHandler);
    }
    parent = scout.nvl(parent, this.parent);
    // Each form field adds its own hierarchyChangeListener but non-form-fields don't -> add listener for every non-form field between this field and the next parent field
    let parentField = this._visitParentsUntilField(parent, parent => parent.on('hierarchyChange', this._hierarchyChangeHandler));
    parentField?.updateSaveNeeded(this);
  }

  protected _unwatchFieldHierarchy(oldParent?: Widget) {
    oldParent = scout.nvl(oldParent, this.parent);
    let oldParentField = this._visitParentsUntilField(oldParent, parent => parent.off('hierarchyChange', this._hierarchyChangeHandler));
    oldParentField?.updateSaveNeeded();
  }

  protected _onHierarchyChange(event: HierarchyChangeEvent) {
    this._unwatchFieldHierarchy(event.oldParent);
    this._watchFieldHierarchy(event.parent);
  }

  protected _visitParentsUntilField(parent: Widget, visitor: (parent: Widget) => void): FormField {
    while (parent) {
      if (parent instanceof FormField) {
        return parent;
      }
      visitor(parent);
      parent = parent.parent;
    }
  }
}

export type FormFieldStyle = EnumObject<typeof FormField.FieldStyle>;
export type FormFieldSuppressStatus = EnumObject<typeof FormField.SuppressStatus>;
export type FormFieldStatusPosition = EnumObject<typeof FormField.StatusPosition>;
export type FormFieldLabelPosition = EnumObject<typeof FormField.LabelPosition>;
export type FormFieldTooltipAnchor = EnumObject<typeof FormField.TooltipAnchor>;
export type FormFieldLabelWidth = EnumObject<typeof FormField.LabelWidth>;
export type FormFieldMode = EnumObject<typeof FormField.Mode>;
export type FormFieldAlignmentUpdateOptions = {
  /**
   * When this option is true, "halign-" classes are added according to gridData.horizontalAlignment. Default is true.
   */
  useHorizontalAlignment?: boolean;

  /**
   * When this option is true, "valign-" classes are added according to gridData.verticalAlignment. Default is true.
   */
  useVerticalAlignment?: boolean;

  /**
   * Specifies the div where the classes should be added. If omitted, this.$fieldContainer is used.
   */
  $fieldContainer?: JQuery;
};
export type ValidationResult = {
  valid: boolean;
  validByMandatory: boolean;
  errorStatus?: Status;
  field: FormField;
  label: string;
  reveal: () => void;
  visitResult?: TreeVisitResult;
};
export type AddCellEditorFieldCssClassesOptions = { cssClass?: string };

export interface VisitParentFieldsOptions {
  /**
   * If set to true, visiting stops if a parent is not a form field.
   *
   * For example: if a group box has a {@link FormFieldMenu}, which is a menu containing a form field, the group box containing the menu won't be visited when the visiting starts at the form field.
   *
   * Default is false.
   */
  limitToSameFieldTree?: boolean;
}

export interface VisitFieldsOptions {
  /**
   * If set to true, visiting the subtree of a child is skipped if the child is a form field. This means only the form fields are considered that are nearest to the field where the visiting started.
   *
   * For example: if a group box has a form field and a menu with a form field, both fields would be visited but the children of these fields would not.
   *
   * Default is false.
   */
  firstLevelFieldsOnly?: boolean;
  /**
   * If set to true, visiting of a child and its subtree is skipped if that child is not a form field, even if it would contain form fields itself.
   *
   * For example: if a group box has a {@link FormFieldMenu}, which is a menu containing a form field, the form field inside the menu won't be visited after visiting the group box.
   *
   * Default is false.
   */
  limitToSameFieldTree?: boolean;
  /**
   * If set to true, the field that started the visiting will be visited as well, otherwise only the children will be visited.
   *
   * Default is true.
   */
  visitSelf?: boolean;
}
