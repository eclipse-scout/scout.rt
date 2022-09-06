/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {objects, QueryBy} from '../index';

/**
 * This class is used to remember what lookup is called (=request) and to compare that
 * request with the result. Each result from the Scout server contains the lookup parameters
 * stored in this class so we can easily compare the parameters from the latest request
 * with the parameters from the result. If the parameters don't match, we simply ignore the
 * result, because it is out-dated.
 *
 * @param {QueryBy} requestType
 * @param {object} requestData
 */
export default class RemoteLookupRequest {

  constructor(requestType, requestData) {
    if (!QueryBy.hasOwnProperty(requestType)) {
      throw new Error('Invalid enum value');
    }
    this.requestType = requestType;
    this.requestData = requestData;
  }

  equals(o) {
    if (!o || !(o instanceof RemoteLookupRequest)) {
      return false;
    }
    return objects.propertiesEquals(this, o, ['requestType', 'requestData']);
  }
}
