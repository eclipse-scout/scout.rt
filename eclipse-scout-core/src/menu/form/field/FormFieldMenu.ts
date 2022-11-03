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
import {ColumnLayout, FormField, FormFieldMenuEventMap, FormFieldMenuModel, FormFieldModel, GridData, HtmlComponent, LogicalGridData, Menu, RefModel} from '../../../index';
import {CloneOptions} from '../../../widget/Widget';
import {Optional} from '../../../types';

export default class FormFieldMenu extends Menu implements FormFieldMenuModel {
  declare model: FormFieldMenuModel;
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

  setField(field: FormField | RefModel<FormFieldModel>) {
    this.setProperty('field', field);
  }

  protected _renderField() {
    if (this.field) {
      // Use gridDataHints as "computed" gridData property, because FormFieldMenu
      // does not have a logical grid (see FormField._updateElementInnerAlignment()).
      this.field.gridData = GridData.createFromHints(this.field, 1);

      this.field.render(this.$container);
      let layoutData = new LogicalGridData(this.field);
      layoutData.validate();
      this.field.setLayoutData(layoutData);
      this.field.$container.addClass('content');
    }
  }

  protected _removeField() {
    if (this.field) {
      this.field.remove();
    }
  }

  override clone(model: Optional<FormFieldMenuModel, 'parent'>, options: CloneOptions): this {
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
