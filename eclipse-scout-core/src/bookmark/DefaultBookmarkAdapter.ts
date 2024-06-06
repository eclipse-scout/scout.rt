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
    return this.useUuidPath ? this._buildUuidPath() : this._buildUuid();
  }

  /**
   * @returns Uuid path
   */
  protected _buildUuidPath(): string {
    return this.owner.uuidPath(this.useFallback);
  }

  /**
   * @returns Uuid without path
   */
  protected _buildUuid(): string {
    return ObjectUuidProvider.get().uuid(this.owner, this.useFallback);
  }
}
