/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, Button, Device, LookupRow, RadioButtonEventMap, RadioButtonKeyStroke, RadioButtonLayout, RadioButtonModel, tooltips} from '../../../index';

export class RadioButton<TValue> extends Button implements RadioButtonModel<TValue> {
  declare model: RadioButtonModel<TValue>;
  declare eventMap: RadioButtonEventMap<TValue>;
  declare self: RadioButton<any>;

  focusWhenSelected: boolean;
  wrapText: boolean;
  radioValue: TValue;
  $radioButton: JQuery;
  lookupRow: LookupRow<TValue>;

  constructor() {
    super();

    this.gridDataHints.fillHorizontal = true;
    this.focusWhenSelected = true;
    this.wrapText = false;
    this.buttonKeyStroke = new RadioButtonKeyStroke(this, null);
    this.radioValue = null;
  }

  protected override _initDefaultKeyStrokes() {
    this.keyStrokeContext.registerKeyStrokes([
      new RadioButtonKeyStroke(this, 'ENTER'),
      new RadioButtonKeyStroke(this, 'SPACE')
    ]);
  }

  protected override _render() {
    this.addContainer(this.$parent, 'radio-button', new RadioButtonLayout(this));
    this.addFieldContainer(this.$parent.makeDiv());
    this.$radioButton = this.$fieldContainer
      .appendDiv('radio-button-circle')
      .data('radiobutton', this);

    // $buttonLabel is used by Button.js as well -> Button.js handles label
    this.$buttonLabel = this.$fieldContainer
      .appendDiv('label');

    aria.role(this.$radioButton, 'radio');
    this.addField(this.$radioButton);

    this.$fieldContainer.on('mousedown', this._onMouseDown.bind(this));

    tooltips.installForEllipsis(this.$buttonLabel, {
      parent: this
    });

    this.addStatus();
    this.session.keyStrokeManager.installKeyStrokeContext(this.formKeyStrokeContext);
  }

  protected override _remove() {
    tooltips.uninstall(this.$buttonLabel);
    this.session.keyStrokeManager.uninstallKeyStrokeContext(this.formKeyStrokeContext);
    super._remove();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderWrapText();
    this._renderSelected();
  }

  setWrapText(wrapText: boolean) {
    this.setProperty('wrapText', wrapText);
  }

  protected _renderWrapText() {
    this.$buttonLabel.toggleClass('white-space-nowrap', !this.wrapText);
    this.invalidateLayoutTree();
  }

  /**
   * Convenience for {@link #setSelected(true)}
   */
  select() {
    this.setSelected(true);
  }

  protected override _renderSelected() {
    this.$fieldContainer.toggleClass('checked', this.selected);
    this.$field.toggleClass('checked', this.selected);
    aria.checked(this.$radioButton, this.selected);
  }

  setTabbable(tabbable: boolean) {
    if (this.rendered) {
      this.$field.setTabbable(tabbable && !Device.get().supportsOnlyTouch());
    }
  }

  isTabbable(): boolean {
    return this.rendered && this.$field.isTabbable();
  }

  protected override _renderIconId() {
    super._renderIconId();
    let $icon = this.get$Icon();
    if ($icon.length > 0) {
      $icon.insertAfter(this.$radioButton);
    }
  }

  protected override _renderSubmenuIcon() {
    // Do not render sub menu icon
  }

  override doAction(): boolean {
    if (!this.enabledComputed || !this.visible) {
      return false;
    }
    // Since RadioButton extends Button, doAction should do something useful because it may be called (and actually is by ButtonKeyStroke)
    this.select();
    return true;
  }

  protected _onMouseDown(event: JQuery.MouseDownEvent) {
    if (!this.enabledComputed) {
      return;
    }
    let onIcon = this.get$Icon().is(event.target),
      onButton = this.$radioButton.is(event.target),
      onLabel = this.$buttonLabel.isOrHas(event.target); // isOrHas is required for HTML enabled labels with nested elements
    if (!onButton && !onLabel && !onIcon) {
      return;
    }
    this.select();
    if (this.focusWhenSelected && (onLabel || onIcon)) {
      this.focusAndPreventDefault(event);
    }
  }
}
