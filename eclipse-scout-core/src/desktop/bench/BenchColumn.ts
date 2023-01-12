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
  arrays, BenchColumnEventMap, BenchColumnModel, BenchRowLayoutData, DesktopTabBoxController, DisplayViewId, EventHandler, FlexboxLayout, FlexboxLayoutData, HtmlComponent, InitModelOf, OutlineContent, scout, SimpleTabBox,
  SimpleTabBoxViewActivateEvent, SimpleTabBoxViewAddEvent, SimpleTabBoxViewDeactivateEvent, SimpleTabBoxViewRemoveEvent, Splitter, SplitterMoveEvent, strings, Widget, widgets
} from '../../index';

export class BenchColumn extends Widget implements BenchColumnModel {
  declare model: BenchColumnModel;
  declare eventMap: BenchColumnEventMap;
  declare self: BenchColumn;

  tabBoxes: SimpleTabBox<OutlineContent>[];
  layoutData: BenchRowLayoutData;
  components: (SimpleTabBox<OutlineContent> | Splitter)[];
  layoutCacheKey: string[];

  protected _widgetToTabBox: Record<string /* viewId */, SimpleTabBox<OutlineContent>>;
  protected _removeViewInProgress: number;
  protected _viewAddHandler: EventHandler<SimpleTabBoxViewAddEvent>;
  protected _viewRemoveHandler: EventHandler<SimpleTabBoxViewRemoveEvent>;
  protected _viewActivateHandler: EventHandler<SimpleTabBoxViewActivateEvent>;
  protected _viewDeactivateHandler: EventHandler<SimpleTabBoxViewDeactivateEvent>;

  constructor() {
    super();
    this.tabBoxes = [];
    this._widgetToTabBox = {};
    this.components = null;
    this._removeViewInProgress = 0;

    this._viewAddHandler = this._onViewAdd.bind(this);
    this._viewRemoveHandler = this._onViewRemove.bind(this);
    this._viewActivateHandler = this._onViewActivate.bind(this);
    this._viewDeactivateHandler = this._onViewDeactivate.bind(this);
  }

  static TAB_BOX_INDEX = {
    TOP: 0,
    CENTER: 1,
    BOTTOM: 2
  } as const;

  static TAB_BOX_CLASSES = [
    'north',
    'center',
    'south'
  ] as const;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.layoutData = model.layoutData;
    this.layoutCacheKey = model.cacheKey;
    this.cssClass = model.cssClass;
    this._createTabBoxes();
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('bench-column');
    if (this.cssClass) {
      this.$container.addClass(this.cssClass);
    }
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(this._createLayout());

    this.htmlComp.layoutData = this.getLayoutData();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderTabBoxes();
    this._revalidateSplitters();
  }

  protected _renderTabBoxes() {
    this.visibleTabBoxes().forEach(tabBox => this._renderTabBox(tabBox));
    this.updateFirstLastMarker();
  }

  protected _renderTabBox(tabBox: SimpleTabBox<OutlineContent>) {
    if (!tabBox.rendered) {
      tabBox.render();
      tabBox.htmlComp.validateRoot = true;
    }
  }

  postRender() {
    this.tabBoxes.forEach(tabBox => tabBox.postRender());
  }

  protected _createLayout(): FlexboxLayout {
    return new FlexboxLayout(FlexboxLayout.Direction.COLUMN, this.layoutCacheKey);
  }

  updateLayoutData(layoutData: BenchRowLayoutData, cacheKey: string[]) {
    if (this.getLayoutData() === layoutData) {
      return;
    }
    this.layoutCacheKey = cacheKey;
    this.setLayoutData(layoutData);

    // update columns
    let rowDatas = this.layoutData.getRows();
    this.tabBoxes.forEach((tb, i) => tb.setLayoutData(rowDatas[i]));
    this._updateSplitterMovable();
    if (this.rendered) {
      let layout = this.htmlComp.layout as FlexboxLayout;
      layout.setCacheKey(this.layoutCacheKey);
      layout.reset();
      this.htmlComp.invalidateLayoutTree();
    }
  }

  override setLayoutData(layoutData: BenchRowLayoutData) {
    super.setLayoutData(layoutData);
    this.layoutData = layoutData;
  }

  getLayoutData(): BenchRowLayoutData {
    return this.layoutData;
  }

  protected _onViewAdd(event: SimpleTabBoxViewAddEvent) {
    this.trigger('viewAdd', {
      view: event.view
    });
  }

  protected _onViewRemove(event: SimpleTabBoxViewRemoveEvent) {
    this.trigger('viewRemove', {
      view: event.view
    });
  }

  protected _onViewActivate(event: SimpleTabBoxViewActivateEvent) {
    this.trigger('viewActivate', {
      view: event.view
    });
  }

  protected _onViewDeactivate(event: SimpleTabBoxViewDeactivateEvent) {
    this.trigger('viewDeactivate', {
      view: event.view
    });
  }

  activateView(view: OutlineContent) {
    let tabBox = this.getTabBox(view.displayViewId);
    tabBox.activateView(view);
  }

  protected _createTabBoxes() {
    let rowLayoutDatas: FlexboxLayoutData[] = [];
    if (this.layoutData) {
      rowLayoutDatas = this.layoutData.getRows();
    }
    for (let i = 0; i < 3; i++) {
      let tabBox = scout.create(SimpleTabBox, {
        parent: this,
        cssClass: strings.join(' ', 'view-tab-box', BenchColumn.TAB_BOX_CLASSES[i]),
        controller: scout.create(DesktopTabBoxController)
      }) as SimpleTabBox<OutlineContent>;
      tabBox.setLayoutData(rowLayoutDatas[i]);
      tabBox.on('viewAdd', this._viewAddHandler);
      tabBox.on('viewRemove', this._viewRemoveHandler);
      tabBox.on('viewActivate', this._viewActivateHandler);
      tabBox.on('viewDeactivate', this._viewDeactivateHandler);
      this.tabBoxes.push(tabBox);
    }
  }

  _revalidateSplitters() {
    // remove old splitters
    if (this.components) {
      this.components.forEach(comp => {
        if (comp instanceof Splitter) {
          comp.destroy();
        }
      });
    }
    this.components = this.visibleTabBoxes()
      .reduce((arr: (SimpleTabBox<OutlineContent> | Splitter)[], col: SimpleTabBox<OutlineContent>) => {
        if (arr.length > 0) {
          // add sep
          let splitter = scout.create(Splitter, {
            parent: this,
            $anchor: arr[arr.length - 1].$container,
            $root: this.$container,
            splitHorizontal: false
          });
          splitter.render();
          splitter.setLayoutData(FlexboxLayoutData.fixed().withOrder(this._getTabBoxLayoutData(col).order - 1));
          splitter.$container.addClass('line');
          arr.push(splitter);
        }
        arr.push(col);
        return arr;
      }, []);
    // well order the dom elements (reduce is used for simple code reasons, the result of reduce is not of interest).
    this.components
      .filter(comp => comp instanceof SimpleTabBox)
      .reduce((c1, c2, index) => {
        if (index > 0) {
          c2.$container.insertAfter(c1.$container);
        }
        return c2;
      }, undefined);
    this._updateSplitterMovable();
  }

  _getTabBoxLayoutData(tabBox: SimpleTabBox): FlexboxLayoutData {
    return tabBox.getLayoutData() as FlexboxLayoutData;
  }

  protected _updateSplitterMovable() {
    if (!this.components) {
      return;
    }
    this.components.forEach((c, i) => {
      if (c instanceof Splitter) {
        // noinspection JSVoidFunctionReturnValueUsed
        let componentsBefore = this.components.slice(0, i).reverse() as SimpleTabBox<OutlineContent>[];
        let componentsAfter = this.components.slice(i + 1) as SimpleTabBox<OutlineContent>[];
        // shrink
        if (componentsBefore.filter(tab => this._getTabBoxLayoutData(tab).shrink > 0).length > 0
          && componentsAfter.filter(tab => this._getTabBoxLayoutData(tab).grow > 0).length > 0) {
          c.setEnabled(true);
          c.on('move', this._onSplitterMove.bind(this));
          return;
        }
        // grow
        if (componentsBefore.filter(c => this._getTabBoxLayoutData(c).grow > 0).length > 0
          && componentsAfter.filter(c => this._getTabBoxLayoutData(c).shrink > 0).length > 0) {
          c.setEnabled(true);
          c.on('move', this._onSplitterMove.bind(this));
          return;
        }
        c.setEnabled(false);
      }
    });
  }

  protected _onSplitterMove(event: SplitterMoveEvent) {
    let splitter = event.source;
    // noinspection UnnecessaryLocalVariableJS
    let diff = event.position - splitter.htmlComp.location().y - splitter.htmlComp.margins().top - splitter.htmlComp.insets().top;
    (splitter.getLayoutData() as FlexboxLayoutData).diff = diff;
    this.revalidateLayout();
    (splitter.getLayoutData() as FlexboxLayoutData).diff = null;
    event.preventDefault();
  }

  addView(view: OutlineContent, bringToFront?: boolean) {
    let tabBox = this.getTabBox(view.displayViewId);
    this._widgetToTabBox[view.id] = tabBox;

    tabBox.addView(view, bringToFront);

    if (this.rendered && tabBox.viewCount() === 1) {
      if (!tabBox.rendered) {
        // lazy render if the first view is added.
        tabBox.render();
      }
      this._revalidateSplitters();
      this.updateFirstLastMarker();
      let layout = this.htmlComp.layout as FlexboxLayout;
      layout.reset();
      this.htmlComp.invalidateLayoutTree();
    }
  }

  getTabBox(displayViewId: DisplayViewId): SimpleTabBox<OutlineContent> {
    let tabBox: SimpleTabBox<OutlineContent>;
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

  removeView(view: OutlineContent, showSiblingView?: boolean) {
    let tabBox = this._widgetToTabBox[view.id];
    if (tabBox) {
      this._removeViewInProgress++;
      tabBox.removeView(view, showSiblingView);
      this._removeViewInProgress--;
      delete this._widgetToTabBox[view.id];
      if (this.rendered && tabBox.viewCount() === 0 && this._removeViewInProgress === 0) {
        // remove view area if no view is left.
        tabBox.remove();
        this._revalidateSplitters();
        this.updateFirstLastMarker();
        let layout = this.htmlComp.layout as FlexboxLayout;
        layout.reset();
        this.htmlComp.invalidateLayoutTree();
      }
    }
  }

  viewCount(): number {
    return this.tabBoxes
      .map(tabBox => tabBox.viewCount())
      .reduce((c1, c2) => c1 + c2, 0);
  }

  hasView(view: OutlineContent): boolean {
    return this.tabBoxes
      .filter(tabBox => tabBox.hasView(view))
      .length > 0;
  }

  hasViews(): boolean {
    return this.viewCount() > 0;
  }

  getViews(displayViewId?: string): OutlineContent[] {
    return this.tabBoxes.reduce((arr: OutlineContent[], tabBox) => {
      arrays.pushAll(arr, tabBox.getViews(displayViewId));
      return arr;
    }, []);
  }

  getComponents(): (SimpleTabBox<OutlineContent> | Splitter)[] {
    return this.components;
  }

  visibleTabBoxes(): SimpleTabBox<OutlineContent>[] {
    return this.tabBoxes.filter(tabBox => tabBox.hasViews());
  }

  updateFirstLastMarker() {
    widgets.updateFirstLastMarker(this.visibleTabBoxes());
  }
}
