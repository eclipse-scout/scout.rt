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
