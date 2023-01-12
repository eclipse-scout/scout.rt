/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Desktop, EventHandler, HtmlComponent, InitModelOf, OutlineOverview, Page, PageTileGrid, PropertyChangeEvent, RowLayout, scout, TileOutlineOverviewEventMap, TileOutlineOverviewModel} from '../../../index';

export class TileOutlineOverview extends OutlineOverview implements TileOutlineOverviewModel {
  declare model: TileOutlineOverviewModel;
  declare eventMap: TileOutlineOverviewEventMap;
  declare self: TileOutlineOverview;

  pageTileGrid: PageTileGrid;
  scrollable: boolean;
  titleVisible: boolean;
  contentHtmlComp: HtmlComponent;
  $title: JQuery;
  protected _desktopNavigationVisibilityChangeHandler: EventHandler<PropertyChangeEvent<boolean, Desktop>>;

  constructor() {
    super();
    this.pageTileGrid = null;
    this.scrollable = true;
    this.titleVisible = true;
    this._addWidgetProperties(['pageTileGrid']);
    this._desktopNavigationVisibilityChangeHandler = this._onDesktopNavigationVisibilityChange.bind(this);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    if (!this.pageTileGrid) {
      this.pageTileGrid = this._createPageTileGrid();
    }
    this.scrollable = !this.outline.compact;
    this.addCssClass('dimmed-background');
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('tile-overview');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new RowLayout({stretch: this.outline.compact}));
    this.$content = this.$container.appendDiv('tile-overview-content');
    this.contentHtmlComp = HtmlComponent.install(this.$content, this.session);
    this.contentHtmlComp.setLayout(new RowLayout({stretch: this.outline.compact}));
    this.$title = this.$content.appendDiv('tile-overview-title').text(this.outline.title);
    HtmlComponent.install(this.$title, this.session);
    this.$title.cssMaxHeight(this.$title.cssHeight());
    this._updateTitle(false);
    this.findDesktop().on('propertyChange:navigationVisible', this._desktopNavigationVisibilityChangeHandler);
  }

  protected override _remove() {
    super._remove();
    this.findDesktop().off('propertyChange:navigationVisible', this._desktopNavigationVisibilityChangeHandler);
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderPageTileGrid();
    this._renderScrollable();
  }

  protected _renderPageTileGrid() {
    this.pageTileGrid.render(this.$content);
  }

  protected _createPageTileGrid(): PageTileGrid {
    let page: Page;
    let nodes: Page[];
    if (this.outline.compact) {
      page = this.outline.compactRootNode();
      if (page) {
        nodes = page.childNodes;
      }
    }
    return scout.create(PageTileGrid, {
      parent: this,
      outline: this.outline,
      compact: this.outline.compact,
      page: page,
      nodes: nodes
    });
  }

  setScrollable(scrollable: boolean) {
    this.setProperty('scrollable', scrollable);
  }

  protected _renderScrollable() {
    if (this.scrollable) {
      this._installScrollbars({
        axis: 'y'
      });
    } else {
      this._uninstallScrollbars();
    }
  }

  protected _updateTitle(animated = true) {
    if (!this.$title) {
      return;
    }
    this.$title.toggleClass('animated', animated);
    this.$title.toggleClass('removed', this.findDesktop().navigationVisible);
  }

  protected _onDesktopNavigationVisibilityChange(event: PropertyChangeEvent<boolean, Desktop>) {
    this._updateTitle();
  }
}
