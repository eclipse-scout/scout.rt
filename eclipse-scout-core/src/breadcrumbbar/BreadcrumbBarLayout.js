/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, arrays, graphics} from '../index';

export default class BreadcrumbBarLayout extends AbstractLayout {

  constructor(breadcrumbBar) {
    super();

    this._breadcrumbBar = breadcrumbBar;
  }

  layout($container) {
    // 1) undo all shrinking etc.
    this._undoCollapse(this._breadcrumbBar.breadcrumbItems);

    const breadcrumbItems = this._visibleBreadcrumbItems();

    const htmlContainer = this._breadcrumbBar.htmlComp;
    const containerSize = htmlContainer.size();
    const breadcrumbItemsWidth = this._actualPrefSize(breadcrumbItems, false).width;
    if (breadcrumbItemsWidth <= containerSize.width) {
      this._applyToEllipsis(ell => {
        ell.setVisible(false);
      });
      // OK, every breadcrumbItems fits into container
      return;
    }
    this._applyToEllipsis(ell => {
      ell.setVisible(true);
    });

    // breadcrumbItems don't fit

    // Third approach: Create ellipsis and move overflown menus into it
    this._collapse(breadcrumbItems, containerSize);
  }

  _collapse($container, containerSize) {
    let currentIndex = 1;
    const visibleBreadcrumbItems = this._visibleBreadcrumbItems();
    let prefSize = this._actualPrefSize(visibleBreadcrumbItems, true);

    while (prefSize.width > containerSize.width && currentIndex < visibleBreadcrumbItems.length - 1) {
      // remove breadcrumbItems until size fits or only 2 breadcrumbItems are visible
      const crumb = visibleBreadcrumbItems[currentIndex];
      crumb.$container.hide();
      crumb._layHidden = true;
      visibleBreadcrumbItems.splice(currentIndex, 1);
      prefSize = this._actualPrefSize(visibleBreadcrumbItems, true);
    }
  }

  _applyToEllipsis(fun) {
    if (this._breadcrumbBar._ellipsisBreadcrumbItem) {
      fun(this._breadcrumbBar._ellipsisBreadcrumbItem);
    }
  }

  _undoCollapse() {
    arrays.ensure(this._breadcrumbBar.breadcrumbItems).forEach(crumb => {
      crumb.$container.show();
      crumb._layHidden = false;
    });
  }

  preferredLayoutSize($container) {
    const breadcrumbItems = this._visibleBreadcrumbItems();

    this._undoCollapse(breadcrumbItems);

    return this._actualPrefSize();
  }

  _breadcrumbSize(breadcrumbItem) {
    const classList = breadcrumbItem.$container.attr('class');
    breadcrumbItem.$container.removeClass('hidden');

    breadcrumbItem.htmlComp.invalidateLayout();
    const prefSize = breadcrumbItem.htmlComp.prefSize({
      useCssSize: true,
      exact: true
    }).add(graphics.margins(breadcrumbItem.$container));

    breadcrumbItem.$container.attrOrRemove('class', classList);
    return prefSize;
  }

  _visibleBreadcrumbItems() {
    return this._breadcrumbBar.breadcrumbItems.filter(breadcrumb => {
      return breadcrumb.visible;
    }, this);
  }

  _actualPrefSize(breadcrumbItems, considerEllipsis) {
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
   * @return {number} the preferred width of all breadcrumbItems (plus ellipsis breadcrumb)
   */
  _breadcrumbItemsWidth(breadcrumbItems, considerEllipsis) {
    let breadcrumbsWidth = 0;
    breadcrumbItems = breadcrumbItems || this._visibleBreadcrumbItems();
    breadcrumbItems.forEach(breadcrumbItem => {
      if (breadcrumbItem.rendered) {
        breadcrumbsWidth += breadcrumbItem.$container.outerWidth(true);
      }
    }, this);

    if (considerEllipsis && this._breadcrumbBar._ellipsisBreadcrumbItem && this._breadcrumbBar._ellipsisBreadcrumbItem.rendered) {
      breadcrumbsWidth += this._breadcrumbBar._ellipsisBreadcrumbItem.$container.outerWidth(true);
    }
    return breadcrumbsWidth;
  }
}
