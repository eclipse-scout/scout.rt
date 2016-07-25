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
     *   und ruft selber scout.create bzw. scout.model.getModel auf.
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
  // FIXME [awe] 6.1 scout.create für detailTable aufrufen, damit man detailTable auch in model.json konfigurieren kann
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
  if (this.detailTable) {
    this.detailTable.deleteAllRows();
    var rows = this._loadTableData();
    if (rows && rows.length > 0) {
      this.detailTable.insertRows(rows);
    }
  }
};

/**
 * Override this method to load table data (rows to be added to table).
 */
scout.Page.prototype._loadTableData = function() {
  // NOP
};

scout.Page.prototype.getTreeNodeFor = function(tableRow) {

};

scout.Page.prototype.getPageFor = function(tableRow) {

};

scout.Page.prototype.getTableRowFor = function(treeNode) {

};

scout.Page.prototype.getTableRowsFor = function(treeNodes) {

};

scout.Page.prototype.addChildPage = function(childPage) {
  this.childNodes.push(childPage);
};
