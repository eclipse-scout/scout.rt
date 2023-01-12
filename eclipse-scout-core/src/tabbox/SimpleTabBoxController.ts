/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  EventHandler, InitModelOf, objects, scout, SimpleTab, SimpleTabArea, SimpleTabAreaTabSelectEvent, SimpleTabBox, SimpleTabBoxControllerModel, SimpleTabBoxViewActivateEvent, SimpleTabBoxViewAddEvent, SimpleTabBoxViewDeactivateEvent,
  SimpleTabBoxViewRemoveEvent, SimpleTabView
} from '../index';
import $ from 'jquery';

/**
 * The {@link SimpleTabBoxController} is used to link a {@link SimpleTabBox} with a {@link SimpleTabArea}.
 * There are {@link SimpleTabBox} with more than one {@link SimpleTabArea} to be actualized.
 * Therefore, the linking is separated in a controller.
 * The controller basically listens to 'viewAdd', 'viewRemove', 'viewActivate', 'viewDeactivate' on the {@link SimpleTabBox} and
 * updates the {@link SimpleTabArea}.
 */
export class SimpleTabBoxController<TView extends SimpleTabView = SimpleTabView> implements SimpleTabBoxControllerModel<TView> {
  declare model: SimpleTabBoxControllerModel<TView>;

  tabBox: SimpleTabBox<TView>;
  tabArea: SimpleTabArea<TView>;

  protected _viewAddHandler: EventHandler<SimpleTabBoxViewAddEvent<TView>>;
  protected _viewRemoveHandler: EventHandler<SimpleTabBoxViewRemoveEvent<TView>>;
  protected _viewActivateHandler: EventHandler<SimpleTabBoxViewActivateEvent<TView>>;
  protected _viewDeactivateHandler: EventHandler<SimpleTabBoxViewDeactivateEvent<TView>>;
  protected _viewTabSelectHandler: EventHandler<SimpleTabAreaTabSelectEvent<TView>>;

  constructor() {
    this.tabBox = null;
    this._viewAddHandler = this._onViewAdd.bind(this);
    this._viewRemoveHandler = this._onViewRemove.bind(this);
    this._viewActivateHandler = this._onViewActivate.bind(this);
    this._viewDeactivateHandler = this._onViewDeactivate.bind(this);

    this.tabArea = null;
    this._viewTabSelectHandler = this._onViewTabSelect.bind(this);
  }

  init(model: InitModelOf<this>) {
    $.extend(this, model);
  }

  install(tabBox: SimpleTabBox<TView>, tabArea?: SimpleTabArea<TView>) {
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

  createTabArea(): SimpleTabArea<TView> {
    return scout.create(SimpleTabArea, {
      parent: this.tabBox
    }) as SimpleTabArea<TView>;
  }

  /** @internal */
  _onViewAdd(event: { view: TView; siblingView?: TView }) {
    let view = event.view;
    if (!SimpleTabBoxController.hasViewTab(view)) {
      return;
    }

    let viewTab = this.getTab(view);
    if (!viewTab && this._shouldCreateTabForView(view)) {
      let siblingView = event.siblingView;
      let siblingViewTab = this.getTab(siblingView); // the sibling to insert the tab after.
      viewTab = this._createTab(view);
      this.tabArea.addTab(viewTab, siblingViewTab);
    }
  }

  protected _shouldCreateTabForView(view: TView): boolean {
    return true;
  }

  protected _onViewRemove(event: SimpleTabBoxViewRemoveEvent<TView>) {
    let view = event.view;
    if (!view) {
      return;
    }
    let viewTab = this.getTab(view);
    if (viewTab) {
      this.tabArea.destroyTab(viewTab);
    }
  }

  /** @internal */
  _onViewActivate(event: { view: TView }) {
    let viewTab = this.getTab(event.view);
    // also reset selection if no view tab of the view is found.
    this.tabArea.selectTab(viewTab);
  }

  protected _onViewDeactivate(event: SimpleTabBoxViewDeactivateEvent<TView>) {
    let viewTab = this.getTab(event.view);
    // also reset selection if no view tab of the view is found.
    this.tabArea.deselectTab(viewTab);
  }

  protected _onViewTabSelect(event: SimpleTabAreaTabSelectEvent<TView>) {
    if (!event.viewTab) {
      return;
    }
    let view = event.viewTab.view;
    this.tabBox.activateView(view);
  }

  protected _createTab(view: TView): SimpleTab<TView> {
    return scout.create(SimpleTab, {
      parent: this.tabArea,
      view: view
    }) as SimpleTab<TView>;
  }

  protected getTab(view: TView): SimpleTab<TView> {
    if (!view) {
      return;
    }
    let viewTab: SimpleTab<TView> = null;
    this.tabArea.getTabs().some(tab => {
      if (tab.view === view) {
        viewTab = tab;
        return true;
      }
      return false;
    });
    return viewTab;
  }

  getTabs(): SimpleTab<TView>[] {
    return this.tabArea.getTabs();
  }

  static hasViewTab(view: SimpleTabView): boolean {
    return view && (!objects.isNullOrUndefined(view.title) || !objects.isNullOrUndefined(view.subTitle) || !objects.isNullOrUndefined(view.iconId));
  }
}
