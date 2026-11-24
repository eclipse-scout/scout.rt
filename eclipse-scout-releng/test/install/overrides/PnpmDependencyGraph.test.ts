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
import {join, sep} from 'node:path';
import {describe, it} from 'node:test';
import {type DeclaredDependencies, PnpmDependencyGraph} from '../../../scripts/install/overrides/PnpmDependencyGraph.ts';
import {type PnpmDependency, type PnpmListOutput} from '../../../scripts/util/pnpm.ts';

describe('PnpmDependencyGraph', () => {

  describe('getDependencies', () => {

    const storePath = (name: string) => join(sep, 'ws', 'node_modules', '.pnpm', `${name}@1.0.0`, 'node_modules', name);
    const dependency = (name: string, extra: Partial<PnpmDependency> = {}): PnpmDependency => ({from: name, version: '1.0.0', path: storePath(name), ...extra});

    it('returns the dependencies of a package listed in any project, also for deduped occurrences', () => {
      const projects: PnpmListOutput = [
        {name: 'app-a', version: '1.0.0', path: join(sep, 'ws', 'app-a'), private: false, dependencies: {a: dependency('a', {deduped: true, dedupedDependenciesCount: 1})}},
        {name: 'app-b', version: '1.0.0', path: join(sep, 'ws', 'app-b'), private: false, devDependencies: {a: dependency('a', {dependencies: {b: dependency('b', {dependencies: {c: dependency('c')}})}})}}
      ];
      const dependencyGraph = new PnpmDependencyGraph(projects);

      assert.deepEqual(Object.keys(dependencyGraph.getDependencies(storePath('a'))), ['b']);
      assert.deepEqual(Object.keys(dependencyGraph.getDependencies(storePath('b'))), ['c']);
      assert.equal(dependencyGraph.getDependencies(storePath('c')), undefined);
    });
  });

  describe('getVersionsByName', () => {

    it('returns the versions of all projects and packages by package name, including deduped occurrences and without links', () => {
      const store = (name: string, version: string) => join(sep, 'ws', 'node_modules', '.pnpm', `${name}@${version}`, 'node_modules', name);
      const dependencyGraph = new PnpmDependencyGraph([
        {
          name: 'app-a', version: '1.0.0', path: join(sep, 'ws', 'app-a'), private: false, dependencies: {
            'debug': {from: 'debug', version: '4.4.3', path: store('debug', '4.4.3'), dependencies: {ms: {from: 'ms', version: '2.1.3', path: store('ms', '2.1.3')}}},
            'ms-alias': {from: 'ms', version: '2.0.0', path: store('ms', '2.0.0'), deduped: true},
            'app-b': {from: 'app-b', version: 'link:../app-b', path: join(sep, 'ws', 'app-b')}
          }
        },
        {name: 'app-b', version: '2.0.0', path: join(sep, 'ws', 'app-b'), private: false},
        {path: join(sep, 'ws', 'unnamed'), private: false}
      ]);

      assert.deepEqual(dependencyGraph.getVersionsByName(), new Map([
        ['app-a', new Set(['1.0.0'])],
        ['debug', new Set(['4.4.3'])],
        ['ms', new Set(['2.1.3', '2.0.0'])],
        ['app-b', new Set(['2.0.0'])]
      ]));
    });
  });

  describe('isProject', () => {

    it('returns true for the directories of the projects only', () => {
      const dependencyGraph = new PnpmDependencyGraph([{name: 'app', version: '1.0.0', path: join(sep, 'ws', 'app'), private: false}]);
      assert.equal(dependencyGraph.isProject(join(sep, 'ws', 'app')), true);
      assert.equal(dependencyGraph.isProject(join(sep, 'ws', 'node_modules', '.pnpm', 'a@1.0.0', 'node_modules', 'a')), false);
    });
  });

  describe('getProjectRuntimeDependencies', () => {

    it('returns the dependencies and optionalDependencies of the project', () => {
      const dependencyGraph = new PnpmDependencyGraph([{
        name: 'app', version: '1.0.0', path: join(sep, 'ws', 'app'), private: false,
        dependencies: {a: {from: 'a', version: '1.0.0', path: '/a'}},
        optionalDependencies: {b: {from: 'b', version: '1.0.0', path: '/b'}},
        devDependencies: {c: {from: 'c', version: '1.0.0', path: '/c'}}
      }]);

      assert.deepEqual(Object.keys(dependencyGraph.getProjectRuntimeDependencies(join(sep, 'ws', 'app'))), ['a', 'b']);
      assert.deepEqual(dependencyGraph.getProjectRuntimeDependencies(join(sep, 'ws', 'other')), {});
    });
  });

  describe('getDeclaredDependencies', () => {

    it('returns null when path does not exist', async () => {
      const dependencyGraph = new TestingPnpmDependencyGraph();
      assert.equal(await dependencyGraph.getDeclaredDependencies('/non/existent/path/xyz'), null);
    });

    it('returns dependencies from package.json', async () => {
      await withPackageJson({dependencies: {lodash: '4.17.21'}}, async packageJsonDirectory => {
        const dependencyGraph = new TestingPnpmDependencyGraph();
        assert.deepEqual(await dependencyGraph.getDeclaredDependencies(packageJsonDirectory), {specifiers: {lodash: '4.17.21'}, peerOnly: new Set()});
      });
    });

    it('caches the result per path, including null', async () => {
      await withPackageJson({dependencies: {lodash: '4.17.21'}}, async packageJsonDirectory => {
        const dependencyGraph = new TestingPnpmDependencyGraph();
        let readCount = 0;
        const readDeclaredDependencies = dependencyGraph._readDeclaredDependencies.bind(dependencyGraph);
        dependencyGraph._readDeclaredDependencies = (dir: string) => {
          readCount++;
          return readDeclaredDependencies(dir);
        };

        const dependencies1 = await dependencyGraph.getDeclaredDependencies(packageJsonDirectory);
        await fs.rm(join(packageJsonDirectory, 'package.json'));
        const dependencies2 = await dependencyGraph.getDeclaredDependencies(packageJsonDirectory);
        await dependencyGraph.getDeclaredDependencies('/non/existent/path/xyz');
        await dependencyGraph.getDeclaredDependencies('/non/existent/path/xyz');

        assert.deepEqual(dependencies1, dependencies2);
        assert.equal(readCount, 2);
      });
    });
  });

  describe('_readDeclaredDependencies', () => {

    it('returns null for a non existing path', async () => {
      const dependencyGraph = new TestingPnpmDependencyGraph();
      assert.equal(await dependencyGraph._readDeclaredDependencies('/non/existent/path/xyz'), null);
    });

    it('returns empty object when package.json has no dependency sections', async () => {
      await withPackageJson({name: 'test-pkg', version: '1.0.0'}, async packageJsonDirectory => {
        assert.deepEqual(await new TestingPnpmDependencyGraph()._readDeclaredDependencies(packageJsonDirectory), {specifiers: {}, peerOnly: new Set()});
      });
    });

    it('merges all dependency types', async () => {
      await withPackageJson({
        dependencies: {a: '4.0.0'},
        devDependencies: {b: '3.0.0'},
        optionalDependencies: {c: '2.0.0'},
        peerDependencies: {d: '1.0.0'}
      }, async packageJsonDirectory => {
        assert.deepEqual((await new TestingPnpmDependencyGraph()._readDeclaredDependencies(packageJsonDirectory)).specifiers, {a: '4.0.0', b: '3.0.0', c: '2.0.0', d: '1.0.0'});
      });
    });

    it('uses the priority peerDependencies < devDependencies < dependencies < optionalDependencies', async () => {
      const cases: [object, string][] = [
        [{optionalDependencies: {a: '4.0.0'}, dependencies: {a: '3.0.0'}, devDependencies: {a: '2.0.0'}, peerDependencies: {a: '1.0.0'}}, '4.0.0'],
        [{dependencies: {a: '3.0.0'}, devDependencies: {a: '2.0.0'}, peerDependencies: {a: '1.0.0'}}, '3.0.0'],
        [{devDependencies: {a: '2.0.0'}, peerDependencies: {a: '1.0.0'}}, '2.0.0']
      ];
      for (const [packageJson, expected] of cases) {
        await withPackageJson(packageJson, async packageJsonDirectory => {
          assert.equal((await new TestingPnpmDependencyGraph()._readDeclaredDependencies(packageJsonDirectory)).specifiers.a, expected);
        });
      }
    });

    it('returns the aliases declared in the peerDependenciesMeta only as peer dependencies', async () => {
      // e.g. terser-webpack-plugin declares the optional peer dependency uglify-js in the peerDependenciesMeta only
      await withPackageJson({
        dependencies: {a: '1.0.0'},
        peerDependencies: {webpack: '^5.1.0'},
        peerDependenciesMeta: {a: {optional: true}, 'uglify-js': {optional: true}}
      }, async packageJsonDirectory => {
        assert.deepEqual((await new TestingPnpmDependencyGraph()._readDeclaredDependencies(packageJsonDirectory)).peerOnly, new Set(['webpack', 'uglify-js']));
      });
    });

    it('returns the aliases declared in the peerDependencies only', async () => {
      await withPackageJson({
        dependencies: {a: '1.0.0'},
        optionalDependencies: {b: '1.0.0'},
        devDependencies: {c: '1.0.0'},
        peerDependencies: {a: '^1.0.0', b: '^1.0.0', c: '^1.0.0', d: '^1.0.0'}
      }, async packageJsonDirectory => {
        assert.deepEqual((await new TestingPnpmDependencyGraph()._readDeclaredDependencies(packageJsonDirectory)).peerOnly, new Set(['c', 'd']));
      });
    });
  });
});

async function withPackageJson(packageJson: object, callback: (packageJsonDirectory: string) => Promise<void>): Promise<void> {
  const dir = await fs.mkdtemp(join(tmpdir(), 'pnpm-dependency-graph-test-'));
  try {
    await fs.writeFile(join(dir, 'package.json'), JSON.stringify(packageJson));
    await callback(dir);
  } finally {
    await fs.rm(dir, {recursive: true});
  }
}

class TestingPnpmDependencyGraph extends PnpmDependencyGraph {

  constructor(projects: PnpmListOutput = []) {
    super(projects);
  }

  override _readDeclaredDependencies(packageJsonDirectory: string): Promise<DeclaredDependencies> {
    return super._readDeclaredDependencies(packageJsonDirectory);
  }
}
