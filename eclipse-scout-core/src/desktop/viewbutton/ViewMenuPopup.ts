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
import {CompositeTile, Icon, Label, scout, Tile, TileGrid, ViewMenuPopupEnterKeyStroke, WidgetPopup} from '../../index';

/**
 * Popup menu to switch between outlines.
 */
export default class ViewMenuPopup extends WidgetPopup {

  constructor() {
    super();
    this.cssClass = 'view-menu-popup';
    this.defaultIconId = null;
    this.viewMenus = [];
    this.trimWidth = true;
  }

  _init(options) {
    super._init(options);
    let tiles = this._createTiles();
    let noIcons = tiles.every(tile => !tile.widgets[0].visible);
    this.content = scout.create(TileGrid, {
      parent: this,
      tiles: tiles,
      cssClass: noIcons ? 'no-icons' : '',
      selectable: true,
      multiSelect: false,
      gridColumnCount: this._computeGridColumnCount(tiles),
      layoutConfig: {
        columnWidth: 100,
        rowHeight: -1,
        vgap: 10,
        hgap: 10
      }
    });
    let tile = this.content.tiles.find(tile => tile.viewMenu.selected);
    if (tile) {
      this.content.selectTile(tile);
    }
  }

  _computeGridColumnCount(tiles) {
    if (tiles.length > 8) {
      return 4;
    }
    if (tiles.length > 4) {
      return 3;
    }
    return 2;
  }

  _createTiles() {
    return this.viewMenus.map(menu => ({
      objectType: CompositeTile,
      displayStyle: Tile.DisplayStyle.PLAIN,
      cssClass: scout.nvl(menu.cssClass, '') + ' view-menu-tile ' + (menu.selected ? 'checked ' : '') + (!menu.iconId ? 'text-only' : ''),
      modelClass: menu.modelClass,
      classId: menu.classId,
      viewMenu: menu,
      enabled: menu.enabled,
      gridDataHints: {
        useUiHeight: true
      },
      widgets: [
        {
          objectType: Icon,
          iconDesc: menu.iconId,
          visible: !!menu.iconId,
          prepend: true
        },
        {
          objectType: Label,
          value: menu.text,
          cssClass: 'label'
        }
      ]
    }));
  }

  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStroke([
      new ViewMenuPopupEnterKeyStroke(this)
    ]);
  }

  _renderContent() {
    super._renderContent();
    this.content.$container.on('click', '.tile', event => {
      let target = scout.widget(event.target);
      if (!(target instanceof Tile)) {
        target = target.findParent(parent => parent instanceof Tile);
      }
      this.activateTile(target);
    });
  }

  activateTile(tile) {
    if (!tile || !tile.viewMenu.enabledComputed) {
      return;
    }
    tile.viewMenu.doAction();
    this.close();
  }
}
