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

import assert from 'node:assert/strict';
import {describe, it} from 'node:test';
import {getDefaultValues, getUnknownOptions, type OptionDefinitions, parseArguments} from '../../scripts/util/cliArguments.ts';

describe('cliArguments', () => {

  const definitions: OptionDefinitions = {
    dryrun: {type: 'boolean', default: false},
    workspaceRoot: {type: 'string', default: null},
    keep: {type: 'number'},
    mapping: {type: 'object'}
  };

  describe('parseArguments', () => {

    it('returns positionals and no values without options', () => {
      assert.deepEqual(parseArguments(['install'], definitions), {positionals: ['install'], values: {}});
      assert.deepEqual(parseArguments([], definitions), {positionals: [], values: {}});
    });

    it('parses string options in both forms', () => {
      assert.deepEqual(parseArguments(['cmd', '--workspaceRoot=/a'], definitions).values, {workspaceRoot: '/a'});
      assert.deepEqual(parseArguments(['cmd', '--workspaceRoot', '/a'], definitions), {positionals: ['cmd'], values: {workspaceRoot: '/a'}});
      assert.deepEqual(parseArguments(['cmd', '--workspaceRoot='], definitions).values, {workspaceRoot: ''});
      assert.deepEqual(parseArguments(['cmd', '--workspaceRoot'], definitions).values, {workspaceRoot: ''});
    });

    it('parses boolean options', () => {
      assert.deepEqual(parseArguments(['cmd', '--dryrun'], definitions).values, {dryrun: true});
      assert.deepEqual(parseArguments(['cmd', '--dryrun=true'], definitions).values, {dryrun: true});
      assert.deepEqual(parseArguments(['cmd', '--dryrun=false'], definitions).values, {dryrun: false});
      assert.deepEqual(parseArguments(['cmd', '--dryrun', 'false'], definitions), {positionals: ['cmd'], values: {dryrun: false}});
      assert.deepEqual(parseArguments(['--dryrun', 'cmd'], definitions), {positionals: ['cmd'], values: {dryrun: true}});
    });

    it('converts number options', () => {
      assert.deepEqual(parseArguments(['cmd', '--keep', '3'], definitions).values, {keep: 3});
      assert.deepEqual(parseArguments(['cmd', '--keep=10'], definitions).values, {keep: 10});
    });

    it('fails for number options without a number', () => {
      for (const args of [['--keep'], ['--keep', 'abc'], ['--keep='], ['--keep', ' '], ['--keep', '--dryrun']]) {
        assert.throws(() => parseArguments(['cmd', ...args], definitions), /Option --keep requires a number/, args.join(' '));
      }
    });

    it('parses object options given with dots', () => {
      assert.deepEqual(parseArguments(['cmd', '--mapping.0.regex', 'a', '--mapping.0.version=1.0.0'], definitions).values, {mapping: {0: {regex: 'a', version: '1.0.0'}}});
    });

    it('parses undefined options with or without value, the following argument is a positional', () => {
      assert.deepEqual(parseArguments(['cmd', '--other', 'x', 'y'], definitions), {positionals: ['cmd', 'x', 'y'], values: {other: true}});
      assert.deepEqual(parseArguments(['--other', 'cmd'], definitions), {positionals: ['cmd'], values: {other: true}});
      assert.deepEqual(parseArguments(['cmd', '--other=x'], definitions).values, {other: 'x'});
      assert.deepEqual(parseArguments(['cmd', '--other', '--dryrun'], definitions).values, {other: true, dryrun: true});
      assert.deepEqual(parseArguments(['cmd', '--other'], definitions).values, {other: true});
    });

    it('creates nested objects for option names with dots', () => {
      const {positionals, values} = parseArguments([
        'version:release:dependencies',
        '--mapping.0.regex', '@eclipse-scout',
        '--mapping.0.version', '26.2.0',
        '--mapping.1.regex=@bsi-scout',
        '--mapping.1.version=26.2.1',
        '--workspaceRoot=/ws'
      ], definitions);

      assert.deepEqual(positionals, ['version:release:dependencies']);
      assert.deepEqual(values, {
        mapping: {
          0: {regex: '@eclipse-scout', version: '26.2.0'},
          1: {regex: '@bsi-scout', version: '26.2.1'}
        },
        workspaceRoot: '/ws'
      });
    });

    it('lets the last occurrence win', () => {
      assert.deepEqual(parseArguments(['cmd', '--workspaceRoot=/a', '--workspaceRoot=/b'], definitions).values, {workspaceRoot: '/b'});
    });

    it('treats arguments after -- as positionals', () => {
      assert.deepEqual(parseArguments(['cmd', '--', '--dryrun'], definitions), {positionals: ['cmd', '--dryrun'], values: {}});
    });
  });

  describe('getUnknownOptions', () => {

    it('returns the names of the values not contained in the definitions', () => {
      const {values} = parseArguments(['cmd', '--dryrun', '--dryRun', '--mapping.0.regex', 'a', '--other', 'x'], definitions);
      assert.deepEqual(getUnknownOptions(values, definitions), ['dryRun', 'other']);
    });
  });

  describe('getDefaultValues', () => {

    it('returns the defaults of all options with a default', () => {
      assert.deepEqual(getDefaultValues(definitions), {dryrun: false, workspaceRoot: null});
    });
  });
});
