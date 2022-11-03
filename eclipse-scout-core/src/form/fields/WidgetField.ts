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
import {FormField, RefModel, Widget, WidgetFieldEventMap, WidgetFieldLayout, WidgetFieldModel, WidgetModel} from '../../index';

export default class WidgetField extends FormField implements WidgetFieldModel {
  declare model: WidgetFieldModel;
  declare eventMap: WidgetFieldEventMap;
  declare self: WidgetField;

  scrollable: boolean;
  fieldWidget: Widget;

  constructor() {
    super();

    this.scrollable = true;
    this.fieldWidget = null;
    this._addWidgetProperties(['fieldWidget']);
  }

  protected override _init(model: WidgetFieldModel) {
    super._init(model);
  }

  protected _render() {
    this.addContainer(this.$parent, 'widget-field', new WidgetFieldLayout(this));
    this.addLabel();
    this.addMandatoryIndicator();
    this.addStatus();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderFieldWidget();
    this._renderScrollable();
  }

  setFieldWidget(fieldWidget: Widget | RefModel<WidgetModel>) {
    this.setProperty('fieldWidget', fieldWidget);
  }

  protected _renderFieldWidget() {
    if (!this.fieldWidget) {
      return;
    }
    this.fieldWidget.render();
    this.addField(this.fieldWidget.$container);
    this.invalidateLayoutTree();
  }

  protected _removeFieldWidget() {
    if (!this.fieldWidget) {
      return;
    }
    this.fieldWidget.remove();
    this._removeField();
    this.invalidateLayoutTree();
  }

  setScrollable(scrollable: boolean) {
    this.setProperty('scrollable', scrollable);
  }

  protected _renderScrollable() {
    this._uninstallScrollbars();
    if (this.scrollable) {
      this._installScrollbars();
    }
  }

  override get$Scrollable(): JQuery {
    return this.$fieldContainer;
  }
}
