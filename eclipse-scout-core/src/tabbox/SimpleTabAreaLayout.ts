/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, ContextMenuPopup, Dimension, graphics, Menu, PrefSizeOptions, scout, SimpleTabArea, styles, widgets} from '../index';
import $ from 'jquery';

export class SimpleTabAreaLayout extends AbstractLayout {
  tabArea: SimpleTabArea;

  tabSize: number;
  tabMinSize: number;
  overflowTabItemSize: number;
  protected _horizontalTabs: boolean;
  protected _$overflowTab: JQuery;
  protected _overflowTabsIndizes: number[];

  constructor(tabArea: SimpleTabArea) {
    super();
    this.tabArea = tabArea;
    this.tabSize = null;
    this.tabMinSize = null;
    this.overflowTabItemSize = null;
    this._horizontalTabs = null;
    this._$overflowTab = null;
    this._overflowTabsIndizes = [];
  }

  /** @deprecated will be removed in a future release, use {@link tabSize} instead */
  get tabWidth(): number {
    return this.tabSize;
  }

  /** @deprecated will be removed in a future release, use {@link tabSize} instead */
  set tabWidth(tabWidth: number) {
    this.tabSize = tabWidth;
  }

  /** @deprecated will be removed in a future release, use {@link tabMinSize} instead */
  get tabMinWidth(): number {
    return this.tabMinSize;
  }

  /** @deprecated will be removed in a future release, use {@link tabMinSize} instead */
  set tabMinWidth(tabMinWidth: number) {
    this.tabMinSize = tabMinWidth;
  }

  /** @deprecated will be removed in a future release, use {@link overflowTabItemSize} instead */
  get overflowTabItemWidth(): number {
    return this.overflowTabItemSize;
  }

  /** @deprecated will be removed in a future release, use {@link overflowTabItemSize} instead */
  set overflowTabItemWidth(overflowTabItemWidth: number) {
    this.overflowTabItemSize = overflowTabItemWidth;
  }

  override layout($container: JQuery) {
    this._initSizes();

    let htmlContainer = this.tabArea.htmlComp,
      containerSize = htmlContainer.size({
        exact: true
      }),
      $tabs = htmlContainer.$comp.children('.simple-tab'),
      numTabs = this.tabArea.getTabs().length,
      smallPrefSize = this.smallPrefSize();

    containerSize = containerSize.subtract(htmlContainer.insets());

    // Reset tabs
    if (this._$overflowTab) {
      this._$overflowTab.remove();
    }
    $tabs.setVisible(true);
    this._overflowTabsIndizes = [];
    widgets.updateFirstLastMarker(this.tabArea.getTabs());

    // All tabs fit in container -> no overflow menu necessary
    if (this._getSize(smallPrefSize) <= this._getSize(containerSize)) {
      $container.removeClass('overflown');
      return;
    }

    // Not all tabs fit in container -> put tabs into overflow menu
    $container.addClass('overflown');
    this._setSize(containerSize, this._getSize(containerSize) - this.overflowTabItemSize);

    // check how many tabs fit into remaining containerSize.width or containerSize.height
    let numVisibleTabs = Math.floor(this._getSize(containerSize) / this.tabMinSize);
    let numOverflowTabs = numTabs - numVisibleTabs;

    let selectedIndex = 0;
    $tabs.each((i, tab) => {
      if ($(tab).hasClass('selected')) {
        selectedIndex = i;
      }
    });

    // determine visible range
    let rightEnd;
    let leftEnd = selectedIndex - Math.floor(numVisibleTabs / 2);
    if (leftEnd < 0) {
      leftEnd = 0;
      rightEnd = numVisibleTabs - 1;
    } else {
      rightEnd = leftEnd + numVisibleTabs - 1;
      if (rightEnd > numTabs - 1) {
        rightEnd = numTabs - 1;
        leftEnd = rightEnd - numVisibleTabs + 1;
      }
    }

    this._$overflowTab = htmlContainer.$comp
      .appendDiv('simple-overflow-tab-item')
      .on('mousedown', this._onOverflowTabItemMouseDown.bind(this));
    this._$overflowTab.appendDiv('num-tabs').text(numOverflowTabs);

    $tabs.each((i, tab) => {
      if (i < leftEnd || i > rightEnd) {
        $(tab).setVisible(false);
        this._overflowTabsIndizes.push(i);
      }
    });
    widgets.updateFirstLastMarker(this.tabArea.getVisibleTabs());
  }

  protected _getSize(dimension: Dimension): number {
    return this._horizontalTabs ? dimension.width : dimension.height;
  }

  protected _setSize(dimension: Dimension, size: number) {
    if (this._horizontalTabs) {
      dimension.width = size;
    } else {
      dimension.height = size;
    }
  }

  smallPrefSize(options: PrefSizeOptions & { tabMinSize?: number } = {}): Dimension {
    this._initSizes();
    options = $.extend({tabMinSize: this.tabMinSize}, options);
    return this.preferredLayoutSize(this.tabArea.$container, options);
  }

  override preferredLayoutSize($container: JQuery, options?: PrefSizeOptions & { tabMinSize?: number }): Dimension {
    this._initSizes();
    let tabMinSize = scout.nvl(options.tabMinSize, 0) || scout.nvl(this.tabSize, 0);
    let numTabs = this.tabArea.getTabs().length;
    let minSize = numTabs * tabMinSize;
    options = $.extend({useCssSize: true}, options);
    let prefSize = graphics.prefSize(this.tabArea.$container, options);
    let sizeHint = this._horizontalTabs ? options.widthHint : options.heightHint;
    if (sizeHint && this.tabArea.displayStyle === SimpleTabArea.DisplayStyle.SPREAD_EVEN) {
      minSize = Math.max(sizeHint, minSize);
    }
    return this._horizontalTabs ? new Dimension(minSize, prefSize.height) : new Dimension(prefSize.width, minSize);
  }

  /**
   * Reads the default sizes from CSS -> the tabs need to specify a width and a min-width or a height and a min-height.
   * The layout expects all tabs to have the same width.
   */
  protected _initSizes() {
    // in case the orientation has changed, the cached sizes must be updated
    const horizontalTabs = this.tabArea.position === SimpleTabArea.Position.TOP || this.tabArea.position === SimpleTabArea.Position.BOTTOM;
    if (this._horizontalTabs !== horizontalTabs) {
      this.tabSize = null;
      this.tabMinSize = null;
      this.overflowTabItemSize = null;
      this._horizontalTabs = horizontalTabs;
    }
    if (this.tabSize != null && this.tabMinSize != null && this.overflowTabItemSize != null) {
      return;
    }
    let $tab = this.tabArea.$container.children('.simple-tab').eq(0);
    if ($tab.length === 0) {
      return;
    }
    $tab = $tab.clone().addClass('selected'); // Non selected items have a margin, selected ones don't -> we need to get the width incl. margin
    let tabAreaClasses = this.tabArea.$container.attr('class');
    let tabItemClasses = $tab.attr('class');
    this._initTabSize([tabAreaClasses, tabItemClasses], this._horizontalTabs);
    this._initTabMinSize([tabAreaClasses, tabItemClasses], this._horizontalTabs);
    this._initOverflowTabItemSize([tabAreaClasses, 'simple-overflow-tab-item'], this._horizontalTabs);
  }

  protected _initTabSize(cssClasses: string[], horizontal: boolean) {
    if (this.tabSize !== null) {
      return;
    }
    this.tabSize = horizontal ? styles.getSize(cssClasses, 'width', 'width', 0) : styles.getSize(cssClasses, 'height', 'height', 0);
  }

  protected _initTabMinSize(cssClasses: string[], horizontal: boolean) {
    if (this.tabMinSize !== null) {
      return;
    }
    this.tabMinSize = horizontal ? styles.getSize(cssClasses, 'min-width', 'minWidth') : styles.getSize(cssClasses, 'min-height', 'minHeight');
  }

  protected _initOverflowTabItemSize(cssClasses: string[], horizontal: boolean) {
    if (this.overflowTabItemSize !== null) {
      return;
    }
    this.overflowTabItemSize = horizontal
      ? styles.getSize(cssClasses, 'min-width', 'minWidth') + styles.getSize(cssClasses, 'margin-left', 'marginLeft') + styles.getSize(cssClasses, 'margin-right', 'marginRight')
      : styles.getSize(cssClasses, 'min-height', 'minHeight') + styles.getSize(cssClasses, 'margin-top', 'marginTop') + styles.getSize(cssClasses, 'margin-bottom', 'marginBottom');
  }

  protected _onOverflowTabItemMouseDown(event: JQuery.MouseDownEvent) {
    let tabArea = this.tabArea;
    let overflowMenus = [];
    let $overflowTabItem = $(event.currentTarget);
    if ($overflowTabItem.data('popup')) {
      $overflowTabItem.data('popup').close();
      return;
    }
    this._overflowTabsIndizes.forEach(i => {
      let tab = this.tabArea.getTabs()[i];
      let menu = scout.create(Menu, {
        parent: this.tabArea,
        text: tab.getMenuText()
      });
      menu.on('action', function() {
        $.log.isDebugEnabled() && $.log.debug('(SimpleTabAreaLayout#_onMouseDownOverflow) tab=' + this);
        tabArea.selectTab(this);
      }.bind(tab));
      overflowMenus.push(menu);
    });

    let popup = scout.create(ContextMenuPopup, {
      parent: this.tabArea,
      menuItems: overflowMenus,
      cloneMenuItems: false,
      $anchor: $overflowTabItem,
      closeOnAnchorMouseDown: false
    });
    $overflowTabItem.addClass('selected');
    $overflowTabItem.data('popup', popup);
    popup.one('remove', () => {
      $overflowTabItem.removeClass('selected');
      $overflowTabItem.data('popup', null);
    });
    popup.open();
  }
}
