/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, BrowserCallbackErrorDo, BrowserCallbackHandler, Desktop, DoEntity, scout, typeName} from '../index';

export class GeoLocationBrowserHandler implements BrowserCallbackHandler {
  handle(callbackId: string, owner: Desktop, request: DoEntity): JQuery.Promise<GeoLocationResponseDo> {
    if (!navigator.geolocation) {
      return $.resolvedPromise(); // no result will let the callback fail.
    }

    const deferred = $.Deferred();
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

  protected _positionError(positionError: GeolocationPositionError): GeoLocationResponseDo {
    return scout.create(BrowserCallbackErrorDo, {
      message: positionError.message,
      code: '' + positionError.code
    });
  }
}

@typeName('scout.GeoLocationResponse')
export class GeoLocationResponseDo extends BaseDoEntity {
  constructor(model: Partial<GeoLocationResponseDo>) {
    super();
    Object.assign(this, model);
  }

  latitude?: string;
  longitude?: string;
}
