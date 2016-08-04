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
scout.Page = function(outline) {
  scout.Page.parent.call(this, outline);

  this.detailTable;
  this.detailTableVisible = true;
  this.detailForm;
  this.detailFormVisible = true;

  /**
   * This property contains the class-name of the form to be instantiated, when createDetailForm() is called.
   */
  this.detailFormType = null;
  this.tableStatusVisible = true;
};
scout.inherits(scout.Page, scout.TreeNode);

scout.Page.prototype.createDetailForm = function() {
  if (this.detailFormType) {
    // FIXME [awe] 6.1 - hier kann folgendes passieren:
    // 1. im model.json eine Page mit einem Detail-Form 'konfigurieren', das Detail-Form ist ebenfalls konfiguriert
    // 2. im model.json eine Page mit einem Detail-Form 'konfigurieren', das Detail-Form liegt als .JS implementierung vor
    // 3. eine Subklasse von Page wurde implementiert und will ein Detail-Form instanzieren, das im model.json konfiguriert ist
    // 4. eine Subklasse von Page wurde implementiert und will ein Detail-Form instanzieren, das Detail-Form liegt als .JS implementierung vor

    /*
     * Ideen:
     *
     * ${ref:x} könnte so implementiert sein, dass man für Name x zuerst in model.json sucht
     *   und wenn man dort nichts findet schaut, ob es einen Constructor für x gibt.
     *
     * Wenn man selber eine Page implementiert hat, überschreibt man createDetailForm
     *   und ruft selber scout.create bzw. scout.models.get auf.
     *
     * Die getModel() method sollte evtl. mit scout.create kombiniert werden (options object?) oder createWithModel() methode?
     */
  }
  return null;
};

/**
 * @override TreeNode.js
 */
scout.Page.prototype._init = function(model) {
  scout.Page.parent.prototype._init.call(this, model);
  if (model.detailTable) { // FIXME [awe] 6.1 - try to get rid of this switch (required for case when server sends detailTable)
    this.detailTable = model.detailTable;
  } else {
    this.detailTable = this._createTable();
    if (this.detailTable) {
      this.detailTable.setTableStatusVisible(this.tableStatusVisible);
    }
  }
};

/**
 * Override this method to create the internal table. Default impl. returns null.
 */
scout.Page.prototype._createTable = function() {
  return null;
};

// AbstractPageWithTable#loadChildren -> hier wird die table geladen und der baum neu aufgebaut
// wird von AbstractTree#P_UIFacade aufgerufen
scout.Page.prototype.loadTableData = function() {
  if (this.detailTable) { // FIXME [awe] 6.1 - check if we ever DO NOT have a detailTable
    this.detailTable.deleteAllRows();
    return this._loadTableData()
      .done(this._onLoadTableDataDone.bind(this))
      .fail(this._onLoadTableDataFail.bind(this));
  } else {
    var deferred = $.Deferred(); // FIXME [awe] 6.1 review with C.GU. is it Ok to return a DFD when no detailTable is available?
    deferred.resolve();
    return deferred; // FIXME [awe] 6.1 - check if we must return deferred.promise() instead of the deferred itself
  }
};

/**
 * Override this method to load table data (rows to be added to table).
 * This is an asynchronous operation working with a Deferred. When table data load is successful
 * <code>_onLoadTableData(data)</code> will be called. When a failure occurs while loading table
 * data <code>_onLoadTableFail(data)</code> will be called.
 * <p>
 * When you want to return static data you still need a deferred. But you can resolve it
 * immediately. Example code:
 * <code>
 *   var deferred = $.Deferred();
 *   deferred.resolve([{...},{...}]);
 *   return deferred;
 * </code>
 *
 * @return jQuery.Deferred
 */
scout.Page.prototype._loadTableData = function() {
  // NOP
};

/**
 * This method is called when table data load is successful. It should transform the table data
 * object to table rows.
 *
 * @param tableData data loaded by <code>_loadTableData</code>
 */
scout.Page.prototype._onLoadTableDataDone = function(tableData) {
  var rows = this._transformTableDataToTableRows(tableData);
  if (rows && rows.length > 0) {
    this.detailTable.insertRows(rows);
  }
  // FIXME [awe] 6.1 - discuss with C.GU, is this the right place?
  this._ensureDetailForm();
};

scout.Page.prototype._ensureDetailForm = function() {
  if (this.detailForm) {
    return;
  }
  this.detailForm = this.createDetailForm();
};

/**
 * This method converts the loaded table data, which can be any object, into table rows.
 * You must override this method unless tableData is already an array of table rows.
 *
 * @param tableData
 * @returns
 */
scout.Page.prototype._transformTableDataToTableRows = function(tableData) {
  return tableData;
};

scout.Page.prototype._onLoadTableDataFail = function(jqXHR, textStatus, errorThrown) {
  $.log.error('Failed to load tableData. error=' + textStatus);
};
