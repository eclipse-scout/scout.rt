/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.CodeLookupCall = function() {
  scout.CodeLookupCall.parent.call(this);
  this.codeType;
};
scout.inherits(scout.CodeLookupCall, scout.StaticLookupCall);

scout.CodeLookupCall.prototype._data = function() {
  var codeType = scout.codes.codeType(this.codeType, true);
  if (codeType) {
    return codeType.codes
      .map(function(code) {
        return [code.id, code.text(this.session.locale)];
      }.bind(this));
  }
  return [];
};
