/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, BaseDoEntity, scout, typeName, UiCallbacksEventMap, Widget} from '../../../index';

/**
 * Processes UI callback requests from the client backend.
 */
export class UiCallbacks extends Widget {

  declare eventMap: UiCallbacksEventMap;
  declare self: UiCallbacks;

  onUiCallbackRequest(handlerObjectType: string, callbackId: string, owner: Widget, request: BaseDoEntity) {
    const handler = scout.create(handlerObjectType) as UiCallbackHandler;
    try {
      handler.handle(callbackId, owner, request)
        .then(response => this._triggerUiResponse(callbackId, response, null))
        .catch(error => this._handleCallbackError(callbackId, error));
    } catch (err: any) {
      this._handleCallbackError(callbackId, err);
    }
  }

  protected _handleCallbackError(callbackId: string, error: any): JQuery.Promise<void> {
    if (error?._type === 'scout.UiCallbackError') {
      // handled by backend
      this._triggerUiResponse(callbackId, null, error);
      return $.resolvedPromise();
    }
    return App.get().errorHandler.analyzeError(error)
      .then(info => {
        const errorDo = scout.create(UiCallbackErrorDo, {
          message: info.message,
          code: info.code
        });
        this._triggerUiResponse(callbackId, null, errorDo);
      });
  }

  protected _triggerUiResponse(callbackId: string, data: BaseDoEntity, error: UiCallbackErrorDo) {
    const eventData = scout.create(UiCallbackResponse, {id: callbackId, data, error});
    this.trigger('uiResponse', {data: eventData});
  }
}

@typeName('scout.UiCallbackError')
export class UiCallbackErrorDo extends BaseDoEntity {
  message: string;
  code: string;
}

/**
 * Browser side UI callback. Called as soon as a corresponding UiCallback is sent from the client backend. A new instance is created for each callback call.
 */
export interface UiCallbackHandler {
  /**
   * Called when a UiCallback is sent from the client backend.
   * @param callbackId The ID of the callback (as sent from the client backend).
   * @param owner The owner {@link Widget} the call belongs to.
   * @param request The optional callback request {@link BaseDoEntity} as sent from the client backend. May be null.
   * @returns {JQuery.Promise} holding the resulting {@link BaseDoEntity} in case it is resolved or the {@link UiCallbackErrorDo} in case it is rejected.
   */
  handle(callbackId: string, owner: Widget, request: BaseDoEntity): JQuery.Promise<BaseDoEntity, UiCallbackErrorDo>;
}

@typeName('scout.UiCallbackResponse')
export class UiCallbackResponse<TObject extends BaseDoEntity = BaseDoEntity> extends BaseDoEntity {
  id: string;
  error?: UiCallbackErrorDo;
  data?: TObject;
}
