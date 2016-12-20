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

  if (model.codes) {
    for (var i = 0; i < model.codes.length; i++) {
      this._initCode(model.codes[i]);
    }
  }
};

scout.CodeType.prototype._initCode = function(modelCode, parent) {
  var code = scout.create(modelCode);
  this.add(code, parent);
  if (modelCode.children) {
    for (var i = 0; i < modelCode.children.length; i++) {
      this._initCode(modelCode.children[i], code);
    }
  }
};

scout.CodeType.prototype.add = function(code, parent) {
  this.codes.push(code);
  this.codeMap[code.id] = code;
  if (parent) {
    parent.children.push(code);
    code.parent = parent;
  }
};

scout.CodeType.prototype.get = function(codeId) {
  var code = this.codeMap[codeId];
  if (!code) {
    throw new Error('No code found for id=' + codeId);
  }
  return code;
};

scout.CodeType.prototype.getCodes = function(rootOnly) {
  if (rootOnly) {
    var rootCodes = [];
    for (var i = 0; i < this.codes.length; i++) {
      if (!this.codes[i].parent) {
        rootCodes.push(this.codes[i]);
      }
    }
    return rootCodes;
  } else {
    return this.codes;
  }
};

scout.CodeType.ensure = function(codeType) {
  if (!codeType) {
    return codeType;
  }
  if (codeType instanceof scout.CodeType) {
    return codeType;
  }
  return scout.create('CodeType', codeType);
};
