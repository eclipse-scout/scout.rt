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
import {ColumnLayout, GridData, HtmlComponent, LogicalGridData, Menu} from '../../../index';

export default class FormFieldMenu extends Menu {

  constructor() {
    super();
    this._addWidgetProperties('field');
  }

  _render() {
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

  _renderProperties() {
    super._renderProperties();
    this._renderField();
  }

  /**
   * Override
   */
  _renderText(text) {
    super._renderText(text);
    if (this.field && this.field.rendered && this.$text) {
      this.field.$container.insertAfter(this.$text);
    }
  }

  setField(field) {
    this.setProperty('field', field);
  }

  _renderField() {
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

  _removeField() {
    if (this.field) {
      this.field.remove();
    }
  }

  clone(model, options) {
    let clone = super.clone(model, options);
    this._deepCloneProperties(clone, ['field'], options);
    return clone;
  }

  isTabTarget() {
    return false;
  }

  _renderOverflown() {
    super._renderOverflown();
    this.field._hideStatusMessage();
  }
}
