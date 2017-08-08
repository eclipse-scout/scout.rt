/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * Widget representing an icon. It may be a font icon or an image icon. Depending on the type, either a span or an img tag will be rendered.
 * <p>
 * See also jquery-scout.icon/appendIcon. Main difference to these implementations is that the image loading will invalidate the layout by using {@link scout.Image}.
 */
scout.Icon = function() {
  scout.Icon.parent.call(this);

  /** @type {scout.IconDesc} */
  this.iconDesc = null;

  /**
   * Is set if the icon is rendered and an image, it is not set if it is a font icon
   * @type {scout.Inage}
   */
  this.image = null;
  this.prepend = false;
};
scout.inherits(scout.Icon, scout.Widget);

scout.Icon.prototype._init = function(model) {
  scout.Icon.parent.prototype._init.call(this, model);
  this._setIconDesc(this.iconDesc);
};

scout.Icon.prototype._renderProperties = function() {
  scout.Icon.parent.prototype._renderProperties.call(this);
  this._renderIconDesc();
};

/**
 * Accepts either an iconId as string or an {@link scout.IconDesc}.
 * @param {(string|scout.IconDesc)} icon
 */
scout.Icon.prototype.setIconDesc = function(iconDesc) {
  this.setProperty('iconDesc', iconDesc);
};

scout.Icon.prototype._setIconDesc = function(iconDesc) {
  iconDesc = scout.IconDesc.ensure(iconDesc);
  this._setProperty('iconDesc', iconDesc);
};

scout.Icon.prototype._renderIconDesc = function() {
  this._removeFontIcon();
  this._removeImageIcon();

  if (!this.iconDesc) {
    this._renderFontIcon(null);
    return;
  }

  if (this.iconDesc.isFontIcon()) {
    this._renderFontIcon();
  } else {
    this._renderImageIcon();
  }
};

/**
 * @param {scout.IconDesc} icon
 */
scout.Icon.prototype._renderFontIcon = function() {
  this.$container = this.$parent.appendIcon(this.iconDesc);
  if (this.prepend) {
    this.$container.prependTo(this.$parent);
  }
};

scout.Icon.prototype._removeFontIcon = function() {
  if (this.$container) {
    this.$container.remove();
    this.$container = null;
  }
};

scout.Icon.prototype._renderImageIcon = function() {
  if (this.image) {
    return;
  }
  this.image = scout.create('Image', {
    parent: this,
    imageUrl: this.iconDesc.iconUrl,
    cssClass: 'icon image-icon',
    prepend: this.prepend
  });
  this.image.render(this.$parent);
  this.image.one('destroy', function() {
    this.image = null;
  }.bind(this));
  this.image.on('load error', function(event) {
    // propagate event
    this.trigger(event.type, event);
  }.bind(this));
  this.$container = this.image.$container;
};

scout.Icon.prototype._removeImageIcon = function() {
  if (this.image) {
    this.image.destroy();
  }
};
