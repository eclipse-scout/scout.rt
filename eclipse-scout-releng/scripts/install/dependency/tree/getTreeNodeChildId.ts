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
import {refToRelative} from '@pnpm/dependency-path';
import {getLockfileImporterId, type ProjectSnapshot} from '@pnpm/lockfile.fs';
import {type TreeNodeId} from './TreeNodeId.ts';

// Inspired by https://github.com/pnpm/pnpm/blob/v10.26.1/reviewing/dependencies-hierarchy/src/getTreeNodeChildId.ts

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

export interface getTreeNodeChildIdOpts {
  readonly parentId: TreeNodeId;
  readonly dep: {
    readonly alias: string;
    readonly ref: string;
  };
  readonly lockfileDir: string;
  readonly importers: Record<string, ProjectSnapshot>;
}

export function getTreeNodeChildId(opts: getTreeNodeChildIdOpts): TreeNodeId | undefined {
  const depPath = refToRelative(opts.dep.ref, opts.dep.alias);
  if (depPath !== null) {
    return {type: 'package', depPath};
  }

  switch (opts.parentId.type) {
    case 'importer': {
      // This should be a link given depPath is null.
      //
      // TODO: Consider updating refToRelative (or writing a new function) to
      // return an enum so there's no implicit assumptions.
      const linkValue = opts.dep.ref.slice('link:'.length);

      // It's a bit roundabout to prepend the lockfile dir only to remove it
      // through getLockfileImporterId, but we can be more certain the right
      // importerId is created by reusing the getLockfileImporterId function.
      const absoluteLinkedPath = path.join(opts.lockfileDir, opts.parentId.importerId, linkValue);
      const childImporterId = getLockfileImporterId(opts.lockfileDir, absoluteLinkedPath);

      // A 'link:' reference may refer to a package outside the pnpm workspace.
      // Return undefined in that case since it would be difficult to list/traverse
      // that package outside the pnpm workspace.
      const isLinkOutsideWorkspace = opts.importers[childImporterId] == null;
      return isLinkOutsideWorkspace
        ? undefined
        : {type: 'importer', importerId: childImporterId};
    }
    case 'package':
      // In theory an external package could be overridden to link to a
      // dependency in the pnpm workspace. Avoid traversing through this
      // edge case for now.
      return undefined;
  }
}
