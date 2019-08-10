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

/**
 * Use this base class for field-adapters that work with lookup-calls like SmartField and TagField.
 */
scout.LookupFieldAdapter = function() {
  scout.LookupFieldAdapter.parent.call(this);
};
scout.inherits(scout.LookupFieldAdapter, scout.ValueFieldAdapter);

/**
 * @param {scout.QueryBy} queryBy
 * @param {object} [queryData] optional data (text, key, rec)
 */
scout.LookupFieldAdapter.prototype.sendLookup = function(queryBy, queryData) {
  var propertyName = queryBy.toLowerCase(),
    requestType = 'lookupBy' + scout.strings.toUpperCaseFirstLetter(propertyName),
    requestData = {
      showBusyIndicator: false
    };
  if (!scout.objects.isNullOrUndefined(queryData)) {
    requestData[propertyName] = queryData;
  }
  this._send(requestType, requestData);
};
