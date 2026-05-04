/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {type DepPath} from '@pnpm/types';

// Inspired by https://github.com/pnpm/pnpm/blob/v10.26.1/reviewing/dependencies-hierarchy/src/TreeNodeId.ts

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

export type TreeNodeId = TreeNodeIdImporter | TreeNodeIdPackage;

/**
 * A project local to the pnpm workspace.
 */
export interface TreeNodeIdImporter {
  readonly type: 'importer';
  readonly importerId: string;
}

/**
 * A npm package depended on externally.
 */
export interface TreeNodeIdPackage {
  readonly type: 'package';
  readonly depPath: DepPath;
}

export function serializeTreeNodeId(treeNodeId: TreeNodeId): string {
  switch (treeNodeId.type) {
    case 'importer': {
      // Only serialize known fields from TreeNodeId. TypeScript is duck typed and
      // objects can have any number of unknown extra fields.
      const {type, importerId} = treeNodeId;
      return JSON.stringify({type, importerId});
    }
    case 'package': {
      const {type, depPath} = treeNodeId;
      return JSON.stringify({type, depPath});
    }
  }
}
