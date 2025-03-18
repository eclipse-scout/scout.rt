/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, BaseDoEntity, HybridActionContextElements, InitModelOf, JsonValue, ObjectWithType, scout, typeName, UiCallbacksEventMap, Widget} from '../../../index';
import $ from 'jquery';

/**
 * Processes UI callback requests from the client backend.
 */
export class UiCallbacks extends Widget {
  declare eventMap: UiCallbacksEventMap;
  declare self: UiCallbacks;

  onCallback(handlerObjectType: string, callbackId: string, owner: Widget, data: BaseDoEntity, contextElements: HybridActionContextElements) {
    $.resolvedPromise() // always start new promise chain so we only need one catch handler
      .then(() => {
        const handler = scout.create(handlerObjectType) as UiCallbackHandler;
        return handler.handle({callbackId, owner, data, contextElements});
      })
      .then(result => this._convertResult(result))
      .then(result => this._triggerCallbackEnd(callbackId, result, null))
      .catch(error => this._convertError(error)
        .then(error => this._triggerCallbackEnd(callbackId, null, error)));
  }

  protected _convertResult(result: any): UiCallbackResult {
    if (result instanceof UiCallbackResult) {
      return result;
    }
    return scout.create(UiCallbackResult, {
      data: result
    });
  }

  protected _convertError(error: any): JQuery.Promise<UiCallbackErrorDo> {
    if (error instanceof UiCallbackErrorDo) {
      return $.resolvedPromise(error);
    }
    return App.get().errorHandler.analyzeError(error)
      .then(info => {
        return scout.create(UiCallbackErrorDo, {
          message: info.message,
          code: info.code
        });
      });
  }

  protected _triggerCallbackEnd(callbackId: string, result: UiCallbackResult, error: UiCallbackErrorDo) {
    this.trigger('callbackEnd', {callbackId, result, error});
  }
}

/**
 * Represents the result of a successful UI callback.
 */
export class UiCallbackResult implements ObjectWithType, UiCallbackResultModel {
  declare model: UiCallbackResultModel;
  objectType: string;

  data: BaseDoEntity | any;
  contextElements: HybridActionContextElements;

  init(model: InitModelOf<this>) {
    $.extend(this, model);
  }
}

export interface UiCallbackResultModel {
  /**
   * Optional data to send back to the server. Can be either a {@link BaseDoEntity} or a {@link JsonValue}.
   */
  data?: BaseDoEntity | any;
  /**
   * Optional {@link HybridActionContextElements context elements} to send back to the server along with the {@link #data}.
   */
  contextElements?: HybridActionContextElements;
}

/**
 * Represents the result of an unsuccessful UI callback.
 */
@typeName('scout.UiCallbackError')
export class UiCallbackErrorDo extends BaseDoEntity {
  message: string;
  code: string;
}

/**
 * Browser-side UI callback handler. Called when a corresponding `callback` event is received from the server.
 * A new instance is created for each callback call.
 */
export interface UiCallbackHandler {

  /**
   * Called when a UI callback is requested from the server.
   *
   * The callback is answered with a `callbackEnd` event when the returned promise is resolved or rejected.
   *
   * The promise can be **resolved** with a {@link UiCallbackResult} or a {@link BaseDoEntity}. The latter will
   * automatically be converted to a {@link UiCallbackResult} and is provided as a convenience for simple callback
   * handlers that don't require {@link HybridActionContextElements context elements}.
   *
   * The promise can be **rejected** with a {@link UiCallbackErrorDo}. Anything else (including `string` and
   * {@link Error}) will automatically be converted to a {@link UiCallbackErrorDo} using the application's
   * {@link ErrorHandler}.
   */
  handle(param: UiCallbackParam): JQuery.Promise<any>;
}

/**
 * Represents the callback arguments sent from the server.
 */
export interface UiCallbackParam {
  /** The ID of the callback (as sent from the server). */
  callbackId: string;
  /** The owner {@link Widget} the call belongs to. */
  owner: Widget;
  /** The optional {@link BaseDoEntity} sent from the server. May be `null`. */
  data?: BaseDoEntity;
  /** The optional context elements sent from the server. May be `null`. */
  contextElements?: HybridActionContextElements;
}
