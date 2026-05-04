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
import files from '../../scripts/util/files.ts';

describe('files', () => {

  describe('list', () => {

    it('returns empty array for empty directory', async () => {
      await withTempDir(async dir => {
        const fileList = await files.list(dir, 'package.json');
        assert.deepEqual(fileList, []);
      });
    });

    it('returns empty array when no files match', async () => {
      await withTempDir(async dir => {
        await fs.writeFile(join(dir, 'other.txt'), '');

        const fileList = await files.list(dir, 'package.json');
        assert.deepEqual(fileList, []);
      });
    });

    it('finds files by exact string name', async () => {
      await withTempDir(async dir => {
        await fs.writeFile(join(dir, 'package.json'), '');
        await fs.writeFile(join(dir, 'package.json.bak'), '');

        const fileList = await files.list(dir, 'package.json');
        assert.deepEqual(fileList, [
          join(dir, 'package.json')
        ]);
      });
    });

    it('finds files by regex', async () => {
      await withTempDir(async dir => {
        await fs.writeFile(join(dir, 'foo.ts'), '');
        await fs.writeFile(join(dir, 'bar.ts'), '');
        await fs.writeFile(join(dir, 'baz.js'), '');

        const fileList = await files.list(dir, /\.ts$/);
        assert.deepEqual(fileList, [
          join(dir, 'bar.ts'),
          join(dir, 'foo.ts')
        ]);
      });
    });

    it('searches recursively without depth limit by default', async () => {
      await withTempDir(async dir => {
        const deep = join(dir, 'a', 'b', 'c', 'd');
        await fs.mkdir(deep, {recursive: true});
        await fs.writeFile(join(deep, 'package.json'), '');

        const fileList = await files.list(dir, 'package.json');
        assert.deepEqual(fileList, [
          join(deep, 'package.json')
        ]);
      });
    });

    it('maxDepth=0 only returns files in root directory', async () => {
      await withTempDir(async dir => {
        const sub = join(dir, 'sub');
        await fs.mkdir(sub);
        await fs.writeFile(join(dir, 'package.json'), '');
        await fs.writeFile(join(sub, 'package.json'), '');

        const fileList = await files.list(dir, 'package.json', {maxDepth: 0});
        assert.deepEqual(fileList, [
          join(dir, 'package.json')
        ]);
      });
    });

    it('maxDepth limits search depth', async () => {
      await withTempDir(async dir => {
        const level1 = join(dir, 'level1');
        const level2 = join(level1, 'level2');
        await fs.mkdir(level2, {recursive: true});
        await fs.writeFile(join(dir, 'package.json'), '');
        await fs.writeFile(join(level1, 'package.json'), '');
        await fs.writeFile(join(level2, 'package.json'), '');

        const fileList = await files.list(dir, 'package.json', {maxDepth: 1});
        assert.deepEqual(fileList, [
          join(level1, 'package.json'),
          join(dir, 'package.json')
        ]);
      });
    });

    it('folderExcludes skips directories with the given names', async () => {
      await withTempDir(async dir => {
        const excluded = join(dir, 'node_modules');
        const included = join(dir, 'src');
        await fs.mkdir(excluded);
        await fs.mkdir(included);
        await fs.writeFile(join(excluded, 'package.json'), '');
        await fs.writeFile(join(included, 'package.json'), '');

        const fileList = await files.list(dir, 'package.json', {folderExcludes: ['node_modules']});
        assert.deepEqual(fileList, [
          join(included, 'package.json')
        ]);
      });
    });

    it('folderExcludes also skips subtrees of excluded directories', async () => {
      await withTempDir(async dir => {
        const deep = join(dir, 'node_modules', 'some-pkg', 'nested');
        await fs.mkdir(deep, {recursive: true});
        await fs.writeFile(join(deep, 'package.json'), '');

        const fileList = await files.list(dir, 'package.json', {folderExcludes: ['node_modules']});
        assert.deepEqual(fileList, []);
      });
    });
  });

  describe('exists', () => {

    it('returns true for an existing file', async () => {
      await withTempDir(async dir => {
        const filePath = join(dir, 'file.txt');
        await fs.writeFile(filePath, '');

        const exists = await files.exists(filePath);
        assert.equal(exists, true);
      });
    });

    it('returns true for an existing directory', async () => {
      await withTempDir(async dir => {
        const exists = await files.exists(dir);
        assert.equal(exists, true);
      });
    });

    it('returns false for a non-existent path', async () => {
      const exists = await files.exists('/non/existent/path/xyz');
      assert.equal(exists, false);
    });

    it('returns false after a file is deleted', async () => {
      await withTempDir(async dir => {
        const filePath = join(dir, 'file.txt');
        await fs.writeFile(filePath, '');

        let exists = await files.exists(filePath);
        assert.equal(exists, true);

        await fs.rm(filePath);

        exists = await files.exists(filePath);
        assert.equal(exists, false);
      });
    });
  });
});

async function withTempDir(callback: (dir: string) => Promise<void>): Promise<void> {
  const dir = await fs.mkdtemp(join(tmpdir(), 'files-test-'));
  try {
    await callback(dir);
  } finally {
    await fs.rm(dir, {recursive: true});
  }
}
