/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  App, ChildModelOf, EventHandler, Form, objects, Outline, Page, RemoteEvent, scout, Table, TableAdapter, TableFilterRemovedEvent, TableRow, TableRowInitEvent, TableRowsInsertedEvent, Tree, TreeAdapter, TreeNode, TreeNodeModel, UuidPool
} from '../../index';

export class OutlineAdapter extends TreeAdapter {
  declare widget: Outline;
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

  protected _onPageChanged(event: RemoteEvent) {
    let success = false;
    let page = this.widget.nodeById(event.nodeId);
    try {
      page.setPageChanging(true);
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
      success = true;
    } finally {
      page.setPageChanging(false);
    }
    if (success) {
      this.widget.pageChanged(page);
    }
  }

  protected _onSearchFilterForPageResponse(event: RemoteEvent) {
    let eventId = event.eventId;
    let searchData = event.searchData;
    this.trigger('searchFilterForPageResponse:' + eventId, {searchData});
  }

  override onModelAction(event: RemoteEvent) {
    if (event.type === 'pageChanged') {
      this._onPageChanged(event);
    } else if (event.type === 'searchFilterForPageResponse') {
      this._onSearchFilterForPageResponse(event);
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

    let node = this.widget.nodesMap[nodeId];
    if (node) {
      node.linkWithRow(row);
    } else {
      // Prepare for linking later because node has not been inserted yet
      // see: #_linkNodeWithRowLater
      this._nodeIdToRowMap[nodeId] = row;
    }
  }

  protected _unlinkNodeWithRow(row: TableRow) {
    let node = this.widget.nodesMap[row.nodeId];
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

    if (this._filterDirty || (table.filterCount() > 0 && event.rows.some(row => !row.filterAccepted))) {
      this._filterDirty = false;
      // Explicitly call filter if some new rows are not accepted.
      // If they are accepted, table.insertRows() will trigger a filter event by itself that will be mediated to the outline by OutlineMediator.js
      this.widget.filter();
    }
  }

  protected _onDetailTableFilterRemoved(event: TableFilterRemovedEvent) {
    let table = event.source;
    let tableModelAdapter = table.modelAdapter as TableAdapter;
    if (tableModelAdapter && tableModelAdapter._rebuildingTable) {
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

  protected override _getDefaultNodeObjectType(): string {
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
    objects.replacePrototypeFunction(Outline, Outline.prototype.getSearchFilterForPage, OutlineAdapter.getSearchFilterForPageRemote, true);
    objects.replacePrototypeFunction(Page, '_updateParentTablePageMenusForDetailForm', OutlineAdapter._updateParentTablePageMenusForDetailForm, true);
    objects.replacePrototypeFunction(Page, '_updateParentTablePageMenusForDetailTable', OutlineAdapter._updateParentTablePageMenusForDetailTable, true);
    objects.replacePrototypeFunction(Page, 'linkWithRow', OutlineAdapter.linkWithRow, true);
    objects.replacePrototypeFunction(Page, 'unlinkWithRow', OutlineAdapter.unlinkWithRow, true);
  }

  /**
   * Replacement for {@link Outline._computeDetailContent}.
   */
  protected static _computeDetailContentRemote(this: Outline & { _computeDetailContentOrig }) {
    if (!this.modelAdapter) {
      return this._computeDetailContentOrig();
    }

    let selectedPage: Page & { detailFormResolved?: boolean } = this.selectedNode();
    if (!selectedPage) {
      // Detail content is shown for the selected node only
      return null;
    }

    // if there is a detail form, use this
    if (selectedPage.detailForm || selectedPage.detailFormResolved) {
      // If there is a detail form -> return (and set flag to true to make updateDetailMenusRemote work)
      selectedPage.detailFormResolved = true;
      return this._computeDetailContentOrig();
    }

    // It is not known yet whether there is a detail form -> wait for the requests to be processed before showing the table row detail
    if (!this.session.areRequestsPending() && !this.session.areEventsQueued()) {
      // There are no requests pending -> return (and set flag to true to make updateDetailMenusRemote work)
      selectedPage.detailFormResolved = true;
      return this._computeDetailContentOrig();
    }

    // Wait for the requests to complete
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
   * Replacement for {@link Outline.updateDetailMenus}.
   */
  static updateDetailMenusRemote(this: Outline & { updateDetailMenusOrig: typeof Outline.prototype.updateDetailMenus }) {
    if (!this.modelAdapter) {
      return this.updateDetailMenusOrig();
    }
    let selectedPage: Page & { detailFormResolved?: boolean } = this.selectedNode();
    if (selectedPage && selectedPage.detailFormResolved) {
      return this.updateDetailMenusOrig();
    }
  }

  /**
   * Replaced to make sure page is correctly initialized (linked with row).
   * This cannot be done using pageInit event because the page needs to be initialized during the outline initialization
   * and the event listener can only be attached afterward.
   */
  protected static _initTreeNodeInternalRemote(this: Outline & { modelAdapter: OutlineAdapter; _initTreeNodeInternalOrig }, page: Page, parentNode: Page) {
    this._initTreeNodeInternalOrig(page, parentNode);
    if (!this.modelAdapter) {
      return;
    }
    // The current method may be called during init of the Outline
    // -> widget is not set yet but the following methods need it
    this.modelAdapter.widget = this;
    if (page.detailTable) {
      this.modelAdapter._initDetailTable(page);
    }
    this.modelAdapter._linkNodeWithRowLater(page);
  }

  protected static override _createTreeNodeRemote(this: Tree & { modelAdapter: TreeAdapter; _createTreeNodeOrig }, nodeModel: TreeNodeModel) {
    // nodeType is only used for Scout Classic pages
    if (!this.modelAdapter || !nodeModel?.nodeType) {
      return this._createTreeNodeOrig(nodeModel);
    }

    nodeModel = (this.modelAdapter as OutlineAdapter)._initNodeModel(nodeModel);

    if (nodeModel.nodeType === 'jsPage') {
      if (!nodeModel.jsPageObjectType?.length) {
        throw new Error('jsPageObjectType not set');
      }

      let jsPageModel = {
        id: nodeModel.id,
        parent: nodeModel.parent,
        owner: nodeModel.owner,
        objectType: nodeModel.jsPageObjectType,
        text: nodeModel.text || undefined // because summary column might come from Java parent page
      };

      if (nodeModel.jsPageModel) {
        jsPageModel = $.extend(true, {}, nodeModel.jsPageModel, jsPageModel);
      }

      nodeModel = jsPageModel;
    }

    return this._createTreeNodeOrig(nodeModel);
  }

  protected static getSearchFilterForPageRemote(this: Outline & { modelAdapter: OutlineAdapter; getSearchFilterForPageOrig: typeof Outline.prototype.getSearchFilterForPage }, page: Page & { remote?: true }) {
    if (this.modelAdapter && page && page.remote) {
      let eventId = UuidPool.take(this.session);
      this.modelAdapter._send('searchFilterForPageRequest', {
        eventId: eventId,
        pageId: page.id
      });
      return this.modelAdapter.when('searchFilterForPageResponse:' + eventId)
        .then((event: any) => event.searchData); // FIXME bsh [js-bookmark] Event Map
    }
    return this.getSearchFilterForPageOrig(page);
  }

  protected override _initNodeModel(nodeModel?: TreeNodeModel): ChildModelOf<TreeNode> {
    const model = super._initNodeModel(nodeModel);
    // This marker is only set for pages that represent a remote page on the UI server. It prevents menus from being inherited
    // from the parent table page, because in the case of Java pages that is already done on the server.
    model.remote = true;
    return model;
  }

  protected static _updateParentTablePageMenusForDetailForm(this: Page & { _updateParentTablePageMenusForDetailFormOrig; remote?: true }) {
    const detailForm = this.detailForm;
    if (detailForm && (!detailForm.modelAdapter || !this.remote)) {
      this._updateParentTablePageMenusForDetailFormOrig();
    }
  }

  protected static _updateParentTablePageMenusForDetailTable(this: Page & { _updateParentTablePageMenusForDetailTableOrig; remote?: true }) {
    const detailTable = this.detailTable;
    if (detailTable && (!detailTable.modelAdapter || !this.remote)) {
      this._updateParentTablePageMenusForDetailTableOrig();
    }
  }

  static linkWithRow(this: Page & { linkWithRowOrig: typeof Page.prototype.linkWithRow }, row: TableRow) {
    this.linkWithRowOrig(row);
    // @ts-expect-error
    this._updateParentTablePageMenusForDetailFormAndDetailTable();
  }

  static unlinkWithRow(this: Page & { unlinkWithRowOrig: typeof Page.prototype.unlinkWithRow }, row: TableRow) {
    this.unlinkWithRowOrig(row);
    // @ts-expect-error
    this._updateParentTablePageMenusForDetailFormAndDetailTable();
  }
}

App.addListener('bootstrap', OutlineAdapter.modifyOutlinePrototype);
