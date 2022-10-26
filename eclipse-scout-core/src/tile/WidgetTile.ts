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
import {EventHandler, PropertyChangeEvent, Tile, Widget} from '../index';
import TileModel from './TileModel';
import TileEventMap from './TileEventMap';

export interface WidgetTileModel extends TileModel {
  /** The widget that should be embedded in the tile */
  tileWidget?: Widget;
}

export interface WidgetTileEventMap extends TileEventMap {
  'propertyChange:tileWidget': PropertyChangeEvent<Widget, WidgetTile>;
}

/**
 * A tile containing a widget.
 */
export default class WidgetTile extends Tile implements WidgetTileModel {
  declare model: WidgetTileModel;
  declare eventMap: WidgetTileEventMap;

  tileWidget: Widget;
  protected _widgetPropertyChangeHandler: EventHandler<PropertyChangeEvent<any, Widget>>;

  constructor() {
    super();
    this.tileWidget = null;
    this._addWidgetProperties(['tileWidget']);
    this._widgetPropertyChangeHandler = this._onWidgetPropertyChange.bind(this);
  }

  protected override _init(model: WidgetTileModel) {
    super._init(model);
    this._setTileWidget(this.tileWidget);
  }

  protected override _destroy() {
    if (this.tileWidget) {
      this.tileWidget.off('propertyChange', this._widgetPropertyChangeHandler);
    }
    super._destroy();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderTileWidget();
  }

  protected _renderTileWidget() {
    if (this.tileWidget) {
      // render the tileWidget into the container of this tile.
      this.tileWidget.render();
    }
  }

  protected _removeTileWidget() {
    if (this.tileWidget) {
      this.tileWidget.remove();
    }
  }

  protected _onWidgetPropertyChange(event: PropertyChangeEvent) {
    if (event.propertyName === 'visible') {
      this.setVisible(event.newValue);
    } else if (event.propertyName === 'enabled') {
      this.setEnabled(event.newValue);
    } else if (event.propertyName === 'disabledStyle') {
      this.setDisabledStyle(event.newValue);
    }
  }

  protected _setTileWidget(tileWidget: Widget) {
    if (this.tileWidget) {
      this.tileWidget.off('propertyChange', this._widgetPropertyChangeHandler);
    }
    this._setProperty('tileWidget', tileWidget);
    if (tileWidget) {
      // Hide tile if tileWidget is made invisible (don't do it if visible is true to not accidentally override the visibility state)
      if (!this.tileWidget.visible) {
        this.setVisible(false);
      }
      if (!this.tileWidget.enabled) {
        this.setEnabled(false);
      }
      if (this.tileWidget.disabledStyle !== Widget.DisabledStyle.DEFAULT) {
        this.setDisabledStyle(this.tileWidget.disabledStyle);
      }
      this.tileWidget.on('propertyChange', this._widgetPropertyChangeHandler);
    }
    this.invalidateLayoutTree();
  }
}
