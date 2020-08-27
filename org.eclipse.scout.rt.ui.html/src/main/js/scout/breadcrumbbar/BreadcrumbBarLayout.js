/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.BreadcrumbBarLayout = function(breadcrumbBar) {
  scout.BreadcrumbBarLayout.parent.call(this);

  this._breadcrumbBar = breadcrumbBar;
};
scout.inherits(scout.BreadcrumbBarLayout, scout.AbstractLayout);

scout.BreadcrumbBarLayout.prototype.layout = function($container) {
  // 1) undo all shrinking etc.
  this._undoCollapse(this._breadcrumbBar.breadcrumbItems);

  var breadcrumbItems = this._visibleBreadcrumbItems();

  var htmlContainer = this._breadcrumbBar.htmlComp;
  var containerSize = htmlContainer.size();
  var breadcrumbItemsWidth = this._actualPrefSize(breadcrumbItems, false).width;
  if (breadcrumbItemsWidth <= containerSize.width) {
    this._applyToEllipsis(function(ell) {
      ell.setVisible(false);
    });
    // OK, every breadcrumbItems fits into container
    return;
  }
  this._applyToEllipsis(function(ell) {
    ell.setVisible(true);
  });

  // breadcrumbItems don't fit

  // Third approach: Create ellipsis and move overflown menus into it
  this._collapse(breadcrumbItems, containerSize);
};
scout.BreadcrumbBarLayout.prototype._collapse = function($container, containerSize) {
  var currentIndex = 1;
  var visibleBreadcrumbItems = this._visibleBreadcrumbItems();
  var prefSize = this._actualPrefSize(visibleBreadcrumbItems, true);

  while (prefSize.width > containerSize.width && currentIndex < visibleBreadcrumbItems.length - 1) {
    // remove breadcrumbItems until size fits or only 2 breadcrumbItems are visible
    var crumb = visibleBreadcrumbItems[currentIndex];
    crumb.$container.hide();
    crumb._layHidden = true;
    visibleBreadcrumbItems.splice(currentIndex, 1);
    prefSize = this._actualPrefSize(visibleBreadcrumbItems, true);
  }
};

scout.BreadcrumbBarLayout.prototype._applyToEllipsis = function(fun) {
  if (this._breadcrumbBar._ellipsisBreadcrumbItem) {
    fun(this._breadcrumbBar._ellipsisBreadcrumbItem);
  }
};

scout.BreadcrumbBarLayout.prototype._undoCollapse = function() {
  scout.arrays.ensure(this._breadcrumbBar.breadcrumbItems).forEach(function(crumb) {
    crumb.$container.show();
    crumb._layHidden = false;
  });
};

scout.BreadcrumbBarLayout.prototype.preferredLayoutSize = function($container) {
  var breadcrumbItems = this._visibleBreadcrumbItems();

  this._undoCollapse(breadcrumbItems);

  return this._actualPrefSize();
};

scout.BreadcrumbBarLayout.prototype._breadcrumbSize = function(breadcrumbItem) {
  var classList = breadcrumbItem.$container.attr('class');
  breadcrumbItem.$container.removeClass('hidden');

  breadcrumbItem.htmlComp.invalidateLayout();
  var prefSize = breadcrumbItem.htmlComp.prefSize({
    useCssSize: true,
    exact: true
  }).add(scout.graphics.margins(breadcrumbItem.$container));

  breadcrumbItem.$container.attrOrRemove('class', classList);
  return prefSize;
};

scout.BreadcrumbBarLayout.prototype._visibleBreadcrumbItems = function() {
  return this._breadcrumbBar.breadcrumbItems.filter(function(breadcrumb) {
    return breadcrumb.visible;
  }, this);
};

scout.BreadcrumbBarLayout.prototype._actualPrefSize = function(breadcrumbItems, considerEllipsis) {
  breadcrumbItems = breadcrumbItems || this._visibleBreadcrumbItems();

  var breadcrumbItemsWidth = this._breadcrumbItemsWidth(breadcrumbItems, considerEllipsis);
  var prefSize = scout.graphics.prefSize(this._breadcrumbBar.$container, {
    includeMargin: true,
    useCssSize: true
  });
  prefSize.width = breadcrumbItemsWidth + this._breadcrumbBar.htmlComp.insets().horizontal();

  return prefSize;
};

/**
 * @return the preferred width of all breadcrumbItems (plus ellipsis breadcrumb)
 */
scout.BreadcrumbBarLayout.prototype._breadcrumbItemsWidth = function(breadcrumbItems, considerEllipsis) {
  var breadcrumbsWidth = 0;
  breadcrumbItems = breadcrumbItems || this._visibleBreadcrumbItems();
  breadcrumbItems.forEach(function(breadcrumbItem) {
    if (breadcrumbItem.rendered) {
      breadcrumbsWidth += breadcrumbItem.$container.outerWidth(true);
    }
  }, this);

  if (considerEllipsis && this._breadcrumbBar._ellipsisBreadcrumbItem) {
    breadcrumbsWidth += this._breadcrumbBar._ellipsisBreadcrumbItem.$container.outerWidth(true);
  }
  return breadcrumbsWidth;
};
