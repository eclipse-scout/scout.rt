/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, ChildModelOf, CompositeTile, CompositeTileModel, Icon, InitModelOf, Label, scout, Tile, TileGrid, ViewButton, ViewMenuPopupEnterKeyStroke, ViewMenuPopupModel, WidgetPopup} from '../../index';

/**
 * Popup menu to switch between outlines.
 */
export class ViewMenuPopup extends WidgetPopup implements ViewMenuPopupModel {
  declare model: ViewMenuPopupModel;
  declare content: TileGrid<ViewButtonTile>;

  defaultIconId: string;
  viewMenus: ViewButton[];

  constructor() {
    super();
    this.cssClass = 'view-menu-popup';
    this.defaultIconId = null;
    this.viewMenus = [];
    this.trimWidth = true;
  }

  protected override _init(options: InitModelOf<this>) {
    super._init(options);
    let tiles = this._createTiles();
    let noIcons = tiles.every(tile => !tile.widgets[0].visible);
    this.content = scout.create((TileGrid<ViewButtonTile>), {
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
    let viewButtonTiles = this.content.tiles;
    let tile = viewButtonTiles.find(tile => tile.viewMenu.selected);
    if (tile) {
      this.content.selectTile(tile);
    }

    this.content.on('propertyChange:selectedTiles', event => {
      this._renderSelectedTiles();
    });
  }

  protected _computeGridColumnCount(tiles: ChildModelOf<ViewButtonTile>[]): number {
    if (tiles.length > 8) {
      return 4;
    }
    if (tiles.length > 4) {
      return 3;
    }
    return 2;
  }

  protected _createTiles(): ChildModelOf<ViewButtonTile>[] {
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

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    this.keyStrokeContext.registerKeyStroke(new ViewMenuPopupEnterKeyStroke(this));
  }

  protected override _renderContent() {
    super._renderContent();
    aria.role(this.content.$container, 'menu');
    this.content.$container.on('click', '.tile', event => {
      let target = scout.widget(event.target);
      if (!(target instanceof Tile)) {
        target = target.findParent(Tile);
      }
      this.activateTile(target as Tile);
    });

    this._renderTiles();
    this._renderSelectedTiles();
  }

  protected _renderTiles() {
    this.content.tiles.forEach(tile => {
      aria.role(tile.$container, 'menuitem');
      this._linkWidgetLabels(tile);
    });
  }

  protected _linkWidgetLabels(tile: ViewButtonTile) {
    let $labels = tile.$container.children('.label');
    if ($labels.length > 0) {
      aria.linkElementWithLabel(tile.$container, $labels.eq(0));
    }
  }

  protected _renderSelectedTiles() {
    let selectedTiles = this.content.selectedTiles;
    if (selectedTiles.length === 1) {
      aria.linkElementWithActiveDescendant(this.content.$container, selectedTiles[0].$container);
    }
  }

  activateTile(tile: Tile & { viewMenu?: ViewButton }) {
    if (!tile || !tile.viewMenu.enabledComputed) {
      return;
    }
    tile.viewMenu.doAction();
    this.close();
  }
}

export type ViewButtonTileModel = CompositeTileModel & { viewMenu?: ViewButton };
export type ViewButtonTile = CompositeTile & { viewMenu?: ViewButton };
