/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  App, ChildModelOf, dataObjects, ErrorHandler, EventHandler, Form, icons, objects, Outline, Page, PageModel, PageParamDo, PageResolver, RemoteEvent, scout, Table, TableAdapter, TableFilterRemovedEvent, TableRow, TableRowInitEvent,
  TableRowsInsertedEvent, TreeAdapter, TreeNodeModel
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

  override onModelAction(event: RemoteEvent) {
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
    objects.replacePrototypeFunction(Page, '_updateDetailFormMenus', OutlineAdapter._updateDetailFormMenus, true);
    objects.replacePrototypeFunction(Page, '_updateDetailTableMenus', OutlineAdapter._updateDetailTableMenus, true);
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

  protected static override _createTreeNodeRemote(this: Outline & { modelAdapter: OutlineAdapter; _createTreeNodeOrig }, pageModel: PageModel) {
    // nodeType is only used for Scout Classic pages
    if (!this.modelAdapter || !pageModel?.nodeType) {
      return this._createTreeNodeOrig(pageModel);
    }

    pageModel = this.modelAdapter._initNodeModel(pageModel);
    if (pageModel.nodeType === 'jsPage') {
      try {
        return this.modelAdapter._createJsPage(pageModel, this._createTreeNodeOrig.bind(this));
      } catch (error) {
        // Create a broken page instead of showing a fatal error so the application can still be used.
        pageModel = this.modelAdapter._createBrokenPageModel(pageModel);
        scout.create(ErrorHandler, {displayError: false, sendError: true}).handle(error);
      }
    }

    return this._createTreeNodeOrig(pageModel);
  }

  protected _createJsPage(pageModel: PageModel, createPage: (pageModel: PageModel) => Page): Page {
    let pageParam = pageModel.pageParam;
    pageModel.jsPageObjectType = pageModel.jsPageObjectType || PageResolver.get(this.session).findObjectTypeForPageParam(pageParam);
    if (!pageModel.jsPageObjectType) {
      if (pageParam) {
        throw new Error('Could not resolve page objectType for pageParam ' + JSON.stringify(pageParam));
      }
      throw new Error('jsPageObjectType not set');
    }

    let jsPageModel = this._createJsPageModel(pageModel, pageParam);
    if (pageModel.jsPageModel) {
      // If the jsPageModel contains data objects, deserialize them if possible.
      // Create POJOs for objects whose _type attribute cannot be resolved to a class to maintain backward compatibility.
      let deserializedJsPageModel = dataObjects.deserialize(pageModel.jsPageModel, null, {createPojoIfDoIsUnknown: true});
      delete deserializedJsPageModel._type; // _type should not be written to page if present
      jsPageModel = $.extend({}, deserializedJsPageModel, jsPageModel);
    }

    let page = createPage(jsPageModel);
    if (page.classId && page.uuid && page.classId !== page.uuid) {
      throw new Error(`ClassId and uuid don't match for page ${page.objectType}. ClassId: ${page.classId}, Uuid: ${page.uuid}`);
    }
    return page;
  }

  protected _createJsPageModel(pageModel: PageModel, pageParam: PageParamDo): PageModel {
    return {
      id: pageModel.id,
      parent: pageModel.parent,
      owner: pageModel.owner,
      objectType: pageModel.jsPageObjectType,
      pageParam: pageParam,
      classId: pageModel.classId,
      modelClass: pageModel.modelClass,
      childNodeIndex: pageModel.childNodeIndex,
      __hybrid: true
    };
  }

  protected _createBrokenPageModel(pageModel: PageModel): PageModel {
    return {
      ...this._createJsPageModel(pageModel, null),
      objectType: Page,
      text: this.session.text('ui.CouldNotCreateElement'),
      iconId: icons.EXCLAMATION_MARK_CIRCLE,
      overviewIconId: pageModel.iconId,
      cssClass: 'broken'
    };
  }

  protected override _initNodeModel(nodeModel?: TreeNodeModel): ChildModelOf<Page> {
    const model = super._initNodeModel(nodeModel) as ChildModelOf<Page>;
    model.pageParam = dataObjects.deserialize(model.pageParam);
    return model;
  }

  protected static _updateDetailFormMenus(this: Page & { _updateDetailFormMenusOrig; remote?: true }) {
    const detailForm = this.detailForm;
    if (detailForm && (!detailForm.modelAdapter || !this.remote)) {
      // Update menus if either the detail form or the page is written in JavaScript
      // -> menus are updated for JS pages with a Java form, Java Pages with a JS form and JS pages with a JS form
      this._updateDetailFormMenusOrig();
    }
  }

  protected static _updateDetailTableMenus(this: Page & { _updateDetailTableMenusOrig; remote?: true }) {
    const detailTable = this.detailTable;
    if (detailTable && (!detailTable.modelAdapter || !this.remote)) {
      // Update menus if either the detail table or the page is written in JavaScript
      // -> menus are updated for JS pages with a Java table, Java Pages with a JS table and JS pages with a JS table
      this._updateDetailTableMenusOrig();
    }
  }

  static linkWithRow(this: Page & { linkWithRowOrig: typeof Page.prototype.linkWithRow }, row: TableRow) {
    this.linkWithRowOrig(row);
    // @ts-expect-error
    this._updateDetailMenus();
  }

  static unlinkWithRow(this: Page & { unlinkWithRowOrig: typeof Page.prototype.unlinkWithRow }, row: TableRow) {
    this.unlinkWithRowOrig(row);
    // @ts-expect-error
    this._updateDetailMenus();
  }
}

App.addListener('bootstrap', OutlineAdapter.modifyOutlinePrototype);
