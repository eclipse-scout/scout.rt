/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, DoEntity, scout, UiCallbacksEventMap, Widget} from '../../../index';

/**
 * Processes UI callback requests from the client backend.
 */
export class UiCallbacks extends Widget {

  declare eventMap: UiCallbacksEventMap;
  declare self: UiCallbacks;

  onUiCallbackRequest(handlerObjectType: string, callbackId: string, owner: Widget, request: DoEntity) {
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
        const errorDo: UiCallbackErrorDo = {
          _type: 'scout.UiCallbackError',
          message: info.message,
          code: info.code
        };
        this._triggerUiResponse(callbackId, null, errorDo);
      });
  }

  protected _triggerUiResponse(callbackId: string, data: DoEntity, error: UiCallbackErrorDo) {
    const eventData: UiCallbackResponse = {id: callbackId, data, error};
    this.trigger('uiResponse', {data: eventData});
  }
}

export interface UiCallbackErrorDo extends DoEntity {
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
   * @param request The optional callback request data as sent from the client backend. May be null.
   * @returns {JQuery.Promise} holding the resulting {@link DoEntity} in case it is resolved or the {@link UiCallbackErrorDo} in case it is rejected.
   */
  handle(callbackId: string, owner: Widget, request: DoEntity): JQuery.Promise<DoEntity, UiCallbackErrorDo>;
}

export interface UiCallbackResponse<TObject extends DoEntity = DoEntity> {
  id: string;
  error?: UiCallbackErrorDo;
  data?: TObject | TObject[];
}
