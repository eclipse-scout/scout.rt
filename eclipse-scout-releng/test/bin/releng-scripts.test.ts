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
import {execFile} from 'node:child_process';
import {promises as fs} from 'node:fs';
import {tmpdir} from 'node:os';
import {join, resolve} from 'node:path';
import {describe, it} from 'node:test';
import {promisify} from 'node:util';

const RELENG_SCRIPTS = resolve(import.meta.dirname, '..', '..', 'bin', 'releng-scripts.ts');

describe('releng-scripts', () => {

  it('fails for an unknown script', async () => {
    await withWorkspace(async dir => {
      await assert.rejects(runRelengScripts(dir, 'unknown:script'), (error: Error) => error.message.includes('Unknown script: "unknown:script"'));
      await assert.rejects(runRelengScripts(dir), (error: Error) => error.message.includes('Unknown script: ""'));
    });
  });

  it('fails for an option not defined for the script', async () => {
    await withWorkspace(async dir => {
      await assert.rejects(runRelengScripts(dir, 'version:release', '--newVersion', '2.0.0', '--dryRun', '--keep', '1'),
        (error: Error) => error.message.includes('Unknown options for script "version:release": --dryRun, --keep'));
      assert.equal((await readPackageJson(dir)).version, '1.0.0-snapshot');
    });
  });

  it('fails for an unknown option before the script name without taking the script name as value', async () => {
    await withWorkspace(async dir => {
      await assert.rejects(runRelengScripts(dir, '--dryRun', 'version:release', '--newVersion', '2.0.0'),
        (error: Error) => error.message.includes('Unknown options for script "version:release": --dryRun') && error.message.includes('version:release failed'));
    });
  });

  it('version:snapshot:dependencies does not rewrite unchanged package.json files', async () => {
    await withWorkspace(async dir => {
      const content = JSON.stringify(await readPackageJson(dir), null, 4);
      await fs.writeFile(join(dir, 'pkg-a', 'package.json'), content);

      await runRelengScripts(dir, 'version:snapshot:dependencies', `--workspaceRoot=${dir}`);

      assert.equal(await fs.readFile(join(dir, 'pkg-a', 'package.json'), 'utf8'), content);
    });
  });

  it('version:release:dependencies applies the mapping given in space separated form', async () => {
    await withWorkspace(async dir => {
      await runRelengScripts(dir, 'version:release:dependencies', '--mapping.0.regex', '@eclipse-scout', '--mapping.0.version', '26.2.0', `--workspaceRoot=${dir}`);

      assert.deepEqual(await readPackageJson(dir), {
        name: 'pkg-a',
        version: '1.0.0-snapshot',
        dependencies: {'@eclipse-scout/core': '26.2.0', 'pkg-b': '>=1.0.0-snapshot <1.0.0'}
      });
    });
  });

  it('version:release:dependencies applies the mapping given in = form', async () => {
    await withWorkspace(async dir => {
      await runRelengScripts(dir, 'version:release:dependencies', '--mapping.0.regex=@eclipse-scout', '--mapping.0.version=26.2.0', `--workspaceRoot=${dir}`);

      assert.equal((await readPackageJson(dir)).dependencies['@eclipse-scout/core'], '26.2.0');
    });
  });

  it('version:release updates versions of the workspace modules', async () => {
    await withWorkspace(async dir => {
      await runRelengScripts(dir, 'version:release', '--newVersion=1.0.0', `--workspaceRoot=${dir}`);

      assert.deepEqual(await readPackageJson(dir), {
        name: 'pkg-a',
        version: '1.0.0',
        dependencies: {'@eclipse-scout/core': '>=26.2.0-snapshot <26.2.0', 'pkg-b': '1.0.0'}
      });
    });
  });

  it('does not write the package.json with --dryrun', async () => {
    await withWorkspace(async dir => {
      const before = await readPackageJson(dir);
      const {stdout} = await runRelengScripts(dir, 'version:release', '--newVersion', '1.0.0', '--workspaceRoot', dir, '--dryrun');

      assert.deepEqual(await readPackageJson(dir), before);
      assert.match(stdout, /"version": "1\.0\.0"/);
    });
  });
});

async function runRelengScripts(cwd: string, ...args: string[]): Promise<{ stdout: string; stderr: string }> {
  return await promisify(execFile)(process.execPath, [RELENG_SCRIPTS, ...args], {cwd});
}

async function readPackageJson(dir: string): Promise<Record<string, any>> {
  return JSON.parse(await fs.readFile(join(dir, 'pkg-a', 'package.json'), 'utf8'));
}

/**
 * Creates a pnpm workspace containing the module `pkg-a` which depends on the workspace module `pkg-b` and on the external `@eclipse-scout/core`, both as snapshot.
 */
async function withWorkspace(callback: (dir: string) => Promise<void>): Promise<void> {
  const dir = await fs.mkdtemp(join(tmpdir(), 'releng-scripts-test-'));
  try {
    await fs.writeFile(join(dir, 'pnpm-workspace.yaml'), 'packages:\n  - pkg-a\n  - pkg-b\n');
    await fs.mkdir(join(dir, 'pkg-a'));
    await fs.writeFile(join(dir, 'pkg-a', 'package.json'), JSON.stringify({
      name: 'pkg-a',
      version: '1.0.0-snapshot',
      dependencies: {'@eclipse-scout/core': '>=26.2.0-snapshot <26.2.0', 'pkg-b': '>=1.0.0-snapshot <1.0.0'}
    }));
    await fs.mkdir(join(dir, 'pkg-b'));
    await fs.writeFile(join(dir, 'pkg-b', 'package.json'), JSON.stringify({name: 'pkg-b', version: '1.0.0-snapshot'}));
    await callback(dir);
  } finally {
    await fs.rm(dir, {recursive: true});
  }
}
