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
  Event, InitModelOf, LookupRow, objects, PropertyChangeEvent, ProposalChooser, ProposalChooserActiveFilterSelectedEvent, ProposalChooserLookupRowSelectedEvent, scout, ScoutKeyboardEvent, SmartField, SmartFieldLookupResult,
  SmartFieldTouchPopupEventMap, SmartFieldTouchPopupModel, Status, StatusOrModel, TouchPopup
} from '../../../index';
import $ from 'jquery';

/**
 * Info: this class must have the same interface as SmartFieldPopup. That's why there's some
 * copy/pasted code here, because we don't have multi inheritance.
 */
export class SmartFieldTouchPopup<TValue> extends TouchPopup implements SmartFieldTouchPopupModel<TValue> {
  declare model: SmartFieldTouchPopupModel<TValue>;
  declare eventMap: SmartFieldTouchPopupEventMap<TValue>;
  declare self: SmartFieldTouchPopup<any>;
  declare _field: SmartField<TValue>;
  declare protected _widget: ProposalChooser<TValue, any, any>;

  field: SmartField<TValue>;
  lookupResult: SmartFieldLookupResult<TValue>;
  smartField: SmartField<TValue>;

  protected _initialFieldState: SmartFieldTouchPopupInitialFieldState<TValue>;

  constructor() {
    super();
    this._initialFieldState = null;
  }

  protected override _init(options: InitModelOf<this>) {
    options.withFocusContext = false;
    options.smartField = options.parent as SmartField<TValue>; // alias for parent (required by proposal chooser)
    super._init(options);
    this._initialFieldState = this._getFieldState();

    this.setLookupResult(options.lookupResult);
    this.setStatus(options.status);
    this.one('close', this._beforeClosePopup.bind(this));
    this.smartField.on('propertyChange', this._onPropertyChange.bind(this));
    this.addCssClass('smart-field-touch-popup');
  }

  protected override _initDelegatedEvents(): string[] {
    return ['prepareLookupCall', 'lookupCallDone'];
  }

  protected override _initWidget(options: SmartFieldTouchPopupModel<TValue>) {
    this._widget = this._createProposalChooser();
    this._widget.on('lookupRowSelected', this._triggerEvent.bind(this));
    this._widget.on('activeFilterSelected', this._triggerEvent.bind(this));
  }

  protected _getFieldState(): SmartFieldTouchPopupInitialFieldState<TValue> {
    const {value, displayText, errorStatus, lookupRow} = this._field;
    return $.extend(true, {}, {value, displayText, errorStatus, lookupRow});
  }

  protected _createProposalChooser(): ProposalChooser<TValue, any, any> {
    let objectType = (this.parent as SmartField<TValue>).browseHierarchy ? 'TreeProposalChooser' : 'TableProposalChooser';
    return scout.create(objectType, {
      parent: this,
      touch: true,
      smartField: this._field
    });
  }

  protected override _fieldOverrides(): InitModelOf<SmartField<TValue>> {
    let obj = super._fieldOverrides() as InitModelOf<SmartField<TValue>>;
    // Make sure proposal chooser does not get cloned, because it would not work (e.g. because selectedRows may not be cloned)
    // It would also generate a loop because field would try to render the chooser and the popup
    // -> The original smart field has to control the chooser
    obj.proposalChooser = null;
    return obj;
  }

  protected override _onMouseDownOutside() {
    this._acceptInput(); // see: #_beforeClosePopup()
  }

  /**
   * Delegates the key event to the proposal chooser.
   */
  delegateKeyEvent(event: ScoutKeyboardEvent & JQuery.Event) {
    event.originalEvent.smartFieldEvent = true;
    this._widget.delegateKeyEvent(event);
  }

  getSelectedLookupRow(): LookupRow<TValue> {
    return this._widget.getSelectedLookupRow();
  }

  protected _triggerEvent(event: ProposalChooserActiveFilterSelectedEvent<TValue> | ProposalChooserLookupRowSelectedEvent<TValue>) {
    this.trigger(event.type, event);
  }

  setLookupResult(result: SmartFieldLookupResult<TValue>) {
    this._widget.setLookupResult(result);
  }

  setStatus(status: StatusOrModel) {
    this._widget.setStatus(status);
  }

  clearLookupRows() {
    this._widget.clearLookupRows();
  }

  selectFirstLookupRow() {
    this._widget.selectFirstLookupRow();
  }

  selectLookupRow() {
    this._widget.triggerLookupRowSelected();
  }

  protected _onPropertyChange(event: PropertyChangeEvent<any, SmartField<TValue>>) {
    if ('lookupStatus' === event.propertyName) {
      this._field.setLookupStatus(event.newValue);
    }
  }

  protected _beforeClosePopup(event: Event<SmartFieldTouchPopup<TValue>>) {
    if (objects.equalsRecursive(this._initialFieldState, this._getFieldState())) {
      return;
    }
    this.smartField.acceptInputFromField(this._field);
  }
}

export type SmartFieldTouchPopupInitialFieldState<TValue> = {
  value: TValue;
  displayText: string;
  errorStatus: Status;
  lookupRow: LookupRow<TValue>;
};
