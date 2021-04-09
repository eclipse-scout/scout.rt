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

export default {
  install
};
