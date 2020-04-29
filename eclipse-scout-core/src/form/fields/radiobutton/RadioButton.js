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
import {Button, Device, fields, RadioButtonKeyStroke, RadioButtonLayout, tooltips} from '../../../index';

export default class RadioButton extends Button {

  constructor() {
    super();

    this.gridDataHints.fillHorizontal = true;
    this.focusWhenSelected = true;
    this.wrapText = false;
    this.buttonKeyStroke = new RadioButtonKeyStroke(this, null);
    this.radioValue = null;
  }

  /**
   * @override Button.js
   */
  _initDefaultKeyStrokes() {
    this.keyStrokeContext.registerKeyStroke([
      new RadioButtonKeyStroke(this, 'ENTER'),
      new RadioButtonKeyStroke(this, 'SPACE')
    ]);
  }

  _render() {
    this.addContainer(this.$parent, 'radio-button', new RadioButtonLayout(this));
    this.addFieldContainer(this.$parent.makeDiv());
    this.$radioButton = this.$fieldContainer
      .appendDiv('radio-button-circle')
      .data('radiobutton', this);
    this.addField(this.$radioButton);

    // $buttonLabel is used by Button.js as well -> Button.js handles label
    this.$buttonLabel = this.$fieldContainer
      .appendDiv('label');

    fields.linkElementWithLabel(this.$radioButton, this.$buttonLabel);

    this.$fieldContainer.on('mousedown', this._onMouseDown.bind(this));

    tooltips.installForEllipsis(this.$buttonLabel, {
      parent: this
    });

    this.addStatus();
    this.session.keyStrokeManager.installKeyStrokeContext(this.formKeyStrokeContext);
  }

  _remove() {
    tooltips.uninstall(this.$buttonLabel);
    this.session.keyStrokeManager.uninstallKeyStrokeContext(this.formKeyStrokeContext);
    super._remove();
  }

  /**
   * @override
   */
  _renderProperties() {
    super._renderProperties();
    this._renderWrapText();
    this._renderSelected();
  }

  setWrapText(wrapText) {
    this.setProperty('wrapText', wrapText);
  }

  _renderWrapText() {
    this.$buttonLabel.toggleClass('white-space-nowrap', !this.wrapText);
    this.invalidateLayoutTree();
  }

  /**
   * Convenience for {@link #setSelected(true)}
   */
  select() {
    this.setSelected(true);
  }

  setSelected(selected) {
    this.setProperty('selected', selected);
  }

  _renderSelected() {
    this.$fieldContainer.toggleClass('checked', this.selected);
    this.$field.toggleClass('checked', this.selected);
  }

  setTabbable(tabbable) {
    if (this.rendered) {
      this.$field.setTabbable(tabbable && !Device.get().supportsOnlyTouch());
    }
  }

  isTabbable() {
    return this.rendered && this.$field.isTabbable();
  }

  _renderIconId() {
    super._renderIconId();
    let $icon = this.get$Icon();
    if ($icon.length > 0) {
      $icon.insertAfter(this.$radioButton);
    }
  }

  /**
   * @override Button.js
   */
  doAction() {
    if (!this.enabledComputed || !this.visible) {
      return false;
    }
    // Since RadioButton extends Button, doAction should do something useful because it may be called (and actually is by ButtonKeyStroke)
    this.select();
    return true;
  }

  _onMouseDown(event) {
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
