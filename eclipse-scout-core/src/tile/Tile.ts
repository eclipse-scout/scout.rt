/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, ColorScheme, colorSchemes, EnumObject, GridData, HtmlComponent, InitModelOf, LoadingSupport, scrollbars, SingleLayout, TileEventMap, TileModel, Widget} from '../index';
import $ from 'jquery';

export type TileDisplayStyle = EnumObject<typeof Tile.DisplayStyle>;

export class Tile extends Widget implements TileModel {
  declare model: TileModel;
  declare eventMap: TileEventMap;
  declare self: Tile;

  animateBoundsChange: boolean;
  colorScheme: ColorScheme;
  displayStyle: TileDisplayStyle;
  gridData: GridData;
  gridDataHints: GridData;
  rowId: string;
  selected: boolean;
  selectable: boolean;
  plainText: string;

  constructor() {
    super();
    this.animateBoundsChange = true;
    this.displayStyle = Tile.DisplayStyle.DEFAULT;
    this.gridData = null;
    this.rowId = null;
    this.gridDataHints = new GridData();
    this.colorScheme = null;
    this.selected = false;
    this.selectable = false;
    this.plainText = null;
    // Null to let TileGrid decide whether to enable animation
    this.animateRemoval = null;
    this._addPropertyDimensionAlias('visible', 'filterAccepted');
  }

  static DisplayStyle = {
    DEFAULT: 'default',
    PLAIN: 'plain'
  }; // not const, can be extended

  protected override _createLoadingSupport(): LoadingSupport {
    return new LoadingSupport({
      widget: this
    });
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._setGridDataHints(this.gridDataHints);
    this._setColorScheme(this.colorScheme);
    this._setSelectable(this.selectable);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('tile');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new SingleLayout());
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderColorScheme();
    this._renderSelectable();
    this._renderSelected();
    this._renderDisplayStyle();
  }

  protected override _postRender() {
    this.$container.addClass('tile');
    // Make sure prefSize returns the size the tile has after the animation even if it is called while the animation runs
    // Otherwise the tile may have the wrong size after making a tile with useUiHeight = true visible
    this.htmlComp.layout.animateClasses = ['animate-visible', 'animate-invisible', 'animate-insert', 'animate-remove'];
  }

  protected _renderDisplayStyle() {
    this.$container.toggleClass('default-tile', this.displayStyle === Tile.DisplayStyle.DEFAULT);
  }

  /** @see TileModel.gridDataHints */
  setGridDataHints(gridData: GridData) {
    this.setProperty('gridDataHints', gridData);
    if (this.rendered) {
      // Do it here instead of _renderGridDataHints because grid does not need to be invalidated when rendering, only when hints change
      // Otherwise it forces too many unnecessary recalculations when tile grid is rendering tiles due to virtual scrolling
      this.parent.invalidateLogicalGrid();
    }
  }

  protected _setGridDataHints(gridData: GridData) {
    this._setProperty('gridDataHints', GridData.ensure(gridData || new GridData()));
  }

  /** @internal */
  _setGridData(gridData: GridData) {
    this._setProperty('gridData', GridData.ensure(gridData || new GridData()));
  }

  /** @see TileModel.colorScheme */
  setColorScheme(colorScheme: ColorScheme | string) {
    this.setProperty('colorScheme', colorScheme);
  }

  protected _setColorScheme(colorScheme: ColorScheme | string) {
    let defaultScheme = {
      scheme: colorSchemes.ColorSchemeId.DEFAULT,
      inverted: false
    };
    colorScheme = colorSchemes.ensureColorScheme(colorScheme, true);
    colorScheme = $.extend({}, defaultScheme, colorScheme);
    this._setProperty('colorScheme', colorScheme);
  }

  protected _renderColorScheme() {
    colorSchemes.toggleColorSchemeClasses(this.$container, this.colorScheme);
  }

  /** @see TileModel.selected */
  setSelected(selected: boolean) {
    if (selected && !this.selectable) {
      return;
    }
    this.setProperty('selected', selected);
  }

  protected _renderSelected() {
    this.$container.toggleClass('selected', this.selected);
  }

  /** @see TileModel.selectable */
  setSelectable(selectable: boolean) {
    this.setProperty('selectable', selectable);
  }

  protected _setSelectable(selectable: boolean) {
    this._setProperty('selectable', selectable);
    if (!this.selectable) {
      this.setSelected(false);
    }
  }

  protected _renderSelectable() {
    this.$container.toggleClass('selectable', this.selectable);
  }

  setFilterAccepted(filterAccepted: boolean) {
    this.setProperty('filterAccepted', filterAccepted);
  }

  get filterAccepted(): boolean {
    return this.getProperty('filterAccepted');
  }

  /** @internal */
  override _renderVisible() {
    if (this.rendering) {
      this.$container.setVisible(this.visible);
      return;
    }
    if (this.removalPending) {
      // Do nothing if removal is in progress. May happen if filter is set, tile is removed by the filter animation, filter is removed again while animation is still running
      // Adding animate-visible in that case would trigger the animationEnd listener of widget._removeAnimated even though it is not the remove animation which finishes
      // That would cause the animate-visible animation to be executed twice because tileGrid._renderTile would render the tile and start the animation anew.
      return;
    }
    if (!this.visible) {
      // Remove animate-visible first to show correct animation even if tile is made invisible while visible animation is still in progress
      // It is also necessary if the container is made invisible before the animation is finished because animationEnd won't fire in that case
      // which means that animate-invisible is still on the element and will trigger the (wrong) animation when container is made visible again
      this.$container.removeClass('invisible animate-visible');
      this.$container.addClassForAnimation('animate-invisible');
      this.$container.oneAnimationEnd(() => {
        // Make the element invisible after the animation (but only if visibility has not changed again in the meantime)
        this.$container.setVisible(this.visible);
        // Layout is invalidated before the animation starts and the TileGridLayout does not listen for visible-animations to update the scrollbar -> do it here
        // It is mainly necessary if every tile is made invisible / visible. If only some tiles are affected, the TileGridLayout animation change will trigger the scrollbar update
        scrollbars.update(this.parent.get$Scrollable());
      });
    } else {
      this.$container.addClass('invisible'); // Don't show it until it has the correct size and position to prevent flickering (Scout JS, non virtual)
      this.$container.setVisible(true);
      // Wait until the tile is layouted before trying to animate it to make sure the layout does not read the size while the animation runs (because it will be the wrong one)
      this.session.layoutValidator.schedulePostValidateFunction(() => {
        if (!this.rendered || !this.visible) {
          return;
        }
        this.$container.removeClass('invisible animate-invisible');
        this.$container.addClassForAnimation('animate-visible');
        this.$container.oneAnimationEnd(() => {
          // See comment above on why this is done here
          scrollbars.update(this.parent.get$Scrollable());
        });
      });
    }
    this.invalidateParentLogicalGrid();
  }

  /**
   * Marks the element of this tile that is "focused" as the active descendant of the
   * container that has the "real" focus. You may override this to e.g. account for
   * custom navigation concepts in your tile, or if your tile should be announced
   * differently by screen readers.
   *
   * @param $container the focused container for which to mark the active descendant
   */
  markAsActiveDescendantFor($container: JQuery) {
    aria.linkElementWithActiveDescendant($container, this.$container);
  }
}
