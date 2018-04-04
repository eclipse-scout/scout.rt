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
scout.icons = {

  /* default font icons (sans-serif, arial) */
  TABLE_SORT_ASC: 'font:\u2191',
  TABLE_SORT_DESC: 'font:\u2193',

  /* scoutIcons: custom icons */
  EXCLAMATION_MARK: 'font:\uE001',
  INFO: 'font:\uE002',
  CALENDAR: 'font:\uE003',
  CLOCK: 'font:\uE004',
  CHECKED: 'font:\uE005',
  GROUP: 'font:\uE006',
  GROUP_PLUS: 'font:\uE007',
  ANGLE_DOUBLE_LEFT: 'font:\uE010',
  ANGLE_DOUBLE_RIGHT: 'font:\uE011',
  ANGLE_LEFT: 'font:\uE012',
  ANGLE_RIGHT: 'font:\uE013',
  ANGLE_DOWN: 'font:\uE014',
  ANGLE_UP: 'font:\uE015',
  LONG_ARROW_DOWN: 'font:\uE016',
  LONG_ARROW_UP: 'font:\uE017',
  LONG_ARROW_DOWN_PLUS: 'font:\uE018',
  LONG_ARROW_UP_PLUS: 'font:\uE019',
  MINUS: 'font:\uE01A',
  PLUS: 'font:\uE01B',
  LIST: 'font:\uE01C',
  TARGET: 'font:\uE020',
  WORLD: 'font:\uE021',
  CHART: 'font:\uE022',
  GRAPH_SOLID: 'font:\uE023',
  CATEGORY_SOLID: 'font:\uE024',
  GEAR: 'font:\uE031',
  STAR: 'font:\uE02D',
  STAR_MARKED: 'font:\uE02E',
  STAR_BOLD: 'font:\uE032',
  STAR_SOLID: 'font:\uE033',
  PERSON_SOLID: 'font:\uE034',
  VERTICAL_DOTS: 'font:\uE040',
  SEARCH: 'font:\uE02A',
  SEARCH_BOLD: 'font:\uE042',
  FOLDER: 'font:\uE02B',
  FOLDER_BOLD: 'font:\uE043',
  SUM_BOLD: 'font:\uE025',
  AVG_BOLD: 'font:\uE026',
  MAX_BOLD: 'font:\uE027',
  MIN_BOLD: 'font:\uE028',
  COLLAPSE: 'font:\uE600',
  ELLIPSIS_V_BOLD: 'font:\uE040',
  ELLIPSIS_V: 'font:\uE041',
  REMOVE_LARGE: 'font:\uE045',
  PENCIL: 'font:\uE02F',
  PENCIL_BOLD: 'font:\uE04B',
  PENCIL_SOLID: 'font:\uE04F',

  /* font awesome icons */
  ROTATE_LEFT_BOLD: 'font:\uF0E2',
  ROTATE_RIGHT_BOLD: 'font:\uF01E',
  CHEVRON_LEFT_BOLD: 'font:\uF053',
  CHEVRON_RIGHT_BOLD: 'font:\uF054',
  CHEVRON_UP_BOLD: 'font:\uF077',
  CHEVRON_DOWN_BOLD: 'font:\uF078',
  ARROW_RIGHT_BOLD: 'font:\uF061',
  PLUS_BOLD: 'font:\uF067',
  MINUS_BOLD: 'font:\uF068',
  SQUARE_BOLD: 'font:\uF0C8',
  MENU_BOLD: 'font:\uF0C9',
  LIST_UL_BOLD: 'font:\uF0CA',
  LIST_OL_BOLD: 'font:\uF0CB',
  CARET_DOWN: 'font:\uF0D7',
  CARET_UP: 'font:\uF0D8',
  CARET_LEFT: 'font:\uF0D9',
  CARET_RIGHT: 'font:\uF0DA',
  ANGLE_DOUBLE_LEFT_BOLD: 'font:\uF100',
  ANGLE_DOUBLE_RIGHT_BOLD: 'font:\uF101',
  ANGLE_DOUBLE_UP_BOLD: 'font:\uF102',
  ANGLE_DOUBLE_DOWN_BOLD: 'font:\uF103',
  ANGLE_LEFT_BOLD: 'font:\uF104',
  ANGLE_RIGHT_BOLD: 'font:\uF105',
  ANGLE_UP_BOLD: 'font:\uF106',
  ANGLE_DOWN_BOLD: 'font:\uF107',
  CIRCLE_BOLD: 'font:\uF111',
  FILE_SOLID: 'font:\uF15B',
  LONG_ARROW_DOWN_BOLD: 'font:\uF175',
  LONG_ARROW_UP_BOLD: 'font:\uF176',
  LONG_ARROW_LEFT_BOLD: 'font:\uF177',
  LONG_ARROW_RIGHT_BOLD: 'font:\uF178',

  // Other constants
  ICON_ID_REGEX: /\$\{iconId\:([a-zA-Z0-9_\.]*)\}/,

  /**
   * Returns an {@link scout.IconDesc} object with structured info contained in the iconId string.
   */
  parseIconId: function(iconId) {
    var icon = new scout.IconDesc();

    if (scout.strings.startsWith(iconId, 'font:')) {
      icon.iconType = scout.IconDesc.IconType.FONT_ICON;
      iconId = iconId.substr(5);
      if (scout.strings.countCodePoints(iconId) === 1) {
        // default icon-font scoutIcons
        icon.font = scout.IconDesc.DEFAULT_FONT;
        icon.iconCharacter = iconId;
      } else {
        var tmp = iconId.split(' ');
        icon.font = tmp[0];
        icon.iconCharacter = tmp[1];
      }
    } else {
      icon.iconType = scout.IconDesc.IconType.BITMAP;
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
