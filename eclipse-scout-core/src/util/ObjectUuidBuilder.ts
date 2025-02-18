/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {InitModelOf, ObjectUuidProvider, ObjectUuidSource, scout} from '../index';

export class ObjectUuidBuilder implements ObjectUuidBuilderModel {
  declare model: ObjectUuidBuilderModel;
  owner: ObjectUuidSource;
  useUuidPath: boolean;
  useFallback: boolean;

  init(model: InitModelOf<this>) {
    this.owner = scout.assertParameter('owner', model.owner);
    this.useUuidPath = scout.nvl(model.useUuidPath, true);
    this.useFallback = scout.nvl(model.useFallback, true);
  }

  /**
   * @returns the {@link ObjectUuidSource.uuidPath} of the owner if {@link useUuidPath} is true
   *          or the single uuid of the owner returned by {@link ObjectUuidProvider.uuid}.
   */
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

export interface ObjectUuidBuilderModel {
  owner: ObjectUuidSource;
  useUuidPath?: boolean;
  useFallback?: boolean;
}

/**
 * Objects having a {@link ObjectUuidBuilder}.
 */
export interface ObjectWithObjectUuidBuilder {
  /**
   * @returns the {@link ObjectUuidBuilder} for this object. Never returns null.
   */
  getObjectUuidBuilder(): ObjectUuidBuilder;
}

