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
scout.NavigateDownMenu = function(outline, node) {
  scout.NavigateDownMenu.parent.call(this, outline, node);
  this._defaultIconId = scout.icons.ANGLE_DOWN;
  this._defaultText = 'ui.Continue';
  this.iconId = this._defaultIconId;
  this.keyStroke = 'enter';
};
scout.inherits(scout.NavigateDownMenu, scout.AbstractNavigateMenu);

scout.NavigateDownMenu.prototype._render = function($parent) {
  scout.NavigateDownMenu.parent.prototype._render.call(this, $parent);
  this.$container.addClass('down');
};

scout.NavigateDownMenu.prototype._isDetail = function() {
  // Button is in "detail mode" if there are both detail form and detail table visible and detail form is _not_ hidden.
  return !!(this.node.detailFormVisible && this.node.detailForm &&
    this.node.detailTableVisible && this.node.detailTable && this.node.detailFormVisibleByUi);
};

scout.NavigateDownMenu.prototype._toggleDetail = function() {
  return false;
};

scout.NavigateDownMenu.prototype._buttonEnabled = function() {
  if (this._isDetail()) {
    return true;
  }
  if (this.node.leaf) {
    return false;
  }

  // when it's not a leaf and not a detail - the button is only enabled when a single row is selected
  var table = this.node.detailTable;
  if (table) {
    return table.selectedRows.length === 1;
  } else {
    return true;
  }
};

scout.NavigateDownMenu.prototype._drill = function() {
  var drillNode;

  if (this.node.detailTable) {
    var rows = this.node.detailTable.selectedRows;
    if (rows.length > 0) {
      var row = rows[0];
      drillNode = this.outline.nodesMap[row.nodeId];
    }
  } else {
    drillNode = this.node.childNodes[0];
  }
  if (drillNode) {
    $.log.debug('drill down to node ' + drillNode);
    // Collapse other expanded child nodes
    var parentNode = drillNode.parentNode;
    if (parentNode) {
      parentNode.childNodes.forEach(function(childNode) {
        if (childNode.expanded && childNode !== drillNode) {
          this.outline.collapseNode(childNode, {
            animateExpansion: false
          });
        }
      }.bind(this));
    }

    // Select the target node
    this.outline.selectNodes(drillNode); // this also expands the parent node, if required
    this.outline.handleOutlineContent(true);

    // If the parent node is a table page node, expand the drillNode
    // --> Same logic as in OutlineMediator.mediateTableRowAction()
    if (parentNode && parentNode.nodeType === 'table') {
      this.outline.expandNode(drillNode);
    }
  }
};
