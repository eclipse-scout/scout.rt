/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  arrays, Event, fields, HtmlComponent, InitModelOf, InputFieldKeyStrokeContext, keys, KeyStrokeContext, LookupCall, LookupCallOrModel, LookupResult, MaxLengthHandler, Popup, PropertyChangeEvent, scout, strings, TagBar,
  TagBarTagRemoveEvent, TagChooserPopup, TagChooserPopupLookupRowSelectedEvent, TagFieldContainerLayout, TagFieldDeleteKeyStroke, TagFieldEnterKeyStroke, TagFieldEventMap, TagFieldLayout, TagFieldModel, TagFieldNavigationKeyStroke,
  TagFieldOpenPopupKeyStroke, ValueField
} from '../../../index';

export class TagField extends ValueField<string[]> implements TagFieldModel {
  declare model: TagFieldModel;
  declare eventMap: TagFieldEventMap;
  declare self: TagField;

  lookupCall: LookupCall<string>;
  maxLength: number;
  fieldHtmlComp: HtmlComponent;
  popup: TagChooserPopup;
  tagBar: TagBar;
  maxLengthHandler: MaxLengthHandler;

  /** @internal */
  _currentLookupCall: LookupCall<string>;

  constructor() {
    super();

    this.$field = null;
    this.fieldHtmlComp = null;
    this.popup = null;
    this.lookupCall = null;
    this._currentLookupCall = null;
    this.tagBar = null;
    this.maxLength = 500;
    this.maxLengthHandler = scout.create(MaxLengthHandler, {
      target: this
    });
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this.tagBar = scout.create(TagBar, {
      parent: this,
      tags: this.value,
      clickable: model.clickable
    });
    this.tagBar.on('tagRemove', this._onTagRemove.bind(this));
    this.on('propertyChange', this._onValueChange.bind(this));
    this._setLookupCall(this.lookupCall);
  }

  protected _onTagRemove(event: TagBarTagRemoveEvent) {
    this.removeTag(event.tag);
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    this.keyStrokeContext.registerKeyStrokes([
      new TagFieldEnterKeyStroke(this),
      new TagFieldNavigationKeyStroke(this._createFieldAdapter()),
      new TagFieldDeleteKeyStroke(this._createFieldAdapter()),
      new TagFieldOpenPopupKeyStroke(this)
    ]);
  }

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new InputFieldKeyStrokeContext();
  }

  protected override _render() {
    this.addContainer(this.$parent, 'tag-field', new TagFieldLayout(this));
    this.addLabel();
    this.addMandatoryIndicator();
    let $fieldContainer = this.$container.appendDiv();
    this.fieldHtmlComp = HtmlComponent.install($fieldContainer, this.session);
    this.fieldHtmlComp.setLayout(new TagFieldContainerLayout(this));
    this.tagBar.render($fieldContainer);
    let $field = $fieldContainer.appendElement('<input>', 'field')
      .attr('type', 'text') // So that css rules from main.less are applied
      .on('keydown', this._onInputKeydown.bind(this))
      .on('keyup', this._onInputKeyup.bind(this))
      .on('input', this._onFieldInput.bind(this)) as JQuery<HTMLInputElement>;
    this.addFieldContainer($fieldContainer);
    this.addField($field);
    this.maxLengthHandler.install($field);
    this.addStatus();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderValue();
    this._renderMaxLength();
  }

  protected _renderValue() {
    this.tagBar.updateTags();
  }

  protected override _setValue(value: string[]) {
    super._setValue(value);
    if (this.tagBar) { // required for _init case
      this.tagBar.setTags(this.value /* do not use the function parameter here. instead use the member variable because the value might have changed in a validator. */);
    }
  }

  protected _setLookupCall(lookupCall: LookupCallOrModel<string>) {
    this._setProperty('lookupCall', LookupCall.ensure(lookupCall, this.session));
  }

  override formatValue(value: string[]): string | JQuery.Promise<string> {
    // Info: value and displayText are not related in the TagField
    return '';
  }

  protected override _validateValue(value: string[]): string[] {
    let tags = arrays.ensure(value);
    let result: string[] = [];
    tags.forEach(tag => {
      if (!strings.empty(tag)) {
        tag = tag.toLowerCase();
        if (result.indexOf(tag) < 0) {
          result.push(tag);
        }
      }
    });
    return result;
  }

  protected override _parseValue(displayText: string): string[] {
    let tags = arrays.ensure(this.value);
    tags = tags.slice();
    tags.push(displayText);
    return tags;
  }

  protected override _renderDisplayText() {
    this.$field.val(this.displayText); // needs to be before super call (otherwise updateHasText fails)
    super._renderDisplayText();
    this._updateInputVisible();
  }

  protected override _renderEnabled() {
    super._renderEnabled();
    this._updateInputVisible();
  }

  protected override _renderFieldStyle() {
    super._renderFieldStyle();
    if (this.rendered) {
      this.fieldHtmlComp.invalidateLayoutTree();
    }
  }

  setMaxLength(maxLength: number) {
    this.setProperty('maxLength', maxLength);
  }

  protected _renderMaxLength() {
    this.maxLengthHandler.render();
  }

  protected _updateInputVisible() {
    let visible: boolean, oldVisible = !this.$field.isVisible();
    if (this.enabledComputed) {
      visible = true;
    } else {
      visible = strings.hasText(this.displayText);
    }
    this.$field.setVisible(visible);
    // update tag-elements (must remove X when disabled)
    if (visible !== oldVisible) {
      this._renderValue();
    }
  }

  override _readDisplayText(): string {
    return this.$field.val() as string;
  }

  protected override _clear() {
    this.$field.val('');
  }

  override acceptInput(whileTyping?: boolean) {
    if (this.popup) {
      if (this.popup.selectedRow()) {
        this.popup.triggerLookupRowSelected();
      } else {
        this.closePopup();
      }
      return;
    }
    super.acceptInput(false);
  }

  override _triggerAcceptInput(whileTyping?: boolean) {
    this.trigger('acceptInput', {
      displayText: this.displayText,
      whileTyping: whileTyping,
      value: this.value
    });
  }

  override aboutToBlurByMouseDown(target: Element) {
    if (fields.eventOutsideProposalField(this, target)) {
      this.acceptInput(true);
    }
  }

  protected override _onFieldBlur(event: JQuery.BlurEvent) {
    // We cannot call super until chooser popup has been closed (see #acceptInput)
    this.closePopup();
    super._onFieldBlur(event);
    if (this.rendered && !this.removing) {
      this.tagBar.blur();
    }
  }

  protected override _onFieldFocus(event: JQuery.FocusEvent) {
    super._onFieldFocus(event);
    if (this.rendered && !this.removing) {
      this.tagBar.focus();
    }
  }

  protected _onFieldInput() {
    this._updateHasText();
  }

  addTag(text: string) {
    let value = this._parseValue(text);
    this.setValue(value);
    this._triggerAcceptInput();
  }

  removeTag(tag: string) {
    if (strings.empty(tag)) {
      return;
    }
    tag = tag.toLowerCase();
    let tags = arrays.ensure(this.value);
    if (tags.indexOf(tag) === -1) {
      return;
    }
    tags = tags.slice();
    arrays.remove(tags, tag);
    this.setValue(tags);
    this._triggerAcceptInput();
    // focus was previously on the removed tag, restore focus on the field.
    this.focus();
  }

  protected _onInputKeydown(event: JQuery.KeyDownEvent) {
    if (this._isNavigationKey(event) && this.popup) {
      this.popup.delegateKeyEvent(event);
    } else if (event.which === keys.ESC) {
      this.closePopup();
    }
  }

  protected _isNavigationKey(event: JQuery.KeyboardEventBase): boolean {
    return scout.isOneOf(event.which, [
      keys.PAGE_UP,
      keys.PAGE_DOWN,
      keys.UP,
      keys.DOWN
    ]);
  }

  protected _onInputKeyup(event: JQuery.KeyUpEvent) {
    // Prevent chooser popup from being opened again, after it has been closed by pressing ESC
    if (event.which === keys.ESC) {
      return;
    }
    if (!this._isNavigationKey(event)) {
      this._lookupByText(this.$field.val() as string);
    }
  }

  protected _lookupByText(text: string) {
    if (!this.lookupCall) {
      return;
    }
    if (strings.empty(text) || text.length < 2) {
      this.closePopup();
      return;
    }

    this._currentLookupCall = this.lookupCall.cloneForText(text);
    this.trigger('prepareLookupCall', {
      lookupCall: this._currentLookupCall
    });
    this._currentLookupCall
      .execute()
      .always(() => {
        this._currentLookupCall = null;
      })
      .done(this._onLookupDone.bind(this));
  }

  protected _onLookupDone(result: LookupResult<string>) {
    try {
      if (!this.rendered || !this.isFocused() || result.lookupRows.length === 0) {
        this.closePopup();
        return;
      }

      this.openPopup();
      this.popup.setLookupResult(result);
    } finally {
      this.trigger('lookupCallDone', {
        result: result
      });
    }
  }

  openPopup() {
    if (this.popup) {
      return;
    }
    this.popup = scout.create(TagChooserPopup, {
      parent: this,
      $anchor: this.$field,
      boundToAnchor: true,
      closeOnAnchorMouseDown: false,
      field: this
    });
    this.popup.on('lookupRowSelected', this._onLookupRowSelected.bind(this));
    this.popup.one('close', this._onPopupClose.bind(this));
    this.popup.open();
  }

  closePopup() {
    if (this.popup && !this.popup.destroying) {
      this.popup.close();
    }
  }

  protected _onLookupRowSelected(event: TagChooserPopupLookupRowSelectedEvent) {
    this._clear();
    this._updateHasText();
    this.addTag(event.lookupRow.key);
    this.closePopup();
  }

  protected _onPopupClose(event: Event<Popup>) {
    this.popup = null;
  }

  isInputFocused(): boolean {
    let ae = this.$fieldContainer.activeElement();
    return this.$field.is(ae);
  }

  protected _onValueChange(event: PropertyChangeEvent<any, TagField>) {
    if ('value' === event.propertyName) {
      this._renderLabel();
    }
  }

  protected override _renderPlaceholder($field?: JQuery) {
    // only render placeholder when tag field is empty (has no tags)
    let hasTags = !!arrays.ensure(this.value).length;
    $field = scout.nvl($field, this.$field);
    if ($field) {
      $field.placeholder(hasTags ? '' : this.label);
    }
  }

  protected _createFieldAdapter(): TagFieldKeyStrokeAdapter {
    return TagField.createFieldAdapter(this);
  }

  static createFieldAdapter(field: TagField): TagFieldKeyStrokeAdapter {
    return {
      $container: () => field.$fieldContainer,
      enabled: () => strings.empty(field._readDisplayText()),
      focus: () => field.$field.focus(),
      removeTag: tag => field.removeTag(tag)
    };
  }
}

export interface TagFieldKeyStrokeAdapter {
  $container(): JQuery;

  enabled(): boolean;

  focus();

  removeTag(tag: string);
}
