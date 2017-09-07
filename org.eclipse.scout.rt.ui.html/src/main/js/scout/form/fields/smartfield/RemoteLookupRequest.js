/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/

/**
 * This class is used to remember what lookup is called (=request) and to compare that
 * request with the result. Each result from the Scout server contains the lookup parameters
 * stored in this class so we can easily compare the parameters from the latest request
 * with the parameters from the result. If the parameters don't match, we simply ignore the
 * result, because it is out-dated.
 */
scout.RemoteLookupRequest = function(requestType, requestData) {
  this.requestType = requestType;
  this.requestData = requestData;
};

scout.RemoteLookupRequest.prototype.equals = function(o) {
  if (!o || !(o instanceof scout.RemoteLookupRequest)) {
    return false;
  }
  return scout.objects.propertiesEquals(this, o, ['requestType', 'requestData']);
};

scout.RemoteLookupRequest.byText = function(searchText) {
  return new scout.RemoteLookupRequest('byText', scout.nvl(searchText, ''));
};

scout.RemoteLookupRequest.byRec = function(rec) {
  return new scout.RemoteLookupRequest('byRec', rec);
};
