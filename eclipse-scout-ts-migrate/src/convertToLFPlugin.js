/**
 * @type import('ts-migrate-server').Plugin<unknown>
 */
import {crlfToLf} from './common.js';

/**
 * @type import('ts-migrate-server').Plugin<{}>
 */
const convertToLFPlugin = {
  name: 'convert-to-lf',

  run({text}) {
    // revert line ending style to lf
    return crlfToLf(text);
  }
};

export default convertToLFPlugin;
