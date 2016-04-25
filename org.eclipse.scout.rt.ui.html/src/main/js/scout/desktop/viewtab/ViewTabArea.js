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
scout.ViewTabArea = function() {
  scout.ViewTabArea.parent.call(this);
  this.viewTabs = [];
};
scout.inherits(scout.ViewTabArea, scout.Widget);

scout.ViewTabArea.prototype._init = function(model) {
  scout.ViewTabArea.parent.prototype._init.call(this, model);
  this.visible = true;
  this._selectedViewTab;

  //  this.menuBar = scout.create('MenuBar', {
  //    parent: this,
  //    menuOrder: new scout.GroupBoxMenuItemsOrder()
  //  });
  this._viewTabSelectionHandler = this._onTabSelection.bind(this);

  this._addEventSupport();
};


scout.ViewTabArea.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('view-tab-area');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.ViewTabAreaLayout(this));
  this._renderTabs();
  if(!this.visible || this.viewTabs.length < 1){
    this._triggerChildrenBeforeDetach(this);
    this._detach();

  }
};

scout.ViewTabArea.prototype._renderTabs = function() {
  // reverse since tabs rendered without a sibling will be prepended.
  this.viewTabs.reverse()
    .forEach(function(tab) {
      this._renderTab(tab);
    }.bind(this));
};

scout.ViewTabArea.prototype._renderTab = function(tab) {
  tab.renderAfter(this.$container);
};

scout.ViewTabArea.prototype._renderVisible = function() {
  if (this.visible && this.viewTabs.length > 0) {
    if (!this.attached) {
      this.attach();
    }
  } else {
    if (this.attached) {
      this.detach();
    }
  }
};

scout.ViewTabArea.prototype._attach = function() {
  this._$parent.prepend(this.$container);
  this.session.detachHelper.afterAttach(this.$container);
  // If the parent was resized while this view was detached, the view has a wrong size.
  this.invalidateLayoutTree(false);
  scout.ViewTabArea.parent.prototype._attach.call(this);
};

/**
 * @override Widget.js
 */
scout.ViewTabArea.prototype._detach = function() {

  this.session.detachHelper.beforeDetach(this.$container);
  this.$container.detach();
  scout.ViewTabArea.parent.prototype._detach.call(this);
  this.invalidateLayoutTree(false);
};

scout.ViewTabArea.prototype._onTabSelection = function(event) {
  this.selectViewTab(event.source);
};

scout.ViewTabArea.prototype.setVisible = function(visible) {
  this.visible = visible;
  this._renderVisible();
  this.invalidateLayoutTree();
};

scout.ViewTabArea.prototype.getViewTabs = function() {
  return this.viewTabs;
};

scout.ViewTabArea.prototype.selectViewTab = function(viewTab) {
  if (this._selectedViewTab === viewTab) {
    return;
  }
  this.deselectViewTab(this._selectedViewTab);

  this._selectedViewTab = viewTab;
  if (viewTab) {
    // Select the new view tab.
    viewTab.select();
  }
  this.trigger('tabSelected', {
    viewTab: viewTab
  });
};

scout.ViewTabArea.prototype.deselectViewTab = function(viewTab) {
  if (!viewTab) {
    return;
  }
  if (this._selectedViewTab !== viewTab) {
    return;
  }
  this._selectedViewTab.deselect();

};

scout.ViewTabArea.prototype.getSelectedViewTab = function() {
  return this._selectedViewTab;
};

scout.ViewTabArea.prototype.addTab = function(tab, sibling) {
  var insertPosition = -1;
  if (sibling) {
    insertPosition = this.viewTabs.indexOf(sibling);
  }
  this.viewTabs.splice(insertPosition + 1, 0, tab);
  tab.on('tabClicked', this._viewTabSelectionHandler);
  if(this.rendered){
    this._renderVisible();
    tab.renderAfter(this.$container, sibling);
    this.invalidateLayoutTree();
  }
};

scout.ViewTabArea.prototype.removeTab = function(tab) {
  var index = this.viewTabs.indexOf(tab);
  if (index > -1) {
    this.viewTabs.splice(index, 1);
    tab.remove();
    tab.off('tabClicked', this._viewTabSelectionHandler);
    this._renderVisible();
    this.invalidateLayoutTree();
  }
};
