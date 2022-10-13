/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {EventHandler, Form, scout, SimpleTab, SimpleTabArea, SimpleTabBox, SimpleTabBoxControllerModel} from '../index';
import $ from 'jquery';
import {SimpleTabBoxViewActivateEvent, SimpleTabBoxViewAddEvent, SimpleTabBoxViewDeactivateEvent, SimpleTabBoxViewRemoveEvent} from './SimpleTabBoxEventMap';
import {SimpleTabAreaTabSelectEvent} from './SimpleTabAreaEventMap';
import {OutlineContent} from '../desktop/bench/DesktopBench';

/**
 * The {@link {@link SimpleTabBoxController}} is used to link a {@link {@link SimpleTabBox}} with a {@link {@link SimpleTabArea}}.
 * There are {@link {@link SimpleTabBox}} with more than one {@link {@link SimpleTabArea}} to actualized.
 * Therefore the linking is separated in a controller.
 * The controller basically listens to 'viewAdd', 'viewRemove', 'viewActivate', 'viewDeactivate' on the {@link {@link SimpleTabBox}} and
 * updates the {@link {@link SimpleTabArea}}.
 */
export default class SimpleTabBoxController implements SimpleTabBoxControllerModel {
  declare model: SimpleTabBoxControllerModel;

  tabBox: SimpleTabBox;
  tabArea: SimpleTabArea;

  protected _viewAddHandler: EventHandler<SimpleTabBoxViewAddEvent>;
  protected _viewRemoveHandler: EventHandler<SimpleTabBoxViewRemoveEvent>;
  protected _viewActivateHandler: EventHandler<SimpleTabBoxViewActivateEvent>;
  protected _viewDeactivateHandler: EventHandler<SimpleTabBoxViewDeactivateEvent>;
  protected _viewTabSelectHandler: EventHandler<SimpleTabAreaTabSelectEvent>;

  constructor() {
    this.tabBox = null;
    this._viewAddHandler = this._onViewAdd.bind(this);
    this._viewRemoveHandler = this._onViewRemove.bind(this);
    this._viewActivateHandler = this._onViewActivate.bind(this);
    this._viewDeactivateHandler = this._onViewDeactivate.bind(this);

    this.tabArea = null;
    this._viewTabSelectHandler = this._onViewTabSelect.bind(this);
  }

  init(model: SimpleTabBoxControllerModel) {
    $.extend(this, model);
  }

  install(tabBox: SimpleTabBox, tabArea?: SimpleTabArea) {
    this.uninstall();
    this.tabBox = scout.assertParameter('tabBox', tabBox);
    this.tabArea = scout.nvl(tabArea, this.tabBox.tabArea);
    if (!this.tabArea) {
      this.tabArea = this.createTabArea();
    }
    this._installListeners();
  }

  uninstall() {
    this._uninstallListeners();
  }

  protected _installListeners() {
    this.tabBox.on('viewAdd', this._viewAddHandler);
    this.tabBox.on('viewRemove', this._viewRemoveHandler);
    this.tabBox.on('viewActivate', this._viewActivateHandler);
    this.tabBox.on('viewDeactivate', this._viewDeactivateHandler);
    this.tabArea.on('tabSelect', this._viewTabSelectHandler);
  }

  protected _uninstallListeners() {
    if (this.tabBox) {
      this.tabBox.off('viewAdd', this._viewAddHandler);
      this.tabBox.off('viewRemove', this._viewRemoveHandler);
      this.tabBox.off('viewActivate', this._viewActivateHandler);
      this.tabBox.off('viewDeactivate', this._viewDeactivateHandler);
    }
    if (this.tabArea) {
      this.tabArea.off('tabSelect', this._viewTabSelectHandler);
    }
  }

  createTabArea(): SimpleTabArea {
    return scout.create(SimpleTabArea, {
      parent: this.tabBox
    });
  }

  protected _onViewAdd(event: { view: OutlineContent; siblingView?: OutlineContent }) {
    let view = event.view;
    if (!SimpleTabBoxController.hasViewTab(view)) {
      return;
    }

    let viewTab = this._getTab(view);
    if (!viewTab && this._shouldCreateTabForView(view)) {
      let siblingView = event.siblingView;
      let siblingViewTab = this._getTab(siblingView); // the sibling to insert the tab after.
      viewTab = this._createTab(view);
      this.tabArea.addTab(viewTab, siblingViewTab);
    }
  }

  protected _shouldCreateTabForView(view: Form): boolean {
    return true;
  }

  protected _onViewRemove(event: SimpleTabBoxViewRemoveEvent) {
    let view = event.view;
    if (!view) {
      return;
    }
    let viewTab = this._getTab(view);
    if (viewTab) {
      this.tabArea.destroyTab(viewTab);
    }
  }

  protected _onViewActivate(event: SimpleTabBoxViewActivateEvent) {
    let viewTab = this._getTab(event.view);
    // also reset selection if no view tab of the view is found.
    this.tabArea.selectTab(viewTab);
  }

  protected _onViewDeactivate(event: SimpleTabBoxViewDeactivateEvent) {
    let viewTab = this._getTab(event.view);
    // also reset selection if no view tab of the view is found.
    this.tabArea.deselectTab(viewTab);
  }

  protected _onViewTabSelect(event: SimpleTabAreaTabSelectEvent) {
    if (!event.viewTab) {
      return;
    }
    let view = event.viewTab.view;
    this.tabBox.activateView(view);
  }

  protected _createTab(view: Form): SimpleTab {
    return scout.create(SimpleTab, {
      parent: this.tabArea,
      view: view
    });
  }

  protected _getTab(view: OutlineContent): SimpleTab {
    if (!view) {
      return;
    }
    let viewTab: SimpleTab = null;
    this.tabArea.getTabs().some(tab => {
      if (tab.view === view) {
        viewTab = tab;
        return true;
      }
      return false;
    });
    return viewTab;
  }

  getTabs(): SimpleTab[] {
    return this.tabArea.getTabs();
  }

  /* ----- static functions ----- */

  static hasViewTab(view: any): view is Form {
    return view && (view.title !== undefined || view.subTitle !== undefined || view.iconId !== undefined);
  }
}
