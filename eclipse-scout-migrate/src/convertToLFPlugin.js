/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

/**
 * @type import('ts-migrate-server').Plugin<unknown>
 */
import {crlfToLf} from './common.js';
import {filesConvertedToCrLf} from './convertToCRLFPlugin.js';

/**
 * @type import('ts-migrate-server').Plugin<{}>
 */
const convertToLFPlugin = {
  name: 'convert-to-lf',

  run({text, fileName}) {
    if (filesConvertedToCrLf.has(fileName)) {
      // revert line ending style to lf
      return crlfToLf(text);
    }
  }
};

export default convertToLFPlugin;
