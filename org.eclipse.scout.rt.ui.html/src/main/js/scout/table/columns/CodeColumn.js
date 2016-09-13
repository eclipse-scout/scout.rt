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
scout.CodeColumn = function() { // FIXME [awe] 6.1 - discuss with C.GU - rename to SmartColumn (as in Java)?
  scout.CodeColumn.parent.call(this);
  this.codeType;
  this.uiSortPossible = true;
  this.comparator = scout.comparators.NUMERIC;
};
scout.inherits(scout.CodeColumn, scout.Column);

/**
 * @override Columns.js
 */
scout.CodeColumn.prototype._createCellModel = function(codeId) {
  var code = scout.codes.get(this.codeType, codeId);
  return {
    text: code.text(this.session.locale),
    value: code
  };
};
