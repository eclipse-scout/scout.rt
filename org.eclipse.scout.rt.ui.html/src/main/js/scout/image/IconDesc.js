/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.IconDesc = function() {
  this.iconType;
  this.font;
  this.iconCharacter;
  this.iconUrl;
};

scout.IconDesc.IconType = {
  FONT_ICON: 0,
  BITMAP: 1
};

scout.IconDesc.DEFAULT_FONT = 'scoutIcons';

/**
 * Returns a CSS class based on the used font-name.
 */
scout.IconDesc.prototype.cssClass = function() {
  if (this.isFontIcon() && this.font !== scout.IconDesc.DEFAULT_FONT) {
    return 'font-' + this.font;
  } else {
    return '';
  }
};

/**
 * Returns a CSS class string to be used with JQuery.add/removeClass().
 */
scout.IconDesc.prototype.appendCssClass = function(cssClass) {
  var additionalCssClass = this.cssClass();
  if (additionalCssClass.length > 0) {
    return cssClass + ' ' + additionalCssClass;
  } else {
    return cssClass;
  }
};

scout.IconDesc.prototype.isFontIcon = function() {
  return this.iconType === scout.IconDesc.IconType.FONT_ICON;
};

scout.IconDesc.prototype.isBitmap = function() {
  return this.iconType === scout.IconDesc.IconType.BITMAP;
};

/**
 * Ensures that the given icon is of type {@link scout.iconDesc}. It a string is provided it is assumed that the string is the iconId which may be parsed to create the {@link scout.IconDesc}.
 * @param {(string|scout.IconDesc)} icon
 */
scout.IconDesc.ensure = function(icon) {
  if (!icon) {
    return icon;
  }
  if (icon instanceof scout.IconDesc) {
    return icon;
  }
  return scout.icons.parseIconId(icon);
};
