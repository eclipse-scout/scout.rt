/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.DesktopTab = function() {
  scout.DesktopTab.parent.call(this);
};
scout.inherits(scout.DesktopTab, scout.SimpleTab);

scout.DesktopTab.prototype._render = function() {
  scout.DesktopTab.parent.prototype._render.call(this);
  this.$container.addClass('desktop-tab');
  this.$container.on('contextmenu', this._onContextMenu.bind(this));
};

scout.DesktopTab.prototype._onContextMenu = function() {
  var menuCloseOtherTabs = scout.create('Menu', {
    parent: this,
    text: this.session.text('ui.CloseOtherTabs'),
    enabled: this.parent.tabs.length > 1
  });
  menuCloseOtherTabs.on('action', this._onCloseOther.bind(this));

  var menuCloseAllTabs = scout.create('Menu', {
    parent: this,
    text: this.session.text('ui.CloseAllTabs')
  });
  menuCloseAllTabs.on('action', this._onCloseAll.bind(this));

  var popup = scout.create('ContextMenuPopup', {
    parent: this,
    menuItems: [menuCloseOtherTabs, menuCloseAllTabs],
    cloneMenuItems: false,
    location: {
      x: event.pageX,
      y: event.pageY
    }
  });
  popup.open();
};

scout.DesktopTab.prototype._onCloseAll = function() {
  var openViews = this.parent.tabs.map(function(desktopTab) {
    return desktopTab.view;
  });
  this.session.desktop.closeViews(openViews);
};

scout.DesktopTab.prototype._onCloseOther = function() {
  var openViews = this.parent.tabs.map(function(desktopTab) {
    return desktopTab.view;
  });
  scout.arrays.remove(openViews, this.view);
  this.session.desktop.closeViews(openViews);
};

