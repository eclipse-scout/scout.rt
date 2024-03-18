/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {EventHandler, icons, InitModelOf, NavigateButton, OutlinePageChangedEvent, OutlinePageRowLinkEvent, Page, TableAllRowsDeletedEvent, TableRowsDeletedEvent, TableRowsInsertedEvent, TableRowsSelectedEvent} from '../../../index';
import $ from 'jquery';

export class NavigateDownButton extends NavigateButton {
  protected _detailTableRowsSelectedHandler: EventHandler<TableRowsSelectedEvent>;
  protected _detailTableRowsChangedHandler: EventHandler<TableRowsInsertedEvent | TableRowsDeletedEvent | TableAllRowsDeletedEvent>;
  protected _outlinePageRowLinkHandler: EventHandler<OutlinePageRowLinkEvent>;
  protected _outlinePageChangedHandler: EventHandler<OutlinePageChangedEvent>;

  constructor() {
    super();
    this._defaultIconId = icons.ANGLE_DOWN;
    this._defaultText = 'ui.Continue';
    this.iconId = this._defaultIconId;
    this.keyStroke = 'enter';
    this._detailTableRowsSelectedHandler = this._onDetailTableRowsSelected.bind(this);
    this._detailTableRowsChangedHandler = this._onDetailTableRowsChanged.bind(this);
    this._outlinePageRowLinkHandler = this._onOutlinePageRowLink.bind(this);
    this._outlinePageChangedHandler = this._onOutlinePageChanged.bind(this);
  }

  protected override _init(options: InitModelOf<this>) {
    super._init(options);

    if (this.node.detailTable) {
      this.node.detailTable.on('rowsSelected', this._detailTableRowsSelectedHandler);
      this.node.detailTable.on('rowsInserted rowsDeleted allRowsDeleted', this._detailTableRowsChangedHandler);
    }
    this.outline.on('pageRowLink', this._outlinePageRowLinkHandler);
    this.outline.on('pageChanged', this._outlinePageChangedHandler);
  }

  protected override _destroy() {
    if (this.node.detailTable) {
      this.node.detailTable.off('rowsSelected', this._detailTableRowsSelectedHandler);
      this.node.detailTable.off('rowsInserted rowsDeleted allRowsDeleted', this._detailTableRowsChangedHandler);
    }
    this.outline.off('pageRowLink', this._outlinePageRowLinkHandler);
    this.outline.off('pageChanged', this._outlinePageChangedHandler);

    super._destroy();
  }

  protected override _render() {
    super._render();
    this.$container.addClass('down');
  }

  protected _isDetail(): boolean {
    // Button is in "detail mode" if there are both detail form and detail table visible and detail form is _not_ hidden.
    return this._hasDetailForm() && this._hasDetailTable() && this.node.detailFormVisibleByUi;
  }

  protected _toggleDetail(): boolean {
    return false;
  }

  protected _buttonEnabled(): boolean {
    if (this._isDetail()) {
      return true;
    }
    if (this.node.leaf) {
      return false;
    }
    if (!this._hasDetailTable()) {
      return false;
    }
    return !!this._getDrillNode();
  }

  protected _getDrillNode(): Page {
    if (this._hasDetailTable()) {
      let selectedRow = this.node.detailTable.selectedRow();
      // the button is only enabled when a single row is selected and that row is linked to a page
      if (selectedRow && selectedRow.page && this.node.detailTable.selectedRows.length === 1) {
        return this.node.pageForTableRow(selectedRow);
      }
      return null;
    }
    return this.node.childNodes[0];
  }

  protected _drill() {
    let drillNode = this._getDrillNode();

    // Note: similar 'drill-down' logic exists at other places [#9xw34fQWo2]
    if (drillNode) {
      $.log.isDebugEnabled() && $.log.debug('drill down to node ' + drillNode);

      // Collapse other expanded child nodes
      let parentNode = drillNode.parentNode;
      if (parentNode) {
        parentNode.childNodes.forEach(childNode => {
          if (childNode.expanded && childNode !== drillNode) {
            this.outline.collapseNode(childNode, {
              renderAnimated: false
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

  protected _onDetailTableRowsSelected(event: TableRowsSelectedEvent) {
    this.updateEnabled();
  }

  protected _onDetailTableRowsChanged(event: TableRowsInsertedEvent | TableRowsDeletedEvent | TableAllRowsDeletedEvent) {
    this.updateEnabled();
  }

  protected _onOutlinePageRowLink(event: OutlinePageRowLinkEvent) {
    this.updateEnabled();
  }

  protected _onOutlinePageChanged(event: OutlinePageChangedEvent) {
    this.updateEnabled();
  }
}
