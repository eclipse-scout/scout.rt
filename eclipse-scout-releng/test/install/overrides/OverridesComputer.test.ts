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
import {DependencyMetaData} from '../../../scripts/install/dependency/visitor/DependencyMetaData.ts';
import {type DependencyConvergenceLogLevel, dependencyConvergenceLogLevel, type DependencyDeclaration, OverridesComputer} from '../../../scripts/install/overrides/OverridesComputer.ts';
import {VersionMetaDataComputer} from '../../../scripts/install/overrides/VersionMetaDataComputer.ts';
import {type PnpmWorkspaceYaml} from '../../../scripts/util/PnpmWorkspaceYaml.ts';

describe('OverridesComputer', () => {

  describe('_computeWorkspacePackages', () => {

    it('returns empty array when workspace has no packages', () => {
      const overridesComputer = new TestingOverridesComputer('workspace', {getPackages: () => []} as unknown as PnpmWorkspaceYaml);
      assert.deepEqual(overridesComputer._computeWorkspacePackages(), []);
    });

    it('returns paths relative to lockfileDir', () => {
      const computer = new TestingOverridesComputer('workspace', {getPackages: () => [join('workspace', 'pkg-a'), join('workspace', 'sub', 'pkg-b')]} as unknown as PnpmWorkspaceYaml);
      assert.deepEqual(computer._computeWorkspacePackages(), ['pkg-a', join('sub', 'pkg-b')]);
    });
  });

  describe('_collectDependencies', () => {

    it('returns true and registers dependency declaration when the dependency is new', async () => {
      const overridesComputer = createOverridesComputer();
      const parent = createDependencyMetaData('app-a');
      const dependency = createDependencyMetaData('lodash', '4.17.21');

      const result = await overridesComputer._collectDependencies([], parent, dependency);

      assert.equal(result, true);
      assert.deepEqual(overridesComputer._dependencyDeclarationsByVersion, new Map([
        [
          'lodash',
          new Map([
            [
              '4.17.21',
              new Map([[parent.id, {parent, fix: false}]])
            ]
          ])
        ]
      ]));
    });

    it('returns true and registers dependency declaration when the same dependency is collected again with a different version', async () => {
      const overridesComputer = createOverridesComputer();
      const parent1 = createDependencyMetaData('app-a');
      const parent2 = createDependencyMetaData('app-b');
      const dependency1 = createDependencyMetaData('lodash', '4.17.20');
      const dependency2 = createDependencyMetaData('lodash', '4.17.21');

      await overridesComputer._collectDependencies([], parent1, dependency1);
      const result = await overridesComputer._collectDependencies([], parent2, dependency2);

      assert.equal(result, true);
      assert.deepEqual(overridesComputer._dependencyDeclarationsByVersion, new Map([
        [
          'lodash',
          new Map([
            [
              '4.17.20',
              new Map([[parent1.id, {parent: parent1, fix: false}]])
            ],
            [
              '4.17.21',
              new Map([[parent2.id, {parent: parent2, fix: false}]])
            ]
          ])
        ]
      ]));
    });

    it('returns false and registers dependency declaration when the same dependency version is collected again', async () => {
      const overridesComputer = createOverridesComputer();
      const parent1 = createDependencyMetaData('app-a');
      const parent2 = createDependencyMetaData('app-b');
      const dependency = createDependencyMetaData('lodash', '4.17.21');

      await overridesComputer._collectDependencies([], parent1, dependency);
      const result = await overridesComputer._collectDependencies([], parent2, dependency);

      assert.equal(result, false);
      assert.deepEqual(overridesComputer._dependencyDeclarationsByVersion, new Map([
        [
          'lodash',
          new Map([
            [
              '4.17.21',
              new Map([
                [parent1.id, {parent: parent1, fix: false}],
                [parent2.id, {parent: parent2, fix: false}]
              ])
            ]
          ])
        ]
      ]));
    });

    it('returns false and does not register a link: dependency declaration that is part of the own pnpm workspace', async () => {
      const overridesComputer = createOverridesComputer();
      const parent = createDependencyMetaData('app-a');
      const dependency = createDependencyMetaData('pkg-a', `link:${join('..', 'pkg-a')}`);

      const result = await overridesComputer._collectDependencies(['pkg-a'], parent, dependency);

      assert.equal(result, false);
      assert.deepEqual(overridesComputer._dependencyDeclarationsByVersion, new Map());
    });

    it('returns true and registers a link: dependency declaration that is outside the own pnpm workspace', async () => {
      const overridesComputer = createOverridesComputer();
      const parent = createDependencyMetaData('app-a');
      const dependency = createDependencyMetaData('pkg-a', `link:${join('..', 'pkg-a')}`);

      const result = await overridesComputer._collectDependencies([], parent, dependency);

      assert.equal(result, true);
      assert.deepEqual(overridesComputer._dependencyDeclarationsByVersion, new Map([
        [
          'pkg-a',
          new Map([
            [
              `link:${join('..', 'pkg-a')}`,
              new Map([[parent.id, {parent, fix: false}]])
            ]
          ])
        ]
      ]));
    });
  });

  describe('_registerDependencyDeclaration', () => {

    it('returns true for first registration of a dependency', async () => {
      const parent = createDependencyMetaData('app-a');
      const dependency = createDependencyMetaData('lodash');

      const overridesComputer = createOverridesComputer({
        dependenciesByPackageJson: new Map([
          [parent.path, Object.fromEntries([[dependency.alias, dependency.version]])]
        ])
      });

      const isNewDependency = await overridesComputer._registerDependencyDeclaration(parent, dependency);

      assert.equal(isNewDependency, true);
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

    it('returns true when a new version of an already registered dependency is encountered', async () => {
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
      const isNewDependency = await overridesComputer._registerDependencyDeclaration(parent2, dependency2);

      assert.equal(isNewDependency, true);
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

    it('returns false when the same dependency version is registered again by a different parent', async () => {
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
      const isNewDependency = await overridesComputer._registerDependencyDeclaration(parent2, dependency);

      assert.equal(isNewDependency, false);
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

    it('registers parent and fix flag using the resolved version from the VersionMetaDataComputer, not the raw dependency version', async () => {
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

    it('stores fix=true when VersionMetaDataComputer resolves a fix version', async () => {
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

    it('stores fix=false when VersionMetaDataComputer resolves a range version', async () => {
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

    it('registers all parents for the same dependency version', async () => {
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
      assert.deepEqual(overrides, {lodash: '4.17.21'});
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
        lodash: '4.17.21',
        'app-c>lodash': '4.17.20'
      });
    });

    it('fixed less common version gets parent-specific override if there is a global override', () => {
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
        lodash: '4.17.21',
        'app-c>lodash': '4.17.20'
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
      assert.deepEqual(Object.keys(overrides), ['alpha', 'mango', 'zebra']);
    });

    it('parent key includes version when parent itself appears in multiple versions', () => {
      const overridesComputer = createOverridesComputer();
      const parentA = createDependencyMetaData('app-a');
      const parentB = createDependencyMetaData('app-b');
      const parentC1 = createDependencyMetaData('app-c', '1.0.0');
      const parentC2 = createDependencyMetaData('app-c', '2.0.0');

      // app-c exists in two versions as a dependency
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
        'app-c': '1.0.0',
        lodash: '4.17.20',
        'app-b>app-c': '2.0.0',
        'app-c@1.0.0>lodash': '4.17.21'
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

  describe('_logNonUniqueDependencyVersions', () => {

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

      await overridesComputer._collectDependencies([], workspaceA, workspaceB);
      await overridesComputer._collectDependencies([], workspaceA, workspaceC1);
      await overridesComputer._collectDependencies([], workspaceB, workspaceC2);

      // externals with unique versions:
      // - 'external-a' is only used by workspace packages
      // - 'external-b' is used by workspace packages and external packages
      // - 'external-c' is only used by external packages
      const externalA = createExternalDependencyMetaData('external-a', '1.0.0');
      const externalB = createExternalDependencyMetaData('external-b', '1.0.0');
      const externalC = createExternalDependencyMetaData('external-c', '1.0.0');

      await overridesComputer._collectDependencies([], workspaceA, externalA);
      await overridesComputer._collectDependencies([], workspaceB, externalA);

      await overridesComputer._collectDependencies([], workspaceA, externalB);
      await overridesComputer._collectDependencies([], workspaceB, externalB);
      await overridesComputer._collectDependencies([], externalA, externalB);

      await overridesComputer._collectDependencies([], externalB, externalC);
      await overridesComputer._collectDependencies([], externalC, externalC);

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

      await overridesComputer._collectDependencies([], workspaceC1, externalD1);
      await overridesComputer._collectDependencies([], workspaceC2, externalD2);

      await overridesComputer._collectDependencies([], workspaceC1, externalE1);
      await overridesComputer._collectDependencies([], workspaceC2, externalE2);
      await overridesComputer._collectDependencies([], externalD1, externalE1);
      await overridesComputer._collectDependencies([], externalD2, externalE1);

      await overridesComputer._collectDependencies([], workspaceC1, externalF1);
      await overridesComputer._collectDependencies([], workspaceC2, externalF2);
      await overridesComputer._collectDependencies([], externalD1, externalF1);
      await overridesComputer._collectDependencies([], externalD2, externalF2);
      await overridesComputer._collectDependencies([], externalE1, externalF1);
      await overridesComputer._collectDependencies([], externalE2, externalF2);

      await overridesComputer._collectDependencies([], externalD1, externalG1);
      await overridesComputer._collectDependencies([], externalD2, externalG2);
      await overridesComputer._collectDependencies([], externalE1, externalG1);
      await overridesComputer._collectDependencies([], externalE2, externalG2);
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
      overridesComputer._logNonUniqueDependencyVersions(dependencyConvergenceLogLevel.NONE);
      assert.deepEqual(warnings, []);
    });

    it('logs only non-unique dependencies from workspace packages when OWN is passed', () => {
      overridesComputer._logNonUniqueDependencyVersions();
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
      overridesComputer._logNonUniqueDependencyVersions();
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
      overridesComputer._logNonUniqueDependencyVersions(dependencyConvergenceLogLevel.SINGLE_EXTERNAL);
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
      overridesComputer._logNonUniqueDependencyVersions(dependencyConvergenceLogLevel.ALL);
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
});

function createOverridesComputer(options: { lockfileDir?: string; pnpmWorkspaceYaml?: PnpmWorkspaceYaml; dependenciesByPackageJson?: Map<string, Record<string, string>> } = {}): TestingOverridesComputer {
  let {lockfileDir, pnpmWorkspaceYaml} = options;
  if (!lockfileDir) {
    lockfileDir = 'workspace';
  }
  if (!pnpmWorkspaceYaml) {
    pnpmWorkspaceYaml = {getPackages: () => []} as unknown as PnpmWorkspaceYaml;
  }
  const overridesComputer = new TestingOverridesComputer(lockfileDir, pnpmWorkspaceYaml);
  overridesComputer._versionMetaDataComputer = new TestingVersionMetaDataComputer(options.dependenciesByPackageJson);
  return overridesComputer;
}

function createDependencyMetaData(name: string, version = '1.0.0', alias?: string): DependencyMetaData {
  if (!alias) {
    alias = name;
  }
  return new DependencyMetaData(null, {name, alias, version, path: join(sep, 'workspace', `${name}-${version}`)});
}

function createExternalDependencyMetaData(name: string, version = '1.0.0', alias?: string): DependencyMetaData {
  if (!alias) {
    alias = name;
  }
  return new DependencyMetaData(null, {name, alias, version, path: join(sep, 'workspace', 'node_modules', '.pnpm', `${name}@${version}`, 'node_modules', name)});
}

class TestingOverridesComputer extends OverridesComputer {

  declare _dependencyDeclarationsByVersion: Map<string /* dependency alias */, Map<string /* dependency version */, Map<string /* parent DependencyMetaData.id() */, DependencyDeclaration>>>;
  declare _versionMetaDataComputer: VersionMetaDataComputer;

  override _computeWorkspacePackages(): string[] {
    return super._computeWorkspacePackages();
  }

  override _collectDependencies(workspacePackages: string[], parent: DependencyMetaData, dependency: DependencyMetaData): Promise<boolean> {
    return super._collectDependencies(workspacePackages, parent, dependency);
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

  override _logNonUniqueDependencyVersions(convergenceLogLevel?: DependencyConvergenceLogLevel): void {
    return super._logNonUniqueDependencyVersions(convergenceLogLevel);
  }
}

class TestingVersionMetaDataComputer extends VersionMetaDataComputer {

  protected _dependenciesByPackageJson: Map<string, Record<string, string>>;

  constructor(dependenciesByPackageJson: Map<string, Record<string, string>>) {
    super();

    this._dependenciesByPackageJson = dependenciesByPackageJson;
  }

  protected override async _readDependencies(pkgJsonPath: string): Promise<Record<string, string>> {
    return this._dependenciesByPackageJson?.get(pkgJsonPath);
  }
}
