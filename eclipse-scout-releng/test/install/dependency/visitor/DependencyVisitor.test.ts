/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import assert from 'node:assert/strict';
import {describe, it} from 'node:test';
import {promises as fs} from 'fs';
import {isAbsolute, join} from 'node:path';
import {tmpdir} from 'node:os';
import pnpm from '../../../../scripts/util/pnpm.ts';
import {visitDependenciesForPackages} from '../../../../scripts/install/dependency/visitor/DependencyVisitor.ts';
import type {DependencyMetaData} from '../../../../scripts/install/dependency/visitor/DependencyMetaData.ts';

describe('DependencyVisitor', () => {

  describe('visitDependenciesForPackages', () => {

    it('visits the dependency tree recursively', async () => {
      await withInstalledProject(
        {
          pnpmWorkspaceYaml: 'packages:\n' +
            '  - pkg-a\n' +
            '  - pkg-b\n',
          'pkg-a': {
            packageJson: {
              name: 'pkg-a',
              version: '1.0.0-snapshot',
              dependencies: {
                'pkg-b': '>=1.0.0-snapshot <1.0.0',
                '@eclipse-scout/core': '26.1.7'
              }
            }
          },
          'pkg-b': {
            packageJson: {
              name: 'pkg-b',
              version: '1.0.0-snapshot',
              dependencies: {
                '@eclipse-scout/chart': '26.1.7'
              }
            }
          }
        },
        async projectDir => {
          const dependencies = new Map<string, Set<string>>();
          const packages = ['pkg-a', 'pkg-b'];
          await visitDependenciesForPackages(projectDir, packages, async (parent: DependencyMetaData, dependency: DependencyMetaData) => {
            assert.ok(isAbsolute(parent.path));
            assert.ok(isAbsolute(dependency.path));

            if (packages.indexOf(parent.name) < 0 && !parent.name.startsWith('@eclipse-scout/')) {
              return false;
            }

            if (!dependencies.has(parent.id)) {
              dependencies.set(parent.id, new Set());
            }
            dependencies.get(parent.id).add(dependency.id);

            return true;
          });

          assert.deepEqual(dependencies, new Map([
            ['pkg-a@1.0.0-snapshot', new Set(['pkg-b@link:../pkg-b', '@eclipse-scout/core@26.1.7'])],
            ['pkg-b@1.0.0-snapshot', new Set(['@eclipse-scout/chart@26.1.7'])],
            ['pkg-b@link:../pkg-b', new Set(['@eclipse-scout/chart@26.1.7'])],
            ['@eclipse-scout/core@26.1.7', new Set(['jquery@3.7.1', 'reflect-metadata@0.2.2', 'sourcemapped-stacktrace@1.1.11'])],
            ['@eclipse-scout/chart@26.1.7', new Set(['@eclipse-scout/core@26.1.7', 'chart.js@4.5.1', 'chartjs-plugin-datalabels@2.2.0', 'jquery@3.7.1'])]
          ]));
        }
      );
    });

    it('passes dependency alias information to the visitor', async () => {
      await withInstalledProject(
        {
          pnpmWorkspaceYaml: 'packages:\n' +
            '  - pkg-a\n',
          'pkg-a': {
            packageJson: {
              name: 'pkg-a',
              version: '1.0.0-snapshot',
              dependencies: {
                'fancy-eclipse-scout-core': 'npm:@eclipse-scout/core@26.1.7'
              }
            }
          }
        },
        async projectDir => {
          await visitDependenciesForPackages(projectDir, ['pkg-a'], async (parent: DependencyMetaData, dependency: DependencyMetaData) => {
            if (parent.name !== 'pkg-a') {
              return false;
            }

            assert.equal(dependency.name, '@eclipse-scout/core');
            assert.equal(dependency.alias, 'fancy-eclipse-scout-core');
            assert.equal(dependency.version, '26.1.7');

            return true;
          });
        }
      );
    });

    it('visits devDependencies, optionalDependencies and peerDependencies', async () => {
      await withInstalledProject(
        {
          pnpmWorkspaceYaml: 'packages:\n' +
            '  - pkg-a\n',
          'pkg-a': {
            packageJson: {
              name: 'pkg-a',
              version: '1.0.0-snapshot',
              devDependencies: {
                '@eclipse-scout/core': '26.1.7'
              },
              optionalDependencies: {
                '@eclipse-scout/chart': '26.1.7'
              },
              peerDependencies: {
                '@eclipse-scout/svg': '26.1.7'
              }
            }
          }
        },
        async projectDir => {
          const dependencies = new Set<string>();
          await visitDependenciesForPackages(projectDir, ['pkg-a'], async (parent: DependencyMetaData, dependency: DependencyMetaData) => {
            if (parent.name !== 'pkg-a') {
              return false;
            }

            dependencies.add(dependency.id);
            return true;
          });

          assert.deepEqual(dependencies, new Set(['@eclipse-scout/core@26.1.7', '@eclipse-scout/chart@26.1.7', '@eclipse-scout/svg@26.1.7']));
        }
      );
    });
  });
});

async function withInstalledProject(project: object, callback: (projectDir: string) => Promise<void>) {
  await withTempDir(async dir => {
    for (const [key, value] of Object.entries(project)) {
      if (key === 'pnpmWorkspaceYaml' && typeof value === 'string') {
        await createPnpmWorkspaceYaml(dir, value);
        continue;
      }
      if (typeof value === 'object') {
        await createPackage(dir, key, value);
      }
    }
    await pnpm.fork(dir, 'install', '--recursive', '--no-lockfile', '--ignore-scripts', '--config.link-workspace-packages=true', '--config.prefer-workspace-packages=true');
    await callback(dir);
  });
}

async function createPnpmWorkspaceYaml(dir: string, content: string) {
  await fs.mkdir(dir, {recursive: true});
  await fs.writeFile(join(dir, 'pnpm-workspace.yaml'), content);
}

async function createPackage(dir: string, name: string, pkg: object) {
  const pkgDir = join(dir, name);
  await fs.mkdir(pkgDir, {recursive: true});

  for (const [key, value] of Object.entries(pkg)) {
    if (typeof value !== 'object') {
      continue;
    }
    if (key === 'packageJson') {
      await createPackageJson(pkgDir, value);
      continue;
    }
    await createPackage(pkgDir, key, value);
  }
}

async function createPackageJson(dir: string, packageJson: object) {
  await fs.mkdir(dir, {recursive: true});
  await fs.writeFile(join(dir, 'package.json'), JSON.stringify(packageJson));
}

async function withTempDir(callback: (dir: string) => Promise<void>): Promise<void> {
  const dir = await fs.mkdtemp(join(tmpdir(), 'dv-test-'));
  try {
    await callback(dir);
  } finally {
    await fs.rm(dir, {recursive: true});
  }
}
