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
import {collectModulesInWorkspace, ensureWorkspaceRoot} from '../scripts/update-version.ts';

describe('update-version', () => {

  describe('collectModulesInWorkspace', () => {

    it('returns only packages from the pnpm-workspace', async () => {
      await withTempDir(async dir => {
        await fs.writeFile(
          join(dir, 'pnpm-workspace.yaml'),
          'packages:\n' +
          '  - pkg-a\n' +
          '  - pkg-b\n' +
          '  - level1/level2/level3/pkg-d\n'
        );
        await createPackageJson(join(dir, 'pkg-a'), 'pkg-a');
        await createPackageJson(join(dir, 'pkg-b'), 'pkg-b');
        await createPackageJson(join(dir, 'pkg-c'), 'pkg-c');
        await createPackageJson(join(dir, 'level1', 'level2', 'level3', 'pkg-d'), 'pkg-d');

        const modules = await collectModulesInWorkspace(dir);
        const names = modules.map(m => m.manifest.name).sort();
        assert.deepEqual(names, ['pkg-a', 'pkg-b', 'pkg-d']);
      });
    });

    it('returns empty array when there are no packages', async () => {
      await withTempDir(async dir => {
        await fs.writeFile(
          join(dir, 'pnpm-workspace.yaml'),
          'packages:\n' +
          '  - pkg-a\n' +
          '  - pkg-b\n'
        );

        const modules = await collectModulesInWorkspace(dir);
        assert.deepEqual(modules, []);
      });
    });

    it('uses workspace root if it does not contain a workspace file', async () => {
      await withTempDir(async dir => {
        await fs.writeFile(
          join(dir, 'pnpm-workspace.yaml'),
          'packages:\n' +
          '  - pkg-a\n' +
          '  - pkg-b\n'
        );
        await createPackageJson(join(dir, 'pkg-a'), 'pkg-a');
        await createPackageJson(join(dir, 'pkg-b'), 'pkg-b');
        await createPackageJson(join(dir, 'pkg-c'), 'pkg-c');

        const level3 = join(dir, 'level1', 'level2', 'level3');
        await fs.mkdir(level3, {recursive: true});
        await createPackageJson(level3, 'pkg-d');
        await createPackageJson(join(level3, 'pkg-e'), 'pkg-e');

        const modules = await collectModulesInWorkspace(dir, level3);
        const names = modules.map(m => m.manifest.name).sort();
        assert.deepEqual(names, ['pkg-d']);
      });
    });

    it('only collects modules in the given workspace root', async () => {
      await withTempDir(async dir => {
        await fs.writeFile(
          join(dir, 'pnpm-workspace.yaml'),
          'packages:\n' +
          '  - pkg-a\n' +
          '  - pkg-b\n'
        );
        await createPackageJson(join(dir, 'pkg-a'), 'pkg-a');
        await createPackageJson(join(dir, 'pkg-b'), 'pkg-b');
        await createPackageJson(join(dir, 'pkg-c'), 'pkg-c');

        const level3 = join(dir, 'level1', 'level2', 'level3');
        await fs.mkdir(level3, {recursive: true});
        await fs.writeFile(
          join(level3, 'pnpm-workspace.yaml'),
          'packages:\n' +
          '  - .\n' +
          '  - pkg-e\n'
        );
        await createPackageJson(level3, 'pkg-d');
        await createPackageJson(join(level3, 'pkg-e'), 'pkg-e');
        await createPackageJson(join(level3, 'pkg-f'), 'pkg-f');

        const modules = await collectModulesInWorkspace(dir, level3);
        const names = modules.map(m => m.manifest.name).sort();
        assert.deepEqual(names, ['pkg-d', 'pkg-e']);
      });
    });
  });

  describe('ensureWorkspaceRoot', () => {

    it('returns the given workspaceRoot immediately when provided', async () => {
      await withTempDir(async dir => {
        const workspaceRoot = await ensureWorkspaceRoot(dir, '/explicit/workspace/root');
        assert.equal(workspaceRoot, '/explicit/workspace/root');
      });
    });

    it('finds the workspace root via findWorkspaceFileDir when workspaceRoot is not given', async () => {
      await withTempDir(async dir => {
        const level3 = join(dir, 'level1', 'level2', 'level3');
        await fs.mkdir(level3, {recursive: true});
        await fs.writeFile(join(dir, 'pnpm-workspace.yaml'), '');
        const workspaceRoot = await ensureWorkspaceRoot(level3);
        assert.equal(workspaceRoot, dir);
      });
    });

    it('falls back to the parent directory when no pnpm-workspace.yaml is found', async () => {
      await withTempDir(async dir => {
        const level1 = join(dir, 'level1');
        const level2 = join(level1, 'level2');
        const level3 = join(level2, 'level3');
        await fs.mkdir(level3, {recursive: true});

        let workspaceRoot = await ensureWorkspaceRoot(level1);
        assert.equal(workspaceRoot, dir);

        workspaceRoot = await ensureWorkspaceRoot(level2);
        assert.equal(workspaceRoot, level1);

        workspaceRoot = await ensureWorkspaceRoot(level3);
        assert.equal(workspaceRoot, level2);
      });
    });
  });
});

async function withTempDir(callback: (dir: string) => Promise<void>): Promise<void> {
  const dir = await fs.mkdtemp(join(tmpdir(), 'cmiw-test-'));
  try {
    await callback(dir);
  } finally {
    await fs.rm(dir, {recursive: true});
  }
}

async function createPackageJson(dir: string, name: string) {
  await fs.mkdir(dir, {recursive: true});
  await fs.writeFile(join(dir, 'package.json'), JSON.stringify({name, version: '1.0.0'}));
}
