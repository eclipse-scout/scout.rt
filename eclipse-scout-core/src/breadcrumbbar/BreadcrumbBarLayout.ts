/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, arrays, BreadcrumbBar, BreadcrumbItem, Dimension, graphics, HtmlCompPrefSizeOptions} from '../index';

export class BreadcrumbBarLayout extends AbstractLayout {
  protected _breadcrumbBar: BreadcrumbBar;

  constructor(breadcrumbBar: BreadcrumbBar) {
    super();
    this._breadcrumbBar = breadcrumbBar;
  }

  override layout($container: JQuery) {
    // 1) undo all shrinking etc.
    this._undoCollapse();

    const breadcrumbItems = this._visibleBreadcrumbItems();

    const htmlContainer = this._breadcrumbBar.htmlComp;
    const containerSize = htmlContainer.size();
    const breadcrumbItemsWidth = this._actualPrefSize(breadcrumbItems, false).width;
    if (breadcrumbItemsWidth <= containerSize.width) {
      this._applyToEllipsis(ell => ell.setVisible(false));
      // OK, every breadcrumbItems fits into container
      return;
    }
    this._applyToEllipsis(ell => ell.setVisible(true));

    // breadcrumbItems don't fit
    // Third approach: Create ellipsis and move overflown menus into it
    this._collapse(containerSize);
  }

  protected _collapse(containerSize: Dimension) {
    let currentIndex = 1;
    const visibleBreadcrumbItems = this._visibleBreadcrumbItems();
    let prefSize = this._actualPrefSize(visibleBreadcrumbItems, true);

    while (prefSize.width > containerSize.width && currentIndex < visibleBreadcrumbItems.length - 1) {
      // remove breadcrumbItems until size fits or only 2 breadcrumbItems are visible
      const crumb = visibleBreadcrumbItems[currentIndex];
      crumb.$container.hide();
      visibleBreadcrumbItems.splice(currentIndex, 1);
      prefSize = this._actualPrefSize(visibleBreadcrumbItems, true);
    }
  }

  protected _applyToEllipsis(fun: (ellipsisBreadcrumbItem: BreadcrumbItem) => void) {
    if (this._breadcrumbBar.ellipsisBreadcrumbItem) {
      fun(this._breadcrumbBar.ellipsisBreadcrumbItem);
    }
  }

  protected _undoCollapse() {
    arrays.ensure(this._breadcrumbBar.breadcrumbItems).forEach(crumb => {
      crumb.$container.show();
    });
  }

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    this._undoCollapse();
    return this._actualPrefSize();
  }

  protected _visibleBreadcrumbItems(): BreadcrumbItem[] {
    return this._breadcrumbBar.breadcrumbItems.filter(breadcrumb => breadcrumb.visible);
  }

  protected _actualPrefSize(breadcrumbItems?: BreadcrumbItem[], considerEllipsis?: boolean): Dimension {
    breadcrumbItems = breadcrumbItems || this._visibleBreadcrumbItems();

    const breadcrumbItemsWidth = this._breadcrumbItemsWidth(breadcrumbItems, considerEllipsis);
    const prefSize = graphics.prefSize(this._breadcrumbBar.$container, {
      includeMargin: true,
      useCssSize: true
    });
    prefSize.width = breadcrumbItemsWidth + this._breadcrumbBar.htmlComp.insets().horizontal();

    return prefSize;
  }

  /**
   * @returns the preferred width of all breadcrumbItems (plus ellipsis breadcrumb)
   */
  protected _breadcrumbItemsWidth(breadcrumbItems?: BreadcrumbItem[], considerEllipsis?: boolean): number {
    let breadcrumbsWidth = 0;
    breadcrumbItems = breadcrumbItems || this._visibleBreadcrumbItems();
    breadcrumbItems.forEach(breadcrumbItem => {
      if (breadcrumbItem.rendered) {
        breadcrumbsWidth += breadcrumbItem.$container.outerWidth(true);
      }
    });

    if (considerEllipsis && this._breadcrumbBar.ellipsisBreadcrumbItem && this._breadcrumbBar.ellipsisBreadcrumbItem.rendered) {
      breadcrumbsWidth += this._breadcrumbBar.ellipsisBreadcrumbItem.$container.outerWidth(true);
    }
    return breadcrumbsWidth;
  }
}
