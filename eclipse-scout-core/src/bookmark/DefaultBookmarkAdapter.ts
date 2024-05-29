/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BookmarkAdapter, ObjectUuidProvider, ObjectUuidSource, scout} from '../index';

export class DefaultBookmarkAdapter implements BookmarkAdapter {

  owner: ObjectUuidSource;
  useUuidPath: boolean;
  useFallback: boolean;

  constructor(owner: ObjectUuidSource, useUuidPath?: boolean, useFallback?: boolean) {
    this.owner = scout.assertParameter('owner', owner);
    this.useUuidPath = scout.nvl(useUuidPath, true);
    this.useFallback = scout.nvl(useFallback, true);
  }

  buildId(): string {
    if (this.useUuidPath) {
      return this.owner.uuidPath(this.useFallback);
    }
    return scout.create(ObjectUuidProvider, {object: this.owner}).uuid(this.useFallback);
  }
}
