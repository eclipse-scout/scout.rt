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
import {strings, texts, ValueField} from '../../../index';

export default class LabelField extends ValueField {

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
  _initValue(value) {
    value = texts.resolveText(value, this.session.locale.languageTag);
    super._initValue(value);
  }

  _render() {
    this.addContainer(this.$parent, 'label-field');
    this.addLabel();
    this.addField(this.$parent.makeDiv());
    this.addStatus();
  }

  _renderProperties() {
    super._renderProperties();
    this._renderWrapText();
    this._renderSelectable();
  }

  /**
   * @override
   */
  _renderFocused() {
    // NOP, don't add "focused" class. It doesn't look good when the label is highlighted but no cursor is visible.
  }

  /**
   * Since a LabelField cannot be changed by a user, acceptInput does nothing.
   * Otherwise LabelFields could 'become' touched, because value and displayText
   * of the LabelField don't match.
   */
  acceptInput() {
    // NOP
  }

  setHtmlEnabled(htmlEnabled) {
    this.setProperty('htmlEnabled', htmlEnabled);
  }

  _renderHtmlEnabled() {
    // Render the display text again when html enabled changes dynamically
    this._renderDisplayText();
  }

  /**
   * @override
   */
  _renderDisplayText() {
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

  setWrapText(wrapText) {
    this.setProperty('wrapText', wrapText);
  }

  _renderWrapText() {
    this.$field.toggleClass('white-space-nowrap', !this.wrapText);
    this.invalidateLayoutTree();
  }

  setSelectable(selectable) {
    this.setProperty('selectable', selectable);
  }

  _renderSelectable() {
    this.$container.toggleClass('selectable', !!this.selectable);
    // Allow this field to receive the focus when selecting text with the mouse. Otherwise, form
    // keystrokes would no longer work because the focus would automatically be set to the desktop
    // for lack of alternatives. The value -1 ensures the field is skipped when tabbing through
    // all form fields.
    this.$field.toggleAttr('tabindex', !!this.selectable, '-1');
  }

  _renderGridData() {
    super._renderGridData();
    this.updateInnerAlignment({
      useHorizontalAlignment: true
    });
  }

  _renderGridDataHints() {
    super._renderGridDataHints();
    this.updateInnerAlignment({
      useHorizontalAlignment: true
    });
  }

  _onAppLinkAction(event) {
    let $target = $(event.delegateTarget);
    let ref = $target.data('ref');
    this.triggerAppLinkAction(ref);
  }

  triggerAppLinkAction(ref) {
    this.trigger('appLinkAction', {
      ref: ref
    });
  }

  addField($field) {
    super.addField($field);
    this.$field.off('blur')
      .off('focus');
  }
}
