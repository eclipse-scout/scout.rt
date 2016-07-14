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
scout.PageWithNodes = function() {
  scout.PageWithNodes.parent.call(this);
  this.nodeType = "nodes";
};
scout.inherits(scout.PageWithNodes, scout.Page);

/**
 * @override Page.js
 */
scout.PageWithNodes.prototype._createTable = function() {
  var nodeColumn = scout.create('Column', {
    index: 0,
    id: 'NodeColumn'
  });
  return scout.create('Table', {
    parent: this.parent,
    id: 'PageWithNodesTable',
    autoResizeColumns: true,
    columns: [nodeColumn]
  });
};

/**
 * @override Page.js
 */
scout.PageWithNodes.prototype._loadTableData = function() {
  var i = 0, rows = [];
  this.childNodes.forEach(function(node) {
    rows.push({id: i.toString(), cells: [node.text]});
    i++;
  });
  return rows;
};

/**
 * @override TreeNode.js
 */
scout.PageWithNodes.prototype.loadChildren = function() {
  // FIXME 6.1 [awe] - remove this hack
  this.getTree()._onPageChanged2(this);
};
