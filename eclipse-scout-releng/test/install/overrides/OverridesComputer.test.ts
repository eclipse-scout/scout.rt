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

import {afterEach, before, beforeEach, describe, it} from 'node:test';
import assert from 'node:assert/strict';
import {join, sep} from 'node:path';
import {DependencyMetaData} from '../../../scripts/install/overrides/DependencyMetaData.ts';
import {type DeclaredDependencies, PnpmDependencyGraph} from '../../../scripts/install/overrides/PnpmDependencyGraph.ts';
import {dependencyConvergenceLogLevel, type DependencyDeclaration, OverridesComputer} from '../../../scripts/install/overrides/OverridesComputer.ts';
import {type Dependencies} from '../../../scripts/util/PackageJson.ts';
import {type PnpmWorkspaceYaml} from '../../../scripts/util/PnpmWorkspaceYaml.ts';
import {type PnpmDependency, type PnpmListOutput} from '../../../scripts/util/pnpm.ts';

describe('OverridesComputer', () => {

  describe('_visitProjects', () => {

    const storePath = (name: string, version: string) => join(sep, 'ws', 'node_modules', '.pnpm', `${name}@${version}`, 'node_modules', name);
    const dependency = (name: string, version: string, extra: Partial<PnpmDependency> = {}): PnpmDependency => ({from: name, version, path: storePath(name, version), ...extra});

    const pnpmList: PnpmListOutput = [
      {
        name: 'root',
        version: '0.0.0',
        path: join(sep, 'ws'),
        private: true,
        dependencies: {
          lodash: dependency('lodash', '4.17.21', {
            dependencies: {
              'dep-x': dependency('dep-x', '1.0.0', {
                dependencies: {
                  'dep-y': dependency('dep-y', '1.0.0', {
                    dependencies: {
                      'dep-x': dependency('dep-x', '1.0.0', {deduped: true, dedupedDependenciesCount: 1})
                    }
                  })
                }
              })
            }
          })
        }
      },
      {
        name: 'app-a',
        version: '1.0.0',
        path: join(sep, 'ws', 'app-a'),
        private: false,
        dependencies: {
          'app-b': {
            from: 'app-b',
            version: 'link:../app-b',
            path: join(sep, 'ws', 'app-b'),
            dependencies: {
              chalk: dependency('chalk', '5.0.0', {
                dependencies: {
                  ansi: dependency('ansi', '6.0.0')
                }
              })
            }
          },
          lodash: dependency('lodash', '4.17.21', {deduped: true, dedupedDependenciesCount: 2})
        }
      },
      {
        name: 'app-b',
        version: '1.0.0',
        path: join(sep, 'ws', 'app-b'),
        private: false,
        devDependencies: {
          chalk: dependency('chalk', '5.0.0', {deduped: true, dedupedDependenciesCount: 1})
        },
        optionalDependencies: {
          'fancy-lodash': dependency('lodash', '4.17.20')
        }
      }
    ];

    it('registers the dependencies of workspace projects and resolves deduped dependencies', async () => {
      const overridesComputer = createOverridesComputer({pnpmList});

      await overridesComputer._visitProjects(workspacePackages('app-a', 'app-b'));

      assert.deepEqual(collectDeclarations(overridesComputer), [
        'ansi@6.0.0 <- chalk@5.0.0',
        'chalk@5.0.0 <- app-b@1.0.0',
        'dep-x@1.0.0 <- dep-y@1.0.0',
        'dep-x@1.0.0 <- lodash@4.17.21',
        'dep-y@1.0.0 <- dep-x@1.0.0',
        'fancy-lodash@npm:lodash@4.17.20 <- app-b@1.0.0',
        'lodash@4.17.21 <- app-a@1.0.0'
      ]);
    });

    it('visits only the dependencies and optionalDependencies of a linked project outside the workspace packages', async () => {
      const lib = join(sep, 'ws', 'lib');
      const overridesComputer = createOverridesComputer({
        pnpmList: [
          {
            name: 'app-a', version: '1.0.0', path: join(sep, 'ws', 'app-a'), private: false, dependencies: {
              // pnpm list expands the linked project including its devDependencies
              lib: {from: 'lib', version: 'link:../lib', path: lib, dependencies: {x: dependency('x', '1.0.0'), karma: dependency('karma', '6.4.4')}}
            }
          },
          {
            name: 'lib', version: '1.0.0', path: lib, private: false,
            dependencies: {x: dependency('x', '1.0.0')},
            optionalDependencies: {y: dependency('y', '1.0.0')},
            devDependencies: {karma: dependency('karma', '6.4.4')}
          }
        ]
      });

      await overridesComputer._visitProjects(workspacePackages('app-a'));

      assert.deepEqual(collectDeclarations(overridesComputer), [
        'lib@link:../lib <- app-a@1.0.0',
        'x@1.0.0 <- lib@link:../lib',
        'y@1.0.0 <- lib@link:../lib'
      ]);
    });

    it('collects the versions by package name of the visited projects and packages without links', async () => {
      const overridesComputer = createOverridesComputer({pnpmList});

      await overridesComputer._visitProjects(workspacePackages('app-a', 'app-b'));

      assert.deepEqual(overridesComputer._versionsByName, new Map([
        ['app-a', new Set(['1.0.0'])],
        ['lodash', new Set(['4.17.21', '4.17.20'])],
        ['dep-x', new Set(['1.0.0'])],
        ['dep-y', new Set(['1.0.0'])],
        ['app-b', new Set(['1.0.0'])],
        ['chalk', new Set(['5.0.0'])],
        ['ansi', new Set(['6.0.0'])]
      ]));
    });

    it('ignores projects whose path only ends with the path of a workspace package', async () => {
      const overridesComputer = createOverridesComputer({
        pnpmList: [
          {name: 'app-a', version: '1.0.0', path: join(sep, 'ws', 'app-a'), private: false, dependencies: {x: dependency('x', '1.0.0')}},
          {name: 'my-app-a', version: '1.0.0', path: join(sep, 'ws', 'my-app-a'), private: false, dependencies: {y: dependency('y', '1.0.0')}},
          {name: 'app-a-copy', version: '1.0.0', path: join(sep, 'other', 'ws', 'app-a'), private: false, dependencies: {z: dependency('z', '1.0.0')}}
        ]
      });

      await overridesComputer._visitProjects(workspacePackages('app-a'));

      assert.deepEqual(collectDeclarations(overridesComputer), ['x@1.0.0 <- app-a@1.0.0']);
    });

    it('visits projects without name or version', async () => {
      const nameless = join(sep, 'ws', 'nameless');
      const overridesComputer = createOverridesComputer({
        pnpmWorkspaceYaml: {getPackages: async () => [join(sep, 'ws', 'app-a'), join(sep, 'ws', 'app-b'), nameless]} as unknown as PnpmWorkspaceYaml,
        pnpmList: [
          {name: 'app-a', version: '1.0.0', path: join(sep, 'ws', 'app-a'), private: false, dependencies: {x: dependency('x', '2.0.0')}},
          {name: 'app-b', path: join(sep, 'ws', 'app-b'), private: true, dependencies: {x: dependency('x', '2.0.0')}},
          {path: nameless, private: true, dependencies: {x: dependency('x', '1.0.0')}}
        ]
      });

      // a project without name cannot be selected as parent: no override for its version of x
      assert.deepEqual(await overridesComputer.compute(), {'x@2.0.0': '2.0.0'});
      assert.deepEqual(collectDeclarations(overridesComputer), [`x@1.0.0 <- ${nameless}`, 'x@2.0.0 <- app-a@1.0.0', 'x@2.0.0 <- app-b']);
      assert.deepEqual(overridesComputer._versionsByName.get('app-b'), undefined);
    });

    it('ignores projects that are not part of the workspace packages', async () => {
      const overridesComputer = createOverridesComputer({pnpmList});

      await overridesComputer._visitProjects(workspacePackages('app-b'));

      assert.deepEqual(collectDeclarations(overridesComputer), [
        'ansi@6.0.0 <- chalk@5.0.0',
        'chalk@5.0.0 <- app-b@1.0.0',
        'fancy-lodash@npm:lodash@4.17.20 <- app-b@1.0.0'
      ]);
    });
  });

  describe('peer dependencies', () => {

    it('does not register a peer dependency of an installed package', async () => {
      const parent = createExternalDependencyMetaData('acorn-jsx', '5.3.2');
      const dependency = createExternalDependencyMetaData('acorn', '8.19.0');
      const overridesComputer = createOverridesComputer({peerOnlyByPackageJson: new Map([[parent.path, ['acorn']]])});

      await overridesComputer._registerDependencyDeclaration(parent, dependency);

      assert.deepEqual(overridesComputer._dependencyDeclarationsByVersion, new Map());
      assert.deepEqual(overridesComputer._peerDependencyAliases, new Set(['acorn']));
    });

    it('registers a peer dependency of a project as it is installed as devDependency', async () => {
      const project = createDependencyMetaData('app-a');
      const dependency = createExternalDependencyMetaData('react', '18.2.0');
      const overridesComputer = createOverridesComputer({
        pnpmList: [{name: project.name, version: project.version, path: project.path, private: false}],
        peerOnlyByPackageJson: new Map([[project.path, ['react']]])
      });

      await overridesComputer._registerDependencyDeclaration(project, dependency);

      assert.deepEqual([...overridesComputer._dependencyDeclarationsByVersion.get('react').keys()], ['18.2.0']);
      assert.deepEqual(overridesComputer._peerDependencyAliases, new Set());
    });

    it('writes overrides with parent only for all versions of a peer dependency', async () => {
      const plugin = createExternalDependencyMetaData('plugin', '1.0.0');
      const overridesComputer = createOverridesComputer({peerOnlyByPackageJson: new Map([[plugin.path, ['lodash']]])});

      await overridesComputer._registerDependencyDeclaration(createDependencyMetaData('app-a'), createDependencyMetaData('lodash', '4.17.21'));
      await overridesComputer._registerDependencyDeclaration(createDependencyMetaData('app-b'), createDependencyMetaData('lodash', '4.17.21'));
      await overridesComputer._registerDependencyDeclaration(createDependencyMetaData('app-c'), createDependencyMetaData('lodash', '4.17.20'));
      await overridesComputer._registerDependencyDeclaration(plugin, createDependencyMetaData('lodash', '4.17.20'));

      assert.deepEqual(overridesComputer._buildOverrides(), {
        'app-a>lodash@4.17.21': '4.17.21',
        'app-b>lodash@4.17.21': '4.17.21',
        'app-c>lodash@4.17.20': '4.17.20'
      });
    });

    it('writes overrides with parent only for a peer dependency with a single version', async () => {
      const plugin = createExternalDependencyMetaData('plugin', '1.0.0');
      const overridesComputer = createOverridesComputer({peerOnlyByPackageJson: new Map([[plugin.path, ['lodash']]])});

      await overridesComputer._registerDependencyDeclaration(createDependencyMetaData('app-a'), createDependencyMetaData('lodash', '4.17.21'));
      await overridesComputer._registerDependencyDeclaration(createDependencyMetaData('app-b'), createDependencyMetaData('lodash', '4.17.21'));
      await overridesComputer._registerDependencyDeclaration(plugin, createDependencyMetaData('lodash', '4.17.21'));

      assert.deepEqual(overridesComputer._buildOverrides(), {
        'app-a>lodash@4.17.21': '4.17.21',
        'app-b>lodash@4.17.21': '4.17.21'
      });
    });

    it('does not pin a peer dependency resolved to different versions in different places', async () => {
      // acorn-jsx@5.3.2 is installed twice: with acorn@7.4.1 (used by espree@7.3.1) and with acorn@8.19.0 (used by espree@10.4.0)
      const store = (name: string, version: string, peerSuffix = '') => join(sep, 'ws', 'node_modules', '.pnpm', `${name}@${version}${peerSuffix}`, 'node_modules', name);
      const espree = (version: string, acornVersion: string): PnpmDependency => ({
        from: 'espree', version, path: store('espree', version), dependencies: {
          'acorn': {from: 'acorn', version: acornVersion, path: store('acorn', acornVersion)},
          'acorn-jsx': {
            from: 'acorn-jsx', version: '5.3.2', path: store('acorn-jsx', '5.3.2', `_acorn@${acornVersion}`), dependencies: {
              acorn: {from: 'acorn', version: acornVersion, path: store('acorn', acornVersion)}
            }
          }
        }
      });
      const overridesComputer = createOverridesComputer({
        pnpmWorkspaceYaml: {getPackages: async () => [join(sep, 'ws', 'app-a'), join(sep, 'ws', 'app-b')]} as unknown as PnpmWorkspaceYaml,
        pnpmList: [
          {name: 'app-a', version: '1.0.0', path: join(sep, 'ws', 'app-a'), private: false, dependencies: {espree: espree('7.3.1', '7.4.1')}},
          {name: 'app-b', version: '1.0.0', path: join(sep, 'ws', 'app-b'), private: false, dependencies: {espree: espree('10.4.0', '8.19.0')}}
        ],
        peerOnlyByPackageJson: new Map([
          [store('acorn-jsx', '5.3.2', '_acorn@7.4.1'), ['acorn']],
          [store('acorn-jsx', '5.3.2', '_acorn@8.19.0'), ['acorn']]
        ])
      });

      assert.deepEqual(await overridesComputer.compute(), {
        'acorn-jsx@5.3.2': '5.3.2',
        'app-a>espree@7.3.1': '7.3.1',
        'espree@10.4.0': '10.4.0',
        'espree@10.4.0>acorn@8.19.0': '8.19.0',
        'espree@7.3.1>acorn@7.4.1': '7.4.1'
      });
    });
  });

  describe('dependencies of peer dependencies', () => {

    const store = (name: string, version: string, peerSuffix = '') => join(sep, 'ws', 'node_modules', '.pnpm', `${name.replace('/', '+')}@${version}${peerSuffix}`, 'node_modules', name);
    const pkg = (name: string, version: string, dependencies?: Record<string, PnpmDependency>, peerSuffix = ''): PnpmDependency =>
      ({from: name, version, path: store(name, version, peerSuffix), dependencies});

    it('does not visit the dependencies of an optional peer dependency taken from another project', async () => {
      // @rushstack/worker-pool declares the optional peer dependency '@types/node: *'. app-c does not provide it, so pnpm takes @types/node@26.6.6 of app-b.
      // A nested pnpm-workspace.yaml containing app-c only must not get the dependencies of @types/node@26.6.6 (undici-types@8.9.0): installing it on its own,
      // pnpm would not install them.
      const typesNode = pkg('@types/node', '26.6.6', {'undici-types': pkg('undici-types', '8.9.0')});
      const workerPool = pkg('@rushstack/worker-pool', '0.4.9', {'@types/node': typesNode}, '_@types+node@26.6.6');
      const options = (...packages: string[]) => ({
        pnpmWorkspaceYaml: {getPackages: async () => packages.map(name => join(sep, 'ws', name))} as unknown as PnpmWorkspaceYaml,
        pnpmList: [
          {name: 'app-b', version: '1.0.0', path: join(sep, 'ws', 'app-b'), private: false, dependencies: {'@types/node': typesNode}},
          {name: 'app-c', version: '1.0.0', path: join(sep, 'ws', 'app-c'), private: false, dependencies: {'@rushstack/worker-pool': workerPool}}
        ],
        peerOnlyByPackageJson: new Map([[workerPool.path, ['@types/node']]])
      });

      assert.deepEqual(await createOverridesComputer(options('app-c')).compute(), {'@rushstack/worker-pool@0.4.9': '0.4.9'});
      assert.deepEqual(await createOverridesComputer(options('app-b', 'app-c')).compute(), {
        '@rushstack/worker-pool@0.4.9': '0.4.9',
        'app-b>@types/node@26.6.6': '26.6.6',
        'undici-types@8.9.0': '8.9.0'
      });
    });

    it('does not visit the dependencies of a peer dependency installed by pnpm', async () => {
      // acorn-jsx declares the peer dependency 'acorn'. app-a does not provide it, so pnpm installs it itself. Neither acorn nor its dependency x are pinned.
      const acornJsx = pkg('acorn-jsx', '5.3.2', {acorn: pkg('acorn', '8.19.0', {x: pkg('x', '1.0.0')})}, '_acorn@8.19.0');
      const overridesComputer = createOverridesComputer({
        pnpmWorkspaceYaml: {getPackages: async () => [join(sep, 'ws', 'app-a')]} as unknown as PnpmWorkspaceYaml,
        pnpmList: [{name: 'app-a', version: '1.0.0', path: join(sep, 'ws', 'app-a'), private: false, dependencies: {'acorn-jsx': acornJsx}}],
        peerOnlyByPackageJson: new Map([[acornJsx.path, ['acorn']]])
      });

      assert.deepEqual(await overridesComputer.compute(), {'acorn-jsx@5.3.2': '5.3.2'});
    });

    it('adds the version to the parent of an override if another version of the parent is only installed below a peer dependency installed by pnpm', async () => {
      // debug@4.4.3 is installed for app-a and below acorn, a peer dependency of acorn-jsx installed by pnpm (not visited). debug@2.6.9 is installed for app-b.
      // In the pnpm-workspace.yaml installing all packages, the override of ms for debug@2.6.9 must not apply to debug@4.4.3 below acorn: it needs the version of debug.
      const debug = (version: string, msVersion: string) => pkg('debug', version, {ms: pkg('ms', msVersion)});
      const acornJsx = pkg('acorn-jsx', '5.3.2', {acorn: pkg('acorn', '8.19.0', {debug: debug('4.4.3', '2.1.3')})}, '_acorn@8.19.0');
      const options = (countVersionsOfAllPackages: boolean) => ({
        pnpmWorkspaceYaml: {getPackages: async () => ['app-a', 'app-b', 'app-c'].map(name => join(sep, 'ws', name))} as unknown as PnpmWorkspaceYaml,
        pnpmList: [
          {name: 'app-a', version: '1.0.0', path: join(sep, 'ws', 'app-a'), private: false, dependencies: {'acorn-jsx': acornJsx, 'ms': pkg('ms', '2.1.3')}},
          {name: 'app-b', version: '1.0.0', path: join(sep, 'ws', 'app-b'), private: false, dependencies: {debug: debug('2.6.9', '2.0.0')}},
          {name: 'app-c', version: '1.0.0', path: join(sep, 'ws', 'app-c'), private: false, dependencies: {ms: pkg('ms', '2.1.3')}}
        ],
        peerOnlyByPackageJson: new Map([[acornJsx.path, ['acorn']]]),
        countVersionsOfAllPackages
      });

      assert.equal((await createOverridesComputer(options(false)).compute())['debug>ms@2.0.0'], '2.0.0');
      const overrides = await createOverridesComputer(options(true)).compute();
      assert.equal(overrides['debug@2.6.9>ms@2.0.0'], '2.0.0');
      assert.equal(overrides['debug>ms@2.0.0'], undefined);
    });

    it('visits the dependencies of a peer dependency from the package providing it', async () => {
      const acorn = pkg('acorn', '8.19.0', {x: pkg('x', '1.0.0')});
      const acornJsx = pkg('acorn-jsx', '5.3.2', {acorn}, '_acorn@8.19.0');
      const overridesComputer = createOverridesComputer({
        pnpmWorkspaceYaml: {getPackages: async () => [join(sep, 'ws', 'app-a')]} as unknown as PnpmWorkspaceYaml,
        pnpmList: [{name: 'app-a', version: '1.0.0', path: join(sep, 'ws', 'app-a'), private: false, dependencies: {'acorn-jsx': acornJsx, acorn}}],
        peerOnlyByPackageJson: new Map([[acornJsx.path, ['acorn']]])
      });

      assert.deepEqual(await overridesComputer.compute(), {'acorn-jsx@5.3.2': '5.3.2', 'app-a>acorn@8.19.0': '8.19.0', 'x@1.0.0': '1.0.0'});
    });
  });

  describe('_registerDependencyDeclaration', () => {

    it('registers the first declaration of a dependency', async () => {
      const parent = createDependencyMetaData('app-a');
      const dependency = createDependencyMetaData('lodash');

      const overridesComputer = createOverridesComputer({
        dependenciesByPackageJson: new Map([
          [parent.path, Object.fromEntries([[dependency.alias, dependency.version]])]
        ])
      });

      await overridesComputer._registerDependencyDeclaration(parent, dependency);

      assert.deepEqual(overridesComputer._dependencyDeclarationsByVersion, new Map([
        [
          'lodash',
          new Map([
            [
              '1.0.0',
              new Map([[parent.id, {parent, fix: true}]])
            ]
          ])
        ]
      ]));
    });

    it('registers a new version of an already registered dependency', async () => {
      const parent1 = createDependencyMetaData('app-a');
      const parent2 = createDependencyMetaData('app-b');
      const dependency1 = createDependencyMetaData('lodash', '4.17.20');
      const dependency2 = createDependencyMetaData('lodash', '4.17.21');

      const overridesComputer = createOverridesComputer({
        dependenciesByPackageJson: new Map([
          [parent1.path, Object.fromEntries([[dependency1.alias, dependency1.version]])],
          [parent2.path, Object.fromEntries([[dependency2.alias, dependency2.version]])]
        ])
      });

      await overridesComputer._registerDependencyDeclaration(parent1, dependency1);
      await overridesComputer._registerDependencyDeclaration(parent2, dependency2);

      assert.deepEqual(overridesComputer._dependencyDeclarationsByVersion, new Map([
        [
          'lodash',
          new Map([
            [
              '4.17.20',
              new Map([[parent1.id, {parent: parent1, fix: true}]])
            ],
            [
              '4.17.21',
              new Map([[parent2.id, {parent: parent2, fix: true}]])
            ]
          ])
        ]
      ]));
    });

    it('registers the same dependency version again for a different parent', async () => {
      const parent1 = createDependencyMetaData('app-a');
      const parent2 = createDependencyMetaData('app-b');
      const dependency = createDependencyMetaData('lodash');

      const overridesComputer = createOverridesComputer({
        dependenciesByPackageJson: new Map([
          [parent1.path, Object.fromEntries([[dependency.alias, dependency.version]])],
          [parent2.path, Object.fromEntries([[dependency.alias, dependency.version]])]
        ])
      });

      await overridesComputer._registerDependencyDeclaration(parent1, dependency);
      await overridesComputer._registerDependencyDeclaration(parent2, dependency);

      assert.deepEqual(overridesComputer._dependencyDeclarationsByVersion, new Map([
        [
          'lodash',
          new Map([
            [
              '1.0.0',
              new Map([
                [parent1.id, {parent: parent1, fix: true}],
                [parent2.id, {parent: parent2, fix: true}]
              ])
            ]
          ])
        ]
      ]));
    });

    it('registers parent and fix flag using the override version, not the raw dependency version', async () => {
      const parent1 = createDependencyMetaData('app-a');
      const parent2 = createDependencyMetaData('app-b');
      const dependency = createDependencyMetaData('lodash', '4.17.20');
      // "fancy-lodash": "npm:lodash@4.17.21"
      const dependencyWithAlias = createDependencyMetaData('lodash', '4.17.21', 'fancy-lodash');

      const overridesComputer = createOverridesComputer({
        dependenciesByPackageJson: new Map([
          [parent1.path, Object.fromEntries([[dependency.alias, dependency.version]])],
          [parent2.path, Object.fromEntries([[dependencyWithAlias.alias, `npm:${dependencyWithAlias.name}@${dependencyWithAlias.version}`]])]
        ])
      });

      await overridesComputer._registerDependencyDeclaration(parent1, dependency);
      await overridesComputer._registerDependencyDeclaration(parent2, dependencyWithAlias);

      assert.deepEqual(overridesComputer._dependencyDeclarationsByVersion, new Map([
        [
          'lodash',
          new Map([
            [
              '4.17.20',
              new Map([[parent1.id, {parent: parent1, fix: true}]])
            ]
          ])
        ],
        [
          'fancy-lodash',
          new Map([
            [
              'npm:lodash@4.17.21',
              new Map([[parent2.id, {parent: parent2, fix: true}]])
            ]
          ])
        ]
      ]));
    });

    it('stores fix=true when the dependency is declared with a fix version', async () => {
      const parent = createDependencyMetaData('app-a');
      const dependency = createDependencyMetaData('lodash', '4.17.21');

      const overridesComputer = createOverridesComputer({
        dependenciesByPackageJson: new Map([
          [parent.path, Object.fromEntries([[dependency.alias, dependency.version]])]
        ])
      });

      await overridesComputer._registerDependencyDeclaration(parent, dependency);

      const declaration = overridesComputer._dependencyDeclarationsByVersion.get('lodash').get('4.17.21').get(parent.id);
      assert.equal(declaration.fix, true);
    });

    it('stores fix=false when the dependency is declared with a range version', async () => {
      const parent = createDependencyMetaData('app-a');
      const dependency = createDependencyMetaData('lodash', '4.17.21');

      const overridesComputer = createOverridesComputer({
        dependenciesByPackageJson: new Map([
          [parent.path, Object.fromEntries([[dependency.alias, '^4.0.0']])]
        ])
      });

      await overridesComputer._registerDependencyDeclaration(parent, dependency);

      const declaration = overridesComputer._dependencyDeclarationsByVersion.get('lodash').get('4.17.21').get(parent.id);
      assert.equal(declaration.fix, false);
    });

    it('stores the fix flag per parent of the same dependency version', async () => {
      const parent1 = createDependencyMetaData('app-a');
      const parent2 = createDependencyMetaData('app-b');
      const dependency = createDependencyMetaData('lodash', '4.17.21');

      const overridesComputer = createOverridesComputer({
        dependenciesByPackageJson: new Map([
          [parent1.path, Object.fromEntries([[dependency.alias, dependency.version]])],
          [parent2.path, Object.fromEntries([[dependency.alias, '^4.0.0']])]
        ])
      });

      await overridesComputer._registerDependencyDeclaration(parent1, dependency);
      await overridesComputer._registerDependencyDeclaration(parent2, dependency);

      assert.deepEqual(overridesComputer._dependencyDeclarationsByVersion.get('lodash').get('4.17.21'), new Map([
        [parent1.id, {parent: parent1, fix: true}],
        [parent2.id, {parent: parent2, fix: false}]
      ]));
    });
  });

  describe('_buildOverrides', () => {

    it('non-fix version creates override', () => {
      const overridesComputer = createOverridesComputer();
      const parent = createDependencyMetaData('app-a');
      overridesComputer._dependencyDeclarationsByVersion.set('lodash', new Map([
        ['4.17.21', new Map([[parent.id, {parent, fix: false}]])]
      ]));

      const overrides = overridesComputer._buildOverrides();
      assert.deepEqual(overrides, {'lodash@4.17.21': '4.17.21'});
    });

    it('fix version produces no override', () => {
      const overridesComputer = createOverridesComputer();
      const parent = createDependencyMetaData('app-a');
      overridesComputer._dependencyDeclarationsByVersion.set('lodash', new Map([
        ['4.17.21', new Map([[parent.id, {parent, fix: true}]])]
      ]));

      const overrides = overridesComputer._buildOverrides();
      assert.deepEqual(overrides, {});
    });

    it('most common version gets global override, less common version gets parent-specific override', () => {
      const overridesComputer = createOverridesComputer();
      const parentA = createDependencyMetaData('app-a');
      const parentB = createDependencyMetaData('app-b');
      const parentC = createDependencyMetaData('app-c');

      overridesComputer._dependencyDeclarationsByVersion.set('lodash', new Map([
        ['4.17.21', new Map([
          [parentA.id, {parent: parentA, fix: false}],
          [parentB.id, {parent: parentB, fix: false}]
        ])],
        ['4.17.20', new Map([
          [parentC.id, {parent: parentC, fix: false}]
        ])]
      ]));

      const overrides = overridesComputer._buildOverrides();
      assert.deepEqual(overrides, {
        'lodash@4.17.21': '4.17.21',
        'app-c>lodash@4.17.20': '4.17.20'
      });
    });

    it('fixed less common version gets no override, as the global override does not apply to its version', () => {
      const overridesComputer = createOverridesComputer();
      const parentA = createDependencyMetaData('app-a');
      const parentB = createDependencyMetaData('app-b');
      const parentC = createDependencyMetaData('app-c');

      overridesComputer._dependencyDeclarationsByVersion.set('lodash', new Map([
        ['4.17.21', new Map<string, DependencyDeclaration>([
          [parentA.id, {parent: parentA, fix: false}],
          [parentB.id, {parent: parentB, fix: false}]
        ])],
        ['4.17.20', new Map<string, DependencyDeclaration>([
          [parentC.id, {parent: parentC, fix: true}]
        ])]
      ]));

      const overrides = overridesComputer._buildOverrides();
      assert.deepEqual(overrides, {
        'lodash@4.17.21': '4.17.21'
      });
    });

    it('fixed less common version gets parent-specific override if there is a global override for a npm alias', () => {
      const overridesComputer = createOverridesComputer();
      const parentA = createDependencyMetaData('app-a');
      const parentB = createDependencyMetaData('app-b');
      const parentC = createDependencyMetaData('app-c');

      overridesComputer._dependencyDeclarationsByVersion.set('my-lodash', new Map([
        ['npm:lodash@4.17.21', new Map<string, DependencyDeclaration>([
          [parentA.id, {parent: parentA, fix: false}],
          [parentB.id, {parent: parentB, fix: false}]
        ])],
        ['npm:lodash@4.17.20', new Map<string, DependencyDeclaration>([
          [parentC.id, {parent: parentC, fix: true}]
        ])]
      ]));

      // overrides of npm aliases have no version selector, the global one also applies to app-c
      assert.deepEqual(overridesComputer._buildOverrides(), {
        'my-lodash': 'npm:lodash@4.17.21',
        'app-c>my-lodash': 'npm:lodash@4.17.20'
      });
    });

    it('fixed less common version gets no override if there is no global override', () => {
      const overridesComputer = createOverridesComputer();
      const parentA = createDependencyMetaData('app-a');
      const parentB = createDependencyMetaData('app-b');
      const parentC = createDependencyMetaData('app-c');

      overridesComputer._dependencyDeclarationsByVersion.set('lodash', new Map([
        ['4.17.21-snapshot', new Map<string, DependencyDeclaration>([
          [parentA.id, {parent: parentA, fix: false}],
          [parentB.id, {parent: parentB, fix: false}]
        ])],
        ['4.17.20', new Map<string, DependencyDeclaration>([
          [parentC.id, {parent: parentC, fix: true}]
        ])]
      ]));

      const overrides = overridesComputer._buildOverrides();
      assert.deepEqual(overrides, {});
    });

    it('snapshot version is excluded from overrides', () => {
      const overridesComputer = createOverridesComputer();
      const parent = createDependencyMetaData('app-a');
      overridesComputer._dependencyDeclarationsByVersion.set('my-lib', new Map([
        ['1.0.0-snapshot', new Map([[parent.id, {parent, fix: false}]])]
      ]));

      const overrides = overridesComputer._buildOverrides();
      assert.deepEqual(overrides, {});
    });

    it('link: version is excluded from overrides', () => {
      const overridesComputer = createOverridesComputer();
      const parent = createDependencyMetaData('app-a');
      overridesComputer._dependencyDeclarationsByVersion.set('my-lib', new Map([
        [`link:${join('..', 'my-lib')}`, new Map([[parent.id, {parent, fix: false}]])]
      ]));

      const overrides = overridesComputer._buildOverrides();
      assert.deepEqual(overrides, {});
    });

    it('result is sorted alphabetically by alias', () => {
      const overridesComputer = createOverridesComputer();
      const parent = createDependencyMetaData('app');
      for (const alias of ['zebra', 'alpha', 'mango']) {
        overridesComputer._dependencyDeclarationsByVersion.set(alias, new Map([
          ['1.0.0', new Map([[parent.id, {parent, fix: false}]])]
        ]));
      }

      const overrides = overridesComputer._buildOverrides();
      assert.deepEqual(Object.keys(overrides), ['alpha@1.0.0', 'mango@1.0.0', 'zebra@1.0.0']);
    });

    it('parent key includes version when parent itself appears in multiple versions', () => {
      const overridesComputer = createOverridesComputer();
      overridesComputer._versionsByName.set('app-c', new Set(['1.0.0', '2.0.0']));
      const parentA = createDependencyMetaData('app-a');
      const parentB = createDependencyMetaData('app-b');
      const parentC1 = createDependencyMetaData('app-c', '1.0.0');
      const parentC2 = createDependencyMetaData('app-c', '2.0.0');

      overridesComputer._dependencyDeclarationsByVersion.set('app-c', new Map([
        ['1.0.0', new Map([[parentA.id, {parent: parentA, fix: false}]])],
        ['2.0.0', new Map([[parentB.id, {parent: parentB, fix: false}]])]
      ]));

      // lodash is used by both versions of app-c
      overridesComputer._dependencyDeclarationsByVersion.set('lodash', new Map([
        ['4.17.21', new Map([[parentC1.id, {parent: parentC1, fix: false}]])],
        ['4.17.20', new Map([[parentC2.id, {parent: parentC2, fix: false}]])]
      ]));

      const overrides = overridesComputer._buildOverrides();
      // parent key for the less common version must include the parent version since app-c has multiple versions
      assert.deepEqual(overrides, {
        'app-c@1.0.0': '1.0.0',
        'lodash@4.17.20': '4.17.20',
        'app-b>app-c@2.0.0': '2.0.0',
        'app-c@1.0.0>lodash@4.17.21': '4.17.21'
      });
    });

    it('parent key includes version when parent appears in multiple versions under different aliases', () => {
      // app-c is installed as 'app-c' in version 2.0.0 and as npm alias 'old-c' in version 1.0.0
      const overridesComputer = createOverridesComputer();
      overridesComputer._versionsByName.set('app-c', new Set(['1.0.0', '2.0.0']));
      const parentC1 = createDependencyMetaData('app-c', '1.0.0', 'old-c');
      const parentC2 = createDependencyMetaData('app-c', '2.0.0');
      const parentD = createDependencyMetaData('app-d');

      overridesComputer._dependencyDeclarationsByVersion.set('lodash', new Map([
        ['4.17.21', new Map([[parentC2.id, {parent: parentC2, fix: false}], [parentD.id, {parent: parentD, fix: false}]])],
        ['4.17.20', new Map([[parentC1.id, {parent: parentC1, fix: false}]])]
      ]));

      // pnpm matches the parent by its name: 'app-c>lodash@4.17.20' would also apply to app-c@2.0.0
      assert.deepEqual(overridesComputer._buildOverrides(), {
        'lodash@4.17.21': '4.17.21',
        'app-c@1.0.0>lodash@4.17.20': '4.17.20'
      });
    });

    it('parent key has no version when parent appears in one version only', () => {
      const overridesComputer = createOverridesComputer();
      overridesComputer._versionsByName.set('app-c', new Set(['1.0.0']));
      const parentC = createDependencyMetaData('app-c', '1.0.0');
      const parentD = createDependencyMetaData('app-d');
      const parentE = createDependencyMetaData('app-e');

      overridesComputer._dependencyDeclarationsByVersion.set('lodash', new Map([
        ['4.17.21', new Map([[parentD.id, {parent: parentD, fix: false}], [parentE.id, {parent: parentE, fix: false}]])],
        ['4.17.20', new Map([[parentC.id, {parent: parentC, fix: false}]])]
      ]));

      assert.deepEqual(overridesComputer._buildOverrides(), {
        'lodash@4.17.21': '4.17.21',
        'app-c>lodash@4.17.20': '4.17.20'
      });
    });
  });

  describe('_getDependencyVersionsSorted', () => {

    it('sorts by declaration count descending', () => {
      const overridesComputer = createOverridesComputer();
      const versions = new Map([
        ['1.0.0', createDependencyDeclarations(1)],
        ['3.0.0', createDependencyDeclarations(3)],
        ['2.0.0', createDependencyDeclarations(2)]
      ]);
      const sorted = overridesComputer._getDependencyVersionsSorted(versions);
      assert.deepEqual(sorted.map(([version, declarations]) => version), ['3.0.0', '2.0.0', '1.0.0']);
    });

    it('stable sort by version string when declaration count is equal', () => {
      const overridesComputer = createOverridesComputer();
      const versions = new Map([
        ['2.0.0', createDependencyDeclarations(2)],
        ['1.0.0', createDependencyDeclarations(2)]
      ]);
      const sorted = overridesComputer._getDependencyVersionsSorted(versions);
      assert.deepEqual(sorted.map(([version, declarations]) => version), ['1.0.0', '2.0.0']);
    });

    function createDependencyDeclarations(count: number): Map<string, DependencyDeclaration> {
      return new Map(
        Array.from({length: count}, (_, i) => {
          const parent = createDependencyMetaData(`parent-${i}`);
          return [parent.id, {parent, fix: false}];
        })
      );
    }
  });

  describe('_isOverridesVersionAllowed', () => {

    it('normal semver is allowed', () => {
      const overridesComputer = createOverridesComputer();
      assert.ok(overridesComputer._isOverrideVersionAllowed('1.2.3'));
    });

    it('pre-release non-snapshot is allowed', () => {
      const overridesComputer = createOverridesComputer();
      assert.ok(overridesComputer._isOverrideVersionAllowed('1.2.3-rc.1'));
      assert.ok(overridesComputer._isOverrideVersionAllowed('1.2.3-beta'));
    });

    it('snapshot version is not allowed', () => {
      const overridesComputer = createOverridesComputer();
      assert.ok(!overridesComputer._isOverrideVersionAllowed('1.2.3-snapshot'));
      assert.ok(!overridesComputer._isOverrideVersionAllowed('1.2.3-SNAPSHOT'));
    });

    it('timestamped snapshot is not allowed', () => {
      const overridesComputer = createOverridesComputer();
      assert.ok(!overridesComputer._isOverrideVersionAllowed('1.2.3-snapshot.20240101120000'));
      assert.ok(!overridesComputer._isOverrideVersionAllowed('26.2.0-snapshot.20250101000000'));
    });

    it('link: version is not allowed', () => {
      const overridesComputer = createOverridesComputer();
      assert.ok(!overridesComputer._isOverrideVersionAllowed(`link:${join('..', 'other-package')}`));
      assert.ok(!overridesComputer._isOverrideVersionAllowed(`link:${join('.', 'local')}`));
    });
  });

  describe('logNonUniqueDependencyVersions', () => {

    const warnings = [];
    let consoleWarnOrig;
    let overridesComputer: TestingOverridesComputer;

    before(async () => {
      overridesComputer = createOverridesComputer();

      // workspace packages, 'app-c' exists in multiple versions
      const workspaceA = createDependencyMetaData('app-a', '1.0.0');
      const workspaceB = createDependencyMetaData('app-b', '1.0.0');
      const workspaceC1 = createDependencyMetaData('app-c', '1.0.0');
      const workspaceC2 = createDependencyMetaData('app-c', '2.0.0');

      await overridesComputer._registerDependencyDeclaration(workspaceA, workspaceB);
      await overridesComputer._registerDependencyDeclaration(workspaceA, workspaceC1);
      await overridesComputer._registerDependencyDeclaration(workspaceB, workspaceC2);

      // externals with unique versions:
      // - 'external-a' is only used by workspace packages
      // - 'external-b' is used by workspace packages and external packages
      // - 'external-c' is only used by external packages
      const externalA = createExternalDependencyMetaData('external-a', '1.0.0');
      const externalB = createExternalDependencyMetaData('external-b', '1.0.0');
      const externalC = createExternalDependencyMetaData('external-c', '1.0.0');

      await overridesComputer._registerDependencyDeclaration(workspaceA, externalA);
      await overridesComputer._registerDependencyDeclaration(workspaceB, externalA);

      await overridesComputer._registerDependencyDeclaration(workspaceA, externalB);
      await overridesComputer._registerDependencyDeclaration(workspaceB, externalB);
      await overridesComputer._registerDependencyDeclaration(externalA, externalB);

      await overridesComputer._registerDependencyDeclaration(externalB, externalC);
      await overridesComputer._registerDependencyDeclaration(externalC, externalC);

      // externals with multiple versions:
      // - 'external-d' is only used by workspace packages
      // - 'external-e' is used by workspace packages and exactly 1 external package
      // - 'external-f' is used by workspace packages and multiple external packages
      // - 'external-g' is only used by external packages
      const externalD1 = createExternalDependencyMetaData('external-d', '1.0.0');
      const externalD2 = createExternalDependencyMetaData('external-d', '2.0.0');
      const externalE1 = createExternalDependencyMetaData('external-e', '1.0.0');
      const externalE2 = createExternalDependencyMetaData('external-e', '2.0.0');
      const externalF1 = createExternalDependencyMetaData('external-f', '1.0.0');
      const externalF2 = createExternalDependencyMetaData('external-f', '2.0.0');
      const externalG1 = createExternalDependencyMetaData('external-g', '1.0.0');
      const externalG2 = createExternalDependencyMetaData('external-g', '2.0.0');

      await overridesComputer._registerDependencyDeclaration(workspaceC1, externalD1);
      await overridesComputer._registerDependencyDeclaration(workspaceC2, externalD2);

      await overridesComputer._registerDependencyDeclaration(workspaceC1, externalE1);
      await overridesComputer._registerDependencyDeclaration(workspaceC2, externalE2);
      await overridesComputer._registerDependencyDeclaration(externalD1, externalE1);
      await overridesComputer._registerDependencyDeclaration(externalD2, externalE1);

      await overridesComputer._registerDependencyDeclaration(workspaceC1, externalF1);
      await overridesComputer._registerDependencyDeclaration(workspaceC2, externalF2);
      await overridesComputer._registerDependencyDeclaration(externalD1, externalF1);
      await overridesComputer._registerDependencyDeclaration(externalD2, externalF2);
      await overridesComputer._registerDependencyDeclaration(externalE1, externalF1);
      await overridesComputer._registerDependencyDeclaration(externalE2, externalF2);

      await overridesComputer._registerDependencyDeclaration(externalD1, externalG1);
      await overridesComputer._registerDependencyDeclaration(externalD2, externalG2);
      await overridesComputer._registerDependencyDeclaration(externalE1, externalG1);
      await overridesComputer._registerDependencyDeclaration(externalE2, externalG2);
    });

    beforeEach(() => {
      warnings.splice(0, warnings.length);
      consoleWarnOrig = console.warn;
      console.warn = (warning: string) => warnings.push(warning);
    });

    afterEach(() => {
      console.warn = consoleWarnOrig;
    });

    it('logs nothing when NONE is passed', async () => {
      overridesComputer.logNonUniqueDependencyVersions(dependencyConvergenceLogLevel.NONE);
      assert.deepEqual(warnings, []);
    });

    it('logs only non-unique dependencies from workspace packages when OWN is passed', () => {
      overridesComputer.logNonUniqueDependencyVersions();
      assert.deepEqual(warnings, [
        'Dependency \'app-c\' does not converge:\n' +
        '1.0.0: [\n  app-a@1.0.0\n]\n' +
        '2.0.0: [\n  app-b@1.0.0\n]\n',
        'Dependency \'external-d\' does not converge:\n' +
        '1.0.0: [\n  app-c@1.0.0\n]\n' +
        '2.0.0: [\n  app-c@2.0.0\n]\n'
      ]);
    });

    it('logs only non-unique dependencies from workspace packages when nothing is passed', () => {
      overridesComputer.logNonUniqueDependencyVersions();
      assert.deepEqual(warnings, [
        'Dependency \'app-c\' does not converge:\n' +
        '1.0.0: [\n  app-a@1.0.0\n]\n' +
        '2.0.0: [\n  app-b@1.0.0\n]\n',
        'Dependency \'external-d\' does not converge:\n' +
        '1.0.0: [\n  app-c@1.0.0\n]\n' +
        '2.0.0: [\n  app-c@2.0.0\n]\n'
      ]);
    });

    it('logs non-unique dependencies with up to one version coming from a external package when SINGLE_EXTERNAL is passed', () => {
      overridesComputer.logNonUniqueDependencyVersions(dependencyConvergenceLogLevel.SINGLE_EXTERNAL);
      assert.deepEqual(warnings, [
        'Dependency \'app-c\' does not converge:\n' +
        '1.0.0: [\n  app-a@1.0.0\n]\n' +
        '2.0.0: [\n  app-b@1.0.0\n]\n',
        'Dependency \'external-d\' does not converge:\n' +
        '1.0.0: [\n  app-c@1.0.0\n]\n' +
        '2.0.0: [\n  app-c@2.0.0\n]\n',
        'Dependency \'external-e\' does not converge:\n' +
        '1.0.0: [\n  app-c@1.0.0,\n  external-d@1.0.0,\n  external-d@2.0.0\n]\n' +
        '2.0.0: [\n  app-c@2.0.0\n]\n'
      ]);
    });

    it('logs all non-unique dependencies when ALL is passed', () => {
      overridesComputer.logNonUniqueDependencyVersions(dependencyConvergenceLogLevel.ALL);
      assert.deepEqual(warnings, [
        'Dependency \'app-c\' does not converge:\n' +
        '1.0.0: [\n  app-a@1.0.0\n]\n' +
        '2.0.0: [\n  app-b@1.0.0\n]\n',
        'Dependency \'external-d\' does not converge:\n' +
        '1.0.0: [\n  app-c@1.0.0\n]\n' +
        '2.0.0: [\n  app-c@2.0.0\n]\n',
        'Dependency \'external-e\' does not converge:\n' +
        '1.0.0: [\n  app-c@1.0.0,\n  external-d@1.0.0,\n  external-d@2.0.0\n]\n' +
        '2.0.0: [\n  app-c@2.0.0\n]\n',
        'Dependency \'external-f\' does not converge:\n' +
        '1.0.0: [\n  app-c@1.0.0,\n  external-d@1.0.0,\n  external-e@1.0.0\n]\n' +
        '2.0.0: [\n  app-c@2.0.0,\n  external-d@2.0.0,\n  external-e@2.0.0\n]\n',
        'Dependency \'external-g\' does not converge:\n' +
        '1.0.0: [\n  external-d@1.0.0,\n  external-e@1.0.0\n]\n' +
        '2.0.0: [\n  external-d@2.0.0,\n  external-e@2.0.0\n]\n'
      ]);
    });
  });

  describe('_computeOverrideVersion', () => {

    it('returns the dependency version if the dependency is not declared as npm alias', () => {
      const overridesComputer = createOverridesComputer();
      assert.equal(overridesComputer._computeOverrideVersion(createDependencyMetaData('lodash', '4.17.21')), '4.17.21');
    });

    it('returns a npm alias with the dependency version if the dependency is declared as npm alias', () => {
      const overridesComputer = createOverridesComputer();
      assert.equal(overridesComputer._computeOverrideVersion(createDependencyMetaData('lodash', '4.17.21', 'fancy-lodash')), 'npm:lodash@4.17.21');
      assert.equal(overridesComputer._computeOverrideVersion(createDependencyMetaData('@eclipse-scout/core', '26.2.0', 'scout-core')), 'npm:@eclipse-scout/core@26.2.0');
    });

    it('returns the link version if the dependency is a link', () => {
      const overridesComputer = createOverridesComputer();
      assert.equal(overridesComputer._computeOverrideVersion(createDependencyMetaData('pkg-a', 'link:../pkg-a', 'pkg-b')), 'link:../pkg-a');
    });
  });

  describe('_isFixVersion', () => {

    it('returns true for fix versions', () => {
      const overridesComputer = createOverridesComputer();
      for (const specifier of ['4.17.21', '=4.17.21', '26.2.0-snapshot', 'npm:lodash@4.17.21', 'npm:@eclipse-scout/core@26.2.0']) {
        assert.equal(overridesComputer._isFixVersion(specifier), true, specifier);
      }
    });

    it('returns false for ranges and missing versions', () => {
      const overridesComputer = createOverridesComputer();
      for (const specifier of ['^4.17.21', '~4.17.21', '>=1.0.0 <2.0.0', '*', 'latest', 'link:../pkg-a', 'npm:lodash@^4.0.0', 'npm:lodash', 'npm:@eclipse-scout/core', '', null, undefined]) {
        assert.equal(overridesComputer._isFixVersion(specifier), false, String(specifier));
      }
    });
  });

});

function createOverridesComputer(options: {
  pnpmWorkspaceYaml?: PnpmWorkspaceYaml; pnpmList?: PnpmListOutput; dependenciesByPackageJson?: Map<string, Dependencies>; peerOnlyByPackageJson?: Map<string, string[]>;
  countVersionsOfAllPackages?: boolean;
} = {}): TestingOverridesComputer {
  let {pnpmWorkspaceYaml} = options;
  if (!pnpmWorkspaceYaml) {
    pnpmWorkspaceYaml = {getPackages: async () => []} as unknown as PnpmWorkspaceYaml;
  }
  return new TestingOverridesComputer(pnpmWorkspaceYaml, new TestingPnpmDependencyGraph(options.pnpmList ?? [], options.dependenciesByPackageJson, options.peerOnlyByPackageJson),
    options.countVersionsOfAllPackages);
}

/**
 * @returns the absolute paths of the given projects in the directory '/ws'.
 */
function workspacePackages(...projects: string[]): Set<string> {
  return new Set(projects.map(project => join(sep, 'ws', project)));
}

/**
 * @returns all registered declarations as sorted strings `<alias>@<version> <- <parent id>`.
 */
function collectDeclarations(overridesComputer: TestingOverridesComputer): string[] {
  return [...overridesComputer._dependencyDeclarationsByVersion]
    .flatMap(([alias, versions]) => [...versions]
      .flatMap(([version, declarations]) => [...declarations.keys()].map(parentId => `${alias}@${version} <- ${parentId}`)))
    .sort();
}

function createDependencyMetaData(name: string, version = '1.0.0', alias?: string): DependencyMetaData {
  if (!alias) {
    alias = name;
  }
  return new DependencyMetaData({name, alias, version, path: join(sep, 'workspace', `${name}-${version}`)});
}

function createExternalDependencyMetaData(name: string, version = '1.0.0', alias?: string): DependencyMetaData {
  if (!alias) {
    alias = name;
  }
  return new DependencyMetaData({name, alias, version, path: join(sep, 'workspace', 'node_modules', '.pnpm', `${name}@${version}`, 'node_modules', name)});
}

class TestingOverridesComputer extends OverridesComputer {

  declare _dependencyDeclarationsByVersion: Map<string /* dependency alias */, Map<string /* dependency version */, Map<string /* parent DependencyMetaData.id() */, DependencyDeclaration>>>;
  declare _peerDependencyAliases: Set<string>;
  declare _versionsByName: Map<string, Set<string>>;
  override _computeOverrideVersion(dependency: DependencyMetaData): string {
    return super._computeOverrideVersion(dependency);
  }

  override _isFixVersion(specifier: string): boolean {
    return super._isFixVersion(specifier);
  }

  override _visitProjects(workspacePackages: Set<string>): Promise<void> {
    return super._visitProjects(workspacePackages);
  }

  override _registerDependencyDeclaration(parent: DependencyMetaData, dependency: DependencyMetaData): Promise<boolean> {
    return super._registerDependencyDeclaration(parent, dependency);
  }

  override _buildOverrides(): Record<string, string> {
    return super._buildOverrides();
  }

  override _getDependencyVersionsSorted(versions: Map<string, Map<string, DependencyDeclaration>>): [string, Map<string, DependencyDeclaration>][] {
    return super._getDependencyVersionsSorted(versions);
  }

  override _isOverrideVersionAllowed(version: string): boolean {
    return super._isOverrideVersionAllowed(version);
  }
}

class TestingPnpmDependencyGraph extends PnpmDependencyGraph {

  /**
   * Declared dependencies by package directory returned instead of reading the `package.json` files.
   */
  protected _dependenciesByPackageJson: Map<string, Dependencies>;
  /**
   * Aliases declared in the peerDependencies only by package directory.
   */
  protected _peerOnlyByPackageJson: Map<string, string[]>;

  constructor(projects: PnpmListOutput, dependenciesByPackageJson: Map<string, Dependencies>, peerOnlyByPackageJson: Map<string, string[]>) {
    super(projects);
    this._dependenciesByPackageJson = dependenciesByPackageJson;
    this._peerOnlyByPackageJson = peerOnlyByPackageJson;
  }

  protected override async _readDeclaredDependencies(packageJsonDirectory: string): Promise<DeclaredDependencies> {
    const specifiers = this._dependenciesByPackageJson?.get(packageJsonDirectory);
    const peerOnly = this._peerOnlyByPackageJson?.get(packageJsonDirectory);
    if (!specifiers && !peerOnly) {
      return null;
    }
    return {specifiers: specifiers ?? {}, peerOnly: new Set(peerOnly)};
  }
}
