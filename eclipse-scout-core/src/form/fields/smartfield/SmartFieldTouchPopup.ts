/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Event, LookupRow, PropertyChangeEvent, ProposalChooser, scout, ScoutKeyboardEvent, SmartField, SmartFieldModel, SmartFieldTouchPopupEventMap, SmartFieldTouchPopupModel, TouchPopup} from '../../../index';
import {ProposalChooserActiveFilterSelectedEvent, ProposalChooserLookupRowSelectedEvent} from './ProposalChooserEventMap';
import {SmartFieldLookupResult} from './SmartField';
import {StatusOrModel} from '../../../status/Status';

/**
 * Info: this class must have the same interface as SmartFieldPopup. That's why there's some
 * copy/pasted code here, because we don't have multi inheritance.
 */
export default class SmartFieldTouchPopup<TValue> extends TouchPopup implements SmartFieldTouchPopupModel<TValue> {
  declare model: SmartFieldTouchPopupModel<TValue>;
  declare eventMap: SmartFieldTouchPopupEventMap<TValue>;
  declare _field: SmartField<TValue>;
  declare protected _widget: ProposalChooser<TValue, any, any>;

  field: SmartField<TValue>;
  lookupResult: SmartFieldLookupResult<TValue>;
  smartField: SmartField<TValue>;

  constructor() {
    super();
  }

  protected override _init(options: SmartFieldTouchPopupModel<TValue>) {
    options.withFocusContext = false;
    options.smartField = options.parent as SmartField<TValue>; // alias for parent (required by proposal chooser)
    super._init(options);

    this.setLookupResult(options.lookupResult);
    this.setStatus(options.status);
    this.one('close', this._beforeClosePopup.bind(this));
    this.smartField.on('propertyChange', this._onPropertyChange.bind(this));
    this.addCssClass('smart-field-touch-popup');
  }

  protected override _initWidget(options: SmartFieldTouchPopupModel<TValue>) {
    this._widget = this._createProposalChooser();
    this._widget.on('lookupRowSelected', this._triggerEvent.bind(this));
    this._widget.on('activeFilterSelected', this._triggerEvent.bind(this));
  }

  protected _createProposalChooser(): ProposalChooser<TValue, any, any> {
    let objectType = (this.parent as SmartField<TValue>).browseHierarchy ? 'TreeProposalChooser' : 'TableProposalChooser';
    return scout.create(objectType, {
      parent: this,
      touch: true,
      smartField: this._field
    });
  }

  protected override _fieldOverrides(): SmartFieldModel<TValue> {
    let obj = super._fieldOverrides() as SmartFieldModel<TValue> & {proposalChooser: ProposalChooser<any, any, any>};
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
    this.smartField.acceptInputFromField(this._field);
  }
}
