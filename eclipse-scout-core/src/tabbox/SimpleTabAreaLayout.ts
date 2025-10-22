/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
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
  protected _$overflowTab: JQuery;
  protected _overflowTabsIndizes: number[];

  constructor(tabArea: SimpleTabArea) {
    super();
    this.tabArea = tabArea;
    this._$overflowTab = null;
    this._overflowTabsIndizes = [];

    this.tabSize = null;
    this.tabMinSize = null;
    this.overflowTabItemSize = null;
  }

  protected _hasHorizontalTabs(): boolean {
    return this.tabArea.position === 'top' || this.tabArea.position === 'bottom';
  }

  override layout($container: JQuery) {
    let getDimension: (object: { width: number; height: number }) => number;
    let setDimension: (object: { width: number; height: number }, v: number) => void;

    if (this._hasHorizontalTabs()) {
      getDimension = object => object.width;
      setDimension = (object, width) => {
        object.width = width;
      };
    } else {
      getDimension = object => object.height;
      setDimension = (object, height) => {
        object.height = height;
      };
    }

    this._layoutImpl($container, getDimension, setDimension);
  }

  protected _layoutImpl($container: JQuery, getDimension: (o: { width: number; height: number }) => number, setDimension: (o: { width: number; height: number }, v: number) => void) {
    let htmlContainer = this.tabArea.htmlComp,
      containerSize = htmlContainer.size({
        exact: true
      }),
      $tabs = htmlContainer.$comp.children('.simple-tab'),
      numTabs = this.tabArea.getTabs().length,
      smallPrefSize = this.smallPrefSize();

    containerSize = containerSize.subtract(htmlContainer.insets());

    this._initSizes();

    // Reset tabs
    if (this._$overflowTab) {
      this._$overflowTab.remove();
    }
    $tabs.setVisible(true);
    this._overflowTabsIndizes = [];
    widgets.updateFirstLastMarker(this.tabArea.getTabs());

    // All tabs fit in container -> no overflow menu necessary
    if (getDimension(smallPrefSize) <= getDimension(containerSize)) {
      $container.removeClass('overflown');
      return;
    }

    // Not all tabs fit in container -> put tabs into overflow menu
    $container.addClass('overflown');
    setDimension(containerSize, getDimension(containerSize) - this.overflowTabItemSize);

    // check how many tabs fit into remaining containerSize.width or containerSize.height
    let numVisibleTabs = Math.floor(getDimension(containerSize) / this.tabMinSize);
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

  smallPrefSize(options: PrefSizeOptions & { tabMinSize?: number } = {}): Dimension {
    this._initSizes();
    options = $.extend({tabMinSize: this.tabMinSize}, options);
    return this.preferredLayoutSize(this.tabArea.$container, options);
  }

  override preferredLayoutSize($container: JQuery, options?: PrefSizeOptions & { tabMinSize?: number }): Dimension {
    this._initSizes();
    let getDimensionHint: (options: PrefSizeOptions) => number;
    let createDimension: (prefSize: Dimension, minSize: number) => Dimension;

    if (this._hasHorizontalTabs()) {
      getDimensionHint = options => options.widthHint;
      createDimension = (prefSize, minWidth) => new Dimension(minWidth, prefSize.height);
    } else {
      getDimensionHint = options => options.heightHint;
      createDimension = (prefSize, minHeight) => new Dimension(prefSize.width, minHeight);
    }

    return this._preferredLayoutSize($container, options, getDimensionHint, createDimension);
  }

  protected _preferredLayoutSize($container: JQuery, options: PrefSizeOptions & { tabMinSize?: number }, getDimensionHint: (o: PrefSizeOptions) => number, createDimension: (prefSize: Dimension, minSize: number) => Dimension): Dimension {
    let tabMinSize = scout.nvl(options.tabMinSize, 0) || scout.nvl(this.tabSize, 0);
    let numTabs = this.tabArea.getTabs().length;
    let minSize = numTabs * tabMinSize;
    options = $.extend({useCssSize: true}, options);
    let prefSize = graphics.prefSize(this.tabArea.$container, options);
    let sizeHint = getDimensionHint(options);
    if (sizeHint && this.tabArea.displayStyle === SimpleTabArea.DisplayStyle.SPREAD_EVEN) {
      minSize = Math.max(sizeHint, minSize);
    }
    return createDimension(prefSize, minSize);
  }

  /**
   * Reads the default sizes from CSS -> the tabs need to specify a width and a min-width or a height and a min-height.
   * The layout expects all tabs to have the same width.
   */
  protected _initSizes() {
    let getStylesTabSize: (cssClasses: string[]) => number;
    let getStylesTabMinSize: (cssClasses: string[]) => number;
    let getStylesOverflowTabItemSize: (cssClasses: string[]) => number;

    if (this._hasHorizontalTabs()) {
      getStylesTabSize = cssClasses => styles.getSize(cssClasses, 'width', 'width', 0);
      getStylesTabMinSize = cssClasses => styles.getSize(cssClasses, 'min-width', 'minWidth');
      getStylesOverflowTabItemSize = cssClasses => styles.getSize(cssClasses, 'min-width', 'minWidth') + styles.getSize(cssClasses, 'margin-left', 'marginLeft') + styles.getSize(cssClasses, 'margin-right', 'marginRight');
    } else {
      getStylesTabSize = cssClasses => styles.getSize(cssClasses, 'height', 'height', 0);
      getStylesTabMinSize = cssClasses => styles.getSize(cssClasses, 'min-height', 'minHeight');
      getStylesOverflowTabItemSize = cssClasses => styles.getSize(cssClasses, 'min-height', 'minHeight') + styles.getSize(cssClasses, 'margin-top', 'marginTop') + styles.getSize(cssClasses, 'margin-bottom', 'marginBottom');
    }

    this._initSizesImpl(getStylesTabSize, getStylesTabMinSize, getStylesOverflowTabItemSize);
  }

  protected _initSizesImpl(getStylesTabSize: (cssClasses: string[]) => number, getStylesTabMinSize: (cssClasses: string[]) => number, getStylesOverflowTabItemSize: (cssClasses: string[]) => number) {
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
    if (this.tabSize === null) {
      this.tabSize = getStylesTabSize([tabAreaClasses, tabItemClasses]);
    }
    if (this.tabMinSize === null) {
      this.tabMinSize = getStylesTabMinSize([tabAreaClasses, tabItemClasses]);
    }
    if (this.overflowTabItemSize === null) {
      this.overflowTabItemSize = getStylesOverflowTabItemSize([tabAreaClasses, 'simple-overflow-tab-item']);
    }
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
