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
import {arrays, FlexboxLayout, FlexboxLayoutData, HtmlComponent, scout, SimpleTabBox, Splitter, strings, Widget, widgets} from '../../index';

export default class BenchColumn extends Widget {

  constructor() {
    super();
    this.tabBoxes = [];
    this._widgetToTabBox = {}; // [key=viewId, value=SimpleTabBox instance]
    this.components = null;
    this._removeViewInProgress = 0;
    this.layoutData;

    // event listener functions
    this._viewAddHandler = this._onViewAdd.bind(this);
    this._viewRemoveHandler = this._onViewRemove.bind(this);
    this._viewActivateHandler = this._onViewActivate.bind(this);
    this._viewDeactivateHandler = this._onViewDeactivate.bind(this);
  }

  static TAB_BOX_INDEX = {
    TOP: 0,
    CENTER: 1,
    BOTTOM: 2
  };

  static TAB_BOX_CLASSES = [
    'north',
    'center',
    'south'
  ];

  _init(model) {
    super._init(model);
    this.layoutData = model.layoutData;
    this.layoutCacheKey = model.cacheKey;
    this.cssClass = model.cssClass;
    this._createTabBoxes();
  }

  /**
   * Returns a $container used as a bind target for the key-stroke context of the group-box.
   * By default this function returns the container of the form, or when group-box is has no
   * form as a parent the container of the group-box.
   */
  _keyStrokeBindTarget() {
    return this.$container;
  }

  _render() {
    this.$container = this.$parent.appendDiv('bench-column');
    if (this.cssClass) {
      this.$container.addClass(this.cssClass);
    }
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(this._createLayout());

    this.htmlComp.layoutData = this.getLayoutData();
  }

  _renderProperties() {
    super._renderProperties();
    this._renderTabBoxes();
    this._revalidateSplitters();
  }

  _renderTabBoxes() {
    this.visibleTabBoxes().forEach(tabBox => {
      this._renderTabBox(tabBox);
    });
    this.updateFirstLastMarker();
  }

  _renderTabBox(tabBox) {
    if (!tabBox.rendered) {
      tabBox.render();
    }
  }

  postRender() {
    this.tabBoxes.forEach(tabBox => {
      tabBox.postRender();
    });
  }

  _createLayout() {
    return new FlexboxLayout(FlexboxLayout.Direction.COLUMN, this.layoutCacheKey);
  }

  updateLayoutData(layoutData, cacheKey) {
    if (this.getLayoutData() === layoutData) {
      return;
    }
    this.layoutCacheKey = cacheKey;
    this.setLayoutData(layoutData);

    // update columns
    let rowDatas = this.layoutData.getRows();
    this.tabBoxes.forEach((tb, i) => {
      tb.setLayoutData(rowDatas[i]);
    });
    this._updateSplitterMovable();
    if (this.rendered) {
      this.htmlComp.layout.setCacheKey(this.layoutCacheKey);
      this.htmlComp.layout.reset();
      this.htmlComp.invalidateLayoutTree();
    }
  }

  setLayoutData(layoutData) {
    super.setLayoutData(layoutData);
    this.layoutData = layoutData;
  }

  getLayoutData() {
    return this.layoutData;
  }

  _onViewAdd(event) {
    this.trigger('viewAdd', {
      view: event.view
    });
  }

  _onViewRemove(event) {
    this.trigger('viewRemove', {
      view: event.view
    });
  }

  _onViewActivate(event) {
    this.trigger('viewActivate', {
      view: event.view
    });
  }

  _onViewDeactivate(event) {
    this.trigger('viewDeactivate', {
      view: event.view
    });
  }

  activateView(view) {
    let tabBox = this.getTabBox(view.displayViewId);
    tabBox.activateView(view);
  }

  _createTabBoxes() {
    let rowLayoutDatas = [];
    if (this.layoutData) {
      rowLayoutDatas = this.layoutData.getRows();
    }
    for (let i = 0; i < 3; i++) {
      let tabBox = scout.create('SimpleTabBox', {
        parent: this,
        cssClass: strings.join(' ', 'view-tab-box', BenchColumn.TAB_BOX_CLASSES[i]),
        controller: scout.create('DesktopTabBoxController')
      });
      tabBox.setLayoutData(rowLayoutDatas[i]);
      tabBox.on('viewAdd', this._viewAddHandler);
      tabBox.on('viewRemove', this._viewRemoveHandler);
      tabBox.on('viewActivate', this._viewActivateHandler);
      tabBox.on('viewDeactivate', this._viewDeactivateHandler);
      this.tabBoxes.push(tabBox);
    }
  }

  _revalidateSplitters(clearPosition) {
    // remove old splitters
    if (this.components) {
      this.components.forEach(comp => {
        if (comp instanceof Splitter) {
          comp.destroy();
        }
      });
    }
    this.components = this.visibleTabBoxes()
      .reduce((arr, col) => {
        if (arr.length > 0) {
          // add sep
          let splitter = scout.create('Splitter', {
            parent: this,
            $anchor: arr[arr.length - 1].$container,
            $root: this.$container,
            splitHorizontal: false,
            maxRatio: 1
          });
          splitter.render();
          splitter.setLayoutData(FlexboxLayoutData.fixed().withOrder(col.getLayoutData().order - 1));
          splitter.$container.addClass('line');
          arr.push(splitter);
        }
        arr.push(col);
        return arr;
      }, []);
    // well order the dom elements (reduce is used for simple code reasons, the result of reduce is not of interest).
    this.components.filter(comp => {
      return comp instanceof SimpleTabBox;
    })
      .reduce((c1, c2, index) => {
        if (index > 0) {
          c2.$container.insertAfter(c1.$container);
        }
        return c2;
      }, undefined);
    this._updateSplitterMovable();
  }

  _updateSplitterMovable() {
    if (!this.components) {
      return;
    }
    this.components.forEach((c, i) => {
      if (c instanceof Splitter) {
        let componentsBefore = this.components.slice(0, i).reverse();
        let componentsAfter = this.components.slice(i + 1);
        // shrink
        if (
          componentsBefore.filter(c => {
            return c.getLayoutData().shrink > 0;
          }).length > 0 &&
          componentsAfter.filter(c => {
            return c.getLayoutData().grow > 0;
          }).length > 0
        ) {
          c.setEnabled(true);
          c.on('move', this._onSplitterMove.bind(this));
          return;
        }
        // grow
        if (
          componentsBefore.filter(c => {
            return c.getLayoutData().grow > 0;
          }).length > 0 &&
          componentsAfter.filter(c => {
            return c.getLayoutData().shrink > 0;
          }).length > 0
        ) {
          c.setEnabled(true);
          c.on('move', this._onSplitterMove.bind(this));
          return;
        }
        c.setEnabled(false);

      }
    });
  }

  _onSplitterMove(event) {
    let splitter = event.source;
    // noinspection UnnecessaryLocalVariableJS
    let diff = event.position - splitter.htmlComp.location().y - splitter.htmlComp.margins().top - splitter.htmlComp.insets().top;
    splitter.getLayoutData().diff = diff;
    this.revalidateLayout();
    splitter.getLayoutData().diff = null;
    event.preventDefault();
  }

  addView(view, bringToFront) {
    let tabBox = this.getTabBox(view.displayViewId);
    this._widgetToTabBox[view.id] = tabBox;

    tabBox.addView(view, bringToFront);

    if (this.rendered && tabBox.viewCount() === 1) {
      if (!tabBox.rendered) {
        // lazy render if the first view is added.
        tabBox.render();
      }
      this._revalidateSplitters(true);
      this.updateFirstLastMarker();
      this.htmlComp.layout.reset();
      this.htmlComp.invalidateLayoutTree();
    }
  }

  getTabBox(displayViewId) {
    let tabBox;
    switch (displayViewId) {
      case 'NW':
      case 'N':
      case 'NE':
        tabBox = this.tabBoxes[BenchColumn.TAB_BOX_INDEX.TOP];
        break;
      case 'SW':
      case 'S':
      case 'SE':
        tabBox = this.tabBoxes[BenchColumn.TAB_BOX_INDEX.BOTTOM];
        break;
      default:
        tabBox = this.tabBoxes[BenchColumn.TAB_BOX_INDEX.CENTER];
        break;
    }
    return tabBox;
  }

  removeView(view, showSiblingView) {
    let tabBox = this._widgetToTabBox[view.id];
    if (tabBox) {
      this._removeViewInProgress++;
      tabBox.removeView(view, showSiblingView);
      this._removeViewInProgress--;
      delete this._widgetToTabBox[view.id];
      if (this.rendered && tabBox.viewCount() === 0 && this._removeViewInProgress === 0) {
        // remove view area if no view is left.
        tabBox.remove();
        this._revalidateSplitters(true);
        this.updateFirstLastMarker();
        this.htmlComp.layout.reset();
        this.htmlComp.invalidateLayoutTree();
      }
    }
  }

  viewCount() {
    return this.tabBoxes.map(tabBox => {
      return tabBox.viewCount();
    }).reduce((c1, c2) => {
      return c1 + c2;
    }, 0);
  }

  hasView(view) {
    return this.tabBoxes.filter(tabBox => {
      return tabBox.hasView(view);
    }).length > 0;
  }

  hasViews() {
    return this.viewCount() > 0;
  }

  getViews(displayViewId) {
    return this.tabBoxes.reduce((arr, tabBox) => {
      arrays.pushAll(arr, tabBox.getViews(displayViewId));
      return arr;
    }, []);
  }

  getComponents() {
    return this.components;
  }

  visibleTabBoxes() {
    return this.tabBoxes.filter(tabBox => {
      return tabBox.hasViews();
    });
  }

  updateFirstLastMarker() {
    widgets.updateFirstLastMarker(this.visibleTabBoxes());
  }
}
