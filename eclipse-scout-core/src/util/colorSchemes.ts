/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {objects, strings} from '../index';

export interface ColorScheme {
  scheme?: string;
  inverted?: boolean;
  tile?: boolean;
}

export const colorSchemes = {
  ColorSchemeId: {
    DEFAULT: 'default',
    ALTERNATIVE: 'alternative',
    RAINBOW: 'rainbow'
  } as const,

  /**
   * Converts the given colorScheme that may be a string into an object.
   */
  ensureColorScheme(colorScheme: ColorScheme | string, tile?: boolean): ColorScheme {
    let colorSchemeObj: ColorScheme = {};
    if (colorScheme && typeof colorScheme === 'object') {
      colorSchemeObj = colorScheme;
    } else if (typeof colorScheme === 'string') {
      // Split up colorScheme in two individual parts ("scheme" and "inverted").
      // This information is then used when rendering the color scheme.
      if (strings.startsWith(colorScheme, colorSchemes.ColorSchemeId.DEFAULT)) {
        colorSchemeObj.scheme = colorSchemes.ColorSchemeId.DEFAULT;
      } else if (strings.startsWith(colorScheme, colorSchemes.ColorSchemeId.ALTERNATIVE)) {
        colorSchemeObj.scheme = colorSchemes.ColorSchemeId.ALTERNATIVE;
      } else if (strings.startsWith(colorScheme, colorSchemes.ColorSchemeId.RAINBOW)) {
        colorSchemeObj.scheme = colorSchemes.ColorSchemeId.RAINBOW;
      } else {
        colorSchemeObj.scheme = strings.removeSuffix(colorScheme, '-inverted');
      }
      colorSchemeObj.inverted = strings.endsWith(colorScheme, '-inverted');
    }
    if (!objects.isNullOrUndefined(tile)) {
      colorSchemeObj.tile = tile;
    }
    return colorSchemeObj;
  },

  /**
   * Toggles the css classes of the given colorScheme on the given $container. Custom colorSchemes are not handled.
   *
   * @param {$} $container
   */
  toggleColorSchemeClasses($container: JQuery, scheme: string | ColorScheme) {
    if (!$container || !scheme) {
      return;
    }
    let colorScheme = colorSchemes.ensureColorScheme(scheme);
    $container.toggleClass('color-alternative', (colorScheme.scheme === colorSchemes.ColorSchemeId.ALTERNATIVE));
    $container.toggleClass('color-rainbow', (colorScheme.scheme === colorSchemes.ColorSchemeId.RAINBOW));
    $container.toggleClass('inverted', !!colorScheme.inverted);
    $container.toggleClass('tile', !!colorScheme.tile);
  },

  /**
   * Get the css classes of the given colorScheme.
   */
  getCssClasses(scheme: ColorScheme | string): string[] {
    let cssClasses: string[] = [];
    if (!scheme) {
      return cssClasses;
    }
    let colorScheme = colorSchemes.ensureColorScheme(scheme);
    if (colorScheme.scheme === colorSchemes.ColorSchemeId.ALTERNATIVE) {
      cssClasses.push('color-alternative');
    } else if (colorScheme.scheme === colorSchemes.ColorSchemeId.RAINBOW) {
      cssClasses.push('color-rainbow');
    } else if (colorScheme.scheme) {
      cssClasses.push(colorScheme.scheme);
    }
    if (colorScheme.inverted) {
      cssClasses.push('inverted');
    }
    if (colorScheme.tile) {
      cssClasses.push('tile');
    }
    return cssClasses;
  }
};
