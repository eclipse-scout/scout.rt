/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TableLayout = function(table) {
  scout.TableLayout.parent.call(this);
  this.table = table;
};
scout.inherits(scout.TableLayout, scout.AbstractLayout);

scout.TableLayout.prototype.layout = function($container) {
  var menuBarSize,
    menuBar = this.table.menuBar,
    footer = this.table.footer,
    header = this.table.header,
    $data = this.table.$data,
    lastColumn = this.table.columns[this.table.columns.length - 1],
    height = 0,
    htmlMenuBar = scout.HtmlComponent.get(menuBar.$container),
    htmlContainer = this.table.htmlComp,
    containerSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets());

  if (menuBar.visible) {
    menuBarSize = scout.MenuBarLayout.size(htmlMenuBar, containerSize);
    htmlMenuBar.setSize(menuBarSize);
    height += menuBarSize.height;
  }
  if (footer) {
    // Layout table footer and add size of footer (including the control content) to 'height'
    footer.htmlComp.revalidateLayout();
    height += scout.graphics.getSize(footer.$container).height;
    height += scout.graphics.getSize(footer.$controlContainer).height;
  }
  if (header) {
    height += scout.graphics.getSize(header.$container).height;
  }
  var dataMargins = scout.graphics.getMargins($data);
  height += dataMargins.top + dataMargins.bottom;
  $data.css('height', 'calc(100% - ' + height + 'px)');

  if (this.table.autoResizeColumns) {
    this._layoutColumns();
  }

  // Size of last column may have to be adjusted due to the header menu items
  if (header) {
    header.resizeHeaderItem(lastColumn);
  }

  this.table.setViewRangeSize(this.table.calculateViewRangeSize());
  // Always render viewport (not only when viewRangeSize changes), because view range depends on scroll position and data height
  this.table._renderViewport();

  // Make sure tooltips and editor popup are at correct position after layouting (e.g after window resizing)
  this.table.tooltips.forEach(function(tooltip) {
    tooltip.position();
  }.bind(this));
  if (this.table.cellEditorPopup && this.table.cellEditorPopup.rendered) {
    this.table.cellEditorPopup.position();
    this.table.cellEditorPopup.pack();
  }
  scout.scrollbars.update(this.table.$data);
};

/**
 * Resizes the columns to make them use all the available space.
 */
scout.TableLayout.prototype._layoutColumns = function() {
  var newWidth, weight,
    relevantColumns = [],
    currentWidth = 0,
    totalInitialWidth = 0,
    availableWidth = Math.floor(this.table.$data.width() - this.table.rowBorderWidth);

  // Handle fixed columns
  this.table.columns.forEach(function(column) {
    if (column.fixedWidth) {
      availableWidth -= column.width;
    } else {
      relevantColumns.push(column);
      currentWidth += column.width;
      totalInitialWidth += column.initialWidth;
    }
  }.bind(this));

  if (availableWidth === currentWidth) {
    // Columns already use the available space, no need to resize
    return;
  }

  var remainingWidth = availableWidth;

  // First, filter columns which would get smaller than their minimal size
  var minWidthColumns = relevantColumns.filter(function(column) {
    if (totalInitialWidth === 0) {
      weight = 1 / relevantColumns.length;
    } else {
      weight = column.initialWidth / totalInitialWidth;
    }
    newWidth = Math.floor(weight * remainingWidth);
    if (newWidth < column.minWidth) {
      newWidth = column.minWidth;
      remainingWidth = Math.max(remainingWidth - newWidth, 0);
      return true;
    }
    return false;
  }.bind(this));
  // Resize them to their minimal width
  minWidthColumns.forEach(function(column, index) {
    scout.arrays.remove(relevantColumns, column);

    newWidth = column.minWidth;
    totalInitialWidth -= column.initialWidth;
    // If this is the last column, add remaining space (due to rounding) to this column
    if (index === minWidthColumns.length - 1 && remainingWidth > 0 && relevantColumns.length === 0) {
      newWidth += remainingWidth;
      remainingWidth = 0;
    }
    if (newWidth !== column.width) {
      this.table.resizeColumn(column, newWidth);
    }
  }.bind(this));

  // Then resize the others
  availableWidth = remainingWidth;
  relevantColumns.forEach(function(column, index) {
    if (totalInitialWidth === 0) {
      weight = 1 / relevantColumns.length;
    } else {
      weight = column.initialWidth / totalInitialWidth;
    }
    newWidth = Math.floor(weight * availableWidth);
    remainingWidth -= newWidth;
    // If this is the last column, add remaining space (due to rounding) to this column
    if (index === relevantColumns.length - 1 && remainingWidth > 0) {
      newWidth += remainingWidth;
      remainingWidth = 0;
    }
    if (newWidth !== column.width) {
      this.table.resizeColumn(column, newWidth);
    }
  }.bind(this));
};
