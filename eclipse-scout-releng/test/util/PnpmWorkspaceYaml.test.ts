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
import {join, relative} from 'node:path';
import {describe, it} from 'node:test';
import {PnpmWorkspaceYaml, toDependencyOfSelector, toSelectorWithoutVersions, toSelectorWithVersion} from '../../scripts/util/PnpmWorkspaceYaml.ts';
import YAML from 'yaml';

describe('PnpmWorkspaceYaml', () => {

  describe('toSelectorWithoutVersions', () => {

    it('removes the versions of the parent and the dependency', () => {
      const cases: [string, string][] = [
        ['lodash', 'lodash'],
        ['lodash@4.17.21', 'lodash'],
        ['@scope/a@^1.0.0', '@scope/a'],
        ['app>lodash', 'app>lodash'],
        ['app@1.0.0>lodash@4.17.21', 'app>lodash'],
        ['@scope/app@>=1.0.0 <2.0.0>@scope/b@>=2.0.0', '@scope/app>@scope/b']
      ];
      for (const [selector, expected] of cases) {
        assert.equal(toSelectorWithoutVersions(selector), expected, selector);
      }
    });
  });

  describe('toDependencyOfSelector', () => {

    it('returns the dependency without version', () => {
      const cases: [string, string][] = [
        ['lodash', 'lodash'],
        ['lodash@4.17.21', 'lodash'],
        ['@scope/a@^1.0.0', '@scope/a'],
        ['app>lodash', 'lodash'],
        ['app@1.0.0>@scope/b@2.0.0', '@scope/b'],
        ['@scope/app@>=1.0.0 <2.0.0>@scope/b@>=2.0.0', '@scope/b']
      ];
      for (const [selector, expected] of cases) {
        assert.equal(toDependencyOfSelector(selector), expected, selector);
      }
    });
  });

  describe('toSelectorWithVersion', () => {

    it('adds the version to the dependency if it has no version and the version is no npm alias', () => {
      const cases: [string, string, string][] = [
        ['lodash', '4.17.21', 'lodash@4.17.21'],
        ['@scope/a', '1.0.0', '@scope/a@1.0.0'],
        ['app>lodash', '4.17.21', 'app>lodash@4.17.21'],
        ['app@1.0.0>@scope/b', '2.0.0', 'app@1.0.0>@scope/b@2.0.0'],
        ['lodash@^4.0.0', '4.17.21', 'lodash@^4.0.0'],
        ['app>lodash@4.17.20', '4.17.21', 'app>lodash@4.17.20'],
        ['lodash', 'npm:lodash-es@4.17.21', 'lodash'],
        ['foo', 'link:../foo', 'foo'],
        ['bar', '-', 'bar']
      ];
      for (const [selector, version, expected] of cases) {
        assert.equal(toSelectorWithVersion(selector, version), expected, selector);
      }
    });
  });

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

    it('throws when the YAML contains parse errors', async () => {
      await withTempDir(async dir => {
        const filePath = join(dir, 'pnpm-workspace.yaml');
        await fs.writeFile(filePath, '{\n' +
          '  packages:\n' +
          '    - pkg-a\n' +
          '}\n');

        await assert.rejects(() => PnpmWorkspaceYaml.parse(filePath), /Error parsing yaml/);
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
        await fs.writeFile(join(dir, 'pnpm-workspacex.yaml'), '');
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
        assert.deepEqual(pnpmWorkspaceFiles, [join(level1, 'level2', 'pnpm-workspace.yaml'), join(level1, 'pnpm-workspace.yaml'), join(dir, 'pnpm-workspace.yaml')].sort());
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

    /**
     * Creates a pnpm-workspace.yaml with the given packages in the directory 'ws' and a package.json in each of the given package directories (relative to 'ws').
     * @returns the packages of the pnpm-workspace.yaml relative to 'ws'.
     */
    async function getPackages(packages: string[], packageDirs: string[], otherDirs: string[] = []): Promise<string[]> {
      let result: string[];
      await withTempDir(async dir => {
        const wsDir = join(dir, 'ws');
        for (const packageDir of packageDirs) {
          await fs.mkdir(join(wsDir, packageDir), {recursive: true});
          await fs.writeFile(join(wsDir, packageDir, 'package.json'), '{}');
        }
        for (const otherDir of otherDirs) {
          await fs.mkdir(join(wsDir, otherDir), {recursive: true});
        }
        await fs.mkdir(wsDir, {recursive: true});
        await fs.writeFile(join(wsDir, 'pnpm-workspace.yaml'), YAML.stringify({packages}));
        const pnpmWorkspaceYaml = await PnpmWorkspaceYaml.parse(wsDir);
        const realDir = await fs.realpath(dir);
        // relative to the workspace directory to compare after the temp dir was removed
        result = (await pnpmWorkspaceYaml.getPackages()).map(p => relative(join(realDir, 'ws'), p));
      });
      return result;
    }

    it('returns the sorted absolute paths of all packages', async () => {
      await withTempDir(async dir => {
        await fs.mkdir(join(dir, 'sub', 'pkg-b'), {recursive: true});
        await fs.mkdir(join(dir, 'pkg-a'), {recursive: true});
        await fs.writeFile(join(dir, 'sub', 'pkg-b', 'package.json'), '{}');
        await fs.writeFile(join(dir, 'pkg-a', 'package.json'), '{}');
        await fs.writeFile(join(dir, 'pnpm-workspace.yaml'), 'packages:\n  - sub/pkg-b\n  - pkg-a\n');

        const pnpmWorkspaceYaml = await PnpmWorkspaceYaml.parse(dir);
        const realDir = await fs.realpath(dir);
        assert.deepEqual(await pnpmWorkspaceYaml.getPackages(), [join(realDir, 'pkg-a'), join(realDir, 'sub', 'pkg-b')]);
      });
    });

    it('includes the directory of the pnpm-workspace.yaml for the pattern \'.\'', async () => {
      const packages = await getPackages(['.', 'pkg-a'], ['.', 'pkg-a']);
      assert.deepEqual(packages, ['', 'pkg-a']);
    });

    it('resolves relative paths against the directory of the pnpm-workspace.yaml', async () => {
      const packages = await getPackages(['../sibling'], ['../sibling']);
      assert.deepEqual(packages, [join('..', 'sibling')]);
    });

    it('expands glob patterns', async () => {
      const packages = await getPackages(['packages/*', 'apps/**'], ['packages/a', 'packages/b', 'packages/b/nested', 'apps/x', 'apps/group/y', 'other/z']);
      assert.deepEqual(packages, [join('apps', 'group', 'y'), join('apps', 'x'), join('packages', 'a'), join('packages', 'b')]);
    });

    it('excludes directories matching a negated pattern', async () => {
      const packages = await getPackages(['packages/**', '!packages/b', '!**/test/**'], ['packages/a', 'packages/b', 'packages/b/nested', 'packages/a/test/t']);
      assert.deepEqual(packages, [join('packages', 'a')]);
    });

    it('ignores directories without package.json and in node_modules or bower_components', async () => {
      const packages = await getPackages(['pkg-a', 'no-package', '**'], ['pkg-a', 'pkg-a/node_modules/dep', 'bower_components/dep'], ['no-package']);
      assert.deepEqual(packages, ['pkg-a']);
    });

    it('returns empty array when packages list is empty', async () => {
      await withPnpmWorkspaceYaml('packages: []\n', async pnpmWorkspaceYaml => {
        assert.deepEqual(await pnpmWorkspaceYaml.getPackages(), []);
      });
    });

    it('returns empty array when packages list does not exist', async () => {
      await withPnpmWorkspaceYaml('', async pnpmWorkspaceYaml => {
        assert.deepEqual(await pnpmWorkspaceYaml.getPackages(), []);
      });
    });
  });

  describe('updateScoutOverrides', () => {

    it('creates the overrides with the generated overrides between markers', async () => {
      await assertTransformed(
        'packages:\n' +
        '  - pkg-a\n',
        pnpmWorkspaceYaml => pnpmWorkspaceYaml.updateScoutOverrides({lodash: '4.17.21', '@scope/a>b': 'npm:b@1.0.0', x: '1.0'}),
        'packages:\n' +
        '  - pkg-a\n' +
        'overrides:\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  lodash: 4.17.21\n' +
        '  "@scope/a>b": npm:b@1.0.0\n' +
        '  x: "1.0"\n' +
        '  # <<< end of generated overrides\n'
      );
    });

    it('adds the markers after existing overrides and skips generated overrides that exist manually', async () => {
      await assertTransformed(
        'overrides:\n' +
        '  # pinned manually\n' +
        '  lodash: 4.17.20\n',
        pnpmWorkspaceYaml => pnpmWorkspaceYaml.updateScoutOverrides({lodash: '4.17.21', express: '4.18.0'}),
        'overrides:\n' +
        '  # pinned manually\n' +
        '  lodash: 4.17.20\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  express: 4.18.0\n' +
        '  # <<< end of generated overrides\n'
      );
    });

    it('skips generated overrides for the parent and dependency of a manual override, regardless of the versions in the selectors', async () => {
      await assertTransformed(
        'overrides:\n' +
        '  lodash: 4.17.20\n' +
        '  app-a>express@^4.0.0: 4.18.0\n',
        pnpmWorkspaceYaml => pnpmWorkspaceYaml.updateScoutOverrides({
          'lodash@4.17.21': '4.17.21',
          'app-a@1.0.0>express@4.18.2': '4.18.2',
          'app-b>express@4.18.2': '4.18.2',
          'app-a>lodash@4.17.21': '4.17.21'
        }),
        'overrides:\n' +
        '  lodash: 4.17.20\n' +
        '  app-a>express@^4.0.0: 4.18.0\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  app-b>express@4.18.2: 4.18.2\n' +
        '  app-a>lodash@4.17.21: 4.17.21\n' +
        '  # <<< end of generated overrides\n'
      );
    });

    it('replaces only the overrides between the markers', async () => {
      await assertTransformed(
        'overrides:\n' +
        '  a: 1.0.0\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  old: 1.0.0\n' +
        '  b: 1.0.0\n' +
        '  # <<< end of generated overrides\n' +
        '  # added after the markers, e.g. by pnpm audit --fix\n' +
        '  c: ">=2.0.0"\n' +
        'other: x\n',
        pnpmWorkspaceYaml => pnpmWorkspaceYaml.updateScoutOverrides({b: '2.0.0', c: '2.0.1', d: '1.0.0'}),
        'overrides:\n' +
        '  a: 1.0.0\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  b: 2.0.0\n' +
        '  d: 1.0.0\n' +
        '  # <<< end of generated overrides\n' +
        '  # added after the markers, e.g. by pnpm audit --fix\n' +
        '  c: ">=2.0.0"\n' +
        'other: x\n'
      );
    });

    it('keeps the markers if there are no generated overrides', async () => {
      await assertTransformed(
        'overrides:\n' +
        '  a: 1.0.0\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  b: 1.0.0\n' +
        '  # <<< end of generated overrides\n',
        pnpmWorkspaceYaml => pnpmWorkspaceYaml.updateScoutOverrides({}),
        'overrides:\n' +
        '  a: 1.0.0\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  # <<< end of generated overrides\n'
      );
    });

    it('migrates the legacy scout block to the position of the merge key', async () => {
      await assertTransformed(
        'packages:\n' +
        '  - pkg-a\n' +
        'scout:\n' +
        '  overrides: &scout-overrides\n' +
        '    lodash: 4.17.21\n' +
        'overrides:\n' +
        '  <<: *scout-overrides\n' +
        '  express: 5.0.0\n',
        pnpmWorkspaceYaml => pnpmWorkspaceYaml.updateScoutOverrides({lodash: '4.17.22', express: '4.18.0'}),
        'packages:\n' +
        '  - pkg-a\n' +
        'overrides:\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  lodash: 4.17.22\n' +
        '  # <<< end of generated overrides\n' +
        '  express: 5.0.0\n'
      );
    });

    it('fails if a marker is missing or duplicated', async () => {
      for (const content of [
        'overrides:\n' + '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' + '  a: 1.0.0\n',
        'overrides:\n' + '  a: 1.0.0\n' + '  # <<< end of generated overrides\n',
        'overrides:\n' + '  # <<< end of generated overrides\n' + '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n',
        'overrides:\n' + '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' + '  # <<< end of generated overrides\n'
        + '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' + '  # <<< end of generated overrides\n'
      ]) {
        await withPnpmWorkspaceYaml(content, async pnpmWorkspaceYaml => {
          assert.throws(() => pnpmWorkspaceYaml.updateScoutOverrides({}), /must contain the comment/);
        });
      }
    });
  });

  describe('disableScoutOverrides', () => {

    it('does nothing without generated overrides', async () => {
      for (const content of [
        'packages:\n' +
        '  - pkg-a\n',
        'overrides:\n' +
        '  # pinned manually\n' +
        '  lodash: 4.17.21\n'
      ]) {
        await assertTransformed(content, pnpmWorkspaceYaml => pnpmWorkspaceYaml.disableScoutOverrides(), content);
      }
    });

    it('turns the generated overrides into comments', async () => {
      await assertTransformed(
        'overrides:\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  "@scope/a>b": npm:b@1.0.0\n' +
        '  x: "1.0"\n' +
        '  # <<< end of generated overrides\n' +
        '  lodash: 4.17.20\n',
        pnpmWorkspaceYaml => pnpmWorkspaceYaml.disableScoutOverrides(),
        'overrides:\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  # "@scope/a>b": npm:b@1.0.0\n' +
        '  # x: "1.0"\n' +
        '  # <<< end of generated overrides\n' +
        '  lodash: 4.17.20\n'
      );
    });

    it('writes an empty map if there are no overrides written manually', async () => {
      await assertTransformed(
        'overrides:\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  lodash: 4.17.21\n' +
        '  # <<< end of generated overrides\n',
        pnpmWorkspaceYaml => pnpmWorkspaceYaml.disableScoutOverrides(),
        'overrides: {}\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  # lodash: 4.17.21\n' +
        '  # <<< end of generated overrides\n'
      );
    });

    it('migrates the legacy scout block', async () => {
      await assertTransformed(
        'scout:\n' +
        '  overrides: &scout-overrides\n' +
        '    lodash: 4.17.21\n' +
        'overrides:\n' +
        '  <<: *scout-overrides\n' +
        '  express: 5.0.0\n',
        pnpmWorkspaceYaml => pnpmWorkspaceYaml.disableScoutOverrides(),
        'overrides:\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  # lodash@4.17.21: 4.17.21\n' +
        '  # <<< end of generated overrides\n' +
        '  express: 5.0.0\n'
      );
    });
  });

  describe('getEnabledScoutOverrides', () => {

    it('returns the enabled generated overrides without the manual ones', async () => {
      await withPnpmWorkspaceYaml(
        'overrides:\n' +
        '  express: 5.0.0\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  lodash@4.17.21: 4.17.21\n' +
        '  "@scope/a>b": npm:b@1.0.0\n' +
        '  # <<< end of generated overrides\n',
        async pnpmWorkspaceYaml => {
          assert.deepEqual(pnpmWorkspaceYaml.getEnabledScoutOverrides(), {'lodash@4.17.21': '4.17.21', '@scope/a>b': 'npm:b@1.0.0'});
        });
    });

    it('returns no disabled overrides', async () => {
      await withPnpmWorkspaceYaml(
        'overrides: {}\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  # lodash@4.17.21: 4.17.21\n' +
        '  # <<< end of generated overrides\n',
        async pnpmWorkspaceYaml => {
          assert.deepEqual(pnpmWorkspaceYaml.getEnabledScoutOverrides(), {});
        });
    });

    it('returns the overrides of the legacy format with version selectors', async () => {
      await withPnpmWorkspaceYaml(
        'scout:\n' +
        '  overrides: &scout-overrides\n' +
        '    lodash: 4.17.21\n' +
        'overrides:\n' +
        '  <<: *scout-overrides\n',
        async pnpmWorkspaceYaml => {
          assert.deepEqual(pnpmWorkspaceYaml.getEnabledScoutOverrides(), {'lodash@4.17.21': '4.17.21'});
        });
    });

    it('returns nothing without overrides', async () => {
      await withPnpmWorkspaceYaml('packages:\n  - a\n', async pnpmWorkspaceYaml => {
        assert.deepEqual(pnpmWorkspaceYaml.getEnabledScoutOverrides(), {});
      });
    });
  });

  describe('migrateLegacyScoutOverrides', () => {

    it('migrates the legacy scout block with version selectors, except for npm aliases and selectors with version', async () => {
      await withPnpmWorkspaceYaml(
        'packages:\n' +
        '  - pkg-a\n' +
        'scout:\n' +
        '  overrides: &scout-overrides\n' +
        '    lodash: 4.17.21\n' +
        '    "@scope/a>@scope/b": 2.0.0\n' +
        '    c@1.0.0>d: 3.0.0\n' +
        '    e@^1.0.0: 1.2.0\n' +
        '    f: npm:g@1.0.0\n' +
        'overrides:\n' +
        '  <<: *scout-overrides\n' +
        '  express: 5.0.0\n',
        async pnpmWorkspaceYaml => {
          assert.equal(pnpmWorkspaceYaml.migrateLegacyScoutOverrides(), true);
          assert.equal(String(pnpmWorkspaceYaml.doc),
            'packages:\n' +
            '  - pkg-a\n' +
            'overrides:\n' +
            '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
            '  lodash@4.17.21: 4.17.21\n' +
            '  "@scope/a>@scope/b@2.0.0": 2.0.0\n' +
            '  c@1.0.0>d@3.0.0: 3.0.0\n' +
            '  e@^1.0.0: 1.2.0\n' +
            '  f: npm:g@1.0.0\n' +
            '  # <<< end of generated overrides\n' +
            '  express: 5.0.0\n');
        });
    });

    it('keeps the text of numeric legacy values', async () => {
      await withPnpmWorkspaceYaml(
        'scout:\n' +
        '  overrides: &scout-overrides\n' +
        '    x: 1.0\n' +
        '    y: 1.10\n' +
        'overrides:\n' +
        '  <<: *scout-overrides\n',
        async pnpmWorkspaceYaml => {
          assert.deepEqual(pnpmWorkspaceYaml.getEnabledScoutOverrides(), {'x@1.0': '1.0', 'y@1.10': '1.10'});
        });
    });

    it('does nothing if there is no legacy scout block', async () => {
      const content =
        'overrides:\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  # lodash@4.17.21: 4.17.21\n' +
        '  # <<< end of generated overrides\n';
      await withPnpmWorkspaceYaml(content, async pnpmWorkspaceYaml => {
        assert.equal(pnpmWorkspaceYaml.migrateLegacyScoutOverrides(), false);
        assert.equal(String(pnpmWorkspaceYaml.doc), content);
      });
    });
  });

  describe('enableScoutOverrides', () => {

    it('does nothing without generated overrides', async () => {
      const content =
        'overrides:\n' +
        '  lodash: 4.17.21\n';
      await assertTransformed(content, pnpmWorkspaceYaml => pnpmWorkspaceYaml.enableScoutOverrides(), content);
    });

    it('turns the commented generated overrides into overrides, except the ones written manually in the meantime', async () => {
      await assertTransformed(
        'overrides: {}\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  # "@scope/a>b": npm:b@1.0.0\n' +
        '  # lodash: 4.17.21\n' +
        '  # x: "1.0"\n' +
        '  # <<< end of generated overrides\n',
        pnpmWorkspaceYaml => {
          pnpmWorkspaceYaml.doc.setIn(['overrides', 'lodash'], '4.17.20');
          pnpmWorkspaceYaml.enableScoutOverrides();
        },
        'overrides:\n' +
        '  lodash: 4.17.20\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  "@scope/a>b": npm:b@1.0.0\n' +
        '  x: "1.0"\n' +
        '  # <<< end of generated overrides\n'
      );
    });

    it('ignores comment lines between the markers that are no disabled override', async () => {
      await assertTransformed(
        'overrides: {}\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  # pinned because of CVE\n' +
        '  # see: https://example.com\n' +
        '  # a: 1, b: 2\n' +
        '  # - x\n' +
        '  # foo: bar: baz\n' +
        '  # lodash@4.17.21: 4.17.21\n' +
        '  # "@scope/a>b": npm:b@1.0.0\n' +
        '  # <<< end of generated overrides\n',
        pnpmWorkspaceYaml => pnpmWorkspaceYaml.enableScoutOverrides(),
        'overrides:\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  lodash@4.17.21: 4.17.21\n' +
        '  "@scope/a>b": npm:b@1.0.0\n' +
        '  # <<< end of generated overrides\n'
      );
    });

    it('migrates the legacy scout block without merge key', async () => {
      await assertTransformed(
        'scout:\n' +
        '  overrides: &scout-overrides\n' +
        '    lodash: 4.17.21\n' +
        'overrides:\n' +
        '  express: 5.0.0\n',
        pnpmWorkspaceYaml => pnpmWorkspaceYaml.enableScoutOverrides(),
        'overrides:\n' +
        '  express: 5.0.0\n' +
        '  # >>> generated by @eclipse-scout/releng, do not edit until the end marker\n' +
        '  lodash@4.17.21: 4.17.21\n' +
        '  # <<< end of generated overrides\n'
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

    it('resolves a relative directory', async () => {
      await withTempDir(async dir => {
        const level2 = join(dir, 'level1', 'level2');
        await fs.mkdir(level2, {recursive: true});
        await fs.writeFile(join(dir, 'level1', 'pnpm-workspace.yaml'), '');

        const workspaceFileDir = await PnpmWorkspaceYaml.findWorkspaceFileDir(relative(process.cwd(), level2));
        assert.equal(workspaceFileDir, join(dir, 'level1'));
      });
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

/**
 * Applies the given transformation to a `pnpm-workspace.yaml` with the given content and asserts the content written by {@link PnpmWorkspaceYaml.flush}.
 */
async function assertTransformed(content: string, transformation: (pnpmWorkspaceYaml: PnpmWorkspaceYaml) => void, expected: string): Promise<void> {
  await withPnpmWorkspaceYaml(content, async pnpmWorkspaceYaml => {
    transformation(pnpmWorkspaceYaml);
    await pnpmWorkspaceYaml.flush();
    assert.equal(await fs.readFile(pnpmWorkspaceYaml.path, 'utf8'), expected);
  });
}
