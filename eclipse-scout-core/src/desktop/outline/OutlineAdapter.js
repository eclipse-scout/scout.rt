/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {App, objects, Outline, scout, TreeAdapter} from '../../index';

export default class OutlineAdapter extends TreeAdapter {

  constructor() {
    super();
    this._filterDirty = false;
    this._nodeIdToRowMap = {};
    this._detailTableRowInitHandler = this._onDetailTableRowInit.bind(this);
    this._detailTableRowsInsertedHandler = this._onDetailTableRowsInserted.bind(this);
    this._detailTableFilterRemoved = this._onDetailTableFilterRemoved.bind(this);
  }

  _onPageChanged(event) {
    let page = this.widget._nodeById(event.nodeId);
    page.overviewIconId = event.overviewIconId;

    page.detailFormVisible = event.detailFormVisible;
    let detailForm = this.session.getOrCreateWidget(event.detailForm, this.widget);
    page.setDetailForm(detailForm);

    page.navigateButtonsVisible = event.navigateButtonsVisible;
    page.detailTableVisible = event.detailTableVisible;
    let detailTable = this.session.getOrCreateWidget(event.detailTable, this.widget);
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

  onModelAction(event) {
    if (event.type === 'pageChanged') {
      this._onPageChanged(event);
    } else {
      super.onModelAction(event);
    }
  }

  _initDetailTable(page) {
    // link already existing rows now
    page.detailTable.rows.forEach(this._linkNodeWithRow.bind(this));
    // rows which are inserted later are linked by _onDetailTableRowInit
    page.detailTable.on('rowInit', this._detailTableRowInitHandler);
    page.detailTable.on('rowsInserted', this._detailTableRowsInsertedHandler);
    page.detailTable.on('filterRemoved', this._detailTableFilterRemoved);
  }

  _destroyDetailTable(page) {
    this._nodeIdToRowMap = {};
    page.detailTable.rows.forEach(this._unlinkNodeWithRow.bind(this));
    page.detailTable.off('rowInit', this._detailTableRowInitHandler);
    page.detailTable.off('rowsInserted', this._detailTableRowsInsertedHandler);
    page.detailTable.off('filterRemoved', this._detailTableFilterRemoved);
  }

  _linkNodeWithRow(row) {
    scout.assertParameter('row', row);
    let node,
      nodeId = row.nodeId;

    if (nodeId === undefined) {
      // nodeId is undefined if no node exists for that row (e.g. happens if the page containing the row is a leaf page)
      return;
    }

    node = this.widget.nodesMap[nodeId];
    if (node) {
      node.linkWithRow(row);
    } else {
      // Prepare for linking later because node has not been inserted yet
      // see: #_linkNodeWithRowLater
      this._nodeIdToRowMap[nodeId] = row;
    }
  }

  _unlinkNodeWithRow(row) {
    let node = this.widget.nodesMap[row.nodeId];
    if (node) {
      node.unlinkWithRow(row);
    }
  }

  _onDetailTableRowInit(event) {
    this._linkNodeWithRow(event.row);

    let node = this.widget.nodesMap[event.row.nodeId];
    if (this.widget.isSelectedNode(node) && !this.widget.detailContent) {
      // Table row detail could not be created because the link from page to row was missing at the time the node got selected -> do it now
      this.widget.updateDetailContent();
    }
  }

  _onDetailTableRowsInserted(event) {
    let table = event.source;

    if (this._filterDirty ||
      (table._filterCount() > 0 && event.rows.some(row => !row.filterAccepted))) {
      this._filterDirty = false;
      // Explicitly call filter if some of the new rows are not accepted.
      // If they are accepted, table.insertRows() will trigger a filter event by itself that will be mediated to the outline by OutlineMediator.js
      this.widget.filter();
    }
  }

  _onDetailTableFilterRemoved(event) {
    let table = event.source;
    if (table.modelAdapter && table.modelAdapter._rebuildingTable) {
      // If a column is removed, the tableAdapter prevents filtering because the flag _rebuildingTable is true
      // -> the outline does not get informed, hence the nodes stay invisible.
      this._filterDirty = true;
    }
  }

  /**
   * Link node with row, if it hasn't been linked yet.
   */
  _linkNodeWithRowLater(page) {
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
  }

  /**
   * Replacement for Outline#_computeDetailContent(). 'This' points to the outline.
   */
  static _computeDetailContentRemote() {
    if (!this.modelAdapter) {
      return this._computeDetailContentOrig();
    }

    let selectedPage = this.selectedNode();
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
   * Replacement for Outline#updateDetailMenusRemote(). 'This' points to the outline.
   */
  static updateDetailMenusRemote() {
    if (!this.modelAdapter) {
      return this.updateDetailMenusOrig();
    }
    if (this.selectedNode() && this.selectedNode().detailFormResolved) {
      return this.updateDetailMenusOrig();
    }
  }

  /**
   * Replaced to make sure page is correctly initialized (linked with row).
   * This cannot be done using pageInit event because the page needs to be initialized during the outline initialization
   * and the event listener can only be attached afterwards.
   */
  static _initTreeNodeInternalRemote(page, parentNode) {
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
}

App.addListener('bootstrap', OutlineAdapter.modifyOutlinePrototype);
