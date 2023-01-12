/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LabelFieldEventMap, LabelFieldModel, strings, texts, ValueField} from '../../../index';

export class LabelField extends ValueField<string> implements LabelFieldModel {
  declare model: LabelFieldModel;
  declare eventMap: LabelFieldEventMap;
  declare self: LabelField;

  htmlEnabled: boolean;
  selectable: boolean;
  wrapText: boolean;

  constructor() {
    super();
    this.htmlEnabled = false;
    this.selectable = true;
    this.wrapText = false;
    this.preventInitialFocus = true;
  }

  /**
   * Resolves the text key if value contains one.
   * This cannot be done in _init because the value field would call _setValue first
   */
  protected override _initValue(value: string) {
    value = texts.resolveText(value, this.session.locale.languageTag);
    super._initValue(value);
  }

  protected override _render() {
    this.addContainer(this.$parent, 'label-field');
    this.addLabel();
    this.addField(this.$parent.makeDiv());
    this.addStatus();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderWrapText();
    this._renderSelectable();
  }

  protected override _renderFocused() {
    // NOP, don't add "focused" class. It doesn't look good when the label is highlighted but no cursor is visible.
  }

  /**
   * Since a LabelField cannot be changed by a user, acceptInput does nothing.
   * Otherwise LabelFields could 'become' touched, because value and displayText
   * of the LabelField don't match.
   */
  override acceptInput(whileTyping?: boolean) {
    // NOP
  }

  setHtmlEnabled(htmlEnabled: boolean) {
    this.setProperty('htmlEnabled', htmlEnabled);
  }

  protected _renderHtmlEnabled() {
    // Render the display text again when html enabled changes dynamically
    this._renderDisplayText();
  }

  protected override _renderDisplayText() {
    let displayText = this.displayText || '';
    if (this.htmlEnabled) {
      this.$field.html(displayText);

      // Add action to app-links
      this.$field.find('.app-link')
        .on('click', this._onAppLinkAction.bind(this));
    } else {
      this.$field.html(strings.nl2br(displayText));
    }

    this.invalidateLayoutTree();
  }

  setWrapText(wrapText: boolean) {
    this.setProperty('wrapText', wrapText);
  }

  protected _renderWrapText() {
    this.$field.toggleClass('white-space-nowrap', !this.wrapText);
    this.invalidateLayoutTree();
  }

  setSelectable(selectable: boolean) {
    this.setProperty('selectable', selectable);
  }

  protected _renderSelectable() {
    this.$container.toggleClass('selectable', !!this.selectable);
    // Allow this field to receive the focus when selecting text with the mouse. Otherwise, form
    // keystrokes would no longer work because the focus would automatically be set to the desktop
    // for lack of alternatives. The value -1 ensures the field is skipped when tabbing through
    // all form fields.
    this.$field.toggleAttr('tabindex', !!this.selectable, '-1');
  }

  protected override _renderGridData() {
    super._renderGridData();
    this.updateInnerAlignment({
      useHorizontalAlignment: true
    });
  }

  protected override _renderGridDataHints() {
    super._renderGridDataHints();
    this.updateInnerAlignment({
      useHorizontalAlignment: true
    });
  }

  protected _onAppLinkAction(event: JQuery.ClickEvent) {
    let $target = $(event.delegateTarget);
    let ref = $target.data('ref') as string;
    this.triggerAppLinkAction(ref);
  }

  triggerAppLinkAction(ref: string) {
    this.trigger('appLinkAction', {
      ref: ref
    });
  }

  override addField($field: JQuery) {
    super.addField($field);
    this.$field.off('blur')
      .off('focus');
  }
}
