/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import {HtmlTile, objects, strings} from '../index';

export default class TileTextFilter {
  constructor() {
    this.text = null;
    this._htmlTileContentChangeHandler = this._onHtmlTileContentChange.bind(this);
    this._tilePropertyChangeHandler = this._onTilePropertyChange.bind(this);
  }

  setText(text) {
    text = text || '';
    text = text.toLowerCase();
    if (objects.equals(this.text, text)) {
      return false;
    }
    this.text = text;
    return true;
  }

  accept(tile) {
    if (strings.empty(this.text)) {
      return true;
    }
    let plainText = this._plainTextForTile(tile);
    let filterText = plainText.trim().toLowerCase();
    if (strings.empty(filterText)) {
      return false;
    }
    return filterText.indexOf(this.text) > -1;
  }

  _plainTextForTile(tile) {
    if (tile.plainText) {
      return tile.plainText;
    }
    if (tile instanceof HtmlTile) {
      tile.plainText = strings.plainText(tile.content) || '';
      tile.one('propertyChange:content', this._htmlTileContentChangeHandler);
      return tile.plainText;
    }
    if (!tile.parent.rendered) {
      return '';
    }
    let remove = false;
    if (!tile.rendered) {
      tile.render();
      remove = true;
    }
    tile.plainText = strings.plainText(tile.$container.html()) || '';
    if (remove) {
      tile.remove();
    }
    tile.on('propertyChange', this._tilePropertyChangeHandler);
    return tile.plainText;
  }

  _onHtmlTileContentChange(event) {
    delete event.source.plainText;
  }

  _onTilePropertyChange(event) {
    if (event.propertyName === 'filterAccepted') {
      return;
    }
    delete event.source.plainText;
    event.source.off('propertyChange', this._tilePropertyChangeHandler);
  }
}
