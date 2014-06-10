// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

/**
 * @param model represents a node page of an outline
 */
scout.DesktopTable = function() {
  scout.DesktopTable.parent.call(this);
  this._addAdapterProperties(['table', 'tableControls']);
  this.table;
  this.tableControls;
};

scout.inherits(scout.DesktopTable, scout.ModelAdapter);

scout.DesktopTable.prototype._render = function($parent) {
  this._$parent = $parent;
  this.table.attach(this._$parent);

  if (this._tableControls) {
    for (var i = 0; i < this._tableControls.length; i++) {
      var control = this._tableControls[i];
      control.table = this.table;
      this.table.footer.addControl(control);
    }
  }

};

scout.DesktopTable.prototype.detach = function() {
  this.table.detach();
};

scout.DesktopTable.prototype.attach = function($parent) {
  if (!this.table.$container) {
    this._render($parent);
  } else {
    this.table.attach($parent);
  }
};
