/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {IconDesc, strings} from '../index';
import $ from 'jquery';

/* These icon ids can either be used directly using JavaScript.
 * Or in a JSON based model file using the syntax ${iconId:ID}. */

/* default font icons (sans-serif, arial) */
const TABLE_SORT_ASC = 'font:\u2191';
const TABLE_SORT_DESC = 'font:\u2193';

/* scoutIcons: custom icons */
const EXCLAMATION_MARK_BOLD = 'font:\uE060';
const EXCLAMATION_MARK_CIRCLE = 'font:\uE001';
const INFO = 'font:\uE002';
const CALENDAR = 'font:\uE029';
const CALENDAR_BOLD = 'font:\uE003';
const CLOCK_BOLD = 'font:\uE004';
const CHECKED_BOLD = 'font:\uE005';
const GROUP = 'font:\uE006';
const GROUP_PLUS = 'font:\uE007';
const ANGLE_DOUBLE_LEFT = 'font:\uE010';
const ANGLE_DOUBLE_RIGHT = 'font:\uE011';
const ANGLE_LEFT = 'font:\uE012';
const ANGLE_RIGHT = 'font:\uE013';
const ANGLE_DOWN = 'font:\uE014';
const ANGLE_UP = 'font:\uE015';
const LONG_ARROW_DOWN = 'font:\uE016';
const LONG_ARROW_UP = 'font:\uE017';
const LONG_ARROW_DOWN_PLUS = 'font:\uE018';
const LONG_ARROW_UP_PLUS = 'font:\uE019';
const MINUS = 'font:\uE01A';
const PLUS = 'font:\uE01B';
const LIST = 'font:\uE01C';
const TARGET = 'font:\uE020';
const WORLD = 'font:\uE021';
const CHART = 'font:\uE022';
const GEAR = 'font:\uE031';
const STAR = 'font:\uE02D';
const STAR_MARKED = 'font:\uE02E';
const STAR_BOLD = 'font:\uE032';
const STAR_SOLID = 'font:\uE033';
const PERSON_SOLID = 'font:\uE034';
const SEARCH = 'font:\uE02A';
const SEARCH_BOLD = 'font:\uE042';
const FOLDER = 'font:\uE02B';
const FOLDER_BOLD = 'font:\uE043';
const SUM = 'font:\uE02C';
const SUM_BOLD = 'font:\uE025';
const AVG = 'font:\uE03A';
const AVG_BOLD = 'font:\uE026';
const MAX = 'font:\uE039';
const MAX_BOLD = 'font:\uE027';
const MIN = 'font:\uE038';
const MIN_BOLD = 'font:\uE028';
const EXPAND_ALL = 'font:\uE036';
const COLLAPSE_ALL = 'font:\uE037';
const COLLAPSE = 'font:\uE600';
const ELLIPSIS_V = 'font:\uE041';
const REMOVE = 'font:\uE035';
const REMOVE_BOLD = 'font:\uE045';
const PENCIL = 'font:\uE02F';
const PENCIL_BOLD = 'font:\uE04B';
const PENCIL_SOLID = 'font:\uE04F';
const PENCIL_UNDERLINE_SOLID = 'font:\uE050';
const SLIPPERY = 'font:\uE044';
const DIAGRAM_AREA = 'font:\uE070';
const DIAGRAM_BAR = 'font:\uE071';
const DIAGRAM_BARS_HORIZONTAL = 'font:\uE072';
const DIAGRAM_BARS_VERTICAL = 'font:\uE073';
const DIAGRAM_DOUGHNUT = 'font:\uE074';
const DIAGRAM_LINE = 'font:\uE075';
const DIAGRAM_LINE_ANGULAR = 'font:\uE076';
const DIAGRAM_LINE_SMOOTH = 'font:\uE077';
const DIAGRAM_PIE = 'font:\uE078';
const DIAGRAM_RADAR = 'font:\uE079';
const DIAGRAM_SCATTER = 'font:\uE07A';

/* font awesome icons */
const ROTATE_LEFT_BOLD = 'font:\uF0E2';
const ROTATE_RIGHT_BOLD = 'font:\uF01E';
const CHEVRON_LEFT_BOLD = 'font:\uF053';
const CHEVRON_RIGHT_BOLD = 'font:\uF054';
const CHEVRON_UP_BOLD = 'font:\uF077';
const CHEVRON_DOWN_BOLD = 'font:\uF078';
const ARROW_RIGHT_BOLD = 'font:\uF061';
const PLUS_BOLD = 'font:\uF067';
const MINUS_BOLD = 'font:\uF068';
const SQUARE_BOLD = 'font:\uF0C8';
const CARET_DOWN = 'font:\uF0D7';
const CARET_UP = 'font:\uF0D8';
const CARET_LEFT = 'font:\uF0D9';
const CARET_RIGHT = 'font:\uF0DA';
const ANGLE_LEFT_BOLD = 'font:\uF104';
const ANGLE_RIGHT_BOLD = 'font:\uF105';
const ANGLE_UP_BOLD = 'font:\uF106';
const ANGLE_DOWN_BOLD = 'font:\uF107';
const CIRCLE_BOLD = 'font:\uF111';
const FILE_SOLID = 'font:\uF15B';
const LONG_ARROW_DOWN_BOLD = 'font:\uF175';
const LONG_ARROW_UP_BOLD = 'font:\uF176';
const LONG_ARROW_LEFT_BOLD = 'font:\uF177';
const LONG_ARROW_RIGHT_BOLD = 'font:\uF178';

// Other constants
const ICON_ID_REGEX = /\${iconId:([a-zA-Z0-9_.]*)}/;

/**
 * Returns an {@link IconDesc} object with structured info contained in the iconId string.
 */
export function parseIconId(iconId) {
  let icon = new IconDesc();

  if (strings.startsWith(iconId, 'font:')) {
    icon.iconType = IconDesc.IconType.FONT_ICON;
    iconId = iconId.substr(5);
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
}

/**
 * Resolves the value of an iconId property, where the value can contain a reference to
 * an icon constant in these formats:
 * <ul>
 *   <li><code>${iconId:ANGLE_UP}</code> references constant ANGLE_UP</li>
 *   <li><code>${iconId:foo.BAR}</code> references constant foo.icons.BAR, this is used for custom objects with icon constants</li>
 * </ul>
 * @param {string} value
 * @returns {string}
 */
export function resolveIconId(value) {
  let iconId, tmp,
    result = ICON_ID_REGEX.exec(value);
  if (result && result.length === 2) {
    iconId = result[1];
    tmp = iconId.split('.');
    if (tmp.length === 1) {
      // look for icon in [0]
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

/**
 * Resolves the value of an iconId property, where the value can contain a reference to
 * an icon constant in these formats:
 * <ul>
 *   <li><code>${iconId:ANGLE_UP}</code> references constant ANGLE_UP</li>
 *   <li><code>${iconId:foo.BAR}</code> references constant foo.icons.BAR, this is used for custom objects with icon constants</li>
 * </ul>
 * @param object object having an icon property which contains a iconId
 * @param {string} iconProperty name of the property where an iconId placeholder should be replaced by the actual iconId. By default 'iconId' is used as property name.
 * @returns {string}
 */
export function resolveIconProperty(object, iconProperty) {
  iconProperty = iconProperty || 'iconId';
  let value = object[iconProperty];
  let newValue = resolveIconId(value);
  if (newValue !== value) {
    object[iconProperty] = newValue;
  }
}

export default {
  ANGLE_DOUBLE_LEFT,
  ANGLE_DOUBLE_RIGHT,
  ANGLE_DOWN,
  ANGLE_DOWN_BOLD,
  ANGLE_LEFT,
  ANGLE_LEFT_BOLD,
  ANGLE_RIGHT,
  ANGLE_RIGHT_BOLD,
  ANGLE_UP,
  ANGLE_UP_BOLD,
  ARROW_RIGHT_BOLD,
  AVG,
  AVG_BOLD,
  CALENDAR,
  CALENDAR_BOLD,
  CARET_DOWN,
  CARET_LEFT,
  CARET_RIGHT,
  CARET_UP,
  CHART,
  CHECKED_BOLD,
  CHEVRON_DOWN_BOLD,
  CHEVRON_LEFT_BOLD,
  CHEVRON_RIGHT_BOLD,
  CHEVRON_UP_BOLD,
  CIRCLE_BOLD,
  CLOCK_BOLD,
  COLLAPSE,
  DIAGRAM_AREA,
  DIAGRAM_BAR,
  DIAGRAM_BARS_HORIZONTAL,
  DIAGRAM_BARS_VERTICAL,
  DIAGRAM_DOUGHNUT,
  DIAGRAM_LINE,
  DIAGRAM_LINE_ANGULAR,
  DIAGRAM_LINE_SMOOTH,
  DIAGRAM_PIE,
  DIAGRAM_RADAR,
  DIAGRAM_SCATTER,
  ELLIPSIS_V,
  EXCLAMATION_MARK_BOLD,
  EXCLAMATION_MARK_CIRCLE,
  FILE_SOLID,
  FOLDER,
  FOLDER_BOLD,
  GEAR,
  GROUP,
  GROUP_PLUS,
  ICON_ID_REGEX,
  INFO,
  LIST,
  LONG_ARROW_DOWN,
  LONG_ARROW_DOWN_BOLD,
  LONG_ARROW_DOWN_PLUS,
  LONG_ARROW_LEFT_BOLD,
  LONG_ARROW_RIGHT_BOLD,
  LONG_ARROW_UP,
  LONG_ARROW_UP_BOLD,
  LONG_ARROW_UP_PLUS,
  MAX,
  MAX_BOLD,
  MIN,
  MINUS,
  MINUS_BOLD,
  MIN_BOLD,
  EXPAND_ALL,
  COLLAPSE_ALL,
  PENCIL,
  PENCIL_BOLD,
  PENCIL_SOLID,
  PENCIL_UNDERLINE_SOLID,
  PERSON_SOLID,
  PLUS,
  PLUS_BOLD,
  REMOVE,
  REMOVE_BOLD,
  ROTATE_LEFT_BOLD,
  ROTATE_RIGHT_BOLD,
  SEARCH,
  SEARCH_BOLD,
  SQUARE_BOLD,
  STAR,
  STAR_BOLD,
  STAR_MARKED,
  STAR_SOLID,
  SUM,
  SUM_BOLD,
  TABLE_SORT_ASC,
  TABLE_SORT_DESC,
  TARGET,
  WORLD,
  SLIPPERY,
  parseIconId,
  resolveIconId,
  resolveIconProperty
};
