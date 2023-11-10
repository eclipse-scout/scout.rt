/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Dimension, graphics, HtmlComponent, HtmlCompPrefSizeOptions, Insets, LogicalGridLayout, Point, Rectangle, scout, scrollbars, Tile, TileGrid, TileGridLayoutConfig, Widget} from '../index';
import $ from 'jquery';

export class TileGridLayout extends LogicalGridLayout {
  declare widget: TileGrid<Tile>;
  containerPos: Point;
  containerScrollTop: number;
  maxWidth: number;
  tiles: Tile[];
  protected _calculatingPrimitivePrefSize: boolean;

  constructor(widget: Widget, layoutConfig: TileGridLayoutConfig) {
    super(widget, layoutConfig);
    this.containerPos = null;
    this.containerScrollTop = null;
    this.tiles = [];
    this._calculatingPrimitivePrefSize = false;
  }

  /**
   * @param scrollTopDirty If the scroll top position should be considered dirty while updating the view port.
   * If true, the view port is not rendered, as the scroll positions are not reliable anyway. Then only the layout of the TileGrid is updated.
   */
  updateViewPort(scrollTopDirty?: boolean) {
    let tileGrid = this.widget;
    if (!tileGrid.rendered) {
      return;
    }

    // Try to layout only as much as needed while scrolling in virtual mode
    // Scroll top may be dirty when layout is validated before scrolling to a specific tile (see tileGrid.scrollTo)
    if (!scout.nvl(scrollTopDirty, false)) {
      tileGrid._renderViewPort();
    }
    this._layout(tileGrid.$container);
    tileGrid.trigger('layoutAnimationDone');
  }

  override layout($container: JQuery) {
    let htmlComp = this.widget.htmlComp;

    // Animate only once on startup (if enabled) but animate every time on resize
    let animated = htmlComp.layouted || (this.widget.startupAnimationEnabled && !this.widget.startupAnimationDone) || this.widget.renderAnimationEnabled;
    this.tiles = this.widget.renderedTiles(true);

    // Make them invisible otherwise the influence scrollHeight (e.g. if grid is scrolled to the very bottom and tiles are filtered, scrollbar would still increase scroll height)
    scrollbars.setVisible($container, false);

    // Store the current position of the tiles
    if (animated) {
      this._storeBounds(this.tiles);
    }

    this._updateMaxWidth();
    this._resetGridColumnCount();

    this.widget.invalidateLayout();
    this.widget.invalidateLogicalGrid(false);
    let contentFits = false;
    let containerWidth = $container.outerWidth();
    containerWidth = Math.max(containerWidth, this.minWidth);
    if (htmlComp.prefSize().width <= containerWidth) {
      this._layout($container);
      contentFits = true;
    }

    // If content does not fit, the columnCount will be reduced until it fits
    while (!contentFits && this.widget.wrappable && this.widget.gridColumnCount > 1) {
      this.widget.gridColumnCount--;
      this.widget.invalidateLayout();
      this.widget.invalidateLogicalGrid(false);
      if (htmlComp.prefSize().width <= containerWidth) {
        this._layout($container);
        contentFits = true;
      }
    }

    // If it does not fit, layout anyway (happens on small sizes if even one column is not sufficient)
    if (!contentFits) {
      this._layout($container);
    }

    if (!htmlComp.layouted) {
      this.widget._renderScrollTop();
    }
    if (this.widget.virtual && (!htmlComp.layouted || this._sizeChanged(htmlComp) || this.widget.withPlaceholders)) {
      // When changing size of the container, more or less tiles might be shown and some tiles might even change rows due to a new gridColumnCount -> ensure correct tiles are rendered in the range
      this.widget.setViewRangeSize(this.widget.calculateViewRangeSize(), false);
      let newTiles = this.widget._renderTileDelta();
      // Make sure newly rendered tiles are animated (if enabled) and layouted as well
      this._storeBounds(newTiles);
      arrays.pushAll(this.tiles, newTiles);
      this._layout($container);
    }

    let promises = [];
    if (animated) {
      promises = this._animateTiles();
    }
    this.widget.startupAnimationDone = true;

    // When all animations have been finished, trigger event and update scrollbar
    if (promises.length > 0) {
      $.promiseAll(promises).done(this._onAnimationDone.bind(this));
    } else {
      this._onAnimationDone();
    }
    this._updateFilterFieldMaxWidth($container);
  }

  protected _sizeChanged(htmlComp: HtmlComponent): boolean {
    return htmlComp.sizeCached && !htmlComp.sizeCached.equals(htmlComp.size());
  }

  protected _storeBounds(tiles: Tile[]) {
    tiles.forEach((tile, i) => {
      let bounds = graphics.cssBounds(tile.$container);
      tile.$container.data('oldBounds', bounds);
      tile.$container.data('was-layouted', tile.htmlComp.layouted);
    });
  }

  protected override _validateGridData(htmlComp: HtmlComponent): boolean {
    htmlComp.$comp.removeClass('newly-rendered');
    return super._validateGridData(htmlComp);
  }

  protected override _layoutCellBounds(containerSize: Dimension, containerInsets: Insets): Rectangle[][] {
    // Since the tiles are positioned absolutely it is necessary to add the height of the filler to the top insets
    if (this.widget.virtual && this.widget.$fillBefore) {
      containerInsets.top += this.widget.$fillBefore.outerHeight(true);
    }
    return super._layoutCellBounds(containerSize, containerInsets);
  }

  protected _animateTiles(): JQuery.Promise<void>[] {
    let htmlComp = this.widget.htmlComp;
    let $container = htmlComp.$comp;

    this.containerPos = htmlComp.offset();
    this.containerScrollTop = $container.scrollTop();

    // Hide scrollbar before the animation (does not look good if scrollbar is hidden after the animation)
    scrollbars.setVisible($container, true);
    scrollbars.opacity($container, 0);

    // Animate the position change of the tiles
    let promises = [];
    this.tiles.forEach((tile, i) => {
      if (!tile.rendered) {
        // Only animate tiles which were there at the beginning of the layout
        // RenderViewPort may remove or render some, the removed ones cannot be animated because $container is missing and don't need to anyway, the rendered ones cannot because fromBounds are missing
        return;
      }

      let promise = this._animateTile(tile);
      if (promise) {
        promises.push(promise);
      }

      tile.$container.removeData('oldBounds');
      tile.$container.removeData('was-layouted');
    });

    return promises;
  }

  protected _animateTile(tile: Tile): void | JQuery.Promise<void> {
    let htmlComp = this.widget.htmlComp;

    // Stop running animations before starting the new ones to make sure existing promises are not resolved too early
    // It may also happen that while the animation of a tile is in progress, the layout is triggered again but the tile should not be animated anymore
    // (e.g. if it is not in the viewport anymore). In that case the animation must be stopped otherwise it may be placed at a wrong position
    tile.$container.stop();

    if (tile.$container.isVisibilityHidden() || tile.$container.hasClass('animate-visible')) {
      // When tiles are inserted they are invisible because a dedicated insert animation will be started after the layouting,
      // the animation here is to animate the position change -> don't animate inserted tiles here

      // Also: don't animate tiles which are fading in (due to filtering), they should appear at the correct position.
      // Already visible tiles which were in the view port before will be moved from the old position. Tiles which were not in the view port before will fly in from the top left corner (same happens when sorting).
      // Reason: When sorting, if some tiles are in the viewport and some not, it is confusing if some tiles just appear and others are moved, even though all actually change position.
      return;
    }

    let bounds = graphics.cssBounds(tile.$container);
    let fromBounds = tile.$container.data('oldBounds');
    if (!tile.$container.data('was-layouted')) {
      // Placeholders may not have fromBounds because they are added while layouting
      // Just let them appear at the correct position
      fromBounds = bounds.clone();
    }

    if (!htmlComp.layouted && (this.widget.startupAnimationDone || !this.widget.startupAnimationEnabled) && this.widget.renderAnimationEnabled) {
      // This is a small, discreet render animation, just move the tiles a little
      // It will happen if the startup animation is disabled or done and every time the tiles are rendered anew
      fromBounds = new Rectangle(bounds.x * 0.95, bounds.y * 0.95, bounds.width, bounds.height);
    }

    if (fromBounds.equals(bounds)) {
      // Don't animate if bounds are equals (otherwise promises would always resolve after 300ms even though no animation was visible)
      return;
    }

    if (!this._inViewport(bounds) && !this._inViewport(fromBounds)) {
      // If neither the new nor the old position is in the viewport don't animate the tile. This will affect the animation performance in a positive way if there are many tiles
      return;
    }

    if (!tile.$container.data('was-layouted') && !this._inViewport(bounds)) {
      // If a newly inserted tile will be rendered outside the view port, don't animate it. If it is rendered inside the view port it is fine if it will be moved from the top left corner
      return;
    }

    // Start animation
    return this._animateTileBounds(tile, fromBounds, bounds);
  }

  protected _inViewport(bounds: Rectangle): boolean {
    bounds = bounds.translate(this.containerPos.x, this.containerPos.y).translate(0, -this.containerScrollTop);
    let topLeftPos = new Point(bounds.x, bounds.y);
    let bottomRightPos = new Point(bounds.x + bounds.width, bounds.y + bounds.height);
    let $scrollable = this.widget.$container.scrollParent();
    return scrollbars.isLocationInView(topLeftPos, $scrollable) || scrollbars.isLocationInView(bottomRightPos, $scrollable);
  }

  protected _onAnimationDone() {
    this._updateScrollbar();
    this.widget.trigger('layoutAnimationDone');
  }

  protected _animateTileBounds(tile: Tile, fromBounds: Rectangle, bounds: Rectangle): JQuery.Promise<void> {
    // jQuery's animate() function sets "overflow: hidden" during the animation. After the animation, the
    // original value is restored. (Search for "opts.overflow" in the jQuery source code, and see
    // https://stackoverflow.com/a/5696656/7188380 for details why this is required.)
    // Unfortunately, because we are running multiple animations in parallel here, the second animation will
    // remember the temporary value set by the first animation and will restore it at the end. This causes the
    // tile to have the inline style "overflow: hidden" after all animations have been completed, even if the
    // CSS rules say something different.
    // As a workaround, we remember the correct original value ourselves and restore it manually after all
    // individual animations have been completed. Only then will the resulting promise be resolved.
    let elem = tile.$container[0];
    let oldOverflowStyles = [elem.style.overflow, elem.style.overflowX, elem.style.overflowY];
    let restoreOverflowStyle = () => {
      elem.style.overflow = oldOverflowStyles[0];
      elem.style.overflowX = oldOverflowStyles[1];
      elem.style.overflowY = oldOverflowStyles[2];
    };

    let promises = [];
    tile.$container
      .cssLeftAnimated(fromBounds.x, bounds.x, {
        start: promise => {
          promises.push(promise);
        },
        queue: false
      })
      .cssTopAnimated(fromBounds.y, bounds.y, {
        start: promise => {
          promises.push(promise);
        },
        queue: false
      })
      .cssWidthAnimated(fromBounds.width, bounds.width, {
        start: promise => {
          promises.push(promise);
        },
        queue: false
      })
      .cssHeightAnimated(fromBounds.height, bounds.height, {
        start: promise => {
          promises.push(promise);
        },
        queue: false
      });

    return $.promiseAll(promises).then(restoreOverflowStyle);
  }

  protected _updateScrollbar() {
    scrollbars.setVisible(this.widget.$container, true);
    scrollbars.opacity(this.widget.$container, 1);

    // Update first scrollable parent (if widget itself is not scrollable, maybe a parent is)
    let htmlComp = this.widget.htmlComp;
    while (htmlComp) {
      if (htmlComp.scrollable) {
        // Update immediately to prevent flickering (scrollbar is made visible on the top of this function)
        scrollbars.update(htmlComp.$comp, true);
        break;
      }
      htmlComp = htmlComp.getParent();
    }
  }

  /**
   * When max. width should be enforced, add a padding to the container if necessary
   * (to make sure, scrollbar position is not changed)
   */
  protected _updateMaxWidth() {
    // Reset padding-right set by layout
    let htmlComp = this.widget.htmlComp;
    htmlComp.$comp.cssPaddingRight(null);

    if (this.maxWidth <= 0) {
      return;
    }

    // Measure current padding-right (by CSS)
    let cssPaddingRight = htmlComp.$comp.cssPaddingRight();

    // Calculate difference between current with and max. width
    let containerSize = htmlComp.size();
    let oldWidth = containerSize.width;
    let newWidth = Math.min(containerSize.width, this.maxWidth);
    let diff = oldWidth - newWidth - htmlComp.$comp.cssPaddingLeft() - htmlComp.$comp.cssBorderWidthX();
    if (diff > cssPaddingRight) {
      htmlComp.$comp.cssPaddingRight(diff);
    }
  }

  protected _resetGridColumnCount() {
    this.widget.gridColumnCount = this.widget.prefGridColumnCount;
  }

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    options = $.extend({}, options);

    if (this.widget.virtual) {
      return this.virtualPrefSize($container, options);
    }
    return this.primitivePrefSize($container, options);
  }

  /**
   * Calculates the preferred size only based on the grid column count, row count and layout config. Does not use rendered elements.
   * Therefore, it only works if all tiles are of the same size (which is a precondition for the virtual scrolling anyway).
   */
  virtualPrefSize($container: JQuery, options: HtmlCompPrefSizeOptions): Dimension {
    let rowCount, columnCount;
    let insets = HtmlComponent.get($container).insets();
    let prefSize = new Dimension();
    let columnWidth = this.columnWidth;
    let rowHeight = this.rowHeight;
    let hgap = this.hgap;
    let vgap = this.vgap;

    if (options.widthHint) {
      columnCount = Math.floor(options.widthHint / (columnWidth + hgap));
      let width = columnCount * (columnWidth + hgap);
      if (options.widthHint - width > columnWidth) {
        // The last column does not have a hgap -> Correct the grid column count if another column would fit in
        columnCount++;
      }
      columnCount = Math.max(Math.min(this.widget.prefGridColumnCount, columnCount), 1);

      rowCount = this.widget.rowCount(columnCount);
      prefSize.width = options.widthHint;
      prefSize.height = Math.max(rowCount * rowHeight + (rowCount - 1) * vgap, 0);
      prefSize.width += insets.horizontal();
      prefSize.height += insets.vertical();
      return prefSize;
    }

    columnCount = this.widget.gridColumnCount;
    rowCount = this.widget.rowCount();
    prefSize.width = Math.max(columnCount * columnWidth + (columnCount - 1) * hgap, 0);
    prefSize.height = Math.max(rowCount * rowHeight + (rowCount - 1) * vgap, 0);
    prefSize.width += insets.horizontal();
    prefSize.height += insets.vertical();
    return prefSize;
  }

  primitivePrefSize($container: JQuery, options: HtmlCompPrefSizeOptions): Dimension {
    if (!options.widthHint || this._calculatingPrimitivePrefSize) {
      return super.preferredLayoutSize($container, options);
    }
    this._calculatingPrimitivePrefSize = true;
    let prefSize = this._primitivePrefSize(options);
    this._calculatingPrimitivePrefSize = false;
    return prefSize;
  }

  protected _primitivePrefSize(options: HtmlCompPrefSizeOptions): Dimension {
    let prefSize,
      htmlComp = this.widget.htmlComp,
      contentFits = false,
      gridColumnCount = this.widget.gridColumnCount,
      width = options.widthHint;

    // prefSize will be called for tileGrid itself, hints must not be adjusted
    options.removeInsetsFromHints = false;

    width += htmlComp.insets().horizontal();
    this._resetGridColumnCount();

    this.widget.invalidateLayout();
    this.widget.invalidateLogicalGrid(false);
    prefSize = htmlComp.prefSize(options);
    if (prefSize.width <= width) {
      contentFits = true;
    }

    while (!contentFits && this.widget.gridColumnCount > 1) {
      this.widget.gridColumnCount--;
      this.widget.invalidateLayout();
      this.widget.invalidateLogicalGrid(false);
      prefSize = htmlComp.prefSize(options);
      if (prefSize.width <= width) {
        contentFits = true;
      }
    }
    // Reset to previous gridColumnCount (prefSize should not modify properties)
    this.widget.gridColumnCount = gridColumnCount;
    return prefSize;
  }

  protected _updateFilterFieldMaxWidth($container: JQuery) {
    let htmlComp = HtmlComponent.get($container),
      width = htmlComp.availableSize().subtract(htmlComp.insets()).width;
    this.widget.$filterFieldContainer.css('--filter-field-max-width', (width * 0.6) + 'px');
  }
}
