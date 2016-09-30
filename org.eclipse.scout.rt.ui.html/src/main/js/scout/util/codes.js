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
scout.codes = {

  registry: {},

  bootstrap: function() {
    return $.ajaxJson('res/codes.json')
      .done(this.init.bind(this));
  },

  init: function(data) {
    data = data || {};
    Object.keys(data).forEach(function(codeTypeId) {
      var codeType = new scout.CodeType();
      codeType.init(data[codeTypeId]);
      this.registry[codeType.id] = codeType;
    }, this);
  },

  /**
   * Returns a code for the given codeId. The codeId is a string in the following format:
   *
   * "[CodeType.id] [Code.id]"
   *
   * Examples:
   * "71074 104860"
   * "MessageChannel Phone"
   *
   * CodeType.id and Code.id are separated by a space.
   * The Code.id alone is not unique, that's why the CodeType.id must be always provided.
   *
   * You can also call this function with two arguments. In that case the first argument
   * is the codeTypeId and the second is the codeId.
   */
  get: function(vararg, codeId) {
    var codeTypeId;
    if (arguments.length === 2) {
      codeTypeId = vararg;
    } else {
      var tmp = vararg.split(' ');
      if (tmp.length !== 2) {
        throw new Error('Invalid string. Must have format "[CodeType.id] [Code.id]"');
      }
      codeTypeId = tmp[0];
      codeId = tmp[1];
    }
    scout.assertParameter('codeTypeId', codeTypeId);
    scout.assertParameter('codeId', codeId);
    return this.codeType(codeTypeId).get(codeId);
  },

  codeType: function(codeTypeId) {
    var codeType = this.registry[codeTypeId];
    if (!codeType) {
      throw new Error('No CodeType found for id=' + codeTypeId);
    }
    return codeType;
  }

};
