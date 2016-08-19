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
 * The controller basically listens to 'viewAdded', 'viewRemoved', 'viewActivated', 'viewDeactivated' on the {@link {@link scout.SimpleTabBox}} and
 * updates the {@link {@link scout.SimpleTabArea}}.
 */
scout.SimpleTabBoxController = function(tabBox, tabArea) {
  this.tabBox = tabBox;
  this.tabArea = tabArea;

  this._viewAddedHandler = this._onViewAdded.bind(this);
  this._viewRemovedHandler = this._onViewRemoved.bind(this);
  this._viewActivatedHandler = this._onViewActivated.bind(this);
  this._viewDeactivatedHandler = this._onViewDeactivated.bind(this);

  this._viewTabSelectedHandler = this._onViewTabSelected.bind(this);

  this._installListeners();
};

scout.SimpleTabBoxController.prototype._installListeners = function() {
  this.tabBox.on('viewAdded', this._viewAddedHandler);
  this.tabBox.on('viewRemoved', this._viewRemovedHandler);
  this.tabBox.on('viewActivated', this._viewActivatedHandler);
  this.tabBox.on('viewDeactivated', this._viewDeactivatedHandler);

  this.tabArea.on('tabSelected', this._viewTabSelectedHandler);
};

scout.SimpleTabBoxController.prototype._onViewAdded = function(event) {
  var view = event.view,
  siblingView = event.siblingView,
  viewTab,
  // the sibling to insert the tab after.
  siblingViewTab;

  if (!scout.SimpleTabBoxController.hasViewTab(view)) {
    return;
  }
  viewTab = this._getTab(view);
  if(!viewTab){
    siblingViewTab = this._getTab(siblingView);
    viewTab = scout.create('DesktopTab', {
      parent: this.tabArea,
      view: view
    });
    this.tabArea.addTab(viewTab, siblingViewTab);
  }

};

scout.SimpleTabBoxController.prototype._onViewRemoved = function(event) {
  var view = event.view;
  if (!view) {
    return;
  }
  var viewTab = this._getTab(view);
  if (viewTab) {
    this.tabArea.destroyTab(viewTab);
  }
};

scout.SimpleTabBoxController.prototype._onViewActivated = function(event) {
  var viewTab = this._getTab(event.view);
  // also reset selection if no view tab of the view is found.
  this.tabArea.selectTab(viewTab);
};

scout.SimpleTabBoxController.prototype._onViewDeactivated = function(event) {
  var viewTab = this._getTab(event.view);
  // also reset selection if no view tab of the view is found.
  this.tabArea.deselectTab(viewTab);
};

scout.SimpleTabBoxController.prototype._onViewTabSelected = function(event) {
  if (!event.viewTab) {
    return;
  }
  var view = event.viewTab.view;
  this.tabBox.activateView(view);
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
  return scout.objects.someProperties(view, ['title', 'subTitle', 'iconId']);
};
