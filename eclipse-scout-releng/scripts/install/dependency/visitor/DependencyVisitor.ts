/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import path from 'node:path';
import realpathMissing from 'realpath-missing';
import {readModulesManifest} from '@pnpm/modules-yaml';
import {normalizeRegistries} from '@pnpm/normalize-registries';
import {getLockfileImporterId, type LockfileObject, readCurrentLockfile, readWantedLockfile} from '@pnpm/lockfile.fs';
import {type DepTypes, detectDepTypes} from '@pnpm/lockfile.detect-dep-types';
import {WORKSPACE_MANIFEST_FILENAME} from '@pnpm/constants';
import {DEPENDENCIES_FIELDS, type DependenciesField, type Registries} from '@pnpm/types';

import {type TreeNodeId} from '../tree/TreeNodeId.ts';
import {getPkgInfo} from '../tree/getPkgInfo.ts';
import {Keypath} from '../tree/Keypath.ts';
import {getTreeNodeChildId} from '../tree/getTreeNodeChildId.ts';
import {DependenciesCache} from '../tree/DependenciesCache.ts';
import {type GetTreeOpts, visitDependencyTree} from './visitDependencyTree.ts';
import {DependencyMetaData} from './DependencyMetaData.ts';

// Inspired by https://github.com/pnpm/pnpm/blob/v10.26.1/reviewing/dependencies-hierarchy/src/buildDependenciesHierarchy.ts

// The MIT License (MIT)
//
// Copyright (c) 2015-2016 Rico Sta. Cruz and other contributors
// Copyright (c) 2016-2026 Zoltan Kochan and other contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

export async function visitDependenciesForPackages(lockfileDir: string, packages: string[], visitor: DependencyVisitor): Promise<void> {
  const modulesDir = await realpathMissing(path.join(lockfileDir, 'node_modules'));
  const modules = await readModulesManifest(modulesDir);
  const registries = normalizeRegistries({...modules?.registries});
  const internalPnpmDir = path.join(modulesDir, '.pnpm');
  const currentLockfile = await readCurrentLockfile(internalPnpmDir, {ignoreIncompatible: false});
  const wantedLockfile = await readWantedLockfile(lockfileDir, {ignoreIncompatible: false});
  const depTypes = detectDepTypes(currentLockfile);
  const dependenciesCache = new DependenciesCache();
  const opts: PackageVisitOptions = {
    depth: Infinity,
    include: {dependencies: true, devDependencies: true, optionalDependencies: true},
    registries,
    onlyProjects: false,
    skipped: new Set(modules?.skipped ?? []),
    lockfileDir: lockfileDir,
    checkWantedLockfileOnly: false,
    virtualStoreDir: modules?.virtualStoreDir,
    virtualStoreDirMaxLength: modules?.virtualStoreDirMaxLength ?? (process.platform === 'win32' ? 60 : 120)
  };
  await Promise.all(packages.map(async pkg => await visitDependenciesForPackage(pkg, currentLockfile, wantedLockfile, depTypes, dependenciesCache, visitor, opts)));
}

async function visitDependenciesForPackage(packagePath: string, currentLockfile: LockfileObject, wantedLockfile: LockfileObject, depTypes: DepTypes, cache: DependenciesCache, visitor: DependencyVisitor,
  opts: PackageVisitOptions): Promise<void> {
  const importerId = getLockfileImporterId(opts.lockfileDir, path.resolve(opts.lockfileDir, packagePath));
  const parentId: TreeNodeId = {type: 'importer', importerId};
  const rootMetaData = await DependencyMetaData.fromPackageJson(path.join(opts.lockfileDir, packagePath));
  const importer = currentLockfile.importers[importerId];
  if (!importer) {
    throw new Error(`Module of pnpm-workspace not found: '${importerId}'. Ensure the module is listed in each ${WORKSPACE_MANIFEST_FILENAME} and try again.`);
  }
  for (const dependenciesField of DEPENDENCIES_FIELDS.sort().filter(dependenciesField => opts.include[dependenciesField])) {
    const resolvedDependencies = importer[dependenciesField] ?? {};
    for (const alias in resolvedDependencies) {
      const ref = resolvedDependencies[alias];
      const {pkgInfo: packageInfo} = getPkgInfo({
        alias,
        currentPackages: currentLockfile.packages ?? {},
        depTypes,
        rewriteLinkVersionDir: packagePath,
        linkedPathBaseDir: packagePath,
        ref,
        registries: opts.registries,
        skipped: opts.skipped,
        wantedPackages: wantedLockfile?.packages ?? {},
        virtualStoreDir: opts.virtualStoreDir,
        virtualStoreDirMaxLength: opts.virtualStoreDirMaxLength
      });
      const depMetaData = new DependencyMetaData(opts.lockfileDir, packageInfo);
      const stepInto = await visitor(rootMetaData, depMetaData);
      if (stepInto) {
        const childNodeId = getTreeNodeChildId({parentId, dep: {alias, ref}, lockfileDir: opts.lockfileDir, importers: currentLockfile.importers});
        const visitOptions: GetTreeOpts = {
          currentPackages: currentLockfile.packages ?? {},
          excludePeerDependencies: opts.excludePeerDependencies,
          importers: currentLockfile.importers,
          includeOptionalDependencies: opts.include.optionalDependencies,
          depTypes,
          lockfileDir: opts.lockfileDir,
          onlyProjects: opts.onlyProjects,
          rewriteLinkVersionDir: packagePath,
          maxDepth: opts.depth,
          registries: opts.registries,
          skipped: opts.skipped,
          wantedPackages: wantedLockfile?.packages ?? {},
          virtualStoreDir: opts.virtualStoreDir,
          virtualStoreDirMaxLength: opts.virtualStoreDirMaxLength
        };
        await visitDependencyTree(visitOptions, Keypath.initialize(childNodeId), cache, childNodeId, depMetaData, visitor);
      }
    }
  }
}

export type DependencyVisitor = (parent: DependencyMetaData, dependency: DependencyMetaData) => Promise<boolean>;

type PackageVisitOptions = {
  depth: number;
  excludePeerDependencies?: boolean;
  include: { [dependenciesField in DependenciesField]: boolean };
  registries: Registries;
  onlyProjects?: boolean;
  skipped: Set<string>;
  lockfileDir: string;
  checkWantedLockfileOnly?: boolean;
  virtualStoreDir?: string;
  virtualStoreDirMaxLength: number;
};
