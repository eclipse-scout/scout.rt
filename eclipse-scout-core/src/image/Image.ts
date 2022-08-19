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
import {Device, HtmlComponent, ImageLayout, Widget} from '../index';
import ImageModel from './ImageModel';

export default class Image extends Widget implements ImageModel {
  autoFit: boolean;
  imageUrl: string;
  prepend: boolean;

  constructor() {
    super();
    this.autoFit = false;
    this.imageUrl = null;
    this.prepend = false;
  }

  init(model: ImageModel) { // FIXME TS better use generic?
    super.init(model);
  }

  protected _render() {
    this.$container = this.$parent.makeElement('<img>', 'image')
      .addDeviceClass()
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

  protected _renderProperties() {
    super._renderProperties();
    this._renderImageUrl();
    this._renderAutoFit();
  }

  protected _remove() {
    super._remove();
    this.htmlComp = null;
  }

  setImageUrl(imageUrl: string) {
    this.setProperty('imageUrl', imageUrl);
  }

  protected _renderImageUrl() {
    this.$container.attr('src', this.imageUrl);

    // Hide <img> when it has no content (event 'load' will not fire)
    if (!this.imageUrl) {
      this.$container.addClass('empty').removeClass('broken');
    }
  }

  setAutoFit(autoFit: boolean) {
    this.setProperty('autoFit', autoFit);
  }

  protected _renderAutoFit() {
    this.$container.toggleClass('autofit', this.autoFit);
  }

  protected _onImageLoad(event) {
    if (!this.rendered) { // check needed, because this is an async callback
      return;
    }
    this._ensureImageLayout();
    this.$container.removeClass('empty broken');
    this.invalidateLayoutTree();
    this.trigger('load');
  }

  /**
   * This function is used to work around a bug in Chrome. Chrome calculates a wrong aspect ratio
   * under certain conditions. For details: https://bugs.chromium.org/p/chromium/issues/detail?id=950881
   * The workaround sets a CSS attribute which forces Chrome to revalidate its internal layout.
   */
  protected _ensureImageLayout() {
    if (!Device.get().isChrome()) {
      return;
    }
    this.$container.addClass('chrome-fix');
    setTimeout(() => {
      if (this.rendered) {
        this.$container.removeClass('chrome-fix');
      }
    });
  }

  protected _onImageError(event) {
    if (!this.rendered) { // check needed, because this is an async callback
      return;
    }
    this.$container.addClass('empty broken');
    this.invalidateLayoutTree();
    this.trigger('error');
  }
}
