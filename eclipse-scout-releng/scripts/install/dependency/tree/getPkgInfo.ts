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
import normalizePath from 'normalize-path';
import {type PackageSnapshot, type PackageSnapshots, type TarballResolution} from '@pnpm/lockfile.fs';
import {nameVerFromPkgSnapshot, pkgSnapshotToResolution} from '@pnpm/lockfile.utils';
import {DepType, type DepTypes} from '@pnpm/lockfile.detect-dep-types';
import {type DependencyManifest, type Registries} from '@pnpm/types';
import {depPathToFilename, refToRelative} from '@pnpm/dependency-path';
import {readPackageJsonFromDirSync} from '@pnpm/read-package-json';

// Inspired by https://github.com/pnpm/pnpm/blob/v10.26.1/reviewing/list/src/getPkgInfo.ts

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

export interface GetPkgInfoOpts {
  readonly alias: string;
  readonly ref: string;
  readonly currentPackages: PackageSnapshots;
  readonly peers?: Set<string>;
  readonly registries: Registries;
  readonly skipped: Set<string>;
  readonly wantedPackages: PackageSnapshots;
  readonly virtualStoreDir?: string;
  readonly virtualStoreDirMaxLength: number;
  readonly depTypes: DepTypes;

  /**
   * The base dir if the `ref` argument is a `"link:"` relative path.
   */
  readonly linkedPathBaseDir: string;

  /**
   * If the `ref` argument is a `"link:"` relative path, the ref is reused for
   * the version field. (Since the true semver may not be known.)
   *
   * Optionally rewrite this relative path to a base dir before writing it to
   * version.
   */
  readonly rewriteLinkVersionDir?: string;
}

export function getPkgInfo(opts: GetPkgInfoOpts): { pkgInfo: PackageInfo; readManifest: () => DependencyManifest } {
  let name!: string;
  let version: string;
  let resolved: string | undefined;
  let depType: DepType | undefined;
  let optional: true | undefined;
  let isSkipped = false;
  let isMissing = false;
  const depPath = refToRelative(opts.ref, opts.alias);
  if (depPath) {
    let pkgSnapshot!: PackageSnapshot;
    if (opts.currentPackages[depPath]) {
      pkgSnapshot = opts.currentPackages[depPath];
      const parsed = nameVerFromPkgSnapshot(depPath, pkgSnapshot);
      name = parsed.name;
      version = parsed.version;
    } else {
      pkgSnapshot = opts.wantedPackages[depPath];
      if (pkgSnapshot) {
        const parsed = nameVerFromPkgSnapshot(depPath, pkgSnapshot);
        name = parsed.name;
        version = parsed.version;
      } else {
        name = opts.alias;
        version = opts.ref;
      }
      isMissing = true;
      isSkipped = opts.skipped.has(depPath);
    }
    resolved = (pkgSnapshotToResolution(depPath, pkgSnapshot, opts.registries) as TarballResolution).tarball;
    depType = opts.depTypes[depPath];
    optional = pkgSnapshot.optional;
  } else {
    name = opts.alias;
    version = opts.ref;
  }
  if (!version) {
    version = opts.ref;
  }
  const fullPackagePath = depPath
    ? path.join(opts.virtualStoreDir ?? '.pnpm', depPathToFilename(depPath, opts.virtualStoreDirMaxLength), 'node_modules', name)
    : path.join(opts.linkedPathBaseDir, opts.ref.slice(5));

  if (version.startsWith('link:') && opts.rewriteLinkVersionDir) {
    version = `link:${normalizePath(path.relative(opts.rewriteLinkVersionDir, fullPackagePath))}`;
  }

  const packageInfo: PackageInfo = {
    alias: opts.alias,
    isMissing,
    isPeer: Boolean(opts.peers?.has(opts.alias)),
    isSkipped,
    name,
    path: fullPackagePath,
    version
  };
  if (resolved) {
    packageInfo.resolved = resolved;
  }
  if (optional === true) {
    packageInfo.optional = true;
  }
  if (depType === DepType.DevOnly) {
    packageInfo.dev = true;
  } else if (depType === DepType.ProdOnly) {
    packageInfo.dev = false;
  }
  return {
    pkgInfo: packageInfo,
    readManifest: () => readPackageJsonFromDirSync(fullPackagePath)
  };
}

export interface PackageInfo {
  alias: string;
  isMissing: boolean;
  isPeer: boolean;
  isSkipped: boolean;
  name: string;
  path: string;
  version: string;
  resolved?: string;
  optional?: true;
  dev?: boolean;
}
