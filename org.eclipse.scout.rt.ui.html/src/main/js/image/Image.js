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
import {ImageLayout} from '../index';
import {HtmlComponent} from '../index';
import {Widget} from '../index';

export default class Image extends Widget {

constructor() {
  super();
  this.autoFit = false;
  this.imageUrl = null;
  this.prepend = false;
}


_render() {
  this.$container = this.$parent.makeElement('<img>', 'image')
    .on('load', this._onImageLoad.bind(this))
    .on('error', this._onImageError.bind(this));

  if (this.prepend) {
    this.$container.prependTo(this.$parent);
  } else {
    this.$container.appendTo(this.$parent);
  }

  this.htmlComp = HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new ImageLayout(this));
  this.htmlComp.pixelBasedSizing = false;
}

_renderProperties() {
  super._renderProperties();
  this._renderImageUrl();
  this._renderAutoFit();
}

_remove() {
  super._remove();
  this.htmlComp = null;
}

setImageUrl(imageUrl) {
  this.setProperty('imageUrl', imageUrl);
}

_renderImageUrl() {
  this.$container.attr('src', this.imageUrl);

  // Hide <img> when it has no content (event 'load' will not fire)
  if (!this.imageUrl) {
    this.$container.addClass('empty').removeClass('broken');
  }
}

setAutoFit(autoFit) {
  this.setProperty('autoFit', autoFit);
}

_renderAutoFit() {
  this.$container.toggleClass('autofit', this.autoFit);
}

_onImageLoad(event) {
  if (!this.rendered) { // check needed, because this is an async callback
    return;
  }
  this.$container.removeClass('empty broken');
  this.invalidateLayoutTree();
  this.trigger('load');
}

_onImageError(event) {
  if (!this.rendered) { // check needed, because this is an async callback
    return;
  }
  this.$container.addClass('empty broken');
  this.invalidateLayoutTree();
  this.trigger('error');
}
}
