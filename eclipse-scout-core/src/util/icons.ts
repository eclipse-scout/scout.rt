/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {IconDesc, strings} from '../index';
import $ from 'jquery';

export const icons = {
  /* These icon ids can either be used directly using JavaScript.
   * Or in a model file using the syntax ${iconId:ID}. */

  /* default font icons (sans-serif, arial) */
  TABLE_SORT_ASC: 'font:\u2191',
  TABLE_SORT_DESC: 'font:\u2193',

  /* scoutIcons: custom icons */
  EXCLAMATION_MARK_BOLD: 'font:\uE060',
  EXCLAMATION_MARK_CIRCLE: 'font:\uE001',
  INFO: 'font:\uE002',
  CALENDAR: 'font:\uE029',
  FILE: 'font:\uE003',
  CLOCK: 'font:\uE004',
  CHECKED_BOLD: 'font:\uE005',
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
  GEAR: 'font:\uE031',
  STAR: 'font:\uE02D',
  STAR_MARKED: 'font:\uE02E',
  STAR_BOLD: 'font:\uE032',
  STAR_SOLID: 'font:\uE033',
  PERSON_SOLID: 'font:\uE034',
  SEARCH: 'font:\uE02A',
  FOLDER: 'font:\uE02B',
  SUM: 'font:\uE02C',
  AVG: 'font:\uE03A',
  MAX: 'font:\uE039',
  MAX_BOLD: 'font:\uE027',
  MIN: 'font:\uE038',
  MIN_BOLD: 'font:\uE028',
  EXPAND_ALL: 'font:\uE036',
  COLLAPSE_ALL: 'font:\uE037',
  COLLAPSE: 'font:\uE600',
  ELLIPSIS_V: 'font:\uE041',
  SLIPPERY: 'font:\uE044',
  REMOVE: 'font:\uE035',
  REMOVE_BOLD: 'font:\uE045',
  PENCIL: 'font:\uE02F',
  PENCIL_UNDERLINE_SOLID: 'font:\uE050',
  ROTATE_LEFT: 'font:\uE051',
  ROTATE_RIGHT: 'font:\uE052',
  HOURGLASS: 'font:\uE053',
  DIAGRAM_AREA: 'font:\uE070',
  DIAGRAM_BAR: 'font:\uE071',
  DIAGRAM_BARS_HORIZONTAL: 'font:\uE072',
  DIAGRAM_BARS_VERTICAL: 'font:\uE073',
  DIAGRAM_DOUGHNUT: 'font:\uE074',
  DIAGRAM_LINE: 'font:\uE075',
  DIAGRAM_LINE_ANGULAR: 'font:\uE076',
  DIAGRAM_LINE_SMOOTH: 'font:\uE077',
  DIAGRAM_PIE: 'font:\uE078',
  DIAGRAM_RADAR: 'font:\uE079',
  DIAGRAM_SCATTER: 'font:\uE07A',

  /* font awesome icons */
  CHEVRON_LEFT_BOLD: 'font:\uF053',
  CHEVRON_RIGHT_BOLD: 'font:\uF054',
  CHEVRON_UP_BOLD: 'font:\uF077',
  CHEVRON_DOWN_BOLD: 'font:\uF078',
  ARROW_RIGHT_BOLD: 'font:\uF061',
  PLUS_BOLD: 'font:\uF067',
  MINUS_BOLD: 'font:\uF068',
  SQUARE_BOLD: 'font:\uF0C8',
  CARET_DOWN: 'font:\uF0D7',
  CARET_UP: 'font:\uF0D8',
  CARET_LEFT: 'font:\uF0D9',
  CARET_RIGHT: 'font:\uF0DA',
  ANGLE_LEFT_BOLD: 'font:\uF104',
  ANGLE_RIGHT_BOLD: 'font:\uF105',
  ANGLE_UP_BOLD: 'font:\uF106',
  ANGLE_DOWN_BOLD: 'font:\uF107',
  CIRCLE_BOLD: 'font:\uF111',
  LONG_ARROW_DOWN_BOLD: 'font:\uF175',
  LONG_ARROW_UP_BOLD: 'font:\uF176',
  LONG_ARROW_LEFT_BOLD: 'font:\uF177',
  LONG_ARROW_RIGHT_BOLD: 'font:\uF178',

  ICON_ID_REGEX: /^\${iconId:([^}]+)}$/,

  /**
   * Returns an {@link IconDesc} object with structured info contained in the iconId string.
   */
  parseIconId(iconId: string): IconDesc {
    let icon = new IconDesc();

    if (strings.startsWith(iconId, 'font:')) {
      icon.iconType = IconDesc.IconType.FONT_ICON;
      iconId = iconId.substring(5); // remove 'font:' prefix
      if (strings.countCodePoints(iconId) === 1) {
        // default icon-font scoutIcons
        icon.font = IconDesc.DEFAULT_FONT;
        icon.iconCharacter = iconId;
      } else {
        let tmp = iconId.split(' ');
        icon.font = tmp[0];
        icon.iconCharacter = tmp[1];
      }
    } else {
      icon.iconType = IconDesc.IconType.BITMAP;
      icon.iconUrl = iconId;
    }

    return icon;
  },

  /**
   * Resolves the value of an iconId property, where the value can be a reference to
   * an icon constant in these formats:
   *
   * - `${iconId:ANGLE_UP}` references the constant `scout.icons.ANGLE_UP`
   * - `${iconId:foo.BAR}` references the constant `foo.icons.BAR`
   *
   * If the specified namespace does not have an `icons` object, an error will be thrown.
   */
  resolveIconId(value: string): string {
    let match = icons.ICON_ID_REGEX.exec(value);
    if (match) {
      let iconId = match[1];
      let parts = iconId.split('.');
      if (parts.length === 1) {
        // look for icon in global object scout.icons.[0]
        value = window['scout']['icons'][parts[0]];
      } else if (parts.length === 2) {
        // look for icon in global object [0].icons.[1]
        value = window[parts[0]]['icons'][parts[1]];
      } else {
        $.log.warn('Invalid iconId: ' + value);
      }
    }
    return value;
  },

  /**
   * Converts the value of the specified property from the form `'${iconId:...}'` into a resolved iconId.
   * The value remains unchanged if it does not match the {@linkplain icons#resolveIconId supported format}.
   *
   * @param object non-null object having an icon property which may contain an iconId
   * @param iconProperty name of the property on the given object which may contain an iconId. By default, `'iconId'` is used as property name.
   */
  resolveIconProperty(object: object, iconProperty?: string) {
    iconProperty = iconProperty || 'iconId';
    let value = object[iconProperty];
    let newValue = icons.resolveIconId(value);
    if (newValue !== value) {
      object[iconProperty] = newValue;
    }
  }
};
