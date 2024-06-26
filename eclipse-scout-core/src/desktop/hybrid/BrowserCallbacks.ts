/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, BaseDoEntity, BrowserCallbacksEventMap, DoEntity, scout, typeName, Widget} from '../../index';

export class BrowserCallbacks extends Widget {

  declare eventMap: BrowserCallbacksEventMap;
  declare self: BrowserCallbacks;

  onBrowserCallbackRequest(handlerObjectType: string, callbackId: string, owner: Widget, request: DoEntity) {
    const handler = scout.create(handlerObjectType) as BrowserCallbackHandler;
    handler.handle(callbackId, owner, request)
      .then(response => this._triggerBrowserResponse(callbackId, response))
      .catch(error => this._handleCallbackError(callbackId, error));
  }

  protected _handleCallbackError(callbackId: string, error: any): JQuery.Promise<void> {
    if (error instanceof BrowserCallbackErrorDo) {
      // handled by backend
      this._triggerBrowserResponse(callbackId, null, error);
      return $.resolvedPromise();
    }
    return App.get().errorHandler.analyzeError(error)
      .then(info => {
        const errorDo: BrowserCallbackErrorDo = scout.create(BrowserCallbackErrorDo, {message: info.message, code: info.code});
        this._triggerBrowserResponse(callbackId, null, errorDo);
      });
  }

  protected _triggerBrowserResponse(callbackId: string, data: DoEntity, error?: BrowserCallbackErrorDo) {
    const eventData: BrowserCallbackResponse = {id: callbackId, data, error};
    this.trigger('browserResponse', {data: eventData});
  }
}

@typeName('scout.BrowserCallbackError')
export class BrowserCallbackErrorDo extends BaseDoEntity {
  message: string;
  code: string;
}

export interface BrowserCallbackHandler {
  handle(callbackId: string, owner: Widget, request: DoEntity): JQuery.Promise<DoEntity, BrowserCallbackErrorDo>;
}

export interface BrowserCallbackResponse<TObject extends DoEntity = DoEntity> {
  id: string;
  error?: BrowserCallbackErrorDo;
  data?: TObject | TObject[];
}
