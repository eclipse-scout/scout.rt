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
import {FormField, WidgetFieldLayout} from '../../index';

export default class WidgetField extends FormField {

  constructor() {
    super();

    this.scrollable = true;
    this.fieldWidget = null;
    this._addWidgetProperties(['fieldWidget']);
  }

  _init(model) {
    super._init(model);
  }

  _render() {
    this.addContainer(this.$parent, 'widget-field', new WidgetFieldLayout(this));
    this.addLabel();
    this.addMandatoryIndicator();
    this.addStatus();
  }

  _renderProperties() {
    super._renderProperties();
    this._renderFieldWidget();
    this._renderScrollable();
  }

  setFieldWidget(fieldWidget) {
    this.setProperty('fieldWidget', fieldWidget);
  }

  _renderFieldWidget() {
    if (!this.fieldWidget) {
      return;
    }
    this.fieldWidget.render();
    this.addField(this.fieldWidget.$container);
    this.invalidateLayoutTree();
  }

  _removeFieldWidget() {
    if (!this.fieldWidget) {
      return;
    }
    this.fieldWidget.remove();
    this._removeField();
    this.invalidateLayoutTree();
  }

  setScrollable(scrollable) {
    this.setProperty('scrollable', scrollable);
  }

  _renderScrollable() {
    this._uninstallScrollbars();
    if (this.scrollable) {
      this._installScrollbars();
    }
  }

  /**
   * @override
   */
  get$Scrollable() {
    return this.$fieldContainer;
  }
}
