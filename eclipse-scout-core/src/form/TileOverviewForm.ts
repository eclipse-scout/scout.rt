/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Desktop, EventHandler, Form, HtmlComponent, InitModelOf, Outline, Page, PageTileGrid, PropertyChangeEvent, RowLayout, scout, TileOverviewFormEventMap, TileOverviewFormModel} from '../index';

export class TileOverviewForm extends Form implements TileOverviewFormModel {
  declare model: TileOverviewFormModel;
  declare eventMap: TileOverviewFormEventMap;
  declare self: TileOverviewForm;

  outline: Outline;
  nodes: Page[];
  tileOverviewTitle: string;
  scrollable: boolean;
  pageTileGrid: PageTileGrid;
  contentHtmlComp: HtmlComponent;
  $content: JQuery;
  protected _desktopNavigationVisibilityChangeHandler: EventHandler<PropertyChangeEvent<boolean, Desktop>>;

  constructor() {
    super();

    this.outline = null;
    this.nodes = null;
    this.tileOverviewTitle = null;
    this.scrollable = true;
    this._addWidgetProperties(['pageTileGrid']);
    this.$content = null;
    this.$title = null;
    this._desktopNavigationVisibilityChangeHandler = this._onDesktopNavigationVisibilityChange.bind(this);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    if (!this.pageTileGrid) {
      this.pageTileGrid = this._createPageTileGrid();
    }
    this.addCssClass('dimmed-background');
  }

  protected override _renderForm() {
    super._renderForm();
    this.htmlComp.setLayout(new RowLayout());
    this.$content = this.$container.appendDiv('tile-overview-content');
    this.contentHtmlComp = HtmlComponent.install(this.$content, this.session);
    this.contentHtmlComp.setLayout(new RowLayout({stretch: this.outline.compact}));
    this.$title = this.$content.appendDiv('tile-overview-title').text(this.tileOverviewTitle);
    HtmlComponent.install(this.$title, this.session);
    this.$title.cssMaxHeight(this.$title.cssHeight());
    this._updateTitle(false);
    this.findDesktop().on('propertyChange:navigationVisible', this._desktopNavigationVisibilityChangeHandler);
  }

  protected override _remove() {
    this.$content = null;
    this.$title = null;
    this.findDesktop().off('propertyChange:navigationVisible', this._desktopNavigationVisibilityChangeHandler);
    super._remove();
  }

  protected override _renderProperties() {
    super._renderProperties();
    if (this.pageTileGrid.rendered) {
      this.pageTileGrid = this._createPageTileGrid();
    }
    this._renderPageTileGrid();
    this._renderScrollable();
  }

  protected _renderPageTileGrid() {
    this.pageTileGrid.render(this.$content);
  }

  protected _createPageTileGrid(): PageTileGrid {
    return scout.create(PageTileGrid, {
      parent: this,
      outline: this.outline,
      nodes: this.nodes
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

  setPage(page: Page) {
    this.outline = page.getOutline();
    this.nodes = page.childNodes;
    this.tileOverviewTitle = page.text;
    this.setScrollable(!this.outline.compact);

    this.pageTileGrid.setCompact(this.outline.compact);
    this.pageTileGrid.setOutline(this.outline);
    this.pageTileGrid.setPage(page);
    this.pageTileGrid.setNodes(this.nodes);
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
