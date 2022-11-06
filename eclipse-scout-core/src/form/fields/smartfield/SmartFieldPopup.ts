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
import {AbstractLayout, Device, Event, FormField, LookupRow, Popup, ProposalChooser, scout, ScoutKeyboardEvent, SmartField, SmartFieldPopupEventMap, SmartFieldPopupLayout, SmartFieldPopupModel} from '../../../index';
import {ProposalChooserActiveFilterSelectedEvent, ProposalChooserLookupRowSelectedEvent} from './ProposalChooserEventMap';
import {SmartFieldLookupResult} from './SmartField';
import {StatusOrModel} from '../../../status/Status';
import {InitModelOf} from '../../../scout';
import {SomeRequired} from '../../../types';

export default class SmartFieldPopup<TValue> extends Popup implements SmartFieldPopupModel<TValue> {
  declare model: SmartFieldPopupModel<TValue>;
  declare initModel: SomeRequired<this['model'], 'parent' | 'field'>;
  declare eventMap: SmartFieldPopupEventMap<TValue>;
  declare self: SmartFieldPopup<any>;

  field: SmartField<TValue>;
  lookupResult: SmartFieldLookupResult<TValue>;
  smartField: SmartField<TValue>;
  proposalChooser: ProposalChooser<TValue, any, any>;

  constructor() {
    super();
    this.animateRemoval = SmartFieldPopup.hasPopupAnimation();
    this.windowPaddingX = 0;
    this.windowPaddingY = 0;
  }

  protected override _init(options: InitModelOf<this>) {
    options.withFocusContext = false;
    super._init(options);

    this.smartField = this.parent as SmartField<TValue>;
    this.proposalChooser = this._createProposalChooser();
    this.proposalChooser.on('lookupRowSelected', this._triggerEvent.bind(this));
    this.proposalChooser.on('activeFilterSelected', this._triggerEvent.bind(this));
    this.smartField.on('remove', this._onRemoveSmartField.bind(this));

    this.setLookupResult(options.lookupResult);
    this.setStatus(options.status);
  }

  protected _createProposalChooser(): ProposalChooser<TValue, any, any> {
    let objectType = this.smartField.browseHierarchy ? 'TreeProposalChooser' : 'TableProposalChooser';
    return scout.create(objectType, {
      parent: this
    });
  }

  protected override _createLayout(): AbstractLayout {
    return new SmartFieldPopupLayout(this);
  }

  protected override _render() {
    let cssClass = this.smartField.cssClassName() + '-popup';
    super._render();
    this.$container
      .addClass(cssClass)
      .on('mousedown', this._onContainerMouseDown.bind(this));
    this.$container.toggleClass('alternative', this.smartField.fieldStyle === FormField.FieldStyle.ALTERNATIVE);
    this.proposalChooser.render();
  }

  setLookupResult(result: SmartFieldLookupResult<TValue>) {
    this._setProperty('lookupResult', result);
    this.proposalChooser.setLookupResult(result);
  }

  /**
   * @returns the selected lookup row from the proposal chooser. If the row is disabled this function returns null.
   */
  getSelectedLookupRow(): LookupRow<TValue> {
    let lookupRow = this.proposalChooser.getSelectedLookupRow();
    if (lookupRow && lookupRow.enabled) {
      return lookupRow;
    }
    return null;
  }

  setStatus(status: StatusOrModel) {
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
  delegateKeyEvent(event: ScoutKeyboardEvent & JQuery.Event) {
    event.originalEvent.smartFieldEvent = true;
    this.proposalChooser.delegateKeyEvent(event);
  }

  protected _triggerEvent(event: ProposalChooserActiveFilterSelectedEvent<TValue> | ProposalChooserLookupRowSelectedEvent<TValue>) {
    this.trigger(event.type, event);
  }

  /**
   * This event handler is called before the mousedown handler on the _document_ is triggered
   * This allows us to prevent the default, which is important for the CellEditorPopup which
   * should stay open when the SmartField popup is closed. It also prevents the focus blur
   * event on the SmartField input-field.
   */
  protected _onContainerMouseDown(event: JQuery.MouseDownEvent): boolean {
    // when user clicks on proposal popup with table or tree (prevent default,
    // so input-field does not lose the focus, popup will be closed by the
    // proposal chooser impl).
    return false;
  }

  // when smart-field is removed, also remove popup. Don't animate removal in that case
  protected _onRemoveSmartField(event: Event<SmartField<TValue>>) {
    this.animateRemoval = false;
    this.remove();
  }

  protected override _isMouseDownOnAnchor(event: MouseEvent): boolean {
    let target = event.target as HTMLElement;
    return this.field.$field.isOrHas(target)
      || this.field.$icon.isOrHas(target)
      || (this.field.$clearIcon && this.field.$clearIcon.isOrHas(target));
  }

  /** @internal */
  _onAnimationEnd() {
    this.proposalChooser.updateScrollbars();
  }

  // --- static helpers --- //

  static hasPopupAnimation(): boolean {
    return Device.get().supportsCssAnimation();
  }
}
