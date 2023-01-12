/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
