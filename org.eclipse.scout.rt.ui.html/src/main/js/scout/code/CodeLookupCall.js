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
scout.CodeLookupCall = function() {
  scout.CodeLookupCall.parent.call(this);
  this.codeType;
};
scout.inherits(scout.CodeLookupCall, scout.LookupCall);

scout.CodeLookupCall.prototype._init = function(model) {
  scout.assertParameter('session', model.session);
  scout.CodeLookupCall.parent.prototype._init.call(this, model);
};

scout.CodeLookupCall.prototype._textByKey = function(key) {
  var code = scout.codes.optGet(this.codeType, key);
  return $.resolvedDeferred(code ? code.text(this.session.locale) : this._textCodeUndefined(key));
};

scout.CodeLookupCall.prototype._textCodeUndefined = function(id) {
  return this.session.text('ui.CodeUndefined');
};
