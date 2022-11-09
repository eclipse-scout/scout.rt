import jscodeshift from 'jscodeshift';
import {methodFilter} from './common.js';

const j = jscodeshift.withParser('ts');
let total = 0;
/**
 * @type import('ts-migrate-server').Plugin<unknown>
 */
const countMethods = {
  name: 'count-methods-plugin',

  async run({text, fileName}) {
    const root = j(text);
    let count = 0;
    root.find(j.Declaration)
      .filter(path => methodFilter(j, path))
      .forEach(expression => {
        count++;
      });
    total += count;
    console.log(fileName + ': ' + count);
    console.log('Total: ' + total);
    return text;
  }
};

export default countMethods;
