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
import {PnpmWorkspaceYaml} from '../../scripts/util/PnpmWorkspaceYaml.ts';
import YAML from 'yaml';

describe('PnpmWorkspaceYaml', () => {

  describe('parse', () => {

    it('accepts a file path', async () => {
      await withTempDir(async dir => {
        const filePath = join(dir, 'some.yaml');
        await fs.writeFile(filePath, 'packages:\n' +
          '  - pkg-a\n');
        const pnpmWorkspaceYaml = await PnpmWorkspaceYaml.parse(filePath);

        assert.equal(pnpmWorkspaceYaml.path, filePath);
        assert.equal(pnpmWorkspaceYaml.dir, dir);
        assert.ok(pnpmWorkspaceYaml.doc.has('packages'));
        assert.deepEqual((pnpmWorkspaceYaml.doc.get('packages') as YAML.YAMLSeq).toJSON(), ['pkg-a']);
      });
    });

    it('accepts a directory path and resolves to pnpm-workspace.yaml inside', async () => {
      await withTempDir(async dir => {
        const filePath = join(dir, 'pnpm-workspace.yaml');
        await fs.writeFile(filePath, 'packages:\n' +
          '  - pkg-a\n');
        const pnpmWorkspaceYaml = await PnpmWorkspaceYaml.parse(dir);

        assert.equal(pnpmWorkspaceYaml.path, filePath);
        assert.equal(pnpmWorkspaceYaml.dir, dir);
        assert.ok(pnpmWorkspaceYaml.doc.has('packages'));
        assert.deepEqual((pnpmWorkspaceYaml.doc.get('packages') as YAML.YAMLSeq).toJSON(), ['pkg-a']);
      });
    });

    it('throws when the file does not exist', async () => {
      await assert.rejects(() => PnpmWorkspaceYaml.parse('/non/existent/path/pnpm-workspace.yaml'));
    });

    it('throws and sets process.exitCode=1 when the YAML contains parse errors', async () => {
      await withTempDir(async dir => {
        const filePath = join(dir, 'pnpm-workspace.yaml');
        await fs.writeFile(filePath, '{\n' +
          '  packages:\n' +
          '    - pkg-a\n' +
          '}\n');

        const exitCode = process.exitCode;
        await assert.rejects(() => PnpmWorkspaceYaml.parse(filePath), {message: 'Yaml parse errors.'});
        assert.equal(process.exitCode, 1);
        process.exitCode = exitCode;
      });
    });
  });

  describe('findPnpmWorkspaceFiles', () => {

    it('returns empty array when no pnpm-workspace files exist', async () => {
      await withTempDir(async dir => {
        const pnpmWorkspaceFiles = await PnpmWorkspaceYaml.findPnpmWorkspaceFiles(dir);
        assert.deepEqual(pnpmWorkspaceFiles, []);
      });
    });

    it('finds pnpm-workspace.yaml in root directory', async () => {
      await withTempDir(async dir => {
        await fs.writeFile(join(dir, 'pnpm-workspace.yaml'), '');
        const pnpmWorkspaceFiles = await PnpmWorkspaceYaml.findPnpmWorkspaceFiles(dir);
        assert.deepEqual(pnpmWorkspaceFiles, [join(dir, 'pnpm-workspace.yaml')]);
      });
    });

    it('finds pnpm-workspace-*.yaml variants', async () => {
      await withTempDir(async dir => {
        await fs.writeFile(join(dir, 'pnpm-workspace-custom.yaml'), '');
        const pnpmWorkspaceFiles = await PnpmWorkspaceYaml.findPnpmWorkspaceFiles(dir);
        assert.deepEqual(pnpmWorkspaceFiles, [join(dir, 'pnpm-workspace-custom.yaml')]);
      });
    });

    it('does not find non-matching yaml files', async () => {
      await withTempDir(async dir => {
        await fs.writeFile(join(dir, 'workspace.yaml'), '');
        await fs.writeFile(join(dir, 'package.json'), '');
        const pnpmWorkspaceFiles = await PnpmWorkspaceYaml.findPnpmWorkspaceFiles(dir);
        assert.deepEqual(pnpmWorkspaceFiles, []);
      });
    });

    it('searches subdirectories up to two levels deep', async () => {
      await withTempDir(async dir => {
        const level1 = join(dir, 'level1');
        const level2 = join(level1, 'level2');
        const level3 = join(level2, 'level3');
        await fs.mkdir(level3, {recursive: true});
        await fs.writeFile(join(dir, 'pnpm-workspace.yaml'), '');
        await fs.writeFile(join(level1, 'pnpm-workspace.yaml'), '');
        await fs.writeFile(join(level2, 'pnpm-workspace.yaml'), '');
        await fs.writeFile(join(level3, 'pnpm-workspace.yaml'), '');

        const pnpmWorkspaceFiles = await PnpmWorkspaceYaml.findPnpmWorkspaceFiles(dir);
        assert.deepEqual(pnpmWorkspaceFiles, [join(level2, 'pnpm-workspace.yaml'), join(level1, 'pnpm-workspace.yaml'), join(dir, 'pnpm-workspace.yaml')]);
      });
    });

    it('excludes node_modules, src, target and .git directories', async () => {
      await withTempDir(async dir => {
        for (const excluded of ['node_modules', 'src', 'target', '.git']) {
          const subDir = join(dir, excluded);
          await fs.mkdir(subDir, {recursive: true});
          await fs.writeFile(join(subDir, 'pnpm-workspace.yaml'), '');
        }
        const pnpmWorkspaceFiles = await PnpmWorkspaceYaml.findPnpmWorkspaceFiles(dir);
        assert.deepEqual(pnpmWorkspaceFiles, []);
      });
    });
  });

  describe('getPackages', () => {

    it('returns absolute paths of all packages', async () => {
      await withPnpmWorkspaceYaml(
        'packages:\n' +
        '  - pkg-a\n' +
        '  - sub/pkg-b\n',
        async pnpmWorkspaceYaml => {
          const packages = pnpmWorkspaceYaml.getPackages();
          assert.deepEqual(packages, [
            join(pnpmWorkspaceYaml.dir, 'pkg-a'),
            join(pnpmWorkspaceYaml.dir, 'sub', 'pkg-b')
          ]);
        }
      );
    });

    it('resolves relative paths against the directory of the pnpm-workspace.yaml', async () => {
      await withPnpmWorkspaceYaml(
        'packages:\n' +
        '  - ../sibling\n',
        async pnpmWorkspaceYaml => {
          const packages = pnpmWorkspaceYaml.getPackages();
          assert.deepEqual(packages, [
            join(pnpmWorkspaceYaml.dir, '..', 'sibling')
          ]);
        }
      );
    });

    it('returns empty array when packages list is empty', async () => {
      await withPnpmWorkspaceYaml(
        'packages: []\n',
        async pnpmWorkspaceYaml => {
          const packages = pnpmWorkspaceYaml.getPackages();
          assert.deepEqual(packages, []);
        }
      );
    });

    it('returns empty array when packages list does not exist', async () => {
      await withPnpmWorkspaceYaml(
        '',
        async pnpmWorkspaceYaml => {
          const packages = pnpmWorkspaceYaml.getPackages();
          assert.deepEqual(packages, []);
        }
      );
    });
  });

  describe('removeScoutOverrides', () => {

    it('does nothing when overrides section is absent', async () => {
      await withPnpmWorkspaceYaml(
        'packages:\n' +
        '  - pkg-a\n',
        async pnpmWorkspaceYaml => {
          pnpmWorkspaceYaml.removeScoutOverrides();

          await pnpmWorkspaceYaml.flush();
          const content = await fs.readFile(pnpmWorkspaceYaml.path, 'utf8');
          assert.equal(
            content,
            'packages:\n' +
            '  - pkg-a\n'
          );
        }
      );
    });

    it('does nothing when overrides has no merge key', async () => {
      await withPnpmWorkspaceYaml(
        'packages:\n' +
        '  - pkg-a\n' +
        'overrides:\n' +
        '  lodash: 4.17.21\n',
        async pnpmWorkspaceYaml => {
          pnpmWorkspaceYaml.removeScoutOverrides();

          await pnpmWorkspaceYaml.flush();
          const content = await fs.readFile(pnpmWorkspaceYaml.path, 'utf8');
          assert.equal(
            content,
            'packages:\n' +
            '  - pkg-a\n' +
            'overrides:\n' +
            '  lodash: 4.17.21\n'
          );
        }
      );
    });

    it('removes the merge key from overrides and keeps other entries', async () => {
      await withPnpmWorkspaceYaml(
        'packages:\n' +
        '  - pkg-a\n' +
        'scout:\n' +
        '  overrides: &scout-overrides\n' +
        '    express: 4.18.0\n' +
        'overrides:\n' +
        '  <<: *scout-overrides\n' +
        '  lodash: 4.17.21\n',
        async pnpmWorkspaceYaml => {
          pnpmWorkspaceYaml.removeScoutOverrides();

          await pnpmWorkspaceYaml.flush();
          const content = await fs.readFile(pnpmWorkspaceYaml.path, 'utf8');
          assert.equal(
            content,
            'packages:\n' +
            '  - pkg-a\n' +
            'scout:\n' +
            '  overrides: &scout-overrides\n' +
            '    express: 4.18.0\n' +
            'overrides:\n' +
            '  lodash: 4.17.21\n'
          );
        }
      );
    });
  });

  describe('addScoutOverrides', () => {

    it('does nothing when scout block is absent', async () => {
      await withPnpmWorkspaceYaml(
        'packages:\n' +
        '  - pkg-a\n' +
        'overrides:\n' +
        '  lodash: 4.17.21\n',
        async pnpmWorkspaceYaml => {
          pnpmWorkspaceYaml.addScoutOverrides();

          await pnpmWorkspaceYaml.flush();
          const content = await fs.readFile(pnpmWorkspaceYaml.path, 'utf8');
          assert.equal(
            content,
            'packages:\n' +
            '  - pkg-a\n' +
            'overrides:\n' +
            '  lodash: 4.17.21\n'
          );
        }
      );
    });

    it('adds anchor to scout overrides and creates overrides with merge key when no overrides section exists', async () => {
      await withPnpmWorkspaceYaml(
        'packages:\n' +
        '  - pkg-a\n' +
        'scout:\n' +
        '  overrides:\n' +
        '    express: 4.18.0\n',
        async pnpmWorkspaceYaml => {
          pnpmWorkspaceYaml.addScoutOverrides();

          await pnpmWorkspaceYaml.flush();
          const content = await fs.readFile(pnpmWorkspaceYaml.path, 'utf8');
          assert.equal(
            content,
            'packages:\n' +
            '  - pkg-a\n' +
            'scout:\n' +
            '  overrides: &scout-overrides\n' +
            '    express: 4.18.0\n' +
            'overrides:\n' +
            '  <<: *scout-overrides\n'
          );
        }
      );
    });

    it('adds anchor to scout overrides and merge key to existing overrides', async () => {
      await withPnpmWorkspaceYaml(
        'packages:\n' +
        '  - pkg-a\n' +
        'scout:\n' +
        '  overrides:\n' +
        '    express: 4.18.0\n' +
        'overrides:\n' +
        '  lodash: 4.17.21\n',
        async pnpmWorkspaceYaml => {
          pnpmWorkspaceYaml.addScoutOverrides();

          await pnpmWorkspaceYaml.flush();
          const content = await fs.readFile(pnpmWorkspaceYaml.path, 'utf8');
          assert.equal(
            content,
            'packages:\n' +
            '  - pkg-a\n' +
            'scout:\n' +
            '  overrides: &scout-overrides\n' +
            '    express: 4.18.0\n' +
            'overrides:\n' +
            '  <<: *scout-overrides\n' +
            '  lodash: 4.17.21\n'
          );
        }
      );
    });

    it('is a no-op when merge key already correctly links to scout overrides', async () => {
      await withPnpmWorkspaceYaml(
        'packages:\n' +
        '  - pkg-a\n' +
        'scout:\n' +
        '  overrides: &scout-overrides\n' +
        '    express: 4.18.0\n' +
        'overrides:\n' +
        '  <<: *scout-overrides\n',
        async pnpmWorkspaceYaml => {
          pnpmWorkspaceYaml.addScoutOverrides();

          await pnpmWorkspaceYaml.flush();
          const content = await fs.readFile(pnpmWorkspaceYaml.path, 'utf8');
          assert.equal(
            content,
            'packages:\n' +
            '  - pkg-a\n' +
            'scout:\n' +
            '  overrides: &scout-overrides\n' +
            '    express: 4.18.0\n' +
            'overrides:\n' +
            '  <<: *scout-overrides\n'
          );
        }
      );
    });

    it('moves merge key to beginning when present but not first in overrides', async () => {
      await withPnpmWorkspaceYaml(
        'packages:\n' +
        '  - pkg-a\n' +
        'scout:\n' +
        '  overrides: &scout-overrides\n' +
        '    express: 4.18.0\n' +
        'overrides:\n' +
        '  lodash: 4.17.21\n' +
        '  <<: *scout-overrides\n',
        async pnpmWorkspaceYaml => {
          pnpmWorkspaceYaml.addScoutOverrides();

          await pnpmWorkspaceYaml.flush();
          const content = await fs.readFile(pnpmWorkspaceYaml.path, 'utf8');
          assert.equal(
            content,
            'packages:\n' +
            '  - pkg-a\n' +
            'scout:\n' +
            '  overrides: &scout-overrides\n' +
            '    express: 4.18.0\n' +
            'overrides:\n' +
            '  <<: *scout-overrides\n' +
            '  lodash: 4.17.21\n'
          );
        }
      );
    });

    it('moves overrides after scout when overrides precedes scout block', async () => {
      await withPnpmWorkspaceYaml(
        'packages:\n' +
        '  - pkg-a\n' +
        'overrides:\n' +
        '  lodash: 4.17.21\n' +
        'scout:\n' +
        '  overrides:\n' +
        '    express: 4.18.0\n',
        async pnpmWorkspaceYaml => {
          pnpmWorkspaceYaml.addScoutOverrides();

          await pnpmWorkspaceYaml.flush();
          const content = await fs.readFile(pnpmWorkspaceYaml.path, 'utf8');
          assert.equal(
            content,
            'packages:\n' +
            '  - pkg-a\n' +
            'scout:\n' +
            '  overrides: &scout-overrides\n' +
            '    express: 4.18.0\n' +
            'overrides:\n' +
            '  <<: *scout-overrides\n' +
            '  lodash: 4.17.21\n'
          );
        }
      );
    });
  });

  describe('updateScoutOverrides', () => {

    it('creates scout block with the given overrides and overrides using a merge key when no overrides exist', async () => {
      await withPnpmWorkspaceYaml(
        'packages:\n' +
        '  - pkg-a\n',
        async pnpmWorkspaceYaml => {
          pnpmWorkspaceYaml.updateScoutOverrides({lodash: '4.17.21', express: '4.18.0'});

          await pnpmWorkspaceYaml.flush();
          const content = await fs.readFile(pnpmWorkspaceYaml.path, 'utf8');
          assert.equal(
            content,
            'packages:\n' +
            '  - pkg-a\n' +
            'scout:\n' +
            '  overrides: &scout-overrides\n' +
            '    lodash: 4.17.21\n' +
            '    express: 4.18.0\n' +
            'overrides:\n' +
            '  <<: *scout-overrides\n'
          );
        }
      );
    });

    it('creates scout block with the given overrides and adds merge key to existing overrides', async () => {
      await withPnpmWorkspaceYaml(
        'packages:\n' +
        '  - pkg-a\n' +
        'overrides:\n' +
        '  lodash: 4.17.20\n',
        async pnpmWorkspaceYaml => {
          pnpmWorkspaceYaml.updateScoutOverrides({lodash: '4.17.21'});

          await pnpmWorkspaceYaml.flush();
          const content = await fs.readFile(pnpmWorkspaceYaml.path, 'utf8');
          assert.equal(
            content,
            'packages:\n' +
            '  - pkg-a\n' +
            'scout:\n' +
            '  overrides: &scout-overrides\n' +
            '    lodash: 4.17.21\n' +
            'overrides:\n' +
            '  <<: *scout-overrides\n' +
            '  lodash: 4.17.20\n'
          );
        }
      );
    });

    it('ensures merge key is the first entry of the overrides', async () => {
      await withPnpmWorkspaceYaml(
        'packages:\n' +
        '  - pkg-a\n' +
        'scout:\n' +
        '  overrides: &scout-overrides\n' +
        '    lodash: 4.17.21\n' +
        'overrides:\n' +
        '  lodash: 4.17.20\n' +
        '  <<: *scout-overrides\n',
        async pnpmWorkspaceYaml => {
          pnpmWorkspaceYaml.updateScoutOverrides({lodash: '4.17.21'});

          await pnpmWorkspaceYaml.flush();
          const content = await fs.readFile(pnpmWorkspaceYaml.path, 'utf8');
          assert.equal(
            content,
            'packages:\n' +
            '  - pkg-a\n' +
            'scout:\n' +
            '  overrides: &scout-overrides\n' +
            '    lodash: 4.17.21\n' +
            'overrides:\n' +
            '  <<: *scout-overrides\n' +
            '  lodash: 4.17.20\n'
          );
        }
      );
    });

    it('ensures overrides come after the scout block', async () => {
      await withPnpmWorkspaceYaml(
        'packages:\n' +
        '  - pkg-a\n' +
        'overrides:\n' +
        '  lodash: 4.17.20\n' +
        'scout:\n' +
        '  overrides:\n' +
        '    lodash: 4.17.21\n',
        async pnpmWorkspaceYaml => {
          pnpmWorkspaceYaml.updateScoutOverrides({lodash: '4.17.21'});

          await pnpmWorkspaceYaml.flush();
          const content = await fs.readFile(pnpmWorkspaceYaml.path, 'utf8');
          assert.equal(
            content,
            'packages:\n' +
            '  - pkg-a\n' +
            'scout:\n' +
            '  overrides: &scout-overrides\n' +
            '    lodash: 4.17.21\n' +
            'overrides:\n' +
            '  <<: *scout-overrides\n' +
            '  lodash: 4.17.20\n'
          );
        }
      );
    });

    it('updates existing scout overrides with new overrides', async () => {
      await withPnpmWorkspaceYaml(
        'packages:\n' +
        '  - pkg-a\n' +
        'scout:\n' +
        '  overrides: &scout-overrides\n' +
        '    lodash: 4.17.20\n' +
        'overrides:\n' +
        '  <<: *scout-overrides\n',
        async pnpmWorkspaceYaml => {
          pnpmWorkspaceYaml.updateScoutOverrides({lodash: '4.17.21', express: '4.18.0'});

          await pnpmWorkspaceYaml.flush();
          const content = await fs.readFile(pnpmWorkspaceYaml.path, 'utf8');
          assert.equal(
            content,
            'packages:\n' +
            '  - pkg-a\n' +
            'scout:\n' +
            '  overrides: &scout-overrides\n' +
            '    lodash: 4.17.21\n' +
            '    express: 4.18.0\n' +
            'overrides:\n' +
            '  <<: *scout-overrides\n'
          );
        }
      );
    });

    it('handles empty overrides', async () => {
      await withPnpmWorkspaceYaml(
        'packages:\n' +
        '  - pkg-a\n' +
        'scout:\n' +
        '  overrides: &scout-overrides\n' +
        '    lodash: 4.17.20\n' +
        'overrides:\n' +
        '  <<: *scout-overrides\n',
        async pnpmWorkspaceYaml => {
          pnpmWorkspaceYaml.updateScoutOverrides({});

          await pnpmWorkspaceYaml.flush();
          const content = await fs.readFile(pnpmWorkspaceYaml.path, 'utf8');
          assert.equal(
            content,
            'packages:\n' +
            '  - pkg-a\n' +
            'scout:\n' +
            '  overrides: &scout-overrides {}\n' +
            'overrides:\n' +
            '  <<: *scout-overrides\n'
          );
        }
      );
    });
  });

  describe('flush', () => {

    it('writes the in-memory YAML document to disk', async () => {
      await withPnpmWorkspaceYaml(
        'packages:\n' +
        '  - pkg-a\n',
        async pnpmWorkspaceYaml => {
          await fs.rm(pnpmWorkspaceYaml.path);
          await pnpmWorkspaceYaml.flush();
          const content = await fs.readFile(pnpmWorkspaceYaml.path, 'utf8');
          assert.equal(
            content,
            'packages:\n' +
            '  - pkg-a\n'
          );
        }
      );
    });

    it('persists doc changes to disk', async () => {
      await withPnpmWorkspaceYaml(
        'packages:\n' +
        '  - old-package\n',
        async pnpmWorkspaceYaml => {
          pnpmWorkspaceYaml.doc.set('packages', ['new-package']);
          await pnpmWorkspaceYaml.flush();
          const content = await fs.readFile(pnpmWorkspaceYaml.path, 'utf8');
          assert.equal(
            content,
            'packages:\n' +
            '  - new-package\n'
          );
        }
      );
    });
  });

  describe('findWorkspaceFileDir', () => {

    it('returns undefined for falsy dir', async () => {
      assert.equal(await PnpmWorkspaceYaml.findWorkspaceFileDir(null), undefined);
      assert.equal(await PnpmWorkspaceYaml.findWorkspaceFileDir(undefined), undefined);
      assert.equal(await PnpmWorkspaceYaml.findWorkspaceFileDir(''), undefined);
    });

    it('returns undefined when no pnpm-workspace.yaml exists in the directory tree', async () => {
      await withTempDir(async dir => {
        const level3 = join(dir, 'level1', 'level2', 'level3');
        await fs.mkdir(level3, {recursive: true});
        const workspaceFileDir = await PnpmWorkspaceYaml.findWorkspaceFileDir(level3);
        assert.equal(workspaceFileDir, undefined);
      });
    });

    it('returns the directory when it directly contains a pnpm-workspace.yaml', async () => {
      await withTempDir(async dir => {
        const level3 = join(dir, 'level1', 'level2', 'level3');
        await fs.mkdir(level3, {recursive: true});
        await fs.writeFile(join(level3, 'pnpm-workspace.yaml'), '');
        const workspaceFileDir = await PnpmWorkspaceYaml.findWorkspaceFileDir(level3);
        assert.equal(workspaceFileDir, level3);
      });
    });

    it('returns a parent directory when pnpm-workspace.yaml is found while traversing upwards', async () => {
      await withTempDir(async dir => {
        const level2 = join(dir, 'level1', 'level2');
        const level3 = join(level2, 'level3');
        await fs.mkdir(level3, {recursive: true});
        await fs.writeFile(join(level2, 'pnpm-workspace.yaml'), '');
        const workspaceFileDir = await PnpmWorkspaceYaml.findWorkspaceFileDir(level3);
        assert.equal(workspaceFileDir, level2);
      });
    });

    it('returns the directory closest to the filesystem root when multiple pnpm-workspace.yaml files exist', async () => {
      await withTempDir(async dir => {
        const level1 = join(dir, 'level1');
        const level3 = join(level1, 'level2', 'level3');
        await fs.mkdir(level3, {recursive: true});
        await fs.writeFile(join(level1, 'pnpm-workspace.yaml'), '');
        await fs.writeFile(join(level3, 'pnpm-workspace.yaml'), '');
        const workspaceFileDir = await PnpmWorkspaceYaml.findWorkspaceFileDir(level3);
        assert.equal(workspaceFileDir, level1);
      });
    });
  });

});

async function withPnpmWorkspaceYaml(content: string, callback: (pnpmWorkspaceYaml: PnpmWorkspaceYaml) => Promise<void>): Promise<void> {
  await withTempDir(async dir => {
    const filePath = join(dir, 'pnpm-workspace.yaml');
    await fs.writeFile(filePath, content);
    const pnpmWorkspaceYaml = await PnpmWorkspaceYaml.parse(filePath);
    await callback(pnpmWorkspaceYaml);
  });
}

async function withTempDir(callback: (dir: string) => Promise<void>): Promise<void> {
  const dir = await fs.mkdtemp(join(tmpdir(), 'pwsy-test-'));
  try {
    await callback(dir);
  } finally {
    await fs.rm(dir, {recursive: true});
  }
}
