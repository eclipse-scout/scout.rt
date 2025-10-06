/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, graphics, PrefSizeOptions, scout, SimpleTabArea, SimpleTabOverflowMenu, styles, widgets} from '../index';
import $ from 'jquery';

export class SimpleTabAreaLayout extends AbstractLayout {
  tabArea: SimpleTabArea;
  tabSize: number;
  tabMinSize: number;
  overflowTabItemSize: number;
  protected _horizontalTabs: boolean;
  protected _overflowTab: SimpleTabOverflowMenu;
  protected _overflowTabIndices: number[];

  constructor(tabArea: SimpleTabArea) {
    super();
    this.tabArea = tabArea;
    this.tabSize = null;
    this.tabMinSize = null;
    this.overflowTabItemSize = null;
    this._horizontalTabs = null;
    this._overflowTabIndices = [];
  }

  override layout($container: JQuery) {
    this._initSizes();

    let htmlContainer = this.tabArea.htmlComp;
    let smallPrefSize = this.smallPrefSize();
    let containerSize = htmlContainer.size({exact: true}).subtract(htmlContainer.insets());

    // Reset tabs
    if (this._overflowTab) {
      this._overflowTab.destroy();
    }
    let tabs = this.tabArea.getVisibleTabs(true);
    tabs.forEach(tab => tab.setOverflown(false));
    this._overflowTabIndices = [];
    widgets.updateFirstLastMarker(tabs);

    // All tabs fit in container -> no overflow menu necessary
    if (this._getSize(smallPrefSize) <= this._getSize(containerSize)) {
      $container.removeClass('overflown');
      this.tabArea._updateTabbableItems();
      return;
    }

    // Not all tabs fit in container -> put tabs into overflow menu
    $container.addClass('overflown');
    this._setSize(containerSize, this._getSize(containerSize) - this.overflowTabItemSize);

    // check how many tabs fit into remaining containerSize.width
    let numVisibleTabs = Math.floor(this._getSize(containerSize) / this.tabMinSize);

    let selectedIndex = 0;
    tabs.forEach((tab, i) => {
      if (tab.$container.isSelected()) {
        selectedIndex = i;
      }
    });

    // determine visible range
    let numTabs = tabs.length;
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

    tabs.forEach((tab, i) => {
      if (i < leftEnd || i > rightEnd) {
        tab.setOverflown(true);
        this._overflowTabIndices.push(i);
      }
    });

    this._overflowTab = scout.create(SimpleTabOverflowMenu, {
      parent: this.tabArea,
      tooltipText: '${textKey:ui.MoreTabs}',
      overflowTabIndices: this._overflowTabIndices
    });
    this._overflowTab.render(htmlContainer.$comp);

    widgets.updateFirstLastMarker(this.tabArea.getVisibleTabs());
    this.tabArea._updateTabbableItems();
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
}
