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

/**
 * The {@link {@link scout.SimpleTabBoxController}} is used to link a {@link {@link scout.SimpleTabBox}} with a {@link {@link scout.SimpleTabArea}}.
 * There are {@link {@link scout.SimpleTabBox}} with more than one {@link {@link scout.SimpleTabArea}} to actualized.
 * Therefore the linking is separated in a controller.
 * The controller basically listens to 'viewAdd', 'viewRemove', 'viewActivate', 'viewDeactivate' on the {@link {@link scout.SimpleTabBox}} and
 * updates the {@link {@link scout.SimpleTabArea}}.
 */
scout.SimpleTabBoxController = function(tabBox, tabArea) {
  this.tabBox;
  this._viewAddHandler = this._onViewAdd.bind(this);
  this._viewRemoveHandler = this._onViewRemove.bind(this);
  this._viewActivateHandler = this._onViewActivate.bind(this);
  this._viewDeactivateHandler = this._onViewDeactivate.bind(this);

  this.tabArea;
  this._viewTabSelectHandler = this._onViewTabSelect.bind(this);
};

scout.SimpleTabBoxController.prototype.init = function(model) {
  $.extend(this, model);
};

scout.SimpleTabBoxController.prototype.install = function(tabBox, tabArea) {
  this.tabBox = scout.assertParameter('tabBox', tabBox);
  this.tabArea = scout.assertParameter('tabArea', tabArea || this.tabBox.tabArea);

  this._installListeners();
};

scout.SimpleTabBoxController.prototype.uninstall = function() {
  this._uninstallListeners();
};

scout.SimpleTabBoxController.prototype._installListeners = function() {
  this.tabBox.on('viewAdd', this._viewAddHandler);
  this.tabBox.on('viewRemove', this._viewRemoveHandler);
  this.tabBox.on('viewActivate', this._viewActivateHandler);
  this.tabBox.on('viewDeactivate', this._viewDeactivateHandler);

  this.tabArea.on('tabSelect', this._viewTabSelectHandler);
};

scout.SimpleTabBoxController.prototype._uninstallListeners = function() {
  this.tabBox.off('viewAdd', this._viewAddHandler);
  this.tabBox.off('viewRemove', this._viewRemoveHandler);
  this.tabBox.off('viewActivate', this._viewActivateHandler);
  this.tabBox.off('viewDeactivate', this._viewDeactivateHandler);

  this.tabArea.off('tabSelect', this._viewTabSelectHandler);
};

scout.SimpleTabBoxController.prototype._onViewAdd = function(event) {
  var view = event.view,
    siblingView = event.siblingView,
    viewTab,
    // the sibling to insert the tab after.
    siblingViewTab;

  if (!scout.SimpleTabBoxController.hasViewTab(view)) {
    return;
  }
  viewTab = this._getTab(view);
  if (!viewTab) {
    siblingViewTab = this._getTab(siblingView);
    viewTab = this._createTab(view);
    this.tabArea.addTab(viewTab, siblingViewTab);
  }
};

scout.SimpleTabBoxController.prototype._onViewRemove = function(event) {
  var view = event.view;
  if (!view) {
    return;
  }
  var viewTab = this._getTab(view);
  if (viewTab) {
    this.tabArea.destroyTab(viewTab);
  }
};

scout.SimpleTabBoxController.prototype._onViewActivate = function(event) {
  var viewTab = this._getTab(event.view);
  // also reset selection if no view tab of the view is found.
  this.tabArea.selectTab(viewTab);
};

scout.SimpleTabBoxController.prototype._onViewDeactivate = function(event) {
  var viewTab = this._getTab(event.view);
  // also reset selection if no view tab of the view is found.
  this.tabArea.deselectTab(viewTab);
};

scout.SimpleTabBoxController.prototype._onViewTabSelect = function(event) {
  if (!event.viewTab) {
    return;
  }
  var view = event.viewTab.view;
  this.tabBox.activateView(view);
};

scout.SimpleTabBoxController.prototype._createTab = function(view) {
  return scout.create('SimpleTab', {
    parent: this.tabArea,
    view: view
  });
};

scout.SimpleTabBoxController.prototype._getTab = function(view) {
  if (!view) {
    return;
  }
  var viewTab;
  this.tabArea.getTabs().some(function(tab) {
    if (tab.view === view) {
      viewTab = tab;
      return true;
    }
    return false;
  });
  return viewTab;
};

scout.SimpleTabBoxController.prototype.getTabs = function() {
  return this.tabArea.getTabs();
};

/* ----- static functions ----- */

scout.SimpleTabBoxController.hasViewTab = function(view) {
  return scout.objects.someProperties(view, ['title', 'subTitle', 'iconId']);  // FIXME STUDIO why???
};
