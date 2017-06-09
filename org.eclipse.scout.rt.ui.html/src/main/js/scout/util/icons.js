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

  // Scout model icons (see AbstractIcons.java)
  TABLE_SORT_ASC: 'font:\u2191',
  TABLE_SORT_DESC: 'font:\u2193',

  EXCLAMATION_MARK: 'font:\uE001',
  INFO: 'font:\uE002',
  CALENDAR: 'font:\uE003',
  CLOCK: 'font:\uE004',
  CHECKED: 'font:\uE005',
  GROUP: 'font:\uE006',
  TARGET: 'font:\uE020',
  WORLD: 'font:\uE021',
  CHART: 'font:\uE022',
  GRAPH: 'font:\uE023',
  CATEGORY: 'font:\uE024',
  GEAR: 'font:\uE031',
  STAR: 'font:\uE032',
  PERSON: 'font:\uE034',
  VERTICAL_DOTS: 'font:\uE040',
  SEARCH: 'font:\uE042',
  FOLDER: 'font:\uE043',
  SUM: 'font:\uE025',

  REMOVE: 'font:\uF00D',
  ROTATE_RIGHT: 'font:\uF01E',
  PENCIL: 'font:\uF040',
  CHEVRON_LEFT: 'font:\uF053',
  CHEVRON_RIGHT: 'font:\uF054',
  ARROW_RIGHT: 'font:\uF061',
  PLUS: 'font:\uF067',
  MINUS: 'font:\uF068',
  CHEVRON_UP: 'font:\uF077',
  CHEVRON_DOWN: 'font:\uF078',
  SQUARE: 'font:\uF0C8',
  MENU: 'font:\uF0C9',
  LIST: 'font:\uF0CA',
  LIST_NUMBERED: 'font:\uF0CB',
  LIST_THICK: 'font:\uF00B',
  CARET_DOWN: 'font:\uF0D7',
  CARET_UP: 'font:\uF0D8',
  CARET_LEFT: 'font:\uF0D9',
  CARET_RIGHT: 'font:\uF0DA',
  ROTATE_LEFT: 'font:\uF0E2',
  ANGLE_DOUBLE_LEFT: 'font:\uF100',
  ANGLE_DOUBLE_RIGHT: 'font:\uF101',
  ANGLE_DOUBLE_UP: 'font:\uF102',
  ANGLE_DOUBLE_DOWN: 'font:\uF103',
  ANGLE_LEFT: 'font:\uF104',
  ANGLE_RIGHT: 'font:\uF105',
  ANGLE_UP: 'font:\uF106',
  ANGLE_DOWN: 'font:\uF107',
  CIRCLE: 'font:\uF111',
  FILE: 'font:\uF15B',
  LONG_ARROW_DOWN: 'font:\uF175',
  LONG_ARROW_UP: 'font:\uF176',
  LONG_ARROW_LEFT: 'font:\uF177',
  LONG_ARROW_RIGHT: 'font:\uF178',

  // UI only icons
  COLLAPSE: 'font:\uE600',
  ELLIPSIS_V: 'font:\uE040',
  AVG: 'font:\uE026',
  MAX: 'font:\uE027',
  MIN: 'font:\uE028',
  OUTLINE: 'font:\uE043',

  // Other constants
  ICON_ID_REGEX: /\$\{iconId\:([a-zA-Z0-9_\.]*)\}/,

  /**
   * Returns an Icon object with structured info contained in the iconId string.
   */
  parseIconId: function(iconId) {
    var icon = new scout.Icon();

    if (scout.strings.startsWith(iconId, 'font:')) {
      icon.iconType = scout.Icon.IconType.FONT_ICON;
      iconId = iconId.substr(5);
      if (scout.strings.countCodePoints(iconId) === 1) {
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
  },

  /**
   * Resolves the value of an iconId property, where the value can contain a reference to
   * an icon constant in these formats:
   * <ul>
   *   <li><code>${iconId:ANGLE_UP}</code> references constant scout.icon.ANGLE_UP</li>
   *   <li><code>${iconId:foo.BAR}</code> references constant foo.icon.BAR, this is used for custom objects with icon constants</li>
   * </ul>
   * @param {string} value
   * @returns {string}
   */
  resolveIconId: function(value) {
    var iconId, tmp,
      result = this.ICON_ID_REGEX.exec(value);
    if (result && result.length === 2) {
      iconId = result[1];
      tmp = iconId.split('.');
      if (tmp.length === 1) {
        // look for icon in scout.icons.[0]
        value = scout.icons[tmp];
      } else if (tmp.length === 2) {
        // look for icon in global object [0].icons.[1]
        value = window[tmp[0]].icons[tmp[1]];
      } else {
        $.log.warn('Invalid iconId: ' + value);
      }
    }
    return value;
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
  var additionalCssClass = this.cssClass();
  if (additionalCssClass.length > 0) {
    return cssClass + ' ' + additionalCssClass;
  } else {
    return cssClass;
  }
};

scout.Icon.prototype.isFontIcon = function() {
  return this.iconType === scout.Icon.IconType.FONT_ICON;
};

scout.Icon.prototype.isBitmap = function() {
  return this.iconType === scout.Icon.IconType.BITMAP;
};
