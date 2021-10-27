/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {KeyStrokeContext, PageTileGridSelectKeyStroke, scout, TileGrid, TileGridLayoutConfig} from '../../../index';

export default class PageTileGrid extends TileGrid {

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

  _init(model) {
    super._init(model);
    this.nodes = this.nodes || (this.page && this.page.childNodes) || (this.outline && this.outline.nodes);
    this._setCompact(this.compact);
    this.setOutline(this.outline);
    this.setPage(this.page);
    this.setNodes(this.nodes);
    this._setCompactLayoutConfig(this.compactLayoutConfig);
  }

  _destroy() {
    this.setOutline(null);
    this.setPage(null);
    this.setNodes(null);
    super._destroy();
  }

  /**
   * @override
   */
  _createKeyStrokeContext() {
    return new KeyStrokeContext();
  }

  /**
   * @override
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    this.keyStrokeContext.$bindTarget = this.session.$entryPoint;
    this.keyStrokeContext.registerKeyStroke([
      new PageTileGridSelectKeyStroke(this)
    ]);
  }

  setOutline(outline) {
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

  setCompact(compact) {
    this.setProperty('compact', compact);
  }

  _setCompact(compact) {
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

  _setCompactLayoutConfig(layoutConfig) {
    if (!layoutConfig) {
      layoutConfig = new TileGridLayoutConfig();
    }
    this._setProperty('compactLayoutConfig', TileGridLayoutConfig.ensure(layoutConfig));
  }

  setPage(page) {
    this._setProperty('page', page);
  }

  setNodes(nodes) {
    this._setProperty('nodes', nodes);
    this._rebuild();
  }

  _createPageTiles(pages) {
    return (pages || []).map(function(page) {
      return this._createPageTile(page);
    }, this);
  }

  _createPageTile(page) {
    let button = scout.create('PageTileButton', {
      parent: this,
      outline: this.outline,
      page: page
    });
    let tile = scout.create('ButtonTile', {
      parent: this,
      cssClass: this.compact ? 'compact' : null,
      tileWidget: button
    });
    page.tile = tile;
    return tile;
  }

  _rebuild() {
    this.setTiles(this._createPageTiles(this.nodes));
  }

  _onOutlineNodeChanged(event) {
    let page = event.node || event.page;
    let tile = page.tile;
    if (!tile) {
      return;
    }
    tile.tileWidget.notifyPageChanged();
  }

  _onOutlineStructureChanged(event) {
    if (this.page) {
      if (this.page === event.parentNode) {
        this.setNodes(this.page.childNodes);
      }
    } else {
      let eventContainsTopLevelNode = event.nodes && event.nodes.some(node => {
        return !node.parentNode;
      }) || event.type === 'allChildNodesDeleted';
      // only rebuild if top level nodes change
      if (eventContainsTopLevelNode) {
        this.setNodes(this.outline.nodes);
      }
    }
  }
}
