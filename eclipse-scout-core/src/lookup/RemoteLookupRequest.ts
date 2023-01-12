/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {objects, QueryBy} from '../index';

/**
 * This class is used to remember what lookup is called (=request) and to compare that
 * request with the result. Each result from the Scout server contains the lookup parameters
 * stored in this class so we can easily compare the parameters from the latest request
 * with the parameters from the result. If the parameters don't match, we simply ignore the
 * result, because it is out-dated.
 */
export class RemoteLookupRequest<TData> {
  requestType: QueryBy;
  requestData: TData;

  constructor(requestType: QueryBy, requestData?: TData) {
    if (!QueryBy.hasOwnProperty(requestType)) {
      throw new Error('Invalid enum value');
    }
    this.requestType = requestType;
    this.requestData = requestData;
  }

  equals(o: any): boolean {
    if (!o || !(o instanceof RemoteLookupRequest)) {
      return false;
    }
    return objects.propertiesEquals(this, o, ['requestType', 'requestData']);
  }
}
