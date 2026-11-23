/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
const {spawn} = require('child_process');
const fs = require('fs');
const os = require('os');
const path = require('path');

/**
 * Minimal WebDriver replacement for the jasmine-browser-runner (get, executeScript, close) based on the Chrome DevTools Protocol.
 * Launches the local Chrome binary directly, no chromedriver is required.
 */
module.exports = class CdpDriver {

  /**
   * @returns {string} the Chrome executable: environment variable CHROME_BIN or the default installation location of Chrome, Chromium, Edge or Brave
   */
  static findChromeBinary() {
    if (process.env.CHROME_BIN) {
      return process.env.CHROME_BIN;
    }
    let candidates;
    if (process.platform === 'win32') {
      const installDirs = [process.env.LOCALAPPDATA, process.env.PROGRAMFILES, process.env['PROGRAMFILES(X86)']].filter(Boolean);
      candidates = [
        path.join('Google', 'Chrome', 'Application', 'chrome.exe'),
        path.join('Microsoft', 'Edge', 'Application', 'msedge.exe'),
        path.join('BraveSoftware', 'Brave-Browser', 'Application', 'brave.exe')
      ].flatMap(executable => installDirs.map(dir => path.join(dir, executable)));
    } else if (process.platform === 'darwin') {
      candidates = [
        'Google Chrome.app/Contents/MacOS/Google Chrome',
        'Microsoft Edge.app/Contents/MacOS/Microsoft Edge',
        'Brave Browser.app/Contents/MacOS/Brave Browser'
      ].flatMap(executable => ['/Applications', path.join(os.homedir(), 'Applications')].map(dir => path.join(dir, executable)));
    } else {
      candidates = ['google-chrome', 'google-chrome-stable', 'chromium', 'chromium-browser', 'microsoft-edge', 'microsoft-edge-stable', 'brave-browser', 'brave-browser-stable', 'brave']
        .flatMap(name => (process.env.PATH || '').split(path.delimiter).map(dir => path.join(dir, name)));
    }
    const binary = candidates.find(file => fs.existsSync(file));
    if (!binary) {
      throw new Error('Chrome not found. Set the environment variable CHROME_BIN to the executable of Chrome or another Chromium based browser.');
    }
    return binary;
  }

  static async launch({binary = CdpDriver.findChromeBinary(), headless = true, args = [], startupTimeoutMs = 30000} = {}) {
    const userDataDir = fs.mkdtempSync(path.join(os.tmpdir(), 'scout-test-chrome-'));
    // Flags of karma-chrome-launcher 3.2.0 (https://github.com/karma-runner/karma-chrome-launcher/blob/v3.2.0/index.js)
    const chromeArgs = [
      ...(headless ? ['--headless=new'] : []),
      '--disable-gpu',
      '--disable-dev-shm-usage',
      // isolated profile without background throttling
      '--enable-automation',
      '--no-default-browser-check',
      '--no-first-run',
      '--disable-default-apps',
      '--disable-popup-blocking',
      '--disable-translate',
      '--disable-background-timer-throttling',
      '--disable-renderer-backgrounding',
      '--disable-device-discovery-notifications',
      '--remote-debugging-port=0',
      `--user-data-dir=${userDataDir}`,
      ...args,
      // Starts with an empty page. The test page is loaded by get(): the URL is built by the jasmine-browser-runner Runner,
      // the browser is reused for every run of test-server:start and focus emulation is enabled before the page loads.
      'about:blank'
    ];
    const proc = spawn(binary, chromeArgs, {stdio: ['ignore', 'ignore', 'pipe']});
    const driver = new CdpDriver(proc, userDataDir);
    try {
      const browserWsUrl = await withTimeout(new Promise((resolve, reject) => {
        let stderr = '';
        const onData = chunk => {
          stderr += chunk;
          const match = /DevTools listening on (ws:\/\/\S+)/.exec(stderr);
          if (match) {
            // the stream stays in flowing mode: the remaining output is discarded
            proc.stderr.off('data', onData);
            resolve(match[1]);
          }
        };
        proc.stderr.on('data', onData);
        proc.on('error', reject);
        proc.on('exit', code => reject(new Error(`Chrome exited with code ${code}: ${stderr}`)));
      }), startupTimeoutMs, 'Chrome startup');
      const httpBase = browserWsUrl.replace(/^ws:\/\/([^/]+)\/.*$/, 'http://$1');
      const page = await withTimeout(waitForPageTarget(httpBase), startupTimeoutMs, 'Chrome page target');
      await driver._connect(page.webSocketDebuggerUrl);
      return driver;
    } catch (err) {
      await driver.close();
      throw err;
    }
  }

  constructor(proc, userDataDir) {
    this._proc = proc;
    this._userDataDir = userDataDir;
    this._ws = null;
    this._nextId = 1;
    this._pending = new Map();
    this._eventListeners = [];
    this._closeHandlers = new Set();
    this._connectionClosed = false;
    this._closing = null;
  }

  /**
   * @returns {boolean} true if the connection to the browser is closed, e.g. because the browser window has been closed by the user
   */
  get connectionClosed() {
    return this._connectionClosed;
  }

  _connect(wsUrl) {
    return new Promise((resolve, reject) => {
      this._ws = new WebSocket(wsUrl);
      this._ws.onopen = resolve;
      this._ws.onerror = reject;
      this._ws.onmessage = event => {
        const msg = JSON.parse(event.data);
        if (msg.id && this._pending.has(msg.id)) {
          const {resolve, reject} = this._pending.get(msg.id);
          this._pending.delete(msg.id);
          msg.error ? reject(new Error(msg.error.message)) : resolve(msg.result);
        } else if (msg.method) {
          this._eventListeners.forEach(l => l(msg));
        }
      };
      this._ws.onclose = () => {
        this._connectionClosed = true;
        this._closeHandlers.forEach(handler => handler());
      };
    });
  }

  /**
   * Creates a promise using the executor which is rejected if it is not settled within the timeout or if the connection to the browser closes.
   */
  _request(executor, timeoutMs, description) {
    if (this._connectionClosed) {
      return Promise.reject(new Error(`${description} failed: the connection to the browser is closed`));
    }
    let onClose;
    const promise = new Promise((resolve, reject) => {
      onClose = () => reject(new Error(`${description} failed: the connection to the browser has been closed`));
      this._closeHandlers.add(onClose);
      executor(resolve, reject);
    });
    return withTimeout(promise, timeoutMs, description).finally(() => this._closeHandlers.delete(onClose));
  }

  _send(method, params = {}, timeoutMs = 30000) {
    const id = this._nextId++;
    return this._request((resolve, reject) => {
      this._pending.set(id, {resolve, reject});
      this._ws.send(JSON.stringify({id, method, params}));
    }, timeoutMs, `CDP ${method}`).finally(() => this._pending.delete(id));
  }

  _waitForEvent(method, timeoutMs) {
    let listener;
    return this._request(resolve => {
      listener = msg => {
        if (msg.method === method) {
          resolve(msg.params);
        }
      };
      this._eventListeners.push(listener);
    }, timeoutMs, `CDP event ${method}`).finally(() => {
      this._eventListeners = this._eventListeners.filter(l => l !== listener);
    });
  }

  async get(url) {
    await this._send('Page.enable');
    // headless pages have no OS focus: emulate it so that focus and blur events are fired
    await this._send('Emulation.setFocusEmulationEnabled', {enabled: true});
    const loaded = this._waitForEvent('Page.loadEventFired', 300000);
    const [navigation] = await Promise.all([this._send('Page.navigate', {url}), loaded]);
    if (navigation.errorText) {
      throw new Error(`Unable to load ${url}: ${navigation.errorText}`);
    }
  }

  async executeScript(script) {
    const result = await this._send('Runtime.evaluate', {
      expression: `(function() {\n${script}\n})()`,
      returnByValue: true
    });
    if (result.exceptionDetails) {
      throw new Error(result.exceptionDetails.exception?.description || result.exceptionDetails.text);
    }
    return result.result.value;
  }

  /**
   * Closes the browser and deletes its profile directory. Subsequent calls return the result of the first call.
   */
  close() {
    this._closing ||= this._close();
    return this._closing;
  }

  async _close() {
    if (this._ws && !this._connectionClosed) {
      try {
        await this._send('Browser.close', {}, 5000);
      } catch {
        // browser already gone
      }
      this._ws.close();
    }
    if (this._proc.exitCode === null && this._proc.signalCode === null) {
      const exited = new Promise(resolve => this._proc.once('exit', resolve));
      try {
        await withTimeout(exited, 10000, 'Chrome shutdown');
      } catch {
        this._proc.kill('SIGKILL');
      }
    }
    try {
      // retries if the browser still holds file locks after exiting (Windows)
      fs.rmSync(this._userDataDir, {recursive: true, force: true, maxRetries: 5, retryDelay: 100});
    } catch (err) {
      console.warn(`Unable to delete the browser profile ${this._userDataDir}: ${err.message}`);
    }
  }
};

/**
 * Polls the DevTools HTTP endpoint /json/list every 100 ms until the browser has opened its initial tab (about:blank).
 * @param {string} httpBase base URL of the DevTools HTTP endpoint, e.g. http://127.0.0.1:12345
 * @returns {Promise<object>} the DevTools target of the tab, its webSocketDebuggerUrl is used to control the page
 */
async function waitForPageTarget(httpBase) {
  for (; ;) {
    const targets = await (await fetch(`${httpBase}/json/list`)).json();
    const page = targets.find(t => t.type === 'page');
    if (page) {
      return page;
    }
    await new Promise(resolve => setTimeout(resolve, 100));
  }
}

function withTimeout(promise, timeoutMs, what) {
  let timer;
  const timeout = new Promise((resolve, reject) => {
    timer = setTimeout(() => reject(new Error(`${what} timed out after ${timeoutMs} ms`)), timeoutMs);
  });
  return Promise.race([promise, timeout]).finally(() => clearTimeout(timer));
}
