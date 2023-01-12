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
  ButtonTile, EventHandler, InitModelOf, KeyStrokeContext, Outline, OutlinePageChangedEvent, Page, PageTileButton, PageTileGridEventMap, PageTileGridModel, PageTileGridSelectKeyStroke, scout, TileGrid, TileGridLayoutConfig,
  TreeAllChildNodesDeletedEvent, TreeChildNodeOrderChangedEvent, TreeNodeChangedEvent, TreeNodesDeletedEvent, TreeNodesInsertedEvent
} from '../../../index';

export class PageTileGrid extends TileGrid implements PageTileGridModel {
  declare model: PageTileGridModel;
  declare eventMap: PageTileGridEventMap;
  declare self: PageTileGrid;
  declare tiles: ButtonTile[];

  compact: boolean;
  compactLayoutConfig: TileGridLayoutConfig;
  outline: Outline;
  page: Page;
  nodes: Page[];

  protected _outlineNodeChangedHandler: EventHandler<TreeNodeChangedEvent<Outline> | OutlinePageChangedEvent>;
  protected _outlineStructureChangedHandler: EventHandler<TreeNodesDeletedEvent | TreeNodesInsertedEvent | TreeAllChildNodesDeletedEvent | TreeChildNodeOrderChangedEvent>;

  constructor() {
    super();
    this.compact = false;
    this.compactLayoutConfig = new TileGridLayoutConfig({
      columnWidth: 120,
      rowHeight: 100,
      hgap: 15,
      vgap: 15
    });
    this.outline = null;
    this.page = null;
    this.nodes = null;
    this.scrollable = false;
    this.renderAnimationEnabled = true;
    this.startupAnimationEnabled = true;
    this._outlineNodeChangedHandler = this._onOutlineNodeChanged.bind(this);
    this._outlineStructureChangedHandler = this._onOutlineStructureChanged.bind(this);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.nodes = this.nodes || (this.page && this.page.childNodes) || (this.outline && this.outline.nodes);
    this._setCompact(this.compact);
    this.setOutline(this.outline);
    this.setPage(this.page);
    this.setNodes(this.nodes);
    this._setCompactLayoutConfig(this.compactLayoutConfig);
  }

  protected override _destroy() {
    this.setOutline(null);
    this.setPage(null);
    this.setNodes(null);
    super._destroy();
  }

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    this.keyStrokeContext.$bindTarget = this.session.$entryPoint;
    this.keyStrokeContext.registerKeyStroke(new PageTileGridSelectKeyStroke(this));
  }

  setOutline(outline: Outline) {
    if (this.outline) {
      this.outline.off('nodeChanged pageChanged', this._outlineNodeChangedHandler);
      this.outline.off('nodesDeleted', this._outlineStructureChangedHandler);
      this.outline.off('nodesInserted', this._outlineStructureChangedHandler);
      this.outline.off('allChildNodesDeleted', this._outlineStructureChangedHandler);
      this.outline.off('childNodeOrderChanged', this._outlineStructureChangedHandler);
    }
    this._setProperty('outline', outline);
    if (this.outline) {
      this.outline.on('nodeChanged pageChanged', this._outlineNodeChangedHandler);
      this.outline.on('nodesDeleted', this._outlineStructureChangedHandler);
      this.outline.on('nodesInserted', this._outlineStructureChangedHandler);
      this.outline.on('allChildNodesDeleted', this._outlineStructureChangedHandler);
      this.outline.on('childNodeOrderChanged', this._outlineStructureChangedHandler);
    }
  }

  setCompact(compact: boolean) {
    this.setProperty('compact', compact);
  }

  protected _setCompact(compact: boolean) {
    this._setProperty('compact', compact);
    if (this.compact) {
      this.setLayoutConfig(this.compactLayoutConfig);
    } else if (this.initialized) {
      // Initially, don't set layout config so that it can be passed by model. If compact is changed later, reset compact layout config to a default one
      this.setLayoutConfig(new TileGridLayoutConfig());
    }
    this.setGridColumnCount(this.compact ? 3 : 4);
    this.startupAnimationEnabled = !this.compact;
    if (this.initialized) {
      this._rebuild();
    }
  }

  protected _setCompactLayoutConfig(layoutConfig: TileGridLayoutConfig) {
    if (!layoutConfig) {
      layoutConfig = new TileGridLayoutConfig();
    }
    this._setProperty('compactLayoutConfig', TileGridLayoutConfig.ensure(layoutConfig));
  }

  setPage(page: Page) {
    this._setProperty('page', page);
  }

  setNodes(nodes: Page[]) {
    this._setProperty('nodes', nodes);
    this._rebuild();
  }

  protected _createPageTiles(pages: Page[]): ButtonTile[] {
    return (pages || []).map(page => this._createPageTile(page));
  }

  protected _createPageTile(page: Page): ButtonTile {
    let button = scout.create(PageTileButton, {
      parent: this,
      outline: this.outline,
      page: page
    });
    let tile = scout.create(ButtonTile, {
      parent: this,
      cssClass: this.compact ? 'compact' : null,
      tileWidget: button
    });
    page.tile = tile;
    return tile;
  }

  protected _rebuild() {
    this.setTiles(this._createPageTiles(this.nodes));
  }

  protected _onOutlineNodeChanged(event: TreeNodeChangedEvent<Outline> | OutlinePageChangedEvent) {
    let page = (event as TreeNodeChangedEvent).node as Page || (event as OutlinePageChangedEvent).page;
    let tile = page.tile;
    if (!tile) {
      return;
    }
    let tileButton = tile.tileWidget as PageTileButton;
    tileButton.notifyPageChanged();
  }

  protected _onOutlineStructureChanged(event: TreeNodesDeletedEvent | TreeNodesInsertedEvent | TreeAllChildNodesDeletedEvent | TreeChildNodeOrderChangedEvent) {
    if (this.page) {
      if (this.page === event.parentNode) {
        this.setNodes(this.page.childNodes);
      }
    } else {
      let evt = event as TreeNodesDeletedEvent | TreeNodesInsertedEvent; // basically may also be a TreeAllChildNodesDeletedEvent | TreeChildNodeOrderChangedEvent. But as the presence of nodes is validated, this cast is ok.
      let eventContainsTopLevelNode = evt.nodes && evt.nodes.some(node => !node.parentNode) || event.type === 'allChildNodesDeleted';
      // only rebuild if top level nodes change
      if (eventContainsTopLevelNode) {
        this.setNodes(this.outline.nodes);
      }
    }
  }
}
