/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * AI Disclosure: This file was partially AI-generated.
 * The AI-generated portions are made available under CC0-1.0
 * and not subject to the project's licence.
 *
 * SPDX-License-Identifier: EPL-2.0 and CC0-1.0
 */
const {before, describe, it} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('fs');
const path = require('path');
const transform = require('./tranform');

const FIXTURES_DIR = path.join(__dirname, 'fixtures');
const EXPECTATIONS_DIR = path.join(__dirname, 'expectations');
const ROOT_FIXTURE = path.join(FIXTURES_DIR, 'TestDo.ts');
const FIXTURES = ['TestDo', 'Test2Do'];

describe('DataObjectTransformer', () => {

  function readTrimmed(file) {
    return fs.readFileSync(file, 'utf-8').trim();
  }

  before(() => {
    assert.equal(transform.compile([ROOT_FIXTURE]), 0, 'Emit of fixtures skipped');
  });

  for (const fixture of FIXTURES) {
    describe(fixture, () => {
      const fixtureFile = path.join(FIXTURES_DIR, fixture + '.ts');
      const expectationFile = path.join(EXPECTATIONS_DIR, fixture + '.js');

      it('transforms with type information', () => {
        const actual = readTrimmed(path.join(FIXTURES_DIR, fixture + '.js'));
        assert.equal(actual, readTrimmed(expectationFile));
      });

      it('transforms without type information (transpileOnly)', () => {
        const actual = transform.transpile(fixtureFile).trim();
        assert.equal(actual, readTrimmed(expectationFile));
      });
    });
  }
});
