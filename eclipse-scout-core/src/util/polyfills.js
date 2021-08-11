/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

/* eslint-disable no-extend-native */

export function install(window) {
  _installPolyfillMath(window);
  _installPolyfillSafeInteger(window);
}

export function _installPolyfillMath(window) {
  if (!window.Math.sign) {
    Object.defineProperty(window.Math, 'sign', {
      value: x => {
        x = +x; // convert to a number
        if (x === 0 || isNaN(x)) {
          return x;
        }
        return x > 0 ? 1 : -1;
      }
    });
  }
}

/**
 * IE support for Number.MAX_SAFE_INTEGER and Number.MIN_SAFE_INTEGER
 */
export function _installPolyfillSafeInteger(window) {
  if (!window.Number.MAX_SAFE_INTEGER) {
    Object.defineProperty(window.Number, 'MAX_SAFE_INTEGER', {
      value: 9007199254740991 // Math.pow(2, 53) - 1;
    });
  }
  if (!window.Number.MIN_SAFE_INTEGER) {
    Object.defineProperty(window.Number, 'MIN_SAFE_INTEGER', {
      value: -9007199254740991 // −(Math.pow(2, 53) - 1);
    });
  }
}

export default {
  install
};
