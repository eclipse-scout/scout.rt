import Widget from '../widget/Widget';
import * as scout from '../scout';
import HtmlComponent from '../layout/HtmlComponent';
import FlexboxLayout, {Direction} from '../layout/FlexboxLayout';
import FlexboxLayoutData from '../layout/FlexboxLayoutData';
import BenchColumn from './BenchColumn';
import HeaderTabBoxController from './HeaderTabBoxController';
import * as arrays from '../utils/arrays';
import Splitter from '../splitter/Splitter';
import Table from '../table/Table';

export const VIEW_MIN_HEIGHT = 50;
export const VIEW_MIN_WIDTH = 50;
export const VIEW_AREA_COLUMN_INDEX = Object.freeze({
  LEFT: 0,
  CENTER: 1,
  RIGHT: 2
});

export const VIEW_AREA_COLUMN_CLASSES = Object.freeze([
  'west',
  'center',
  'east'
]);

export default class DesktopBench extends Widget {

  constructor() {
    super();
    this.htmlComp;
    this.columns = [];
    this.components;
    this.tabBoxMap = {}; // [key=viewId, value=SimpleTabBox instance]
    this._removeViewInProgress = 0;
    this.changingCounter = 0;
    this.changed = false;
    this.layoutCacheKey = [];

    this._desktopOutlineChangeHandler = this._onDesktopOutlineChange.bind(this);
    this._desktopPropertyChangeHandler = this._onDesktopPropertyChange.bind(this);
    this._outlineNodesSelectedHandler = this._onOutlineNodesSelected.bind(this);
    this._outlinePageChangedHandler = this._onOutlinePageChanged.bind(this);
    this._outlinePropertyChangeHandler = this._onOutlinePropertyChange.bind(this);
    this._outlineContentDestroyHandler = this._onoutlineContentDestroy.bind(this);

    // event listener functions
    this._viewAddHandler = this._onViewAdd.bind(this);
    this._viewRemoveHandler = this._onViewRemove.bind(this);
    this._viewActivateHandler = this._onViewActivate.bind(this);
    this._viewDeactivateHandler = this._onViewDeactivate.bind(this);

    this._desktopAnimationEndHandler = this._onDesktopAnimationEnd.bind(this);
  }

  _init(model) {
    super._init(model);

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
  };

  _createColumns() {
    var layoutData = this.getLayoutData(),
      columnLayoutData = [];

    if (layoutData) {
      columnLayoutData = this.getLayoutData().getColumns();
    }
    for (var i = 0; i < 3; i++) {
      var cacheKey = this.layoutCacheKey.slice();
      if (cacheKey.length > 0) {
        cacheKey.push('column' + i);
      }
      var column = scout.create(BenchColumn, {
        parent: this,
        layoutData: columnLayoutData[i],
        cacheKey: cacheKey,
        cssClass: VIEW_AREA_COLUMN_CLASSES[i]
      });
      column.on('viewAdd', this._viewAddHandler);
      column.on('viewRemove', this._viewRemoveHandler);
      column.on('viewActivate', this._viewActivateHandler);
      column.on('viewDeactivate', this._viewDeactivateHandler);
      this.columns.push(column);
    }
  };

  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    // Bound to desktop
    /*this.desktopKeyStrokeContext = new scout.KeyStrokeContext();
    this.desktopKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
    this.desktopKeyStrokeContext.$bindTarget = this.desktop.$container;
    this.desktopKeyStrokeContext.$scopeTarget = this.desktop.$container;
    this.desktopKeyStrokeContext.registerKeyStroke(new scout.DesktopTabSelectKeyStroke(this.desktop));*/
  };

  _render() {
    this.$container = this.$parent.appendDiv('desktop-bench');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);

    this.htmlComp.setLayout(this._createLayout());
    this.htmlComp.layoutData = this.getLayoutData();

    this._renderColumns();
    this._revalidateSplitters();
    this._renderNavigationHandleVisible();

    //this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
    this.desktop.on('propertyChange', this._desktopPropertyChangeHandler);
    this.desktop.on('outlineChange', this._desktopOutlineChangeHandler);
    this.desktop.on('animationEnd', this._desktopAnimationEndHandler);
  };

  _createLayout() {
    return new FlexboxLayout(Direction.ROW, this.layoutCacheKey);
  };

  visibleColumns() {
    return this.columns.filter(function(column) {
      return column.hasViews();
    });
  };

  _renderColumns() {
    this.visibleColumns().forEach(function(column) {
      this._renderColumn(column);
    }, this);
    this.updateFirstLastMarker();
  };

  _renderColumn(column) {
    if (!column || column.rendered) {
      return;
    }
    column.render();
  };

  _remove() {
    this.desktop.off('propertyChange', this._desktopPropertyChangeHandler);
    this.desktop.off('outlineChange', this._desktopOutlineChangeHandler);
    this.desktop.off('animationEnd', this._desktopAnimationEndHandler);
    //this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
    super._remove();
  };

  updateFirstLastMarker() {
    Widget.updateFirstLastMarker(this.visibleColumns());
  };

  _renderOutlineContent() {
    if (!this.outlineContent) {
      return;
    }

    // Reset view tab relevant properties to make sure no tab is visible for the outline content
    delete this.outlineContent.title;
    delete this.outlineContent.subTitle;
    delete this.outlineContent.iconId;

    // bring the view to top if the desktop is not in background.
    this.addView(this.outlineContent, !this.desktop.inBackground);

    if (this.desktop.rendered) {
      // Request focus on first element in outline content
      //this.session.focusManager.validateFocus();
    }

  };

  _removeOutlineContent() {
    if (!this.outlineContent) {
      return;
    }
    this.removeView(this.outlineContent, false);
  };

  _createNavigationHandle() {
    return scout.create('DesktopNavigationHandle', {
      parent: this,
      leftVisible: false
    });
  };

  _renderNavigationHandle() {
    if (this.navigationHandle) {
      return;
    }
    this.navigationHandle = this._createNavigationHandle();
    this.navigationHandle.render();
    this.navigationHandle.addCssClass('navigation-closed');
    this.navigationHandle.on('action', this._onNavigationHandleAction.bind(this));
  };

  _removeNavigationHandle() {
    if (!this.navigationHandle) {
      return;
    }
    this.navigationHandle.destroy();
    this.navigationHandle = null;
  };

  _renderNavigationHandleVisible() {
    if (this.navigationHandleVisible) {
      this._renderNavigationHandle();
    } else {
      this._removeNavigationHandle();
    }
    this.$container.toggleClass('has-navigation-handle', this.navigationHandleVisible);
  };

  /**
   * is called in post render of desktop used to initialize the ui state. E.g. show default views
   */
  postRender() {
    this.columns.forEach(function(column) {
      column.postRender();
    });
  };

  setChanging(changing) {
    if (changing) {
      this.changingCounter++;
    } else {
      this.changingCounter--;
    }
    if (this.changingCounter === 0 && this.changed && this.rendered) {
      this.htmlComp.layout.reset();
      this.htmlComp.invalidateLayoutTree();
      this.htmlComp.validateLayoutTree();
      this.changed = false;
    }
    this.chaningCounter = Math.max(this.changingCounter - 1, 0);
  };

  updateLayoutData(layoutData) {
    if (this.getLayoutData() === layoutData) {
      return;
    }
    this.setLayoutData(layoutData);

    // update columns
    var columnDatas = layoutData.getColumns();

    this.columns.forEach(function(c, i) {
      var cacheKey;
      if (this.layoutCacheKey && this.layoutCacheKey.length > 0) {
        cacheKey = this.layoutCacheKey.slice();
        cacheKey.push('column' + i);
      }
      c.updateLayoutData(columnDatas[i], cacheKey);
    }.bind(this));
    if (this.rendered) {
      this.htmlComp.layout.setCacheKey(this.layoutCacheKey);
      this.htmlComp.layout.reset();
      this.htmlComp.invalidateLayoutTree();
      this.htmlComp.validateLayoutTree();
    }
    this._updateSplitterMovable();
  };

  setLayoutData(layoutData) {
    if (this.layoutData === layoutData) {
      return;
    }
    super.setLayoutData(layoutData);
    this.layoutData = layoutData;
    this.layoutCacheKey = [];
    if (layoutData.cacheKey) {
      this.layoutCacheKey.push(layoutData.cacheKey);
    }
  };

  getLayoutData() {
    return this.layoutData;
  };

  setNavigationHandleVisible(visible) {
    this.setProperty('navigationHandleVisible', visible);
  };

  setOutline(outline) {
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
  };

  setOutlineContent(content) {
    var oldContent = this.outlineContent;
    if (this.outlineContent === content) {
      return;
    }
    if (oldContent) {
      oldContent.off('destroy', this._outlineContentDestroyHandler);
    }
    if (this.rendered) {
      this._removeOutlineContent();
    }
    // add a destroy listener to the outline-content, so we can reset the property - otherwise we'd work
    // with a potentially destroyed content which would cause an error later, when we try to render the
    // bench with the outline-content.
    if (content) {
      content.one('destroy', this._outlineContentDestroyHandler);
    }

    this._setProperty('outlineContent', content);

    // Inform header that outline content has changed
    // (having a listener in the header is quite complex due to initialization phase, a direct call here is much easier to implement)
    if (this.desktop.header) {
      this.desktop.header.onBenchOutlineContentChange(content, oldContent);
    }
    this._renderOutlineContent();
  };

  setOutlineContentVisible(visible) {
    if (visible === this.outlineContentVisible) {
      return;
    }
    this._setProperty('outlineContentVisible', visible);
    this.updateOutlineContent();
  };

  bringToFront() {
    if (!this.outlineContent) {
      return;
    }
    this._renderOutlineContent();
  };

  sendToBack() {
    // nop
  };

  _computeDefaultDetailForm() {
    return this.outline.defaultDetailForm;
  };

  _computeOutlineOverview() {
    return this.outline.outlineOverview;
  };

  _computeDetailContentForPage(node) {
    if (!node) {
      throw new Error('called _showDetailContentForPage without node');
    }

    var content;
    if (node.detailForm && node.detailFormVisible && node.detailFormVisibleByUi) {
      content = node.detailForm;
      content.uiCssClass = 'detail-form';
    } else if (node.detailTable && node.detailTableVisible) {
      content = node.detailTable;
      content.uiCssClass = 'detail-table';
    }

    return content;
  };

  updateOutlineContent() {
    if (!this.outlineContentVisible || !this.outline) {
      return;
    }
    var content,
      selectedPage = this.outline.selectedNode();
    if (selectedPage) {
      // Outline does not support multi selection
      content = this._computeDetailContentForPage(selectedPage);
    } else {
      if (this.outline.defaultDetailForm) {
        content = this._computeDefaultDetailForm();
      } else if (this.outline.outlineOverview) {
        content = this._computeOutlineOverview();
      }
    }
    if (content) {
      if (content instanceof Table) {
        content.menuBar.top();
        content.menuBar.large();
      }
      content.displayViewId = 'C';
    }
    this.setOutlineContent(content);
  };

  updateOutlineContentDebounced() {
    clearTimeout(this._updateOutlineContentTimeout);
    this._updateOutlineContentTimeout = setTimeout(function() {
      this.updateOutlineContent();
    }.bind(this), 300);
  };

  updateNavigationHandleVisibility() {
    // Don't show handle if desktop says handle must not be visible
    // Only show handle if navigation is invisible
    this.setNavigationHandleVisible(this.desktop.navigationHandleVisible && !this.desktop.navigationVisible);
  };

  _onDesktopOutlineChange(event) {
    this.setOutline(this.desktop.outline);
    this.updateNavigationHandleVisibility();
  };

  _onoutlineContentDestroy(event) {
    this.setOutlineContent(null);
  };

  _onOutlineNodesSelected(event) {
    if (event.debounce) {
      this.updateOutlineContentDebounced();
    } else {
      this.updateOutlineContent();
    }
  };

  _onOutlinePageChanged(event) {
    var selectedPage = this.outline.selectedNode();
    if (!event.page && !selectedPage || event.page === selectedPage) {
      this.updateOutlineContent();
    }
  };

  _onOutlinePropertyChange(event) {
    if (scout.isOneOf(event.propertyName, ['defaultDetailForm', 'outlineOverview'])) {
      this.updateOutlineContent();
    }
  };

  _onDesktopNavigationVisibleChange(event) {
    // If navigation gets visible: Hide handle immediately
    // If navigation gets hidden using animation: Show handle when animation ends
    if (this.desktop.navigationVisible) {
      this.updateNavigationHandleVisibility();
    }
  };

  _onDesktopNavigationHandleVisibleChange(event) {
    this.updateNavigationHandleVisibility();
  };

  _onDesktopAnimationEnd(event) {
    if (!this.desktop.navigationVisible) {
      this.updateNavigationHandleVisibility();
    }
  };
  _onBenchLayoutDataChange(event) {
    this.updateLayoutData(this.desktop.benchLayoutData);
  };

  _onDesktopPropertyChange(event) {
    if (event.propertyName === 'navigationVisible') {
      this._onDesktopNavigationVisibleChange();
    } else if (event.propertyName === 'navigationHandleVisible') {
      this._onDesktopNavigationHandleVisibleChange();
    }
    if (event.propertyName === 'benchLayoutData') {
      this._onBenchLayoutDataChange();
    }
  };

  _onNavigationHandleAction(event) {
    this.desktop.enlargeNavigation();
  };

  _revalidateSplitters() {
    // remove old splitters
    if (this.components) {
      this.components.forEach(function(comp) {
        if (comp instanceof Splitter) {
          comp.destroy();
        }
      });
    }
    this.components = this.visibleColumns()
      .reduce(function(arr, col) {
        if (arr.length > 0) {
          // add sep
          var splitter = scout.create('Splitter', {
            parent: this,
            $anchor: arr[arr.length - 1].$container,
            $root: this.$container,
            maxRatio: 1
          });
          splitter.render();
          splitter.setLayoutData(FlexboxLayoutData.fixed().withOrder(col.getLayoutData().order - 1));
          splitter.$container.addClass('line');

          arr.push(splitter);
        }
        arr.push(col);
        return arr;
      }.bind(this), []);
    // well order the dom elements (reduce is used for simple code reasons, the result of reduce is not of interest).
    this.components.filter(function(comp) {
        return comp instanceof BenchColumn;
      })
      .reduce(function(c1, c2, index) {
        if (index > 0) {
          c2.$container.insertAfter(c1.$container);
        }
        return c2;
      }, undefined);
    this._updateSplitterMovable();
  };

  _updateSplitterMovable() {
    if (!this.components) {
      return;
    }
    this.components.forEach(function(c, i) {
      if (c instanceof Splitter) {
        var componentsBefore = this.components.slice(0, i).reverse();
        var componentsAfter = this.components.slice(i + 1);
        // shrink
        if (
          componentsBefore.filter(function(c) {
            return c.getLayoutData().shrink > 0;
          }).length > 0 &&
          componentsAfter.filter(function(c) {
            return c.getLayoutData().grow > 0;
          }).length > 0
        ) {
          c.setEnabled(true);
          c.on('move', this._onSplitterMove.bind(this));
          return;
        }
        // grow
        if (
          componentsBefore.filter(function(c) {
            return c.getLayoutData().grow > 0;
          }).length > 0 &&
          componentsAfter.filter(function(c) {
            return c.getLayoutData().shrink > 0;
          }).length > 0
        ) {
          c.setEnabled(true);
          c.on('move', this._onSplitterMove.bind(this));
          return;
        }
        c.setEnabled(false);

      }
    }.bind(this));
  };

  _onSplitterMove(event) {
    var splitter = event.source;
    var diff = event.position - splitter.htmlComp.location().x - splitter.htmlComp.margins().left - splitter.htmlComp.insets().left;
    splitter.getLayoutData().diff = diff;
    this.revalidateLayout();
    splitter.getLayoutData().diff = null;
    event.preventDefault();
  };

  _onViewAdd(event) {
    this.trigger('viewAdd', {
      view: event.view
    });
  };

  _onViewRemove(event) {
    this.trigger('viewRemove', {
      view: event.view
    });
  };

  _onViewActivate(event) {
    var view = event.view;
    if (this.outlineContent === view) {
      this.desktop.bringOutlineToFront(this.desktop.outline);
    }
    this.trigger('viewActivate', {
      view: view
    });
  };

  _onViewDeactivate(event) {
    if (this.outlineContent === event.view) {
      this.desktop.sendOutlineToBack();
    }
    this.trigger('viewDeactivate', {
      view: event.view
    });
  };

  addView(view, activate) {
    // normalize displayViewId
    switch (view.displayViewId) {
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
        view.displayViewId = 'C';
        break;
    }
    var column = this._getColumn(view.displayViewId);
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
          this.htmlComp.layout.reset();
          this.htmlComp.invalidateLayoutTree();
          // Layout immediate to prevent 'laggy' form visualization,
          // but not initially while desktop gets rendered because it will be done at the end anyway
          this.htmlComp.validateLayoutTree();
        }
      }
    }
  };

  activateView(view) {
    // activate views is only for existing views allowed.
    if (!this.hasView(view)) {
      return;
    }
    var column = this._getColumn(view.displayViewId);
    if (column) {
      column.activateView(view);
    }
  };

  _getColumn(displayViewId) {
    var column;

    switch (displayViewId) {
      case 'NW':
      case 'W':
      case 'SW':
        column = this.columns[VIEW_AREA_COLUMN_INDEX.LEFT];
        break;
      case 'NE':
      case 'E':
      case 'SE':
        column = this.columns[VIEW_AREA_COLUMN_INDEX.RIGHT];
        break;
      default:
        column = this.columns[VIEW_AREA_COLUMN_INDEX.CENTER];
        break;
    }
    return column;
  };

  removeView(view, showSiblingView) {
    var column = this.tabBoxMap[view.id];
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
          this.htmlComp.layout.reset();
          this.htmlComp.invalidateLayoutTree();
          // Layout immediate to prevent 'laggy' form visualization,
          // but not initially while desktop gets rendered because it will be done at the end anyway
          this.htmlComp.validateLayoutTree();
        }
      }
    }
  };

  getComponents() {
    return this.components;
  };

  getTabBox(displayViewId) {
    var viewColumn = this._getColumn(displayViewId);
    if (!viewColumn) {
      return;
    }
    return viewColumn.getTabBox(displayViewId);
  };

  visibleTabBoxes() {
    return this.visibleColumns().reduce(function(arr, column) {
      arrays.pushAll(arr, column.visibleTabBoxes());
      return arr;
    }, []);
  };

  hasView(view) {
    return this.columns.filter(function(column) {
      return column.hasView(view);
    }).length > 0;
  };

  getViews(displayViewId) {
    return this.columns.reduce(function(arr, column) {
      arrays.pushAll(arr, column.getViews(displayViewId));
      return arr;
    }, []);
  };

  getViewTab(view) {
    var viewTab;
    this.getTabs().some(function(vt) {
      if (vt.view === view) {
        viewTab = vt;
        return true;
      }
      return false;
    });
    return viewTab;
  };

  getTabs() {
    var tabs = [];
    // consider right order
    tabs = tabs.concat(this.getTabBox('NW').getController().getTabs());
    tabs = tabs.concat(this.getTabBox('W').getController().getTabs());
    tabs = tabs.concat(this.getTabBox('SW').getController().getTabs());
    tabs = tabs.concat(this.getTabBox('N').getController().getTabs());
    if (this.headerTabAreaController) {
      tabs = tabs.concat(this.headerTabAreaController.getTabs());
    } else {
      tabs = tabs.concat(this.getTabBox('C').getController().getTabs());
    }
    tabs = tabs.concat(this.getTabBox('S').getController().getTabs());
    tabs = tabs.concat(this.getTabBox('NE').getController().getTabs());
    tabs = tabs.concat(this.getTabBox('E').getController().getTabs());
    tabs = tabs.concat(this.getTabBox('SE').getController().getTabs());
    return tabs;
  };

  /**
   * @returns all the currently active views (the selected ones) of all the visible tab boxes
   */
  activeViews() {
    var activeViews = [];
    this.visibleColumns().forEach(function(column) {
      column.visibleTabBoxes().forEach(function(tabBox) {
        activeViews.push(tabBox.currentView);
      });
    });
    return activeViews;
  };

}
