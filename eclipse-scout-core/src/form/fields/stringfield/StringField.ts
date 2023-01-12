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
  BasicField, DesktopNotification, EnumObject, fields, InitModelOf, InputFieldKeyStrokeContext, MaxLengthHandler, objects, OldWheelEvent, scout, Status, StringFieldCtrlEnterKeyStroke, StringFieldEnterKeyStroke, StringFieldEventMap,
  StringFieldLayout, StringFieldModel, strings, texts
} from '../../../index';

export class StringField extends BasicField<string> {
  declare model: StringFieldModel;
  declare eventMap: StringFieldEventMap;
  declare self: StringField;
  declare keyStrokeContext: InputFieldKeyStrokeContext;
  declare $field: JQuery | JQuery<HTMLInputElement>;

  format: StringFieldFormat;
  hasAction: boolean;
  inputMasked: boolean;
  inputObfuscated: boolean;
  maxLength: number;
  maxLengthHandler: MaxLengthHandler;
  multilineText: boolean;
  selectionStart: number;
  selectionEnd: number;
  selectionTrackingEnabled: boolean;
  spellCheckEnabled: boolean;
  trimText: boolean;
  wrapText: boolean;
  mouseClicked: boolean;
  protected _onSelectionChangingActionHandler: (event: JQuery.TriggeredEvent) => void;

  constructor() {
    super();

    this.format = null;
    this.hasAction = false;
    this.inputMasked = false;
    this.inputObfuscated = false;
    this.maxLength = 4000;
    this.maxLengthHandler = scout.create(MaxLengthHandler, {
      target: this
    });
    this.multilineText = false;
    this.selectionStart = 0;
    this.selectionEnd = 0;
    this.selectionTrackingEnabled = false;
    this.spellCheckEnabled = false;
    this.trimText = true;
    this.wrapText = false;

    this._onSelectionChangingActionHandler = this._onSelectionChangingAction.bind(this);
  }

  static Format = {
    LOWER: 'a' /* IStringField.FORMAT_LOWER */,
    UPPER: 'A' /* IStringField.FORMAT_UPPER */
  } as const;

  static TRIM_REGEXP = new RegExp('^(\\s*)(.*?)(\\s*)$');

  /**
   * Resolves the text key if value contains one.
   * This cannot be done in _init because the value field would call _setValue first
   */
  protected override _initValue(value: string) {
    value = texts.resolveText(value, this.session.locale.languageTag);
    super._initValue(value);
  }

  override _readDisplayText(): string {
    return this.$field ? this.$field.val() as string : '';
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStrokes([
      new StringFieldEnterKeyStroke(this),
      new StringFieldCtrlEnterKeyStroke(this)
    ]);
  }

  protected override _createKeyStrokeContext(): InputFieldKeyStrokeContext {
    return new InputFieldKeyStrokeContext();
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._setMultilineText(this.multilineText);
  }

  protected override _render() {
    this.addContainer(this.$parent, 'string-field', new StringFieldLayout(this));
    this.addLabel();
    this.addMandatoryIndicator();

    let $field;
    if (this.multilineText) {
      $field = this._makeMultilineField();
      this.$container.addClass('multiline');
    } else {
      $field = fields.makeTextField(this.$parent);
    }

    this.addField($field);
    this.maxLengthHandler.install($field);
    this.addStatus();
  }

  protected _makeMultilineField(): JQuery {
    let mouseDownHandler = function() {
      this.mouseClicked = true;
    }.bind(this);

    return this.$parent.makeElement('<textarea>')
      .on('DOMMouseScroll mousewheel', this._onMouseWheel.bind(this))
      .on('mousedown', mouseDownHandler)
      .on('focus', event => {
        (this.$field as JQuery).off('mousedown', mouseDownHandler);
        if (!this.mouseClicked) { // only trigger on tab focus in
          setTimeout(() => {
            if (!this.rendered || this.session.focusManager.isElementCovertByGlassPane(this.$field)) {
              return;
            }
            this._renderSelectionStart();
            this._renderSelectionEnd();
          });
        }
        this.mouseClicked = false;
      })
      .on('focusout', () => {
        this.$field.on('mousedown', mouseDownHandler);
      })
      .addDeviceClass();
  }

  protected override _onFieldBlur(event: JQuery.BlurEvent) {
    super._onFieldBlur(event);
    if (this.multilineText) {
      this._updateSelection();
    }
    if (this.inputObfuscated) {
      // Restore obfuscated display text.
      this.$field.val(this.displayText);
    }
  }

  protected _onMouseWheel(event: JQuery.TriggeredEvent) {
    let originalEvent: OldWheelEvent = event.originalEvent || this.$container.window(true).event['originalEvent'];
    let delta = originalEvent.wheelDelta ? -originalEvent.wheelDelta : originalEvent.detail;
    let scrollTop = this.$field[0].scrollTop;
    if (delta < 0 && scrollTop === 0) {
      // StringField is scrolled to the very top -> parent may scroll
      return;
    }
    let maxScrollTop = this.$field[0].scrollHeight - this.$field[0].clientHeight;
    if (delta > 0 && scrollTop >= maxScrollTop - 1) { // -1 because it can sometimes happen that scrollTop is maxScrollTop -1 or +1, just because clientHeight and scrollHeight are rounded values
      // StringField is scrolled to the very bottom -> parent may scroll
      this.$field[0].scrollTop = maxScrollTop; // Ensure it is really at the bottom (not -1px above)
      return;
    }
    // Don't allow others to scroll (e.g. Scrollbar) while scrolling in the text area
    originalEvent.stopPropagation();
  }

  protected override _renderProperties() {
    super._renderProperties();

    this._renderInputMasked();
    this._renderWrapText();
    this._renderFormat();
    this._renderSpellCheckEnabled();
    this._renderHasAction();
    this._renderMaxLength();
    this._renderSelectionTrackingEnabled();
    // Do not render selectionStart and selectionEnd here, because that would cause the focus to
    // be set to <textarea>s in IE. Instead, the selection is rendered when the focus has entered
    // the field, see _render(). #168648
    this._renderDropType();
  }

  /**
   * Adds a click handler instead of a mouse down handler because it executes an action.
   */
  override addIcon($parent?: JQuery) {
    this.$icon = fields.appendIcon(this.$container)
      .on('click', this._onIconClick.bind(this));
  }

  /**
   * override to ensure dropdown fields and touch mode smart fields does not have a clear icon.
   */
  override isClearable(): boolean {
    return super.isClearable() && !this.multilineText;
  }

  setSelectionStart(selectionStart: number) {
    this.setProperty('selectionStart', selectionStart);
  }

  protected _renderSelectionStart() {
    if (scout.nvl(this.selectionStart, null) !== null) {
      (this.$field[0] as HTMLInputElement).selectionStart = this.selectionStart;
    }
  }

  setSelectionEnd(selectionEnd: number) {
    this.setProperty('selectionEnd', selectionEnd);
  }

  protected _renderSelectionEnd() {
    if (scout.nvl(this.selectionEnd, null) !== null) {
      (this.$field[0] as HTMLInputElement).selectionEnd = this.selectionEnd;
    }
  }

  setSelectionTrackingEnabled(selectionTrackingEnabled: boolean) {
    this.setProperty('selectionTrackingEnabled', selectionTrackingEnabled);
  }

  protected _renderSelectionTrackingEnabled() {
    (this.$field as JQuery)
      .off('select', this._onSelectionChangingActionHandler)
      .off('mousedown', this._onSelectionChangingActionHandler)
      .off('keydown', this._onSelectionChangingActionHandler)
      .off('input', this._onSelectionChangingActionHandler);
    if (this.selectionTrackingEnabled) {
      (this.$field as JQuery)
        .on('select', this._onSelectionChangingActionHandler)
        .on('mousedown', this._onSelectionChangingActionHandler)
        .on('keydown', this._onSelectionChangingActionHandler)
        .on('input', this._onSelectionChangingActionHandler);
    }
  }

  setInputMasked(inputMasked: boolean) {
    this.setProperty('inputMasked', inputMasked);
  }

  protected _renderInputMasked() {
    if (this.multilineText) {
      return;
    }
    this.$field.attr('type', this.inputMasked ? 'password' : 'text');
  }

  protected _renderInputObfuscated() {
    if (this.inputObfuscated && this.focused) {
      // If a new display text is set (e.g. because value in model changed) and field is focused,
      // do not display new display text but clear content (as in _onFieldFocus).
      // Depending on order of property render, either this or _renderDisplayText is called first
      // (inputObfuscated flag might be still in the old state in _renderDisplayText).
      this.$field.val('');
    }
  }

  setHasAction(hasAction: boolean) {
    this.setProperty('hasAction', hasAction);
  }

  protected _renderHasAction() {
    if (this.hasAction) {
      if (!this.$icon) {
        this.addIcon();
        this.$icon.addClass('action');
      }
      this.$container.addClass('has-icon');
    } else {
      this._removeIcon();
      this.$container.removeClass('has-icon');
    }
    this.revalidateLayout();
  }

  protected override _renderEnabled() {
    super._renderEnabled();
    this.revalidateLayout();
  }

  setFormatUpper(formatUpper: boolean) {
    if (formatUpper) {
      this.setFormat(StringField.Format.UPPER);
    } else {
      this.setFormat(null);
    }
  }

  setFormatLower(formatLower: boolean) {
    if (formatLower) {
      this.setFormat(StringField.Format.LOWER);
    } else {
      this.setFormat(null);
    }
  }

  setFormat(format: StringFieldFormat) {
    this.setProperty('format', format);
  }

  protected _renderFormat() {
    if (this.format === StringField.Format.LOWER) {
      this.$field.css('text-transform', 'lowercase');
    } else if (this.format === StringField.Format.UPPER) {
      this.$field.css('text-transform', 'uppercase');
    } else {
      this.$field.css('text-transform', '');
    }
  }

  setSpellCheckEnabled(spellCheckEnabled: boolean) {
    this.setProperty('spellCheckEnabled', spellCheckEnabled);
  }

  protected _renderSpellCheckEnabled() {
    if (this.spellCheckEnabled) {
      this.$field.attr('spellcheck', 'true');
    } else {
      this.$field.attr('spellcheck', 'false');
    }
  }

  protected override _renderDisplayText() {
    if (this.inputObfuscated && this.focused) {
      // If a new display text is set (e.g. because value in model changed) and field is focused,
      // do not display new display text but clear content (as in _onFieldFocus).
      // Depending on order of property render, either this or _renderInputObfuscated is called first
      // (inputObfuscated flag might be still in the old state in this method).
      this.$field.val('');
      return;
    }

    let displayText = strings.nvl(this.displayText);
    let oldDisplayText = strings.nvl(this.$field.val() as string);
    let oldSelection = this._getSelection();
    super._renderDisplayText();
    // Try to keep the current selection for cases where the old and new display
    // text only differ because of the automatic trimming.
    if (this.trimText && oldDisplayText !== displayText) {
      let matches = oldDisplayText.match(StringField.TRIM_REGEXP);
      if (matches && matches[2] === displayText) {
        this._setSelection({
          start: Math.max(oldSelection.start - matches[1].length, 0),
          end: Math.min(oldSelection.end - matches[1].length, displayText.length)
        });
      }
    }
  }

  insertText(text: string) {
    if (!this.rendered) {
      this._postRenderActions.push(this.insertText.bind(this, text));
      return;
    }
    this._insertText(text);
  }

  protected _insertText(textToInsert: string) {
    if (!textToInsert) {
      return;
    }

    // Prevent insert if new length would exceed maxLength to prevent unintended deletion of characters at the end of the string
    let selection = this._getSelection();
    let text = this._applyTextToSelection(this.$field.val() as string, textToInsert, selection);
    if (text.length > this.maxLength) {
      this._showNotification('ui.CannotInsertTextTooLong');
      return;
    }

    this.$field.val(text);
    this._setSelection(selection.start + textToInsert.length);

    // Make sure display text gets sent (necessary if field does not have the focus)
    if (this.updateDisplayTextOnModify) {
      // If flag is true, we need to send two events (First while typing=true, second = false)
      this.acceptInput(true);
    }
    this.acceptInput();
  }

  protected _applyTextToSelection(text: string, textToInsert: string, selection: StringFieldSelection): string {
    if (this.inputObfuscated) {
      // Use empty text when input is obfuscated, otherwise text will be added to obfuscated text
      text = '';
    }
    return text.slice(0, selection.start) + textToInsert + text.slice(selection.end);
  }

  setWrapText(wrapText: boolean) {
    this.setProperty('wrapText', wrapText);
  }

  protected _renderWrapText() {
    this.$field.attr('wrap', this.wrapText ? 'soft' : 'off');
  }

  setTrimText(trimText: boolean) {
    this.setProperty('trimText', trimText);
  }

  protected _renderTrimText() {
    // nop, property used in _validateDisplayText()
  }

  setMultilineText(multilineText: boolean) {
    this.setProperty('multilineText', multilineText);
  }

  protected _setMultilineText(multilineText: boolean) {
    this._setProperty('multilineText', multilineText);
    this.keyStrokeContext.setMultiline(this.multilineText);
  }

  protected override _renderGridData() {
    super._renderGridData();
    this.updateInnerAlignment({
      useHorizontalAlignment: !this.multilineText
    });
  }

  protected override _renderGridDataHints() {
    super._renderGridDataHints();
    this.updateInnerAlignment({
      useHorizontalAlignment: true
    });
  }

  setMaxLength(maxLength: number) {
    this.setProperty('maxLength', maxLength);
  }

  protected _renderMaxLength() {
    this.maxLengthHandler.render();
  }

  /** @internal */
  _onIconClick() {
    this.acceptInput();
    this.$field.focus();
    this.trigger('action');
  }

  protected _onSelectionChangingAction(event: JQuery.TriggeredEvent) {
    if (event.type === 'mousedown') {
      this.$field.window().one('mouseup.stringfield', () => {
        // For some reason, when clicking side an existing selection (which clears the selection), the old
        // selection is still visible. To get around this case, we use setTimeout to handle the new selection
        // after it really has been changed.
        setTimeout(this._updateSelection.bind(this));
      });
    } else if (event.type === 'keydown') {
      // Use set timeout to let the cursor move to the target position
      setTimeout(this._updateSelection.bind(this));
    } else {
      this._updateSelection();
    }
  }

  protected _getSelection(): StringFieldSelection {
    let start = scout.nvl((this.$field[0] as HTMLInputElement).selectionStart, null);
    let end = scout.nvl((this.$field[0] as HTMLInputElement).selectionEnd, null);
    if (start === null || end === null) {
      start = 0;
      end = 0;
    }
    return {
      start: start,
      end: end
    };
  }

  protected _setSelection(selectionStartOrSelection: number | StringFieldSelection, selectionEnd?: number) {
    if (typeof selectionStartOrSelection === 'number') {
      selectionEnd = scout.nvl(selectionEnd, selectionStartOrSelection);
    } else if (typeof selectionStartOrSelection === 'object') {
      selectionEnd = selectionStartOrSelection.end;
      selectionStartOrSelection = selectionStartOrSelection.start;
    }
    (this.$field[0] as HTMLInputElement).selectionStart = selectionStartOrSelection;
    (this.$field[0] as HTMLInputElement).selectionEnd = selectionEnd;
    this._updateSelection();
  }

  protected _updateSelection() {
    let oldSelectionStart = this.selectionStart;
    let oldSelectionEnd = this.selectionEnd;
    this.selectionStart = (this.$field[0] as HTMLInputElement).selectionStart;
    this.selectionEnd = (this.$field[0] as HTMLInputElement).selectionEnd;
    if (this.selectionTrackingEnabled) {
      let selectionChanged = this.selectionStart !== oldSelectionStart || this.selectionEnd !== oldSelectionEnd;
      if (selectionChanged) {
        this.triggerSelectionChange();
      }
    }
  }

  triggerSelectionChange() {
    this.trigger('selectionChange', {
      selectionStart: this.selectionStart,
      selectionEnd: this.selectionEnd
    });
  }

  protected override _validateValue(value: string): string {
    if (objects.isNullOrUndefined(value)) {
      return value;
    }
    value = strings.asString(value);
    if (this.trimText) {
      value = value.trim();
    }
    return super._validateValue(value);
  }

  protected override _clear() {
    super._clear();

    // Disable obfuscation when user clicks on clear icon.
    this.inputObfuscated = false;
  }

  protected override _updateEmpty() {
    this.empty = strings.empty(this.value);
  }

  override acceptInput(whileTyping?: boolean) {
    let displayText = scout.nvl(this._readDisplayText(), '');
    if (this.inputObfuscated && displayText !== '') {
      // Disable obfuscation if user has typed text (on focus, field will be cleared if obfuscated, so any typed text is new text).
      this.inputObfuscated = false;
    }

    super.acceptInput(whileTyping);
  }

  protected override _onFieldFocus(event: JQuery.FocusEvent) {
    super._onFieldFocus(event);

    if (this.inputObfuscated) {
      this.$field.val('');

      // Without properly setting selection start and end, cursor is not visible in IE and Firefox.
      setTimeout(() => {
        if (!this.rendered) {
          return;
        }
        let $field = this.$field[0] as HTMLInputElement;
        $field.selectionStart = 0;
        $field.selectionEnd = 0;
      });
    }
  }

  protected _showNotification(textKey: string) {
    scout.create(DesktopNotification, {
      parent: this,
      severity: Status.Severity.WARNING,
      message: this.session.text(textKey)
    }).show();
  }

  protected override _checkDisplayTextChanged(displayText: string, whileTyping?: boolean): boolean {
    let displayTextChanged = super._checkDisplayTextChanged(displayText, whileTyping);

    // Display text hasn't changed if input is obfuscated and current display text is empty (because field will be cleared if user focuses obfuscated text field).
    if (displayTextChanged && this.inputObfuscated && displayText === '') {
      return false;
    }

    return displayTextChanged;
  }
}

export type StringFieldFormat = EnumObject<typeof StringField.Format>;
export type StringFieldSelection = {
  start: number;
  end: number;
};
