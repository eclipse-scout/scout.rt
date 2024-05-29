/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Desktop, InitModelOf, NullWidget, ObjectFactory, ObjectModel, ObjectWithType, ObjectWithUuid, scout, SomeRequired, strings, Widget} from '../index';

/**
 * Helper class to extract IDs of objects and to compute uuidPaths.
 */
export class ObjectUuidProvider implements ObjectUuidProviderModel, ObjectWithType {

  declare model: ObjectUuidProviderModel;
  declare initModel: SomeRequired<this['model'], 'object'>;
  declare self: ObjectUuidProvider;

  objectType: string;
  id: string;
  object: ObjectUuidSource;
  parent: Widget;

  /**
   * Prefix for all UI generated IDs.
   */
  static UI_ID_PREFIX = '_ui_'; // must not contain any dots ('.') so that the id can be used as css selector "#..." and for the RegExp 'UI_ID_PATTERN'.
  /**
   * Delimiter for the segments of a uuidPath.
   */
  static UUID_PATH_DELIMITER = '|'; // "-" is used by UUID, "." by ClassNames, "_" by ClassId path from Java (see ITypeWithClassId.ID_CONCAT_SYMBOL).
  /**
   * Delimiter used between id and objectType in case a fallback uuid is created and both attributes are available.
   */
  static UUID_FALLBACK_DELIMITER = '@';
  /**
   * Set of widgets which will be skipped when building the uuidPath. A widget is skipped if its class is exactly one of these (NOT instanceof!).
   */
  static UuidPathSkipWidgets: Set<new() => Widget> = new Set<new() => Widget>();
  /** use {@link createUiId} to generate a new ID */
  protected static _uniqueIdSeqNo = 0;
  protected static UI_ID_PATTERN = new RegExp('^' + ObjectUuidProvider.UI_ID_PREFIX + '\\d+$');

  constructor() {
    this.objectType = null;
    this.id = null;
    this.object = null;
    this.parent = null;
  }

  init(model: InitModelOf<this>) {
    this.object = scout.assertParameter('object', model.object);
    this.parent = model.parent || this.object.parent;
  }

  /**
   * Computes a path starting with the {@link uuid} of this object. If a parent is available, its {@link uuidPath} is appended (recursively).
   * {@link UUID_PATH_DELIMITER} is used as delimiter between the segments.
   * If the object is a remote (Scout Classic) object, the classId path has already been computed on the backend and is directly returned without consulting the parent.
   *
   * @param useFallback Optional boolean specifying if a fallback identifier may be used or created in case an object has no specific identifier set. The fallback may be less stable. Default is true.
   * @returns the uuid path starting with this object's uuid or null if no path can be created.
   */
  uuidPath(useFallback?: boolean) {
    const {uuid, isFromClassId} = this._uuid(useFallback);
    if (isFromClassId || !uuid || !this.parent) {
      // Remote ClassId always already includes the path if required. No need to build any path again.
      return uuid;
    }
    const parent = this._findUuidPathParent();
    return strings.join(ObjectUuidProvider.UUID_PATH_DELIMITER, uuid, parent?.uuidPath(useFallback));
  }

  protected _findUuidPathParent(): Widget {
    if (!this.parent) {
      return null;
    }
    if (this._isPathRelevantParent(this.parent)) {
      return this.parent;
    }
    return this.parent.findParent(p => this._isPathRelevantParent(p));
  }

  protected _isPathRelevantParent(w: Widget): boolean {
    if (ObjectUuidProvider.isUuidPathSkipWidget(w) || w instanceof Desktop || w instanceof NullWidget) {
      return false; // always uninteresting parents, event if they have an ID.
    }
    if (w.uuid || w.classId) {
      return true; // accept element if it has a stable id
    }
    // only relevant for fallback case: don't use UI generated Ids.
    return w.id && !ObjectUuidProvider.isUiId(w.id);
  }

  /**
   * Computes an uuid for the object. The result may be a 'classId' for remote objects (Scout Classic) or an 'uuid' for Scout JS elements (if available).
   * If the fallback is enabled, an id might be created using the 'id' property and 'objectType' property.
   * @param includeFallback Optional boolean specifying if a fallback identifier may be used or created in case an object has no specific identifier set. The fallback may be less stable. Default is true.
   * @returns the uuid for the object or null.
   */
  uuid(includeFallback?: boolean): string {
    return this._uuid(includeFallback).uuid;
  }

  protected _uuid(includeFallback?: boolean): { uuid: string; isFromClassId: boolean } {
    // Scout Classic ID
    if (this.object.classId) {
      return {uuid: this.object.classId, isFromClassId: true};
    }

    // Scout JS ID
    if (this.object.uuid) {
      return {uuid: this.object.uuid, isFromClassId: false};
    }

    // Fallback
    if (!scout.nvl(includeFallback, true) || ObjectUuidProvider.isUiId(this.object.id)) {
      return {uuid: null, isFromClassId: false};
    }
    const objectType = this.object.objectType || ObjectFactory.get().getObjectType(this.object.constructor as new() => object);
    let fallbackId = strings.join(ObjectUuidProvider.UUID_FALLBACK_DELIMITER, this.object.id, objectType);
    if (strings.empty(fallbackId)) {
      fallbackId = null; // don't return empty strings
    }
    return {uuid: fallbackId, isFromClassId: false};
  }

  /**
   * @returns true if the given widget should be skipped when computing the {@link uuidPath}.
   */
  static isUuidPathSkipWidget(obj: Widget): boolean {
    return !obj || ObjectUuidProvider.UuidPathSkipWidgets.has(obj.constructor as new() => Widget);
  }

  /**
   * Checks if the given id is a UI ID created from the sequence.
   * @param id The id to check or null.
   * @returns true if the id follows the format of UI IDs (e.g. starts with {@link UI_ID_PREFIX}).
   */
  static isUiId(id: string): boolean {
    return ObjectUuidProvider.UI_ID_PATTERN.test(id);
  }

  /**
   * Returns a new unique UI ID.
   * @returns id with prefix {@link ObjectUuidProvider.UI_ID_PREFIX}.
   */
  static createUiId(): string {
    return ObjectUuidProvider.UI_ID_PREFIX + (++this._uniqueIdSeqNo).toString();
  }
}

/**
 * An object for which an uuid and/or uuidPath can be computed using {@link ObjectUuidProvider}.
 */
export interface ObjectUuidSource extends Partial<ObjectWithUuid>, Partial<ObjectWithType> {
  id?: string;
  classId?: string;
  parent?: Widget;
}

export interface ObjectUuidProviderModel extends ObjectModel<ObjectUuidProvider> {
  object?: ObjectUuidSource;
  parent?: Widget;
}
