/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, Desktop, PageParamDo, PageResolver, scout, StringValueDo, UiCallbackHandler} from '../index';
import $ from 'jquery';

export class JsPageUiHandler implements UiCallbackHandler {
  handle(callbackId: string, owner: Desktop, request: PageParamDo): JQuery.Promise<BaseDoEntity> {
    const objectType = PageResolver.get().findObjectTypeForPageParam(request);
    if (!objectType) {
      $.log.warn('No page objectType found for ' + JSON.stringify(request));
    }
    const result = scout.create(StringValueDo, {value: objectType});
    return $.resolvedPromise(result);
  }
}
