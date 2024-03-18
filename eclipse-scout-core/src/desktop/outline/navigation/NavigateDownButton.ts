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
    return !!this._getDrillNode();
  }

  protected _getDrillNode(): Page {
    if (this.node.leaf) {
      return null;
    }

    // If there is a detail table that defines the child pages, check if a row is selected.
    if (this._hasDetailTable(false)) {
      let selectedRows = this.node.detailTable.selectedRows;
      // Drill down is only possible when a single row is selected and that row is linked to a page
      if (selectedRows.length === 1 && selectedRows[0].page) {
        return this.node.pageForTableRow(selectedRows[0]);
      }
      return null;
    }

    // If the detail table is not available, the user cannot select a row, thus we default to the first child node.
    // Further child nodes cannot be reached by this, this is considered to be a configuration error.
    return this.node.childNodes[0];
  }

  protected _drill() {
    let drillNode = this._getDrillNode();
    if (drillNode) {
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

      this.outline.drillDown(drillNode);
    }
  }

  // Notes about event listeners:
  // - There are up to two instances for each navigate button per page: one for the detail form, one for the detail table.
  //   They only _look_ like the same button because they occupy the same space.
  // - Listeners are attached when buttons are created, which is the case when the page is first activated.
  // - Listeners remain attached until the page is destroyed. Deselecting the page does not change the listeners.
  // - Buttons only need to be updated when "their" rows/nodes change. This is naturally the case for detail table events.
  //   Outline listeners on the other hand will be triggered for _any_ change in the tree. To prevent too many or unnecessary
  //   updates, some additional filters are applied in the event handlers.

  protected _onDetailTableRowsSelected(event: TableRowsSelectedEvent) {
    this.updateEnabled();
  }

  protected _onDetailTableRowsChanged(event: TableRowsInsertedEvent | TableRowsDeletedEvent | TableAllRowsDeletedEvent) {
    this.updateEnabled();
  }

  protected _onOutlinePageRowLink(event: OutlinePageRowLinkEvent) {
    // This listener is needed because linking a row with a page can happen independent of other events,
    // e.g. OutlineAdapter#_linkNodeWithRowLater. However, we only need to update the button when exactly
    // one row is selected and that row is referenced in the event. In all other cases the availability
    // of the button would not change anyway (see logic in _getDrillNode).
    if (this._hasDetailTable(false)) {
      let selectedRows = this.node.detailTable.selectedRows;
      if (selectedRows.length === 1 && selectedRows[0] === event.row) {
        this.updateEnabled();
      }
    }
  }

  protected _onOutlinePageChanged(event: OutlinePageChangedEvent) {
    if (event.page === this.node) {
      this.updateEnabled();
    }
  }
}
