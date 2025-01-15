/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
const transform = require('./tranform');
const fs = require('fs');
const path = require('path');

function runTests() {
  const rootFixture = './test/fixtures/TestDo.ts';
  transform([rootFixture]);

  const fixtures = [rootFixture, './test/fixtures/Test2Do.ts'];
  const failedFixtures = [];
  for (const fixture of fixtures) {
    const transpiled = fixture.substring(0, fixture.length - 3) + '.js';
    const expected = './test/expectations/' + path.basename(transpiled);
    const transpiledContent = fs.readFileSync(transpiled, 'utf-8').toString().trim();
    const expectedContent = fs.readFileSync(expected, 'utf-8').toString().trim();
    if (transpiledContent !== expectedContent) {
      failedFixtures.push(fixtures);
    }
  }
  if (failedFixtures.length) {
    throw new Error('The following fixtures transpiled to unexpected content: [\n' + failedFixtures.join('\n') + '\n]\nCompare transpiled file with expectation and fix differences!\n');
  }
}

runTests();
