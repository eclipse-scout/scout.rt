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
import {HtmlComponent, Tile} from '../index';

export default class HtmlTile extends Tile {

  constructor() {
    super();
    this.content = null;
  }

  _render() {
    this.$container = this.$parent.appendDiv('html-tile');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
  }

  _renderProperties() {
    super._renderProperties();
    this._renderContent();
  }

  setContent(content) {
    this.setProperty('content', content);
  }

  _renderContent() {
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

  _onImageLoad(event) {
    this.invalidateLayoutTree();
  }

  _onImageError(event) {
    this.invalidateLayoutTree();
  }
}
