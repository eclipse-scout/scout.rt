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
scout.styles = {

  /**
   * Generates an invisible div and appends it to the body, only once. The same div will be reused on subsequent calls.
   * Adds the given css class to that element and returns a style object containing the values for every given property.
   * The style is cached. Subsequent calls with the same css class will return the same style object.
   */
  get: function(cssClass, properties) {
    var key = cssClass;
    if (!scout.styles.styleMap) {
      scout.styles.styleMap = {};
    }

    var style = scout.styles.styleMap[key];
    if (style) {
      return style;
    }
    style = {};

    var elem = scout.styles.element;
    if (!elem) {
      elem = window.document.createElement('div');
      elem.style.display = 'none';
      window.document.body.appendChild(elem);
      scout.styles.element = elem;
    }

    elem.className = cssClass;
    var computedStyle = window.getComputedStyle(elem);

    properties = scout.arrays.ensure(properties);
    properties.forEach(function(property) {
      style[property] = computedStyle[property];
    });

    scout.styles.styleMap[key] = style;
    return style;
  },

  /**
   * Creates an rgb object based on the given rgb string with the format rgb(0, 0, 0).
   */
  rgb: function(rgbString) {
    var rgb = rgbString.replace(/\s/g, '').match(/^rgba?\(([0-9]+),([0-9]+),([0-9]+)/i);
    return {
      red: parseInt(rgb[1], 10),
      green: parseInt(rgb[2], 10),
      blue: parseInt(rgb[3], 10)
    };
  }

};
