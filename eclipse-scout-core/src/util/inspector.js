/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

/**
 * Adds inspector info (e.g. classId) from the given 'model' to the DOM. The target element
 * is either the given '$container' or model.$container. Nothing happens if model or target
 * element is undefined.
 */
export function applyInfo(model, $container) {
  if (!model) {
    return;
  }
  $container = $container || model.$container;
  if (!$container) {
    return;
  }
  $container.toggleAttr('data-id', !!model.id, model.id);
  $container.toggleAttr('data-modelclass', !!model.modelClass, model.modelClass);
  $container.toggleAttr('data-classid', !!model.classId, model.classId);
}

export default {
  applyInfo
};
