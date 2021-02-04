/*
 * Copyright (c) 2014-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {colorSchemes, objects, strings} from '../index';

const ColorSchemeId = {
  DEFAULT: 'default',
  ALTERNATIVE: 'alternative',
  RAINBOW: 'rainbow'
};

/**
 * Converts the given colorScheme that may be a string into an object.
 */
export function ensureColorScheme(colorScheme, tile) {
  let colorSchemeObj = {};
  if (colorScheme && typeof colorScheme === 'object') {
    colorSchemeObj = colorScheme;
  } else if (typeof colorScheme === 'string') {
    // Split up colorScheme in two individual parts ("scheme" and "inverted").
    // This information is then used when rendering the color scheme.
    if (strings.startsWith(colorScheme, ColorSchemeId.DEFAULT)) {
      colorSchemeObj.scheme = ColorSchemeId.DEFAULT;
    } else if (strings.startsWith(colorScheme, ColorSchemeId.ALTERNATIVE)) {
      colorSchemeObj.scheme = ColorSchemeId.ALTERNATIVE;
    } else if (strings.startsWith(colorScheme, ColorSchemeId.RAINBOW)) {
      colorSchemeObj.scheme = ColorSchemeId.RAINBOW;
    } else {
      colorSchemeObj.scheme = strings.removeSuffix(colorScheme, '-inverted');
    }
    colorSchemeObj.inverted = strings.endsWith(colorScheme, '-inverted');
  }
  if (!objects.isNullOrUndefined(tile)) {
    colorSchemeObj.tile = tile;
  }
  return colorSchemeObj;
}

/**
 * Toggles the css classes of the given colorScheme on the given $container. Custom colorSchemes are not handled.
 *
 * @param {$} $container
 */
export function toggleColorSchemeClasses($container, colorScheme) {
  if (!$container || !colorScheme) {
    return;
  }
  colorScheme = ensureColorScheme(colorScheme);
  $container.toggleClass('color-alternative', (colorScheme.scheme === colorSchemes.ColorSchemeId.ALTERNATIVE));
  $container.toggleClass('color-rainbow', (colorScheme.scheme === colorSchemes.ColorSchemeId.RAINBOW));
  $container.toggleClass('inverted', !!colorScheme.inverted);
  $container.toggleClass('tile', !!colorScheme.tile);
}

/**
 * Get the css classes of the given colorScheme.
 */
export function getCssClasses(colorScheme) {
  let cssClasses = [];
  if (!colorScheme) {
    return cssClasses;
  }
  colorScheme = ensureColorScheme(colorScheme);
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

export default {
  ColorSchemeId,
  ensureColorScheme,
  toggleColorSchemeClasses,
  getCssClasses
};
