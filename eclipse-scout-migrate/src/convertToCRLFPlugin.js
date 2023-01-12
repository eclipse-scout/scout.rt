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
import {lfToCrlf} from './common.js';

let filesConvertedToCrLf = new Set();
/**
 * @type import('ts-migrate-server').Plugin<{}>
 */
const convertToCRLFPlugin = {
  name: 'convert-to-crlf',

  run({text, fileName}) {
    // declareMissingClassPropertiesPlugin needs \r\n to work -> ensure crlf line breaks. The next regex also depends on that.
    if (text.indexOf('\r\n') >= 0) {
      return text;
    }
    filesConvertedToCrLf.add(fileName); // remember files for which the newlines have been changed. Required to only revert the ones that have been modified.
    return lfToCrlf(text);
  }
};

export default convertToCRLFPlugin;
export {filesConvertedToCrLf};
