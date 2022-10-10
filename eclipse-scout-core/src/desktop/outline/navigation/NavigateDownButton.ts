/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {icons, NavigateButton, Page} from '../../../index';
import $ from 'jquery';

export default class NavigateDownButton extends NavigateButton {

  constructor() {
    super();
    this._defaultIconId = icons.ANGLE_DOWN;
    this._defaultText = 'ui.Continue';
    this.iconId = this._defaultIconId;
    this.keyStroke = 'enter';
    this._detailTableRowsSelectedHandler = this._onDetailTableRowsSelected.bind(this);
    this._outlinePageRowLinkHandler = this._onOutlinePageRowLink.bind(this);
  }

  _init(options) {
    super._init(options);

    if (this.node.detailTable) {
      this.node.detailTable.on('rowsSelected', this._detailTableRowsSelectedHandler);
    }
    this.outline.on('pageRowLink', this._outlinePageRowLinkHandler);
  }

  _destroy() {
    if (this.node.detailTable) {
      this.node.detailTable.off('rowsSelected', this._detailTableRowsSelectedHandler);
    }
    this.outline.off('pageRowLink', this._outlinePageRowLinkHandler);

    super._destroy();
  }

  _render() {
    super._render();
    this.$container.addClass('down');
  }

  _isDetail() {
    // Button is in "detail mode" if there are both detail form and detail table visible and detail form is _not_ hidden.
    return !!(this.node.detailFormVisible && this.node.detailForm &&
      this.node.detailTableVisible && this.node.detailTable && this.node.detailFormVisibleByUi);
  }

  _toggleDetail() {
    return false;
  }

  _buttonEnabled() {
    if (this._isDetail()) {
      return true;
    }
    if (this.node.leaf) {
      return false;
    }

    // when it's not a leaf and not a detail - the button is only enabled when a single row is selected and that row is linked to a page
    let table = this.node.detailTable;
    if (table) {
      return table.selectedRows.length === 1 && !!table.selectedRows[0].page;
    }
    return true;
  }

  _drill() {
    let drillNode;

    if (this.node.detailTable) {
      let row = this.node.detailTable.selectedRow();
      drillNode = this.node.pageForTableRow(row);
    } else {
      drillNode = this.node.childNodes[0];
    }

    if (drillNode) {
      $.log.isDebugEnabled() && $.log.debug('drill down to node ' + drillNode);
      // Collapse other expanded child nodes
      let parentNode = drillNode.parentNode;
      if (parentNode) {
        parentNode.childNodes.forEach(childNode => {
          if (childNode.expanded && childNode !== drillNode) {
            this.outline.collapseNode(childNode, {
              animateExpansion: false
            });
          }
        });
      }

      // Select the target node
      this.outline.selectNodes(drillNode); // this also expands the parent node, if required

      // If the parent node is a table page node, expand the drillNode
      // --> Same logic as in OutlineMediator.mediateTableRowAction()
      if (parentNode && parentNode.nodeType === Page.NodeType.TABLE) {
        this.outline.expandNode(drillNode);
      }
    }
  }

  _onDetailTableRowsSelected(event) {
    this.updateEnabled();
  }

  _onOutlinePageRowLink(event) {
    let table = this.node.detailTable;
    if (table && table.selectedRows.length === 1 && table.selectedRows[0].page === event.page) {
      this.updateEnabled();
    }
  }
}
