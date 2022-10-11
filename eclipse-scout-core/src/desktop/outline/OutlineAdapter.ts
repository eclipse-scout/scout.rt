/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {App, EventHandler, Form, objects, Outline, Page, scout, Table, TableRow, TreeAdapter} from '../../index';
import {TableFilterRemovedEvent, TableRowInitEvent, TableRowsInsertedEvent} from '../../table/TableEventMap';

export default class OutlineAdapter<TWidget extends Outline = Outline> extends TreeAdapter<TWidget> {

  protected _filterDirty: boolean;
  protected _nodeIdToRowMap: Record<string, TableRow>;
  protected _detailTableRowInitHandler: EventHandler<TableRowInitEvent>;
  protected _detailTableRowsInsertedHandler: EventHandler<TableRowsInsertedEvent>;
  protected _detailTableFilterRemoved: EventHandler<TableFilterRemovedEvent>;

  constructor() {
    super();
    this._filterDirty = false;
    this._nodeIdToRowMap = {};
    this._detailTableRowInitHandler = this._onDetailTableRowInit.bind(this);
    this._detailTableRowsInsertedHandler = this._onDetailTableRowsInserted.bind(this);
    this._detailTableFilterRemoved = this._onDetailTableFilterRemoved.bind(this);
  }

  protected _onPageChanged(event: any) {
    // @ts-ignore
    let page = this.widget._nodeById(event.nodeId) as Page;
    page.overviewIconId = event.overviewIconId;

    page.detailFormVisible = event.detailFormVisible;
    let detailForm = this.session.getOrCreateWidget(event.detailForm, this.widget) as Form;
    page.setDetailForm(detailForm);

    page.navigateButtonsVisible = event.navigateButtonsVisible;
    page.detailTableVisible = event.detailTableVisible;
    let detailTable = this.session.getOrCreateWidget(event.detailTable, this.widget) as Table;
    if (page.detailTable !== detailTable) {
      if (page.detailTable) {
        this._destroyDetailTable(page);
      }
      page.setDetailTable(detailTable);
      if (page.detailTable) {
        this._initDetailTable(page);
      }
    }

    this.widget.pageChanged(page);
  }

  override onModelAction(event: any) {
    if (event.type === 'pageChanged') {
      this._onPageChanged(event);
    } else {
      super.onModelAction(event);
    }
  }

  protected _initDetailTable(page: Page) {
    // link already existing rows now
    page.detailTable.rows.forEach(this._linkNodeWithRow.bind(this));
    // rows which are inserted later are linked by _onDetailTableRowInit
    page.detailTable.on('rowInit', this._detailTableRowInitHandler);
    page.detailTable.on('rowsInserted', this._detailTableRowsInsertedHandler);
    page.detailTable.on('filterRemoved', this._detailTableFilterRemoved);
  }

  protected _destroyDetailTable(page: Page) {
    this._nodeIdToRowMap = {};
    page.detailTable.rows.forEach(this._unlinkNodeWithRow.bind(this));
    page.detailTable.off('rowInit', this._detailTableRowInitHandler);
    page.detailTable.off('rowsInserted', this._detailTableRowsInsertedHandler);
    page.detailTable.off('filterRemoved', this._detailTableFilterRemoved);
  }

  protected _linkNodeWithRow(row: TableRow) {
    scout.assertParameter('row', row);
    let nodeId = row.nodeId;

    if (nodeId === undefined) {
      // nodeId is undefined if no node exists for that row (e.g. happens if the page containing the row is a leaf page)
      return;
    }

    let node = this.widget.nodesMap[nodeId] as Page;
    if (node) {
      node.linkWithRow(row);
    } else {
      // Prepare for linking later because node has not been inserted yet
      // see: #_linkNodeWithRowLater
      this._nodeIdToRowMap[nodeId] = row;
    }
  }

  protected _unlinkNodeWithRow(row: TableRow) {
    let node = this.widget.nodesMap[row.nodeId] as Page;
    if (node) {
      node.unlinkWithRow(row);
    }
  }

  protected _onDetailTableRowInit(event: TableRowInitEvent) {
    this._linkNodeWithRow(event.row);

    let node = this.widget.nodesMap[event.row.nodeId];
    if (this.widget.isSelectedNode(node) && !this.widget.detailContent) {
      // Table row detail could not be created because the link from page to row was missing at the time the node got selected -> do it now
      this.widget.updateDetailContent();
    }
  }

  protected _onDetailTableRowsInserted(event: TableRowsInsertedEvent) {
    let table = event.source;

    if (this._filterDirty ||
      // @ts-ignore
      (table._filterCount() > 0 && event.rows.some(row => !row.filterAccepted))) {
      this._filterDirty = false;
      // Explicitly call filter if some of the new rows are not accepted.
      // If they are accepted, table.insertRows() will trigger a filter event by itself that will be mediated to the outline by OutlineMediator.js
      this.widget.filter();
    }
  }

  protected _onDetailTableFilterRemoved(event: TableFilterRemovedEvent) {
    let table = event.source;
    // @ts-ignore
    if (table.modelAdapter && table.modelAdapter._rebuildingTable) {
      // If a column is removed, the tableAdapter prevents filtering because the flag _rebuildingTable is true
      // -> the outline does not get informed, hence the nodes stay invisible.
      this._filterDirty = true;
    }
  }

  /**
   * Link node with row, if it hasn't been linked yet.
   */
  protected _linkNodeWithRowLater(page: Page) {
    if (!page.parentNode || !page.parentNode.detailTable) {
      return;
    }
    if (!this._nodeIdToRowMap.hasOwnProperty(page.id)) {
      return;
    }
    let row = this._nodeIdToRowMap[page.id];
    page.linkWithRow(row);
    delete this._nodeIdToRowMap[page.id];
  }

  protected override _getDefaultNodeObjectType() {
    return 'Page';
  }

  /**
   * Static method to modify the prototype of Outline.
   */
  static modifyOutlinePrototype() {
    if (!App.get().remote) {
      return;
    }

    objects.replacePrototypeFunction(Outline, '_computeDetailContent', OutlineAdapter._computeDetailContentRemote, true);
    objects.replacePrototypeFunction(Outline, 'updateDetailMenus', OutlineAdapter.updateDetailMenusRemote, true);
    objects.replacePrototypeFunction(Outline, '_initTreeNodeInternal', OutlineAdapter._initTreeNodeInternalRemote, true);
    objects.replacePrototypeFunction(Outline, '_createTreeNode', OutlineAdapter._createTreeNodeRemote, true);
  }

  /**
   * Replacement for Outline#_computeDetailContent(). 'This' points to the outline.
   */
  protected static _computeDetailContentRemote() {
    // @ts-ignore
    if (!this.modelAdapter) {
      // @ts-ignore
      return this._computeDetailContentOrig();
    }

    // @ts-ignore
    let selectedPage = this.selectedNode();
    if (!selectedPage) {
      // Detail content is shown for the selected node only
      return null;
    }

    // if there is a detail form, use this
    if (selectedPage.detailForm || selectedPage.detailFormResolved) {
      // If there is a detail form -> return (and set flag to true to make updateDetailMenusRemote work)
      selectedPage.detailFormResolved = true;
      // @ts-ignore
      return this._computeDetailContentOrig();
    }

    // It is not known yet whether there is a detail form -> wait for the requests to be processed before showing the table row detail
    // @ts-ignore
    if (!this.session.areRequestsPending() && !this.session.areEventsQueued()) {
      // There are no requests pending -> return (and set flag to true to make updateDetailMenusRemote work)
      selectedPage.detailFormResolved = true;
      // @ts-ignore
      return this._computeDetailContentOrig();
    }

    // Wait for the requests to complete
    // @ts-ignore
    this.session.listen().done(function(selectedPage) {
      if (selectedPage.detailFormResolved) {
        // No need to update detail content again if resolved is true
        return;
      }
      // Make sure the next time the page is selected it returns immediately and does not wait for requests to be completed
      selectedPage.detailFormResolved = true;
      this.updateDetailContent();
    }.bind(this, selectedPage));
  }

  /**
   * Replacement for Outline#updateDetailMenusRemote(). 'This' points to the outline.
   */
  static updateDetailMenusRemote() {
    // @ts-ignore
    if (!this.modelAdapter) {
      // @ts-ignore
      return this.updateDetailMenusOrig();
    }
    // @ts-ignore
    if (this.selectedNode() && this.selectedNode().detailFormResolved) {
      // @ts-ignore
      return this.updateDetailMenusOrig();
    }
  }

  /**
   * Replaced to make sure page is correctly initialized (linked with row).
   * This cannot be done using pageInit event because the page needs to be initialized during the outline initialization
   * and the event listener can only be attached afterwards.
   */
  protected static _initTreeNodeInternalRemote(page: Page, parentNode) {
    // @ts-ignore
    this._initTreeNodeInternalOrig(page, parentNode);
    // @ts-ignore
    if (!this.modelAdapter) {
      return;
    }
    // The current method may be called during init of the Outline
    // -> widget is not set yet but the following methods need it
    // @ts-ignore
    this.modelAdapter.widget = this;
    if (page.detailTable) {
      // @ts-ignore
      this.modelAdapter._initDetailTable(page);
    }
    // @ts-ignore
    this.modelAdapter._linkNodeWithRowLater(page);
  }
}

App.addListener('bootstrap', OutlineAdapter.modifyOutlinePrototype);
