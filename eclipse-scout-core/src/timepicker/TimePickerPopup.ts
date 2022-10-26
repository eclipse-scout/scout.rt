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
import {AbstractLayout, DateField, FormField, HtmlComponent, Popup, scout, TimePicker, TimePickerPopupLayout, TimePickerPopupModel} from '../index';

export default class TimePickerPopup extends Popup {
  declare model: TimePickerPopupModel;

  picker: TimePicker;
  field: DateField;

  constructor() {
    super();
    this.windowPaddingX = 0;
    this.windowPaddingY = 0;
    this.picker = null;
  }

  protected override _init(options: TimePickerPopupModel) {
    options.scrollType = options.scrollType || 'layoutAndPosition';
    options.withFocusContext = false;
    super._init(options);

    this.picker = scout.create(TimePicker, {
      parent: this,
      timeResolution: options.timeResolution
    });
  }

  protected override _createLayout(): AbstractLayout {
    return new TimePickerPopupLayout(this);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('popup time-picker-popup');
    this.$container.toggleClass('alternative', this.field.fieldStyle === FormField.FieldStyle.ALTERNATIVE);
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(this._createLayout());
    this.htmlComp.validateRoot = true;
    this.picker.render();
  }

  getTimePicker(): TimePicker {
    return this.picker;
  }

  /**
   * override because the icon is not in the $anchor container.
   */
  protected override _isMouseDownOnAnchor(event: MouseEvent): boolean {
    let target = event.target as HTMLElement;
    return this.field.$timeField.isOrHas(target) || this.field.$timeFieldIcon.isOrHas(target) || (this.field.$timeClearIcon && this.field.$timeClearIcon.isOrHas(target));
  }
}
