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
import {type PackageSnapshots, type ProjectSnapshot} from '@pnpm/lockfile.fs';
import {type DepTypes} from '@pnpm/lockfile.detect-dep-types';
import {type Finder, type Registries} from '@pnpm/types';

import {type PackageNode} from '../tree/PackageNode.ts';
import {getPkgInfo} from '../tree/getPkgInfo.ts';
import {DependenciesCache} from '../tree/DependenciesCache.ts';
import {type TreeNodeId} from '../tree/TreeNodeId.ts';
import {Keypath} from '../tree/Keypath.ts';
import {getTreeNodeChildId} from '../tree/getTreeNodeChildId.ts';

import {DependencyMetaData} from './DependencyMetaData.ts';
import {type DependencyVisitor} from './DependencyVisitor.ts';

// Inspired by https://github.com/pnpm/pnpm/blob/v10.26.1/reviewing/dependencies-hierarchy/src/getTree.ts

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

export async function visitDependencyTree(opts: GetTreeOpts, keypath: Keypath, dependenciesCache: DependenciesCache, parentId: TreeNodeId, parentDepMetaData: DependencyMetaData, visitor: DependencyVisitor): Promise<DependencyInfo> {
  if (opts.maxDepth <= 0) {
    return {dependencies: [], height: 'unknown'};
  }

  function getSnapshot(treeNodeId: TreeNodeId) {
    switch (treeNodeId.type) {
      case 'importer':
        return opts.importers[treeNodeId.importerId];
      case 'package':
        return opts.currentPackages[treeNodeId.depPath];
    }
  }

  const snapshot = getSnapshot(parentId);
  if (!snapshot) {
    return {dependencies: [], height: 0};
  }

  const deps = !opts.includeOptionalDependencies ? snapshot.dependencies : {
    ...snapshot.dependencies,
    ...snapshot.optionalDependencies
  };
  if (deps == null) {
    return {dependencies: [], height: 0};
  }

  function getPeerDependencies() {
    switch (parentId.type) {
      case 'importer':
        // Projects in the pnpm workspace can declare peer dependencies, but pnpm
        // doesn't record this block to the importers lockfile object. Returning
        // undefined for now.
        return undefined;
      case 'package':
        return opts.currentPackages[parentId.depPath]?.peerDependencies;
    }
  }

  const peers = new Set(Object.keys(getPeerDependencies() ?? {}));

  // If the "ref" of any dependency is a file system path (e.g. link:../), the
  // base directory of this relative path depends on whether the dependent
  // package is in the pnpm workspace or from node_modules.
  function getLinkedPathBaseDir() {
    switch (parentId.type) {
      case 'importer':
        return path.join(opts.lockfileDir, parentId.importerId);
      case 'package':
        return opts.lockfileDir;
    }
  }

  const linkedPathBaseDir = getLinkedPathBaseDir();
  const resultDependencies: PackageNode[] = [];
  let resultHeight: number | 'unknown' = 0;
  let resultCircular = false;

  for (const alias in deps) {
    const ref = deps[alias];
    const {pkgInfo: packageInfo} = getPkgInfo({
      alias,
      currentPackages: opts.currentPackages,
      depTypes: opts.depTypes,
      rewriteLinkVersionDir: opts.rewriteLinkVersionDir,
      linkedPathBaseDir,
      peers,
      ref,
      registries: opts.registries,
      skipped: opts.skipped,
      wantedPackages: opts.wantedPackages,
      virtualStoreDir: opts.virtualStoreDir,
      virtualStoreDirMaxLength: opts.virtualStoreDirMaxLength
    });
    let circular: boolean;
    let newEntry: PackageNode | null = null;
    const depMetaData = new DependencyMetaData(opts.lockfileDir, packageInfo);
    const stepInto = opts.excludePeerDependencies && packageInfo.isPeer ? false : await visitor(parentDepMetaData, depMetaData);
    const childTreeMaxDepth = stepInto ? opts.maxDepth - 1 : -1;
    const nodeId = getTreeNodeChildId({parentId, dep: {alias, ref}, lockfileDir: opts.lockfileDir, importers: opts.importers});
    if (nodeId == null) {
      circular = false;
      newEntry = packageInfo;
    } else {
      circular = keypath.includes(nodeId);
      let dependencies: PackageNode[] | undefined;
      if (circular) {
        dependencies = [];
      } else {
        const cacheEntry = dependenciesCache.get({parentId: nodeId, requestedDepth: childTreeMaxDepth});
        const children = cacheEntry ?? await visitDependencyTree({...opts, maxDepth: childTreeMaxDepth}, keypath.concat(nodeId), dependenciesCache, nodeId, depMetaData, visitor);
        if (cacheEntry == null && !children.circular) {
          if (children.height === 'unknown') {
            dependenciesCache.addPartiallyVisitedResult(nodeId, {dependencies: children.dependencies, depth: childTreeMaxDepth});
          } else {
            dependenciesCache.addFullyVisitedResult(nodeId, {dependencies: children.dependencies, height: children.height});
          }
        }

        const heightOfCurrentDepNode = children.height === 'unknown' ? 'unknown' : children.height + 1;
        dependencies = children.dependencies;
        resultHeight = resultHeight === 'unknown' || heightOfCurrentDepNode === 'unknown' ? 'unknown' : Math.max(resultHeight, heightOfCurrentDepNode);
        resultCircular = resultCircular || (children.circular ?? false);
      }

      if (dependencies.length > 0) {
        newEntry = {...packageInfo, dependencies};
      } else {
        newEntry = packageInfo;
      }
    }

    if (newEntry != null) {
      if (circular) {
        newEntry.circular = true;
        resultCircular = true;
      }
      if (!newEntry.isPeer || !opts.excludePeerDependencies || newEntry.dependencies?.length) {
        resultDependencies.push(newEntry);
      }
    }
  }

  const result: DependencyInfo = {dependencies: resultDependencies, height: resultHeight};
  if (resultCircular) {
    result.circular = resultCircular;
  }
  return result;
}

export interface GetTreeOpts {
  maxDepth: number;
  rewriteLinkVersionDir: string;
  includeOptionalDependencies: boolean;
  excludePeerDependencies?: boolean;
  lockfileDir: string;
  onlyProjects?: boolean;
  search?: Finder;
  skipped: Set<string>;
  registries: Registries;
  importers: Record<string, ProjectSnapshot>;
  depTypes: DepTypes;
  currentPackages: PackageSnapshots;
  wantedPackages: PackageSnapshots;
  virtualStoreDir?: string;
  virtualStoreDirMaxLength: number;
}

export interface DependencyInfo {
  dependencies: PackageNode[];

  circular?: true;

  /**
   * The number of edges along the longest path, including the parent node.
   *
   *   - `"unknown"` if traversal was limited by a max depth option, therefore
   *      making the true height of a package undetermined.
   *   - `0` if the dependencies array is empty.
   *   - `1` if the dependencies array has at least 1 element and no child
   *     dependencies.
   */
  height: number | 'unknown';
}
