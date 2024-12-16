/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Desktop, NullWidget, ObjectFactory, ObjectModel, ObjectWithType, ObjectWithUuid, scout, SomeRequired, strings, Widget} from '../index';

/**
 * Helper class to extract IDs of objects and to compute uuidPaths.
 */
export class ObjectUuidProvider implements ObjectUuidProviderModel, ObjectWithType {

  declare model: ObjectUuidProviderModel;
  declare initModel: SomeRequired<this['model'], 'object'>;
  declare self: ObjectUuidProvider;

  objectType: string;
  id: string;

  static INSTANCE: ObjectUuidProvider = null;
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
  }

  /**
   * Computes a path starting with the {@link uuid} of this object. If a parent is available, its {@link uuidPath} is appended to the right (recursively).
   * {@link UUID_PATH_DELIMITER} is used as delimiter between the segments.
   * By default, if the object is a remote (Scout Classic) object having a classId, its value is directly returned without consulting the parent has classIds typically already include its parents.
   *
   * @param object The object for which the uuidPath should be computed.
   * @param options Optional {@link UuidPathOptions} controlling the computation of the path.
   *
   * @returns the uuid path starting with this object's uuid or null if no path can be created.
   */
  uuidPath(object: ObjectUuidSource, options?: UuidPathOptions) {
    const uuid = this.uuid(object, options?.useFallback);
    if (!uuid) {
      return null;
    }
    let parent = options?.parent || object.parent;
    if (!parent) {
      return uuid;
    }
    const skipParent = !scout.nvl(options?.appendParent, !object.classId); // by default stop on classIds as they typically include its parents already
    if (skipParent) {
      return uuid;
    }
    parent = this._findUuidPathParent(parent);
    return strings.join(ObjectUuidProvider.UUID_PATH_DELIMITER, uuid, parent?.uuidPath(options?.useFallback));
  }

  protected _findUuidPathParent(parent: Widget): Widget {
    if (!parent) {
      return null;
    }
    if (this._isPathRelevantParent(parent)) {
      return parent;
    }
    return parent.findParent(p => this._isPathRelevantParent(p));
  }

  protected _isPathRelevantParent(parent: Widget): boolean {
    if (ObjectUuidProvider.isUuidPathSkipWidget(parent) || parent instanceof Desktop || parent instanceof NullWidget) {
      return false; // always uninteresting parents, event if they have a stable ID.
    }
    if (parent.uuid || parent.classId) {
      return true; // accept element if it has a stable id
    }
    // only relevant for fallback case: don't use UI generated Ids.
    return parent.id && !ObjectUuidProvider.isUiId(parent.id);
  }

  /**
   * Computes an uuid for the given object. The result may be a 'classId' for remote objects (Scout Classic) or an 'uuid' for Scout JS elements (if available).
   * If the fallback is enabled, an id might be created using the 'id' property and 'objectType' property.
   * @param includeFallback Optional boolean specifying if a fallback identifier may be created in case an object has no specific identifier set. The fallback may be less stable. Default is true.
   * @returns the uuid for the object or null.
   */
  uuid(object: ObjectUuidSource, includeFallback?: boolean): string {
    if (!object) {
      return null;
    }

    // Scout Classic ID
    if (object.classId) {
      return object.classId;
    }

    // Scout JS ID
    if (object.uuid) {
      return object.uuid;
    }

    // Fallback
    if (!scout.nvl(includeFallback, true) || ObjectUuidProvider.isUiId(object.id)) {
      return null; // no fallback
    }
    const objectType = object.objectType || ObjectFactory.get().getObjectType(object.constructor as new() => object);
    let fallbackId = strings.join(ObjectUuidProvider.UUID_FALLBACK_DELIMITER, object.id, objectType);
    if (!fallbackId) {
      return null; // don't return empty strings
    }
    return fallbackId;
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
    // FIXME mvi [js-bookmark] Find better name than UI ID. Sounds too similar to UUID. Also adapt prefix?
    return ObjectUuidProvider.UI_ID_PREFIX + (++this._uniqueIdSeqNo).toString();
  }

  /**
   * @returns The shared singleton {@link ObjectUuidProvider} instance.
   */
  static get(): ObjectUuidProvider {
    if (!ObjectUuidProvider.INSTANCE) {
      ObjectUuidProvider.INSTANCE = scout.create(ObjectUuidProvider);
    }
    return ObjectUuidProvider.INSTANCE;
  }
}

export interface UuidPathOptions {
  /**
   * Optional boolean specifying if a fallback identifier may be created in case an object has no specific identifier set. The fallback may be less stable. Default is true.
   */
  useFallback?: boolean;

  /**
   * Optional boolean to control if the path should include the {@link uuidPath} of the parent.
   * By default, the parent is included unless the object has a classId set as classIds typically already include its parents (computed by the Java server).
   */
  appendParent?: boolean;

  /**
   * Optional {@link Widget} to use as parent of the object given. By the default 'object.parent' is used.
   */
  parent?: Widget;
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
