/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Constructor, numbers, ObjectFactory, ObjectModel, objects, ObjectWithType, ObjectWithUuid, scout, SomeRequired, strings, Widget} from '../index';

/**
 * Helper class to extract IDs of objects and to compute uuidPaths.
 */
export class ObjectUuidProvider implements ObjectUuidProviderModel, ObjectWithType {

  declare model: ObjectUuidProviderModel;
  declare initModel: SomeRequired<this['model'], 'object'>;
  declare self: ObjectUuidProvider;

  objectType: string;
  id: string;

  protected _uiSeqIdNo = 0;

  /**
   * Prefix for all UI generated IDs.
   */
  static UI_SEQ_ID_PREFIX = '_ui_'; // must not contain any dots ('.') so that the id can be used as css selector "#..." and for the RegExp 'UI_SEQ_PATTERN'.

  /**
   * Delimiter for the segments of a uuidPath.
   */
  static UUID_PATH_DELIMITER = '|'; // "-" is used by UUID, "." by ClassNames, "_" by ClassId path from Java (see ITypeWithClassId.ID_CONCAT_SYMBOL).

  /**
   * Delimiter for dependent uuids
   */
  static DEPENDENT_UUID_DELIMITER = '@';

  /** use {@link createUiSeqId} to generate a new ID */
  protected static _UI_SEQ_ID_PATTERN = new RegExp('^' + ObjectUuidProvider.UI_SEQ_ID_PREFIX + '\\d+$');
  protected static _INSTANCE: ObjectUuidProvider;

  /**
   * Modifiable set of widgets which will be skipped when building the uuidPath.
   * A widget is skipped if its class is exactly one of these (NOT instanceof!).
   *
   * A widget may be skipped if it is not relevant for computing the uuidPath, e.g. if it is only a layouting component.
   * For example: A group box is skipped because the id or uuid of a widget is normally unique inside a form so the group box would unnecessarily enlarge the uuidPath.
   * Also, if the widget is moved into another group box, the uuidPath won't be affected.
   * If the group box is extracted into a separate widget and gets its own class (aka. template field)
   * it must not be skipped anymore because this template can be used multiple times on the same form and must therefore be part of the uuidPath.
   * This template use case is the reason why the subclasses of the registered widgets are not considered.
   */
  static uuidPathSkipWidgets: Set<Constructor<Widget>> = new Set<Constructor<Widget>>();

  /**
   * Modifiable list of rules which are used to determine if a parent should be skipped when building the uuidPath.
   *
   * These rules are always applied, even if {@link UuidPathOptions.considerSkipWidgets} is set to false.
   */
  static uuidPathAlwaysSkipRules: ((widget: Widget) => boolean)[] = [];

  constructor() {
    this.objectType = null;
    this.id = null;
  }

  /**
   * Computes a path starting with the {@link uuid} of this object. If a parent is available, its {@link uuidPath} is appended to the right (recursively).
   * {@link UUID_PATH_DELIMITER} is used as delimiter between the segments.
   * By default, if the object is a remote (Scout Classic) object having a classId, its value is directly returned without appending the parent path because classIds typically already include its parents.
   *
   * @param object The object for which the uuidPath should be computed.
   * @param options Optional {@link UuidPathOptions} controlling the computation of the path.
   *
   * @returns the uuid path starting with this object's uuid or null if no path can be created.
   */
  uuidPath(object: ObjectUuidSource, options?: UuidPathOptions) {
    options = scout.nvl(options, {});

    const uuid = this._buildUuid(object, options.useFallback);

    // Abort if the starting element (not a parent) does not have an uuid
    if (!uuid && scout.nvl(options.abortIfNoUuidFound, true)) {
      return null;
    }

    // Abort if there is no parent
    let parent = options.parent || object.parent;
    if (!parent) {
      return uuid;
    }

    // By default, stop on classIds as they typically include its parents already
    const appendParent = !object.classId;
    if (!appendParent) {
      return uuid;
    }

    // Find the next relevant parent (some parents may be skipped)
    let considerSkipWidgets = this._computeConsiderSkipWidgets(options, object);
    parent = this._findUuidPathParent(parent, considerSkipWidgets);

    // Prepare options for the next iteration
    options.considerSkipWidgets = considerSkipWidgets;
    options.abortIfNoUuidFound = scout.nvl(options.abortIfNoUuidFound, false); // Skip parents without an uuid
    options.parent = null; // Passed element must only be used for starting element

    // Build the parent path and join it with the current uuid
    return strings.join(ObjectUuidProvider.UUID_PATH_DELIMITER, uuid, parent?.buildUuidPath(options));
  }

  protected _computeConsiderSkipWidgets(options: UuidPathOptions, object: ObjectUuidSource): UuidPathConsiderSkipWidgets {
    if (objects.isNullOrUndefined(options.considerSkipWidgets) || options.considerSkipWidgets === 'dynamicFalse') {
      // Do not skip parent widgets if the object only has an object type because the objectType normally is not unique enough
      return (!!object.uuid || !!object.classId || this._considerId(object)) ? 'dynamicTrue' : 'dynamicFalse';
    }
    return options.considerSkipWidgets;
  }

  protected _findUuidPathParent(parent: Widget, considerSkipWidgets: UuidPathConsiderSkipWidgets): Widget {
    if (!parent) {
      return null;
    }
    if (this._isPathRelevantParent(parent, considerSkipWidgets)) {
      return parent;
    }
    return parent.findParent(p => this._isPathRelevantParent(p, considerSkipWidgets));
  }

  protected _isPathRelevantParent(parent: Widget, considerSkipWidgets: UuidPathConsiderSkipWidgets): boolean {
    if (this._skipParent(parent, scout.isOneOf(considerSkipWidgets, true, 'dynamicTrue'))) {
      return false; // always uninteresting parents, event if they have a stable ID.
    }
    return true;
  }

  protected _buildUuid(object: ObjectUuidSource, useFallback?: boolean) {
    if (object.buildUuid) {
      return object.buildUuid(useFallback);
    }
    return this.uuid(object, useFallback);
  }

  /**
   * Computes an uuid for the given object. The result may be a 'classId' for remote objects (Scout Classic) or an 'uuid' for Scout JS elements (if available).
   * If the fallback is enabled, an id might be created using the 'id' property and 'objectType' property.
   *
   * @param useFallback Optional boolean specifying if a fallback identifier may be created in case an object has no specific identifier set. The fallback may be less stable. Default is true.
   * @returns the uuid for the object or null.
   */
  uuid(object: ObjectUuidSource, useFallback?: boolean): string {
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
    if (!scout.nvl(useFallback, true)) {
      return null; // no fallback
    }
    if (this._considerId(object)) {
      return object.id;
    }
    let objectType;
    if (typeof object.objectType === 'string') {
      objectType = object.objectType;
    } else {
      const objectFactory = ObjectFactory.get();
      objectType = objectFactory.getObjectType(object.constructor as Constructor) || objectFactory.getObjectType(object.objectType);
    }
    if (objectType) {
      return objectType;
    }
    return null;
  }

  protected _considerId(object: ObjectUuidSource) {
    let id = object.id;
    if (strings.empty(id)) {
      return false;
    }
    if (this.isUiSeqId(id)) {
      return false;
    }
    if (numbers.isNumber(parseInt(id))) {
      // Model adapter ids
      return false;
    }
    return true;
  }

  /**
   * @returns true if the given widget should be skipped when computing the {@link uuidPath}.
   */
  protected _skipParent(obj: Widget, considerSkipWidgets = true): boolean {
    if (!obj) {
      return true;
    }
    let skip = considerSkipWidgets && ObjectUuidProvider.uuidPathSkipWidgets.has(obj.constructor as Constructor<Widget>);
    if (skip) {
      return true;
    }
    return ObjectUuidProvider.uuidPathAlwaysSkipRules.some(rule => rule(obj));
  }

  /**
   * Builds the uuid of the object and prepends the given prefix.
   *
   * This is useful for objects not having an own uuid but need to be referenced nevertheless.
   */
  createDependentUuid(prefix: string, source: ObjectUuidSource): string {
    const uuid = this._buildUuid(source);
    if (!uuid) {
      return null;
    }
    return strings.join(ObjectUuidProvider.DEPENDENT_UUID_DELIMITER, prefix, uuid);
  }

  setDependentUuid(prefix: string, source: ObjectUuidSource, target: Required<ObjectUuidSource>): string {
    if (target.uuid || target.classId) {
      return;
    }
    const uuid = this.createDependentUuid(prefix, source);
    if (!uuid) {
      return;
    }
    return target.setUuid(uuid);
  }

  /**
   * Checks if the given id is an id created from the ui sequence.
   * @param id The id to check or null.
   * @returns true if the id follows the format of UI SEQ IDs (e.g. starts with {@link UI_SEQ_ID_PREFIX}).
   */
  isUiSeqId(id: string): boolean {
    return ObjectUuidProvider._UI_SEQ_ID_PATTERN.test(id);
  }

  /**
   * Returns a new unique UI ID.
   * @returns id with prefix {@link ObjectUuidProvider.UI_SEQ_ID_PREFIX}.
   */
  createUiSeqId(): string {
    return ObjectUuidProvider.UI_SEQ_ID_PREFIX + (++this._uiSeqIdNo).toString();
  }

  /**
   * @returns current uiSeqId number
   */
  get uiSeqIdNo(): number {
    return this._uiSeqIdNo;
  }

  /**
   * @returns The shared singleton {@link ObjectUuidProvider} instance.
   */
  static get(): ObjectUuidProvider {
    if (!ObjectUuidProvider._INSTANCE) {
      ObjectUuidProvider._INSTANCE = scout.create(ObjectUuidProvider);
    }
    return ObjectUuidProvider._INSTANCE;
  }
}

export interface UuidPathOptions {
  /**
   * Specifies if a fallback identifier may be created in case an object has no specific identifier set.
   * The fallback may be less stable.
   *
   * Default is true.
   */
  useFallback?: boolean;

  /**
   * Specifies whether computation of the uuidPath should be aborted as soon as {@link ObjectUuidProvider.uuid} returns null.
   * By default, the computation will be aborted if no uuid can be computed for the starting element.
   * If no uuid can be computed for a parent, the parent will be skipped and the computation continues with the next parent.
   */
  abortIfNoUuidFound?: boolean;

  /**
   * Specifies whether the {@link ObjectUuidProvider.uuidPathSkipWidgets} should be considered when building the uuidPath.
   * By default, the skipWidgets are considered once an object (either the starting element or a parent) is found with a relevant id (id, uuid or classId).
   *
   * For example:
   * - When the starting element contains a relevant id, the skipWidgets are considered for the parents -> all parents that are part of skipWidgets will be skipped.
   * - When the starting element does not contain a relevant id, the parents are not skipped even if they are part of the skipWidgets until a parent is reached with a relevant id.
   *   All further parents may be skipped again if they are part of the skipWidgets.
   */
  considerSkipWidgets?: UuidPathConsiderSkipWidgets;

  /**
   * Optional {@link Widget} to use as parent of the object given.
   * By the default 'object.parent' is used.
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

export type UuidPathConsiderSkipWidgets = boolean | 'dynamicTrue' | 'dynamicFalse';
