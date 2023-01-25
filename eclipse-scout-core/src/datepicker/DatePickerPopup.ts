/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, DateField, DatePicker, DatePickerPopupLayout, DatePickerPopupModel, FormField, HtmlComponent, InitModelOf, Popup, scout, SomeRequired} from '../index';

export class DatePickerPopup extends Popup implements DatePickerPopupModel {
  declare model: DatePickerPopupModel;
  declare initModel: SomeRequired<this['model'], 'parent' | 'field'>;

  field: DateField;
  picker: DatePicker;

  constructor() {
    super();
    this.windowPaddingX = 0;
    this.windowPaddingY = 0;
  }

  protected override _init(options: InitModelOf<this>) {
    options.scrollType = options.scrollType || 'layoutAndPosition';
    options.withFocusContext = false;
    super._init(options);

    this.picker = scout.create(DatePicker, {
      parent: this,
      dateFormat: options.dateFormat,
      allowedDates: options.allowedDates
    });
  }

  protected override _createLayout(): AbstractLayout {
    return new DatePickerPopupLayout(this);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('popup date-picker-popup');
    this.$container.toggleClass('alternative', this.field.fieldStyle === FormField.FieldStyle.ALTERNATIVE);
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(this._createLayout());
    this.htmlComp.validateRoot = true;
    this.findDesktop().adjustOverlayOrder(this);
    this.picker.render();
  }

  getDatePicker(): DatePicker {
    return this.picker;
  }

  /**
   * override because the icon is not in the $anchor container.
   */
  protected override _isMouseDownOnAnchor(event: MouseEvent): boolean {
    let target = event.target as HTMLElement;
    return this.field.$dateField.isOrHas(target)
      || this.field.$dateFieldIcon.isOrHas(target)
      || (this.field.$dateClearIcon && this.field.$dateClearIcon.isOrHas(target));
  }
}
