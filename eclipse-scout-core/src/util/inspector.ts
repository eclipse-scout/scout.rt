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
   * Adds inspector info (e.g. classId) from the given 'model' to the DOM. The target element
   * is either the given '$container' or model.$container. Nothing happens if model or target
   * element is undefined or the inspector is disabled in the session.
   */
  applyInfo(model: InspectorModel, $container?: JQuery, session?: Session) {
    if (!model) {
      return;
    }
    $container = $container || model.$container;
    if (!$container) {
      return;
    }
    session = session || model.session;
    if (!session?.inspector) {
      return;
    }

    let modelClass = model.modelClass || (typeof model.objectType === 'string' && model.objectType);
    let uuid: string = null;
    if (model.buildUuidPath) {
      uuid = model.buildUuidPath();
    } else {
      uuid = model.classId || model.uuid;
    }
    $container.toggleAttr('data-modelclass', !!modelClass, modelClass);
    $container.toggleAttr('data-uuid', !!uuid, uuid);
    $container.toggleAttr('data-id', !!model.id, model.id);
  }
};

export interface InspectorModel extends Partial<ObjectWithUuid> {
  session?: Session;
  $container?: JQuery;

  id?: string;
  modelClass?: string;
  classId?: string;
  objectType?: ObjectType;
}
