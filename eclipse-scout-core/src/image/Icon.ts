/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HtmlComponent, IconDesc, IconEventMap, IconModel, Image, InitModelOf, scout, Widget} from '../index';

/**
 * Widget representing an icon. It may be a font icon or an image icon. Depending on the type, either a span or an img tag will be rendered.
 * <p>
 * See also {@link JQuery.icon} and {@link JQuery.appendIcon}. Main difference to these implementations is that the image loading will invalidate the layout by using {@link Image}.
 */
export class Icon extends Widget implements IconModel {
  declare model: IconModel;
  declare eventMap: IconEventMap;
  declare self: Icon;

  autoFit: boolean;
  iconDesc: IconDesc;
  /**
   * Is set if the icon is rendered and an image, it is not set if it is a font icon
   */
  image: Image;
  prepend: boolean;

  constructor() {
    super();

    this.autoFit = false;
    this.iconDesc = null;
    this.image = null;
    this.prepend = false;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._setIconDesc(this.iconDesc);
  }

  protected override _render() {
    this._renderIconDesc(); // Must not be in _renderProperties because it creates $container -> properties like visible etc. need to be rendered afterwards
  }

  /**
   * Accepts either an iconId as string or an {@link IconDesc}.
   */
  setIconDesc(iconDesc: IconDesc | string) {
    this.setProperty('iconDesc', iconDesc);
  }

  protected _setIconDesc(iconDesc: IconDesc | string) {
    iconDesc = IconDesc.ensure(iconDesc);
    this._setProperty('iconDesc', iconDesc);
  }

  protected _renderIconDesc() {
    this._removeFontIcon();
    this._removeImageIcon();

    if (!this.iconDesc || this.iconDesc.isFontIcon()) {
      this._renderFontIcon();
    } else {
      this._renderImageIcon();
    }
    if (!this.rendering) {
      this._renderProperties();
    }
    this.invalidateLayoutTree();
  }

  protected _renderFontIcon() {
    this.$container = this.$parent.appendIcon(this.iconDesc);
    if (this.prepend) {
      this.$container.prependTo(this.$parent);
    }
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
  }

  protected _removeFontIcon() {
    if (this.$container) {
      this.$container.remove();
      this.$container = null;
    }
  }

  protected _renderImageIcon() {
    if (this.image) {
      return;
    }
    this.image = scout.create(Image, {
      parent: this,
      imageUrl: this.iconDesc.iconUrl,
      cssClass: 'icon image-icon',
      autoFit: this.autoFit,
      prepend: this.prepend
    });
    this.image.render(this.$parent);
    this.image.one('destroy', () => {
      this.image = null;
    });
    this.image.on('load error', event => {
      // propagate event
      this.trigger(event.type, event);
    });
    this.$container = this.image.$container;
    this.htmlComp = this.image.htmlComp;
  }

  protected _removeImageIcon() {
    if (this.image) {
      this.image.destroy();
      this.image = null;
    }
  }

  /**
   * Delegates to {@link Image.setAutoFit}, but only if Icon is an image. Beside updating the autoFit property, this method has no effect if the icon is a font-icon.
   */
  setAutoFit(autoFit: boolean) {
    this.setProperty('autoFit', autoFit);
    if (this.image) {
      this.image.setAutoFit(autoFit);
    }
  }
}
