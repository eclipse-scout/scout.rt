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
const {afterEach, beforeEach, describe, it} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('fs');
const os = require('os');
const path = require('path');
const JasmineJUnitReporter = require('../scripts/test-support/JasmineJUnitReporter');

describe('JasmineJUnitReporter', () => {
  let dir;
  let file;

  beforeEach(() => {
    dir = fs.mkdtempSync(path.join(os.tmpdir(), 'scout-junit-test-'));
    file = path.join(dir, 'reports', 'test-results.xml');
  });

  afterEach(() => {
    fs.rmSync(dir, {recursive: true, force: true});
  });

  function spec(fullName, description, status, extra = {}) {
    return {fullName, description, status, duration: 12, failedExpectations: [], ...extra};
  }

  function testCases(xml) {
    return xml.split('\n').filter(line => line.trim().startsWith('<testcase'));
  }

  it('writes the executed specs as test cases and computes the stats', () => {
    const reporter = new JasmineJUnitReporter(file);
    reporter.specDone(spec('Suite A passes', 'passes', 'passed'));
    reporter.specDone(spec('Suite A fails', 'fails', 'failed', {failedExpectations: [{message: 'Expected 1 to be 2.', stack: 'at spec.js:1'}]}));
    reporter.specDone(spec('Suite A is pending', 'is pending', 'pending'));
    reporter.specDone(spec('Suite A is excluded', 'is excluded', 'excluded'));
    reporter.jasmineDone({failedExpectations: []});

    assert.deepEqual(reporter.stats, {total: 3, failed: 1, skipped: 1});
    const xml = fs.readFileSync(file, 'utf8');
    assert.match(xml, /^<\?xml version="1.0" encoding="UTF-8"\?>\n<testsuite name="jasmine" tests="3" failures="1" skipped="1" time="[\d.]+">/);
    const cases = testCases(xml);
    assert.equal(cases.length, 3);
    assert.equal(cases[0], '  <testcase classname="Suite A" name="passes" time="0.012"></testcase>');
    assert.equal(cases[1], '  <testcase classname="Suite A" name="fails" time="0.012"><failure message="Expected 1 to be 2.">at spec.js:1</failure></testcase>');
    assert.equal(cases[2], '  <testcase classname="Suite A" name="is pending" time="0.012"><skipped/></testcase>');
    assert.doesNotMatch(xml, /is excluded/);
  });

  it('reports suite errors and global errors as failed test cases', () => {
    const reporter = new JasmineJUnitReporter(file);
    reporter.specDone(spec('Suite A passes', 'passes', 'passed'));
    reporter.suiteDone({fullName: 'Suite A', description: 'Suite A', failedExpectations: [{message: 'afterAll failed', stack: ''}]});
    reporter.suiteDone({fullName: 'Suite B', description: 'Suite B', failedExpectations: []});
    reporter.jasmineDone({failedExpectations: [{message: 'Unhandled promise rejection: error', stack: ''}]});

    assert.deepEqual(reporter.stats, {total: 3, failed: 2, skipped: 0});
    const cases = testCases(fs.readFileSync(file, 'utf8'));
    assert.equal(cases[1], '  <testcase classname="Suite A" name="(suite error)" time="0"><failure message="afterAll failed"></failure></testcase>');
    assert.equal(cases[2], '  <testcase classname="" name="(global error)" time="0"><failure message="Unhandled promise rejection: error"></failure></testcase>');
  });

  it('escapes the XML special characters and removes the characters not allowed in XML', () => {
    const reporter = new JasmineJUnitReporter(file);
    const message = 'Expected "<a>" & \u001b[31mred\u001b[0m\u0000 to be \'b\'\t😀';
    reporter.specDone(spec('Suite <A> escapes', 'escapes', 'failed', {failedExpectations: [{message, stack: undefined}]}));
    reporter.jasmineDone({});

    const cases = testCases(fs.readFileSync(file, 'utf8'));
    assert.equal(cases[0], '  <testcase classname="Suite &lt;A&gt;" name="escapes" time="0.012">'
      + '<failure message="Expected &quot;&lt;a&gt;&quot; &amp; [31mred[0m to be \'b\'\t😀"></failure></testcase>');
  });

  it('writes an empty test suite if no specs are executed', () => {
    const reporter = new JasmineJUnitReporter(file);
    reporter.jasmineDone({});

    assert.deepEqual(reporter.stats, {total: 0, failed: 0, skipped: 0});
    assert.match(fs.readFileSync(file, 'utf8'), /<testsuite name="jasmine" tests="0" failures="0" skipped="0" time="[\d.]+">\n\n<\/testsuite>/);
  });
});
