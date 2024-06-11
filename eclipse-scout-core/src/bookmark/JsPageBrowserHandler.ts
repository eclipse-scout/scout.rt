/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BrowserCallbackHandler, Desktop, DoEntity, PageParamDo, PageResolver, ValueDo} from '../index';
import $ from 'jquery';

export class JsPageBrowserHandler implements BrowserCallbackHandler {
  handle(callbackId: string, owner: Desktop, request: PageParamDo): JQuery.Promise<DoEntity> {
    const objectType = PageResolver.get().findObjectTypeForPageParam(request);
    const result: ValueDo<string> = {
      _type: 'scout.StringValue',
      value: objectType
    };
    return $.resolvedPromise(result);
  }
}
