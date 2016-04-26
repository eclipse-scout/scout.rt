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
 * The {@link {@link scout.ViewTabAreaController}} is used to link a {@link {@link scout.ViewArea}} with a {@link {@link scout.ViewTabArea}}.
 * There are {@link {@link scout.ViewArea}} with more than one {@link {@link scout.ViewTabArea}} to actualized.
 * Therefore the linking is separated in a controller.
 * The controller basically listens to 'viewAdded', 'viewRemoved', 'viewActivated', 'viewDeactivated' on the {@link {@link scout.ViewArea}} and
 * updates the {@link {@link scout.ViewTabArea}}.
 */
scout.ViewTabAreaController = function(viewArea, viewTabArea) {
  this.viewArea = viewArea;
  this.viewTabArea = viewTabArea;

  this._viewAddedHandler = this._onViewAdded.bind(this);
  this._viewRemovedHandler = this._onViewRemoved.bind(this);
  this._viewActivatedHandler = this._onViewActivated.bind(this);
  this._viewDeactivatedHandler = this._onViewDeactivated.bind(this);

  this._viewTabSelectedHandler = this._onViewTabSelected.bind(this);

  this._installListeners();

  // TODO add initial tabs
};

scout.ViewTabAreaController.prototype._installListeners = function() {
  this.viewArea.on('viewAdded', this._viewAddedHandler);
  this.viewArea.on('viewRemoved', this._viewRemovedHandler);
  this.viewArea.on('viewActivated', this._viewActivatedHandler);
  this.viewArea.on('viewDeactivated', this._viewDeactivatedHandler);

  this.viewTabArea.on('tabSelected', this._viewTabSelectedHandler);
};

scout.ViewTabAreaController.prototype._onViewAdded = function(event) {
  var view = event.view,
  siblingView = event.siblingView,
  viewTab,
  // the sibling to insert the tab after.
  siblingViewTab;

  if (!scout.ViewTabAreaController.hasViewTab(view)) {
    return;
  }
  viewTab = this._getViewTab(view);
  if(!viewTab){
    siblingViewTab = this._getViewTab(siblingView);
    viewTab = scout.create('DesktopViewTab', {
      parent: this.viewTabArea,
      view: view
    });
    this.viewTabArea.addTab(viewTab, siblingViewTab);
  }

};

scout.ViewTabAreaController.prototype._onViewRemoved = function(event) {
  var view = event.view;
  if (!view) {
    return;
  }
  var viewTab = this._getViewTab(view);
  if (viewTab) {
    this.viewTabArea.removeTab(viewTab);
  }
};

scout.ViewTabAreaController.prototype._onViewActivated = function(event) {
  var viewTab = this._getViewTab(event.view);
  // also reset selection if no view tab of the view is found.
  this.viewTabArea.selectViewTab(viewTab);
};

scout.ViewTabAreaController.prototype._onViewDeactivated = function(event) {
  var viewTab = this._getViewTab(event.view);
  // also reset selection if no view tab of the view is found.
  this.viewTabArea.deselectViewTab(viewTab);
};

scout.ViewTabAreaController.prototype._onViewTabSelected = function(event) {
  if (!event.viewTab) {
    return;
  }
  var view = event.viewTab.view;
  this.viewArea.activateView(view);
};

scout.ViewTabAreaController.prototype._getViewTab = function(view) {
  if (!view) {
    return;
  }
  var viewTab;
  this.viewTabArea.getViewTabs().some(function(tab) {
    if (tab.view === view) {
      viewTab = tab;
      return true;
    }
    return false;
  });
  return viewTab;
};

scout.ViewTabAreaController.prototype.getViewTabs = function() {
  return this.viewTabArea.getViewTabs();
};

/** static functions **/
scout.ViewTabAreaController.hasViewTab = function(view) {
  return scout.objects.someProperties(view, ['title', 'subTitle', 'iconId']);
};
