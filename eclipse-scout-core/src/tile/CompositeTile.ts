/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HtmlComponent, ObjectOrChildModel, PropertyChangeEvent, Tile, TileEventMap, TileModel, Widget} from '../index';

export interface CompositeTileModel extends TileModel {
  widgets?: ObjectOrChildModel<Widget>[];
}

export interface CompositeTileEventMap extends TileEventMap {
  'propertyChange:widgets': PropertyChangeEvent<Widget[], CompositeTile>;
}

export class CompositeTile extends Tile implements CompositeTileModel {
  declare model: CompositeTileModel;
  declare eventMap: CompositeTileEventMap;
  declare self: CompositeTile;

  widgets: Widget[];

  constructor() {
    super();

    this.widgets = [];
    this._addWidgetProperties(['widgets']);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv();
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderWidgets();
  }

  setWidgets(widgets: ObjectOrChildModel<Widget>[]) {
    this.setProperty('widgets', widgets);
  }

  protected _renderWidgets() {
    this.widgets.forEach(widget => {
      widget.render();
    });
    this.invalidateLayoutTree();
  }
}
