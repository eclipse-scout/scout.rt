/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, scout, typeName, UiCallbackErrorDo, UiCallbackHandler, UiCallbackParam} from '../index';

export class GeoLocationUiCallbackHandler implements UiCallbackHandler {

  handle(param: UiCallbackParam): JQuery.Promise<any> {
    if (!navigator.geolocation) {
      return $.rejectedPromise('Geolocation API not supported');
    }
    let deferred = $.Deferred();
    navigator.geolocation.getCurrentPosition(
      position => deferred.resolve(this._positionSuccess(position)),
      positionError => deferred.reject(this._positionError(positionError)));
    return deferred.promise();
  }

  protected _positionSuccess(position: GeolocationPosition): GeoLocationResponseDo {
    return scout.create(GeoLocationResponseDo, {
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
export class GeoLocationResponseDo extends BaseDoEntity {
  latitude?: string;
  longitude?: string;
}
