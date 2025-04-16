/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ObjectType, ObjectWithUuid, Session} from '../index';

export const inspector = {
  /**
   * Adds inspector info (uuid, modelclass, id) from the given `object` to the DOM.
   *
   * The target element is either the given `$target` or `object.$container`.
   * Nothing happens if the object or target element is undefined or the inspector is disabled in the session.
   *
   * @param $target needs to be set if the object does not provide a $container
   * @param session needs to be set if the object does not provide a session
   */
  applyInfo(object: InspectedObject, $target?: JQuery, session?: Session) {
    if (!object) {
      return;
    }
    $target = $target || object.$container;
    if (!$target) {
      return;
    }
    session = session || object.session;
    if (!session?.inspector) {
      return;
    }

    let modelClass = object.modelClass;
    if (!modelClass && typeof object.objectType === 'string') {
      modelClass = object.objectType;
    }
    let uuid: string = null;
    if (object.buildUuidPath) {
      uuid = object.buildUuidPath();
    } else {
      uuid = object.classId || object.uuid;
    }
    $target.toggleAttr('data-modelclass', !!modelClass, modelClass);
    $target.toggleAttr('data-uuid', !!uuid, uuid);
    $target.toggleAttr('data-id', !!object.id, object.id);
  }
};

export interface InspectedObject extends Partial<ObjectWithUuid> {
  session?: Session;
  $container?: JQuery;

  id?: string;
  modelClass?: string;
  classId?: string;
  objectType?: ObjectType;
}
