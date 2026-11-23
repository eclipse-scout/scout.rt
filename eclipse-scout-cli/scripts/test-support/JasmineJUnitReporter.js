/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
const fs = require('fs');
const path = require('path');

/**
 * Jasmine reporter (Node side) writing a JUnit XML file. Suite and global errors (e.g. in afterAll or asynchronous errors between specs) are reported as failed test cases.
 */
module.exports = class JasmineJUnitReporter {

  constructor(file) {
    this.file = file;
    this.cases = [];
    this.start = Date.now();
    this.stats = {total: 0, failed: 0, skipped: 0};
  }

  specDone(result) {
    this.cases.push(result);
  }

  suiteDone(result) {
    if (result.failedExpectations?.length) {
      this.cases.push({...result, description: '(suite error)', fullName: `${result.fullName} (suite error)`, status: 'failed'});
    }
  }

  jasmineDone(result) {
    if (result.failedExpectations?.length) {
      this.cases.push({...result, description: '(global error)', fullName: '(global error)', status: 'failed', duration: 0});
    }
    const executed = this.cases.filter(c => c.status !== 'excluded');
    this.stats = {
      total: executed.length,
      failed: executed.filter(c => c.status === 'failed').length,
      skipped: executed.filter(c => c.status !== 'passed' && c.status !== 'failed').length
    };
    const testCases = executed.map(c => {
      const suite = c.fullName.substring(0, c.fullName.length - c.description.length).trim();
      let body = '';
      if (c.status === 'failed') {
        body = c.failedExpectations.map(f => `<failure message="${escape(f.message)}">${escape(f.stack)}</failure>`).join('');
      } else if (c.status !== 'passed') {
        body = '<skipped/>';
      }
      return `  <testcase classname="${escape(suite)}" name="${escape(c.description)}" time="${(c.duration || 0) / 1000}">${body}</testcase>`;
    });
    const xml = '<?xml version="1.0" encoding="UTF-8"?>\n'
      + `<testsuite name="jasmine" tests="${this.stats.total}" failures="${this.stats.failed}" skipped="${this.stats.skipped}" time="${(Date.now() - this.start) / 1000}">\n`
      + testCases.join('\n')
      + '\n</testsuite>\n';
    fs.mkdirSync(path.dirname(this.file), {recursive: true});
    fs.writeFileSync(this.file, xml);
  }
};

/**
 * Removes the characters not allowed in XML 1.0 and escapes the XML special characters.
 */
function escape(s) {
  return String(s ?? '')
    .replace(/[^\t\n\r\x20-\uD7FF\uE000-\uFFFD\u{10000}-\u{10FFFF}]/gu, '')
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}
