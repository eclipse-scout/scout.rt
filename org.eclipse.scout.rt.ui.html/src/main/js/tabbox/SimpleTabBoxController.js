/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {SimpleTabArea} from '../index';
import {objects} from '../index';
import {SimpleTabBox} from '../index';
import * as $ from 'jquery';
import {scout} from '../index';


/**
 * The {@link {@link SimpleTabBoxController}} is used to link a {@link {@link SimpleTabBox}} with a {@link {@link SimpleTabArea}}.
 * There are {@link {@link SimpleTabBox}} with more than one {@link {@link SimpleTabArea}} to actualized.
 * Therefore the linking is separated in a controller.
 * The controller basically listens to 'viewAdd', 'viewRemove', 'viewActivate', 'viewDeactivate' on the {@link {@link SimpleTabBox}} and
 * updates the {@link {@link SimpleTabArea}}.
 */
export default class SimpleTabBoxController {

constructor(tabBox, tabArea) {
  this.tabBox = null;
  this._viewAddHandler = this._onViewAdd.bind(this);
  this._viewRemoveHandler = this._onViewRemove.bind(this);
  this._viewActivateHandler = this._onViewActivate.bind(this);
  this._viewDeactivateHandler = this._onViewDeactivate.bind(this);

  this.tabArea = null;
  this._viewTabSelectHandler = this._onViewTabSelect.bind(this);
}

init(model) {
  $.extend(this, model);
}

install(tabBox, tabArea) {
  this.tabBox = scout.assertParameter('tabBox', tabBox);
  this.tabArea = scout.assertParameter('tabArea', tabArea || this.tabBox.tabArea);

  this._installListeners();
}

uninstall() {
  this._uninstallListeners();
}

_installListeners() {
  this.tabBox.on('viewAdd', this._viewAddHandler);
  this.tabBox.on('viewRemove', this._viewRemoveHandler);
  this.tabBox.on('viewActivate', this._viewActivateHandler);
  this.tabBox.on('viewDeactivate', this._viewDeactivateHandler);

  this.tabArea.on('tabSelect', this._viewTabSelectHandler);
}

_uninstallListeners() {
  this.tabBox.off('viewAdd', this._viewAddHandler);
  this.tabBox.off('viewRemove', this._viewRemoveHandler);
  this.tabBox.off('viewActivate', this._viewActivateHandler);
  this.tabBox.off('viewDeactivate', this._viewDeactivateHandler);

  this.tabArea.off('tabSelect', this._viewTabSelectHandler);
}

_onViewAdd(event) {
  var view = event.view,
    siblingView = event.siblingView,
    viewTab,
    // the sibling to insert the tab after.
    siblingViewTab;

  if (!SimpleTabBoxController.hasViewTab(view)) {
    return;
  }
  viewTab = this._getTab(view);
  if (!viewTab) {
    siblingViewTab = this._getTab(siblingView);
    viewTab = this._createTab(view);
    this.tabArea.addTab(viewTab, siblingViewTab);
  }
}

_onViewRemove(event) {
  var view = event.view;
  if (!view) {
    return;
  }
  var viewTab = this._getTab(view);
  if (viewTab) {
    this.tabArea.destroyTab(viewTab);
  }
}

_onViewActivate(event) {
  var viewTab = this._getTab(event.view);
  // also reset selection if no view tab of the view is found.
  this.tabArea.selectTab(viewTab);
}

_onViewDeactivate(event) {
  var viewTab = this._getTab(event.view);
  // also reset selection if no view tab of the view is found.
  this.tabArea.deselectTab(viewTab);
}

_onViewTabSelect(event) {
  if (!event.viewTab) {
    return;
  }
  var view = event.viewTab.view;
  this.tabBox.activateView(view);
}

_createTab(view) {
  return scout.create('SimpleTab', {
    parent: this.tabArea,
    view: view
  });
}

_getTab(view) {
  if (!view) {
    return;
  }
  var viewTab = null;
  this.tabArea.getTabs().some(function(tab) {
    if (tab.view === view) {
      viewTab = tab;
      return true;
    }
    return false;
  });
  return viewTab;
}

getTabs() {
  return this.tabArea.getTabs();
}

/* ----- static functions ----- */

static hasViewTab(view) {
  return objects.someProperties(view, ['title', 'subTitle', 'iconId']);
}
}
