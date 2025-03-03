/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {PageParamDo, PageResolver, scout, UiCallbackHandler, UiCallbackParam} from '../index';
import $ from 'jquery';

export class ResolveJsPageUiCallbackHandler implements UiCallbackHandler {

  handle(param: UiCallbackParam): JQuery.Promise<any> {
    const pageParam = scout.assertInstance(param.data, PageParamDo);

    let objectType = PageResolver.get().findObjectTypeForPageParam(pageParam);
    if (!objectType) {
      return $.rejectedPromise('No page objectType found for ' + JSON.stringify(pageParam));
    }
    return $.resolvedPromise(objectType);
  }
}
