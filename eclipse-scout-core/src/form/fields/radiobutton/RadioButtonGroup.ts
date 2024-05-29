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
  aria, arrays, CloneOptions, FormField, HorizontalGrid, HtmlComponent, InitModelOf, LoadingSupport, LogicalGrid, LogicalGridData, LogicalGridLayout, LogicalGridLayoutConfig, LookupCall, LookupCallOrModel, LookupResult, LookupRow,
  ObjectOrChildModel, ObjectOrModel, objects, ObjectUuidProvider, PropertyChangeEvent, RadioButton, RadioButtonGroupEventMap, RadioButtonGroupGridConfig, RadioButtonGroupLeftOrUpKeyStroke, RadioButtonGroupModel,
  RadioButtonGroupRightOrDownKeyStroke, scout, Status, ValueField
} from '../../../index';
import $ from 'jquery';

export class RadioButtonGroup<TValue> extends ValueField<TValue> implements RadioButtonGroupModel<TValue> {
  declare model: RadioButtonGroupModel<TValue>;
  declare eventMap: RadioButtonGroupEventMap<TValue>;
  declare self: RadioButtonGroup<any>;

  layoutConfig: LogicalGridLayoutConfig;
  fields: FormField[];
  radioButtons: RadioButton<TValue>[];
  gridColumnCount: number;
  selectedButton: RadioButton<TValue>;
  lookupStatus: Status;
  lookupCall: LookupCall<TValue>;
  htmlBody: HtmlComponent;
  $body: JQuery;
  protected _lookupExecuted: boolean;
  protected _selectButtonLocked: boolean;
  protected _lookupInProgress: boolean;
  protected _currentLookupCall: LookupCall<TValue>;
  protected _buttonPropertyChangeHandler: (event: PropertyChangeEvent<any, RadioButton<TValue>>) => void;

  constructor() {
    super();
    this.logicalGrid = scout.create(HorizontalGrid);
    this.layoutConfig = null;
    this.fields = [];
    this.radioButtons = [];
    this.gridColumnCount = RadioButtonGroup.DEFAULT_GRID_COLUMN_COUNT;
    this.selectedButton = null;
    this.$body = null;
    this.lookupStatus = null;
    this.lookupCall = null;

    this._lookupExecuted = false;
    this._selectButtonLocked = false;
    this._lookupInProgress = false;
    this._currentLookupCall = null;

    this._addWidgetProperties(['fields']);
    this._addCloneProperties(['lookupCall', 'layoutConfig', 'gridColumnCount']);
    this._buttonPropertyChangeHandler = this._onButtonPropertyChange.bind(this);
  }

  static DEFAULT_GRID_COLUMN_COUNT = -1;

  static ErrorCode = {
    NO_DATA: 1
  } as const;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this._setLayoutConfig(this.layoutConfig);
    this._setGridColumnCount(this.gridColumnCount);
  }

  protected override _initValue(value: TValue) {
    if (this.lookupCall) {
      this._setLookupCall(this.lookupCall);
    }
    // must be called before value is set
    this._setFields(this.fields);
    super._initValue(value);
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStrokes([
      new RadioButtonGroupLeftOrUpKeyStroke(this),
      new RadioButtonGroupRightOrDownKeyStroke(this)
    ]);
  }

  protected _initButtons() {
    this.radioButtons = this.fields.filter(formField => formField instanceof RadioButton) as RadioButton<TValue>[];
    this.radioButtons.forEach(this._initButton.bind(this));
  }

  protected _initButton(button: RadioButton<TValue>) {
    if (button.events.count('propertyChange', this._buttonPropertyChangeHandler) === 0) {
      button.on('propertyChange', this._buttonPropertyChangeHandler);
    }
    if (button.selected) {
      this.setValue(button.radioValue);
      this.selectButton(button);
      if (button.focused) {
        this.setFocused(true);
      }
    }
  }

  protected override _render() {
    this.addContainer(this.$parent, 'radiobutton-group');
    this.addLabel();
    this.addMandatoryIndicator();

    this.$body = this.$container.appendDiv('radiobutton-group-body');
    aria.role(this.$body, 'radiogroup');
    this.htmlBody = HtmlComponent.install(this.$body, this.session);
    this.htmlBody.setLayout(this._createBodyLayout());

    // fields are rendered in _renderFields
    this.addField(this.$body);
    this.addStatus();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderFields();
    this._renderLayoutConfig();
  }

  protected _createBodyLayout(): LogicalGridLayout {
    return new LogicalGridLayout(this, this.layoutConfig);
  }

  protected override _setLogicalGrid(logicalGrid: LogicalGrid | string) {
    super._setLogicalGrid(logicalGrid);
    if (this.logicalGrid) {
      this.logicalGrid.setGridConfig(new RadioButtonGroupGridConfig());
    }
  }

  override invalidateLogicalGrid(invalidateLayout?: boolean) {
    super.invalidateLogicalGrid(false);
    if (scout.nvl(invalidateLayout, true) && this.rendered) {
      this.htmlBody.invalidateLayoutTree();
    }
  }

  setLayoutConfig(layoutConfig: ObjectOrModel<LogicalGridLayoutConfig>) {
    this.setProperty('layoutConfig', layoutConfig);
  }

  protected _setLayoutConfig(layoutConfig: ObjectOrModel<LogicalGridLayoutConfig>) {
    this._setProperty('layoutConfig', LogicalGridLayoutConfig.ensure(layoutConfig || {}).withSmallHgapDefaults());
    LogicalGridLayoutConfig.initHtmlEnvChangeHandler(this, () => this.layoutConfig, layoutConfig => this.setLayoutConfig(layoutConfig));
  }

  protected _renderLayoutConfig() {
    this.layoutConfig.applyToLayout(this.htmlBody.layout as LogicalGridLayout);
    if (this.rendered) {
      this.htmlBody.invalidateLayoutTree();
    }
  }

  override isClearable(): boolean {
    return false;
  }

  getFields(): FormField[] {
    return this.fields;
  }

  override getFocusableElement(): HTMLElement | JQuery {
    // The first button may not be focusable because it is not selected and therefore has no tab index -> find the first focusable button
    return this.session.focusManager.findFirstFocusableElement(this.$container);
  }

  setFields(fields: ObjectOrChildModel<FormField>[]) {
    this.setProperty('fields', fields);
  }

  protected _setFields(fields: FormField[]) {
    this._setProperty('fields', fields);
    this._initButtons();
  }

  protected _renderFields() {
    this._ensureLookupCallExecuted();
    this.fields.forEach(function(formField) {
      formField.render(this.$body);

      // set each children layout data to logical grid data
      formField.setLayoutData(new LogicalGridData(formField));

      this._linkWithLabel(formField.$field);
    }, this);
    this._provideTabIndex(); // depends on rendered fields
    this.invalidateLogicalGrid();
  }

  protected override _renderEnabled() {
    super._renderEnabled();
    this._provideTabIndex();
  }

  /**
   * Set the selected (or first if none is selected) to tabbable
   */
  protected _provideTabIndex() {
    let tabSet;
    this.radioButtons.forEach(function(radioButton) {
      if (radioButton.enabledComputed && this.enabledComputed && !tabSet) {
        radioButton.setTabbable(true);
        tabSet = radioButton;
      } else if (tabSet && this.enabledComputed && radioButton.enabledComputed && radioButton.selected) {
        tabSet.setTabbable(false);
        radioButton.setTabbable(true);
        tabSet = radioButton;
      } else {
        radioButton.setTabbable(false);
      }
    }, this);
  }

  setGridColumnCount(gridColumnCount: number) {
    this.setProperty('gridColumnCount', gridColumnCount);
  }

  protected _setGridColumnCount(gridColumnCount: number): boolean {
    if (gridColumnCount < 0) {
      gridColumnCount = this._calcDefaultGridColumnCount();
    }
    if (gridColumnCount === this.gridColumnCount) {
      return false;
    }

    this._setProperty('gridColumnCount', gridColumnCount);
    this.invalidateLogicalGrid();
    return true;
  }

  protected _calcDefaultGridColumnCount(): number {
    let height = 1,
      hints = this.gridDataHints;
    if (hints && hints.h > 1) {
      height = hints.h;
    }
    return Math.ceil(this.fields.length / height);
  }

  getButtonForRadioValue(radioValue: TValue): RadioButton<TValue> {
    if (radioValue === null) {
      return null;
    }
    return arrays.find(this.radioButtons, button => {
      return objects.equals(button.radioValue, radioValue);
    });
  }

  /**
   * Search and then select the button with the corresponding radioValue
   */
  protected override _validateValue(value: TValue): TValue {
    super._validateValue(value);

    if (!this.initialized && this.lookupCall) {
      // lookup call may not be started during field initialization. otherwise lookup prepare listeners cannot be attached.
      // do not validate now (as there are no buttons yet, because the lookup call has not yet been executed).
      // validation will be done later again when the lookup call is executed.
      return value;
    }

    let lookupScheduled = this._ensureLookupCallExecuted();
    if (lookupScheduled) {
      // the first lookup was scheduled now: buttons are not yet available, not possible to select one. will be done later as soon as the lookup call is finished.
      return value;
    }

    // only show error if value is not null or undefined
    let buttonToSelect = this.getButtonForRadioValue(value);
    if (!buttonToSelect && value !== null && value !== undefined && !this._lookupInProgress) {
      throw this.session.text('InvalidValueMessageX', value);
    }
    return value;
  }

  protected override _valueChanged() {
    super._valueChanged();
    // Don't select button during initialization if value is null to not override selected state of a button
    if (this.value !== null || this.initialized) {
      this.selectButton(this.getButtonForRadioValue(this.value));
    }
  }

  selectFirstButton() {
    this.selectButtonByIndex(0);
  }

  selectLastButton() {
    this.selectButtonByIndex(this.radioButtons.length - 1);
  }

  selectButtonByIndex(index: number) {
    if (this.radioButtons.length && index >= 0 && index < this.radioButtons.length) {
      this.selectButton(this.radioButtons[index]);
    }
  }

  selectButton(radioButton: RadioButton<TValue>) {
    if (this.selectedButton === radioButton) {
      // Already selected
      return;
    }
    if (this._selectButtonLocked) {
      // Don't execute when triggered by this function to make sure the states are updated before firing the selectButton event
      return;
    }
    this._selectButtonLocked = true;

    // Deselect previously selected button
    if (this.selectedButton) {
      // Do not unset tabbable here, because at least one button has to be tabbable even if the button is deselected
      this.selectedButton.setSelected(false);
    }

    // Select new button
    if (radioButton) {
      let tabbableButton = this.getTabbableButton();
      let needsFocus = false;
      if (tabbableButton) {
        // Only one button in the group should have a tab index -> remove it from the current tabbable button after the new button is tabbable.
        // If that button is focused the newly selected button needs to gain the focus otherwise the focus would fall back to the body.
        needsFocus = tabbableButton.isFocused();
      }
      radioButton.setSelected(true);
      radioButton.setTabbable(true);
      if (needsFocus) {
        radioButton.focus();
      }
      if (tabbableButton && tabbableButton !== radioButton) {
        tabbableButton.setTabbable(false);
      }
    }
    this._selectButtonLocked = false;
    this.setProperty('selectedButton', radioButton);
  }

  getTabbableButton(): RadioButton<TValue> {
    return arrays.find(this.radioButtons, button => {
      return button.visible && button.isTabbable();
    });
  }

  insertButton(radioButton: RadioButton<TValue>) {
    let newFields = this.fields.slice();
    newFields.push(radioButton);
    this.setFields(newFields);
  }

  protected _onButtonPropertyChange(event: PropertyChangeEvent<any, RadioButton<TValue>>) {
    if (event.propertyName === 'selected') {
      let selected = event.newValue;
      if (selected) {
        this.setValue(event.source.radioValue);
        this.selectButton(event.source);
      } else if (event.source === this.selectedButton) {
        this.selectButton(null);
      }
    } else if (event.propertyName === 'focused') {
      this.setFocused(event.newValue);
    }
  }

  setLookupCall(lookupCall: LookupCallOrModel<TValue>) {
    this.setProperty('lookupCall', lookupCall);
  }

  protected _setLookupCall(lookupCall: LookupCallOrModel<TValue>) {
    this._setProperty('lookupCall', LookupCall.ensure(lookupCall, this.session));
    this._lookupExecuted = false;
    if (this.rendered) {
      this._ensureLookupCallExecuted();
    }
  }

  /**
   * @returns true if a lookup call execution has been scheduled now. false otherwise.
   */
  protected _ensureLookupCallExecuted(): boolean {
    if (!this.lookupCall) {
      return false;
    }
    if (this._lookupExecuted) {
      return false;
    }
    this._lookupByAll();
    return true;
  }

  protected override _createLoadingSupport(): LoadingSupport {
    return new LoadingSupport({
      widget: this,
      $container: () => this.$body
    });
  }

  protected _lookupByAll(): JQuery.Promise<LookupResult<TValue>> {
    if (!this.lookupCall) {
      return;
    }

    let deferred = $.Deferred();
    this._executeLookup(this.lookupCall.cloneForAll(), true)
      .done(result => {
        this._lookupByAllDone(result);
        deferred.resolve(result);
      });

    return deferred.promise();
  }

  /**
   * A wrapper function around lookup calls used to set the _lookupInProgress flag, and display the state in the UI.
   */
  protected _executeLookup(lookupCall: LookupCall<TValue>, abortExisting: boolean): JQuery.Promise<LookupResult<TValue>> {
    if (abortExisting && this._currentLookupCall) {
      this._currentLookupCall.abort();
    }

    this._lookupInProgress = true;
    this.setLoading(true);

    this._currentLookupCall = lookupCall;
    this.trigger('prepareLookupCall', {
      lookupCall: lookupCall
    });

    return lookupCall
      .execute()
      .always(() => {
        this._lookupInProgress = false;
        this._lookupExecuted = true;
        this._currentLookupCall = null;
        this.setLoading(false);
        this._clearLookupStatus();
      });
  }

  protected _lookupByAllDone(result: LookupResult<TValue>) {
    try {
      if (result.exception) {
        // Oops! Something went wrong while the lookup has been processed.
        this.setLookupStatus(Status.warning({
          message: result.exception
        }));
        return;
      }
      this._populateRadioButtonGroup(result);
    } finally {
      this.trigger('lookupCallDone', {
        result: result
      });
    }
  }

  protected _populateRadioButtonGroup(result: LookupResult<TValue>) {
    let lookupRows = result.lookupRows;
    let newFields = this.fields.slice();
    lookupRows.forEach(lookupRow => {
      newFields.push(this._createLookupRowRadioButton(lookupRow));
    });
    this.setFields(newFields);

    // because the lookup call is asynchronous, reset the value so that it is revalidated.
    this.setValue(this.value);
    // also select the button (the line above does not change the value, therefore _valueChanged is not called)
    this.selectButton(this.getButtonForRadioValue(this.value));
  }

  protected _clearLookupStatus() {
    this.setLookupStatus(null);
  }

  setLookupStatus(lookupStatus: Status) {
    this.setProperty('lookupStatus', lookupStatus);
    if (this.rendered) {
      this._renderErrorStatus();
    }
  }

  protected override _errorStatus(): Status {
    return this.lookupStatus || this.errorStatus;
  }

  protected _createLookupRowRadioButton(lookupRow: LookupRow<TValue>): RadioButton<TValue> {
    let button: InitModelOf<RadioButton<TValue>> = {
      parent: this,
      label: lookupRow.text,
      radioValue: lookupRow.key
    };

    if (lookupRow.iconId) {
      button.iconId = lookupRow.iconId;
    }
    if (lookupRow.tooltipText) {
      button.tooltipText = lookupRow.tooltipText;
    }
    if (lookupRow.backgroundColor) {
      button.backgroundColor = lookupRow.backgroundColor;
    }
    if (lookupRow.foregroundColor) {
      button.foregroundColor = lookupRow.foregroundColor;
    }
    if (lookupRow.font) {
      button.font = lookupRow.font;
    }
    if (lookupRow.enabled === false) {
      button.enabled = false;
    }
    if (lookupRow.active === false) {
      button.visible = false;
    }
    if (lookupRow.cssClass) {
      button.cssClass = lookupRow.cssClass;
    }

    let radioButton = scout.create((RadioButton<TValue>), button);
    radioButton.lookupRow = lookupRow;
    return radioButton;
  }

  override clone(model: RadioButtonGroupModel<TValue>, options?: CloneOptions): this {
    let clone = super.clone(model, options);
    this._deepCloneProperties(clone, 'fields', options);
    clone._initButtons();
    return clone;
  }
}

ObjectUuidProvider.UuidPathSkipWidgets.add(RadioButtonGroup);
