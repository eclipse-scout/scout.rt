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
import {promises as fs} from 'node:fs';
import {tmpdir} from 'node:os';
import {join} from 'node:path';
import {describe, it} from 'node:test';
import pnpm from '../../scripts/util/pnpm.ts';

describe('pnpm', () => {

  describe('findPnpm', () => {

    it('returns path to pnpm.mjs at the Linux candidate path', async () => {
      await withFakeLinuxNodeInstallation(async (nodeExec, linuxPnpmMjs) => {
        await withPatchedExecPath(nodeExec, async () => {
          const mjs = await pnpm.findPnpm();
          assert.equal(mjs, linuxPnpmMjs);
        });
      });
    });

    it('returns path to pnpm.mjs at the Windows candidate path when Linux path does not exist', async () => {
      await withFakeWindowsNodeInstallation(async (nodeExec, windowsPnpmMjs) => {
        await withPatchedExecPath(nodeExec, async () => {
          const mjs = await pnpm.findPnpm();
          assert.equal(mjs, windowsPnpmMjs);
        });
      });
    });

    it('throws with informative error when pnpm.mjs is not found', async () => {
      await withTempDir(async dir => {
        const nodeExec = join(dir, 'node');
        await fs.writeFile(nodeExec, '');

        await withPatchedExecPath(nodeExec, async () => {
          await assert.rejects(
            () => pnpm.findPnpm(),
            {message: `Cannot find 'pnpm.mjs' in node installation '${nodeExec}'.`}
          );
        });
      });
    });
  });
});

async function withFakeLinuxNodeInstallation(callback: (nodeExec: string, linuxPnpmMjs: string) => Promise<void>): Promise<void> {
  await withTempDir(async dir => {
    // typical linux node installation layout
    //   bin/node          <- node executable
    //   lib/node_modules  <- node modules directory containing pnpm
    const fakeLinuxNodeDir = join(dir, 'fake-linux-node');
    await fs.mkdir(fakeLinuxNodeDir, {recursive: true});

    const nodeExecDir = join(fakeLinuxNodeDir, 'bin');
    const nodeExec = join(nodeExecDir, 'node');
    await fs.mkdir(nodeExecDir, {recursive: true});
    await fs.writeFile(nodeExec, '');

    const linuxPnpmMjsDir = join(fakeLinuxNodeDir, 'lib', 'node_modules', 'pnpm', 'bin');
    const linuxPnpmMjs = join(linuxPnpmMjsDir, 'pnpm.mjs');
    await fs.mkdir(linuxPnpmMjsDir, {recursive: true});
    await fs.writeFile(linuxPnpmMjs, '');

    await callback(nodeExec, linuxPnpmMjs);
  });
}

async function withFakeWindowsNodeInstallation(callback: (nodeExec: string, windowsPnpmMjs: string) => Promise<void>): Promise<void> {
  await withTempDir(async dir => {
    // typical windows node installation layout
    //   node.exe          <- node executable
    //   node_modules      <- node modules directory containing pnpm
    const fakeWindowsNodeDir = join(dir, 'fake-windows-node');
    await fs.mkdir(fakeWindowsNodeDir, {recursive: true});

    const nodeExec = join(fakeWindowsNodeDir, 'node.exe');
    await fs.writeFile(nodeExec, '');

    const windowsPnpmMjsDir = join(fakeWindowsNodeDir, 'node_modules', 'pnpm', 'bin');
    const windowsPnpmMjs = join(windowsPnpmMjsDir, 'pnpm.mjs');
    await fs.mkdir(windowsPnpmMjsDir, {recursive: true});
    await fs.writeFile(windowsPnpmMjs, '');

    await callback(nodeExec, windowsPnpmMjs);
  });
}

async function withTempDir(callback: (dir: string) => Promise<void>): Promise<void> {
  const dir = await fs.mkdtemp(join(tmpdir(), 'pnpm-test-'));
  try {
    await callback(dir);
  } finally {
    await fs.rm(dir, {recursive: true});
  }
}

async function withPatchedExecPath(execPath: string, callback: () => Promise<void>): Promise<void> {
  const originalExecPath = process.execPath;
  process.execPath = execPath;
  try {
    await callback();
  } finally {
    process.execPath = originalExecPath;
  }
}
