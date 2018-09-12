/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TileTableField = function() {
  scout.TileTableField.parent.call(this);

  this._tableBlurHandler = this._onTableBlur.bind(this);
  this._tableFocusHandler = this._onTableFocus.bind(this);
  this._menuBarPropertyChangeHandler = this._onMenuBarPropertyChange.bind(this);
};
scout.inherits(scout.TileTableField, scout.TableField);

/**
 * @override
 */
scout.TileTableField.prototype._renderTable = function() {
  scout.TileTableField.parent.prototype._renderTable.call(this);
  if (this.parent.displayStyle !== scout.FormFieldTile.DisplayStyle.DASHBOARD) {
    return;
  }
  if (this.table) {
    // Never show header menus for this widget
    this.table.setHeaderMenusEnabled(false);
    // Disable sorting for user but don't disabled sorting in Java because CoreTableTile needs sorting functionality.
    this.table.setSortEnabled(false);
    this.table.$container
      .on('blur', this._tableBlurHandler)
      .on('focus', this._tableFocusHandler);
    this.table.menuBar.on('propertyChange', this._menuBarPropertyChangeHandler);
    this._toggleHasMenuBar();
    if (document.activeElement !== this.table.$container[0]) {
      this._hideMenuBar(true);
    }
  }
};

/**
 * @override
 */
scout.TileTableField.prototype._removeTable = function() {
  if (this.parent.displayStyle !== scout.FormFieldTile.DisplayStyle.DASHBOARD) {
    return;
  }
  if (this.table) {
    this.table.$container
      .off('blur', this._tableBlurHandler)
      .off('focus', this._tableFocusHandler);
    this.table.menuBar.off('propertyChange', this._menuBarPropertyChangeHandler);
  }
  scout.TileTableField.parent.prototype._removeTable.call(this);
};

scout.TileTableField.prototype._onTableBlur = function(event) {
  var popup = $('.popup').data('widget');

  // hide menu bar if context menu popup is not attached to TileTableField
  if (!this.has(popup)) {
    this._hideMenuBar(true);
  }
};

scout.TileTableField.prototype._onTableFocus = function(event) {
  this._hideMenuBar(false);
};

scout.TileTableField.prototype._hideMenuBar = function(hiddenByUi) {
  this.table.menuBar.hiddenByUi = hiddenByUi;
  this.table.menuBar.updateVisibility();
};

scout.TileTableField.prototype._onMenuBarPropertyChange = function(event) {
  if (event.propertyName === 'visible') {
    this._toggleHasMenuBar();
  }
};

scout.TileTableField.prototype._toggleHasMenuBar = function() {
  // adjust menu bar on TileTableField with the additional class has-menubar.
  this.$container.toggleClass('has-menubar', this.table.menuBar.visible);
};
