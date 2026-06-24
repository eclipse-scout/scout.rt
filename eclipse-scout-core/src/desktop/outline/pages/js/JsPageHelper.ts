/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {
  arrays, BookmarkSupport, dataObjects, HybridActionContextElement, HybridActionContextElements, HybridManager, InitModelOf, LoadChildPagesHybridActionDo, objects, ObjectWithType, Outline, OutlineAdapter, Page, PageParamDo, PageWithTable,
  scout, SomeRequired, Table, TableRowsInsertedEvent, TreeAllChildNodesDeletedEvent, TreeNodesDeletedEvent, TreeNodesInsertedEvent, TreeNodesUpdatedEvent, typeName
} from '../../../../index';

/**
 * This helper can be used to create child pages for a JsPage on the UI server.
 * The child pages are created using an id or a {@link PageParamDo}.
 * The child pages are created by the Java equivalent of the JsPage (see AbstractJsPage.createChildPage) when {@link #callLoadChildPages} is called.
 * After the returned {@link Promise} is resolved the child pages are available by {@link #findChildPage}.
 *
 * To create child pages for a {@link PageWithTable}:
 * - Call and await {@link #callLoadChildPages} in {@link PageWithTable#_loadTableData} using ids or {@link PageParamDo}s created from the loaded data.
 * - Implement {@link PageWithTable#_createChildPage} and return the result of {@link #findChildPage} using an id or {@link PageParamDo} created for the given {@link TableRow}.
 *
 * To create child pages for a {@link PageWithNodes}:
 * - Implement {@link PageWithNodes#_createChildPages} and call {@link #callLoadChildPages} using ids or {@link PageParamDo}s.
 * - After the {@link Promise} is resolved use the same ids or {@link PageParamDo}s to call {@link #findChildPages} and return the resulting child pages.
 */
export class JsPageHelper implements JsPageHelperModel, ObjectWithType {

  declare model: JsPageHelperModel;
  declare initModel: SomeRequired<this['model'], 'page'>;

  objectType: string;

  page: Page;

  protected _replaceChildPages = true;
  /**
   * Child pages by id (see {@link #_createChildPageId}).
   */
  protected _childPagesById = new Map<string, JsPageChildPage>();

  // general listeners

  protected _nodesUpdatedHandler = this._onNodesUpdated.bind(this);
  protected _nodesDeletedHandler = this._onNodesDeleted.bind(this);
  protected _allChildNodesDeletedHandler = this._onAllChildNodesDeleted.bind(this);

  // listeners for PageWithTable

  protected _nodesInsertedHandler = this._onNodesInserted.bind(this);
  protected _tableRowsInsertHandler = this._onTableRowsInserted.bind(this);

  init(model: InitModelOf<this>) {
    $.extend(this, model);

    scout.assertInstance(this.page, Page, 'Page not set or has wrong type');

    // do not replace child pages initially
    // if e.g. the browser is reloaded the previously created child pages already exist on the UI server and therefore childNodes are set in the pages initModel
    // if the page is a PageWithTable its table will be loaded when the page is selected and this load must not lead to all child pages being replaced
    this._replaceChildPages = !this.page.childNodes?.length;

    this._installOutlineListeners();
    this._installPageWithTableListeners();
  }

  public destroy() {
    this._uninstallOutlineListeners();
    this._uninstallPageWithTableListeners();
    this.page = null;
  }

  protected _installOutlineListeners() {
    this.outline.on('nodesUpdated', this._nodesUpdatedHandler);
    this.outline.on('nodesDeleted', this._nodesDeletedHandler);
    this.outline.on('allChildNodesDeleted', this._allChildNodesDeletedHandler);
  }

  protected _uninstallOutlineListeners() {
    this.outline.off('nodesUpdated', this._nodesUpdatedHandler);
    this.outline.off('nodesDeleted', this._nodesDeletedHandler);
    this.outline.off('allChildNodesDeleted', this._allChildNodesDeletedHandler);
  }

  protected _onNodesUpdated(event: TreeNodesUpdatedEvent) {
    if (!event.nodes?.length) {
      return;
    }

    // filter child pages of page
    const childPages = event.nodes.filter(node => node.parentNode === this.page) as Page[];
    if (!childPages.length) {
      return;
    }

    // Child pages may have been updated with e.g. a new text. Send a nodesChanged event in order to send this information to the UI server.
    // This information is needed when the browser is reloaded as the child pages will still be available.
    (this.outline.modelAdapter as OutlineAdapter).sendNodesChanged(childPages);
  }

  protected _onNodesDeleted(event: TreeNodesDeletedEvent) {
    if (!event.nodes?.length) {
      return;
    }

    // mark page as childrenLoaded=false if all child pages are deleted
    if (!this.page.childNodes.length) {
      this.page.childrenLoaded = false;
    }

    // clean up _childPagesById map
    const pages = event.nodes as Page[];
    this._removeChildPagesFromIdMap(pages);

    if (!this.page.detailTable) {
      return;
    }

    // unlink corresponding rows, otherwise e.g. double-clicking a row will lead to errors as the page was deleted
    if (this.page instanceof PageWithTable) {
      pages.forEach(page => page.row && page.unlinkWithRow(page.row));
    }
    // Note: nothing to be done for PageWithNodes, because OutlineMediator#onChildPagesChanged automatically rebuilds the detail table on tree changes
  }

  protected _onAllChildNodesDeleted(event: TreeAllChildNodesDeletedEvent) {
    if (event.parentNode !== this.page) {
      return;
    }

    // always mark page as childrenLoaded=false as all child pages are deleted
    this.page.childrenLoaded = false;

    // clean up _childPagesById map
    this._childPagesById.clear();

    if (!this.page.detailTable) {
      return;
    }

    // unlink all rows, otherwise e.g. double-clicking a row will lead to errors as the page was deleted
    if (this.page instanceof PageWithTable) {
      this.page.detailTable.rows.forEach(row => row.page?.unlinkWithRow(row));
    }
    // Note: nothing to be done for PageWithNodes, because OutlineMediator#onChildPagesChanged automatically rebuilds the detail table on tree changes
  }

  protected _installPageWithTableListeners() {
    if (!(this.page instanceof PageWithTable)) {
      return;
    }

    this.outline.on('nodesInserted', this._nodesInsertedHandler);

    // detailTable may change -> install for current and listen on propertyChange
    this._installPageWithTableDetailTableListeners(this.page?.detailTable);
    this.page.on('propertyChange:detailTable', e => {
      this._uninstallPageWithTableDetailTableListeners(e.oldValue);
      this._installPageWithTableDetailTableListeners(e.newValue);
    });
  }

  protected _uninstallPageWithTableListeners() {
    if (!(this.page instanceof PageWithTable)) {
      return;
    }

    this.outline.off('nodesInserted', this._nodesInsertedHandler);
    this._uninstallPageWithTableDetailTableListeners(this.page?.detailTable);
  }

  protected _onNodesInserted(event: TreeNodesInsertedEvent) {
    if (event.parentNode !== this.page) {
      return;
    }

    // The event is triggered twice during the load/reload of a PageWithTable that creates its child pages using the JsPageHelper.
    // 1. When the child pages are inserted on the UI server it is called for the first time.
    //    In this case e.g. the text may not be correct as the page was not updated from the table row yet.
    //    If this happens, the PageWithTable is marked with childrenLoaded=false.
    // 2. The second time this event is triggered is when the PageWithTable replaces its rows.
    //    Now the child page is ready to be inserted as it was linked to the row and updated from it.
    //    As the table load/reload is now completed, the PageWithTable is marked with childrenLoaded=true.

    // Mark all child pages loading iff the PageWithTable has not loaded its children.
    for (const childPage of event.nodes) {
      childPage.toggleCssClass('js-page-child-page-loading', !this.page.childrenLoaded);
      childPage._decorate();
    }
  }

  protected _installPageWithTableDetailTableListeners(detailTable: Table) {
    if (!detailTable) {
      return;
    }
    detailTable.on('rowsInserted', this._tableRowsInsertHandler);
  }

  protected _uninstallPageWithTableDetailTableListeners(detailTable: Table) {
    if (!detailTable) {
      return;
    }
    detailTable.off('rowsInserted', this._tableRowsInsertHandler);
  }

  protected _onTableRowsInserted(event: TableRowsInsertedEvent) {
    // When rows are inserted in a PageWithTable, all child pages are ready, i.e. they are linked to their row and updated from it.
    // Send a nodesChanged event in order to send summary information from the PageWithTable to the UI server.
    // This information is needed when the browser is reloaded as the child pages will still be available but the table data won't.
    const pages = event.rows.map(row => row.page).filter(Boolean);
    (this.outline.modelAdapter as OutlineAdapter).sendNodesChanged(pages);
  }

  get outline(): Outline {
    return this.page.outline;
  }

  /**
   * Calls loadChildPages on the UI server for the given ids or {@link PageParamDo}s. The pages are created by the Java equivalent of the JsPage (see AbstractJsPage.createChildPage).
   * The returned {@link Promise} is resolved when all child pages are created on the UI server.
   *
   * @param idsOrPageParams ids or {@link PageParamDo}s that are used to create child pages on the UI server.
   * @param replace Whether existing child pages shall be replaced or reused. If not specified, the child pages will always be replaced except for the first call in order to keep existing child pages after a browser reload.
   */
  async callLoadChildPages(idsOrPageParams: (string | PageParamDo)[], replace?: boolean): Promise<void> {
    if (this.page.leaf) {
      return;
    }
    // Ensure pageParams and collect them distinctly.
    // The given list may contain duplicate pageParams.
    // Sending these duplicates to the server would result in pages added multiple times to the outline which leads to unexpected behaviour.
    const childPageIds = new Set<string>();
    const pageParams: PageParamDo[] = [];
    arrays.ensure(idsOrPageParams)
      .map(idOrPageParam => this._createChildPageParam(idOrPageParam))
      .filter(Boolean)
      .forEach(pageParam => {
        // The pageParam is a PageParamDo and a Set only checks "===" and not ".equals()".
        // Therefore, create the child page id to ensure that duplicate page params are only used once.
        const childPageId = this._createChildPageId(pageParam);
        if (childPageIds.has(childPageId)) {
          return;
        }
        childPageIds.add(childPageId);

        pageParams.push(pageParam);
      });
    // ensure replace flag
    // FIXME [js-page-reload] Beim Reload von Js-TablePages während dem Laden eines Bookmarks (Data-Changed-Event + "reload from root")
    //       sollen existierende-Child-Nodes wiederverwendet werden, daher replace = false. Ansonsten werden die Nodes entfernt und
    //       mit ihnen auch die Selektion, was der Benutzer dann sieht.
    replace = scout.nvl(replace, BookmarkSupport.get(this.page.session).loading ? false : this._replaceChildPages);

    // call load child pages
    const hybridManager = await HybridManager.get(this.page.session, true);
    await hybridManager.callActionAndWaitWithContext('scout.LoadChildPages',
      scout.create(LoadChildPagesHybridActionDo, {pageParams, replace}),
      scout.create(HybridActionContextElements)
        .withElement('page', HybridActionContextElement.of(this.outline, this.page))
    );

    // child pages must be replaced after the first load
    this._replaceChildPages = true;

    // The LoadChildPagesHybridAction fires the hybridActionEnd-event after the child pages are created and inserted.
    // The tree and its JsonAdapter may buffer the tree events, but it is ensured that they are contained in the same response from the UI server as the hybridActionEnd-event.
    // As the session processes all events in the response synchronously the nodesInserted-event is processed before the Promise that is waiting for the hybrid action to complete is resolved.
    // Therefore, all child pages created on the UI server are already inserted into the tree, and they can be collected into the child pages map.
    this._addChildPagesToIdMap();
  }

  /**
   * Loads the child page for the given id or {@link PageParamDo}.
   */
  async loadChildPage(idOrPageParam: string | PageParamDo, replace?: boolean): Promise<Page> {
    await this.callLoadChildPages([idOrPageParam], replace);
    return this.findChildPage(idOrPageParam);
  }

  /**
   * Loads the child pages for the given ids or {@link PageParamDo}s and returns them in the order of the given ids or {@link PageParamDo}s.
   */
  async loadChildPages(idsOrPageParams: (string | PageParamDo)[], replace?: boolean): Promise<Page[]> {
    await this.callLoadChildPages(idsOrPageParams, replace);
    return this.findChildPages(idsOrPageParams);
  }

  /**
   * Finds the child page for the given id or {@link PageParamDo}.
   */
  findChildPage(idOrPageParam: string | PageParamDo): Page {
    if (!idOrPageParam) {
      return null;
    }

    // create id and lookup child page
    return this._childPagesById.get(this._createChildPageId(idOrPageParam));
  }

  /**
   * Finds the child pages for the given ids or {@link PageParamDo}s and returns them in the order of the given ids or {@link PageParamDo}s.
   */
  findChildPages(idsOrPageParams: string | PageParamDo | (string | PageParamDo)[]): Page[] {
    if (!idsOrPageParams) {
      return [];
    }

    const idsOrPageParamsArray = arrays.ensure(idsOrPageParams);
    return idsOrPageParamsArray.map(idsOrPageParam => this.findChildPage(idsOrPageParam));
  }

  /**
   * Adds all given child pages to the {@link #_childPagesById}-map. If no child pages are given the child pages of {@link #page} are taken.
   */
  protected _addChildPagesToIdMap(childPages?: JsPageChildPage[]) {
    // use child pages of this.page as default
    childPages = childPages || this.page.childNodes;

    for (const childPage of childPages) {
      // get and assert id
      const id = this._getChildPageId(childPage);
      if (!id) {
        continue;
      }

      // add to map
      this._childPagesById.set(id, childPage);

      // if the page is linked to a row it may be deleted if the row is replaced -> remove link as it will be created again later on
      if (childPage.row) {
        childPage.unlinkWithRow(childPage.row);
      }

      // the child page must not be shown initially -> hide it until e.g. the PageWithTable inserts all nodes for its rows
      this.outline.hideNode(childPage, false);
    }

    // mark page with childrenLoaded=true
    this.page.childrenLoaded = true;
  }

  /**
   * Removes all given child pages from the {@link #_childPagesById}-map. If no child pages are given the child pages of {@link #page} are taken.
   */
  protected _removeChildPagesFromIdMap(childPages?: JsPageChildPage[]) {
    // use child pages of this.page as default
    childPages = childPages || this.page.childNodes;

    for (const childPage of childPages) {
      // get and assert id
      const id = this._getChildPageId(childPage);
      if (!id) {
        continue;
      }

      // remove from map
      this._childPagesById.delete(id);
    }
  }

  /**
   * Creates a {@link PageParamDo} from the given id or {@link PageParamDo}.
   * If the given id or param is an id, this id is wrapped in an {@link IdPageParamDo}.
   * If the given id or param is a {@link PageParamDo} already, it is simply returned.
   */
  protected _createChildPageParam(idOrPageParam?: string | PageParamDo): PageParamDo {
    if (!idOrPageParam) {
      return null;
    }

    // wrap id in IdPageParamDo
    if (objects.isString(idOrPageParam)) {
      return scout.create(IdPageParamDo, {id: idOrPageParam});
    }

    // deserialize object literal DO and check whether the result is a PageParamDo
    idOrPageParam = dataObjects.deserialize(idOrPageParam);
    if (!(idOrPageParam instanceof PageParamDo)) {
      return null;
    }
    return idOrPageParam;
  }

  /**
   * Creates a child page id for the given id or {@link PageParamDo}.
   * If the given id or param is an id already, it is simply returned.
   * If the given id or param is a {@link PageParamDo} it is stringified (see {@link dataObjects#stringify}) in order to create a child page id.
   */
  protected _createChildPageId(idOrPageParam?: string | PageParamDo): string {
    if (!idOrPageParam) {
      return null;
    }

    // simply return id
    if (objects.isString(idOrPageParam)) {
      return idOrPageParam;
    }

    // deserialize object literal DO and check whether the result is a PageParamDo
    idOrPageParam = dataObjects.deserialize(idOrPageParam);
    if (!(idOrPageParam instanceof PageParamDo)) {
      return null;
    }

    // stringify PageParamDo
    return dataObjects.stringify(idOrPageParam);
  }

  /**
   * Gets the child page param from the given {@link JsPageChildPage}.
   */
  protected _getChildPageParam(childPage?: JsPageChildPage): PageParamDo {
    if (!childPage?.__jsPageChildPageParam) {
      return null;
    }

    // ensure that __jsPageChildPageParam is a PageParamDo
    this._ensureJsPageChildPage(childPage);

    return childPage.__jsPageChildPageParam;
  }

  /**
   * Gets the child page id from the given {@link JsPageChildPage}.
   */
  protected _getChildPageId(childPage?: JsPageChildPage): string {
    // get child page param
    const pageParam = this._getChildPageParam(childPage);
    if (!pageParam) {
      return null;
    }

    // page param is IdPageParamDo -> use its id (see _createChildPageParam)
    if (pageParam instanceof IdPageParamDo) {
      return this._createChildPageId(pageParam.id);
    }

    return this._createChildPageId(pageParam);
  }

  /**
   * Ensures the given {@link JsPageChildPage}, i.e. ensures that its properties are of the correct type.
   */
  protected _ensureJsPageChildPage(childPage?: JsPageChildPage) {
    if (!childPage?.__jsPageChildPageParam) {
      return;
    }

    // __jsPageChildPageParam is a PageParamDo already -> return
    if (childPage.__jsPageChildPageParam instanceof PageParamDo) {
      return;
    }

    // deserialize __jsPageChildPageParam as it may be an object literal DO when it comes from the server
    childPage.__jsPageChildPageParam = dataObjects.deserialize(childPage.__jsPageChildPageParam);
  }
}

export interface JsPageHelperModel {
  page?: Page;
}

/**
 * Page param that is used by {@link JsPageHelper} to create child pages if only an id is provided.
 */
@typeName('scout.IdPageParam')
export class IdPageParamDo extends PageParamDo {
  id: string;
}

interface JsPageChildPage extends Page {
  /**
   * The {@link PageParamDo} sent to the server by the {@link JsPageHelper} in order to create this child page.
   */
  __jsPageChildPageParam?: PageParamDo;
}
