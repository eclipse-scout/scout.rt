/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CloneOptions, ColumnLayout, ColumnLayoutData, FormField, FormFieldMenuEventMap, FormFieldMenuModel, GridData, HtmlComponent, Menu, ObjectOrChildModel, SomeRequired} from '../../../index';

export class FormFieldMenu extends Menu implements FormFieldMenuModel {
  declare model: FormFieldMenuModel;
  declare initModel: SomeRequired<this['model'], 'parent' | 'field'>;
  declare eventMap: FormFieldMenuEventMap;
  declare self: FormFieldMenu;

  field: FormField;

  constructor() {
    super();
    this._addWidgetProperties('field');
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('menu-item');
    this.$container.addClass('form-field-menu');
    if (this.uiCssClass) {
      this.$container.addClass(this.uiCssClass);
    }
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new ColumnLayout({
      stretch: false
    }));
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderField();
  }

  protected override _renderText() {
    super._renderText();
    if (this.field && this.field.rendered && this.$text) {
      this.field.$container.insertAfter(this.$text);
    }
  }

  setField(field: ObjectOrChildModel<FormField>) {
    this.setProperty('field', field);
  }

  protected _renderField() {
    if (this.field) {
      // Use gridDataHints as "computed" gridData property, because FormFieldMenu
      // does not have a logical grid (see FormField._updateElementInnerAlignment()).
      this.field.gridData = GridData.createFromHints(this.field, 1);

      this.field.render(this.$container);
      this.field.setLayoutData({widthHint: this.field.gridData.widthInPixel} as ColumnLayoutData);
      this.field.$container.addClass('content');
    }
  }

  protected _removeField() {
    if (this.field) {
      this.field.remove();
    }
  }

  override clone(model: FormFieldMenuModel, options: CloneOptions): this {
    let clone = super.clone(model, options) as FormFieldMenu;
    this._deepCloneProperties(clone, ['field'], options);
    return clone as this;
  }

  override isTabTarget(): boolean {
    return false;
  }

  protected override _renderOverflown() {
    super._renderOverflown();
    this.field._hideStatusMessage();
  }
}
