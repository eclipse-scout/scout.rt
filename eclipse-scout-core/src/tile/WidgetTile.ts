/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {EventHandler, InitModelOf, ObjectOrChildModel, ObjectOrModel, PropertyChangeEvent, Tile, TileEventMap, TileModel, Widget} from '../index';

export interface WidgetTileModel extends TileModel {
  /** The widget that should be embedded in the tile */
  tileWidget?: ObjectOrChildModel<Widget>;
}

export interface WidgetTileEventMap extends TileEventMap {
  'propertyChange:tileWidget': PropertyChangeEvent<Widget, WidgetTile>;
}

/**
 * A tile containing a widget.
 */
export class WidgetTile extends Tile implements WidgetTileModel {
  declare model: WidgetTileModel;
  declare eventMap: WidgetTileEventMap;
  declare self: WidgetTile;

  tileWidget: Widget;
  protected _widgetPropertyChangeHandler: EventHandler<PropertyChangeEvent<any, Widget>>;

  constructor() {
    super();
    this.tileWidget = null;
    this._addWidgetProperties(['tileWidget']);
    this._widgetPropertyChangeHandler = this._onWidgetPropertyChange.bind(this);
  }

  protected override _init(model: InitModelOf<this>) {
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

  setTileWidget(tileWidget: ObjectOrModel<Widget>) {
    this.setProperty('tileWidget', tileWidget);
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
