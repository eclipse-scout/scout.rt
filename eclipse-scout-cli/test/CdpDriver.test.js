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
const {after, before, describe, it} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('fs');
const CdpDriver = require('../scripts/test-support/CdpDriver');

function chromeAvailable() {
  try {
    return fs.existsSync(CdpDriver.findChromeBinary());
  } catch (err) {
    return false;
  }
}

describe('CdpDriver', () => {

  describe('findChromeBinary', () => {
    it('returns the value of the environment variable CHROME_BIN', () => {
      const original = process.env.CHROME_BIN;
      try {
        process.env.CHROME_BIN = '/custom/chrome';
        assert.equal(CdpDriver.findChromeBinary(), '/custom/chrome');
      } finally {
        if (original === undefined) {
          delete process.env.CHROME_BIN;
        } else {
          process.env.CHROME_BIN = original;
        }
      }
    });

    it('rejects the launch if the binary cannot be started', async () => {
      await assert.rejects(CdpDriver.launch({binary: '/nonexistent/chrome'}), /ENOENT/);
    });
  });

  describe('with Chrome', {skip: !chromeAvailable() && 'Chrome not found (set CHROME_BIN)', timeout: 120000}, () => {
    let driver;

    before(async () => {
      driver = await CdpDriver.launch({headless: true});
    });

    after(async () => {
      await driver?.close();
    });

    it('loads a page and executes scripts in it', async () => {
      await driver.get('data:text/html,<title>CdpDriver test</title>');
      assert.equal(await driver.executeScript('return document.title;'), 'CdpDriver test');
      assert.deepEqual(await driver.executeScript('return {sum: 1 + 2, list: [1, "a"]};'), {sum: 3, list: [1, 'a']});
    });

    it('emulates the focus so that focus events are fired in headless mode', async () => {
      await driver.get('data:text/html,<input id="input">');
      assert.equal(await driver.executeScript(`
        const input = document.getElementById('input');
        let focused = false;
        input.addEventListener('focus', () => focused = true);
        input.focus();
        return focused && document.hasFocus() && document.activeElement === input;
      `), true);
    });

    it('rejects scripts throwing an error with the error description', async () => {
      await assert.rejects(driver.executeScript('throw new Error("script failed");'), /Error: script failed/);
    });

    it('rejects if the page cannot be loaded', async () => {
      await assert.rejects(driver.get('http://127.0.0.1:9/'), /Unable to load http:\/\/127\.0\.0\.1:9\/: net::ERR_/);
    });
  });

  describe('close', {skip: !chromeAvailable() && 'Chrome not found (set CHROME_BIN)', timeout: 120000}, () => {
    it('closes the browser and deletes the profile directory, subsequent calls return the same promise', async () => {
      const driver = await CdpDriver.launch({headless: true});
      const userDataDir = driver._userDataDir;
      assert.ok(fs.existsSync(userDataDir));

      const closing = driver.close();
      assert.equal(driver.close(), closing);
      await closing;

      assert.equal(fs.existsSync(userDataDir), false);
      assert.equal(driver._proc.exitCode !== null || driver._proc.signalCode !== null, true);
    });

    it('fails immediately after the browser has been closed by the user', async () => {
      const driver = await CdpDriver.launch({headless: true});
      try {
        const exited = new Promise(resolve => driver._proc.once('exit', resolve));
        driver._proc.kill('SIGKILL');
        await exited;
        await waitFor(() => driver.connectionClosed);

        const start = Date.now();
        await assert.rejects(driver.get('about:blank'), /the connection to the browser is closed/);
        assert.ok(Date.now() - start < 1000, 'get() fails without waiting for a timeout');
      } finally {
        const start = Date.now();
        await driver.close();
        assert.ok(Date.now() - start < 1000, 'close() does not wait for a timeout');
      }
    });

    it('rejects pending requests if the browser is closed', async () => {
      const driver = await CdpDriver.launch({headless: true});
      try {
        // noinspection InfiniteLoopJS
        const loading = driver.get('data:text/html,<script>while (true) {}</script>');
        setTimeout(() => driver._proc.kill('SIGKILL'), 500);
        await assert.rejects(loading, /the connection to the browser has been closed/);
      } finally {
        await driver.close();
      }
    });
  });
});

async function waitFor(condition, timeoutMs = 5000) {
  const start = Date.now();
  while (!condition()) {
    if (Date.now() - start > timeoutMs) {
      throw new Error('Condition not met within ' + timeoutMs + ' ms');
    }
    await new Promise(resolve => setTimeout(resolve, 20));
  }
}
