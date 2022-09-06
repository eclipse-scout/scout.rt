/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {objects, QueryBy, strings, ValueFieldAdapter} from '../../../index';

/**
 * Use this base class for field-adapters that work with lookup-calls like SmartField and TagField.
 */
export default class LookupFieldAdapter extends ValueFieldAdapter {

  constructor() {
    super();
  }

  /**
   * @param {QueryBy} queryBy
   * @param {object} [queryData] optional data (text, key, rec)
   */
  sendLookup(queryBy, queryData) {
    let propertyName = queryBy.toLowerCase(),
      requestType = 'lookupBy' + strings.toUpperCaseFirstLetter(propertyName),
      requestData = {
        showBusyIndicator: false
      };
    if (!objects.isNullOrUndefined(queryData)) {
      requestData[propertyName] = queryData;
    }
    this._send(requestType, requestData);
  }
}
