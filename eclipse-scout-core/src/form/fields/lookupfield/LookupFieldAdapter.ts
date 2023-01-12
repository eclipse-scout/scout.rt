/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {objects, QueryBy, strings, ValueFieldAdapter} from '../../../index';

/**
 * Use this base class for field-adapters that work with lookup-calls like SmartField and TagField.
 */
export class LookupFieldAdapter extends ValueFieldAdapter {

  /**
   * @param queryData optional data (text, key, rec)
   */
  sendLookup(queryBy: QueryBy, queryData?: any) {
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
