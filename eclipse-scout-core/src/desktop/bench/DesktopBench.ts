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
  aria, arrays, BenchColumn, BenchColumnLayoutData, BenchColumnViewActivateEvent, BenchColumnViewAddEvent, BenchColumnViewDeactivateEvent, BenchColumnViewRemoveEvent, BenchRowLayoutData, CollapseHandleActionEvent, Desktop,
  DesktopBenchEventMap, DesktopBenchModel, DesktopNavigationHandle, DesktopTab, DesktopTabArea, DesktopTabSelectKeyStroke, DisplayViewId, Event, EventHandler, FlexboxLayout, FlexboxLayoutData, Form, HeaderTabBoxController, HtmlComponent,
  InitModelOf, KeyStrokeContext, Outline, OutlineOverview, OutlinePageChangedEvent, Page, PropertyChangeEvent, scout, SimpleTab, SimpleTabBox, Splitter, SplitterMoveEvent, styles, Table, TreeNodesSelectedEvent, Widget, widgets
} from '../../index';
import $ from 'jquery';

export class DesktopBench extends Widget implements DesktopBenchModel {
  declare model: DesktopBenchModel;
  declare eventMap: DesktopBenchEventMap;
  declare self: DesktopBench;

  desktop: Desktop;
  outline: Outline;
  outlineContent: OutlineContent;
  navigationHandle: DesktopNavigationHandle;
  headerTabArea: DesktopTabArea;
  columns: BenchColumn[];
  components: (BenchColumn | Splitter)[];
  tabBoxMap: Record<string /* viewId */, BenchColumn>;
  layoutData: BenchColumnLayoutData;
  headerTabAreaController: HeaderTabBoxController;
  changingCounter: number;
  changed: boolean;
  outlineContentVisible: boolean;
  navigationHandleVisible: boolean;
  layoutCacheKey: string[];
  desktopKeyStrokeContext: KeyStrokeContext;

  protected _removeViewInProgress: number;
  protected _updateOutlineContentTimeout: number;
  protected _desktopOutlineChangeHandler: EventHandler<Event<Desktop>>;
  protected _desktopPropertyChangeHandler: EventHandler<PropertyChangeEvent<any, Desktop>>;
  protected _desktopAnimationEndHandler: EventHandler<Event<Desktop>>;

  protected _outlineNodesSelectedHandler: EventHandler<TreeNodesSelectedEvent>;
  protected _outlinePageChangedHandler: EventHandler<OutlinePageChangedEvent>;
  protected _outlinePropertyChangeHandler: EventHandler<PropertyChangeEvent<any, Outline>>;
  protected _outlineContentDestroyHandler: EventHandler<Event<Widget>>;
  protected _outlineContentCssClassChangeHandler: EventHandler<PropertyChangeEvent<string, Widget>>;

  protected _viewAddHandler: EventHandler<BenchColumnViewAddEvent>;
  protected _viewRemoveHandler: EventHandler<BenchColumnViewRemoveEvent>;
  protected _viewActivateHandler: EventHandler<BenchColumnViewActivateEvent>;
  protected _viewDeactivateHandler: EventHandler<BenchColumnViewDeactivateEvent>;

  constructor() {
    super();
    this.columns = [];
    this.components = null;
    this.tabBoxMap = {};
    this._removeViewInProgress = 0;
    this.changingCounter = 0;
    this.changed = false;
    this.layoutCacheKey = [];

    this._desktopOutlineChangeHandler = this._onDesktopOutlineChange.bind(this);
    this._desktopPropertyChangeHandler = this._onDesktopPropertyChange.bind(this);
    this._desktopAnimationEndHandler = this._onDesktopAnimationEnd.bind(this);

    this._outlineNodesSelectedHandler = this._onOutlineNodesSelected.bind(this);
    this._outlinePageChangedHandler = this._onOutlinePageChanged.bind(this);
    this._outlinePropertyChangeHandler = this._onOutlinePropertyChange.bind(this);
    this._outlineContentDestroyHandler = this._onOutlineContentDestroy.bind(this);

    this._outlineContentCssClassChangeHandler = this._onOutlineContentCssClassChange.bind(this);
    this._viewAddHandler = this._onViewAdd.bind(this);
    this._viewRemoveHandler = this._onViewRemove.bind(this);
    this._viewActivateHandler = this._onViewActivate.bind(this);
    this._viewDeactivateHandler = this._onViewDeactivate.bind(this);
  }

  static VIEW_MIN_HEIGHT = null; // Configured in sizes.css
  static VIEW_MIN_WIDTH = null; // Configured in sizes.css

  static VIEW_AREA_COLUMN_INDEX = {
    LEFT: 0,
    CENTER: 1,
    RIGHT: 2
  } as const;

  static VIEW_AREA_COLUMN_CLASSES = [
    'west',
    'center',
    'east'
  ] as const;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    DesktopBench.VIEW_MIN_HEIGHT = $.pxToNumber(styles.get('view-tab-box', 'min-height').minHeight);
    DesktopBench.VIEW_MIN_WIDTH = $.pxToNumber(styles.get('view-tab-box', 'min-width').minWidth);

    this.desktop = this.session.desktop;

    this.setLayoutData(this.desktop.benchLayoutData);
    this._createColumns();
    this.headerTabArea = model.headerTabArea;
    // controller for headerTabArea
    if (this.headerTabArea) {
      this.headerTabAreaController = scout.create(HeaderTabBoxController);
      this.headerTabAreaController.install(this, this.headerTabArea);
    }
    this.outlineContentVisible = scout.nvl(model.outlineContentVisible, true);
    this.setOutline(this.desktop.outline);
    this.updateNavigationHandleVisibility();
  }

  /** @internal */
  _setTabArea(headerTabArea: DesktopTabArea) {
    this.headerTabArea = headerTabArea;
    if (this.headerTabAreaController) {
      this.headerTabAreaController.install(this, this.headerTabArea);
      // for all views
      let tabBox = this.getTabBox('C');
      tabBox.viewStack.slice().reverse().forEach(view => {
        // @ts-expect-error
        this.headerTabAreaController._onViewAdd({view: view});
        if (tabBox.currentView === view) {
          // @ts-expect-error
          this.headerTabAreaController._onViewActivate({view: view});
        }
      });
      // ensure the correct view tab area is visible (header or center part)
      this.headerTabAreaController._onViewsChanged();
    }
  }

  protected _createColumns() {
    let layoutData = this.getLayoutData(),
      columnLayoutData: BenchRowLayoutData[] = [];

    if (layoutData) {
      columnLayoutData = this.getLayoutData().getColumns();
    }
    for (let i = 0; i < 3; i++) {
      let cacheKey = this.layoutCacheKey.slice();
      if (cacheKey.length > 0) {
        cacheKey.push('column' + i);
      }
      let column = scout.create(BenchColumn, {
        parent: this,
        layoutData: columnLayoutData[i],
        cacheKey: cacheKey,
        cssClass: DesktopBench.VIEW_AREA_COLUMN_CLASSES[i]
      });
      column.on('viewAdd', this._viewAddHandler);
      column.on('viewRemove', this._viewRemoveHandler);
      column.on('viewActivate', this._viewActivateHandler);
      column.on('viewDeactivate', this._viewDeactivateHandler);
      this.columns.push(column);
    }
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    // Bound to desktop
    this.desktopKeyStrokeContext = new KeyStrokeContext();
    this.desktopKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
    this.desktopKeyStrokeContext.$bindTarget = this.desktop.$container;
    this.desktopKeyStrokeContext.$scopeTarget = this.desktop.$container;
    this.desktopKeyStrokeContext.registerKeyStroke(new DesktopTabSelectKeyStroke(this.desktop));
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('desktop-bench');
    aria.role(this.$container, 'main');
    aria.label(this.$container, this.session.text('ui.MainArea'));
    this.htmlComp = HtmlComponent.install(this.$container, this.session);

    this.htmlComp.setLayout(this._createLayout());
    this.htmlComp.layoutData = this.getLayoutData();

    this._renderColumns();
    this._revalidateSplitters();
    this._renderNavigationHandleVisible();

    this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
    this.desktop.on('propertyChange', this._desktopPropertyChangeHandler);
    this.desktop.on('outlineChange', this._desktopOutlineChangeHandler);
    this.desktop.on('animationEnd', this._desktopAnimationEndHandler);
  }

  protected _createLayout(): FlexboxLayout {
    return new FlexboxLayout(FlexboxLayout.Direction.ROW, this.layoutCacheKey);
  }

  visibleColumns(): BenchColumn[] {
    return this.columns.filter(column => column.hasViews());
  }

  protected _renderColumns() {
    this.visibleColumns().forEach(column => this._renderColumn(column));
    this.updateFirstLastMarker();
  }

  protected _renderColumn(column: BenchColumn) {
    if (!column || column.rendered) {
      return;
    }
    column.render();
  }

  protected override _remove() {
    this.desktop.off('propertyChange', this._desktopPropertyChangeHandler);
    this.desktop.off('outlineChange', this._desktopOutlineChangeHandler);
    this.desktop.off('animationEnd', this._desktopAnimationEndHandler);
    this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
    super._remove();
  }

  updateFirstLastMarker() {
    widgets.updateFirstLastMarker(this.visibleColumns());
  }

  protected _renderOutlineContent() {
    if (!this.outlineContent) {
      return;
    }

    // Reset view tab relevant properties to make sure no tab is visible for the outline content
    // @ts-expect-error
    delete this.outlineContent.title;
    // @ts-expect-error
    delete this.outlineContent.subTitle;
    // @ts-expect-error
    delete this.outlineContent.iconId;

    // bring the view to top if the desktop is not in background.
    this.addView(this.outlineContent, !this.desktop.inBackground);

    if (this.desktop.rendered) {
      // Request focus on first element in outline content
      this.session.focusManager.validateFocus();
    }
  }

  protected _removeOutlineContent() {
    if (!this.outlineContent) {
      return;
    }
    this.removeView(this.outlineContent, false);
  }

  protected _createNavigationHandle(): DesktopNavigationHandle {
    return scout.create(DesktopNavigationHandle, {
      parent: this,
      leftVisible: false
    });
  }

  protected _renderNavigationHandle() {
    if (this.navigationHandle) {
      return;
    }
    this.navigationHandle = this._createNavigationHandle();
    this.navigationHandle.render();
    this.navigationHandle.addCssClass('navigation-closed');
    this.navigationHandle.on('action', this._onNavigationHandleAction.bind(this));
  }

  protected _removeNavigationHandle() {
    if (!this.navigationHandle) {
      return;
    }
    this.navigationHandle.destroy();
    this.navigationHandle = null;
  }

  protected _renderNavigationHandleVisible() {
    if (this.navigationHandleVisible) {
      this._renderNavigationHandle();
    } else {
      this._removeNavigationHandle();
    }
    this.$container.toggleClass('has-navigation-handle', this.navigationHandleVisible);
  }

  /**
   * is called in post render of desktop used to initialize the ui state. E.g. show default views
   */
  postRender() {
    this.columns.forEach(column => column.postRender());
  }

  setChanging(changing: boolean) {
    if (changing) {
      this.changingCounter++;
    } else {
      this.changingCounter--;
    }
    if (this.changingCounter === 0 && this.changed && this.rendered) {
      let layout = this.htmlComp.layout as FlexboxLayout;
      layout.reset();
      this.htmlComp.invalidateLayoutTree();
      this.changed = false;
    }
  }

  updateLayoutData(layoutData: BenchColumnLayoutData) {
    if (this.getLayoutData() === layoutData) {
      return;
    }
    this.setLayoutData(layoutData);

    // update columns
    let columnDatas = layoutData.getColumns();

    this.columns.forEach((c, i) => {
      let cacheKey: string[];
      if (this.layoutCacheKey && this.layoutCacheKey.length > 0) {
        cacheKey = this.layoutCacheKey.slice();
        cacheKey.push('column' + i);
      }
      c.updateLayoutData(columnDatas[i], cacheKey);
    });
    if (this.rendered) {
      let layout = this.htmlComp.layout as FlexboxLayout;
      layout.setCacheKey(this.layoutCacheKey);
      layout.reset();
      this.htmlComp.invalidateLayoutTree();
    }
    this._updateSplitterMovable();
  }

  override setLayoutData(layoutData: BenchColumnLayoutData) {
    if (this.layoutData === layoutData) {
      return;
    }
    super.setLayoutData(layoutData);
    this.layoutData = layoutData;
    this.layoutCacheKey = [];
    if (layoutData['cacheKey']) {
      this.layoutCacheKey.push(layoutData['cacheKey']);
    }
  }

  getLayoutData(): BenchColumnLayoutData {
    return this.layoutData;
  }

  setNavigationHandleVisible(visible: boolean) {
    this.setProperty('navigationHandleVisible', visible);
  }

  setOutline(outline: Outline) {
    if (this.outline) {
      this.outline.off('nodesSelected', this._outlineNodesSelectedHandler);
      this.outline.off('pageChanged', this._outlinePageChangedHandler);
      this.outline.off('propertyChange', this._outlinePropertyChangeHandler);
    }
    this._setProperty('outline', outline);
    if (this.outline) {
      this.outline.on('nodesSelected', this._outlineNodesSelectedHandler);
      this.outline.on('pageChanged', this._outlinePageChangedHandler);
      this.outline.on('propertyChange', this._outlinePropertyChangeHandler);
    }
    this.updateOutlineContent();
  }

  setOutlineContent(content: OutlineContent) {
    if (this.outlineContent === content) {
      return;
    }
    let oldContent = this.outlineContent;
    if (oldContent) {
      oldContent.off('destroy', this._outlineContentDestroyHandler);
      oldContent.off('propertyChange:cssClass', this._outlineContentCssClassChangeHandler);
    }
    if (this.rendered) {
      this._removeOutlineContent();
    }
    // add a destroy listener to the outline-content, so we can reset the property - otherwise we'd work
    // with a potentially destroyed content which would cause an error later, when we try to render the
    // bench with the outline-content.
    if (content) {
      content.one('destroy', this._outlineContentDestroyHandler);
      content.on('propertyChange:cssClass', this._outlineContentCssClassChangeHandler);
    }

    this._setProperty('outlineContent', content);

    // Inform header that outline content has changed
    // (having a listener in the header is quite complex due to initialization phase, a direct call here is much easier to implement)
    if (this.desktop.header) {
      this.desktop.header.onBenchOutlineContentChange(content, oldContent);
    }

    this._updateOutlineContentHasDimmedBackground();

    this._renderOutlineContent();
  }

  setOutlineContentVisible(visible: boolean) {
    if (visible === this.outlineContentVisible) {
      return;
    }
    this._setProperty('outlineContentVisible', visible);
    this.updateOutlineContent();
  }

  bringToFront() {
    if (!this.outlineContent) {
      return;
    }
    this._renderOutlineContent();
  }

  sendToBack() {
    // nop
  }

  protected _updateOutlineContentHasDimmedBackground() {
    if (!this.outlineContent) {
      return;
    }
    let hasDimmedBackground = false;
    if (this.outlineContent.cssClass) {
      hasDimmedBackground = this.outlineContent.cssClass.indexOf('dimmed-background') > -1;
    }
    this.toggleCssClass('outline-content-has-dimmed-background', hasDimmedBackground);
  }

  protected _computeDetailContentForPage(node: Page): Form | Table {
    if (!node) {
      throw new Error('called _showDetailContentForPage without node');
    }

    let content: Form | Table;
    if (node.detailForm && node.detailFormVisible && node.detailFormVisibleByUi) {
      content = node.detailForm;
      content.uiCssClass = 'detail-form';
    } else if (node.detailTable && node.detailTableVisible) {
      content = node.detailTable;
      content.uiCssClass = 'detail-table perma-focus';
    }

    return content;
  }

  updateOutlineContent() {
    if (!this.outlineContentVisible || !this.outline) {
      return;
    }
    let content: OutlineContent,
      selectedPage = this.outline.selectedNode();
    if (selectedPage) {
      // Outline does not support multi selection
      content = this._computeDetailContentForPage(selectedPage);
    } else {
      content = this.outline.getRootContent();
    }
    if (content) {
      if (content instanceof Table) {
        content.menuBar.addCssClass('main-menubar');
        content.menuBar.removeCssClass('bounded');
      }
      content.displayViewId = 'C';
    }
    this.setOutlineContent(content);
  }

  updateOutlineContentDebounced() {
    clearTimeout(this._updateOutlineContentTimeout);
    this._updateOutlineContentTimeout = setTimeout(() => this.updateOutlineContent(), 300);
  }

  updateNavigationHandleVisibility() {
    // Don't show handle if desktop says handle must not be visible
    // Only show handle if navigation is invisible
    this.setNavigationHandleVisible(this.desktop.navigationHandleVisible && !this.desktop.navigationVisible);
  }

  protected _onDesktopOutlineChange(event: Event<Desktop>) {
    this.setOutline(this.desktop.outline);
    this.updateNavigationHandleVisibility();
  }

  protected _onOutlineContentDestroy(event: Event<Widget>) {
    if (event.source === this.outlineContent) {
      this.setOutlineContent(null);
    }
  }

  protected _onOutlineContentCssClassChange(event: PropertyChangeEvent<string, Widget>) {
    this._updateOutlineContentHasDimmedBackground();
  }

  protected _onOutlineNodesSelected(event: TreeNodesSelectedEvent) {
    if (event.debounce) {
      this.updateOutlineContentDebounced();
    } else {
      this.updateOutlineContent();
    }
  }

  protected _onOutlinePageChanged(event: OutlinePageChangedEvent) {
    let selectedPage = this.outline.selectedNode();
    if (!event.page && !selectedPage || event.page === selectedPage) {
      this.updateOutlineContent();
    }
  }

  protected _onOutlinePropertyChange(event: PropertyChangeEvent<any, Outline>) {
    if (scout.isOneOf(event.propertyName, ['defaultDetailForm', 'outlineOverview'])) {
      this.updateOutlineContent();
    }
  }

  protected _onDesktopNavigationVisibleChange() {
    // If navigation gets visible: Hide handle immediately
    // If navigation gets hidden using animation: Show handle when animation ends
    if (this.desktop.navigationVisible) {
      this.updateNavigationHandleVisibility();
    }
  }

  protected _onDesktopNavigationHandleVisibleChange() {
    this.updateNavigationHandleVisibility();
  }

  protected _onDesktopAnimationEnd(event: Event<Desktop>) {
    if (!this.desktop.navigationVisible) {
      this.updateNavigationHandleVisibility();
    }
  }

  protected _onBenchLayoutDataChange() {
    this.updateLayoutData(this.desktop.benchLayoutData);
  }

  protected _onDesktopPropertyChange(event: PropertyChangeEvent<any, Desktop>) {
    if (event.propertyName === 'navigationVisible') {
      this._onDesktopNavigationVisibleChange();
    } else if (event.propertyName === 'navigationHandleVisible') {
      this._onDesktopNavigationHandleVisibleChange();
    }
    if (event.propertyName === 'benchLayoutData') {
      this._onBenchLayoutDataChange();
    }
  }

  protected _onNavigationHandleAction(event: CollapseHandleActionEvent) {
    this.desktop.enlargeNavigation();
  }

  protected _revalidateSplitters() {
    // remove old splitters
    if (this.components) {
      this.components.forEach(comp => {
        if (comp instanceof Splitter) {
          comp.destroy();
        }
      });
    }
    this.components = this.visibleColumns()
      .reduce((arr, col) => {
        if (arr.length > 0) {
          // add sep
          let splitter = scout.create(Splitter, {
            parent: this,
            $anchor: arr[arr.length - 1].$container,
            $root: this.$container
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
    this.components
      .filter(comp => comp instanceof BenchColumn)
      .reduce((c1: BenchColumn, c2: BenchColumn, index) => {
        if (index > 0) {
          c2.$container.insertAfter(c1.$container);
        }
        return c2;
      }, undefined);
    this._updateSplitterMovable();
  }

  protected _updateSplitterMovable() {
    if (!this.components) {
      return;
    }
    this.components.forEach((c, i) => {
      if (c instanceof Splitter) {
        let componentsBefore = this.components.slice(0, i).reverse() as BenchColumn[];
        let componentsAfter = this.components.slice(i + 1) as BenchColumn[];
        // shrink
        if (componentsBefore.filter(col => col.getLayoutData().shrink > 0).length > 0 && componentsAfter.filter(c => c.getLayoutData().grow > 0).length > 0) {
          c.setEnabled(true);
          c.on('move', this._onSplitterMove.bind(this));
          return;
        }
        // grow
        if (componentsBefore.filter(c => c.getLayoutData().grow > 0).length > 0 && componentsAfter.filter(c => c.getLayoutData().shrink > 0).length > 0) {
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
    let diff = event.position - splitter.htmlComp.location().x - splitter.htmlComp.margins().left - splitter.htmlComp.insets().left;
    (splitter.getLayoutData() as FlexboxLayoutData).diff = diff;
    this.revalidateLayout();
    (splitter.getLayoutData() as FlexboxLayoutData).diff = null;
    event.preventDefault();
  }

  protected _onViewAdd(event: BenchColumnViewAddEvent) {
    this.trigger('viewAdd', {
      view: event.view
    });
  }

  protected _onViewRemove(event: BenchColumnViewRemoveEvent) {
    this.trigger('viewRemove', {
      view: event.view
    });
  }

  protected _onViewActivate(event: BenchColumnViewActivateEvent) {
    let view = event.view;
    if (this.outlineContent === view) {
      this.desktop.bringOutlineToFront();
    }
    this.trigger('viewActivate', {
      view: view
    });
  }

  protected _onViewDeactivate(event: BenchColumnViewDeactivateEvent) {
    if (this.outlineContent === event.view) {
      this.desktop.sendOutlineToBack();
    }
    this.trigger('viewDeactivate', {
      view: event.view
    });
  }

  addView(view: OutlineContent, activate?: boolean) {
    view.displayViewId = DesktopBench.normalizeDisplayViewId(view.displayViewId);
    let column = this._getColumn(view.displayViewId);
    this.tabBoxMap[view.id] = column;
    column.addView(view, activate);

    if (this.rendered) {
      if (column.viewCount() === 1) {
        this._renderColumn(column);
        this._revalidateSplitters();
        this.updateFirstLastMarker();
        if (this.changingCounter > 0) {
          this.changed = true;
        } else {
          let layout = this.htmlComp.layout as FlexboxLayout;
          layout.reset();
          this.htmlComp.invalidateLayoutTree();
        }
      }
    }
  }

  activateView(view: OutlineContent) {
    // activate views is only for existing views allowed.
    if (!this.hasView(view)) {
      return;
    }
    let column = this._getColumn(view.displayViewId);
    if (column) {
      column.activateView(view);
    }
  }

  protected _getColumn(displayViewId: DisplayViewId): BenchColumn {
    let column: BenchColumn;
    switch (displayViewId) {
      case 'NW':
      case 'W':
      case 'SW':
        column = this.columns[DesktopBench.VIEW_AREA_COLUMN_INDEX.LEFT];
        break;
      case 'NE':
      case 'E':
      case 'SE':
        column = this.columns[DesktopBench.VIEW_AREA_COLUMN_INDEX.RIGHT];
        break;
      default:
        column = this.columns[DesktopBench.VIEW_AREA_COLUMN_INDEX.CENTER];
        break;
    }
    return column;
  }

  removeView(view: OutlineContent, showSiblingView?: boolean) {
    let column = this.tabBoxMap[view.id];
    if (column) {
      this._removeViewInProgress++;
      column.removeView(view, showSiblingView);
      this._removeViewInProgress--;
      delete this.tabBoxMap[view.id];
      // remove if empty
      if (this.rendered && column.viewCount() === 0 && this._removeViewInProgress === 0) {
        column.remove();
        this._revalidateSplitters();
        this.updateFirstLastMarker();
        if (this.changingCounter > 0) {
          this.changed = true;
        } else {
          let layout = this.htmlComp.layout as FlexboxLayout;
          layout.reset();
          this.htmlComp.invalidateLayoutTree();
        }
      }
    }
  }

  getComponents(): (BenchColumn | Splitter)[] {
    return this.components;
  }

  getTabBox(displayViewId: DisplayViewId): SimpleTabBox<OutlineContent> {
    let viewColumn = this._getColumn(displayViewId);
    if (!viewColumn) {
      return;
    }
    return viewColumn.getTabBox(displayViewId);
  }

  visibleTabBoxes(): SimpleTabBox<OutlineContent>[] {
    return this.visibleColumns().reduce((arr, column) => {
      arrays.pushAll(arr, column.visibleTabBoxes());
      return arr;
    }, []);
  }

  hasView(view: OutlineContent): boolean {
    return this.columns
      .filter(column => column.hasView(view))
      .length > 0;
  }

  getViews(displayViewId?: string): OutlineContent[] {
    return this.columns.reduce((arr, column) => {
      arrays.pushAll(arr, column.getViews(displayViewId));
      return arr;
    }, []);
  }

  getViewTab(view: OutlineContent): SimpleTab<OutlineContent> {
    let viewTab: SimpleTab<OutlineContent> = null;
    this.getTabs().some(vt => {
      if (vt.view === view) {
        viewTab = vt;
        return true;
      }
      return false;
    });
    return viewTab;
  }

  getTabs(): DesktopTab[] {
    let tabs: DesktopTab[] = [];
    // consider right order
    tabs = tabs.concat(this._getTabsForDisplayViewId('NW'));
    tabs = tabs.concat(this._getTabsForDisplayViewId('W'));
    tabs = tabs.concat(this._getTabsForDisplayViewId('SW'));
    tabs = tabs.concat(this._getTabsForDisplayViewId('N'));
    if (this.headerTabAreaController) {
      tabs = tabs.concat(this.headerTabAreaController.getTabs());
    } else {
      tabs = tabs.concat(this._getTabsForDisplayViewId('C'));
    }
    tabs = tabs.concat(this._getTabsForDisplayViewId('S'));
    tabs = tabs.concat(this._getTabsForDisplayViewId('NE'));
    tabs = tabs.concat(this._getTabsForDisplayViewId('E'));
    tabs = tabs.concat(this._getTabsForDisplayViewId('SE'));
    return tabs;
  }

  _getTabsForDisplayViewId(displayViewId: DisplayViewId): DesktopTab[] {
    return this.getTabBox(displayViewId).getController().getTabs() as DesktopTab[];
  }

  /**
   * @returns all the currently active views (the selected ones) of all the visible tab boxes
   */
  activeViews(): OutlineContent[] {
    let activeViews: OutlineContent[] = [];
    this.visibleColumns().forEach(column => {
      column.visibleTabBoxes().forEach(tabBox => {
        activeViews.push(tabBox.currentView);
      });
    });
    return activeViews;
  }

  static normalizeDisplayViewId(displayViewId: DisplayViewId): DisplayViewId {
    switch (displayViewId) {
      case 'NW':
      case 'W':
      case 'SW':
      case 'N':
      case 'C':
      case 'S':
      case 'NE':
      case 'E':
      case 'SE':
        break;
      default:
        // map all other displayViewIds to center
        displayViewId = 'C';
        break;
    }
    return displayViewId;
  }
}

export type OutlineContent = Form | Table | OutlineOverview;
