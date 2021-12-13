/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {scout, TouchPopup} from '../../../index';

/**
 * Info: this class must have the same interface as SmartFieldPopup. That's why there's some
 * copy/pasted code here, because we don't have multi inheritance.
 */
export default class SmartFieldTouchPopup extends TouchPopup {

  constructor() {
    super();
  }

  _init(options) {
    options.withFocusContext = false;
    options.smartField = options.parent; // alias for parent (required by proposal chooser)
    super._init(options);

    this.setLookupResult(options.lookupResult);
    this.setStatus(options.status);
    this.one('close', this._beforeClosePopup.bind(this));
    this.smartField.on('propertyChange', this._onPropertyChange.bind(this));
    this.addCssClass('smart-field-touch-popup');
  }

  _initWidget(options) {
    this._widget = this._createProposalChooser();
    this._widget.on('lookupRowSelected', this._triggerEvent.bind(this));
    this._widget.on('activeFilterSelected', this._triggerEvent.bind(this));
  }

  _createProposalChooser() {
    let objectType = this.parent.browseHierarchy ? 'TreeProposalChooser' : 'TableProposalChooser';
    return scout.create(objectType, {
      parent: this,
      touch: true,
      smartField: this._field
    });
  }

  _fieldOverrides() {
    let obj = super._fieldOverrides();
    // Make sure proposal chooser does not get cloned, because it would not work (e.g. because selectedRows may not be cloned)
    // It would also generate a loop because field would try to render the chooser and the popup
    // -> The original smart field has to control the chooser
    obj.proposalChooser = null;
    return obj;
  }

  _onMouseDownOutside() {
    this._acceptInput(); // see: #_beforeClosePopup()
  }

  /**
   * Delegates the key event to the proposal chooser.
   */
  delegateKeyEvent(event) {
    event.originalEvent.smartFieldEvent = true;
    this._widget.delegateKeyEvent(event);
  }

  getSelectedLookupRow() {
    return this._widget.getSelectedLookupRow();
  }

  _triggerEvent(event) {
    this.trigger(event.type, event);
  }

  setLookupResult(result) {
    this._widget.setLookupResult(result);
  }

  setStatus(status) {
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

  _onPropertyChange(event) {
    if ('lookupStatus' === event.propertyName) {
      this._field.setLookupStatus(event.newValue);
    }
  }

  _beforeClosePopup(event) {
    this.smartField.acceptInputFromField(this._field);
  }
}
