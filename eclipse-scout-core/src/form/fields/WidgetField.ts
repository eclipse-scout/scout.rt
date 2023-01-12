/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormField, InitModelOf, ObjectOrChildModel, Widget, WidgetFieldEventMap, WidgetFieldLayout, WidgetFieldModel} from '../../index';

export class WidgetField extends FormField implements WidgetFieldModel {
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

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
  }

  protected override _render() {
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

  setFieldWidget(fieldWidget: ObjectOrChildModel<Widget>) {
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
