/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Device, HtmlComponent, ImageEventMap, ImageLayout, ImageModel, Widget} from '../index';

export class Image extends Widget implements ImageModel {
  declare model: ImageModel;
  declare eventMap: ImageEventMap;
  declare self: Image;

  autoFit: boolean;
  imageUrl: string;
  prepend: boolean;
  altText: string;

  constructor() {
    super();
    this.autoFit = false;
    this.imageUrl = null;
    this.prepend = false;
    this.altText = '';
  }

  protected override _render() {
    this.$container = this.$parent.makeElement('<img>', 'image')
      .attr('alt', this.altText)
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

  protected override _renderProperties() {
    super._renderProperties();
    this._renderImageUrl();
    this._renderAutoFit();
  }

  protected override _remove() {
    super._remove();
    this.htmlComp = null;
  }

  setImageUrl(imageUrl: string) {
    this.setProperty('imageUrl', imageUrl);
  }

  setAltText(altText: string) {
    this.setProperty('altText', altText);
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

  protected _onImageLoad(event: JQuery.TriggeredEvent) {
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

  protected _onImageError(event: JQuery.TriggeredEvent) {
    if (!this.rendered) { // check needed, because this is an async callback
      return;
    }
    this.$container.addClass('empty broken');
    this.invalidateLayoutTree();
    this.trigger('error');
  }
}
