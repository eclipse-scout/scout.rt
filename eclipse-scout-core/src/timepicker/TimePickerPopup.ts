/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, DateField, FormField, HtmlComponent, InitModelOf, Popup, scout, SomeRequired, TimePicker, TimePickerPopupLayout, TimePickerPopupModel} from '../index';

export class TimePickerPopup extends Popup implements TimePickerPopupModel {
  declare model: TimePickerPopupModel;
  declare initModel: SomeRequired<this['model'], 'parent' | 'timeResolution' | 'field'>;

  picker: TimePicker;
  field: DateField;

  constructor() {
    super();
    this.windowPaddingX = 0;
    this.windowPaddingY = 0;
    this.picker = null;
  }

  protected override _init(options: InitModelOf<this>) {
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
    this.findDesktop().adjustOverlayOrder(this);
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
