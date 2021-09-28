/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, fields, graphics, HtmlComponent, SmartField, SmartFieldLayout, SmartFieldMultilineLayout} from '../../../index';

export default class SmartFieldMultiline extends SmartField {

  constructor() {
    super();
    this.options;
    this._$multilineLines;
  }

  _render() {
    let $input, htmlComp;

    this.addContainer(this.$parent, 'smart-field has-icon', new SmartFieldLayout(this));
    this.addLabel();
    this.addFieldContainer(this.$parent.makeDiv('multiline'));
    htmlComp = HtmlComponent.install(this.$fieldContainer, this.session);
    htmlComp.setLayout(new SmartFieldMultilineLayout(this));

    $input = fields.makeInputOrDiv(this, 'multiline-input')
      .on('mousedown', this._onFieldMouseDown.bind(this))
      .appendTo(this.$fieldContainer);

    if (!this.touchMode) {
      $input
        .keyup(this._onFieldKeyUp.bind(this))
        .keydown(this._onFieldKeyDown.bind(this))
        .on('input', this._onFieldInput.bind(this));
    }
    this.addField($input);

    this._$multilineLines = this.$fieldContainer
      .appendDiv('multiline-lines')
      .on('click', this._onMultilineLinesClick.bind(this));
    if (!this.embedded) {
      this.addMandatoryIndicator();
    }
    this.addIcon();
    this.addStatus();
  }

  _renderEnabled() {
    super._renderEnabled();
    this._$multilineLines.setEnabled(this.enabledComputed);
  }

  /**
   * Sets the focus to the input field when user clicks on text lines, but only if nothing is selected.
   * Otherwise it would be impossible for the user to select the text. That's why we cannot use the
   * mousedown event here too.
   */
  _onMultilineLinesClick(event) {
    if (this.enabledComputed) {
      let selection = this.$field.window(true).getSelection();
      if (!selection.toString()) {
        this.$field.focus();
      }
    }
  }

  _renderDisplayText() {
    super._renderDisplayText();
    let additionalLines = this.additionalLines();
    if (additionalLines) {
      this._$multilineLines.html(arrays.formatEncoded(additionalLines, '<br/>'));
    } else {
      this._$multilineLines.empty();
    }
  }

  _getInputBounds() {
    let fieldBounds = graphics.offsetBounds(this.$fieldContainer),
      textFieldBounds = graphics.offsetBounds(this.$field);
    fieldBounds.height = textFieldBounds.height;
    return fieldBounds;
  }

  _renderFocused() {
    super._renderFocused();
    this._$multilineLines.toggleClass('focused', this.focused);
  }

  _updateErrorStatusClasses(statusClass, hasStatus) {
    super._updateErrorStatusClasses(statusClass, hasStatus);
    this._updateErrorStatusClassesOnElement(this._$multilineLines, statusClass, hasStatus);
  }
}
