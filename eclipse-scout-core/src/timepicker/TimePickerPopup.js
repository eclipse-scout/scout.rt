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
import {FormField, HtmlComponent, Popup, scout, TimePickerPopupLayout} from '../index';

export default class TimePickerPopup extends Popup {

  constructor() {
    super();
    this.windowPaddingX = 0;
    this.windowPaddingY = 0;
  }

  _init(options) {
    options.scrollType = options.scrollType || 'layoutAndPosition';
    options.withFocusContext = false;
    super._init(options);

    this.picker = scout.create('TimePicker', {
      parent: this,
      timeResolution: options.timeResolution
    });
  }

  _createLayout() {
    return new TimePickerPopupLayout(this);
  }

  _render() {
    this.$container = this.$parent.appendDiv('popup time-picker-popup');
    this.$container.toggleClass('alternative', this.field.fieldStyle === FormField.FieldStyle.ALTERNATIVE);
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(this._createLayout());
    this.htmlComp.validateRoot = true;
    this.picker.render();
  }

  getTimePicker() {
    return this.picker;
  }

  /**
   * @override because the icon is not in the $anchor container.
   */
  _isMouseDownOnAnchor(event) {
    return this.field.$timeField.isOrHas(event.target) || this.field.$timeFieldIcon.isOrHas(event.target) || (this.field.$timeClearIcon && this.field.$timeClearIcon.isOrHas(event.target));
  }
}
