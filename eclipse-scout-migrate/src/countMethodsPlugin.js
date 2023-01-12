/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
