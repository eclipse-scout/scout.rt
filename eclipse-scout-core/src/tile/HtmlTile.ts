/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HtmlComponent, PropertyChangeEvent, Tile, TileEventMap, TileModel} from '../index';

export interface HtmlTileModel extends TileModel {
  content?: string;
}

export interface HtmlTileEventMap extends TileEventMap {
  'propertyChange:content': PropertyChangeEvent<string, HtmlTile>;
}

export class HtmlTile extends Tile implements HtmlTileModel {
  declare model: HtmlTileModel;
  declare eventMap: HtmlTileEventMap;
  declare self: HtmlTile;

  content: string;

  constructor() {
    super();
    this.content = null;
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('html-tile');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderContent();
  }

  setContent(content: string) {
    this.setProperty('content', content);
  }

  protected _renderContent() {
    if (!this.content) {
      this.$container.empty();
      return;
    }
    this.$container.html(this.content);

    // Add listener to images to update the layout when the images are loaded
    this.$container.find('img')
      .on('load', this._onImageLoad.bind(this))
      .on('error', this._onImageError.bind(this));

    this.invalidateLayoutTree();
  }

  protected _onImageLoad(event: JQuery.TriggeredEvent) {
    this.invalidateLayoutTree();
  }

  protected _onImageError(event: JQuery.TriggeredEvent) {
    this.invalidateLayoutTree();
  }
}
