/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, arrays, fields, graphics, HtmlComponent, Rectangle, SmartField, SmartFieldLayout, SmartFieldMultilineLayout} from '../../../index';

export class SmartFieldMultiline<TValue> extends SmartField<TValue> {
  protected _$multilineLines: JQuery;

  constructor() {
    super();
    this._$multilineLines = null;
  }

  protected override _render() {
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
    aria.role($input, 'combobox');
    aria.expanded($input, false);
    this._addScreenReaderStatus();

    this._$multilineLines = this.$fieldContainer
      .appendDiv('multiline-lines')
      .on('click', this._onMultilineLinesClick.bind(this));
    if (!this.embedded) {
      this.addMandatoryIndicator();
    }
    this.addIcon();
    this.addStatus();
    this._addAriaFieldDescription();
  }

  protected override _renderEnabled() {
    super._renderEnabled();
    this._$multilineLines.setEnabled(this.enabledComputed);
  }

  /**
   * Sets the focus to the input field when user clicks on text lines, but only if nothing is selected.
   * Otherwise, it would be impossible for the user to select the text. That's why we cannot use the
   * mousedown event here too.
   */
  protected _onMultilineLinesClick(event: JQuery.ClickEvent) {
    if (this.enabledComputed) {
      let selection = this.$field.window(true).getSelection();
      if (!selection.toString()) {
        this.$field.focus();
      }
    }
  }

  protected override _renderDisplayText() {
    super._renderDisplayText();
    let additionalLines = this.additionalLines();
    if (additionalLines) {
      this._$multilineLines.html(arrays.formatEncoded(additionalLines, '<br/>'));
    } else {
      this._$multilineLines.empty();
    }
  }

  protected _getInputBounds(): Rectangle {
    let fieldBounds = graphics.offsetBounds(this.$fieldContainer),
      textFieldBounds = graphics.offsetBounds(this.$field);
    fieldBounds.height = textFieldBounds.height;
    return fieldBounds;
  }

  protected override _renderFocused() {
    super._renderFocused();
    this._$multilineLines.toggleClass('focused', this.focused);
  }

  protected override _updateErrorStatusClasses(statusClass: string, hasStatus: boolean) {
    super._updateErrorStatusClasses(statusClass, hasStatus);
    this._updateErrorStatusClassesOnElement(this._$multilineLines, statusClass, hasStatus);
  }
}
