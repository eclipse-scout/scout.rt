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
import {Device, FormField, Popup, scout, SmartFieldPopupLayout} from '../../../index';

export default class SmartFieldPopup extends Popup {

  constructor() {
    super();
    this.animateRemoval = SmartFieldPopup.hasPopupAnimation();
    this.windowPaddingX = 0;
    this.windowPaddingY = 0;
  }

  _init(options) {
    options.withFocusContext = false;
    super._init(options);

    this.smartField = this.parent;
    this.proposalChooser = this._createProposalChooser();
    this.proposalChooser.on('lookupRowSelected', this._triggerEvent.bind(this));
    this.proposalChooser.on('activeFilterSelected', this._triggerEvent.bind(this));
    this.smartField.on('remove', this._onRemoveSmartField.bind(this));

    this.setLookupResult(options.lookupResult);
    this.setStatus(options.status);
  }

  _createProposalChooser() {
    let objectType = this.smartField.browseHierarchy ? 'TreeProposalChooser' : 'TableProposalChooser';
    return scout.create(objectType, {
      parent: this
    });
  }

  _createLayout() {
    return new SmartFieldPopupLayout(this, this.proposalChooser);
  }

  _render() {
    let cssClass = this.smartField.cssClassName() + '-popup';
    super._render();
    this.$container
      .addClass(cssClass)
      .on('mousedown', this._onContainerMouseDown.bind(this));
    this.$container.toggleClass('alternative', this.smartField.fieldStyle === FormField.FieldStyle.ALTERNATIVE);
    this.proposalChooser.render();
  }

  setLookupResult(result) {
    this._setProperty('lookupResult', result);
    this.proposalChooser.setLookupResult(result);
  }

  /**
   * @returns the selected lookup row from the proposal chooser. If the row is disabled this function returns null.
   */
  getSelectedLookupRow() {
    let lookupRow = this.proposalChooser.getSelectedLookupRow();
    if (lookupRow && lookupRow.enabled) {
      return lookupRow;
    }
    return null;
  }

  setStatus(status) {
    this.proposalChooser.setStatus(status);
  }

  selectFirstLookupRow() {
    this.proposalChooser.selectFirstLookupRow();
  }

  selectLookupRow() {
    this.proposalChooser.triggerLookupRowSelected();
  }

  /**
   * Delegates the key event to the proposal chooser.
   */
  delegateKeyEvent(event) {
    event.originalEvent.smartFieldEvent = true;
    this.proposalChooser.delegateKeyEvent(event);
  }

  _triggerEvent(event) {
    this.trigger(event.type, event);
  }

  /**
   * This event handler is called before the mousedown handler on the _document_ is triggered
   * This allows us to prevent the default, which is important for the CellEditorPopup which
   * should stay open when the SmartField popup is closed. It also prevents the focus blur
   * event on the SmartField input-field.
   */
  _onContainerMouseDown(event) {
    // when user clicks on proposal popup with table or tree (prevent default,
    // so input-field does not lose the focus, popup will be closed by the
    // proposal chooser impl.
    return false;
  }

  // when smart-field is removed, also remove popup. Don't animate removal in that case
  _onRemoveSmartField(event) {
    this.animateRemoval = false;
    this.remove();
  }

  /**
   * @override because the icon is not in the $anchor container.
   */
  _isMouseDownOnAnchor(event) {
    return this.field.$field.isOrHas(event.target) || this.field.$icon.isOrHas(event.target) || (this.field.$clearIcon && this.field.$clearIcon.isOrHas(event.target));
  }

  _onAnimationEnd() {
    this.proposalChooser.updateScrollbars();
  }

  // --- static helpers --- //

  static hasPopupAnimation() {
    return Device.get().supportsCssAnimation();
  }
}
