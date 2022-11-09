/**
 * @type import('ts-migrate-server').Plugin<unknown>
 */
import {lfToCrlf} from './common.js';

/**
 * @type import('ts-migrate-server').Plugin<{}>
 */
const convertToCRLFPlugin = {
  name: 'convert-to-crlf',

  run({text}) {
    // declareMissingClassPropertiesPlugin needs \r\n to work -> ensure crlf line breaks. The next regex also depends on that.
    return lfToCrlf(text);
  }
};

export default convertToCRLFPlugin;
