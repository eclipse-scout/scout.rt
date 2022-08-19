/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {CompositeModel, HtmlComponent, Widget} from '../index';

export default class Composite extends Widget implements CompositeModel {
  widgets: Widget[];

  constructor() {
    super();

    this.widgets = [];
    this._addWidgetProperties(['widgets']);
  }

  protected _render() {
    this.$container = this.$parent.appendDiv();
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
  }

  protected _renderProperties() {
    super._renderProperties();
    this._renderWidgets();
  }

  setWidgets(widgets: Widget[]) {
    this.setProperty('widgets', widgets);
  }

  protected _renderWidgets() {
    this.widgets.forEach(widget => widget.render());
    this.invalidateLayoutTree();
  }
}
