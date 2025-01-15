/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, Desktop, scout, typeName, UiCallbackErrorDo, UiCallbackHandler} from '../index';

export class GeoLocationUiHandler implements UiCallbackHandler {
  handle(callbackId: string, owner: Desktop, request: BaseDoEntity): JQuery.Promise<GeoLocationResponse> {
    if (!navigator.geolocation) {
      return $.resolvedPromise(); // no result will let the callback fail.
    }

    const deferred = $.Deferred();
    navigator.geolocation.getCurrentPosition(
      position => deferred.resolve(this._positionSuccess(position)),
      positionError => deferred.reject(this._positionError(positionError)));
    return deferred.promise();
  }

  protected _positionSuccess(position: GeolocationPosition): GeoLocationResponse {
    return scout.create(GeoLocationResponse, {
      latitude: '' + position.coords.latitude,
      longitude: '' + position.coords.longitude
    });
  }

  protected _positionError(positionError: GeolocationPositionError): UiCallbackErrorDo {
    return scout.create(UiCallbackErrorDo, {
      message: positionError.message,
      code: '' + positionError.code
    });
  }
}

@typeName('scout.GeoLocationResponse')
export class GeoLocationResponse extends BaseDoEntity {
  latitude?: string;
  longitude?: string;
}
