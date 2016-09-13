/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
 scout.CodeType = function() {
  this.id;
  this.codes = [];
  this.codeMap = {};
};

scout.CodeType.prototype.init = function(model) {
  scout.assertParameter('id', model.id);
  this.id = model.id;
  this.modelClass = model.modelClass;

  var i, code;
  if (model.codes) {
    for (i = 0; i < model.codes.length; i++) {
      code = new scout.Code();
      code.init(model.codes[i]);
      this.add(code);
    }
  }
};

scout.CodeType.prototype.add = function(code) {
  this.codes.push(code);
  this.codeMap[code.id] = code;
};

scout.CodeType.prototype.get = function(codeId) {
  var code = this.codeMap[codeId];
  if (!code) {
    throw new Error('No code found for id=' + codeId);
  }
  return code;
};
