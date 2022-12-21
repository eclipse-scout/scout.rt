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
