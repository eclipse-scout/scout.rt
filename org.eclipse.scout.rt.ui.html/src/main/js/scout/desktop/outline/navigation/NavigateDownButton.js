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
scout.NavigateDownButton = function() {
  scout.NavigateDownButton.parent.call(this);
  this._defaultIconId = scout.icons.ANGLE_DOWN;
  this._defaultText = 'ui.Continue';
  this.iconId = this._defaultIconId;
  this.keyStroke = 'enter';
  this._detailTableRowsSelectedHandler = this._onDetailTableRowsSelected.bind(this);
  this._outlinePageRowLinkHandler = this._onOutlinePageRowLink.bind(this);
};
scout.inherits(scout.NavigateDownButton, scout.NavigateButton);

scout.NavigateDownButton.prototype._init = function(options) {
  scout.NavigateDownButton.parent.prototype._init.call(this, options);

  if (this.node.detailTable) {
    this.node.detailTable.on('rowsSelected', this._detailTableRowsSelectedHandler);
  }
  this.outline.on('pageRowLink', this._outlinePageRowLinkHandler);
};

scout.NavigateDownButton.prototype._destroy = function() {
  if (this.node.detailTable) {
    this.node.detailTable.off('rowsSelected', this._detailTableRowsSelectedHandler);
  }
  this.outline.off('pageRowLink', this._outlinePageRowLinkHandler);

  scout.NavigateDownButton.parent.prototype._destroy.call(this);
};

scout.NavigateDownButton.prototype._render = function() {
  scout.NavigateDownButton.parent.prototype._render.call(this);
  this.$container.addClass('down');
};

scout.NavigateDownButton.prototype._isDetail = function() {
  // Button is in "detail mode" if there are both detail form and detail table visible and detail form is _not_ hidden.
  return !!(this.node.detailFormVisible && this.node.detailForm &&
    this.node.detailTableVisible && this.node.detailTable && this.node.detailFormVisibleByUi);
};

scout.NavigateDownButton.prototype._toggleDetail = function() {
  return false;
};

scout.NavigateDownButton.prototype._buttonEnabled = function() {
  if (this._isDetail()) {
    return true;
  }
  if (this.node.leaf) {
    return false;
  }

  // when it's not a leaf and not a detail - the button is only enabled when a single row is selected and that row is linked to a page
  var table = this.node.detailTable;
  if (table) {
    return table.selectedRows.length === 1 && !!table.selectedRows[0].page;
  }
  return true;
};

scout.NavigateDownButton.prototype._drill = function() {
  var drillNode;

  if (this.node.detailTable) {
    var row = this.node.detailTable.selectedRow();
    drillNode = this.node.pageForTableRow(row);
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

    // If the parent node is a table page node, expand the drillNode
    // --> Same logic as in OutlineMediator.mediateTableRowAction()
    if (parentNode && parentNode.nodeType === scout.Page.NodeType.TABLE) {
      this.outline.expandNode(drillNode);
    }
  }
};

scout.NavigateDownButton.prototype._onDetailTableRowsSelected = function(event) {
  this.updateEnabled();
};

scout.NavigateDownButton.prototype._onOutlinePageRowLink = function(event) {
  var table = this.node.detailTable;
  if (table && table.selectedRows.length === 1 && table.selectedRows[0].page === event.page) {
    this.updateEnabled();
  }
};
