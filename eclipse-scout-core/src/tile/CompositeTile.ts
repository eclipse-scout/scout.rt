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
import {HtmlComponent, PropertyChangeEvent, Tile, Widget} from '../index';
import TileModel from './TileModel';
import TileEventMap from './TileEventMap';

export interface CompositeTileModel extends TileModel {
  widgets?: Widget[];
}

export interface CompositeTileEventMap extends TileEventMap {
  'propertyChange:widgets': PropertyChangeEvent<Widget[], CompositeTile>;
}

export default class CompositeTile extends Tile implements CompositeTileModel {
  declare model: CompositeTileModel;
  declare eventMap: CompositeTileEventMap;

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

  setWidgets(widgets: Widget[]) {
    this.setProperty('widgets', widgets);
  }

  protected _renderWidgets() {
    this.widgets.forEach(widget => {
      widget.render();
    });
    this.invalidateLayoutTree();
  }
}
