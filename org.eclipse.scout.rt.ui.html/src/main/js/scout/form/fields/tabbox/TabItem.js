/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.TabItem = function() {
  scout.TabItem.parent.call(this);
  this.marked = false;
};
scout.inherits(scout.TabItem, scout.GroupBox);

scout.TabItem.prototype._init = function(model) {
  scout.TabItem.parent.prototype._init.call(this, model);
  this._setMenusVisible(this.menusVisible);
};

scout.TabItem.prototype._createLayout = function() {
  return new scout.TabItemLayout(this);
};

/**
 * @override GroupBox.js
 *
 * handled by Tab.js
 */
scout.TabItem.prototype._computeTitleVisible = function(labelVisible) {
  return false;
};

/**
 * @override GroupBox.js
 *
 * handled by Tab.js
 */
scout.TabItem.prototype.addStatus = function() {
  // void
};
/**
 * @override GroupBox.js
 *
 * handled by Tab.js
 */
scout.TabItem.prototype._computeStatusVisible = function() {
  return false;
};

scout.TabItem.prototype.setMarked = function(marked) {
  this.setProperty('marked', marked);
};

scout.TabItem.prototype._setMenusVisible = function() {
  // Always invisible because menus are displayed in menu bar and not with status icon
  // Actually not needed at the moment because only value fields have menus (at least at the java model).
  // But actually we should change this so that menus are possible for every form field
  // TODO [7.0] cgu: remove this comment if java model supports form field menus
  this._setProperty('menusVisible', false);
};

/**
 * @override FormField.js
 */
scout.TabItem.prototype.focus = function() {
  if (this.parent.selectedTab !== this) {
    this.parent.setSelectedTab(this);
  }
  // ensure the focus is on the tab
  this.parent.focusTab(this);
};
