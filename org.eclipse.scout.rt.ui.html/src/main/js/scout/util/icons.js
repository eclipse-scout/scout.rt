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
scout.icons = {

  ANGLE_DOWN: 'font:\uF107',
  ANGLE_UP: 'font:\uF106',
  OUTLINE: 'font:\uE043',
  ELLIPSIS_V: 'font:\uE040',
  COLLAPSE: 'font:\uE600',
  HOME: 'font:\uE601',
  SUM: 'font:\ue025',
  AVG: 'font:\ue026',
  MAX: 'font:\ue027',
  MIN: 'font:\ue028',

  /**
   * Returns an Icon object with structured info contained in the iconId string.
   */
  parseIconId: function(iconId) {
    if (!iconId) {
      return null;
    }
    var icon = new scout.Icon();
    if (scout.strings.startsWith(iconId, 'font:')) {
      icon.iconType = scout.Icon.IconType.FONT_ICON;
      iconId = iconId.substr(5);
      if (iconId.length === 1) {
        // default icon-font scoutIcons
        icon.font = scout.Icon.DEFAULT_FONT;
        icon.iconCharacter = iconId;
      } else {
        var tmp = iconId.split(' ');
        icon.font = tmp[0];
        icon.iconCharacter = tmp[1];
      }
    } else {
      icon.iconType = scout.Icon.IconType.BITMAP;
      icon.iconUrl = iconId;
    }
    return icon;
  }
};

scout.Icon = function() {
  this.iconType;
  this.font;
  this.iconCharacter;
  this.iconUrl;
};

scout.Icon.IconType = {
  FONT_ICON: 0,
  BITMAP: 1
};

scout.Icon.DEFAULT_FONT = 'scoutIcons';

/**
 * Returns a CSS class based on the used font-name.
 */
scout.Icon.prototype.cssClass = function() {
  if (this.isFontIcon() && this.font !== scout.Icon.DEFAULT_FONT) {
    return 'font-' + this.font;
  } else {
    return '';
  }
};

/**
 * Returns a CSS class string to be used with JQuery.add/removeClass().
 */
scout.Icon.prototype.appendCssClass = function(cssClass) {
  return scout.strings.join(' ', cssClass, this.cssClass());
};

scout.Icon.prototype.isFontIcon = function() {
  return this.iconType === scout.Icon.IconType.FONT_ICON;
};

scout.Icon.prototype.isBitmap = function() {
  return this.iconType === scout.Icon.IconType.BITMAP;
};
